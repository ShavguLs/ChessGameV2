package com.ShavguLs.chess.client.controller;

import com.ShavguLs.chess.client.view.PromotionHandler;
import com.ShavguLs.chess.common.MoveObject;
import com.ShavguLs.chess.common.logic.*;
import com.ShavguLs.chess.client.view.GameWindow;

import javax.swing.*;

// Make sure it implements the listener interface
public class GameController implements ServerUpdateListener {

    // --- Fields for both Local and Online games ---
    private Board logicBoard;
    private boolean isWhiteTurn;
    private Piece selectedPiece;
    private int selectedPieceRow;
    private int selectedPieceCol;
    private final GameWindow gameWindow;
    private boolean isAwaitingPromotion = false;

    // --- Fields for LOCAL games ONLY ---
    private Clock whiteClock;
    private Clock blackClock;
    private Timer timer;
    private final Object timerLock = new Object();
    private MoveTracker moveTracker;
    private PGNManager pgnManager;
    private String whitePlayerName = "White";
    private String blackPlayerName = "Black";

    // --- Fields for ONLINE games ONLY ---
    private NetworkClient networkClient;
    private boolean isOnlineGame = false;
    private boolean isPlayerWhite; // What color am I in an online game?

    // ======================================================================
    // --- CONSTRUCTOR 1: FOR LOCAL GAMES (Your Original Constructor) ---
    // ======================================================================
    public GameController(int hh, int mm, int ss, GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        this.isOnlineGame = false; // This is a local game
        this.moveTracker = new MoveTracker();
        this.pgnManager = new PGNManager(moveTracker);
        this.pgnManager.setTimeControl(hh, mm, ss);
        setUpNewGame();
        if (hh > 0 || mm > 0 || ss > 0) {
            setupClock(hh, mm, ss);
            startTimer();
        }
    }

