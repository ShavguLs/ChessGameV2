package com.ShavguLs.chess.common.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class KingTest {

    private Board board;
    private King whiteKing;

    @BeforeEach
    void setUp() {
        board = new Board();
        whiteKing = new King(true);
        // Place the king in the center for easy testing of all directions.
        board.setPiece(4, 4, whiteKing);
    }

    @Test
    void isValidMove_shouldBeTrue_forAllOneSquareMovesToEmptySquares() {
        // Arrange
        int startRow = 4;
        int startCol = 4;

        // Act & Assert for all 8 directions
        assertTrue(whiteKing.isValidMove(startRow, startCol, 3, 4, board.getBoardArray()), "Move Up");
        assertTrue(whiteKing.isValidMove(startRow, startCol, 5, 4, board.getBoardArray()), "Move Down");
        assertTrue(whiteKing.isValidMove(startRow, startCol, 4, 3, board.getBoardArray()), "Move Left");
        assertTrue(whiteKing.isValidMove(startRow, startCol, 4, 5, board.getBoardArray()), "Move Right");
        assertTrue(whiteKing.isValidMove(startRow, startCol, 3, 3, board.getBoardArray()), "Move Up-Left");
        assertTrue(whiteKing.isValidMove(startRow, startCol, 3, 5, board.getBoardArray()), "Move Up-Right");
        assertTrue(whiteKing.isValidMove(startRow, startCol, 5, 3, board.getBoardArray()), "Move Down-Left");
        assertTrue(whiteKing.isValidMove(startRow, startCol, 5, 5, board.getBoardArray()), "Move Down-Right");
    }

    @Test
    void isValidMove_shouldBeFalse_forMovesMoreThanOneSquare() {
        // Arrange
        int startRow = 4;
        int startCol = 4;

        // Act & Assert
        assertFalse(whiteKing.isValidMove(startRow, startCol, 6, 4, board.getBoardArray()), "Vertical two squares");
        assertFalse(whiteKing.isValidMove(startRow, startCol, 4, 6, board.getBoardArray()), "Horizontal two squares (non-castle)");
        assertFalse(whiteKing.isValidMove(startRow, startCol, 6, 6, board.getBoardArray()), "Diagonal two squares");
        assertFalse(whiteKing.isValidMove(startRow, startCol, 2, 4, board.getBoardArray()), "Knight-like move");
    }

    @Test
    void isValidMove_shouldBeFalse_whenTargetIsOccupiedByFriendlyPiece() {
        // Arrange
        board.setPiece(4, 5, new Pawn(true)); // Place a friendly pawn next to the king

        // Act & Assert
        assertFalse(whiteKing.isValidMove(4, 4, 4, 5, board.getBoardArray()));
    }

    @Test
    void isValidMove_shouldBeTrue_whenTargetIsOccupiedByEnemyPiece() {
        // Arrange
        board.setPiece(4, 5, new Pawn(false)); // Place an enemy pawn next to the king

        // Act & Assert
        assertTrue(whiteKing.isValidMove(4, 4, 4, 5, board.getBoardArray()));
    }

    @Test
    void isValidMove_shouldReturnTrueForCastlingAttempt_whenKingHasNotMoved() {
        // Arrange: King is fresh and hasn't moved (hasMoved is false by default)
        board.setPiece(7, 4, whiteKing); // Place on its starting square

        // Act & Assert
        assertTrue(whiteKing.isValidMove(7, 4, 7, 6, board.getBoardArray()), "Kingside castle attempt");
        assertTrue(whiteKing.isValidMove(7, 4, 7, 2, board.getBoardArray()), "Queenside castle attempt");
    }

    @Test
    void isValidMove_shouldReturnFalseForCastlingAttempt_whenKingHasMoved() {
        // Arrange
        board.setPiece(7, 4, whiteKing);
        whiteKing.markMove(); // Simulate the king having moved previously

        // Act & Assert
        assertFalse(whiteKing.isValidMove(7, 4, 7, 6, board.getBoardArray()), "Should not be able to castle after moving.");
    }
}