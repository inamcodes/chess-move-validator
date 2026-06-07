# Chess Move Validator

<img width="1080" height="360" alt="image" src="https://github.com/user-attachments/assets/34dd1c80-0de6-4af7-a025-15a87538383f" />
A pure Java library that validates chess moves for all piece types according to standard FIDE rules. It is <b>stateless and side-effect free</b> — it never mutates board or piece state, making it safe to embed in any chess engine, GUI, or game server.

---

## Table of Contents

- [Features](#features)
- [Project Structure](#project-structure)
- [Board Coordinate System](#board-coordinate-system)
- [Setup](#setup)
- [Usage Guide](#usage-guide)
  - [Creating Pieces](#creating-pieces)
  - [Validating a Move](#validating-a-move)
  - [Reading the Result](#reading-the-result)
  - [Handling Special Moves](#handling-special-moves)
- [API Reference](#api-reference)
  - [MoveValidator](#movevalidator)
  - [MoveResult](#moveresult)
  - [Piece](#piece)
  - [Color](#color)
- [Known Limitations](#known-limitations)
- [License](#license)

---

## Features

- Validates legal moves for all six piece types: King, Queen, Rook, Bishop, Knight, Pawn
- Castling (king-side and queen-side) with full eligibility checks
- En passant capture with pinpoint correctness (only the pawn that just double-pushed is eligible)
- Friendly-fire blocking (cannot move to a square occupied by your own piece)
- Path obstruction checks for sliding pieces (Queen, Rook, Bishop)
- Immutable `MoveResult` return value — no shared mutable state
- Zero external dependencies

---

## Project Structure

```
chess-move-validator/
├── MoveValidator.java   # Core validation logic (public API entry point)
├── MoveResult.java      # Immutable result object returned by validate()
├── Piece.java           # Abstract base class for all pieces
├── Color.java           # Enum: WHITE / BLACK
├── King.java
├── Queen.java
├── Rook.java
├── Bishop.java
├── Knight.java
├── Pawn.java
└── pieces/
    └── img/             # PNG images for each piece (loaded at construction)
        ├── wKing.png
        ├── bKing.png
        └── ...          # wQueen, bQueen, wRook, bRook, wBishop, bBishop,
                         # wKnight, bKnight, wPawn, bPawn
```

---

## Board Coordinate System

The library uses a row/column system where the origin is the **top-left** of the board (Black's back rank):

| Logical position | Row | Col |
|-----------------|-----|-----|
| Black's back rank (rank 8) | 0 | — |
| White's back rank (rank 1) | 7 | — |
| a-file | — | 0 |
| h-file | — | 7 |

```
     a    b    c    d    e    f    g    h
   +----+----+----+----+----+----+----+----+
8  | 0,0| 0,1| 0,2| 0,3| 0,4| 0,5| 0,6| 0,7|  ← Black back rank (row 0)
7  | 1,0| ...                           | 1,7|
   | ...                                     |
1  | 7,0| 7,1| 7,2| 7,3| 7,4| 7,5| 7,6| 7,7|  ← White back rank (row 7)
   +----+----+----+----+----+----+----+----+
```

---

## Setup

### Requirements

- Java 17 or later (uses switch expressions and records)
- Piece images must exist at `pieces/img/<color><PieceName>.png` relative to the working directory

### Compile

```bash
javac *.java
```

### Run (if you have a main class)

```bash
java YourMainClass
```

> **Image assets:** Each piece constructor loads its PNG from `pieces/img/`. Ensure the image directory is present in your working directory, or the constructor will throw a `RuntimeException`. If you are using the validator in a headless environment (no GUI), you can subclass `Piece` and skip image loading.

---

## Usage Guide

### Creating Pieces

Instantiate pieces by passing `row`, `col`, and a color flag (`0` = White, `1` = Black):

```java
// Standard starting positions
King   whiteKing   = new King  (7, 4, 0);   // e1
Queen  whiteQueen  = new Queen (7, 3, 0);   // d1
Rook   whiteRookA  = new Rook  (7, 0, 0);   // a1
Rook   whiteRookH  = new Rook  (7, 7, 0);   // h1
Bishop whiteBishopC = new Bishop(7, 2, 0);  // c1
Bishop whiteBishopF = new Bishop(7, 5, 0);  // f1
Knight whiteKnightB = new Knight(7, 1, 0);  // b1
Knight whiteKnightG = new Knight(7, 6, 0);  // g1
Pawn   whitePawnE  = new Pawn  (6, 4, 0);   // e2

King   blackKing   = new King  (0, 4, 1);   // e8
Pawn   blackPawnD  = new Pawn  (1, 3, 1);   // d7
```

Collect all pieces into a `List<Piece>`:

```java
List<Piece> pieces = new ArrayList<>(Arrays.asList(
    whiteKing, whiteQueen, whiteRookA, whiteRookH,
    whiteBishopC, whiteBishopF, whiteKnightB, whiteKnightG,
    whitePawnE, blackKing, blackPawnD
));
```

---

### Validating a Move

Call `MoveValidator.validate()` with the full board state, the piece to move, the destination, and the last double-pushed pawn (for en passant):

```java
Pawn lastDoublePushedPawn = null; // null if no pawn double-pushed last turn

MoveResult result = MoveValidator.validate(
    pieces,             // all pieces currently on the board
    whitePawnE,         // the piece the current player wants to move
    4,                  // destination row (rank 5 = row 4 from top)
    4,                  // destination col (e-file = col 4)
    lastDoublePushedPawn
);
```

---

### Reading the Result

`MoveResult` is immutable. Check `isLegal()` first, then handle any special move type:

```java
if (result.isLegal()) {
    // Apply the move — the validator never does this for you
    activePiece.setRow(toRow);
    activePiece.setCol(toCol);

    if (result.isCastling()) {
        // Move the rook to the correct square
        switch (result.getCastlingSide()) {
            case KING_SIDE  -> movRook(pieces, fromRow, 7, fromRow, 5); // h-file → f-file
            case QUEEN_SIDE -> moveRook(pieces, fromRow, 0, fromRow, 3); // a-file → d-file
        }
    }

    if (result.isEnPassant()) {
        // Remove the captured pawn from the board
        Piece capturedPawn = result.getEnPassantPawn();
        pieces.remove(capturedPawn);
    }

    // Track the last double-pushed pawn for next turn's en passant check
    if (activePiece instanceof Pawn) {
        boolean doublePush = Math.abs(toRow - fromRow) == 2;
        lastDoublePushedPawn = doublePush ? (Pawn) activePiece : null;
    } else {
        lastDoublePushedPawn = null;
    }

} else {
    System.out.println("Illegal move!");
}
```

---

### Handling Special Moves

#### Castling

The validator checks all castling preconditions automatically:

- Neither the King nor the target Rook has previously moved (`hasMoved == false`)
- All squares between them are empty
- The King moves from col 4 to col 2 (queen-side) or col 6 (king-side)

> ⚠️ Check detection through castling squares is a **known limitation** — see below.

```java
// Try king-side castling: king at e1 → g1 (row 7, col 4 → col 6)
MoveResult result = MoveValidator.validate(pieces, whiteKing, 7, 6, null);

if (result.isCastling() && result.getCastlingSide() == MoveResult.CastleSide.KING_SIDE) {
    whiteKing.setCol(6);   // king → g1
    whiteRookH.setCol(5);  // rook → f1
}
```

#### En Passant

Pass the pawn that just made a double push as `lastDoublePushedPawn`. The validator checks that:

- The capturing pawn is on the correct rank (row 3 for White, row 4 for Black)
- The double-pushed pawn is directly adjacent
- The destination is the square behind the captured pawn

```java
// Black pawn on d5 just double-pushed (row 3, col 3)
Pawn blackPawnD5 = new Pawn(3, 3, 1);
lastDoublePushedPawn = blackPawnD5;

// White pawn on e5 tries en passant to d6 (row 2, col 3)
MoveResult result = MoveValidator.validate(pieces, whitePawnE5, 2, 3, lastDoublePushedPawn);

if (result.isEnPassant()) {
    whitePawnE5.setRow(2);
    whitePawnE5.setCol(3);
    pieces.remove(result.getEnPassantPawn()); // remove black pawn from d5
}
```

---

## API Reference

### MoveValidator

```java
public static MoveResult validate(
    List<Piece> pieces,
    Piece activePiece,
    int toRow,
    int toCol,
    Pawn lastDoublePushedPawn
)
```

| Parameter | Type | Description |
|-----------|------|-------------|
| `pieces` | `List<Piece>` | All pieces currently on the board. Must not be `null`. |
| `activePiece` | `Piece` | The piece the current player is trying to move. Must not be `null`. |
| `toRow` | `int` | Destination rank index, range `[0–7]`. |
| `toCol` | `int` | Destination file index, range `[0–7]`. |
| `lastDoublePushedPawn` | `Pawn` | The pawn that just advanced two squares, or `null` if none. Used exclusively for en passant eligibility. |

**Returns:** A `MoveResult` — never `null`.

**Throws:** `IllegalArgumentException` if `pieces` or `activePiece` is `null`.

---

### MoveResult

All factory methods and accessors:

| Method | Returns | Description |
|--------|---------|-------------|
| `MoveResult.illegal()` | `MoveResult` | Shared singleton for an illegal move. |
| `MoveResult.normal()` | `MoveResult` | A new result for a standard legal move. |
| `MoveResult.enPassant(Piece)` | `MoveResult` | A result for a legal en passant capture. |
| `MoveResult.castling(CastleSide)` | `MoveResult` | A result for a legal castling move. |
| `isLegal()` | `boolean` | `true` if the move is legal. |
| `isEnPassant()` | `boolean` | `true` if the move is an en passant capture. |
| `getEnPassantPawn()` | `Piece` | The captured pawn (non-null only when `isEnPassant()` is true). |
| `isCastling()` | `boolean` | `true` if the move is a castling move. |
| `getCastlingSide()` | `CastleSide` | `QUEEN_SIDE` or `KING_SIDE` (non-null only when `isCastling()` is true). |

---

### Piece

Abstract base class. Key members:

| Member | Type | Description |
|--------|------|-------------|
| `color` | `Color` | `WHITE` or `BLACK`. Final, set at construction. |
| `name` | `String` | Piece identifier used by the validator (e.g. `"king"`, `"w-pawn"`, `"b-pawn"`). |
| `hasMoved` | `boolean` | Set to `true` automatically by `setRow()` / `setCol()`. Used for castling and pawn double-advance eligibility. |
| `getRow()` | `int` | Current rank index `[0–7]`. |
| `getCol()` | `int` | Current file index `[0–7]`. |
| `setRow(int)` | `void` | Moves the piece to a new rank and sets `hasMoved = true`. |
| `setCol(int)` | `void` | Moves the piece to a new file and sets `hasMoved = true`. |
| `isWhite()` | `boolean` | Returns `true` if the piece is White. |
| `isEnemyOf(Piece)` | `boolean` | Returns `true` if the two pieces have opposite colors. |

---

### Color

```java
Color.WHITE   // light side, moves first, starts on ranks 6–7
Color.BLACK   // dark side, starts on ranks 0–1

Color.fromInt(int isBlack)  // 0 → WHITE, 1 → BLACK
color.opposite()            // WHITE → BLACK, BLACK → WHITE
```

---

## Known Limitations

These features are not yet implemented and are called out in the source:

| Feature | Status |
|---------|--------|
| **Check detection** | Not implemented. Moves that leave the moving king in check are not rejected. |
| **Checkmate / stalemate detection** | Not implemented. |
| **Pawn promotion** | Not implemented inside the validator. Handle in your game controller after a pawn reaches row 0 (Black) or row 7 (White). |
| **50-move rule** | Not implemented. |
| **Threefold repetition** | Not implemented. |
| **Castling through check** | Not detected. The path-clear check is present but attacked-square detection is missing. |

Contributions to address any of these are very welcome — see [CONTRIBUTING.md](CONTRIBUTING.md).

---

## License

MIT License — Copyright (c) 2026 Inamullah Mahar. See [LICENSE](LICENSE) for full terms.
