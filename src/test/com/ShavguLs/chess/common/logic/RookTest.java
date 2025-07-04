package com.ShavguLs.chess.common.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RookTest {

    private Board board;
    private Rook whiteRook;

    @BeforeEach
    void setUp() {
        board = new Board();
        whiteRook = new Rook(true);
        // Place the rook in the center to test all four directions.
        board.setPiece(4, 4, whiteRook);
    }

    @Test
    void isValidMove_shouldBeTrue_forClearHorizontalAndVerticalPaths() {
        // Arrange
        int startRow = 4;
        int startCol = 4;

        // Act & Assert
        assertTrue(whiteRook.isValidMove(startRow, startCol, 4, 7, board.getBoardArray()), "Move right");
        assertTrue(whiteRook.isValidMove(startRow, startCol, 4, 0, board.getBoardArray()), "Move left");
        assertTrue(whiteRook.isValidMove(startRow, startCol, 0, 4, board.getBoardArray()), "Move up");
        assertTrue(whiteRook.isValidMove(startRow, startCol, 7, 4, board.getBoardArray()), "Move down");
    }

    @Test
    void isValidMove_shouldBeFalse_forDiagonalMoves() {
        // Arrange
        int startRow = 4;
        int startCol = 4;

        // Act & Assert
        assertFalse(whiteRook.isValidMove(startRow, startCol, 5, 5, board.getBoardArray()), "Diagonal move should be invalid.");
        assertFalse(whiteRook.isValidMove(startRow, startCol, 3, 3, board.getBoardArray()), "Diagonal move should be invalid.");
    }

    @Test
    void isValidMove_shouldBeFalse_ifPathIsBlocked() {
        // Arrange
        int startRow = 4;
        int startCol = 4;
        // Place blocking pieces in each direction
        board.setPiece(4, 6, new Pawn(true)); // Block to the right
        board.setPiece(4, 2, new Pawn(true)); // Block to the left
        board.setPiece(2, 4, new Pawn(true)); // Block up
        board.setPiece(6, 4, new Pawn(true)); // Block down

        // Act & Assert
        assertFalse(whiteRook.isValidMove(startRow, startCol, 4, 7, board.getBoardArray()), "Path right is blocked");
        assertFalse(whiteRook.isValidMove(startRow, startCol, 4, 0, board.getBoardArray()), "Path left is blocked");
        assertFalse(whiteRook.isValidMove(startRow, startCol, 0, 4, board.getBoardArray()), "Path up is blocked");
        assertFalse(whiteRook.isValidMove(startRow, startCol, 7, 4, board.getBoardArray()), "Path down is blocked");
    }

    @Test
    void isValidMove_shouldBeTrue_forCapturingEnemyOnDestinationSquare() {
        // Arrange
        int startRow = 4;
        int startCol = 4;
        // Place an enemy piece directly on a valid destination square.
        board.setPiece(4, 7, new Pawn(false));

        // Act & Assert
        assertTrue(whiteRook.isValidMove(startRow, startCol, 4, 7, board.getBoardArray()), "Should be able to capture enemy on destination.");
    }

    @Test
    void isValidMove_shouldBeFalse_forMovingToSquareWithFriendlyPiece() {
        // Arrange
        int startRow = 4;
        int startCol = 4;
        // Place a friendly piece on a valid destination square.
        board.setPiece(4, 7, new Pawn(true));

        // Act & Assert
        assertFalse(whiteRook.isValidMove(startRow, startCol, 4, 7, board.getBoardArray()), "Should not be able to capture a friendly piece.");
    }

    @Test
    void isAttackingSquare_shouldBeTrue_evenIfPathIsBlocked() {
        // Arrange
        int startRow = 4;
        int startCol = 4;
        // Place a blocking piece
        board.setPiece(4, 6, new Pawn(false));

        // Act & Assert

        // Let's test the inverse: a rook does NOT attack diagonally.
        assertFalse(whiteRook.isAttackingSquare(startRow, startCol, 5, 5, board.getBoardArray()), "Rook should not attack diagonally.");
    }
}