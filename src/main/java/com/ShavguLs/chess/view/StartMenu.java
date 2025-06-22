package com.ShavguLs.chess.view;

import com.ShavguLs.chess.logic.GameValidator;
import com.ShavguLs.chess.logic.PGNFileReader;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

// Make StartMenu a JFrame so it can be a standalone window
public class StartMenu extends JFrame implements ActionListener {

    private static final String START_GAME_COMMAND = "START_GAME";
    private static final String VALIDATE_PGN_COMMAND = "VALIDATE_PGN";

    public StartMenu() {
        super("Chess Game - Main Menu");

        // Frame setup
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(350, 250);
        setLocationRelativeTo(null); // Center the window
        setLayout(new GridLayout(3, 1, 10, 10)); // Changed to 3 rows for the new button

        // Welcome Label
        JLabel welcomeLabel = new JLabel("Welcome to Chess!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Serif", Font.BOLD, 24));
        add(welcomeLabel);

        // Start Button
        JButton startButton = new JButton("Start New Game");
        startButton.setActionCommand(START_GAME_COMMAND);
        startButton.addActionListener(this);
        add(startButton);

        // Validate PGN Button
        JButton validateButton = new JButton("Validate PGN File");
        validateButton.setActionCommand(VALIDATE_PGN_COMMAND);
        validateButton.addActionListener(this);
        add(validateButton);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (START_GAME_COMMAND.equals(command)) {
            startNewGame();
        } else if (VALIDATE_PGN_COMMAND.equals(command)) {
            validatePGNFile();
        }
    }

    private void startNewGame() {
        // --- Create the option dialog ---
        Object[] options = {"Play Locally", "Play Online"};
        int choice = JOptionPane.showOptionDialog(this,
                "How would you like to play?",
                "Choose Game Mode",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == JOptionPane.YES_OPTION) { // Play Locally
            // This is your original logic for starting a local game
            String whitePlayerName = "White";
            String blackPlayerName = "Black";
            int hours = 0, minutes = 10, seconds = 0; // Default 10-minute game

            // Create the LOCAL game window using the original constructor
            SwingUtilities.invokeLater(() -> new GameWindow(
                    blackPlayerName,
                    whitePlayerName,
                    hours,
                    minutes,
                    seconds
            ));
            this.dispose(); // Close the start menu

        } else if (choice == JOptionPane.NO_OPTION) { // Play Online
            // --- Get Server Address from User ---
            String serverAddress = (String) JOptionPane.showInputDialog(
                    this,                       // parent component
                    "Enter Server Address:",    // message
                    "Connect to Server",        // title
                    JOptionPane.QUESTION_MESSAGE, // message type
                    null,                       // icon (null for default)
                    null,                       // selection values (not used for text input)
                    "localhost"                 // initial selection value
            );
            // Default to localhost if the user enters nothing or cancels
            if (serverAddress == null || serverAddress.trim().isEmpty()) {
                serverAddress = "localhost";
            }

            // The default port our server uses.
            // A more advanced version could also ask the user for the port.
            final int port = 8888;
            final String finalServerAddress = serverAddress;

            // Create the ONLINE game window using the new constructor
            SwingUtilities.invokeLater(() -> new GameWindow(finalServerAddress, port));
            this.dispose(); // Close the start menu
        }
        // If the user closes the dialog, 'choice' will be JOptionPane.CLOSED_OPTION, and we do nothing.
    }

    private void validatePGNFile() {
        // Create file chooser for PGN files
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("PGN Files (*.pgn)", "pgn"));
        fileChooser.setDialogTitle("Select PGN File to Validate");

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            validateSelectedFile(selectedFile);
        }
    }

    private void validateSelectedFile(File file) {
        // Show progress dialog since validation might take time
        JDialog progressDialog = createProgressDialog();

        // Use SwingWorker to validate in background thread
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private String resultMessage = "";
            private String resultTitle = "";
            private int messageType = JOptionPane.INFORMATION_MESSAGE;

            @Override
            protected Void doInBackground() throws Exception {
                try {
                    // Read all games from the PGN file
                    List<String> games = PGNFileReader.readGames(file.getAbsolutePath());

                    if (games.isEmpty()) {
                        resultTitle = "Validation Result";
                        resultMessage = "No games found in the PGN file.";
                        messageType = JOptionPane.WARNING_MESSAGE;
                        return null;
                    }

                    // Validate each game
                    StringBuilder results = new StringBuilder();
                    int validGames = 0;
                    int totalGames = games.size();

                    for (int i = 0; i < games.size(); i++) {
                        String game = games.get(i);
                        GameValidator.ValidationResult validation = GameValidator.validateGame(game);

                        if (validation.isValid()) {
                            validGames++;
                        } else {
                            if (results.length() > 0) results.append("\n\n");
                            results.append("Game ").append(i + 1).append(": ");
                            results.append(validation.getErrorMessage());
                        }
                    }

                    // Prepare result message
                    if (validGames == totalGames) {
                        resultTitle = "Validation Successful";
                        resultMessage = String.format("All %d game(s) in the file are valid!\n\n%s",
                                totalGames,
                                totalGames == 1 ? GameValidator.getBoardAnalysis(games.get(0)) : "");
                        messageType = JOptionPane.INFORMATION_MESSAGE;
                    } else {
                        resultTitle = "Validation Failed";
                        resultMessage = String.format("Validation Results:\n" +
                                        "Valid games: %d/%d\n" +
                                        "Invalid games: %d\n\n" +
                                        "Errors found:\n%s",
                                validGames, totalGames, totalGames - validGames, results.toString());
                        messageType = JOptionPane.ERROR_MESSAGE;
                    }

                } catch (Exception e) {
                    resultTitle = "Validation Error";
                    resultMessage = "Error reading or validating PGN file:\n" + e.getMessage();
                    messageType = JOptionPane.ERROR_MESSAGE;
                }
                return null;
            }

            @Override
            protected void done() {
                progressDialog.dispose();

                // Show result in a scrollable dialog for long messages
                if (resultMessage.length() > 500) {
                    showScrollableResult(resultTitle, resultMessage, messageType);
                } else {
                    JOptionPane.showMessageDialog(StartMenu.this, resultMessage, resultTitle, messageType);
                }
            }
        };

        worker.execute();
        progressDialog.setVisible(true);
    }

    private JDialog createProgressDialog() {
        JDialog progressDialog = new JDialog(this, "Validating PGN File...", true);
        JLabel progressLabel = new JLabel("Please wait while validating the PGN file...", SwingConstants.CENTER);
        progressLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add a progress bar
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(progressLabel, BorderLayout.CENTER);
        panel.add(progressBar, BorderLayout.SOUTH);

        progressDialog.add(panel);
        progressDialog.setSize(300, 120);
        progressDialog.setLocationRelativeTo(this);
        progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        return progressDialog;
    }

    private void showScrollableResult(String title, String message, int messageType) {
        JTextArea textArea = new JTextArea(message);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));

        String iconType = switch (messageType) {
            case JOptionPane.ERROR_MESSAGE -> "Error";
            case JOptionPane.WARNING_MESSAGE -> "Warning";
            default -> "Information";
        };

        JOptionPane.showMessageDialog(this, scrollPane, title + " - " + iconType, messageType);
    }
}