package com.ShavguLs.chess.logic;

public class Bishop extends Piece {
    public Bishop(boolean isWhite) {
        super(isWhite);
    }

    public Bishop clone() {
        Bishop copy = new Bishop(this.isWhite);
        copy.hasMoved = this.hasMoved;
        return copy;
    }

    @Override
    public char getFenChar() {
        return isWhite ? 'B' : 'b';
    }

    @Override
    boolean isValidMove(int srcRow, int srcCol, int destRow, int destCol, Piece[][] board) {

        if (srcRow == destRow && srcCol == destCol) {
            return false;
        }


        int rowDiff = Math.abs(srcRow - destRow);
        int colDiff = Math.abs(srcCol - destCol);

        int rowStep = (destRow - srcRow) > 0 ? 1 : -1;
        int colStep = (destCol - srcCol) > 0 ? 1 : -1;

        int currentRow = srcRow + rowStep;
        int currentCol = srcCol + colStep;

        //Bishop can move in diagonal direction by as much as he wants
        if(rowDiff != colDiff){
            return false;
        }
        //While moving in diagonal row and column change the same way


        int row = srcRow + rowStep;
        int col = srcCol + colStep;

        while (currentRow != destRow && currentCol != destCol) {
            // If any square along the path is occupied, the move is illegal
            if (board[currentRow][currentCol] != null) {
                return false;
            }
            currentRow += rowStep;
            currentCol += colStep;
        }

        Piece destPiece = board[destRow][destCol];

        return destPiece == null || destPiece.isWhite != this.isWhite;
    }
}
