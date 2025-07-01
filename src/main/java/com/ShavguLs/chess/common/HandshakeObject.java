package com.ShavguLs.chess.common;

import java.io.Serializable;

/**
 * A structured object for initial client-server utility commands.
 * This allows sending a command and its data (like a PGN string) together.
 */
public record HandshakeObject(String command, String data) implements Serializable {
    private static final long serialVersionUID = 1L;

    // A convenience constructor for commands that don't need data.
    public HandshakeObject(String command) {
        this(command, null);
    }
}