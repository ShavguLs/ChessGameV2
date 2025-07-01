package com.ShavguLs.chess.common.logic;

public class PGNManager {
    private GameRecorder recorder;
    private MoveTracker moveTracker;

    public PGNManager(MoveTracker tracker) {
        this.moveTracker = tracker;
        this.recorder = new GameRecorder();
    }

    public void setPlayerNames(String whiteName, String blackName) {
        recorder.setPlayerNames(whiteName, blackName);
    }

    public void setTimeControl(int hours, int minutes, int seconds) {
        recorder.setTimeInformation(hours, minutes, seconds);
    }

    public void setGameInfo(String event, String site, String round) {
        recorder.setGameInformation(event, site, round);
    }

    public void setResult(String result) {
        recorder.setGameResult(result);
    }

    public String getPGNText() {
        recorder.setAllMoves(moveTracker.getAllMoves());
        return recorder.createPGNText();
    }

    public String getWhitePlayerName() { return recorder.getWhitePlayerName(); }
    public String getBlackPlayerName() { return recorder.getBlackPlayerName(); }
    public String getResult() { return recorder.getResult(); }

    public boolean saveToFile(String filename) {
        recorder.setAllMoves(moveTracker.getAllMoves());
        return recorder.saveToFile(filename);
    }

    public void reset() {
        recorder = new GameRecorder();
    }

    public String convertGameResult(boolean whiteWins, String reason) {
        return GameRecorder.convertResultToPGN(whiteWins, reason);
    }
}