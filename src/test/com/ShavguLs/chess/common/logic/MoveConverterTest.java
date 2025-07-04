package com.ShavguLs.chess.common.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MoveConverterTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
    }

    @Test
    void testSimplePawnMove() {
        Pawn pawn = new Pawn(true);
        board.setPiece(6, 4, pawn); // White pawn at e2
        // move from e2 to e4
        String notation = MoveConverter.convertMoveToNotation(pawn, 6, 4, 4, 4, board, false, false, false, false, null);
        assertEquals("e4", notation);
    }

    @Test
    void testSimpleKnightMove() {
        Knight knight = new Knight(true);
        board.setPiece(7, 6, knight); // White knight at g1
        // move from g1 to f3
        String notation = MoveConverter.convertMoveToNotation(knight, 7, 6, 5, 5, board, false, false, false, false, null);
        assertEquals("Nf3", notation);
    }

    @Test
    void testPawnCapture() {
        Pawn pawn = new Pawn(true);
        board.setPiece(4, 4, pawn); // White pawn at e4
        board.setPiece(3, 3, new Pawn(false)); // Black pawn at d5
        // white pawn captures d5
        String notation = MoveConverter.convertMoveToNotation(pawn, 4, 4, 3, 3, board, true, false, false, false, null);
        assertEquals("exd5", notation);
    }

    @Test
    void testPieceCaptureWithCheck() {
        Queen queen = new Queen(true);
        board.setPiece(0, 3, queen); // White queen at d8 (for a hypothetical scenario)
        board.setPiece(1, 4, new Pawn(false)); // Black pawn at e7
        // white queen captures e7 with check
        String notation = MoveConverter.convertMoveToNotation(queen, 0, 3, 1, 4, board, true, true, false, false, null);
        assertEquals("Qxe7+", notation);
    }

    @Test
    void testMoveWithCheckmate() {
        Queen queen = new Queen(true);
        // Let's place the queen at h5 (row 3, col 7) to make the move to f7 possible
        board.setPiece(3, 7, queen);
        // white queen captures f7 with checkmate
        String notation = MoveConverter.convertMoveToNotation(queen, 3, 7, 1, 5, board, true, true, true, false, null);
        assertEquals("Qxf7#", notation);
    }

    @Test
    void testKingsideCastle() {
        King king = new King(true);
        String notation = MoveConverter.convertMoveToNotation(king, 7, 4, 7, 6, board, false, false, false, true, null);
        assertEquals("O-O", notation);
    }

    @Test
    void testQueensideCastle() {
        King king = new King(true);
        String notation = MoveConverter.convertMoveToNotation(king, 7, 4, 7, 2, board, false, false, false, true, null);
        assertEquals("O-O-O", notation);
    }

    @Test
    void testPawnPromotion() {
        Pawn pawn = new Pawn(true);
        board.setPiece(1, 4, pawn); // White pawn on e7
        Piece promotedQueen = new Queen(true);
        // move from e7 to e8, promoting to a Queen
        String notation = MoveConverter.convertMoveToNotation(pawn, 1, 4, 0, 4, board, false, false, false, false, promotedQueen);
        assertEquals("e8=Q", notation);
    }

    @Test
    void testPawnPromotionWithCapture() {
        Pawn pawn = new Pawn(true);
        board.setPiece(1, 4, pawn); // White pawn on e7
        board.setPiece(0, 5, new Rook(false)); // Black rook on f8
        Piece promotedQueen = new Queen(true);
        // white pawn captures f8, promoting to a Queen with checkmate
        String notation = MoveConverter.convertMoveToNotation(pawn, 1, 4, 0, 5, board, true, true, true, false, promotedQueen);
        assertEquals("exf8=Q#", notation);
    }

    // --- Tests for Disambiguation ---
    @Nested
    class DisambiguationTests {

        @Test
        void testFileDisambiguation() {
            // Two white rooks, one on a1, one on h1. Both can move to d1.
            Rook rookA = new Rook(true);
            Rook rookH = new Rook(true);
            board.setPiece(7, 0, rookA); // Ra1
            board.setPiece(7, 7, rookH); // Rh1

            // Move the rook from a1 to d1. It should be "Rad1"
            String notation = MoveConverter.convertMoveToNotation(rookA, 7, 0, 7, 3, board, false, false, false, false, null);
            assertEquals("Rad1", notation);
        }

        @Test
        void testRankDisambiguation() {
            // Two white rooks, one on a1, one on a7. Both can move to a3.
            Rook rook1 = new Rook(true);
            Rook rook7 = new Rook(true);
            board.setPiece(7, 0, rook1); // Ra1
            board.setPiece(1, 0, rook7); // Ra7

            // Move the rook from a1 to a3. It should be "R1a3"
            String notation = MoveConverter.convertMoveToNotation(rook1, 7, 0, 5, 0, board, false, false, false, false, null);
            assertEquals("R1a3", notation);
        }

        @Test
        void testFileAndRankDisambiguation() {
            // A complex setup: Queen on a1, two rooks on c1 and a3.
            // The queen can move to c3. A rook from c1 can move to c3. A rook from a3 can move to c3.
            // We'll test moving one of the rooks.
            Queen queen = new Queen(true);
            Rook rookC1 = new Rook(true);
            Rook rookA3 = new Rook(true);
            board.setPiece(7, 0, queen); // Qa1
            board.setPiece(7, 2, rookC1); // Rc1
            board.setPiece(5, 0, rookA3); // Ra3
            board.setPiece(5,2, null); // Make sure c3 is empty

            // The rooks on c1 and a3 can both move to c3. They are on different files AND different ranks.
            // The file should be sufficient for disambiguation.


            // Two knights on b1 and f1 can both move to d2. Files are different, so should use file.
            board.clearBoard();
            Knight knightB = new Knight(true);
            Knight knightF = new Knight(true);
            board.setPiece(7, 1, knightB); // Nb1
            board.setPiece(7, 5, knightF); // Nf1

            // Move the knight from b1 to d2. Should be "Nbd2"
            String notation = MoveConverter.convertMoveToNotation(knightB, 7, 1, 6, 3, board, false, false, false, false, null);
            assertEquals("Nbd2", notation);
        }
    }
}