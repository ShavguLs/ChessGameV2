package com.ShavguLs.chess.server;

import com.ShavguLs.chess.common.HandshakeObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class ChessServer {
    private static final int PORT = 8888;
    private static final List<Socket> waitingClients = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("Chess Server is starting...");
        DatabaseManager.initializeDatabase();
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleConnection(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * NEW: This method is the entry point for every new client connection.
     * It determines what the client wants to do (play a game, import PGN, etc.).
     */
    private static void handleNewConnection(Socket clientSocket) {
        System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress() + ". Awaiting initial command...");
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            clientSocket.setSoTimeout(5000); // 5-second timeout for the client to send its command
            String command = in.readLine();

            if (command == null) {
                System.out.println("Client connected but sent no data. Closing connection.");
                clientSocket.close();
                return;
            }

            System.out.println("Client sent command: " + command);

            // Check the command to decide what to do
            if (command.startsWith("IMPORT_PGN:")) {
                handlePgnImport(clientSocket, command);
            } else if (command.equals("PLAY_CHESS")) {
                // This is your original logic, now placed here
                handleGamePlayer(clientSocket);
            } else {
                System.out.println("Client sent an unknown command. Closing connection.");
                clientSocket.close();
            }

        } catch (SocketTimeoutException e) {
            System.err.println("Connection timed out waiting for client's initial command.");
        } catch (IOException e) {
            System.err.println("Error on new connection handler: " + e.getMessage());
        } finally {
            // Ensure the socket is closed if it wasn't handled elsewhere
            if (!clientSocket.isClosed()) {
                try { clientSocket.close(); } catch (IOException e) { /* ignore */ }
            }
        }
    }

    /**
     * NEW: Handles the PGN import request.
     */
    private static void handlePgnImport(Socket clientSocket, String message) throws IOException {
        System.out.println("Processing PGN import command.");
        String pgnData = message.substring(11).replace("||NEWLINE||", "\n");

        // Call the DatabaseManager to do the actual work.
        // We will create this method in the next step.
        String response = DatabaseManager.importPgn(pgnData);

        // Send the success/failure message back to the client and close.
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        out.println(response);
        System.out.println("Sent response to PGN import client: " + response);
        clientSocket.close();
    }

    /**
     * Your original matchmaking logic, now in its own method.
     * This method is thread-safe thanks to the synchronized block.
     */
    private static void handleGamePlayer(Socket clientSocket) {
        System.out.println("Client wants to play. Adding to waiting pool.");
        synchronized (waitingClients) {
            waitingClients.add(clientSocket);
            System.out.println("Client added to waiting pool. Total waiting: " + waitingClients.size());

            if (waitingClients.size() >= 2) {
                System.out.println("Two players found! Starting a new game session.");
                Socket player1Socket = waitingClients.remove(0);
                Socket player2Socket = waitingClients.remove(0);

                GameSession gameSession = new GameSession(player1Socket, player2Socket);
                new Thread(gameSession).start();
                System.out.println("GameSession thread started. Server is ready for new connections.");
            }
        }
    }

    private static void handleConnection(Socket clientSocket) {
        System.out.println("New connection from " + clientSocket.getInetAddress().getHostAddress() + ". Awaiting handshake.");
        try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {
            HandshakeObject handshake = (HandshakeObject) in.readObject();
            String command = handshake.command();
            System.out.println("Received command: " + command);

            if ("IMPORT_PGN".equals(command)) {
                String pgnData = handshake.data();
                String response = DatabaseManager.importPgn(pgnData);
                // Send response back and close
                try (ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {
                    out.writeObject(response);
                }
            }
            // Add other utility commands here with "else if" in the future
            else {
                System.out.println("Unknown utility command. Closing connection.");
            }

        } catch (Exception e) {
            System.err.println("Error handling utility connection: " + e.getMessage());
        } finally {
            try {
                if (!clientSocket.isClosed()) clientSocket.close();
            } catch (IOException e) { /* ignore */ }
        }
    }
}