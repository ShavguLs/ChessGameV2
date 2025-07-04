package com.ShavguLs.chess.common.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MoveInterpreterTest {

    private Board board;
    private MoveInterpreter interpreter;

    @BeforeEach
    void setUp() {
        board = new Board();
        board.setupStandardBoard();
        interpreter = new MoveInterpreter(board);
    }

    @Test
    void interpretMove_shouldExecuteSimplePawnMove() throws IllegalMoveException {
        // Arrange (done in setUp)

        // Act: White plays e4
        interpreter.interpretMove("e4");

        // Assert
        assertNull(board.getPieceAt(6, 4), "e2 should be empty.");
        assertTrue(board.getPieceAt(4, 4) instanceof Pawn && board.getPieceAt(4, 4).isWhite(), "White Pawn should be on e4.");
        assertFalse(board.isWhiteTurn(), "It should now be Black's turn.");
    }

    @Test
    void interpretMove_shouldExecuteKnightMove() throws IllegalMoveException {
        // Act: White plays Nf3
        interpreter.interpretMove("Nf3");

        // Assert
        assertNull(board.getPieceAt(7, 6), "g1 should be empty.");
        assertTrue(board.getPieceAt(5, 5) instanceof Knight, "Knight should be on f3.");
    }

    @Test
    void interpretMove_shouldExecuteCapture() throws IllegalMoveException {
        // Arrange: Set up a capture scenario
        interpreter.interpretMove("e4"); // White e4
        interpreter.interpretMove("d5"); // Black d5

        // Act: White captures with exd5
        interpreter.interpretMove("exd5");

        // Assert
        assertNull(board.getPieceAt(4, 4), "e4 should be empty.");
        assertTrue(board.getPieceAt(3, 3) instanceof Pawn && board.getPieceAt(3, 3).isWhite(), "White pawn should be on d5.");
        assertFalse(board.isWhiteTurn(), "It should be Black's turn.");
    }

    @Test
    void interpretMove_shouldHandleKingsideCastle() throws IllegalMoveException {
        // Arrange
        board.setPiece(7, 5, null); // Clear f1
        board.setPiece(7, 6, null); // Clear g1

        // Act
        interpreter.interpretMove("O-O");

        // Assert
        assertTrue(board.getPieceAt(7, 6) instanceof King, "King should be on g1.");
        assertTrue(board.getPieceAt(7, 5) instanceof Rook, "Rook should be on f1.");
    }

    @Test
    void interpretMove_shouldHandlePromotion() throws IllegalMoveException {
        // Arrange: Put a white pawn on e7, ready to promote
        board.clearBoard();
        board.setPiece(1, 4, new Pawn(true)); // White Pawn on e7

        // Act
        interpreter.interpretMove("e8=Q");

        // Assert
        assertNull(board.getPieceAt(1, 4), "e7 should be empty.");
        assertTrue(board.getPieceAt(0, 4) instanceof Queen, "A Queen should be on e8.");
    }

    @Test
    void interpretMove_shouldHandleDisambiguationByFile() throws IllegalMoveException {
        // Arrange: Two rooks that can move to the same square (d1)
        board.clearBoard();
        board.setPiece(7, 0, new Rook(true)); // Ra1
        board.setPiece(7, 7, new Rook(true)); // Rh1

        // Act: Move the rook from h1 to d1
        interpreter.interpretMove("Rhd1");

        // Assert
        assertNull(board.getPieceAt(7, 7), "h1 should be empty.");
        assertTrue(board.getPieceAt(7, 3) instanceof Rook, "Rook should be on d1.");
    }

    @Test
    void interpretMove_shouldThrowExceptionForIllegalMove() {
        // Arrange: It's White's turn.

        // Act & Assert: Try to move a non-existent piece
        assertThrows(IllegalMoveException.class, () -> {
            interpreter.interpretMove("e5"); // Illegal first move for white pawn
        }, "Interpreter should throw exception for an illegal move.");
    }
}