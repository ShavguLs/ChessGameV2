//package test.com.ShavguLs.chess.model;
//
//import main.com.ShavguLs.chess.model.*;
//import org.junit.Test;
//
//public class CheckmateDetectorTest {
//
//    @Test
//    public void testInitialBoardNoCheck(){
//        Board board = new Board();
//        CheckmateDetector detector = board.getCheckmateDetector();
//
//        System.out.println("Initial board - white in check: " + detector.whiteInCheck());
//        System.out.println("Initial board - black in check: " + detector.blackInCheck());
//
//        if (detector.whiteInCheck() || detector.blackInCheck()){
//            System.out.println("Error: no one should be in check at start of game");
//        }else {
//            System.out.println("Test passed: no one in check at start!");
//        }
//    }
//
//    @Test
//    public void testCheckmate(){
//        Board board = new Board();
//        Square[][] squares = board.getSquareArray();
//
//        for (int i = 0; i < 8; i++) {
//            for (int j = 0; j < 8; j++) {
//                if (squares[i][j].isOccupied()){
//                    squares[i][j].removePiece();
//                }
//            }
//        }
//
//        King whiteKing = new King(1, squares[7][7]);
//        squares[7][7].put(whiteKing);
//
//        King blackKing = new King(0, squares[0][0]);
//        squares[0][0].put(blackKing);
//
//        Queen blackQueen = new Queen(0, squares[6][7]);
//        squares[6][7].put(blackQueen);
//
//        board.getWhitePieces().clear();
//        board.getBlackPieces().clear();
//
//        board.getWhitePieces().add(whiteKing);
//        board.getBlackPieces().add(blackKing);
//        board.getBlackPieces().add(blackQueen);
//
//        CheckmateDetector detector = new CheckmateDetector(
//                board, board.getWhitePieces(), board.getBlackPieces(), whiteKing, blackKing
//        );
//
//        detector.update();
//        System.out.println("White in check: " + detector.whiteInCheck());
//
//        System.out.println("White checkmated: " + detector.whiteCheckMated());
//    }
//}
