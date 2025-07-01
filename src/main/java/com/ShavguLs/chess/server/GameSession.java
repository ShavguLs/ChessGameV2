package com.ShavguLs.chess.server;

import com.ShavguLs.chess.common.logic.*;
import com.ShavguLs.chess.common.MoveObject;

import java.io.IOException;
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
            whitePlayerSocket.setSoTimeout(1000);
            blackPlayerSocket.setSoTimeout(1000);

            whiteOut.writeObject("WELCOME:WHITE:Player 2 (Black)");
            blackOut.writeObject("WELCOME:BLACK:Player 1 (White)");
            System.out.println("[SERVER LOG] Welcome messages sent. White player is assigned.");

            startClockTimer();

            while (!logicBoard.isGameOver() && !checkDrawConditions() && !isTimeUp()) {
                broadcastGameState(); // Send state at the start of each turn's thinking time

                boolean isWhiteMoving = logicBoard.isWhiteTurn();
                ObjectInputStream currentTurnInput = isWhiteMoving ? whiteIn : blackIn;

                try {
                    // This will now wait for up to 1 second.
                    MoveObject move = (MoveObject) currentTurnInput.readObject();

                    // If we get here, a move was actually received.
                    isClockRunning.set(false); // Pause clock while processing
                    processMove(move, isWhiteMoving);
                    if (!logicBoard.isGameOver() && !checkDrawConditions() && !isTimeUp()) {
                        isClockRunning.set(true); // Resume clock for next turn
                    }

                } catch (SocketTimeoutException e) {
                    // THIS IS EXPECTED! It means no move was sent in the last second.
                    // We do nothing here but let the loop continue.
                    // The while condition at the top will re-check if time has run out.
                    // System.out.println("[SERVER HEARTBEAT] No move received, checking game state."); // Optional debug log
                } catch (ClassNotFoundException | IOException e) {
                    // This is a real error (like a disconnect)
                    System.err.println("[SERVER ERROR] Player disconnected or sent invalid data: " + e.getMessage());
                    break; // Break the loop on a real error
                }
            }

            // --- Game Over Sequence ---
            System.out.println("[SERVER LOG] Game over condition met.");
            stopClockTimer();
            String result = getFinalGameResult();
            pgnManager.setResult(extractResultCode(result));

            System.out.println("[SERVER DEBUG] Moves recorded in MoveTracker: " + moveTracker.getAllMoves().size());
            System.out.println("[SERVER DEBUG] Last move was: " + moveTracker.getLastMove());

            broadcastGameState(); // Send final board state
            broadcastMessage("GAMEOVER:" + result);
            System.out.println("[SERVER LOG] Sent GAMEOVER message: " + result);

            String finalPGN = pgnManager.getPGNText();
            broadcastMessage("FINAL_PGN:" + finalPGN);
            System.out.println("[SERVER LOG] Sent final PGN.");
            DatabaseManager.saveGame(this.pgnManager);

        } catch (Exception e) {
            System.err.println("[SERVER ERROR] Unhandled exception in GameSession run loop: " + e.getMessage());
            e.printStackTrace();
            broadcastMessage("ERROR:A player has disconnected. The game cannot continue.");
        } finally {
            closeConnections();
        }
    }

    private void processMove(MoveObject move, boolean isWhiteMoving) throws IOException {
        System.out.println("\n[SERVER PROCESS_MOVE] -----------------------------");
        System.out.println("[SERVER PROCESS_MOVE] Received move from " + (isWhiteMoving ? "White" : "Black") + ": " + move);
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

        // This is the SINGLE, definitive call.
        System.out.println("[SERVER PROCESS_MOVE] Calling logicBoard.attemptMove...");
        boolean moveWasSuccessful = logicBoard.attemptMove(srcRow, srcCol, destRow, destCol, isWhiteMoving, promotionChoice);
        System.out.println("[SERVER PROCESS_MOVE] ...attemptMove returned: " + moveWasSuccessful);


        if (moveWasSuccessful) {
            System.out.println("[SERVER PROCESS_MOVE] SUCCESS: Move was legal. Recording to PGN.");
            // --- Post-move PGN logic ---
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
                broadcastMessage(String.format("CLOCK_UPDATE:%s;%s", whiteClock.getTime(), blackClock.getTime()));

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


    private boolean isTimeUp() {
        return whiteClock.outOfTime() || blackClock.outOfTime();
    }
}