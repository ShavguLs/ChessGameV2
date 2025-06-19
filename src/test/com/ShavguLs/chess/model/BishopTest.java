//    package test.com.ShavguLs.chess.model;
//
//    import main.com.ShavguLs.chess.model.*;
//    import org.junit.Before;
//    import org.junit.Test;
//
//    import java.util.List;
//
//    import static junit.framework.TestCase.*;
//
//    public class BishopTest {
//        private Board board;
//
//        @Before
//        public void setUp() {
//            board = new Board();
//        }
//
//        @Test
//        public void testBishopInitialMoves() {
//            Square[][] squares = board.getSquareArray();
//
//            Bishop whiteBishop = (Bishop) squares[7][2].getOccupyingPiece();
//            List<Square> whiteMoves = whiteBishop.getLegalMoves(board);
//            assertEquals(0, whiteMoves.size());
//
//            squares[6][1].removePiece();
//
//            whiteMoves = whiteBishop.getLegalMoves(board);
//            assertTrue(whiteMoves.contains(squares[6][1]));
//            assertTrue(whiteMoves.contains(squares[5][0]));
//        }
//
//        @Test
//        public void testBishopDiagonalMovement() {
//            Square[][] squares = board.getSquareArray();
//
//            Bishop whiteBishop = (Bishop) squares[7][2].getOccupyingPiece();
//            squares[7][2].removePiece();
//            squares[4][4].put(whiteBishop);
//
//            if (squares[5][3].isOccupied()) squares[5][3].removePiece();
//            if (squares[6][2].isOccupied()) squares[6][2].removePiece();
//            if (squares[5][5].isOccupied()) squares[5][5].removePiece();
//            if (squares[6][6].isOccupied()) squares[6][6].removePiece();
//            if (squares[3][3].isOccupied()) squares[3][3].removePiece();
//            if (squares[2][2].isOccupied()) squares[2][2].removePiece();
//            if (squares[1][1].isOccupied()) squares[1][1].removePiece();
//            if (squares[3][5].isOccupied()) squares[3][5].removePiece();
//            if (squares[2][6].isOccupied()) squares[2][6].removePiece();
//            if (squares[1][7].isOccupied()) squares[1][7].removePiece();
//
//            List<Square> moves = whiteBishop.getLegalMoves(board);
//
//    //        System.out.println("Bishop at (4,4) can move to:");
//    //        for (Square s : moves) {
//    //            System.out.println("(" + s.getYNum() + "," + s.getXNum() + ")");
//    //        }
//
//            assertTrue(moves.contains(squares[3][3]));
//            assertTrue(moves.contains(squares[2][2]));
//            assertTrue(moves.contains(squares[1][1]));
//            assertTrue(moves.contains(squares[5][3]));
//            if (!squares[6][2].isOccupied()) {
//                assertTrue(moves.contains(squares[6][2]));
//            }
//            assertTrue(moves.contains(squares[3][5]));
//            assertTrue(moves.contains(squares[2][6]));
//            assertTrue(moves.contains(squares[1][7]));
//            assertTrue(moves.contains(squares[5][5]));
//            if (!squares[6][6].isOccupied()) {
//                assertTrue(moves.contains(squares[6][6]));
//            }
//        }
//    }

