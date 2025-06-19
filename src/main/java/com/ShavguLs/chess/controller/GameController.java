package com.ShavguLs.chess.controller;

import com.ShavguLs.chess.logic.*;
import com.ShavguLs.chess.view.GameWindow;

import javax.swing.*;

public class GameController {

    private Board logicBoard;
    private boolean isWhiteTurn;
    private Piece selectedPiece;
    private int selectedPieceRow;
    private int selectedPieceCol;

    private final GameWindow gameWindow;
    private Clock whiteClock;
    private Clock blackClock;

    private Timer timer;
    private final Object timerLock = new Object();

    // PGN functionality
    private MoveTracker moveTracker;
    private PGNManager pgnManager;
    private String whitePlayerName = "White";
    private String blackPlayerName = "Black";

    public GameController(int hh, int mm, int ss, GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        this.moveTracker = new MoveTracker();
        this.pgnManager = new PGNManager(moveTracker);
        this.pgnManager.setTimeControl(hh, mm, ss);
        setUpNewGame();
        // Only set up and start the clock if a time was actually given
        if (hh > 0 || mm > 0 || ss > 0) {
            setupClock(hh, mm, ss);
            startTimer();
        }
    }

    public void setPlayerNames(String whiteName, String blackName) {
        this.whitePlayerName = whiteName;
        this.blackPlayerName = blackName;
        this.pgnManager.setPlayerNames(whiteName, blackName);
    }

    public void setUpNewGame() {
        stopTimer(); // Stop timer from the previous game
        this.logicBoard = new Board();
        this.logicBoard.setupStandardBoard();
        this.isWhiteTurn = true;
        this.selectedPiece = null;

        // Reset PGN tracking
        this.moveTracker.reset();
        this.pgnManager.reset();
        this.pgnManager.setPlayerNames(whitePlayerName, blackPlayerName);

        // If clocks exist (meaning it's a timed game), we need to handle them.
        if (whiteClock != null) {
            // --- THE FIX IS HERE ---
            // Instead of resetting, we get the original time from the old clock
            // and create NEW clock objects with that starting time.
            int originalSeconds = whiteClock.getInitialSeconds();
            int hh = originalSeconds / 3600;
            int mm = (originalSeconds % 3600) / 60;
            int ss = originalSeconds % 60;

            // Create new clock objects, effectively resetting them
            setupClock(hh, mm, ss);

            // Update the display to show the reset time
            gameWindow.updateWhiteClock(whiteClock.getTime());
            gameWindow.updateBlackClock(blackClock.getTime());

            // Restart the timer for the new game
            startTimer();
        }
    }

    public boolean selectPiece(int row, int col) {
        // DEBUG: Add logging to track selection state
        System.out.println("DEBUG: selectPiece called for (" + row + "," + col + ")");
        System.out.println("DEBUG: Current turn is white: " + isWhiteTurn);
        System.out.println("DEBUG: Currently selected piece: " +
                (selectedPiece != null ? selectedPiece.getClass().getSimpleName() + " at (" +
                        selectedPieceRow + "," + selectedPieceCol + ")" : "none"));

        Piece clickedPiece = logicBoard.getPieceAt(row, col);
        System.out.println("DEBUG: Clicked piece: " +
                (clickedPiece != null ? clickedPiece.getClass().getSimpleName() +
                        " (white: " + clickedPiece.isWhite() + ")" : "empty square"));

        // FIXED: If we already have a piece selected, this could be either:
        // 1. A move attempt (if clicking empty square or opponent piece)
        // 2. Selecting a different piece of the same color
        // 3. Clicking the same piece (deselect)
        if (selectedPiece != null) {
            // If clicking the same square, deselect
            if (row == selectedPieceRow && col == selectedPieceCol) {
                System.out.println("DEBUG: Deselecting current piece");
                selectedPiece = null;
                return false;
            }

            // If clicking a piece of the same color, select that piece instead
            if (clickedPiece != null && clickedPiece.isWhite() == isWhiteTurn) {
                System.out.println("DEBUG: Selecting different piece of same color");
                selectedPiece = clickedPiece;
                selectedPieceRow = row;
                selectedPieceCol = col;
                return true;
            }

            // Otherwise, this is a move attempt
            System.out.println("DEBUG: Attempting move from (" + selectedPieceRow + "," +
                    selectedPieceCol + ") to (" + row + "," + col + ")");
            return makeMove(row, col);
        }

        // No piece currently selected - try to select the clicked piece
        if (clickedPiece != null && clickedPiece.isWhite() == isWhiteTurn) {
            System.out.println("DEBUG: Selecting new piece");
            this.selectedPiece = clickedPiece;
            this.selectedPieceRow = row;
            this.selectedPieceCol = col;
            return true;
        }

        System.out.println("DEBUG: Cannot select - wrong turn or empty square");
        return false;
    }

