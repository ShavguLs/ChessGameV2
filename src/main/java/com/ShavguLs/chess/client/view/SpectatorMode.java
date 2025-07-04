package com.ShavguLs.chess.client.view;

import com.ShavguLs.chess.client.controller.SpectatorController;
import com.ShavguLs.chess.common.logic.Piece;
import javax.swing.*;
import java.awt.*;

public class SpectatorMode extends JFrame {
    private static final int SQUARE_SIZE = 50;
    private JLabel statusLabel;
    private SpectatorBoardPanel boardPanel;
    private SpectatorController controller;

    public SpectatorMode(int gameId, String serverAddress, int port) {
        super("Spectator - Game #" + gameId);
        this.controller = new SpectatorController(this, serverAddress, port, gameId);
        setupUI();
        if (!controller.connect()) {
            JOptionPane.showMessageDialog(this, "Failed to connect to game", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    private void setupUI() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        statusLabel = new JLabel("Connecting...", SwingConstants.CENTER);
        add(statusLabel, BorderLayout.NORTH);

        boardPanel = new SpectatorBoardPanel(controller);
        add(boardPanel, BorderLayout.CENTER);

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> {
            controller.disconnect();
            dispose();
        });
        add(exitButton, BorderLayout.SOUTH);

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                controller.disconnect();
                dispose();
            }
        });

        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    public void updatePlayerNames(String whiteName, String blackName) {
        SwingUtilities.invokeLater(() -> setTitle("Spectator - " + whiteName + " vs " + blackName));
    }

    public void updateStatus(String status) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(status));
    }

    public void refreshBoard() {
        if (boardPanel != null) boardPanel.repaint();
    }

    public void showGameOver(String result) {
        SwingUtilities.invokeLater(() -> {
            updateStatus("Game Over: " + result);
            JOptionPane.showMessageDialog(this, "Game ended: " + result, "Game Over", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    public void onConnected() {
        SwingUtilities.invokeLater(() -> updateStatus("Connected - watching game..."));
    }

    public void onConnectionFailed(String error) {
        SwingUtilities.invokeLater(() -> updateStatus("Connection failed: " + error));
    }

    // TODO
    public void updateClocks(String whiteTime, String blackTime) {}
    public SpectatorBoardPanel getBoardPanel() { return boardPanel; }
}

class SpectatorBoardPanel extends JPanel {
    private static final int SQUARE_SIZE = 50;
    private final SpectatorController controller;

    public SpectatorBoardPanel(SpectatorController controller) {
        this.controller = controller;
        this.setPreferredSize(new Dimension(SQUARE_SIZE * 8, SQUARE_SIZE * 8));
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int xPos = col * SQUARE_SIZE;
                int yPos = row * SQUARE_SIZE;

                g.setColor((row + col) % 2 == 0 ? new Color(221, 192, 127) : new Color(101, 67, 33));
                g.fillRect(xPos, yPos, SQUARE_SIZE, SQUARE_SIZE);

                Piece piece = controller.getLogicBoard().getPieceAt(row, col);
                if (piece != null) {
                    Image img = ImageManager.getInstance().getPieceImage(piece);
                    if (img != null) {
                        g.drawImage(img, xPos, yPos, SQUARE_SIZE, SQUARE_SIZE, null);
                    }
                }
            }
        }
    }
}