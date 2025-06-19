//package test.com.ShavguLs.chess.model;
//
//import main.com.ShavguLs.chess.model.*;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.util.List;
//
//import static junit.framework.TestCase.*;
//
//public class RookTest {
//    private Board board;
//
//    @Before
//    public void setUp() {
//        board = new Board();
//    }
//
//    @Test
//    public void testRookInitialMoves() {
//        Square[][] squares = board.getSquareArray();
//
//        Rook whiteRook = (Rook) squares[7][0].getOccupyingPiece();
//
//        squares[7][1].removePiece();  // Remove knight
//        squares[6][0].removePiece();  // Remove pawn
//
//        List<Square> afterMoves = whiteRook.getLegalMoves(board);
//        System.out.println("After clearing, rook has " + afterMoves.size() + " moves:");
//        for (Square s : afterMoves) {
//            System.out.println("- Can move to: (" + s.getYNum() + "," + s.getXNum() + ")");
//        }
//
//        assertTrue("Rook should be able to move horizontally after knight removed",
//                afterMoves.contains(squares[7][1]));
//    }
//
//    @Test
//    public void testRookHorizontalVerticalMovement() {
//        Board testBoard = new Board();
//        Square[][] squares = testBoard.getSquareArray();
//
//        for (int y = 0; y < 8; y++) {
//            for (int x = 0; x < 8; x++) {
//                if (squares[y][x].isOccupied()) {
//                    squares[y][x].removePiece();
//                }
//            }
//        }
//
//        Rook whiteRook = new Rook(1, squares[4][4]);
//        squares[4][4].put(whiteRook);
//
//        List<Square> moves = whiteRook.getLegalMoves(testBoard);
//
//        System.out.println("Rook at (4,4) can move to " + moves.size() + " squares:");
//        for (Square s : moves) {
//            System.out.println("- (" + s.getYNum() + "," + s.getXNum() + ")");
//        }
//
//        assertTrue("Rook should be able to move up", moves.contains(squares[0][4]));
//        assertTrue("Rook should be able to move down", moves.contains(squares[7][4]));
//        assertTrue("Rook should be able to move left", moves.contains(squares[4][0]));
//    }
//}