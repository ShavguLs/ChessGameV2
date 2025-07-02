package com.ShavguLs.chess.server;

import com.ShavguLs.chess.common.logic.*;
import com.ShavguLs.chess.common.MoveObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
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

    // Player nicknames
    private String whitePlayerNickname = "White Player";
    private String blackPlayerNickname = "Black Player";

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
        int hh = 0, mm = 10, ss = 0; // Hardcoded 10 minutes for now
        this.whiteClock = new Clock(hh, mm, ss);
        this.blackClock = new Clock(hh, mm, ss);

        // --- Initialize Game Logic ---
        this.logicBoard = new Board();
        this.logicBoard.setupStandardBoard();

        // --- Initialize PGN System ---
        this.moveTracker = new MoveTracker();
        this.pgnManager = new PGNManager(this.moveTracker);
        // Player names will be set after we receive them from clients
        this.pgnManager.setTimeControl(hh, mm, ss);

        // --- Assign Players ---
        this.whitePlayerSocket = player1;
        this.blackPlayerSocket = player2;
    }

    @Override
    public void run() {
        String gameStatus = "IN_PROGRESS";
        boolean whiteDisconnected = false;
        boolean blackDisconnected = false;

        try {
            setupStreams();
            System.out.println("[SERVER LOG] Game Session started. Streams are set up.");

            // Get player nicknames first
            getPlayerNicknames();

            // Set nicknames in PGN manager
            pgnManager.setPlayerNames(whitePlayerNickname, blackPlayerNickname);

            // Send welcome messages with opponent nicknames
            whiteOut.writeObject("WELCOME:WHITE:" + blackPlayerNickname);
            blackOut.writeObject("WELCOME:BLACK:" + whitePlayerNickname);
            System.out.println("[SERVER LOG] Welcome messages sent.");

            startClockTimer();

            while (gameStatus.equals("IN_PROGRESS")) {
                // Check for game-ending conditions at the start of each loop
                if (logicBoard.isGameOver()) {
                    gameStatus = "NORMAL_CONCLUSION";
                    continue;
                }
                if (checkDrawConditions()) {
                    gameStatus = "DRAW";
                    continue;
                }
                if (isTimeUp()) {
                    gameStatus = "TIMEOUT";
                    continue;
                }

                broadcastGameState();

                boolean isWhiteMoving = logicBoard.isWhiteTurn();
                ObjectInputStream currentTurnInput = isWhiteMoving ? whiteIn : blackIn;
                Socket currentPlayerSocket = isWhiteMoving ? whitePlayerSocket : blackPlayerSocket;

                try {
                    currentPlayerSocket.setSoTimeout(1000); // 1-second timeout for a move
                    MoveObject move = (MoveObject) currentTurnInput.readObject();

                    isClockRunning.set(false);
                    processMove(move, isWhiteMoving);
                    isClockRunning.set(true);

                } catch (SocketTimeoutException e) {
                    // Expected, do nothing, let the loop re-check game state
                } catch (ClassNotFoundException | IOException e) {
                    // A real error, likely a disconnect
                    System.err.println("[SERVER ERROR] Player disconnected or sent invalid data: " + e.getMessage());
                    gameStatus = "DISCONNECT";
                    if (isWhiteMoving) whiteDisconnected = true; else blackDisconnected = true;
                }
            }

            // Game Over Sequence
            System.out.println("[SERVER LOG] Game over. Reason: " + gameStatus);
            stopClockTimer();

            String finalResult;
            String finalPgnResult;

            if (gameStatus.equals("DISCONNECT")) {
                if (whiteDisconnected) {
                    finalResult = "0-1 (" + whitePlayerNickname + " disconnected)";
                    finalPgnResult = "0-1";
                } else {
                    finalResult = "1-0 (" + blackPlayerNickname + " disconnected)";
                    finalPgnResult = "1-0";
                }
            } else if (gameStatus.equals("TIMEOUT")) {
                if (whiteClock.outOfTime()) {
                    finalResult = "0-1 (" + blackPlayerNickname + " wins on time)";
                    finalPgnResult = "0-1";
                } else {
                    finalResult = "1-0 (" + whitePlayerNickname + " wins on time)";
                    finalPgnResult = "1-0";
                }
            } else {
                finalResult = getFinalGameResult();
                finalPgnResult = extractResultCode(finalResult);
            }

            pgnManager.setResult(finalPgnResult);

            broadcastGameState();
            broadcastMessage("GAMEOVER:" + finalResult);
            System.out.println("[SERVER LOG] Sent GAMEOVER message: " + finalResult);

            String finalPGN = pgnManager.getPGNText();
            broadcastMessage("FINAL_PGN:" + finalPGN);
            System.out.println("[SERVER LOG] Sent final PGN.");
            DatabaseManager.saveGame(this.pgnManager);

        } catch (Exception e) {
            System.err.println("[SERVER ERROR] Unhandled exception in GameSession: " + e.getMessage());
            e.printStackTrace();
            broadcastMessage("ERROR:A server error occurred. The game cannot continue.");
        } finally {
            closeConnections();
        }
    }

    private void getPlayerNicknames() throws IOException {
        try {
            // Request nicknames from both players
            whiteOut.writeObject("REQUEST_NICKNAME");
            blackOut.writeObject("REQUEST_NICKNAME");

            // Set timeout for nickname reception
            whitePlayerSocket.setSoTimeout(5000);
            blackPlayerSocket.setSoTimeout(5000);

            // Receive nicknames
            String whiteNick = (String) whiteIn.readObject();
            String blackNick = (String) blackIn.readObject();

            if (whiteNick != null && !whiteNick.isEmpty()) {
                whitePlayerNickname = whiteNick;
            }
            if (blackNick != null && !blackNick.isEmpty()) {
                blackPlayerNickname = blackNick;
            }

            System.out.println("[SERVER LOG] Players: " + whitePlayerNickname + " (White) vs " + blackPlayerNickname + " (Black)");

        } catch (Exception e) {
            System.err.println("[SERVER ERROR] Could not get player nicknames: " + e.getMessage());
            // continue with default names
        }
    }

    private void processMove(MoveObject move, boolean isWhiteMoving) throws IOException {
        System.out.println("\n[SERVER PROCESS_MOVE] -----------------------------");
        System.out.println("[SERVER PROCESS_MOVE] Received move from " + (isWhiteMoving ? whitePlayerNickname : blackPlayerNickname) + ": " + move);
        System.out.println("[SERVER PROCESS_MOVE] Board's current turn is: " + (logicBoard.isWhiteTurn() ? "White" : "Black"));

        int srcRow = move.startRow();
        int srcCol = move.startCol();
        int destRow = move.endRow();
        int destCol = move.endCol();

        Piece pieceToMove = logicBoard.getPieceAt(srcRow, srcCol);
        if (pieceToMove == null) {
            System.out.println("[SERVER PROCESS_MOVE] FAILED: No piece at source square (" + srcRow + "," + srcCol + ").");
            sendInvalidMoveMessage(isWhiteMoving, "No piece at source square.");
            return;
        }
        System.out.println("[SERVER PROCESS_MOVE] Piece to move: " + pieceToMove.getClass().getSimpleName());

        // Gather pre-move info for PGN
        boolean wasCapture = logicBoard.getPieceAt(destRow, destCol) != null ||
                (pieceToMove instanceof Pawn && logicBoard.isEnPassantMove(srcRow, srcCol, destRow, destCol, isWhiteMoving));
        boolean wasPawnMove = pieceToMove instanceof Pawn;
        boolean isCastling = pieceToMove instanceof King && Math.abs(destCol - srcCol) == 2;

        Piece promotionChoice = null;
        char promoChar = move.promotionPiece();
        if (promoChar != ' ') {
            boolean isWhite = isWhiteMoving;
            if (promoChar == 'q' || promoChar == 'Q') promotionChoice = new Queen(isWhite);
            else if (promoChar == 'r' || promoChar == 'R') promotionChoice = new Rook(isWhite);
            else if (promoChar == 'b' || promoChar == 'B') promotionChoice = new Bishop(isWhite);
            else if (promoChar == 'n' || promoChar == 'N') promotionChoice = new Knight(isWhite);
            System.out.println("[SERVER PROCESS_MOVE] Promotion choice detected: " + promotionChoice.getClass().getSimpleName());
        }

        System.out.println("[SERVER PROCESS_MOVE] Calling logicBoard.attemptMove...");
        boolean moveWasSuccessful = logicBoard.attemptMove(srcRow, srcCol, destRow, destCol, isWhiteMoving, promotionChoice);
        System.out.println("[SERVER PROCESS_MOVE] ...attemptMove returned: " + moveWasSuccessful);

        if (moveWasSuccessful) {
            System.out.println("[SERVER PROCESS_MOVE] SUCCESS: Move was legal. Recording to PGN.");
            // Post-move PGN logic
            Piece movedPiece = logicBoard.getPieceAt(destRow, destCol);
            Piece promotedPieceForPgn = null;
            if (wasPawnMove && (destRow == 0 || destRow == 7)) {
                promotedPieceForPgn = movedPiece;
            }

            boolean opponentIsWhite = logicBoard.isWhiteTurn();
            boolean causesCheck = logicBoard.isKingInCheck(opponentIsWhite);
            boolean causesCheckmate = causesCheck && !logicBoard.hasLegalMoves(opponentIsWhite);

            String moveNotation = MoveConverter.convertMoveToNotation(pieceToMove, srcRow, srcCol, destRow, destCol,
                    logicBoard, wasCapture, causesCheck,
                    causesCheckmate, isCastling, promotedPieceForPgn);
            System.out.println("[SERVER PROCESS_MOVE] Generated PGN notation: " + moveNotation);

            moveTracker.addMove(moveNotation);
            moveTracker.updateMoveCounter(wasCapture, wasPawnMove);
            moveTracker.addPosition(logicBoard.generatePositionString());

        } else {
            System.out.println("[SERVER PROCESS_MOVE] FAILED: Move was illegal according to Board.attemptMove.");
            sendInvalidMoveMessage(isWhiteMoving, "The move is illegal.");
        }
        System.out.println("[SERVER PROCESS_MOVE] -----------------------------\n");
    }

    private String getFinalGameResult() {
        if (moveTracker.hasFiftyMoveRule()) return "1/2-1/2 (Draw by 50-move rule)";
        if (moveTracker.hasThreefoldRepetition()) return "1/2-1/2 (Draw by threefold repetition)";
        if (whiteClock.outOfTime()) return "0-1 (" + blackPlayerNickname + " wins on time)";
        if (blackClock.outOfTime()) return "1-0 (" + whitePlayerNickname + " wins on time)";

        // Get result from board state (checkmate/stalemate)
        String result = logicBoard.getGameResult();
        if (result.equals("1-0 (White wins)")) {
            return "1-0 (" + whitePlayerNickname + " wins by checkmate)";
        } else if (result.equals("0-1 (Black wins)")) {
            return "0-1 (" + blackPlayerNickname + " wins by checkmate)";
        }
        return result;
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
        try {
            whiteOut = new ObjectOutputStream(whitePlayerSocket.getOutputStream());
            whiteOut.flush();
            blackOut = new ObjectOutputStream(blackPlayerSocket.getOutputStream());
            blackOut.flush();

            // Small delay to ensure both sides are ready
            Thread.sleep(100);

            whiteIn = new ObjectInputStream(whitePlayerSocket.getInputStream());
            blackIn = new ObjectInputStream(blackPlayerSocket.getInputStream());
            System.out.println("[SERVER LOG] Streams successfully established for both players.");
        } catch (InterruptedException e) {
            throw new IOException("Stream setup interrupted", e);
        }
    }

    private void startClockTimer() {
        isClockRunning.set(true);
        clockExecutor.scheduleAtFixedRate(() -> {
            if (isClockRunning.get()) {
                Clock currentClock = logicBoard.isWhiteTurn() ? whiteClock : blackClock;
                boolean wasOutOfTime = currentClock.outOfTime();
                currentClock.decr();
                broadcastMessage(String.format("CLOCK_UPDATE:%s;%s", whiteClock.getTime(), blackClock.getTime()));

                if (currentClock.outOfTime() && !wasOutOfTime) {
                    System.out.println("A player has run out of time.");
                    isClockRunning.set(false);
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

    private boolean isTimeUp() {
        return whiteClock.outOfTime() || blackClock.outOfTime();
    }
}