package com.ShavguLs.chess.common.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QueenTest {

    private Board board;
    private Queen whiteQueen;

    @BeforeEach
    void setUp() {
        board = new Board();
        whiteQueen = new Queen(true);
        // Place the queen in the center for easy testing.
        board.setPiece(4, 4, whiteQueen);
    }

    @Test
    void isValidMove_shouldBeTrue_forClearPathsInAll8Directions() {
        // Arrange
        int startRow = 4;
        int startCol = 4;

        // Act & Assert for Rook-like moves
        assertTrue(whiteQueen.isValidMove(startRow, startCol, 4, 7, board.getBoardArray()), "Move right");
        assertTrue(whiteQueen.isValidMove(startRow, startCol, 4, 0, board.getBoardArray()), "Move left");
        assertTrue(whiteQueen.isValidMove(startRow, startCol, 0, 4, board.getBoardArray()), "Move up");
        assertTrue(whiteQueen.isValidMove(startRow, startCol, 7, 4, board.getBoardArray()), "Move down");

        // Act & Assert for Bishop-like moves
        assertTrue(whiteQueen.isValidMove(startRow, startCol, 1, 1, board.getBoardArray()), "Move up-left");
        assertTrue(whiteQueen.isValidMove(startRow, startCol, 1, 7, board.getBoardArray()), "Move up-right");
        assertTrue(whiteQueen.isValidMove(startRow, startCol, 7, 1, board.getBoardArray()), "Move down-left");
        assertTrue(whiteQueen.isValidMove(startRow, startCol, 7, 7, board.getBoardArray()), "Move down-right");
    }

    @Test
    void isValidMove_shouldBeFalse_forInvalidMovesLikeKnight() {
        // Arrange
        int startRow = 4;
        int startCol = 4;

        // Act & Assert
        assertFalse(whiteQueen.isValidMove(startRow, startCol, 6, 5, board.getBoardArray()), "Knight's L-shape move should be invalid.");
    }

    @Test
    void isValidMove_shouldBeFalse_ifPathIsBlocked() {
        // Arrange
        int startRow = 4;
        int startCol = 4;
        // Place blocking pieces in each of the 8 directions
        board.setPiece(4, 6, new Pawn(true)); // Right
        board.setPiece(4, 2, new Pawn(true)); // Left
        board.setPiece(2, 4, new Pawn(true)); // Up
        board.setPiece(6, 4, new Pawn(true)); // Down
        board.setPiece(2, 2, new Pawn(true)); // Up-Left
        board.setPiece(2, 6, new Pawn(true)); // Up-Right
        board.setPiece(6, 2, new Pawn(true)); // Down-Left
        board.setPiece(6, 6, new Pawn(true)); // Down-Right

        // Act & Assert
        assertFalse(whiteQueen.isValidMove(startRow, startCol, 4, 7, board.getBoardArray()), "Path right is blocked");
        assertFalse(whiteQueen.isValidMove(startRow, startCol, 4, 0, board.getBoardArray()), "Path left is blocked");
        assertFalse(whiteQueen.isValidMove(startRow, startCol, 0, 4, board.getBoardArray()), "Path up is blocked");
        assertFalse(whiteQueen.isValidMove(startRow, startCol, 7, 4, board.getBoardArray()), "Path down is blocked");
        assertFalse(whiteQueen.isValidMove(startRow, startCol, 1, 1, board.getBoardArray()), "Path up-left is blocked");
        assertFalse(whiteQueen.isValidMove(startRow, startCol, 1, 7, board.getBoardArray()), "Path up-right is blocked");
        assertFalse(whiteQueen.isValidMove(startRow, startCol, 7, 1, board.getBoardArray()), "Path down-left is blocked");
        assertFalse(whiteQueen.isValidMove(startRow, startCol, 7, 7, board.getBoardArray()), "Path down-right is blocked");
    }

    @Test
    void isValidMove_shouldBeTrue_forCapturingEnemyOnDestinationSquare() {
        // Arrange
        int startRow = 4;
        int startCol = 4;
        // Place an enemy piece on a valid destination square.
        board.setPiece(1, 1, new Pawn(false));

        // Act & Assert
        assertTrue(whiteQueen.isValidMove(startRow, startCol, 1, 1, board.getBoardArray()), "Should be able to capture enemy on destination.");
    }

    @Test
    void isValidMove_shouldBeFalse_forMovingToSquareWithFriendlyPiece() {
        // Arrange
        int startRow = 4;
        int startCol = 4;
        // Place a friendly piece on a valid destination square.
        board.setPiece(1, 1, new Pawn(true));

        // Act & Assert
        assertFalse(whiteQueen.isValidMove(startRow, startCol, 1, 1, board.getBoardArray()), "Should not be able to capture a friendly piece.");
    }
}