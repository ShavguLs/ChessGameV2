package com.ShavguLs.chess.common.logic;

public class Knight extends Piece {
    public Knight(boolean isWhite) {
        super(isWhite);
    }

    public Knight clone() {
        Knight copy = new Knight(this.isWhite);
        copy.hasMoved = this.hasMoved;
        return copy;
    }

    @Override
    public char getFenChar() {
        return isWhite ? 'N' : 'n';
    }

    @Override
    boolean isValidMove(int srcRow, int srcCol, int destRow, int destCol, Piece[][] board) {
        int rowDiff = Math.abs(destRow-srcRow);
        int colDiff = Math.abs(destCol-srcCol);
        Piece destPiece = board[destRow][destCol];


        //Knight moves in L-shapes. So either 2 spaces by row and 1 space by column or vice versa
        if ((rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2)) {
            // valid knight move
            return destPiece == null || destPiece.isWhite != this.isWhite;
        }



        return false;
    }
}
