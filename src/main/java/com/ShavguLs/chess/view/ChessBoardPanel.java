package com.ShavguLs.chess.view;

import com.ShavguLs.chess.controller.GameController;
import com.ShavguLs.chess.logic.Piece; // <-- Import YOUR Piece class

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ChessBoardPanel extends JPanel {
    private static final int SQUARE_SIZE = 50;

    private final GameController controller;
    private final GameWindow gameWindow;

    // State for visually dragging a piece
    private Piece pieceBeingDragged;
    private int dragX, dragY;

    public ChessBoardPanel(GameWindow gameWindow, GameController controller) {
        this.controller = controller;
        this.gameWindow = gameWindow;
        this.setPreferredSize(new Dimension(SQUARE_SIZE * 8, SQUARE_SIZE * 8));

        MouseHandler mouseHandler = new MouseHandler();
        this.addMouseListener(mouseHandler);
        this.addMouseMotionListener(mouseHandler);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw the checkerboard pattern
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int xPos = col * SQUARE_SIZE;
                int yPos = row * SQUARE_SIZE;

                if ((row + col) % 2 == 0) {
                    g.setColor(new Color(221, 192, 127)); // Light square
                } else {
                    g.setColor(new Color(101, 67, 33)); // Dark square
                }
                g.fillRect(xPos, yPos, SQUARE_SIZE, SQUARE_SIZE);

                // Draw the piece from the logic board
                Piece piece = controller.getLogicBoard().getPieceAt(row, col);
                if (piece != null) {
                    // Don't draw the piece if it's the one we are currently dragging
                    if (piece != pieceBeingDragged) {
                        drawPiece(g, piece, xPos, yPos);
                    }
                }
            }
        }

        // If a piece is being dragged, draw it at the mouse cursor's position
        if (pieceBeingDragged != null) {
            drawPiece(g, pieceBeingDragged, dragX - SQUARE_SIZE / 2, dragY - SQUARE_SIZE / 2);
        }
    }

    private void drawPiece(Graphics g, Piece piece, int x, int y) {
        Image img = ImageManager.getInstance().getPieceImage(piece);
        if (img != null) {
            g.drawImage(img, x, y, SQUARE_SIZE, SQUARE_SIZE, null);
        }
    }

    private class MouseHandler extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            int col = e.getX() / SQUARE_SIZE;
            int row = e.getY() / SQUARE_SIZE;

            // DEBUG: Add bounds checking and logging
            System.out.println("DEBUG: Mouse pressed at pixel (" + e.getX() + "," + e.getY() + ")");
            System.out.println("DEBUG: Calculated square (" + row + "," + col + ")");

            if (row < 0 || row >= 8 || col < 0 || col >= 8) {
                System.out.println("DEBUG: Click outside board bounds");
                return;
            }

            // DEBUG: Show what piece is at this location
            Piece clickedPiece = controller.getLogicBoard().getPieceAt(row, col);
            if (clickedPiece != null) {
                System.out.println("DEBUG: Clicked on " + clickedPiece.getClass().getSimpleName() +
                        " (white: " + clickedPiece.isWhite() + ") at (" + row + "," + col + ")");
            }

            if (controller.selectPiece(row, col)) {
                pieceBeingDragged = controller.getLogicBoard().getPieceAt(row, col);
                dragX = e.getX();
                dragY = e.getY();
                repaint();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (pieceBeingDragged != null) {
                int col = e.getX() / SQUARE_SIZE;
                int row = e.getY() / SQUARE_SIZE;

                // DEBUG: Add bounds checking and logging
                System.out.println("DEBUG: Mouse released at pixel (" + e.getX() + "," + e.getY() + ")");
                System.out.println("DEBUG: Calculated destination square (" + row + "," + col + ")");

                // FIXED: Ensure we're within bounds before attempting move
                if (row >= 0 && row < 8 && col >= 0 && col < 8) {
                    System.out.println("DEBUG: Attempting move to (" + row + "," + col + ")");

                    // Check what piece (if any) is at the destination
                    Piece destPiece = controller.getLogicBoard().getPieceAt(row, col);
                    if (destPiece != null) {
                        System.out.println("DEBUG: Destination has " + destPiece.getClass().getSimpleName() +
                                " (white: " + destPiece.isWhite() + ")");
                    } else {
                        System.out.println("DEBUG: Destination is empty");
                    }

                    controller.makeMove(row, col);
                } else {
                    System.out.println("DEBUG: Release outside board bounds - move cancelled");
                }

                pieceBeingDragged = null;
                repaint();
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (pieceBeingDragged != null) {
                dragX = e.getX();
                dragY = e.getY();
                repaint();
            }
        }
    }
}