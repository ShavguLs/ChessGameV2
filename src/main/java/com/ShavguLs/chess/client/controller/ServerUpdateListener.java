package com.ShavguLs.chess.client.controller;

/**
 * This interface defines the callback methods that our NetworkClient will use
 * to communicate with the GameController. It allows the networking logic to be
 * decoupled from the game logic.
 */
public interface ServerUpdateListener {

    /**
     * Called when the server sends a full game state update.
     * The FEN string represents the entire board layout and whose turn it is.
     * @param fen The FEN string for the current board state.
     */
    void onGameStateUpdate(String fen);

    /**
     * Called when the server assigns a color to this client at the start of the game.
     * @param color "WHITE" or "BLACK".
     * @param opponentName The name of the opponent.
     */
    void onGameStart(String color, String opponentName);

    /**
     * Called when the server declares the game is over.
     * @param resultMessage A message describing the outcome (e.g., "CHECKMATE! White wins.").
     */
    void onGameOver(String resultMessage);

    /**
     * Called when the server rejects a move made by this client.
     * @param reason The reason the move was invalid.
     */
    void onInvalidMove(String reason);

    /**
     * Called when an error occurs in the network communication.
     * @param errorMessage The error message.
     */
    void onNetworkError(String errorMessage);

    // ... (keep the existing methods)

    /**
     * Called when the server sends updated clock times.
     * @param whiteTime The formatted time string for white's clock.
     * @param blackTime The formatted time string for black's clock.
     */
    void onClockUpdate(String whiteTime, String blackTime);

    /**
     * Called when the server sends the final PGN string at the end of the game.
     * @param pgn The complete PGN text of the game.
     */
    void onReceivePgn(String pgn);
}