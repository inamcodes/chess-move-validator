
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * Represents a Queen chess piece.
 *
 * <p>The Queen combines the movement of a {@link Rook} and a
 * {@link Bishop}: it moves any number of squares horizontally,
 * vertically, or diagonally, provided no pieces block the path.
 *
 * @see MoveValidator
 */
public class Queen extends Piece {

    /**
     * Constructs a Queen at the given board position.
     *
     * <p>Image loaded from {@code pieces/img/wQueen.png} or
     * {@code pieces/img/bQueen.png} depending on color.
     *
     * @param row      rank index (0 = Black's back rank, 7 = White's back rank)
     * @param col      file index (0 = a-file, 7 = h-file)
     * @param isBlack  0 for White, 1 for Black
     * @throws RuntimeException if the image file cannot be loaded
     */
    public Queen(int row, int col, int isBlack) {
        super(row, col, isBlack);
        name = "queen";
        String imagePath = isBlack == 0 ? "pieces/img/wQueen.png" : "pieces/img/bQueen.png";
        try {
            image = ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Queen image: " + imagePath, e);
        }
    }
}
