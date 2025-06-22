package com.ShavguLs.chess.common.logic;

public class Rook extends Piece{


    public Rook(boolean isWhite) {
        super(isWhite);
    }

    public Rook clone() {
        Rook copy = new Rook(this.isWhite);
        copy.hasMoved = this.hasMoved;
        return copy;
    }


    @Override
    public char getFenChar() {
        return isWhite ? 'R' : 'r';
    }

    // In: main/com/ShavguLs/chess/logic/Rook.java
// Replace ONLY the isValidMove method.

    @Override
    boolean isValidMove(int srcRow, int srcCol, int destRow, int destCol, Piece[][] board) {

        // THE FIX: PART 1 - Check if the move is actually a move.
        // This prevents hasLegalMoves() from testing a "move" from a square to itself.
        if (srcRow == destRow && srcCol == destCol) {
            return false;
        }

        // This is YOUR original code structure.
        if (srcRow == destRow) {
            int step = (destCol > srcCol) ? 1 : -1;

            for (int col = srcCol + step; col != destCol; col += step) {
                if (board[srcRow][col] != null) {
                    return false; // Path is blocked, exit immediately
                }
            }
        } else if (srcCol == destCol) {
            int step = (destRow > srcRow) ? 1 : -1;

            for (int row = srcRow + step; row != destRow; row += step) {
                if (board[row][srcCol] != null) {
                    return false; // Path is blocked, exit immediately
                }
            }
        } else {
            // Not a horizontal or vertical move, so it's illegal for a rook.
            return false; // THE FIX: PART 2 - Exit immediately.
        }

        // This code now only runs if the path was a straight line AND was clear.
        Piece destPiece = board[destRow][destCol];

        return destPiece == null || destPiece.isWhite != this.isWhite;
    }
}
