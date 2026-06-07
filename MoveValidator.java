
import java.util.List;

/**
 * Validates chess moves for all piece types according to standard FIDE rules.
 *
 * <h2>Design principles</h2>
 * <ul>
 * <li><strong>No side effects</strong> — validation never mutates the board or
 * piece state. Results are communicated through {@link MoveResult}.</li>
 * <li><strong>Explicit inputs</strong> — every piece of context needed for
 * validation (active piece, destination, last double-pushed pawn) is passed as
 * a parameter, not read from mutable fields.</li>
 * <li><strong>One responsibility per method</strong> — each piece type and each
 * special rule has its own private method.</li>
 * </ul>
 *
 * <h2>Board coordinate convention</h2>
 * <ul>
 * <li>Row 0 = Black's back rank (rank 8), Row 7 = White's back rank (rank
 * 1)</li>
 * <li>Col 0 = a-file, Col 7 = h-file</li>
 * </ul>
 *
 * <h2>Known limitations (not yet implemented)</h2>
 * <ul>
 * <li>Check detection — moves that leave the moving king in check are not yet
 * rejected. Add a {@code leavesKingInCheck()} helper that applies the move on a
 * copy of the board and tests if the king is attacked.</li>
 * <li>Stalemate and checkmate detection</li>
 * <li>50-move rule and threefold repetition</li>
 * </ul>
 *
 * @see MoveResult
 * @see Piece
 */
public class MoveValidator {

    // ------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------
    /**
     * Validates whether {@code activePiece} can legally move to ({@code toRow},
     * {@code toCol}) given the current board state.
     *
     * <p>
     * The method does <em>not</em> mutate any piece or board state. If the move
     * is legal, the caller is responsible for updating positions and handling
     * any side effects described in the returned {@link MoveResult}.
     *
     * @param pieces all pieces currently on the board; must not be null
     * @param activePiece the piece the current player is trying to move
     * @param toRow destination rank index [0–7]
     * @param toCol destination file index [0–7]
     * @param lastDoublePushedPawn the pawn that advanced two squares on the
     * immediately preceding half-move, or {@code null} if no such pawn exists —
     * used for en passant
     * @return a {@link MoveResult} describing whether the move is legal and
     * what special effect (castling, en passant) it has
     * @throws IllegalArgumentException if {@code pieces} or {@code activePiece}
     * is null
     */
    public MoveResult validate(List<Piece> pieces, Piece activePiece,
            int toRow, int toCol, Pawn lastDoublePushedPawn) {
        if (pieces == null) {
            throw new IllegalArgumentException("pieces must not be null");
        }
        if (activePiece == null) {
            throw new IllegalArgumentException("activePiece must not be null");
        }

        // A piece cannot move to a square occupied by a friendly piece.
        Piece target = getPieceAt(pieces, toRow, toCol);
        if (target != null && !activePiece.isEnemyOf(target)) {
            return MoveResult.illegal();
        }

        return switch (activePiece.name) {
            case "king" ->
                validateKing(pieces, (King) activePiece, toRow, toCol);
            case "queen" ->
                validateQueen(pieces, activePiece, toRow, toCol);
            case "rook" ->
                validateRook(pieces, activePiece, toRow, toCol);
            case "bishop" ->
                validateBishop(pieces, activePiece, toRow, toCol);
            case "knight" ->
                validateKnight(activePiece, toRow, toCol);
            case "w-pawn" ->
                validatePawn(pieces, (Pawn) activePiece, toRow, toCol,
                lastDoublePushedPawn, Color.WHITE);
            case "b-pawn" ->
                validatePawn(pieces, (Pawn) activePiece, toRow, toCol,
                lastDoublePushedPawn, Color.BLACK);
            default ->
                MoveResult.illegal();
        };
    }

