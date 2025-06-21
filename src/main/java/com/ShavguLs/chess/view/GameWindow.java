package com.ShavguLs.chess.view;

import com.ShavguLs.chess.controller.GameController;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;

public class GameWindow extends JFrame {

    private JLabel blackTimeLabel;
    private JLabel whiteTimeLabel;
    private ChessBoardPanel boardPanel;
    private GameController controller;
    private JButton saveGame; // Field to hold the save button

    // ===================================================================
    // --- CONSTRUCTOR 1: FOR LOCAL GAMES (YOUR ORIGINAL CONSTRUCTOR) ---
    // ===================================================================
    public GameWindow(String blackName, String whiteName, int hh, int mm, int ss){
        super("Chess Game");
        // Creates a LOCAL game controller
        this.controller = new GameController(hh, mm, ss, this);
        controller.setPlayerNames(whiteName, blackName);

        // --- Standard UI Setup ---
        try{
            Image blackImg = ImageIO.read(getClass().getResource("/images/bk.png"));
            this.setIconImage(blackImg);
        }catch (Exception ex){
            System.out.println("window icon 'bk.png' not found - using default icon");
        }

        this.setLocation(100, 100);
        this.setLayout(new BorderLayout(20, 20));
        JPanel gameData = gameDataPanel(blackName, whiteName, hh, mm, ss);
        this.add(gameData, BorderLayout.NORTH);
        this.boardPanel = new ChessBoardPanel(this, controller);
        this.add(boardPanel, BorderLayout.CENTER);
        this.add(buttons(), BorderLayout.SOUTH);
        this.setMinimumSize(this.getPreferredSize());
        this.setSize(this.getPreferredSize());
        this.setResizable(false);
        this.pack();
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    // =================================================================
    // --- CONSTRUCTOR 2: FOR ONLINE GAMES (THE NEW CONSTRUCTOR) ---
    // =================================================================
    public GameWindow(String serverAddress, int port) {
        super("Chess - Connecting...");
        // Creates an ONLINE game controller
        this.controller = new GameController(this, serverAddress, port);

        // --- Standard UI Setup ---
        try {
            Image blackImg = ImageIO.read(getClass().getResource("/images/bk.png"));
            this.setIconImage(blackImg);
        } catch (Exception ex) {
            System.out.println("window icon 'bk.png' not found - using default icon");
        }

        this.setLocation(100, 100);
        this.setLayout(new BorderLayout(20, 20));
        // Use placeholder names. The server will provide the real names.
        JPanel gameData = gameDataPanel("Opponent", "You", 0, 0, 0);
        this.add(gameData, BorderLayout.NORTH);
        this.boardPanel = new ChessBoardPanel(this, controller);
        this.add(boardPanel, BorderLayout.CENTER);
        this.add(buttons(), BorderLayout.SOUTH);

        // CRITICAL: Disable the manual "Save Game" button for online play.
        if (this.saveGame != null) {
            this.saveGame.setEnabled(false);
            this.saveGame.setToolTipText("Game is saved automatically at the end of an online match.");
        }

        this.setMinimumSize(this.getPreferredSize());
        this.setSize(this.getPreferredSize());
        this.setResizable(false);
        this.pack();
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }


    // --- UI HELPER METHODS (Unchanged, but provided for completeness) ---

    public ChessBoardPanel getChessBoardPanel() {
        return boardPanel;
    }

    private JPanel gameDataPanel(final String bn, final String wn,
                                 final int hh, final int mm, final int ss){
        JPanel gameData = new JPanel();
        gameData.setLayout(new GridLayout(2,2,5,5));
        JLabel whiteLabel = new JLabel(wn, SwingConstants.CENTER);
        JLabel blackLabel = new JLabel(bn, SwingConstants.CENTER);
        whiteLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        blackLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        gameData.add(whiteLabel);
        gameData.add(blackLabel);
        whiteTimeLabel = new JLabel("Untimed game!", SwingConstants.CENTER);
        blackTimeLabel = new JLabel("Untimed game!", SwingConstants.CENTER);
        Font clockFont = new Font("Monospaced", Font.BOLD, 16);
        whiteTimeLabel.setFont(clockFont);
        blackTimeLabel.setFont(clockFont);
        if (!(hh == 0 && mm == 0 && ss == 0)){
            whiteTimeLabel.setText(String.format("%02d:%02d:%02d", hh, mm, ss));
            blackTimeLabel.setText(String.format("%02d:%02d:%02d", hh, mm, ss));
        }
        JPanel whiteClockPanel = new JPanel(new BorderLayout());
        whiteClockPanel.setBorder(BorderFactory.createTitledBorder("White Clock"));
        whiteClockPanel.add(whiteTimeLabel, BorderLayout.CENTER);
        JPanel blackClockPanel = new JPanel(new BorderLayout());
        blackClockPanel.setBorder(BorderFactory.createTitledBorder("Black Clock"));
        blackClockPanel.add(blackTimeLabel, BorderLayout.CENTER);
        gameData.add(whiteClockPanel);
        gameData.add(blackClockPanel);
        return gameData;
    }

    private JPanel buttons() {
        JPanel buttons = new JPanel(new GridLayout(2, 2, 10, 5));
        final JButton quit = new JButton("Quit");
        quit.addActionListener(e -> {
            int n = JOptionPane.showConfirmDialog(this, "Are you sure you want to quit?", "Confirm quit", JOptionPane.YES_NO_OPTION);
            if (n == JOptionPane.YES_OPTION) {
                controller.stopTimer();
                this.dispose();
            }
        });
        final JButton nGame = new JButton("New game");
        nGame.addActionListener(e -> {
            int n = JOptionPane.showConfirmDialog(this, "Are you sure you want to begin a new game?", "Confirm new game", JOptionPane.YES_NO_OPTION);
            if (n == JOptionPane.YES_OPTION) {
                controller.stopTimer();
                SwingUtilities.invokeLater(() -> new StartMenu().setVisible(true));
                this.dispose();
            }
        });
        final JButton instr = new JButton("How to play");
        instr.addActionListener(e -> JOptionPane.showMessageDialog(this, "How to play instructions...", "How to play", JOptionPane.PLAIN_MESSAGE));

        // Use the class field for the saveGame button
        this.saveGame = new JButton("Save Game");
        this.saveGame.addActionListener(e -> {
            // This button is only enabled for local games, so this call is safe.
            GameSaveDialog saveDialog = new GameSaveDialog(this, controller.getPGNManager(), "White", "Black");
            saveDialog.setVisible(true);
        });
        buttons.add(instr);
        buttons.add(this.saveGame); // Add the field to the panel
        buttons.add(nGame);
        buttons.add(quit);
        return buttons;
    }

    // --- METHODS CALLED BY CONTROLLER ---

    public void updateWhiteClock(String time) {
        if (whiteTimeLabel != null) {
            SwingUtilities.invokeLater(() -> whiteTimeLabel.setText(time));
        }
    }

    public void updateBlackClock(String time) {
        if (blackTimeLabel != null) {
            SwingUtilities.invokeLater(() -> blackTimeLabel.setText(time));
        }
    }

    public void timeOut(boolean whiteTimedOut) {
        controller.stopTimer();
        String winner = whiteTimedOut ? "Black" : "White";
        int n = JOptionPane.showConfirmDialog(this, winner + " wins on time! Play a new game?", winner + " Wins!", JOptionPane.YES_NO_OPTION);
        if (n == JOptionPane.YES_OPTION){
            SwingUtilities.invokeLater(() -> new StartMenu().setVisible(true));
            this.dispose();
        }
    }

    // NEW method to be called for online game saves
    public void showOnlineGameSaveDialog(String pgnText) {
        GameSaveDialog saveDialog = new GameSaveDialog(this, pgnText);
        saveDialog.setVisible(true);
    }

    public void checkmateOccurred(boolean whiteLost) {
        String winner = whiteLost ? "Black" : "White";
        String message = "Checkmate! " + winner + " wins.";
        JOptionPane.showMessageDialog(this, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }

    public void stalemateOccurred() {
        String message = "Stalemate! The game is a draw.";
        JOptionPane.showMessageDialog(this, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);
    }
}