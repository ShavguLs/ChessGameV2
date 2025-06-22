package com.ShavguLs.chess.common.logic;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

// This class helps save chess games in PGN format
public class GameRecorder {

    private String player1Name;
    private String player2Name;
    private String gameName;
    private String gameLocation;
    private String roundNumber;
    private String finalResult;
    private ArrayList<String> gameMovements;
    private String timeSettings;

    public GameRecorder() {
        this.gameName = "Chess Game";
        this.gameLocation = "Local Computer";
        this.roundNumber = "1";
        this.finalResult = "*"; // means game is still going
        this.gameMovements = new ArrayList<String>();
    }

    public void setPlayerNames(String whiteName, String blackName) {
        this.player1Name = whiteName;
        this.player2Name = blackName;
    }

    public void setGameInformation(String event, String site, String round) {
        if (event != null && !event.equals("")) {
            this.gameName = event;
        }
        if (site != null && !site.equals("")) {
            this.gameLocation = site;
        }
        if (round != null && !round.equals("")) {
            this.roundNumber = round;
        }
    }

    public void setTimeInformation(int hours, int minutes, int seconds) {
        if (hours == 0 && minutes == 0 && seconds == 0) {
            this.timeSettings = "No time limit";
        } else {
            int totalTime = hours * 3600 + minutes * 60 + seconds;
            this.timeSettings = String.valueOf(totalTime);
        }
    }

    public void addMove(String move) {
        this.gameMovements.add(move);
    }

    public void setAllMoves(ArrayList<String> moves) {
        this.gameMovements = new ArrayList<String>(moves);
    }

    public void setGameResult(String result) {
        this.finalResult = result;
    }

    public String createPGNText() {
        StringBuilder pgnText = new StringBuilder();
        pgnText.append("[Event \"").append(gameName).append("\"]\n");
        pgnText.append("[Site \"").append(gameLocation).append("\"]\n");
        Date currentDate = new Date();
        String dateString = String.format("%tY.%tm.%td", currentDate, currentDate, currentDate);
        pgnText.append("[Date \"").append(dateString).append("\"]\n");
        pgnText.append("[Round \"").append(roundNumber).append("\"]\n");
        String whiteName = (player1Name != null) ? player1Name : "White Player";
        String blackName = (player2Name != null) ? player2Name : "Black Player";
        pgnText.append("[White \"").append(whiteName).append("\"]\n");
        pgnText.append("[Black \"").append(blackName).append("\"]\n");
        pgnText.append("[Result \"").append(finalResult).append("\"]\n");

        if (timeSettings != null) {
            pgnText.append("[TimeControl \"").append(timeSettings).append("\"]\n");
        }
        pgnText.append("\n");
        if (gameMovements != null && gameMovements.size() > 0) {
            StringBuilder moveLine = new StringBuilder();
            for (int i = 0; i < gameMovements.size(); i++) {
                if (i % 2 == 0) {
                    if (moveLine.length() > 0) {
                        moveLine.append(" ");
                    }
                    int moveNumber = (i / 2) + 1;
                    moveLine.append(moveNumber).append(". ");
                } else {
                    moveLine.append(" ");
                }
                moveLine.append(gameMovements.get(i));
                if (moveLine.length() > 75) {
                    pgnText.append(moveLine.toString()).append("\n");
                    moveLine = new StringBuilder();
                }
            }
            if (moveLine.length() > 0) {
                pgnText.append(moveLine.toString());
            }
        }
        pgnText.append(" ").append(finalResult).append("\n");

        return pgnText.toString();
    }

    public boolean saveToFile(String fileName) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(createPGNText());
            writer.close();
            return true;
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
            return false;
        }
    }

    public static String convertResultToPGN(boolean whiteWon, String reason) {
        if (reason.equals("stalemate") ||
                reason.equals("insufficient material") ||
                reason.equals("50-move rule") ||
                reason.equals("threefold repetition")) {
            return "1/2-1/2"; // draw
        }
        else if (reason.equals("checkmate") || reason.contains("time")) {
            if (whiteWon) {
                return "1-0"; // white wins
            } else {
                return "0-1"; // black wins
            }
        }
        else {
            return "*";
        }
    }
}