    // ------------------------------------------------------------------
    // Per-piece validation
    // ------------------------------------------------------------------
    /**
     * Validates a King move, including castling.
     *
     * <p>
     * Normal king moves cover exactly one square in any direction. Castling is
     * legal when:
     * <ol>
     * <li>Neither the king nor the target rook has previously moved</li>
     * <li>All squares between king and rook are empty</li>
     * <li>The king does not start, pass through, or end on a checked square
     * (check detection is a TODO — see class-level notes)</li>
     * </ol>
     *
     * @param pieces all pieces on the board
     * @param king the king being moved
     * @param toRow destination rank
     * @param toCol destination file
     * @return legal normal, legal castling, or illegal result
     */
    private MoveResult validateKing(List<Piece> pieces, King king,
            int toRow, int toCol) {
        int fromRow = king.getRow();
        int fromCol = king.getCol();

        // Castling — only if the king has never moved
        if (!king.hasMoved) {
            MoveResult castle = tryCastle(pieces, king, toRow, toCol, fromRow, fromCol);
            if (castle.isLegal()) {
                return castle;
            }
        }

        // Normal one-square move in any direction
        int rowDelta = Math.abs(fromRow - toRow);
        int colDelta = Math.abs(fromCol - toCol);
        boolean oneSquareAny = (rowDelta <= 1 && colDelta <= 1)
                && (rowDelta + colDelta > 0); // must actually move
        return oneSquareAny ? MoveResult.normal() : MoveResult.illegal();
    }

    /**
     * Attempts to validate a castling move for the given king.
     *
     * <p>
     * Queen-side castling: king moves from col 4 to col 2 (toCol == 2).
     * King-side castling: king moves from col 4 to col 6 (toCol == 6).
     *
     * @param pieces all pieces on the board
     * @param king the king attempting to castle
     * @param toRow destination rank (must equal fromRow for castling)
     * @param toCol destination file (2 = queen-side, 6 = king-side)
     * @param fromRow king's current rank
     * @param fromCol king's current file (must be 4)
     * @return a castling {@link MoveResult} if legal, illegal otherwise
     */
    private MoveResult tryCastle(List<Piece> pieces, King king,
            int toRow, int toCol,
            int fromRow, int fromCol) {
        // King must be on its starting file (e-file = col 4)
        if (fromCol != 4 || toRow != fromRow) {
            return MoveResult.illegal();
        }

        if (toCol == 2) {
            // Queen-side: rook must be on col 0
            Piece rook = getPieceAt(pieces, fromRow, 0);
            if (rook instanceof Rook && !rook.hasMoved
                    && isRookPathClear(pieces, fromRow, fromCol, fromRow, 0)) {
                return MoveResult.castling(MoveResult.CastleSide.QUEEN_SIDE);
            }
        } else if (toCol == 6) {
            // King-side: rook must be on col 7
            Piece rook = getPieceAt(pieces, fromRow, 7);
            if (rook instanceof Rook && !rook.hasMoved
                    && isRookPathClear(pieces, fromRow, fromCol, fromRow, 7)) {
                return MoveResult.castling(MoveResult.CastleSide.KING_SIDE);
            }
        }
        return MoveResult.illegal();
    }

    /**
     * Validates a Queen move (bishop-style diagonal OR rook-style straight).
     *
     * @param pieces all pieces on the board
     * @param queen the queen being moved
     * @param toRow destination rank
     * @param toCol destination file
     * @return legal or illegal result
     */
    private MoveResult validateQueen(List<Piece> pieces, Piece queen,
            int toRow, int toCol) {
        boolean diagonal = isDiagonalMove(queen, toRow, toCol)
                && isBishopPathClear(pieces, queen.getRow(), queen.getCol(), toRow, toCol);
        boolean straight = isStraightMove(queen, toRow, toCol)
                && isRookPathClear(pieces, queen.getRow(), queen.getCol(), toRow, toCol);
        return (diagonal || straight) ? MoveResult.normal() : MoveResult.illegal();
    }

    /**
     * Validates a Rook move (horizontal or vertical, path must be clear).
     *
     * @param pieces all pieces on the board
     * @param rook the rook being moved
     * @param toRow destination rank
     * @param toCol destination file
     * @return legal or illegal result
     */
    private MoveResult validateRook(List<Piece> pieces, Piece rook,
            int toRow, int toCol) {
        boolean ok = isStraightMove(rook, toRow, toCol)
                && isRookPathClear(pieces, rook.getRow(), rook.getCol(), toRow, toCol);
        return ok ? MoveResult.normal() : MoveResult.illegal();
    }