    // Also fix the makeMove method to ensure proper cleanup
    public boolean makeMove(int destRow, int destCol) {
        if (selectedPiece == null) {
            System.out.println("DEBUG: makeMove called but no piece selected");
            return false;
        }

        System.out.println("DEBUG: makeMove from (" + selectedPieceRow + "," + selectedPieceCol +
                ") to (" + destRow + "," + destCol + ")");

        // Store move information for PGN notation
        int srcRow = selectedPieceRow;
        int srcCol = selectedPieceCol;
        boolean wasCapture = logicBoard.getPieceAt(destRow, destCol) != null;
        boolean wasPawnMove = selectedPiece instanceof Pawn;
        boolean isCastling = selectedPiece instanceof King && Math.abs(destCol - srcCol) == 2;

        boolean moveWasSuccessful = logicBoard.attemptMove(
                selectedPieceRow, selectedPieceCol, destRow, destCol, isWhiteTurn);

        if (moveWasSuccessful) {
            System.out.println("DEBUG: Move successful, switching turns");

            // Handle promotion
            Piece promotedPiece = null;
            Piece movedPiece = logicBoard.getPieceAt(destRow, destCol);
            if (movedPiece instanceof Pawn) {
                if ((movedPiece.isWhite() && destRow == 0) || (!movedPiece.isWhite() && destRow == 7)) {
                    // Promotion occurred, get the promoted piece
                    promotedPiece = logicBoard.getPieceAt(destRow, destCol);
                }
            }

            // Generate PGN notation for the move
            String moveNotation = generateMoveNotation(selectedPiece, srcRow, srcCol,
                    destRow, destCol, wasCapture,
                    isCastling, promotedPiece);

            // Add move to tracker
            moveTracker.addMove(moveNotation);
            moveTracker.updateMoveCounter(wasCapture, wasPawnMove);

            // Add position to tracker for repetition detection
            String position = generatePositionString();
            moveTracker.addPosition(position);

            isWhiteTurn = !isWhiteTurn; // Switch turns
            checkGameEndingConditions();
        } else {
            System.out.println("DEBUG: Move failed");
        }

        // CRITICAL FIX: Always clear the selected piece after a move attempt
        System.out.println("DEBUG: Clearing selected piece");
        this.selectedPiece = null;
        gameWindow.getChessBoardPanel().repaint();
        return moveWasSuccessful;
    }

    private String generateMoveNotation(Piece piece, int srcRow, int srcCol,
                                        int destRow, int destCol, boolean wasCapture,
                                        boolean isCastling, Piece promotedPiece) {
        boolean causesCheck = logicBoard.isKingInCheck(!isWhiteTurn);
        boolean causesCheckmate = causesCheck && !logicBoard.hasLegalMoves(!isWhiteTurn);
        return MoveConverter.convertMoveToNotation(piece, srcRow, srcCol, destRow, destCol,
                logicBoard, wasCapture, causesCheck,
                causesCheckmate, isCastling, promotedPiece);
    }

