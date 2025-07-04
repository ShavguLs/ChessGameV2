package com.ShavguLs.chess.client.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameListDialog extends JDialog {
    private JTable gamesTable;
    private DefaultTableModel tableModel;
    private JButton spectateButton;
    private String serverAddress;
    private int serverPort;
    private int selectedGameId = -1;

    public GameListDialog(JFrame parent, String serverAddress, int serverPort) {
        super(parent, "Active Games", true);
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;

        setupUI();
        refreshGameList();
    }

    private void setupUI() {
        setLayout(new BorderLayout());
        setSize(600, 350);
        setLocationRelativeTo(getParent());

        String[] columnNames = {"Game ID", "White Player", "Black Player", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        gamesTable = new JTable(tableModel);
        gamesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gamesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = gamesTable.getSelectedRow();
                if (selectedRow >= 0) {
                    selectedGameId = (int) tableModel.getValueAt(selectedRow, 0);
                    spectateButton.setEnabled(true);
                } else {
                    selectedGameId = -1;
                    spectateButton.setEnabled(false);
                }
            }
        });

        add(new JScrollPane(gamesTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        spectateButton = new JButton("Spectate Game");
        spectateButton.setEnabled(false);
        spectateButton.addActionListener(e -> spectateSelectedGame());

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshGameList());

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(spectateButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    private void refreshGameList() {
        try {
            List<GameInfo> games = fetchActiveGames();
            updateTable(games);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to fetch games: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<GameInfo> fetchActiveGames() throws IOException {
        List<GameInfo> games = new ArrayList<>();
        try (Socket socket = new Socket(serverAddress, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("LIST_GAMES");
            String response = in.readLine();

            if (response != null && response.startsWith("GAMES:")) {
                String gamesData = response.substring(6);
                if (!gamesData.isEmpty()) {
                    String[] gameStrings = gamesData.split("\\|");
                    for (String gameString : gameStrings) {
                        String[] parts = gameString.split(",");
                        if (parts.length >= 4) {
                            games.add(new GameInfo(Integer.parseInt(parts[0]), parts[1], parts[2], parts[3]));
                        }
                    }
                }
            }
        }
        return games;
    }

    private void updateTable(List<GameInfo> games) {
        tableModel.setRowCount(0);
        if (games.isEmpty()) {
            tableModel.addRow(new Object[]{"No games", "available", "at the", "moment"});
            spectateButton.setEnabled(false);
        } else {
            for (GameInfo game : games) {
                tableModel.addRow(new Object[]{game.gameId, game.whitePlayer, game.blackPlayer, game.status});
            }
        }
    }

    private void spectateSelectedGame() {
        if (selectedGameId >= 0) {
            dispose();
            SwingUtilities.invokeLater(() -> {
                SpectatorMode spectatorMode = new SpectatorMode(selectedGameId, serverAddress, serverPort);
                spectatorMode.setVisible(true);
            });
        }
    }

    private static class GameInfo {
        int gameId;
        String whitePlayer, blackPlayer, status;
        GameInfo(int gameId, String whitePlayer, String blackPlayer, String status) {
            this.gameId = gameId;
            this.whitePlayer = whitePlayer;
            this.blackPlayer = blackPlayer;
            this.status = status;
        }
    }
}