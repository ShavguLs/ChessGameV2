package com.ShavguLs.chess.common.logic;

import java.util.List;

public class GameValidator {

    public static class ValidationResult {
        private final boolean isValid;
        private final String errorMessage;
        private final int errorMoveNumber;
        private final String errorMove;

        public ValidationResult(boolean isValid, String errorMessage, int errorMoveNumber, String errorMove) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
            this.errorMoveNumber = errorMoveNumber;
            this.errorMove = errorMove;
        }

        public boolean isValid() { return isValid; }
        public String getErrorMessage() { return errorMessage; }
        public int getErrorMoveNumber() { return errorMoveNumber; }
        public String getErrorMove() { return errorMove; }
    }

    public static ValidationResult validateGame(String pgnText) {
        try {
            // Parse moves from PGN text
            List<String> moves = PGNParser.parseMoves(pgnText);

            // Create a fresh board and interpreter
            Board board = new Board();
            board.setupStandardBoard();
            MoveInterpreter interpreter = new MoveInterpreter(board);

            // Replay each move
            for (int i = 0; i < moves.size(); i++) {
                String move = moves.get(i);
                try {
                    interpreter.interpretMove(move);
                } catch (IllegalMoveException e) {
                    // Calculate move number for user-friendly error reporting
                    int moveNumber = (i / 2) + 1;
                    String color = (i % 2 == 0) ? "White" : "Black";
                    String errorMsg = String.format("Invalid move by %s on move %d: %s\nError: %s",
                            color, moveNumber, move, e.getMessage());
                    return new ValidationResult(false, errorMsg, moveNumber, move);
                }
            }

            // If we reach here, all moves were valid
            return new ValidationResult(true, "Game is valid! All " + moves.size() + " moves are legal.", 0, "");

        } catch (Exception e) {
            return new ValidationResult(false, "Error parsing PGN: " + e.getMessage(), 0, "");
        }
    }

    public static ValidationResult validateMoveSequence(List<String> moves) {
        try {
            Board board = new Board();
            board.setupStandardBoard();
            MoveInterpreter interpreter = new MoveInterpreter(board);

            for (int i = 0; i < moves.size(); i++) {
                String move = moves.get(i);
                try {
                    interpreter.interpretMove(move);
                } catch (IllegalMoveException e) {
                    int moveNumber = (i / 2) + 1;
                    String color = (i % 2 == 0) ? "White" : "Black";
                    String errorMsg = String.format("Invalid move by %s on move %d: %s\nError: %s",
                            color, moveNumber, move, e.getMessage());
                    return new ValidationResult(false, errorMsg, moveNumber, move);
                }
            }

            return new ValidationResult(true, "Move sequence is valid! All " + moves.size() + " moves are legal.", 0, "");

        } catch (Exception e) {
            return new ValidationResult(false, "Error validating moves: " + e.getMessage(), 0, "");
        }
    }

    public static String getBoardAnalysis(String pgnText) {
        try {
            List<String> moves = PGNParser.parseMoves(pgnText);
            Board board = new Board();
            board.setupStandardBoard();
            MoveInterpreter interpreter = new MoveInterpreter(board);

            // Replay all moves
            for (String move : moves) {
                interpreter.interpretMove(move);
            }

            StringBuilder analysis = new StringBuilder();
            analysis.append("Game Analysis:\n");
            analysis.append("Total moves played: ").append(moves.size()).append("\n");
            analysis.append("Current turn: ").append(moves.size() % 2 == 0 ? "White" : "Black").append("\n");

            // Check game state
            boolean whiteToMove = moves.size() % 2 == 0;
            boolean whiteInCheck = board.isKingInCheck(true);
            boolean blackInCheck = board.isKingInCheck(false);
            boolean whiteHasLegalMoves = board.hasLegalMoves(true);
            boolean blackHasLegalMoves = board.hasLegalMoves(false);

            if (whiteInCheck) analysis.append("White king is in check!\n");
            if (blackInCheck) analysis.append("Black king is in check!\n");

            if (!whiteHasLegalMoves) {
                if (whiteInCheck) {
                    analysis.append("White is checkmated!\n");
                } else {
                    analysis.append("White is stalemated!\n");
                }
            }

            if (!blackHasLegalMoves) {
                if (blackInCheck) {
                    analysis.append("Black is checkmated!\n");
                } else {
                    analysis.append("Black is stalemated!\n");
                }
            }

            return analysis.toString();

        } catch (Exception e) {
            return "Error analyzing game: " + e.getMessage();
        }
    }
}