    private String generatePositionString() {
        StringBuilder sb = new StringBuilder();
        Piece[][] board = logicBoard.getBoardArray();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board[row][col];
                if (piece == null) {
                    sb.append('.');
                } else {
                    char c = piece.getFenChar();
                    sb.append(c);
                }
            }
        }

        // Add turn indicator
        sb.append(isWhiteTurn ? 'W' : 'B');

        return sb.toString();
    }

    public Board getLogicBoard() { return logicBoard; }
    public Clock getWhiteClock() { return this.whiteClock; }
    public Clock getBlackClock() { return this.blackClock; }
    public PGNManager getPGNManager() { return this.pgnManager; }

    private void setupClock(int hh, int mm, int ss) {
        whiteClock = new Clock(hh, mm, ss);
        blackClock = new Clock(hh, mm, ss);
    }

    public void stopTimer() {
        synchronized (timerLock) {
            if (timer != null) {
                timer.stop();
                timer = null;
            }
        }
    }

    public void startTimer() {
        stopTimer(); // Always stop the old timer before starting a new one

        synchronized (timerLock) {
            // Create a new timer that will execute the code inside every 1000ms (1 sec)
            timer = new Timer(1000, e -> {
                // This block of code is the "action" that happens every second
                synchronized (timerLock) {
                    if (timer == null) return;

                    if (isWhiteTurn) {
                        whiteClock.decr();
                        // Tell the UI to update its display
                        SwingUtilities.invokeLater(() -> gameWindow.updateWhiteClock(whiteClock.getTime()));
                        if (whiteClock.outOfTime()) {
                            // Set PGN result
                            pgnManager.setResult("0-1");
                            SwingUtilities.invokeLater(() -> gameWindow.timeOut(true));
                        }
                    } else {
                        blackClock.decr();
                        SwingUtilities.invokeLater(() -> gameWindow.updateBlackClock(blackClock.getTime()));
                        if (blackClock.outOfTime()) {
                            // Set PGN result
                            pgnManager.setResult("1-0");
                            SwingUtilities.invokeLater(() -> gameWindow.timeOut(false));
                        }
                    }
                }
            });
            timer.start();
        }
    }

    private void checkGameEndingConditions() {
        // Check the status of the player WHOSE TURN IT IS NOW.
        boolean canMove = logicBoard.hasLegalMoves(isWhiteTurn);

        // If the current player has no legal moves...
        if (!canMove) {
            // ...we then check if their king is in check.
            if (logicBoard.isKingInCheck(isWhiteTurn)) {
                // No legal moves AND in check? That's CHECKMATE.
                // The player who just moved is the winner.
                System.out.println("GAME OVER: CHECKMATE! " + (isWhiteTurn ? "Black" : "White") + " wins.");
                stopTimer(); // Stop the clocks!

                // Set PGN result
                if (isWhiteTurn) {
                    pgnManager.setResult("0-1"); // Black wins
                } else {
                    pgnManager.setResult("1-0"); // White wins
                }

                // Tell the GameWindow to show the checkmate message
                gameWindow.checkmateOccurred(isWhiteTurn);
            } else {
                // No legal moves and NOT in check? That's STALEMATE.
                System.out.println("GAME OVER: STALEMATE!");
                stopTimer(); // Stop the clocks!

                // Set PGN result
                pgnManager.setResult("1/2-1/2");

                // Tell the GameWindow to show the stalemate message
                gameWindow.stalemateOccurred();
            }
        }

        // Check for other draw conditions
        else if (moveTracker.hasFiftyMoveRule()) {
            stopTimer();
            pgnManager.setResult("1/2-1/2");
            // Could add a method to GameWindow to show fifty-move rule draw
        }
        else if (moveTracker.hasThreefoldRepetition()) {
            stopTimer();
            pgnManager.setResult("1/2-1/2");
            // Could add a method to GameWindow to show threefold repetition draw
        }
    }
}