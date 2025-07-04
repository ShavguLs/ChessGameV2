package com.ShavguLs.chess.common.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PawnTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board(); // Provides an empty board for each test
    }

    // @Nested classes are a great way to group related tests.
    // Here, we group all tests related to the White Pawn.
    @Nested
    class WhitePawnTests {
        private Pawn whitePawn;

        @BeforeEach
        void setUp() {
            whitePawn = new Pawn(true);
        }

        @Test
        void canMoveOneSquareForwardToEmptySquare() {
            // Arrange
            board.setPiece(6, 4, whitePawn);
            // Act & Assert
            assertTrue(whitePawn.isValidMove(6, 4, 5, 4, board.getBoardArray()));
        }

        @Test
        void canMoveTwoSquaresForwardFromStartRowIfPathIsClear() {
            // Arrange
            board.setPiece(6, 4, whitePawn);
            // Act & Assert
            assertTrue(whitePawn.isValidMove(6, 4, 4, 4, board.getBoardArray()));
        }

        @Test
        void cannotMoveTwoSquaresForwardIfNotOnStartRow() {
            // Arrange
            board.setPiece(5, 4, whitePawn); // Pawn is already on row 5
            whitePawn.markMove(); // Mark it as having moved
            // Act & Assert
            assertFalse(whitePawn.isValidMove(5, 4, 3, 4, board.getBoardArray()));
        }

        @Test
        void cannotMoveTwoSquaresForwardIfPathIsBlocked() {
            // Arrange
            board.setPiece(6, 4, whitePawn);
            board.setPiece(5, 4, new Pawn(false)); // An enemy pawn is blocking the path
            // Act & Assert
            assertFalse(whitePawn.isValidMove(6, 4, 4, 4, board.getBoardArray()));
        }

        @Test
        void cannotMoveOneSquareForwardIfBlocked() {
            // Arrange
            board.setPiece(6, 4, whitePawn);
            board.setPiece(5, 4, new Pawn(false)); // An enemy pawn is blocking
            // Act & Assert
            assertFalse(whitePawn.isValidMove(6, 4, 5, 4, board.getBoardArray()));
        }

        @Test
        void canCaptureDiagonally() {
            // Arrange
            board.setPiece(6, 4, whitePawn);
            board.setPiece(5, 3, new Pawn(false)); // Enemy piece to the left
            board.setPiece(5, 5, new Pawn(false)); // Enemy piece to the right
            // Act & Assert
            assertTrue(whitePawn.isValidMove(6, 4, 5, 3, board.getBoardArray()));
            assertTrue(whitePawn.isValidMove(6, 4, 5, 5, board.getBoardArray()));
        }

        @Test
        void cannotMoveDiagonallyToEmptySquare() {
            // Arrange
            board.setPiece(6, 4, whitePawn);
            // Act & Assert
            assertFalse(whitePawn.isValidMove(6, 4, 5, 5, board.getBoardArray()));
        }

        @Test
        void cannotCaptureFriendlyPieceDiagonally() {
            // Arrange
            board.setPiece(6, 4, whitePawn);
            board.setPiece(5, 5, new Pawn(true)); // Friendly pawn
            // Act & Assert
            assertFalse(whitePawn.isValidMove(6, 4, 5, 5, board.getBoardArray()));
        }
    }


    // Here, we group all tests related to the Black Pawn.
    @Nested
    class BlackPawnTests {
        private Pawn blackPawn;

        @BeforeEach
        void setUp() {
            blackPawn = new Pawn(false);
        }

        @Test
        void canMoveOneSquareForwardToEmptySquare() {
            // Arrange
            board.setPiece(1, 4, blackPawn);
            // Act & Assert
            assertTrue(blackPawn.isValidMove(1, 4, 2, 4, board.getBoardArray()));
        }

        @Test
        void canMoveTwoSquaresForwardFromStartRowIfPathIsClear() {
            // Arrange
            board.setPiece(1, 4, blackPawn);
            // Act & Assert
            assertTrue(blackPawn.isValidMove(1, 4, 3, 4, board.getBoardArray()));
        }

        @Test
        void canCaptureDiagonally() {
            // Arrange
            board.setPiece(1, 4, blackPawn);
            board.setPiece(2, 3, new Pawn(true)); // Enemy piece to the left
            board.setPiece(2, 5, new Pawn(true)); // Enemy piece to the right
            // Act & Assert
            assertTrue(blackPawn.isValidMove(1, 4, 2, 3, board.getBoardArray()));
            assertTrue(blackPawn.isValidMove(1, 4, 2, 5, board.getBoardArray()));
        }
    }
}