    // ======================================================================
    // --- CONSTRUCTOR 2: FOR ONLINE GAMES (The New Constructor) ---
    // ======================================================================
    public GameController(GameWindow gameWindow, String serverAddress, int port, String playerNickname) {
        this.gameWindow = gameWindow;
        this.isOnlineGame = true; // This is an online game

        // We disable local-only components. The server handles them.
        this.moveTracker = null;
        this.pgnManager = null;
        this.timer = null;
        this.whiteClock = null;
        this.blackClock = null;

        // The board is needed for display, but it starts empty. Server will populate it.
        this.logicBoard = new Board();

        this.networkClient = new NetworkClient(serverAddress, port, this);
        this.networkClient.setPlayerNickname(playerNickname); // Set the nickname
        if (!networkClient.connect()) {
            JOptionPane.showMessageDialog(gameWindow, "Failed to connect to the server.", "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    // --- Core Game Logic Methods (Now with Online/Local split) ---

    public boolean selectPiece(int row, int col) {
        // In online games, you can't move if it's not your turn.
        if (isOnlineGame && isWhiteTurn != isPlayerWhite) {
            return false;
        }

        if (isAwaitingPromotion) {
            return false; // Ignore all clicks while waiting for promotion choice
        }

        Piece clickedPiece = logicBoard.getPieceAt(row, col);

        if (selectedPiece != null) {
            if (row == selectedPieceRow && col == selectedPieceCol) {
                selectedPiece = null;
                return false;
            }
            if (clickedPiece != null && clickedPiece.isWhite() == isWhiteTurn) {
                selectedPiece = clickedPiece;
                selectedPieceRow = row;
                selectedPieceCol = col;
                return true;
            }
            return makeMove(row, col);
        }

        if (clickedPiece != null && clickedPiece.isWhite() == isWhiteTurn) {
            this.selectedPiece = clickedPiece;
            this.selectedPieceRow = row;
            this.selectedPieceCol = col;
            return true;
        }
        return false;
    }

    public boolean makeMove(int destRow, int destCol) {
        if (selectedPiece == null) {
            return false;
        }

        // --- 1. DECLARE AND CALCULATE PROMOTION *ONCE* AT THE TOP ---
        boolean isPromotion = (selectedPiece instanceof Pawn) &&
                ((selectedPiece.isWhite() && destRow == 0) ||
                        (!selectedPiece.isWhite() && destRow == 7));

        if (isOnlineGame) {
            // --- ONLINE MOVE LOGIC ---
            if (isPromotion) {
                isAwaitingPromotion = true;
                Piece chosenPiece = PromotionHandler.getPromotionChoice(gameWindow, isPlayerWhite);
                isAwaitingPromotion = false;
                if (chosenPiece == null) {
                    this.selectedPiece = null;
                    gameWindow.getChessBoardPanel().repaint();
                    return false;
                }
                char promoChar = chosenPiece.getFenChar();
                networkClient.sendMove(new MoveObject(selectedPieceRow, selectedPieceCol, destRow, destCol, promoChar));
            } else {
                networkClient.sendMove(new MoveObject(selectedPieceRow, selectedPieceCol, destRow, destCol));
            }
            this.selectedPiece = null;
            return true;

        } else {
            // --- LOCAL MOVE LOGIC ---
            int srcRow = selectedPieceRow;
            int srcCol = selectedPieceCol;

            Piece promotionChoice = null;
            if (isPromotion) {
                isAwaitingPromotion = true;
                promotionChoice = PromotionHandler.getPromotionChoice(gameWindow, selectedPiece.isWhite());
                isAwaitingPromotion = false;
                if (promotionChoice == null) {
                    this.selectedPiece = null;
                    gameWindow.getChessBoardPanel().repaint();
                    return false;
                }
            }

            boolean wasCapture = logicBoard.getPieceAt(destRow, destCol) != null ||
                    (selectedPiece instanceof Pawn && logicBoard.isEnPassantMove(srcRow, srcCol, destRow, destCol, isWhiteTurn));
            boolean wasPawnMove = selectedPiece instanceof Pawn;
            boolean isCastling = selectedPiece instanceof King && Math.abs(destCol - srcCol) == 2;

            boolean moveWasSuccessful = logicBoard.attemptMove(srcRow, srcCol, destRow, destCol, isWhiteTurn, promotionChoice);

            if (moveWasSuccessful) {
                this.isWhiteTurn = logicBoard.isWhiteTurn();
                String moveNotation = generateMoveNotation(selectedPiece, srcRow, srcCol, destRow, destCol,
                        wasCapture, isCastling, logicBoard.getPieceAt(destRow, destCol));
                moveTracker.addMove(moveNotation);
                moveTracker.updateMoveCounter(wasCapture, wasPawnMove);
                moveTracker.addPosition(logicBoard.generatePositionString());
                checkGameEndingConditions();
            }

            this.selectedPiece = null;
            gameWindow.getChessBoardPanel().repaint();
            return moveWasSuccessful;
        }
    }

    // --- Local Game Helper Methods ---

    public void setPlayerNames(String whiteName, String blackName) {
        if (!isOnlineGame) {
            this.whitePlayerName = whiteName;
            this.blackPlayerName = blackName;
            this.pgnManager.setPlayerNames(whiteName, blackName);
        }
    }

    public void setUpNewGame() {
        stopTimer();
        this.logicBoard = new Board();
        this.logicBoard.setupStandardBoard();
        this.isWhiteTurn = true;
        this.selectedPiece = null;
        if (moveTracker != null) {
            this.moveTracker.reset();
            this.pgnManager.reset();
            this.pgnManager.setPlayerNames(whitePlayerName, blackPlayerName);
        }
        if (whiteClock != null) {
            int originalSeconds = whiteClock.getInitialSeconds();
            int hh = originalSeconds / 3600;
            int mm = (originalSeconds % 3600) / 60;
            int ss = originalSeconds % 60;
            setupClock(hh, mm, ss);
            gameWindow.updateWhiteClock(whiteClock.getTime());
            gameWindow.updateBlackClock(blackClock.getTime());
            startTimer();
        }
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

    private void checkGameEndingConditions() {
        if (logicBoard.isGameOver()) {
            stopTimer();
            if (logicBoard.isKingInCheck(isWhiteTurn)) {
                pgnManager.setResult(isWhiteTurn ? "0-1" : "1-0");
                gameWindow.checkmateOccurred(isWhiteTurn);
            } else {
                pgnManager.setResult("1/2-1/2");
                gameWindow.stalemateOccurred();
            }
        } else if (moveTracker.hasFiftyMoveRule() || moveTracker.hasThreefoldRepetition()) {
            stopTimer();
            pgnManager.setResult("1/2-1/2");
            gameWindow.stalemateOccurred(); // Or a custom draw message
        }
    }


    // --- Clock Methods (for local games) ---

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
        stopTimer();
        synchronized (timerLock) {
            timer = new Timer(1000, e -> {
                synchronized (timerLock) {
                    if (timer == null) return;
                    if (isWhiteTurn) {
                        whiteClock.decr();
                        SwingUtilities.invokeLater(() -> gameWindow.updateWhiteClock(whiteClock.getTime()));
                        if (whiteClock.outOfTime()) {
                            pgnManager.setResult("0-1");
                            SwingUtilities.invokeLater(() -> gameWindow.timeOut(true));
                        }
                    } else {
                        blackClock.decr();
                        SwingUtilities.invokeLater(() -> gameWindow.updateBlackClock(blackClock.getTime()));
                        if (blackClock.outOfTime()) {
                            pgnManager.setResult("1-0");
                            SwingUtilities.invokeLater(() -> gameWindow.timeOut(false));
                        }
                    }
                }
            });
            timer.start();
        }
    }


    // --- Getters ---
    public Board getLogicBoard() { return logicBoard; }
    public Clock getWhiteClock() { return this.whiteClock; }
    public Clock getBlackClock() { return this.blackClock; }
    public PGNManager getPGNManager() { return this.pgnManager; }


    // =================================================================
    // --- IMPLEMENTATION OF ServerUpdateListener INTERFACE METHODS  ---
    // =================================================================

    @Override
    public void onGameStart(String color, String opponentName) {
        this.isPlayerWhite = color.equalsIgnoreCase("WHITE");

        String myName = networkClient.getPlayerNickname();
        if (myName == null || myName.isEmpty()){
            myName = "You";
        }

        gameWindow.setTitle("Online Chess - " + myName + " (" + color + ") vs. " + opponentName);

        if (isPlayerWhite) {
            // If im white, update the display
            gameWindow.updatePlayerNames(myName, opponentName);
        } else {
            // If im black, update the display
            gameWindow.updatePlayerNames(opponentName, myName);
        }

        if (this.isOnlineGame) {
            stopTimer();
        }
    }

    @Override
    public void onGameStateUpdate(String fen) {
        logicBoard.loadFen(fen);
        this.isWhiteTurn = logicBoard.isWhiteTurn();
        this.selectedPiece = null;
        gameWindow.getChessBoardPanel().repaint();
    }

    @Override
    public void onGameOver(String resultMessage) {
        stopTimer();
        JOptionPane.showMessageDialog(gameWindow, resultMessage, "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void onInvalidMove(String reason) {
        JOptionPane.showMessageDialog(gameWindow, "Invalid Move: " + reason, "Move Rejected", JOptionPane.WARNING_MESSAGE);
    }

    @Override
    public void onNetworkError(String errorMessage) {
        stopTimer();
        JOptionPane.showMessageDialog(gameWindow, "Network Error: " + errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void onClockUpdate(String whiteTime, String blackTime) {
        if (isOnlineGame) {
            gameWindow.updateWhiteClock(whiteTime);
            gameWindow.updateBlackClock(blackTime);
        }
    }

    @Override
    public void onReceivePgn(String pgn) {
        if (isOnlineGame) {
            gameWindow.showOnlineGameSaveDialog(pgn);
        }
    }

    public boolean isWhiteTurn() {
        return this.isWhiteTurn;
    }
}