package com.ShavguLs.chess.common.logic;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

// This class keeps track of all the moves made in the game
public class MoveTracker {

    private ArrayList<String> movesInText;
    private List<String> positionHistory;
    private int movesSinceLastCapture;

    public MoveTracker() {
        this.movesInText = new ArrayList<String>();
        this.positionHistory = new LinkedList<String>();
        this.movesSinceLastCapture = 0;
    }

    public void addMove(String moveNotation) {
        movesInText.add(moveNotation);
    }

    public void addPosition(String position) {
        positionHistory.add(position);
    }

    public void updateMoveCounter(boolean wasCapture, boolean wasPawnMove) {
        if (wasCapture || wasPawnMove) {
            movesSinceLastCapture = 0;
        } else {
            movesSinceLastCapture++;
        }
    }

    public ArrayList<String> getAllMoves() {
        return new ArrayList<String>(movesInText);
    }

    public int getMovesSinceCapture() {
        return movesSinceLastCapture;
    }

    public boolean hasThreefoldRepetition() {
        // A threefold repetition is not possible if there aren't enough moves
        // for a position to even be repeated once. A simple check is enough.
        if (positionHistory.isEmpty()) {
            return false;
        }

        // Get the most recent position that was just added.
        String currentPosition = positionHistory.get(positionHistory.size() - 1);

        // Count how many times this exact position has occurred in the history.
        int count = 0;
        for (String position : positionHistory) {
            if (position.equals(currentPosition)) {
                count++;
            }
        }

        // The rule is met if the count is 3 or more.
        return count >= 3;
    }

    public boolean hasFiftyMoveRule() {
        return movesSinceLastCapture >= 100;
    }

    public void reset() {
        movesInText.clear();
        positionHistory.clear();
        movesSinceLastCapture = 0;
    }

    public int getTotalMoves() {
        return movesInText.size();
    }

    public String getLastMove() {
        if (movesInText.isEmpty()) {
            return "";
        }
        return movesInText.get(movesInText.size() - 1);
    }
}