
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * Represents a Rook chess piece.
 *
 * <p>The Rook moves any number of squares horizontally or vertically,
 * provided no pieces block the path. It also participates in castling
 * with the King, tracked via the inherited {@link Piece#hasMoved} flag.
 *
 * @see King
 * @see MoveValidator
 */
public class Rook extends Piece {

    /**
     * Constructs a Rook at the given board position.
     *
     * <p>Image loaded from {@code pieces/img/wRook.png} or
     * {@code pieces/img/bRook.png} depending on color.
     *
     * @param row      rank index (0 = Black's back rank, 7 = White's back rank)
     * @param col      file index (0 = a-file, 7 = h-file)
     * @param isBlack  0 for White, 1 for Black
     * @throws RuntimeException if the image file cannot be loaded
     */
    public Rook(int row, int col, int isBlack) {
        super(row, col, isBlack);
        name = "rook";
        String imagePath = isBlack == 0 ? "pieces/img/wRook.png" : "pieces/img/bRook.png";
        try {
            image = ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Rook image: " + imagePath, e);
        }
    }
}
