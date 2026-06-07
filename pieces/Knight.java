
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * Represents a Knight chess piece.
 *
 * <p>The Knight moves in an L-shape: two squares in one direction then
 * one square perpendicular (or vice versa). It is the only piece that
 * can jump over other pieces.
 *
 * <p>Validation shortcut: {@code |Δrow| * |Δcol| == 2} covers all
 * valid L-shapes (2×1 and 1×2) without listing each case.
 *
 * @see MoveValidator
 */
public class Knight extends Piece {

    /**
     * Constructs a Knight at the given board position.
     *
     * <p>Image loaded from {@code pieces/img/wKnight.png} or
     * {@code pieces/img/bKnight.png} depending on color.
     *
     * @param row      rank index (0 = Black's back rank, 7 = White's back rank)
     * @param col      file index (0 = a-file, 7 = h-file)
     * @param isBlack  0 for White, 1 for Black
     * @throws RuntimeException if the image file cannot be loaded
     */
    public Knight(int row, int col, int isBlack) {
        super(row, col, isBlack);
        name = "knight";
        String imagePath = isBlack == 0 ? "pieces/img/wKnight.png" : "pieces/img/bKnight.png";
        try {
            image = ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Knight image: " + imagePath, e);
        }
    }
}
