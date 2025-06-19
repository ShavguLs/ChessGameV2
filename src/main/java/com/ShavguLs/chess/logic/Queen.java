package com.ShavguLs.chess.logic;

public class Queen extends Piece{


    public Queen(boolean isWhite) {
        super(isWhite);
    }

    public Queen clone() {
        Queen copy = new Queen(this.isWhite);
        copy.hasMoved = this.hasMoved;
        return copy;
    }


    @Override
    public char getFenChar() {
        return isWhite ? 'Q' : 'q';
    }


    @Override
    boolean isValidMove(int srcRow, int srcCol, int destRow, int destCol, Piece[][] board)  {

        //Queen just combines bishop and rook logic
        int rowDiff = Math.abs(srcRow - destRow);
        int colDiff = Math.abs(srcCol - destCol);


        if (srcRow == destRow && srcCol == destCol) {
            return false;
        }


        if(rowDiff == colDiff){
            int rowStep = destRow > srcRow ? 1: -1;
            int colStep = destCol > srcCol ? 1: -1;

            int row = srcRow + rowStep;
            int col = srcCol + colStep;

            while(row != destRow){
                if(board[row][col] != null){
                    return false;
                }
                row+=rowStep;
                col+=colStep;
            }
        }else if(srcRow == destRow){
            int step = (destCol > srcCol) ? 1 : -1;

            for(int col = srcCol + step; col != destCol; col+=step){
                if(board[srcRow][col] != null){
                    return false;
                }
            }
        } else if (srcCol == destCol) {

            int step = (destRow > srcRow) ? 1 : -1;

            for(int row = srcRow + step; row != destRow; row+=step){
                if(board[row][srcCol] != null){
                    return false;
                }
            }
        }else {
            return false;
        }





        Piece destPiece = board[destRow][destCol];

        return destPiece == null || destPiece.isWhite != this.isWhite;
    }
}
