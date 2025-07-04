package com.ShavguLs.chess.client.view;

import com.ShavguLs.chess.client.controller.GameController;
import com.ShavguLs.chess.common.logic.Piece;

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
        private int startRow, startCol; // Store the starting square of a drag

        @Override
        public void mousePressed(MouseEvent e) {
            startCol = e.getX() / SQUARE_SIZE;
            startRow = e.getY() / SQUARE_SIZE;

            if (startRow < 0 || startRow >= 8 || startCol < 0 || startCol >= 8) {
                return;
            }

            // This is now purely for starting a drag-and-drop visual
            Piece clickedPiece = controller.getLogicBoard().getPieceAt(startRow, startCol);
            if (clickedPiece != null) {
                // Check if it's the player's turn to move this piece
                // Note: This check is a bit simplified, the controller is the real authority
                if (controller.isWhiteTurn() == clickedPiece.isWhite()) {
                    pieceBeingDragged = clickedPiece;
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            int destCol = e.getX() / SQUARE_SIZE;
            int destRow = e.getY() / SQUARE_SIZE;

            // Clear the visual drag piece regardless of what happens
            pieceBeingDragged = null;
            repaint();

            if (destRow < 0 || destRow >= 8 || destCol < 0 || destCol >= 8) {
                return;
            }

            // Check if this was a DRAG move or a CLICK move
            if (startRow == destRow && startCol == destCol) {
                // --- This was a CLICK ---
                // Let the controller handle the selection/deselection/move logic
                System.out.println("DEBUG: Forwarding CLICK to controller for square (" + destRow + "," + destCol + ")");
                controller.selectPiece(destRow, destCol);

            } else {
                // --- This was a DRAG ---
                // We have a start and an end square, so we can attempt a move directly.
                // First, select the piece at the start...
                controller.selectPiece(startRow, startCol);
                // ...then immediately try to move it to the destination.
                System.out.println("DEBUG: Forwarding DRAG to controller from ("+startRow+","+startCol+") to (" + destRow + "," + destCol + ")");
                controller.makeMove(destRow, destCol);
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