    /**
     * Validates a Bishop move (diagonal only, path must be clear).
     *
     * @param pieces all pieces on the board
     * @param bishop the bishop being moved
     * @param toRow destination rank
     * @param toCol destination file
     * @return legal or illegal result
     */
    private MoveResult validateBishop(List<Piece> pieces, Piece bishop,
            int toRow, int toCol) {
        boolean ok = isDiagonalMove(bishop, toRow, toCol)
                && isBishopPathClear(pieces, bishop.getRow(), bishop.getCol(), toRow, toCol);
        return ok ? MoveResult.normal() : MoveResult.illegal();
    }

    /**
     * Validates a Knight move (L-shape: 2×1 or 1×2 squares).
     *
     * <p>
     * Knights jump over other pieces, so no path-clear check is needed. The
     * formula {@code |Δrow| * |Δcol| == 2} is equivalent to checking all 8
     * possible L-shape offsets without enumerating them.
     *
     * @param knight the knight being moved
     * @param toRow destination rank
     * @param toCol destination file
     * @return legal or illegal result
     */
    private MoveResult validateKnight(Piece knight, int toRow, int toCol) {
        int rowDelta = Math.abs(knight.getRow() - toRow);
        int colDelta = Math.abs(knight.getCol() - toCol);
        return (rowDelta * colDelta == 2) ? MoveResult.normal() : MoveResult.illegal();
    }

    /**
     * Validates a Pawn move, including en passant.
     *
     * <p>
     * Movement direction: White pawns move toward row 0 (row delta = -1), Black
     * pawns move toward row 7 (row delta = +1).
     *
     * <p>
     * En passant is legal only when:
     * <ul>
     * <li>The adjacent pawn is exactly {@code lastDoublePushedPawn} — meaning
     * it advanced two squares on the immediately preceding half-move</li>
     * <li>The capturing pawn is on the correct rank (row 3 for White, row 4 for
     * Black)</li>
     * <li>The destination is the square directly behind the captured pawn</li>
     * </ul>
     * Note: the captured pawn is NOT on the destination square — it sits one
     * rank behind {@code toRow} on {@code toCol}. The caller must remove it.
     *
     * @param pieces all pieces on the board
     * @param pawn the pawn being moved
     * @param toRow destination rank
     * @param toCol destination file
     * @param lastDoublePushedPawn the pawn eligible for en passant, or null
     * @param color the moving pawn's color
     * @return legal (normal or en passant) or illegal result
     */
    private MoveResult validatePawn(List<Piece> pieces, Pawn pawn,
            int toRow, int toCol,
            Pawn lastDoublePushedPawn, Color color) {
        int fromRow = pawn.getRow();
        int fromCol = pawn.getCol();

        // Direction: White moves up (decreasing row), Black moves down (increasing row)
        int direction = (color == Color.WHITE) ? -1 : 1;
        int startRow = (color == Color.WHITE) ? 6 : 1;
        int enPassantRow = (color == Color.WHITE) ? 3 : 4;

        Piece targetPiece = getPieceAt(pieces, toRow, toCol);

        // --- En passant capture ---
        // The pawn must be on the correct rank, and the adjacent pawn must be
        // exactly the one that just made a double-push last turn.
        if (fromRow == enPassantRow && lastDoublePushedPawn != null) {
            boolean adjacentLeft = (fromCol - 1 >= 0)
                    && lastDoublePushedPawn == getPieceAt(pieces, fromRow, fromCol - 1);
            boolean adjacentRight = (fromCol + 1 <= 7)
                    && lastDoublePushedPawn == getPieceAt(pieces, fromRow, fromCol + 1);

            if (adjacentLeft && toRow == fromRow + direction && toCol == fromCol - 1) {
                return MoveResult.enPassant(lastDoublePushedPawn);
            }
            if (adjacentRight && toRow == fromRow + direction && toCol == fromCol + 1) {
                return MoveResult.enPassant(lastDoublePushedPawn);
            }
        }

        // --- Double advance from starting rank ---
        // Both the destination square and the intermediate square must be empty.
        boolean intermediateEmpty = getPieceAt(pieces, fromRow + direction, toCol) == null;
        if (fromRow == startRow
                && toCol == fromCol
                && targetPiece == null
                && toRow == fromRow + 2 * direction
                && intermediateEmpty) {
            return MoveResult.normal();
        }

        // --- Single forward advance ---
        if (toCol == fromCol && targetPiece == null && toRow == fromRow + direction) {
            return MoveResult.normal();
        }

        // --- Diagonal capture ---
        boolean isDiagonalCapture = (toRow == fromRow + direction)
                && (Math.abs(toCol - fromCol) == 1);
        if (isDiagonalCapture && targetPiece != null && pawn.isEnemyOf(targetPiece)) {
            return MoveResult.normal();
        }

        return MoveResult.illegal();
    }

