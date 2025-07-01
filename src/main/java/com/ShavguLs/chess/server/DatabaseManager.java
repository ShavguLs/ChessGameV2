package com.ShavguLs.chess.server; // Or your primary package

import com.ShavguLs.chess.common.logic.PGNManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

public class DatabaseManager {

    // --- Final, agreed-upon credentials ---
    private static final String DB_HOST = "localhost";
    private static final String DB_NAME = "chess_db";
    private static final String DB_USER = "chess_app_user";
    private static final String DB_PASS = "ChessV2";
    // ------------------------------------

    // The JDBC connection URL for MariaDB
    private static final String DATABASE_URL = "jdbc:mariadb://" + DB_HOST + "/" + DB_NAME;

    /**
     * Establishes a connection to the MariaDB database.
     * @return a Connection object, or null if connection fails.
     */
    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DATABASE_URL, DB_USER, DB_PASS);
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
        return conn;
    }

    /**
     * Initializes the database. Creates the 'games' table if it does not already exist.
     * This should be called once when the server starts.
     */
    public static void initializeDatabase() {
        // SQL for creating the games table.
        // Your partner's code will create the 'users' table.
        String createGamesTableSql = "CREATE TABLE IF NOT EXISTS games ("
                + " id INT AUTO_INCREMENT PRIMARY KEY,"
                + " white_player VARCHAR(255) NOT NULL,"
                + " black_player VARCHAR(255) NOT NULL,"
                + " result VARCHAR(10) NOT NULL,"
                + " game_date DATETIME NOT NULL,"
                + " pgn_text TEXT NOT NULL"
                // Optional: We can later add foreign keys to the users table
                // + ", white_user_id INT, black_user_id INT"
                // + ", FOREIGN KEY (white_user_id) REFERENCES users(id)"
                // + ", FOREIGN KEY (black_user_id) REFERENCES users(id)"
                + ");";

        // Using try-with-resources to ensure connection is closed
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            if (conn == null) {
                System.err.println("Cannot initialize database: connection is null. Check credentials and if MariaDB is running.");
                return;
            }

            stmt.execute(createGamesTableSql);
            System.out.println("Database initialized. 'games' table is ready.");

        } catch (SQLException e) {
            System.err.println("Error initializing database table: " + e.getMessage());
        }
    }

    /**
     * Saves a completed game to the database.
     * @param pgnManager The PGNManager containing the final game data.
     * @return true if the game was saved successfully, false otherwise.
     */
    public static boolean saveGame(PGNManager pgnManager) {
        // Get the required data from the PGNManager
        String pgnText = pgnManager.getPGNText();
        String whitePlayer = pgnManager.getWhitePlayerName();
        String blackPlayer = pgnManager.getBlackPlayerName();
        String result = pgnManager.getResult();

        // The SQL INSERT statement with placeholders (?) for security
        String sql = "INSERT INTO games(white_player, black_player, result, game_date, pgn_text) VALUES(?,?,?,?,?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn == null) {
                System.err.println("Cannot save game: database connection is null.");
                return false;
            }

            // Set the values for the placeholders
            pstmt.setString(1, whitePlayer);
            pstmt.setString(2, blackPlayer);
            pstmt.setString(3, result);
            pstmt.setTimestamp(4, new Timestamp(System.currentTimeMillis())); // Current date and time
            pstmt.setString(5, pgnText);

            // Execute the insert statement
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Game successfully saved to the database.");
                return true;
            } else {
                System.err.println("Game was not saved to the database, no rows affected.");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Error saving game to database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Imports a game from a full PGN text string into the database.
     * @param pgnText The full PGN content.
     * @return A success or failure message string to be sent back to the client.
     */
    public static String importPgn(String pgnText) {
        String whitePlayer = "Unknown";
        String blackPlayer = "Unknown";
        String result = "*";

        // A simple but effective parser to extract the required PGN headers
        try {
            String[] lines = pgnText.split("\n");
            for (String line : lines) {
                if (line.trim().startsWith("[White ")) {
                    whitePlayer = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
                } else if (line.trim().startsWith("[Black ")) {
                    blackPlayer = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
                } else if (line.trim().startsWith("[Result ")) {
                    result = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
                }
            }
        } catch (Exception e) {
            System.err.println("Could not parse PGN headers: " + e.getMessage());
            // We can still try to save it with default values
        }


        String sql = "INSERT INTO games(white_player, black_player, result, game_date, pgn_text) VALUES(?,?,?,?,?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn == null) {
                return "ERROR: Database connection failed.";
            }

            pstmt.setString(1, whitePlayer);
            pstmt.setString(2, blackPlayer);
            pstmt.setString(3, result);
            pstmt.setTimestamp(4, new Timestamp(System.currentTimeMillis())); // Use current time for import date
            pstmt.setString(5, pgnText);
            pstmt.executeUpdate();

            System.out.println("Successfully imported PGN for " + whitePlayer + " vs. " + blackPlayer);
            return "SUCCESS: Game imported to database.";

        } catch (SQLException e) {
            e.printStackTrace();
            return "ERROR: An SQL error occurred during import. " + e.getMessage();
        }
    }
}