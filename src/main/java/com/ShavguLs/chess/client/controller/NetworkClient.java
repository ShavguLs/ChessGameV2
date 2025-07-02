package com.ShavguLs.chess.client.controller;

import com.ShavguLs.chess.common.MoveObject;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class NetworkClient implements Runnable {

    private final String serverAddress;
    private final int serverPort;
    private final ServerUpdateListener listener;
    private ObjectOutputStream out;
    private Socket socket;
    private String playerNickname; // Store the player's nickname

    public NetworkClient(String serverAddress, int serverPort, ServerUpdateListener listener) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.listener = listener;
    }

    public void setPlayerNickname(String nickname) {
        this.playerNickname = nickname;
    }

    public String getPlayerNickname() {
        return this.playerNickname;
    }

    /**
     * Attempts to connect to the server.
     * @return true if connection is successful, false otherwise.
     */
    public boolean connect() {
        try {
            socket = new Socket(serverAddress, serverPort);

            // First, send a plain text line to identify this connection as a game player
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println("PLAY_CHESS");
            writer.flush();

            // IMPORTANT
            Thread.sleep(100);

            // Set up the object stream for sending game objects
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();

            new Thread(this).start(); // Start the listener thread
            return true;
        } catch (UnknownHostException e) {
            SwingUtilities.invokeLater(() -> listener.onNetworkError("Connection failed: Unknown host '" + serverAddress + "'"));
            return false;
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> listener.onNetworkError("Connection failed: Could not connect to server at '" + serverAddress + ":" + serverPort + "'"));
            return false;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This is the heart of the client's listening mechanism.
     * It runs in a separate thread and continuously waits for objects from the server.
     */
    @Override
    public void run() {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            while (!socket.isClosed()) {
                Object serverMessage = in.readObject();

                // Handle nickname request immediately in this thread
                if (serverMessage instanceof String && serverMessage.equals("REQUEST_NICKNAME")) {
                    sendNickname();
                } else {
                    // Other messages go to the UI thread
                    SwingUtilities.invokeLater(() -> handleServerMessage(serverMessage));
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            if (!socket.isClosed()) {
                SwingUtilities.invokeLater(() -> listener.onNetworkError("Lost connection to server."));
            }
        }
    }

    /**
     * Sends the player's nickname to the server when requested.
     */
    private void sendNickname() {
        try {
            if (playerNickname != null && !playerNickname.isEmpty()) {
                out.writeObject(playerNickname);
            } else {
                out.writeObject("Anonymous");
            }
            out.flush();
        } catch (IOException e) {
            System.err.println("Failed to send nickname: " + e.getMessage());
        }
    }

    /**
     * Sends a MoveObject to the server.
     * @param move The move to send.
     */
    public void sendMove(MoveObject move) {
        try {
            out.writeObject(move);
            out.flush();
        } catch (IOException e) {
            listener.onNetworkError("Failed to send move: " + e.getMessage());
        }
    }

    /**
     * Disconnects from the server gracefully.
     */
    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error while disconnecting: " + e.getMessage());
        }
    }

    /**
     * A helper method to process messages received from the server.
     */
    private void handleServerMessage(Object message) {
        if (message instanceof String) {
            String text = (String) message;

            if (text.startsWith("FEN:")) {
                listener.onGameStateUpdate(text.substring(4));
            } else if (text.startsWith("WELCOME:")) {
                String[] parts = text.substring(8).split(":", 2);
                String color = parts[0];
                String opponentName = parts.length > 1 ? parts[1] : "Opponent";
                listener.onGameStart(color, opponentName);
            } else if (text.startsWith("GAMEOVER:")) {
                listener.onGameOver(text.substring(9));
            } else if (text.startsWith("INVALID_MOVE:")) {
                listener.onInvalidMove(text.substring(13));
            } else if (text.startsWith("CLOCK_UPDATE:")) {
                String[] parts = text.substring(13).split(";");
                if (parts.length == 2) {
                    listener.onClockUpdate(parts[0], parts[1]);
                }
            } else if (text.startsWith("FINAL_PGN:")) {
                listener.onReceivePgn(text.substring(10));
            } else {
                System.out.println("Server says: " + text);
            }
        }
    }
}