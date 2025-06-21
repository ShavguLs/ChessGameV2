package com.ShavguLs.chess;

import com.ShavguLs.chess.logic.*;
import com.ShavguLs.chess.common.MoveObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameSession implements Runnable {

    // Network and Core Logic
    private final Socket whitePlayerSocket;
    private final Socket blackPlayerSocket;
    private ObjectOutputStream whiteOut;
    private ObjectInputStream whiteIn;
    private ObjectOutputStream blackOut;
    private ObjectInputStream blackIn;
    private final Board logicBoard;

    // Clock Management
    private final Clock whiteClock;
    private final Clock blackClock;
    private final ScheduledExecutorService clockExecutor = Executors.newSingleThreadScheduledExecutor();
    private final AtomicBoolean isClockRunning = new AtomicBoolean(false);

    // PGN and Game Recording Logic (Using your classes)
    private final MoveTracker moveTracker;
    private final PGNManager pgnManager;

    public GameSession(Socket player1, Socket player2) {
        // --- Initialize Time ---
        int hh = 0, mm = 5, ss = 0; // Hardcoded 5 minutes for now
        this.whiteClock = new Clock(hh, mm, ss);
        this.blackClock = new Clock(hh, mm, ss);

        // --- Initialize Game Logic ---
        this.logicBoard = new Board();
        this.logicBoard.setupStandardBoard();

        // --- Initialize PGN System ---
        this.moveTracker = new MoveTracker();
        this.pgnManager = new PGNManager(this.moveTracker);
        this.pgnManager.setPlayerNames("Player 1 (White)", "Player 2 (Black)"); // Can be improved with login system
        this.pgnManager.setTimeControl(hh, mm, ss);
        // We can set other PGN headers like Event, Site, etc. here if desired.

        // --- Assign Players ---
        this.whitePlayerSocket = player1;
        this.blackPlayerSocket = player2;
    }

    @Override
    public void run() {
        try {
            setupStreams();
            System.out.println("[SERVER LOG] Game Session started. Streams are set up.");

            whiteOut.writeObject("WELCOME:WHITE:Player 2 (Black)");
            blackOut.writeObject("WELCOME:BLACK:Player 1 (White)");
            System.out.println("[SERVER LOG] Welcome messages sent. White player is assigned.");

            startClockTimer();

            while (!logicBoard.isGameOver() && !checkDrawConditions()) {
                broadcastGameState();

                boolean isWhiteMoving = logicBoard.isWhiteTurn();
                System.out.println("\n[SERVER LOG] ----- NEW TURN -----");
                System.out.println("[SERVER LOG] It is " + (isWhiteMoving ? "WHITE's" : "BLACK's") + " turn to move.");

                ObjectInputStream currentTurnInput = isWhiteMoving ? whiteIn : blackIn;
                Socket currentSocket = isWhiteMoving ? whitePlayerSocket : blackPlayerSocket;

                System.out.println("[SERVER LOG] Waiting for move from: " + currentSocket.getInetAddress());

                // This is a blocking call, it waits for the client to send a move
                MoveObject move = (MoveObject) currentTurnInput.readObject();
                System.out.println("[SERVER LOG] Received MoveObject: " + move);

                isClockRunning.set(false);

                processMove(move, isWhiteMoving);

                if (!logicBoard.isGameOver() && !checkDrawConditions()) {
                    isClockRunning.set(true);
                }
            }

            // --- Game Over Sequence ---
            System.out.println("[SERVER LOG] Game over condition met.");
            stopClockTimer();
            String result = getFinalGameResult();
            pgnManager.setResult(extractResultCode(result));

            broadcastGameState(); // Send final board state
            broadcastMessage("GAMEOVER:" + result);
            System.out.println("[SERVER LOG] Sent GAMEOVER message: " + result);

            String finalPGN = pgnManager.getPGNText();
            broadcastMessage("FINAL_PGN:" + finalPGN);
            System.out.println("[SERVER LOG] Sent final PGN.");

        } catch (Exception e) {
            System.err.println("[SERVER ERROR] Unhandled exception in GameSession run loop: " + e.getMessage());
            e.printStackTrace();
            broadcastMessage("ERROR:A player has disconnected. The game cannot continue.");
        } finally {
            closeConnections();
        }
    }

    private void processMove(MoveObject move, boolean isWhiteMoving) throws IOException {
        System.out.println("[SERVER LOG] Processing move: " + move);
        int srcRow = move.startRow();
        int srcCol = move.startCol();
        int destRow = move.endRow();
        int destCol = move.endCol();

        Piece pieceToMove = logicBoard.getPieceAt(srcRow, srcCol);
        if (pieceToMove == null) {
            System.out.println("[SERVER LOG] Move failed: No piece at source square.");
            sendInvalidMoveMessage(isWhiteMoving, "No piece at source square.");
            return;
        }

        Piece promotionChoice = null;
        char promoChar = move.promotionPiece();
        if (promoChar != ' ') {
            boolean isWhite = isWhiteMoving;
            if (promoChar == 'q' || promoChar == 'Q') promotionChoice = new Queen(isWhite);
            else if (promoChar == 'r' || promoChar == 'R') promotionChoice = new Rook(isWhite);
            else if (promoChar == 'b' || promoChar == 'B') promotionChoice = new Bishop(isWhite);
            else if (promoChar == 'n' || promoChar == 'N') promotionChoice = new Knight(isWhite);
            System.out.println("[SERVER LOG] Promotion piece selected: " + promoChar);
        }

        // The single, definitive call that modifies the board
        boolean moveWasSuccessful = logicBoard.attemptMove(srcRow, srcCol, destRow, destCol, isWhiteMoving, promotionChoice);

        System.out.println("[SERVER LOG] Board.attemptMove returned: " + moveWasSuccessful);

        if (moveWasSuccessful) {
            System.out.println("[SERVER LOG] Move was successful. Board state is now:");
            System.out.println(logicBoard.generateFen());
            // The rest of the logic for PGN...
            
        } else {
            System.out.println("[SERVER LOG] Move was illegal. Sending invalid move message.");
            sendInvalidMoveMessage(isWhiteMoving, "The move is illegal.");
        }
    }

    private String getFinalGameResult() {
        if (moveTracker.hasFiftyMoveRule()) return "1/2-1/2 (Draw by 50-move rule)";
        if (moveTracker.hasThreefoldRepetition()) return "1/2-1/2 (Draw by threefold repetition)";
        // Check for timeout
        if (whiteClock.outOfTime()) return "0-1 (Black wins on time)";
        if (blackClock.outOfTime()) return "1-0 (White wins on time)";

        // Otherwise, get result from board state (checkmate/stalemate)
        return logicBoard.getGameResult();
    }

    private boolean checkDrawConditions() {
        return moveTracker.hasFiftyMoveRule() || moveTracker.hasThreefoldRepetition();
    }

    // --- Helper Methods ---

    private void sendInvalidMoveMessage(boolean isWhiteMoving, String reason) throws IOException {
        ObjectOutputStream out = isWhiteMoving ? whiteOut : blackOut;
        out.writeObject("INVALID_MOVE:" + reason);
        out.flush();
    }

    private String extractResultCode(String fullResult) {
        if (fullResult.startsWith("1-0")) return "1-0";
        if (fullResult.startsWith("0-1")) return "0-1";
        if (fullResult.startsWith("1/2-1/2")) return "1/2-1/2";
        return "*";
    }

    private void setupStreams() throws IOException {
        whiteOut = new ObjectOutputStream(whitePlayerSocket.getOutputStream());
        whiteIn = new ObjectInputStream(whitePlayerSocket.getInputStream());
        blackOut = new ObjectOutputStream(blackPlayerSocket.getOutputStream());
        blackIn = new ObjectInputStream(blackPlayerSocket.getInputStream());
        // Flush streams to send headers, preventing constructor block
        whiteOut.flush();
        blackOut.flush();
    }

    private void startClockTimer() {
        isClockRunning.set(true);
        clockExecutor.scheduleAtFixedRate(() -> {
            if (isClockRunning.get()) {
                Clock currentClock = logicBoard.isWhiteTurn() ? whiteClock : blackClock;
                boolean wasOutOfTime = currentClock.outOfTime();
                currentClock.decr();
                broadcastMessage(String.format("CLOCK_UPDATE:%s:%s", whiteClock.getTime(), blackClock.getTime()));

                if (currentClock.outOfTime() && !wasOutOfTime) {
                    // This is a simplified check. We'll properly end the game on the main thread.
                    System.out.println("A player has run out of time.");
                    isClockRunning.set(false); // Stop the clock
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    private void stopClockTimer() {
        isClockRunning.set(false);
        clockExecutor.shutdownNow();
    }

    private void broadcastGameState() {
        String fen = logicBoard.generateFen();
        broadcastMessage("FEN:" + fen);
    }

    private void broadcastMessage(String message) {
        try {
            if(whiteOut != null) { whiteOut.writeObject(message); whiteOut.flush(); }
        } catch (IOException e) { System.err.println("Failed to send to white: " + e.getMessage()); }
        try {
            if(blackOut != null) { blackOut.writeObject(message); blackOut.flush(); }
        } catch (IOException e) { System.err.println("Failed to send to black: " + e.getMessage()); }
    }

    private void closeConnections() {
        stopClockTimer();
        try { if(whitePlayerSocket != null) whitePlayerSocket.close(); } catch (IOException e) { /* ignore */ }
        try { if(blackPlayerSocket != null) blackPlayerSocket.close(); } catch (IOException e) { /* ignore */ }
        System.out.println("GameSession ended and connections closed.");
    }
}