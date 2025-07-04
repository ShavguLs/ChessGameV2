package com.ShavguLs.chess.common.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MoveTrackerTest {

    private MoveTracker moveTracker;

    // The @BeforeEach annotation makes this method run BEFORE every single @Test method.
    // This is useful for creating a fresh, clean object for each test.
    @BeforeEach
    void setUp() {
        moveTracker = new MoveTracker();
    }

    @Test
    void newMoveTracker_shouldBeEmpty() {
        // Arrange (done in setUp)
        // Act & Assert
        assertEquals(0, moveTracker.getTotalMoves(), "A new tracker should have zero moves.");
        assertEquals(0, moveTracker.getMovesSinceCapture(), "A new tracker should have zero moves since last capture.");
        assertTrue(moveTracker.getAllMoves().isEmpty(), "The moves list should be empty.");
        assertEquals("", moveTracker.getLastMove(), "Get last move on an empty tracker should return an empty string.");
    }

    @Test
    void addMove_shouldIncreaseTotalMoves() {
        // Arrange
        String move1 = "e4";
        String move2 = "e5";

        // Act
        moveTracker.addMove(move1);
        moveTracker.addMove(move2);

        // Assert
        assertEquals(2, moveTracker.getTotalMoves());
        assertEquals(move2, moveTracker.getLastMove());
        assertEquals(2, moveTracker.getAllMoves().size());
        assertTrue(moveTracker.getAllMoves().contains(move1));
    }

    @Test
    void updateMoveCounter_shouldResetOnPawnMove() {
        // Arrange
        moveTracker.updateMoveCounter(false, false); // Knight move
        moveTracker.updateMoveCounter(false, false); // Bishop move
        assertEquals(2, moveTracker.getMovesSinceCapture());

        // Act: A pawn moves
        moveTracker.updateMoveCounter(false, true);

        // Assert
        assertEquals(0, moveTracker.getMovesSinceCapture(), "Counter should reset on a pawn move.");
    }

    @Test
    void updateMoveCounter_shouldResetOnCapture() {
        // Arrange
        moveTracker.updateMoveCounter(false, false); // Knight move
        moveTracker.updateMoveCounter(false, false); // Bishop move
        assertEquals(2, moveTracker.getMovesSinceCapture());

        // Act: A capture occurs
        moveTracker.updateMoveCounter(true, false);

        // Assert
        assertEquals(0, moveTracker.getMovesSinceCapture(), "Counter should reset on a capture.");
    }

    @Test
    void updateMoveCounter_shouldIncrementOnQuietMove() {
        // Arrange (done in setUp)
        // Act
        moveTracker.updateMoveCounter(false, false);

        // Assert
        assertEquals(1, moveTracker.getMovesSinceCapture());
    }

    @Test
    void hasFiftyMoveRule_shouldBeTrueAt100HalfMoves() {
        // Arrange
        for (int i = 0; i < 99; i++) {
            moveTracker.updateMoveCounter(false, false); // 99 quiet half-moves
        }
        assertFalse(moveTracker.hasFiftyMoveRule(), "Should not be 50-move rule before 100 half-moves.");

        // Act
        moveTracker.updateMoveCounter(false, false); // The 100th quiet half-move

        // Assert
        assertTrue(moveTracker.hasFiftyMoveRule(), "Should be 50-move rule at exactly 100 half-moves (50 full moves).");
    }

    @Test
    void hasThreefoldRepetition_shouldBeTrueOnThirdOccurrence() {
        // Arrange
        // A simple back-and-forth sequence to force repetition
        String position1 = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"; // Start
        String position2 = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"; // After e4
        String position3 = "rnbqkbnr/ppp1pppp/8/3p4/4P3/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 2"; // After e4 d5

        // Act
        moveTracker.addPosition(position1); // Occurs 1st time
        moveTracker.addPosition(position2);
        moveTracker.addPosition(position1); // Occurs 2nd time
        moveTracker.addPosition(position3);

        assertFalse(moveTracker.hasThreefoldRepetition(), "Should not be threefold repetition on 2nd occurrence.");

        moveTracker.addPosition(position1); // Occurs 3rd time

        // Assert
        assertTrue(moveTracker.hasThreefoldRepetition(), "Should be threefold repetition on 3rd occurrence.");
    }

    @Test
    void hasThreefoldRepetition_shouldBeFalseForDifferentPositions() {
        // Arrange
        moveTracker.addPosition("pos1");
        moveTracker.addPosition("pos2");
        moveTracker.addPosition("pos3");
        moveTracker.addPosition("pos4");
        moveTracker.addPosition("pos5");
        moveTracker.addPosition("pos6");
        moveTracker.addPosition("pos7");
        moveTracker.addPosition("pos8");
        moveTracker.addPosition("pos9");
        moveTracker.addPosition("pos1"); // Only two occurrences of pos1

        // Act & Assert
        assertFalse(moveTracker.hasThreefoldRepetition());
    }

    @Test
    void reset_shouldClearAllState() {
        // Arrange
        moveTracker.addMove("e4");
        moveTracker.addPosition("some_fen_string");
        moveTracker.updateMoveCounter(false, false);

        // Act
        moveTracker.reset();

        // Assert
        assertEquals(0, moveTracker.getTotalMoves());
        assertEquals(0, moveTracker.getMovesSinceCapture());
        assertTrue(moveTracker.getAllMoves().isEmpty());
    }
}