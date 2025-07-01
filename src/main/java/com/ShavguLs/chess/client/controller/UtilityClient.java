package com.ShavguLs.chess.client.controller;

import com.ShavguLs.chess.common.HandshakeObject;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class UtilityClient {
    /**
     * Sends a HandshakeObject to the server and returns the server's String response.
     * @param serverAddress The server's IP or hostname.
     * @param port The server's port.
     * @param handshake The HandshakeObject containing the command and data.
     * @return The String response from the server.
     */
    public static String sendCommand(String serverAddress, int port, HandshakeObject handshake) {
        try (Socket socket = new Socket(serverAddress, port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            out.flush(); // Send stream header first.
            out.writeObject(handshake); // Send the entire object.
            out.flush();

            // Now, wait for and read the response.
            try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                return (String) in.readObject();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR: Could not communicate with server. " + e.getMessage();
        }
    }
}