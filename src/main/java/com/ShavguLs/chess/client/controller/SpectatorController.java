package com.ShavguLs.chess.client.controller;

import com.ShavguLs.chess.client.view.SpectatorMode;
import com.ShavguLs.chess.common.logic.Board;
import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class SpectatorController implements Runnable {
    private SpectatorMode spectatorWindow;
    private int gameId;
    private boolean isConnected = false;
    protected Board logicBoard;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private boolean running = false;
    private Timer refreshTimer;

    public SpectatorController(SpectatorMode window, String serverAddress, int port, int gameId) {
        this.spectatorWindow = window;
        this.gameId = gameId;
        this.logicBoard = new Board();

        try {
            socket = new Socket(serverAddress, port);
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println("SPECTATE_GAME:" + gameId);
            Thread.sleep(200);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            Thread.sleep(100);
            in = new ObjectInputStream(socket.getInputStream());
        } catch (Exception e) {
            System.err.println("Connection failed: " + e.getMessage());
        }
    }

    public boolean connect() {
        if (socket != null && in != null) {
            isConnected = true;
            running = true;
            new Thread(this).start();
            startAutoRefresh();
            spectatorWindow.onConnected();
            return true;
        }
        spectatorWindow.onConnectionFailed("Failed to connect");
        return false;
    }

    public void disconnect() {
        running = false;
        stopAutoRefresh();
        try { if (socket != null) socket.close(); } catch (Exception e) {}
        isConnected = false;
    }

    private void startAutoRefresh() {
        refreshTimer = new Timer(1000, e -> requestGameState()); // Refresh every 1 second
        refreshTimer.start();
    }

    private void stopAutoRefresh() {
        if (refreshTimer != null) {
            refreshTimer.stop();
            refreshTimer = null;
        }
    }

    public Board getLogicBoard() { return logicBoard; }

    public void requestGameState() {
        try {
            if (out != null && isConnected && running) {
                out.writeObject("REQUEST_UPDATE");
                out.flush();
            }
        } catch (IOException e) {
            // Ignore failed refresh requests
        }
    }

    @Override
    public void run() {
        try {
            while (running && !socket.isClosed()) {
                socket.setSoTimeout(2000); // 2 second timeout
                Object message = in.readObject();
                if (message instanceof String) {
                    handleMessage((String) message);
                }
            }
        } catch (java.net.SocketTimeoutException e) {
            // Normal timeout, continue
            if (running) run();
        } catch (Exception e) {
            if (running) {
                SwingUtilities.invokeLater(() -> spectatorWindow.updateStatus("Connection lost"));
            }
        }
    }

    private void handleMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            if (message.startsWith("SPECTATOR_WELCOME:")) {
                String[] parts = message.substring(18).split(":");
                if (parts.length >= 2) {
                    spectatorWindow.updatePlayerNames(parts[0], parts[1]);
                }
            } else if (message.startsWith("FEN:")) {
                logicBoard.loadFen(message.substring(4));
                spectatorWindow.refreshBoard();
                String turn = logicBoard.isWhiteTurn() ? "White" : "Black";
                spectatorWindow.updateStatus("Current turn: " + turn);
            } else if (message.startsWith("CLOCK_UPDATE:")) {
                // Just update status with current turn info
                String turn = logicBoard.isWhiteTurn() ? "White" : "Black";
                spectatorWindow.updateStatus("Current turn: " + turn);
            } else if (message.startsWith("GAMEOVER:")) {
                stopAutoRefresh();
                spectatorWindow.showGameOver(message.substring(9));
                running = false;
            } else if (message.equals("GAME_NOT_FOUND")) {
                stopAutoRefresh();
                spectatorWindow.updateStatus("Game not found");
                JOptionPane.showMessageDialog(spectatorWindow, "Game not found");
            }
        });
    }
}