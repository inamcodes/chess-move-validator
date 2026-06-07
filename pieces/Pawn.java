
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * Represents a Pawn chess piece.
 *
 * <p>Pawns have the most complex movement rules of any piece:
 * <ul>
 *   <li>Advance one square forward (toward the opponent's back rank)</li>
 *   <li>Advance two squares forward from the starting rank only</li>
 *   <li>Capture one square diagonally forward</li>
 *   <li>En passant capture — see {@link MoveValidator} for details</li>
 * </ul>
 *
 * <p>The name is set to {@code "w-pawn"} for White and {@code "b-pawn"}
 * for Black so that direction logic in {@link MoveValidator} can branch
 * on a single string comparison.
 *
 * <p>Pawn promotion (reaching the opponent's back rank) is handled by
 * the game controller after a move is validated, not inside this class.
 *
 * @see MoveValidator
 */
public class Pawn extends Piece {

    /**
     * Constructs a Pawn at the given board position.
     *
     * <p>Image loaded from {@code pieces/img/wPawn.png} or
     * {@code pieces/img/bPawn.png} depending on color.
     *
     * @param row      rank index (0 = Black's back rank, 7 = White's back rank)
     * @param col      file index (0 = a-file, 7 = h-file)
     * @param isBlack  0 for White, 1 for Black
     * @throws RuntimeException if the image file cannot be loaded
     */
    public Pawn(int row, int col, int isBlack) {
        super(row, col, isBlack);
        name = isBlack == 0 ? "w-pawn" : "b-pawn";
        String imagePath = isBlack == 0 ? "pieces/img/wPawn.png" : "pieces/img/bPawn.png";
        try {
            image = ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Pawn image: " + imagePath, e);
        }
    }
}
