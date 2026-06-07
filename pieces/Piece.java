
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Abstract base class for all chess pieces.
 *
 * <p>Stores the piece's position in pixel coordinates internally,
 * exposing rank/file as logical row/column via getters. All subclasses
 * must set {@link #name} and {@link #image} in their constructor.
 *
 * <p>Board coordinate convention:
 * <ul>
 *   <li>Row 0 = rank 8 (Black's back rank, top of board)</li>
 *   <li>Row 7 = rank 1 (White's back rank, bottom of board)</li>
 *   <li>Col 0 = a-file, Col 7 = h-file</li>
 * </ul>
 *
 * <p>This class is <strong>not</strong> thread-safe.
 */
public abstract class Piece {

    /** Pixel size of one board square. Must match the board renderer. */
    protected static final int SQUARE_SIZE = 85;

    /** Rendered image for this piece. Set by each subclass constructor. */
    protected BufferedImage image;

    /** The color of this piece. Never null after construction. */
    public final Color color;

    /**
     * Human-readable piece identifier used in validation logic.
     * Set by each subclass (e.g. "king", "w-pawn").
     */
    public String name;

    /**
     * Whether this piece has moved at least once during the game.
     * Used for castling eligibility (king and rook) and pawn
     * double-advance eligibility.
     */
    public boolean hasMoved = false;

    /** Pixel x-coordinate (derived from column). */
    private int x;

    /** Pixel y-coordinate (derived from row). */
    private int y;

    /**
     * Constructs a piece at the given board position with the given color.
     *
     * @param row      rank index (0 = Black's back rank, 7 = White's back rank)
     * @param col      file index (0 = a-file, 7 = h-file)
     * @param isBlack  legacy color flag: 0 = white, 1 = black
     */
    protected Piece(int row, int col, int isBlack) {
        this.x = col * SQUARE_SIZE;
        this.y = row * SQUARE_SIZE;
        this.color = Color.fromInt(isBlack);
    }

    // -------------------------------------------------------------------------
    // Position accessors
    // -------------------------------------------------------------------------

    /**
     * Returns the file (column) index of this piece.
     *
     * @return column index in range [0, 7]
     */
    public int getCol() {
        return x / SQUARE_SIZE;
    }

    /**
     * Returns the rank (row) index of this piece.
     *
     * @return row index in range [0, 7]
     */
    public int getRow() {
        return y / SQUARE_SIZE;
    }

    /**
     * Sets the rank (row) of this piece and marks it as having moved.
     *
     * @param row new row index in range [0, 7]
     */
    public void setRow(int row) {
        this.y = row * SQUARE_SIZE;
        this.hasMoved = true;
    }

    /**
     * Sets the file (column) of this piece and marks it as having moved.
     *
     * @param col new column index in range [0, 7]
     */
    public void setCol(int col) {
        this.x = col * SQUARE_SIZE;
        this.hasMoved = true;
    }

    // -------------------------------------------------------------------------
    // Color helpers
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if this piece belongs to the White side.
     *
     * @return true if color is {@link Color#WHITE}
     */
    public boolean isWhite() {
        return color == Color.WHITE;
    }

    /**
     * Returns {@code true} if this piece is an enemy of the given piece.
     *
     * @param other the piece to compare against; must not be null
     * @return true if the two pieces have opposite colors
     */
    public boolean isEnemyOf(Piece other) {
        return this.color != other.color;
    }

    // -------------------------------------------------------------------------
    // Rendering
    // -------------------------------------------------------------------------

    /**
     * Draws this piece onto the board at its current pixel position.
     *
     * @param g the graphics context to draw into; must not be null
     */
    public void draw(Graphics2D g) {
        g.drawImage(image, x, y, SQUARE_SIZE, SQUARE_SIZE, null);
    }

    // -------------------------------------------------------------------------
    // Object overrides
    // -------------------------------------------------------------------------

    /**
     * Returns a debug-friendly string showing the piece name, color,
     * and current position.
     *
     * @return e.g. {@code "King(WHITE, row=7, col=4)"}
     */
    @Override
    public String toString() {
        return String.format("%s(%s, row=%d, col=%d)",
            getClass().getSimpleName(), color, getRow(), getCol());
    }
}
