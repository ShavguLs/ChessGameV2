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
//public class QueenTest {
//    private Board board;
//
//    @Before
//    public void setUp() {
//        board = new Board();
//    }
//
//    @Test
//    public void testQueenInitialMoves() {
//        Square[][] squares = board.getSquareArray();
//
//        Queen whiteQueen = (Queen) squares[7][3].getOccupyingPiece();
//        List<Square> whiteMoves = whiteQueen.getLegalMoves(board);
//        assertEquals(0, whiteMoves.size());
//
//        squares[6][3].removePiece();
//
//        whiteMoves = whiteQueen.getLegalMoves(board);
//        assertTrue(whiteMoves.contains(squares[6][3]));
//        assertTrue(whiteMoves.contains(squares[5][3]));
//        assertTrue(whiteMoves.contains(squares[4][3]));
//    }
//
//    @Test
//    public void testQueenCombinedMovement() {
//        Square[][] squares = board.getSquareArray();
//
//        Queen whiteQueen = (Queen) squares[7][3].getOccupyingPiece();
//        squares[7][3].removePiece();
//        squares[4][4].put(whiteQueen);
//
//        for (int y = 0; y < 8; y++) {
//            for (int x = 0; x < 8; x++) {
//                if (!(y == 4 && x == 4) && squares[y][x].isOccupied()) {
//                    squares[y][x].removePiece();
//                }
//            }
//        }
//
//        List<Square> moves = whiteQueen.getLegalMoves(board);
//
////        System.out.println("queen at (4,4) can move to:");
////        for (Square s : moves) {
////            System.out.println("(" + s.getYNum() + "," + s.getXNum() + ")");
////        }
//
//        assertTrue(moves.contains(squares[3][3]));
//        assertTrue(moves.contains(squares[2][2]));
//        assertTrue(moves.contains(squares[1][1]));
//        assertTrue(moves.contains(squares[0][0]));
//
//        assertTrue(moves.contains(squares[3][5]));
//        assertTrue(moves.contains(squares[2][6]));
//        assertTrue(moves.contains(squares[1][7]));
//
//        assertTrue(moves.contains(squares[5][3]));
//        assertTrue(moves.contains(squares[6][2]));
//        assertTrue(moves.contains(squares[7][1]));
//
//        assertTrue(moves.contains(squares[5][5]));
//        assertTrue(moves.contains(squares[6][6]));
//        assertTrue(moves.contains(squares[7][7]));
//
//        for (int x = 0; x < 8; x++) {
//            if (x != 4) {
//                assertTrue(moves.contains(squares[4][x]));
//            }
//        }
//
//        for (int y = 0; y < 8; y++) {
//            if (y != 4) {
//                assertTrue(moves.contains(squares[y][4]));
//            }
//        }
//    }
//}