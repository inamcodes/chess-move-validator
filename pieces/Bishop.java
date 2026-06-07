
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * Represents a Bishop chess piece.
 *
 * <p>The Bishop moves any number of squares diagonally, provided no
 * pieces block the path. It always remains on the same color square
 * it started on.
 *
 * @see MoveValidator
 */
public class Bishop extends Piece {

    /**
     * Constructs a Bishop at the given board position.
     *
     * <p>Image loaded from {@code pieces/img/wBishop.png} or
     * {@code pieces/img/bBishop.png} depending on color.
     *
     * @param row      rank index (0 = Black's back rank, 7 = White's back rank)
     * @param col      file index (0 = a-file, 7 = h-file)
     * @param isBlack  0 for White, 1 for Black
     * @throws RuntimeException if the image file cannot be loaded
     */
    public Bishop(int row, int col, int isBlack) {
        super(row, col, isBlack);
        name = "bishop";
        String imagePath = isBlack == 0 ? "pieces/img/wBishop.png" : "pieces/img/bBishop.png";
        try {
            image = ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Bishop image: " + imagePath, e);
        }
    }
}
