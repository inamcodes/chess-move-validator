# Contributing to Chess Move Validator

Thank you for your interest in contributing! This document explains how to get started, what the codebase expects, and how to submit good pull requests.

---

## Table of Contents

- [Ways to Contribute](#ways-to-contribute)
- [Good First Issues](#good-first-issues)
- [Getting Started](#getting-started)
- [Code Style](#code-style)
- [Design Principles](#design-principles)
- [Submitting a Pull Request](#submitting-a-pull-request)
- [Reporting Bugs](#reporting-bugs)
- [Suggesting Features](#suggesting-features)
- [License](#license)

---

## Ways to Contribute

- Fix a bug or edge case in move validation
- Implement one of the known missing features (check detection, promotion, etc.)
- Improve Javadoc or inline comments
- Add unit tests
- Improve the README or this file

All contributions are welcome regardless of experience level.

---

## Good First Issues

These missing features are well-scoped and clearly documented in the source. They are great starting points:

### 1. Check Detection
`MoveValidator` does not yet reject moves that leave the moving king in check. The approach is:
- Add a private `leavesKingInCheck(List<Piece> pieces, Piece moving, int toRow, int toCol)` method.
- Deep-copy the board state (or apply the move temporarily), find the moving side's king, then check whether any opponent piece can reach it.
- Call this at the end of each `validateXxx` method before returning a legal result.

### 2. Castling Through Check
Castling is currently blocked if squares between king and rook are occupied, but not if those squares are under attack. Add attacked-square detection to `tryCastle()` — the king must not pass through or land on a square controlled by an enemy piece.

### 3. Pawn Promotion
When a pawn reaches row 0 (Black's pawn) or row 7 (White's pawn), it should be replaced by a Queen, Rook, Bishop, or Knight of the same color. This is currently handled outside the validator. Consider adding a `isPromotion()` flag to `MoveResult` so callers can respond to it cleanly.

### 4. Checkmate and Stalemate Detection
After every move, the game controller needs to know if the opponent has any legal moves left. A helper that iterates all opponent pieces and calls `MoveValidator.validate()` for every reachable square would do the job.

### 5. Unit Tests
There are currently no automated tests. Adding a JUnit 5 test class for `MoveValidator` — covering normal moves, illegal moves, castling, and en passant for each piece type — would be a huge help.

---

## Getting Started

### 1. Fork and clone

```bash
git clone https://github.com/<your-username>/chess-move-validator.git
cd chess-move-validator
```

### 2. Compile

```bash
javac *.java
```

### 3. Make your changes

Work in a dedicated branch named after what you're doing:

```bash
git checkout -b feature/check-detection
git checkout -b fix/en-passant-edge-case
git checkout -b docs/improve-readme
```

### 4. Test manually (or write automated tests)

If you are adding JUnit 5 tests, place them in a `test/` directory and compile with:

```bash
javac -cp .:junit-platform-console-standalone.jar test/*.java
java -jar junit-platform-console-standalone.jar --class-path . --scan-class-path
```

### 5. Commit and push

```bash
git add .
git commit -m "feat: add check detection to MoveValidator"
git push origin feature/check-detection
```

---

## Code Style

This project follows the conventions already established in the codebase. Please match them:

**Naming**
- Classes: `PascalCase` — `MoveValidator`, `MoveResult`
- Methods and variables: `camelCase` — `validateKing`, `lastDoublePushedPawn`
- Constants: `UPPER_SNAKE_CASE` — `SQUARE_SIZE`, `ILLEGAL`

**Formatting**
- 4-space indentation, no tabs
- Opening braces on the same line
- One blank line between methods
- Keep lines under ~100 characters

**Javadoc**
- Every public class and method must have a Javadoc comment
- Use `@param`, `@return`, and `@throws` where applicable
- Private helpers should have at minimum a one-line comment explaining their purpose

**Design rules — please read carefully**

- `MoveValidator.validate()` must remain **stateless**. It must never mutate any piece or list passed to it.
- Return `MoveResult` factory values (`MoveResult.normal()`, `MoveResult.illegal()`, etc.) — do not add new boolean flags or return types without discussion.
- New piece types must extend `Piece` and set `name` and `image` in the constructor.
- Avoid adding external dependencies. The library currently has zero.

---

## Design Principles

Understanding these will help you write changes that fit naturally:

**No side effects in validation.** The validator reads board state but never writes it. All outcomes are expressed through the returned `MoveResult`. The caller decides what to apply.

**One method per responsibility.** Each piece type and each special rule has its own private method. Keep that separation. If a method is growing large, split it.

**Explicit inputs.** Every piece of context needed for a decision is passed as a parameter — not stored in a field. This makes the validator trivially thread-safe and easy to test.

**Factory methods on MoveResult.** Use `MoveResult.illegal()`, `MoveResult.normal()`, `MoveResult.enPassant(pawn)`, and `MoveResult.castling(side)`. If you genuinely need a new outcome type, add a factory method and a corresponding accessor — do not break the existing API.

---

## Submitting a Pull Request

1. Make sure your branch is up to date with `main` before opening a PR.
2. Fill in the PR description:
   - **What** does this change?
   - **Why** is it needed?
   - **How** was it tested?
3. Keep PRs focused. One feature or fix per PR — don't bundle unrelated changes.
4. If your PR addresses a known limitation listed in the README, mention it explicitly.
5. Be responsive to review comments. PRs that go stale for more than 30 days may be closed.

---

## Reporting Bugs

Open a GitHub Issue with:

- A clear title (e.g. "En passant not detected when pawn is on a-file")
- The exact board state that triggers the bug (piece positions, whose turn, last move)
- Expected behavior vs actual behavior
- Java version you are running

---

## Suggesting Features

Open a GitHub Issue tagged `enhancement`. Describe:

- What you want to add and why
- Whether it fits within the current design (stateless validator, no external deps)
- Any alternative approaches you considered

For larger changes, open an issue first before writing code so we can agree on the approach.

---

## License

By contributing, you agree that your contributions will be licensed under the same [MIT License](LICENSE) that covers this project.
