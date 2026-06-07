
/**
 * Represents the color of a chess piece.
 *
 * <p>Replaces the previous {@code int colorIsBlack} convention
 * (0 = white, 1 = black) with a type-safe enum, eliminating
 * comparison bugs like {@code colorIsBlack == 0}.
 */
public enum Color {

    /** The white (light) side — moves first, starts on ranks 6–7. */
    WHITE,

    /** The black (dark) side — starts on ranks 0–1. */
    BLACK;

    /**
     * Converts the legacy integer color flag to a {@code Color}.
     *
     * @param  isBlack  0 for white, 1 for black
     * @return the corresponding {@code Color}
     * @throws IllegalArgumentException if {@code isBlack} is not 0 or 1
     */
    public static Color fromInt(int isBlack) {
        return switch (isBlack) {
            case 0 -> WHITE;
            case 1 -> BLACK;
            default -> throw new IllegalArgumentException(
                "Invalid color flag: " + isBlack + ". Expected 0 (white) or 1 (black).");
        };
    }

    /** Returns the opposite color. */
    public Color opposite() {
        return this == WHITE ? BLACK : WHITE;
    }
}
