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
//public class KingTest {
//    private Board board;
//
//    @Before
//    public void setUp() {
//        board = new Board();
//    }
//
//    @Test
//    public void testKingInitialMoves() {
//        Square[][] squares = board.getSquareArray();
//
//        King whiteKing = (King) squares[7][4].getOccupyingPiece();
//        List<Square> whiteMoves = whiteKing.getLegalMoves(board);
//        assertEquals(0, whiteMoves.size());
//
//        squares[6][3].removePiece();
//        squares[6][4].removePiece();
//        squares[6][5].removePiece();
//        squares[7][5].removePiece();
//
//        whiteMoves = whiteKing.getLegalMoves(board);
//        assertTrue(whiteMoves.contains(squares[6][3]));
//        assertTrue(whiteMoves.contains(squares[6][4]));
//        assertTrue(whiteMoves.contains(squares[6][5]));
//        assertTrue(whiteMoves.contains(squares[7][5]));
//    }
//
//    @Test
//    public void testKingStandardMovement() {
//        Square[][] squares = board.getSquareArray();
//
//        King whiteKing = (King) squares[7][4].getOccupyingPiece();
//        squares[7][4].removePiece();
//        squares[4][4].put(whiteKing);
//
//        List<Square> moves = whiteKing.getLegalMoves(board);
//
//        assertTrue(moves.contains(squares[3][3])); // NW
//        assertTrue(moves.contains(squares[3][4])); // N
//        assertTrue(moves.contains(squares[3][5])); // NE
//        assertTrue(moves.contains(squares[4][3])); // W
//        assertTrue(moves.contains(squares[4][5])); // E
//        assertTrue(moves.contains(squares[5][3])); // SW
//        assertTrue(moves.contains(squares[5][4])); // S
//        assertTrue(moves.contains(squares[5][5])); // SE
//    }
//
//    @Test
//    public void testCastlingConditions() {
//        Square[][] squares = board.getSquareArray();
//
//        King whiteKing = (King) squares[7][4].getOccupyingPiece();
//        squares[7][5].removePiece();
//        squares[7][6].removePiece();
//
//        List<Square> moves = whiteKing.getLegalMoves(board);
//
//        assertTrue(moves.contains(squares[7][6]));
//
//        Square originalSquare = whiteKing.getPosition();
//        Square tempSquare = squares[6][4];
//
//        squares[6][4].removePiece();
//
//        whiteKing.move(tempSquare);
//
//        whiteKing.move(originalSquare);
//
//        moves = whiteKing.getLegalMoves(board);
//        assertFalse(moves.contains(squares[7][6])); // no castl after king has moved
//    }
//}