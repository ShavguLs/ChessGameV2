package com.ShavguLs.chess;

import com.ShavguLs.chess.client.view.StartMenu;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Could not set the system look and feel.");
                e.printStackTrace();
            }

            StartMenu startMenu = new StartMenu();

            startMenu.setVisible(true);
        });
    }
}