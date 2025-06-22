package com.ShavguLs.chess.common.logic;

public abstract class Piece {
    boolean isWhite;
    boolean hasMoved = false;

    public void markMove(){
        this.hasMoved = true;
    }

    public int getColor() {
        return isWhite ? 1 : 0;
    }

    public boolean isWhite() {
        return isWhite;
    }

    public abstract char getFenChar();

    public boolean hasMoved(){
        return this.hasMoved;
    }

    public boolean isAttackingSquare(int srcRow, int srcCol, int destRow, int destCol, Piece[][] board) {
        // For every piece EXCEPT the pawn, the attack rule is the same as the move rule.
        // We will override this method in the Pawn class.
        return this.isValidMove(srcRow, srcCol, destRow, destCol, board);
    }

    public Piece(boolean isWhite) {
        this.isWhite = isWhite;
    }

    public abstract Piece clone();

    abstract boolean isValidMove(int srcRow, int srcCol, int destRow, int destCol, Piece[][] board);
}
