
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * Represents a King chess piece.
 *
 * <p>The King moves exactly one square in any direction. It also
 * participates in castling, which is validated in {@link MoveValidator}
 * using the inherited {@link Piece#hasMoved} flag.
 *
 * <p>Castling is legal only when:
 * <ul>
 *   <li>Both the king and the target rook have {@code hasMoved == false}</li>
 *   <li>All squares between them are empty</li>
 *   <li>The king does not start in, pass through, or land on a checked square</li>
 * </ul>
 *
 * @see MoveValidator
 * @see Rook
 */
public class King extends Piece {

    /**
     * Constructs a King at the given board position.
     *
     * <p>Image loaded from {@code pieces/img/wKing.png} or
     * {@code pieces/img/bKing.png} depending on color.
     *
     * @param row      rank index (0 = Black's back rank, 7 = White's back rank)
     * @param col      file index (0 = a-file, 7 = h-file)
     * @param isBlack  0 for White, 1 for Black
     * @throws RuntimeException if the image file cannot be loaded
     */
    public King(int row, int col, int isBlack) {
        super(row, col, isBlack);
        name = "king";
        String imagePath = isBlack == 0 ? "pieces/img/wKing.png" : "pieces/img/bKing.png";
        try {
            image = ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load King image: " + imagePath, e);
        }
    }
}
