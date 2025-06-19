//package test.com.ShavguLs.chess.model;
//
//import com.sun.security.jgss.GSSUtil;
//import main.com.ShavguLs.chess.model.Board;
//import main.com.ShavguLs.chess.model.Pawn;
//import main.com.ShavguLs.chess.model.Square;
//import org.junit.Test;
//
//import java.util.List;
//
//public class PawnTest {
//    @Test
//    public void testInitialPawnMoves(){
//        Board board = new Board();
//
//        Square pawnSquare = board.getSquareArray()[6][0];
//        Pawn pawn = (Pawn) pawnSquare.getOccupyingPiece();
//
//        if (pawn.getColor() != 1){
//            System.out.println("Error: not a white pawn!");
//        }
//
//        List<Square> moves = pawn.getLegalMoves(board);
//        System.out.println("White pawn has " + moves.size() + " possible moves");
//
//        if (moves.size() != 2){
//            System.out.println("Error: expected 2 moves but got " + moves.size());
//        }else {
//            System.out.println("white pawn has correct number of moves");
//        }
//
//        Square blackPawnSquare = board.getSquareArray()[1][0];
//        Pawn blackPawn = (Pawn) blackPawnSquare.getOccupyingPiece();
//
//        if (blackPawn.getColor() != 0){
//            System.out.println("Error: not a black pawn");
//        }
//
//        List<Square> blackMoves = blackPawn.getLegalMoves(board);
//        System.out.println("Black pawn has " + blackMoves.size() + " possible moves");
//
//        if (blackMoves.size() != 2){
//            System.out.println("Error: expected 2 moves but got " + blackMoves.size());
//        }else {
//            System.out.println("black pawn has correct number of moves");
//        }
//    }
//}
