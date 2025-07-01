package com.ShavguLs.chess.client.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerUtilityClient {

    /**
     * Sends a single command to the server and returns the server's response.
     * @param serverAddress The IP address or hostname of the server.
     * @param port The port the server is listening on.
     * @param command The command string to send to the server.
     * @return The server's single-line response.
     */
    public static String sendCommand(String serverAddress, int port, String command) {
        // Using try-with-resources to ensure everything is closed automatically
        try (Socket socket = new Socket(serverAddress, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send our command to the server
            out.println(command);

            // Wait for and return the server's response
            String response = in.readLine();
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR: Could not connect to server or send command. " + e.getMessage();
        }
    }
}