//package test.com.ShavguLs.chess.model;
//
//import main.com.ShavguLs.chess.model.*;
//import org.junit.Before;
//import org.junit.Test;
//import static org.junit.Assert.*;
//
//public class BoardTest {
//    private Board board;
//
//    @Before
//    public void setUp() {
//        board = new Board();
//    }
//
//    @Test
//    public void testInitialBoardState() {
//        Square[][] squares = board.getSquareArray();
//
//        // board dimensions
//        assertEquals(8, squares.length);
//        assertEquals(8, squares[0].length);
//
//        assertTrue(board.getTurn());
//
//        // Pawns
//        for (int i = 0; i < 8; i++) {
//            assertTrue(squares[1][i].isOccupied());
//            assertTrue(squares[6][i].isOccupied());
//            assertTrue(squares[1][i].getOccupyingPiece() instanceof Pawn);
//            assertTrue(squares[6][i].getOccupyingPiece() instanceof Pawn);
//            assertEquals(0, squares[1][i].getOccupyingPiece().getColor());
//            assertEquals(1, squares[6][i].getOccupyingPiece().getColor());
//        }
//        // Rooks
//        assertTrue(squares[0][0].getOccupyingPiece() instanceof Rook);
//        assertTrue(squares[0][7].getOccupyingPiece() instanceof Rook);
//        assertTrue(squares[7][0].getOccupyingPiece() instanceof Rook);
//        assertTrue(squares[7][7].getOccupyingPiece() instanceof Rook);
//        // Knights
//        assertTrue(squares[0][1].getOccupyingPiece() instanceof Knight);
//        assertTrue(squares[0][6].getOccupyingPiece() instanceof Knight);
//        assertTrue(squares[7][1].getOccupyingPiece() instanceof Knight);
//        assertTrue(squares[7][6].getOccupyingPiece() instanceof Knight);
//        // Bishops
//        assertTrue(squares[0][2].getOccupyingPiece() instanceof Bishop);
//        assertTrue(squares[0][5].getOccupyingPiece() instanceof Bishop);
//        assertTrue(squares[7][2].getOccupyingPiece() instanceof Bishop);
//        assertTrue(squares[7][5].getOccupyingPiece() instanceof Bishop);
//        // Queens
//        assertTrue(squares[0][3].getOccupyingPiece() instanceof Queen);
//        assertTrue(squares[7][3].getOccupyingPiece() instanceof Queen);
//        // Kings
//        assertTrue(squares[0][4].getOccupyingPiece() instanceof King);
//        assertTrue(squares[7][4].getOccupyingPiece() instanceof King);
//    }
//
//    @Test
//    public void testPieceCollections() {
//        assertEquals(16, board.getWhitePieces().size());
//        assertEquals(16, board.getBlackPieces().size());
//    }
//    @Test
//    public void testTurnSwitching() {
//        assertTrue(board.getTurn());
//        board.switchTurn();
//        assertFalse(board.getTurn());
//        board.switchTurn();
//        assertTrue(board.getTurn());
//    }
//}