

/**
 * Immutable result returned by {@link MoveValidator#validate}.
 *
 * <p>
 * Instead of scattering boolean flags ({@code castledAtThisMove},
 * {@code wEnPassant}, {@code bEnPassant}) across class-level state, all
 * outcomes are bundled here and returned to the caller. The caller decides what
 * to do with them — the validator never mutates board state.
 *
 * <p>
 * Usage example:
 * <pre>{@code
 * MoveResult result = validator.validate(pieces, activePiece, toRow, toCol, lastDoublePushedPawn);
 * if (result.isLegal()) {  
 *     activePiece.setRow(toRow);
 *     activePiece.setCol(toCol);
 *     if (result.isCastling())   handleCastling(result.getCastlingSide());
 *     if (result.isEnPassant())  removeCapturedPawn(result.getEnPassantPawn());
 * }
 * }</pre>
 *
 * @see MoveValidator
 */
public class MoveResult {

    /**
     * Castling side constants returned by {@link #getCastlingSide()}.
     */
    public enum CastleSide {
        QUEEN_SIDE, KING_SIDE
    }

    // ------------------------------------------------------------------
    // Shared illegal singleton — avoids allocating a new object per
    // rejected move, since most candidate moves are illegal.
    // ------------------------------------------------------------------
    private static final MoveResult ILLEGAL = new MoveResult(false, false, null, null);

    private final boolean legal;
    private final boolean enPassant;
    private final Piece enPassantPawn;   // the pawn to remove from the board
    private final CastleSide castlingSide; // non-null only when castling

    private MoveResult(boolean legal, boolean enPassant,
            Piece enPassantPawn, CastleSide castlingSide) {
        this.legal = legal;
        this.enPassant = enPassant;
        this.enPassantPawn = enPassantPawn;
        this.castlingSide = castlingSide;
    }

    // ------------------------------------------------------------------
    // Factory methods
    // ------------------------------------------------------------------
    /**
     * Returns the shared illegal-move singleton.
     */
    public static MoveResult illegal() {
        return ILLEGAL;
    }

    /**
     * Returns a result representing a normal legal move.
     */
    public static MoveResult normal() {
        return new MoveResult(true, false, null, null);
    }

    /**
     * Returns a result representing a legal en passant capture.
     *
     * @param capturedPawn the pawn that will be removed from the board; must
     * not be null
     */
    public static MoveResult enPassant(Piece capturedPawn) {
        return new MoveResult(true, true, capturedPawn, null);
    }

    /**
     * Returns a result representing a legal castling move.
     *
     * @param side which side the king is castling toward
     */
    public static MoveResult castling(CastleSide side) {
        return new MoveResult(true, false, null, side);
    }

    // ------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------
    /**
     * Returns {@code true} if the move is fully legal.
     */
    public boolean isLegal() {
        return legal;
    }

    /**
     * Returns {@code true} if this move is an en passant capture.
     */
    public boolean isEnPassant() {
        return enPassant;
    }

    /**
     * Returns the pawn captured by en passant, or {@code null} if this is not
     * an en passant move.
     */
    public Piece getEnPassantPawn() {
        return enPassantPawn;
    }

    /**
     * Returns {@code true} if this move is a castling move.
     */
    public boolean isCastling() {
        return castlingSide != null;
    }

    /**
     * Returns which side the king is castling toward, or {@code null} if this
     * is not a castling move.
     */
    public CastleSide getCastlingSide() {
        return castlingSide;
    }

    @Override
    public String toString() {
        if (!legal) {
            return "MoveResult[ILLEGAL]";
        }
        if (isCastling()) {
            return "MoveResult[CASTLING, side=" + castlingSide + "]";
        }
        if (isEnPassant()) {
            return "MoveResult[EN_PASSANT, captured=" + enPassantPawn + "]";
        }
        return "MoveResult[NORMAL]";
    }
}
