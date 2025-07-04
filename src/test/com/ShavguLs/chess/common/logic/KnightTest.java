package com.ShavguLs.chess.common.logic; // Make sure this package is correct

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class KnightTest {

    private Board board;
    private Knight whiteKnight;

    @BeforeEach
    void setUp() {
        // Create a fresh board and a single white knight for each test.
        board = new Board(); // Assuming Board() creates an empty 8x8 array
        whiteKnight = new Knight(true);
        // Place the knight somewhere in the middle of the board for testing.
        board.setPiece(4, 4, whiteKnight);
    }

    @Test
    void isValidMove_shouldReturnTrue_forValidLShapeMovesToEmptySquares() {
        // Arrange (done in setUp)
        int startRow = 4;
        int startCol = 4;

        // Act & Assert for all 8 possible L-shaped moves
        assertTrue(whiteKnight.isValidMove(startRow, startCol, 2, 3, board.getBoardArray()), "Move Up-Left");
        assertTrue(whiteKnight.isValidMove(startRow, startCol, 2, 5, board.getBoardArray()), "Move Up-Right");
        assertTrue(whiteKnight.isValidMove(startRow, startCol, 3, 2, board.getBoardArray()), "Move Left-Up");
        assertTrue(whiteKnight.isValidMove(startRow, startCol, 3, 6, board.getBoardArray()), "Move Right-Up");
        assertTrue(whiteKnight.isValidMove(startRow, startCol, 5, 2, board.getBoardArray()), "Move Left-Down");
        assertTrue(whiteKnight.isValidMove(startRow, startCol, 5, 6, board.getBoardArray()), "Move Right-Down");
        assertTrue(whiteKnight.isValidMove(startRow, startCol, 6, 3, board.getBoardArray()), "Move Down-Left");
        assertTrue(whiteKnight.isValidMove(startRow, startCol, 6, 5, board.getBoardArray()), "Move Down-Right");
    }

    @Test
    void isValidMove_shouldReturnFalse_forInvalidMoves() {
        // Arrange
        int startRow = 4;
        int startCol = 4;

        // Act & Assert for various illegal moves
        assertFalse(whiteKnight.isValidMove(startRow, startCol, 4, 5, board.getBoardArray()), "Horizontal move");
        assertFalse(whiteKnight.isValidMove(startRow, startCol, 5, 4, board.getBoardArray()), "Vertical move");
        assertFalse(whiteKnight.isValidMove(startRow, startCol, 5, 5, board.getBoardArray()), "Diagonal move");
        assertFalse(whiteKnight.isValidMove(startRow, startCol, 4, 4, board.getBoardArray()), "Move to same square");
    }

    @Test
    void isValidMove_shouldReturnFalse_whenTargetIsOccupiedByFriendlyPiece() {
        // Arrange
        int startRow = 4;
        int startCol = 4;
        int destRow = 2;
        int destCol = 5;
        // Place another white piece (e.g., a Pawn) on a valid destination square.
        board.setPiece(destRow, destCol, new Pawn(true));

        // Act & Assert
        assertFalse(whiteKnight.isValidMove(startRow, startCol, destRow, destCol, board.getBoardArray()),
                "Should not be able to capture a friendly piece.");
    }

    @Test
    void isValidMove_shouldReturnTrue_whenTargetIsOccupiedByEnemyPiece() {
        // Arrange
        int startRow = 4;
        int startCol = 4;
        int destRow = 2;
        int destCol = 5;
        // Place a black piece (e.g., a Pawn) on a valid destination square.
        board.setPiece(destRow, destCol, new Pawn(false));

        // Act & Assert
        assertTrue(whiteKnight.isValidMove(startRow, startCol, destRow, destCol, board.getBoardArray()),
                "Should be able to capture an enemy piece.");
    }
}