package com.ShavguLs.chess.view;

import com.ShavguLs.chess.logic.Piece;
import com.ShavguLs.chess.logic.Queen;
import com.ShavguLs.chess.logic.Rook;
import com.ShavguLs.chess.logic.Bishop;
import com.ShavguLs.chess.logic.Knight;

import javax.swing.JOptionPane;

public class PromotionHandler {
    public static Piece getPromotionChoice(boolean isWhite) {
        String[] options = {"Queen", "Rook", "Bishop", "Knight"};
        int choice = JOptionPane.showOptionDialog(null,
                "Choose a piece for promotion:",
                "Pawn Promotion",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        switch (choice) {
            case 0:  return new Queen(isWhite);
            case 1:  return new Rook(isWhite);
            case 2:  return new Bishop(isWhite);
            case 3:  return new Knight(isWhite);
            default: return new Queen(isWhite);
        }
    }
}