package com.ShavguLs.chess.common;

import java.io.Serializable;

/**
 * A record to represent a chess move.
 * Can be a simple move or a move that includes a promotion choice.
 */
public record MoveObject(int startRow, int startCol, int endRow, int endCol, char promotionPiece) implements Serializable {
    private static final long serialVersionUID = 2L; // Version 2

    /**
     * Constructor for a standard move without promotion.
     */
    public MoveObject(int startRow, int startCol, int endRow, int endCol) {
        this(startRow, startCol, endRow, endCol, ' '); // ' ' indicates no promotion
    }

    @Override
    public String toString() {
        if (promotionPiece != ' ') {
            return "Move from (" + startRow + "," + startCol + ") to (" + endRow + "," + endCol + ") promoting to " + promotionPiece;
        }
        return "Move from (" + startRow + "," + startCol + ") to (" + endRow + "," + endCol + ")";
    }
}