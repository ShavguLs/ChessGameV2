package com.ShavguLs.chess.view;

import com.ShavguLs.chess.logic.PGNManager;
import com.ShavguLs.chess.logic.GameValidator;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GameSaveDialog extends JDialog {
    // --- FIELDS ---
    private PGNManager pgnManager; // Used for local games
    private String rawPgnText;     // Used for online games

    private JTextField eventNameField;
    private JTextField locationField;
    private JTextField roundField;
    private JTextArea gamePreview;

    // --- CONSTRUCTORS ---

    /**
     * Constructor for LOCAL games, which uses a live PGNManager.
     */
    public GameSaveDialog(JFrame parentWindow, PGNManager manager, String whiteName, String blackName) {
        super(parentWindow, "Save Game", true);
        this.pgnManager = manager;
        this.rawPgnText = null;

        setupComponents();
        arrangeComponents();
        pack();
        setLocationRelativeTo(parentWindow);
    }

    /**
     * NEW: Constructor for ONLINE games, which receives a finalized PGN string.
     */
    public GameSaveDialog(JFrame parentWindow, String pgnText) {
        super(parentWindow, "Save Game", true);
        this.rawPgnText = pgnText;
        this.pgnManager = null; // We don't have a live manager in this mode

        setupComponentsForPgnText();
        arrangeComponents();
        pack();
        setLocationRelativeTo(parentWindow);
    }


    // --- UI SETUP ---

    /**
     * Setup method for LOCAL games.
     */
    private void setupComponents() {
        eventNameField = new JTextField("Chess Game", 20);
        locationField = new JTextField("Home", 20);
        roundField = new JTextField("1", 5);

        gamePreview = new JTextArea(15, 50);
        gamePreview.setEditable(false);
        gamePreview.setFont(new Font("Courier", Font.PLAIN, 12));

        updateGamePreview(); // Update preview on initial load
    }

    /**
     * NEW: Setup method for ONLINE games.
     */
    private void setupComponentsForPgnText() {
        // We can't edit event/location/round, so we create disabled fields.
        eventNameField = new JTextField("Online Game", 20);
        eventNameField.setEnabled(false);
        locationField = new JTextField("Server", 20);
        locationField.setEnabled(false);
        roundField = new JTextField("N/A", 5);
        roundField.setEnabled(false);

        gamePreview = new JTextArea(15, 50);
        gamePreview.setEditable(false);
        gamePreview.setFont(new Font("Courier", Font.PLAIN, 12));
        gamePreview.setText(this.rawPgnText); // Directly set the text
        gamePreview.setCaretPosition(0);
    }

    private void arrangeComponents() {
        setLayout(new BorderLayout());
        JPanel inputPanel = createInputPanel();
        add(inputPanel, BorderLayout.NORTH);
        JPanel previewPanel = createPreviewPanel();
        add(previewPanel, BorderLayout.CENTER);
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        inputPanel.add(new JLabel("Event Name:"), constraints);
        constraints.gridx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(eventNameField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.NONE;
        inputPanel.add(new JLabel("Location:"), constraints);
        constraints.gridx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(locationField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.fill = GridBagConstraints.NONE;
        inputPanel.add(new JLabel("Round:"), constraints);
        constraints.gridx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(roundField, constraints);

        // Only add the update listener if we are in local mode
        if (pgnManager != null) {
            InputUpdateListener updateListener = new InputUpdateListener();
            eventNameField.getDocument().addDocumentListener(updateListener);
            locationField.getDocument().addDocumentListener(updateListener);
            roundField.getDocument().addDocumentListener(updateListener);
        }

        return inputPanel;
    }

    private JPanel createPreviewPanel() {
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBorder(BorderFactory.createTitledBorder("Game Preview"));
        previewPanel.add(new JScrollPane(gamePreview), BorderLayout.CENTER);
        return previewPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton validateButton = new JButton("Validate");
        validateButton.addActionListener(new ValidateListener());
        JButton saveFileButton = new JButton("Save to File");
        saveFileButton.addActionListener(new SaveFileListener());
        JButton copyTextButton = new JButton("Copy Text");
        copyTextButton.addActionListener(new CopyTextListener());
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new CancelListener());

        buttonPanel.add(validateButton);
        buttonPanel.add(saveFileButton);
        buttonPanel.add(copyTextButton);
        buttonPanel.add(cancelButton);

        return buttonPanel;
    }

    private void updateGamePreview() {
        // This method is only relevant for local games with a PGNManager
        if (pgnManager == null) return;

        SwingUtilities.invokeLater(() -> {
            pgnManager.setGameInfo(eventNameField.getText(),
                    locationField.getText(),
                    roundField.getText());
            String gameText = pgnManager.getPGNText();
            gamePreview.setText(gameText);
            gamePreview.setCaretPosition(0);
        });
    }

    // --- ACTION LISTENERS AND HELPERS ---

    private void saveGameToFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("PGN Files", "pgn"));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
        String timeStamp = dateFormat.format(new Date());
        String defaultFileName = "chess_game_" + timeStamp + ".pgn";
        fileChooser.setSelectedFile(new File(defaultFileName));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String fileName = selectedFile.getAbsolutePath();
            if (!fileName.toLowerCase().endsWith(".pgn")) {
                fileName += ".pgn";
            }

            boolean saveSuccess;
            if (rawPgnText != null) {
                // Online mode: We just write the raw text to a file.
                try (java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(fileName))) {
                    writer.write(rawPgnText);
                    saveSuccess = true;
                } catch (java.io.IOException ex) {
                    ex.printStackTrace();
                    saveSuccess = false;
                }
            } else {
                // Local mode: Use the existing PGNManager.
                pgnManager.setGameInfo(eventNameField.getText(), locationField.getText(), roundField.getText());
                saveSuccess = pgnManager.saveToFile(fileName);
            }

            if (saveSuccess) {
                JOptionPane.showMessageDialog(this,
                        "Game saved successfully!\nFile: " + fileName,
                        "Save Successful",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Could not save the file. Please check the location and try again.",
                        "Save Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void copyGameText() {
        String gameText = (rawPgnText != null) ? rawPgnText : pgnManager.getPGNText();
        java.awt.datatransfer.StringSelection selection =
                new java.awt.datatransfer.StringSelection(gameText);
        java.awt.datatransfer.Clipboard clipboard =
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, null);
        JOptionPane.showMessageDialog(this,
                "Game text copied to clipboard!",
                "Copy Successful",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private class InputUpdateListener implements javax.swing.event.DocumentListener {
        public void changedUpdate(javax.swing.event.DocumentEvent e) { updateGamePreview(); }
        public void removeUpdate(javax.swing.event.DocumentEvent e) { updateGamePreview(); }
        public void insertUpdate(javax.swing.event.DocumentEvent e) { updateGamePreview(); }
    }

    private class ValidateListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            final String pgnTextToValidate = (rawPgnText != null) ? rawPgnText : pgnManager.getPGNText();

            JDialog progressDialog = new JDialog(GameSaveDialog.this, "Validating...", true);
            progressDialog.add(new JLabel("Validating game moves...", SwingConstants.CENTER));
            progressDialog.setSize(250, 100);
            progressDialog.setLocationRelativeTo(GameSaveDialog.this);

            SwingWorker<GameValidator.ValidationResult, Void> worker = new SwingWorker<>() {
                @Override
                protected GameValidator.ValidationResult doInBackground() {
                    return GameValidator.validateGame(pgnTextToValidate);
                }

                @Override
                protected void done() {
                    progressDialog.dispose();
                    try {
                        GameValidator.ValidationResult result = get();
                        String analysis = GameValidator.getBoardAnalysis(pgnTextToValidate);
                        String message = result.getErrorMessage() + "\n\n" + analysis;

                        if (result.isValid()) {
                            JOptionPane.showMessageDialog(GameSaveDialog.this, message, "Validation Successful", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(GameSaveDialog.this, result.getErrorMessage(), "Validation Failed", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(GameSaveDialog.this, "Error during validation: " + ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
            progressDialog.setVisible(true);
        }
    }

    private class SaveFileListener implements ActionListener {
        public void actionPerformed(ActionEvent e) { saveGameToFile(); }
    }

    private class CopyTextListener implements ActionListener {
        public void actionPerformed(ActionEvent e) { copyGameText(); }
    }

    private class CancelListener implements ActionListener {
        public void actionPerformed(ActionEvent e) { dispose(); }
    }
}