    // ------------------------------------------------------------------
    // Movement geometry helpers
    // ------------------------------------------------------------------
    /**
     * Returns {@code true} if the move is strictly diagonal (|Δrow| == |Δcol|
     * and the piece actually moves).
     *
     * @param piece the moving piece
     * @param toRow destination rank
     * @param toCol destination file
     * @return true if the move traces a diagonal line
     */
    private boolean isDiagonalMove(Piece piece, int toRow, int toCol) {
        int rowDelta = Math.abs(piece.getRow() - toRow);
        int colDelta = Math.abs(piece.getCol() - toCol);
        return rowDelta == colDelta && rowDelta > 0;
    }

    /**
     * Returns {@code true} if the move is strictly horizontal or vertical
     * (either row or column stays the same, but not both).
     *
     * @param piece the moving piece
     * @param toRow destination rank
     * @param toCol destination file
     * @return true if the move traces a straight line
     */
    private boolean isStraightMove(Piece piece, int toRow, int toCol) {
        boolean sameRow = piece.getRow() == toRow;
        boolean sameCol = piece.getCol() == toCol;
        return sameRow != sameCol; // exactly one must be true
    }

    /**
     * Returns {@code true} if all squares along a diagonal path are empty.
     *
     * <p>
     * The source and destination squares themselves are not checked — only the
     * squares strictly between them.
     *
     * @param pieces all pieces on the board
     * @param fromRow starting rank
     * @param fromCol starting file
     * @param toRow destination rank
     * @param toCol destination file
     * @return true if the diagonal path is clear
     */
    private boolean isBishopPathClear(List<Piece> pieces,
            int fromRow, int fromCol,
            int toRow, int toCol) {
        int rowStep = toRow > fromRow ? 1 : -1;
        int colStep = toCol > fromCol ? 1 : -1;
        int steps = Math.abs(toRow - fromRow);

        for (int i = 1; i < steps; i++) {
            if (getPieceAt(pieces, fromRow + rowStep * i, fromCol + colStep * i) != null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns {@code true} if all squares along a horizontal or vertical path
     * are empty.
     *
     * <p>
     * The source and destination squares themselves are not checked — only the
     * squares strictly between them.
     *
     * @param pieces all pieces on the board
     * @param fromRow starting rank
     * @param fromCol starting file
     * @param toRow destination rank
     * @param toCol destination file
     * @return true if the straight path is clear
     */
    private boolean isRookPathClear(List<Piece> pieces,
            int fromRow, int fromCol,
            int toRow, int toCol) {
        if (fromRow == toRow) {
            // Horizontal move — iterate along the rank
            int colStep = toCol > fromCol ? 1 : -1;
            for (int c = fromCol + colStep; c != toCol; c += colStep) {
                if (getPieceAt(pieces, fromRow, c) != null) {
                    return false;
                }
            }
        } else {
            // Vertical move — iterate along the file
            int rowStep = toRow > fromRow ? 1 : -1;
            for (int r = fromRow + rowStep; r != toRow; r += rowStep) {
                if (getPieceAt(pieces, r, fromCol) != null) {
                    return false;
                }
            }
        }
        return true;
    }

    // ------------------------------------------------------------------
    // Board query helper
    // ------------------------------------------------------------------
    /**
     * Returns the piece at the given board square, or {@code null} if the
     * square is empty.
     *
     * <p>
     * Linear search over the piece list — acceptable for a board with at most
     * 32 pieces (O(n) where n ≤ 32).
     *
     * @param pieces all pieces on the board
     * @param row rank index [0–7]
     * @param col file index [0–7]
     * @return the piece at (row, col), or {@code null} if empty
     */
    private Piece getPieceAt(List<Piece> pieces, int row, int col) {
        for (Piece p : pieces) {
            if (p.getRow() == row && p.getCol() == col) {
                return p;
            }
        }
        return null;
    }
}
