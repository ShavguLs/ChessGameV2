//package test.com.ShavguLs.chess.model;
//
//import main.com.ShavguLs.chess.model.Board;
//import main.com.ShavguLs.chess.model.Knight;
//import main.com.ShavguLs.chess.model.Square;
//import org.junit.Test;
//
//import java.util.List;
//
//public class KnightTest {
//    @Test
//    public void testInitialKnightMoves(){
//        Board board = new Board();
//
//        Square knightSquare = board.getSquareArray()[7][1];
//        Knight knight = (Knight) knightSquare.getOccupyingPiece();
//
//        if (knight.getColor() != 1){
//            System.out.println("Error: not a white knight!");
//        }
//
//        List<Square> moves = knight.getLegalMoves(board);
//        System.out.println("Knight has " + moves.size() + " possible moves");
//
//        if (moves.size() != 2){
//            System.out.println("Error: expected 2 moves but got " + moves.size());
//        }else {
//            System.out.println("Knight has correct number of moves");
//        }
//
//        boolean foundMoves1 = false;
//        boolean foundMoves2 = false;
//
//        for (Square square : moves){
//            if (square.getYNum() == 5 && square.getXNum() == 0){
//                foundMoves1 = true;
//            }
//            if (square.getYNum() == 5 && square.getXNum() == 2){
//                foundMoves2 = true;
//            }
//        }
//
//        if (!foundMoves1){
//            System.out.println("Error: knight should be able to move to (5, 0)");
//        }
//
//        if (!foundMoves2){
//            System.out.println("Error: knight should be able to move to (5, 2)");
//        }
//
//        if (foundMoves1 && foundMoves2){
//            System.out.println("knight moves to correct squares!");
//        }
//    }
//}
