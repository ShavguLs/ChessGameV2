package com.ShavguLs.chess.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChessServer {

    // Define a port for the server to listen on.
    // This must be a number between 1024 and 65535.
    // Clients will need to know this port number to connect.
    private static final int PORT = 8888;

    // A list to hold clients who are waiting for an opponent.
    private static final List<Socket> waitingClients = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("Chess Server is starting...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);

            while (true) {
                // This line blocks and waits for a new client to connect.
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());

                // We need to handle the connected client.
                // Let's add them to a waiting pool.
                synchronized (waitingClients) {
                    waitingClients.add(clientSocket);
                    System.out.println("Client added to the waiting pool. Total waiting: " + waitingClients.size());

                    // If we now have two clients waiting, we can start a game!
                    if (waitingClients.size() >= 2) {
                        System.out.println("Two players found! Starting a new game session.");

                        // Pull the two players from the waiting list.
                        Socket player1Socket = waitingClients.remove(0);
                        Socket player2Socket = waitingClients.remove(0);

                        // Here is where we will create and start the actual game logic.
                        // We will create a "GameSession" class for this in the next step.

                        GameSession gameSession = new GameSession(player1Socket, player2Socket);

                        // Run the game session in a new thread so the server can
                        // immediately go back to waiting for more connections.
                        Thread gameThread = new Thread(gameSession);
                        gameThread.start();

                        System.out.println("GameSession thread started. Server is ready for new connections.");
                        // TODO: Create a new GameSession(player1Socket, player2Socket) and start it in a new thread.
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}