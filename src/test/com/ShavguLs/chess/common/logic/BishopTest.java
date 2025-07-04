package com.ShavguLs.chess.common.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BishopTest {

    private Board board;
    private Bishop whiteBishop;

    @BeforeEach
    void setUp() {
        board = new Board();
        whiteBishop = new Bishop(true);
        // Place the bishop in the center to test all four diagonal directions.
        board.setPiece(4, 4, whiteBishop);
    }

    @Test
    void isValidMove_shouldBeTrue_forClearDiagonalPaths() {
        // Arrange
        int startRow = 4;
        int startCol = 4;

        // Act & Assert
        assertTrue(whiteBishop.isValidMove(startRow, startCol, 1, 1, board.getBoardArray()), "Move up-left");
        assertTrue(whiteBishop.isValidMove(startRow, startCol, 1, 7, board.getBoardArray()), "Move up-right");
        assertTrue(whiteBishop.isValidMove(startRow, startCol, 7, 1, board.getBoardArray()), "Move down-left");
        assertTrue(whiteBishop.isValidMove(startRow, startCol, 7, 7, board.getBoardArray()), "Move down-right");
    }

    @Test
    void isValidMove_shouldBeFalse_forHorizontalAndVerticalMoves() {
        // Arrange
        int startRow = 4;
        int startCol = 4;

        // Act & Assert
        assertFalse(whiteBishop.isValidMove(startRow, startCol, 4, 7, board.getBoardArray()), "Horizontal move should be invalid.");
        assertFalse(whiteBishop.isValidMove(startRow, startCol, 7, 4, board.getBoardArray()), "Vertical move should be invalid.");
    }

    @Test
    void isValidMove_shouldBeFalse_ifDiagonalPathIsBlocked() {
        // Arrange
        int startRow = 4;
        int startCol = 4;
        // Place blocking pieces in each diagonal direction
        board.setPiece(2, 2, new Pawn(true)); // Block up-left
        board.setPiece(2, 6, new Pawn(true)); // Block up-right
        board.setPiece(6, 2, new Pawn(true)); // Block down-left
        board.setPiece(6, 6, new Pawn(true)); // Block down-right

        // Act & Assert
        assertFalse(whiteBishop.isValidMove(startRow, startCol, 1, 1, board.getBoardArray()), "Path up-left is blocked");
        assertFalse(whiteBishop.isValidMove(startRow, startCol, 1, 7, board.getBoardArray()), "Path up-right is blocked");
        assertFalse(whiteBishop.isValidMove(startRow, startCol, 7, 1, board.getBoardArray()), "Path down-left is blocked");
        assertFalse(whiteBishop.isValidMove(startRow, startCol, 7, 7, board.getBoardArray()), "Path down-right is blocked");
    }

    @Test
    void isValidMove_shouldBeTrue_forCapturingEnemyOnDestinationSquare() {
        // Arrange
        int startRow = 4;
        int startCol = 4;
        // Place an enemy piece directly on a valid destination square.
        board.setPiece(1, 1, new Pawn(false));

        // Act & Assert
        assertTrue(whiteBishop.isValidMove(startRow, startCol, 1, 1, board.getBoardArray()), "Should be able to capture enemy on destination.");
    }

    @Test
    void isValidMove_shouldBeFalse_forMovingToSquareWithFriendlyPiece() {
        // Arrange
        int startRow = 4;
        int startCol = 4;
        // Place a friendly piece on a valid destination square.
        board.setPiece(1, 1, new Pawn(true));

        // Act & Assert
        assertFalse(whiteBishop.isValidMove(startRow, startCol, 1, 1, board.getBoardArray()), "Should not be able to capture a friendly piece.");
    }

    @Test
    void loopInIsValidMove_hasIncorrectCondition() {


        // Arrange
        board.setPiece(7, 0, whiteBishop); // Bishop at a1
        board.setPiece(1, 6, new Pawn(false)); // Blocking piece at g7

        // Act & Assert
        // A move from a1 (7,0) to h8 (0,7) should be blocked by the pawn at g7 (1,6).
        // My prediction is your current code will say this move is VALID, which is a bug.
        assertFalse(whiteBishop.isValidMove(7, 0, 0, 7, board.getBoardArray()),
                "The path to the destination square should be fully checked for blockers.");
    }
}