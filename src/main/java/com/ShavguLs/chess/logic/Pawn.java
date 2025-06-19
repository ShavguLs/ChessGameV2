package com.ShavguLs.chess.logic;

public class Pawn extends Piece {

    public Pawn(boolean isWhite) {
        super(isWhite);
    }


    public Pawn clone() {
        Pawn copy = new Pawn(this.isWhite);
        copy.hasMoved = this.hasMoved;
        return copy;
    }

    @Override
    public char getFenChar() {
        return isWhite ? 'P' : 'p';
    }

    @Override
    boolean isValidMove(int srcRow, int srcCol, int destRow, int destCol, Piece[][] board) {
        int direction = this.isWhite() ? -1 : 1;
        int startRow = this.isWhite() ? 6 : 1;

        // --- Block 1: Handle forward non-capture moves ---
        if (srcCol == destCol) { // The move is in the same column
            // Check for a single-step forward move to an empty square
            if (destRow == srcRow + direction && board[destRow][destCol] == null) {
                return true;
            }
            // Check for a double-step forward move from the start row to an empty square
            if (srcRow == startRow && destRow == srcRow + (2 * direction) &&
                    board[destRow][destCol] == null && // Destination must be empty
                    board[srcRow + direction][destCol] == null) { // Path must be clear
                return true;
            }
        }

        // --- Block 2: Handle diagonal capture moves ---
        else if (Math.abs(srcCol - destCol) == 1) { // The move is one column over
            Piece destPiece = board[destRow][destCol];
            // Check for a one-step diagonal move to a square with an enemy piece
            if (destRow == srcRow + direction && destPiece != null && destPiece.isWhite() != this.isWhite()) {
                return true;
            }
        }

        // --- Block 3: If none of the above, the move is illegal ---
        return false;
    }

    @Override
    public boolean isAttackingSquare(int srcRow, int srcCol, int destRow, int destCol, Piece[][] board) {
        int direction = this.isWhite() ? -1 : 1;

        // A pawn ONLY attacks the two squares diagonally in front of it.
        // It does not care if those squares are empty or not for the purpose of an attack check.
        if (destRow == srcRow + direction) {
            if (destCol == srcCol + 1 || destCol == srcCol - 1) {
                return true;
            }
        }
        return false;
    }
}
