package com.ShavguLs.chess.common.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
    }

    @Test
    void setupStandardBoard_shouldPlacePiecesCorrectly() {
        // Arrange & Act
        board.setupStandardBoard();

        // Assert key positions
        // White pieces
        assertTrue(board.getPieceAt(7, 0) instanceof Rook && board.getPieceAt(7, 0).isWhite(), "White Rook on a1");
        assertTrue(board.getPieceAt(7, 4) instanceof King && board.getPieceAt(7, 4).isWhite(), "White King on e1");
        assertTrue(board.getPieceAt(6, 5) instanceof Pawn && board.getPieceAt(6, 5).isWhite(), "White Pawn on f2");

        // Black pieces
        assertTrue(board.getPieceAt(0, 3) instanceof Queen && !board.getPieceAt(0, 3).isWhite(), "Black Queen on d8");
        assertTrue(board.getPieceAt(1, 6) instanceof Pawn && !board.getPieceAt(1, 6).isWhite(), "Black Pawn on g7");

        // Check an empty square
        assertNull(board.getPieceAt(4, 4), "e4 should be empty at the start.");

        // Check turn
        assertTrue(board.isWhiteTurn(), "It should be White's turn at the start of the game.");
    }

    @Test
    void attemptMove_shouldExecuteLegalMoveAndFlipTurn() {
        // Arrange
        board.setupStandardBoard(); // Start with a standard board

        // Act: White moves pawn from e2 to e4
        boolean wasMoveSuccessful = board.attemptMove(6, 4, 4, 4, true);

        // Assert
        assertTrue(wasMoveSuccessful, "A legal first pawn move should be successful.");
        assertNull(board.getPieceAt(6, 4), "The original square (e2) should now be empty.");
        assertTrue(board.getPieceAt(4, 4) instanceof Pawn, "A pawn should now be on the destination square (e4).");
        assertTrue(board.getPieceAt(4, 4).isWhite(), "The moved pawn should be white.");
        assertFalse(board.isWhiteTurn(), "After a successful white move, it should be Black's turn.");
    }

    // In BoardTest.java
    @Test
    void attemptMove_shouldFailForIllegalMoveAndNotFlipTurn() {
        // Arrange
        board.setupStandardBoard(); // Start with a standard board
        // We will try to move the a1 Rook diagonally to b2, which is illegal.
        int startRow = 7;
        int startCol = 0;
        int destRow = 6;
        int destCol = 1;

        // Save the original state for assertion
        Piece originalPiece = board.getPieceAt(startRow, startCol);
        boolean initialTurn = board.isWhiteTurn();

        // Act: White tries to move the Rook diagonally.
        boolean wasMoveSuccessful = board.attemptMove(startRow, startCol, destRow, destCol, true);

        // Assert
        assertFalse(wasMoveSuccessful, "A rook moving diagonally should be an illegal move.");
        // Check that the original piece is still in its starting position.
        assertSame(originalPiece, board.getPieceAt(startRow, startCol), "The piece should not have moved from its original square.");
        // Check that the destination square (a pawn is there) is unchanged.
        assertTrue(board.getPieceAt(destRow, destCol) instanceof Pawn, "The destination square should be unchanged.");
        // Check that the turn did not flip.
        assertEquals(initialTurn, board.isWhiteTurn(), "After a failed move, the turn should not change.");
    }
    @Test
    void attemptMove_shouldFailWhenMovingOpponentsPiece() {
        // Arrange
        board.setupStandardBoard();

        // Act: White tries to move Black's pawn
        boolean wasMoveSuccessful = board.attemptMove(1, 4, 3, 4, true); // White's turn, moving black piece

        // Assert
        assertFalse(wasMoveSuccessful, "Should not be able to move an opponent's piece.");
        assertTrue(board.isWhiteTurn(), "Turn should not change after a failed move.");
    }

    @Test
    void isKingInCheck_shouldBeTrue_whenAttackedByRook() {
        // Arrange: Place a White King and a threatening Black Rook on an empty board.
        board.setPiece(4, 4, new King(true));  // White King at e4
        board.setPiece(4, 0, new Rook(false)); // Black Rook at a4

        // Act
        boolean kingInCheck = board.isKingInCheck(true);

        // Assert
        assertTrue(kingInCheck, "King should be in check from a rook on the same rank.");
    }

    @Test
    void isKingInCheck_shouldBeTrue_whenAttackedByBishopDiagonally() {
        // Arrange
        board.setPiece(4, 4, new King(true));  // White King at e4
        board.setPiece(1, 1, new Bishop(false)); // Black Bishop at b7

        // Act
        boolean kingInCheck = board.isKingInCheck(true);

        // Assert
        assertTrue(kingInCheck, "King should be in check from a bishop on the same diagonal.");
    }

    @Test
    void isKingInCheck_shouldBeFalse_whenAttackPathIsBlocked() {
        // Arrange
        board.setPiece(4, 4, new King(true));      // White King at e4
        board.setPiece(4, 0, new Rook(false));     // Black Rook at a4
        board.setPiece(4, 2, new Pawn(true));      // A friendly pawn blocking the path at c4

        // Act
        boolean kingInCheck = board.isKingInCheck(true);

        // Assert
        assertFalse(kingInCheck, "King should NOT be in check when the attack path is blocked.");
    }

    @Test
    void isKingInCheck_shouldBeFalse_whenNotAttacked() {
        // Arrange
        board.setPiece(4, 4, new King(true));  // White King at e4
        board.setPiece(0, 0, new Rook(false)); // Black Rook is not on a threatening square

        // Act
        boolean kingInCheck = board.isKingInCheck(true);

        // Assert
        assertFalse(kingInCheck, "King should not be in check when no piece is attacking it.");
    }

    @Test
    void isKingInCheck_shouldNotBeInCheckFromFriendlyPiece() {
        // Arrange
        board.setPiece(4, 4, new King(true));  // White King at e4
        board.setPiece(4, 0, new Rook(true));  // A FRIENDLY White Rook on a4

        // Act
        boolean kingInCheck = board.isKingInCheck(true);

        // Assert
        assertFalse(kingInCheck, "King should not be in check from a piece of the same color.");
    }

    @Test
    void isKingInCheck_shouldBeTrue_whenAttackedByPawn() {
        // Arrange
        board.setPiece(4, 4, new King(true));    // White King at e4
        board.setPiece(3, 3, new Pawn(false));   // A Black Pawn attacking from d5

        // Act
        boolean kingInCheck = board.isKingInCheck(true);

        // Assert
        assertTrue(kingInCheck, "King should be in check from an attacking pawn.");
    }

    // In BoardTest.java

    @Test
    void attemptMove_shouldBeFalse_ifMoveExposesKingToCheck_VerticalPin() {
        // Arrange: Set up a vertical pin.
        board.setPiece(7, 4, new King(true));   // White King at e1
        board.setPiece(6, 4, new Rook(true));   // White Rook at e2
        board.setPiece(4, 4, new Queen(false)); // Black Queen at e4

        // By default, a new Board's turn is White's. So we don't need to set it.

        // Act: Try to move the pinned Rook horizontally from e2 to a2.
        boolean wasMoveSuccessful = board.attemptMove(6, 4, 6, 0, true);

        // Assert
        assertFalse(wasMoveSuccessful, "A vertically pinned piece cannot move horizontally.");
        assertTrue(board.getPieceAt(6, 4) instanceof Rook, "The Rook should not have moved.");
        assertTrue(board.isWhiteTurn(), "The turn should not change on an illegal move.");
    }

    @Test
    void attemptMove_shouldBeTrue_ifPinnedPieceMovesAlongAttackAxis() {
        // Arrange: Same vertical pin.
        board.setPiece(7, 4, new King(true));
        board.setPiece(6, 4, new Rook(true));
        board.setPiece(4, 4, new Queen(false));

        // Act: Move the pinned Rook along the line of attack. This is a legal move.
        boolean wasMoveSuccessful = board.attemptMove(6, 4, 5, 4, true);

        // Assert
        assertTrue(wasMoveSuccessful, "A pinned piece should be able to move along the axis of the pin.");
        assertFalse(board.isWhiteTurn(), "The turn should flip after a legal move.");
    }

    @Test
    void attemptMove_shouldBeFalse_ifMoveExposesKingToCheck_DiagonalPin() {
        // Arrange: Set up a diagonal pin.
        board.setPiece(7, 0, new King(true));    // White King at a1
        board.setPiece(6, 1, new Bishop(true));  // White Bishop at b2
        board.setPiece(4, 3, new Rook(false));   // Black Rook at d4

        // Act: Try to move the pinned Bishop to a non-diagonal square.
        boolean wasMoveSuccessful = board.attemptMove(6, 1, 5, 1, true);

        // Assert
        assertFalse(wasMoveSuccessful, "A diagonally pinned piece cannot move off the pin axis.");
        assertTrue(board.isWhiteTurn(), "The turn should not change.");
    }

    @Test
    void attemptMove_shouldBeFalse_ifKingTriesToMoveIntoCheck() {
        // Arrange
        board.setPiece(7, 4, new King(true));  // White King at e1
        board.setPiece(0, 5, new Rook(false)); // Black Rook at f8, attacking the f-file

        // Act: Try to move the King from e1 into the attacked f1 square.
        boolean moveIntoCheck = board.attemptMove(7, 4, 7, 5, true);

        // Assert
        assertFalse(moveIntoCheck, "King should not be able to move into a square attacked by an enemy piece.");
    }

    @Test
    void attemptMove_shouldPerformKingsideCastle_whenAllConditionsAreMet() {
        // Arrange
        board.setupStandardBoard();
        // Clear the path for White's kingside castle (remove bishop and knight)
        board.setPiece(7, 5, null);
        board.setPiece(7, 6, null);

        // Act: White attempts to castle kingside
        boolean wasSuccessful = board.attemptMove(7, 4, 7, 6, true);

        // Assert
        assertTrue(wasSuccessful, "Kingside castling should be successful with a clear path.");
        assertNull(board.getPieceAt(7, 4), "Original king square should be empty.");
        assertNull(board.getPieceAt(7, 7), "Original rook square should be empty.");
        assertTrue(board.getPieceAt(7, 6) instanceof King, "King should be on g1.");
        assertTrue(board.getPieceAt(7, 5) instanceof Rook, "Rook should be on f1.");
        assertFalse(board.isWhiteTurn(), "Turn should flip to Black after castling.");
    }

    @Test
    void attemptMove_shouldFailQueensideCastle_whenPathIsBlocked() {
        // Arrange
        board.setupStandardBoard();
        // Clear MOST of the path for queenside castle, but leave one piece (the knight at b1)
        board.setPiece(7, 3, null); // Remove queen
        board.setPiece(7, 2, null); // Remove bishop
        // The knight at (7, 1) is still blocking the path.

        // Act: White attempts to castle queenside
        boolean wasSuccessful = board.attemptMove(7, 4, 7, 2, true);

        // Assert
        assertFalse(wasSuccessful, "Queenside castling should fail if the path is blocked.");
        assertTrue(board.isWhiteTurn(), "Turn should not flip on a failed move.");
        assertTrue(board.getPieceAt(7, 4) instanceof King, "King should not have moved.");
    }

    @Test
    void attemptMove_shouldFailCastle_whenKingHasMoved() {
        // Arrange
        board.setupStandardBoard();
        board.setPiece(7, 5, null);
        board.setPiece(7, 6, null);

        // Simulate the king moving and coming back
        Piece king = board.getPieceAt(7, 4);
        king.markMove(); // Manually mark the king as having moved

        // Act
        boolean wasSuccessful = board.attemptMove(7, 4, 7, 6, true);

        // Assert
        assertFalse(wasSuccessful, "Should not be able to castle if the king has moved.");
    }

    @Test
    void attemptMove_shouldFailCastle_whenRookHasMoved() {
        // Arrange
        board.setupStandardBoard();
        board.setPiece(7, 5, null);
        board.setPiece(7, 6, null);

        // Simulate the rook moving and coming back
        Piece rook = board.getPieceAt(7, 7);
        rook.markMove(); // Manually mark the rook as having moved

        // Act
        boolean wasSuccessful = board.attemptMove(7, 4, 7, 6, true);

        // Assert
        assertFalse(wasSuccessful, "Should not be able to castle if the rook has moved.");
    }

   @Test
    void attemptMove_shouldFailCastle_whenPassingThroughAttackedSquare() {
        // Arrange
        board.setupStandardBoard();
        board.setPiece(7, 5, null);
        board.setPiece(7, 6, null);
        // Place an enemy piece that attacks one of the squares the king passes through (f1)
        board.setPiece(6, 5, new Rook(false)); // Black rook at f8 attacks f1

        // Act
        boolean wasSuccessful = board.attemptMove(7, 4, 7, 6, true);

        // Assert
        assertFalse(wasSuccessful, "Should not be able to castle through an attacked square.");
    }

    @Test
    void attemptMove_shouldPerformEnPassant_whenConditionsAreMet() {
        // Arrange
        // 1. Place a White Pawn on e5 and a Black Pawn on d7.
        board.setPiece(3, 4, new Pawn(true)); // White Pawn on e5
        board.setPiece(1, 3, new Pawn(false)); // Black Pawn on d7

        // 2. We need it to be Black's turn. To do this, we'll make a dummy move for White
        // that doesn't affect the test. Let's move a corner rook back and forth.
        board.setPiece(7,0, new Rook(true));
        board.attemptMove(7, 0, 6, 0, true); // White moves Ra1-a2. It is now Black's turn.
        assertFalse(board.isWhiteTurn(), "Setup Failure: It should be Black's turn.");

        // 3. Black moves its pawn two squares forward from d7 to d5, landing next to the white pawn.
        board.attemptMove(1, 3, 3, 3, false);

        // 4. It is now White's turn. The en passant capture is possible.
        assertTrue(board.isWhiteTurn(), "It should be White's turn after Black's pawn move.");

        // Act: White pawn at e5 captures the Black pawn at d5 via en passant by moving to d6.
        boolean wasSuccessful = board.attemptMove(3, 4, 2, 3, true);

        // Assert
        assertTrue(wasSuccessful, "En passant capture should be a legal move.");
        assertNull(board.getPieceAt(3, 4), "Original white pawn square (e5) should be empty.");
        assertNull(board.getPieceAt(3, 3), "Captured black pawn square (d5) should be empty.");
        assertTrue(board.getPieceAt(2, 3) instanceof Pawn && board.getPieceAt(2, 3).isWhite(),
                "White pawn should now be on the en passant square (d6).");
    }

    @Test
    void attemptMove_shouldFailEnPassant_ifNotPerformedImmediately() {
        // Arrange
        // 1. Set up the same en passant possibility.
        board.setPiece(3, 4, new Pawn(true));
        board.setPiece(1, 3, new Pawn(false));
        board.setPiece(7,0, new Rook(true));
        board.attemptMove(7, 0, 6, 0, true);    // Dummy move to make it Black's turn
        board.attemptMove(1, 3, 3, 3, false);   // Black moves d7-d5. It's now White's turn.

        // 2. Now, have White and Black make a pair of unrelated moves, forfeiting the right.
        board.setPiece(0, 6, new Knight(true)); // Add a white knight to make a move.
        board.attemptMove(0, 6, 2, 5, true);    // White moves a knight.

        board.setPiece(0, 1, new Knight(false));// Add a black knight.
        board.attemptMove(0, 1, 2, 2, false);   // Black moves a knight.

        // 3. It is now White's turn again, but the right to en passant has expired.

        // Act: White tries to perform the en passant capture from the original setup.
        boolean wasSuccessful = board.attemptMove(3, 4, 2, 3, true);

        // Assert
        assertFalse(wasSuccessful, "En passant must be performed on the turn immediately following the two-square pawn advance.");
    }

    @Test
    void attemptMove_shouldFailEnPassant_ifItExposesKingToCheck() {
        // Arrange
        // Setup a pin where an en passant capture would be illegal.
        board.setPiece(3, 4, new King(true));   // White King on e5
        board.setPiece(3, 3, new Pawn(true));   // White Pawn on d5
        board.setPiece(3, 0, new Rook(false));  // Black Rook on a5, pinning the d5 pawn to the king.
        board.setPiece(1, 2, new Pawn(false));  // Black Pawn on c7.

        // Make it Black's turn with a dummy move for White
        board.setPiece(0,0, new Rook(true));
        board.attemptMove(0,0,0,1,true); // White dummy move

        // Act 1: Black moves pawn from c7 to c5, creating an en passant opportunity for the pinned white pawn.
        board.attemptMove(1, 2, 3, 2, false);

        // Act 2: White tries to capture en passant. This is illegal as it would expose the King.
        boolean wasSuccessful = board.attemptMove(3, 3, 2, 2, true);

        // Assert
        assertFalse(wasSuccessful, "Cannot perform en passant if it leaves the king in check.");
    }
}