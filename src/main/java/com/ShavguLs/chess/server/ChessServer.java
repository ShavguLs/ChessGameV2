package com.ShavguLs.chess.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ChessServer {
    private static final int PORT = 8888;
    private static final List<Socket> waitingClients = new ArrayList<>();

    private static final Map<Integer, GameSession> activeGames = new ConcurrentHashMap<>();
    private static final AtomicInteger gameIdCounter = new AtomicInteger(1);

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

                // Generate game ID first, then create session, then register !!!
                int gameId = gameIdCounter.getAndIncrement();
                GameSession gameSession = new GameSession(player1Socket, player2Socket, gameId);
                activeGames.put(gameId, gameSession); // Now register with actual session

                new Thread(gameSession).start();
                System.out.println("GameSession #" + gameId + " thread started. Server is ready for new connections.");
            }
        }
    }

    private static void handleConnection(Socket clientSocket) {
        System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress() + ". Awaiting initial command...");
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
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
                handleGamePlayer(clientSocket);
            } else if (command.startsWith("SPECTATE_GAME:")) {
                handleSpectator(clientSocket, command);
            } else if (command.equals("LIST_GAMES")) {
                handleGameListRequest(clientSocket);
            } else {
                System.out.println("Client sent an unknown command. Closing connection.");
                clientSocket.close();
            }

        } catch (SocketTimeoutException e) {
            System.err.println("Connection timed out waiting for client's initial command.");
            try { clientSocket.close(); } catch (IOException ex) { }
        } catch (IOException e) {
            System.err.println("Error on new connection handler: " + e.getMessage());
            try { clientSocket.close(); } catch (IOException ex) { }
        }
    }

     // Handles spectator connections
    private static void handleSpectator(Socket clientSocket, String command) {
        try {
            // Extract game id from command
            String gameIdStr = command.substring(14);
            int gameId = Integer.parseInt(gameIdStr.trim());

            System.out.println("Client wants to spectate game #" + gameId);

            // Find the game session
            GameSession gameSession = activeGames.get(gameId);

            if (gameSession != null && gameSession.isActive()) {
                // Add spectator to the game session
                gameSession.addSpectator(clientSocket);
                System.out.println("Spectator added to game #" + gameId);
            } else {
                // Game not found or ended
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                out.println("GAME_NOT_FOUND");
                System.out.println("Game #" + gameId + " not found for spectator");
                clientSocket.close();
            }

        } catch (Exception e) {
            System.err.println("Error handling spectator: " + e.getMessage());
            try { clientSocket.close(); } catch (IOException ex) { }
        }
    }

     // Handles requests for the list of active games
    private static void handleGameListRequest(Socket clientSocket) {
        try {
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            StringBuilder gamesList = new StringBuilder("GAMES:");
            boolean first = true;

            // Build list of active games
            for (Map.Entry<Integer, GameSession> entry : activeGames.entrySet()) {
                GameSession session = entry.getValue();
                if (session.isActive()) {
                    if (!first) {
                        gamesList.append("|");
                    }
                    first = false;

                    // Format: gameId,white,black,status,time
                    gamesList.append(entry.getKey()).append(",");
                    gamesList.append(session.getWhitePlayerName()).append(",");
                    gamesList.append(session.getBlackPlayerName()).append(",");
                    gamesList.append(session.getCurrentTurn()).append(" to move,");
                    gamesList.append(session.getTimeStatus());
                }
            }

            out.println(gamesList.toString());
            System.out.println("Sent games list to client: " + gamesList.toString());
            clientSocket.close();

        } catch (IOException e) {
            System.err.println("Error sending games list: " + e.getMessage());
            try { clientSocket.close(); } catch (IOException ex) { }
        }
    }

    public static void removeGame(int gameId) {
        activeGames.remove(gameId);
    }
}