package com.ShavguLs.chess.controller;

import com.ShavguLs.chess.common.MoveObject;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class NetworkClient implements Runnable {

    private final String serverAddress;
    private final int serverPort;
    private final ServerUpdateListener listener; // This will be our GameController
    private ObjectOutputStream out;
    private Socket socket;

    public NetworkClient(String serverAddress, int serverPort, ServerUpdateListener listener) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.listener = listener;
    }

    /**
     * Attempts to connect to the server.
     * @return true if connection is successful, false otherwise.
     */
    public boolean connect() {
        try {
            socket = new Socket(serverAddress, serverPort);
            out = new ObjectOutputStream(socket.getOutputStream());
            // The input stream is handled in the run() method's thread.
            new Thread(this).start(); // Start the listener thread
            return true;
        } catch (UnknownHostException e) {
            listener.onNetworkError("Connection failed: Unknown host '" + serverAddress + "'");
            return false;
        } catch (IOException e) {
            listener.onNetworkError("Connection failed: Could not connect to server at '" + serverAddress + ":" + serverPort + "'");
            return false;
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
                // We use SwingUtilities.invokeLater to ensure that any UI updates
                // happen on the correct thread (the Event Dispatch Thread).
                SwingUtilities.invokeLater(() -> handleServerMessage(serverMessage));
            }
        } catch (IOException | ClassNotFoundException e) {
            if (!socket.isClosed()) {
                SwingUtilities.invokeLater(() -> listener.onNetworkError("Lost connection to server."));
            }
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
            // Log or handle the error, but the main goal is to close the socket.
            System.err.println("Error while disconnecting: " + e.getMessage());
        }
    }

    /**
     * A helper method to process messages received from the server.
     * This is where we'll define our "client-side protocol".
     */
    private void handleServerMessage(Object message) {
        if (message instanceof String) {
            String text = (String) message;

            // Use startsWith for safety, as messages might have variable parts.
            if (text.startsWith("FEN:")) {
                listener.onGameStateUpdate(text.substring(4));
            } else if (text.startsWith("WELCOME:")) { // e.g., "WELCOME:WHITE:OpponentName"
                String[] parts = text.substring(8).split(":", 2);
                String color = parts[0];
                String opponentName = parts.length > 1 ? parts[1] : "Opponent";
                listener.onGameStart(color, opponentName);
            } else if (text.startsWith("GAMEOVER:")) {
                listener.onGameOver(text.substring(9));
            } else if (text.startsWith("INVALID_MOVE:")) {
                listener.onInvalidMove(text.substring(13));
            } else if (text.startsWith("CLOCK_UPDATE:")) { // NEW
                String[] parts = text.substring(13).split(":", 2);
                if (parts.length == 2) {
                    listener.onClockUpdate(parts[0], parts[1]);
                }
            } else if (text.startsWith("FINAL_PGN:")) { // NEW
                listener.onReceivePgn(text.substring(10));
            }
            // ... (keep other else/if blocks if you have them)
            else {
                System.out.println("Server says: " + text); // For generic messages
            }
        }
    }
        // We can later handle other object types, e.g.,
        // else if (message instanceof ClockUpdateObject) { ... }
}
