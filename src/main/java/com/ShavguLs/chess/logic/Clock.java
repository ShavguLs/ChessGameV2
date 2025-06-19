package com.ShavguLs.chess.logic;

public class Clock {
    private int hh;
    private int mm;
    private int ss;
    private final int initialSeconds;

    public Clock(int hh, int mm, int ss) {
        this.hh = hh;
        this.mm = mm;
        this.ss = ss;
        this.initialSeconds = hh * 3600 + mm * 60 + ss;
    }

    public boolean outOfTime() {
        return (hh == 0 && mm == 0 && ss == 0);
    }

    public void decr() {
        if (this.ss == 0) {
            if (this.mm == 0) {
                if (this.hh > 0) {
                    this.hh--;
                    this.mm = 59;
                    this.ss = 59;
                }
            } else {
                this.mm--;
                this.ss = 59;
            }
        } else {
            this.ss--;
        }
    }

    public String getTime() {
        String fHrs = String.format("%02d", this.hh);
        String fMins = String.format("%02d", this.mm);
        String fSecs = String.format("%02d", this.ss);
        String fTime = fHrs + ":" + fMins + ":" + fSecs;
        return fTime;
    }

    public int getInitialSeconds() {
        return initialSeconds;
    }

    public int getHours(){
        return hh;
    }

    public int getMinutes(){
        return mm;
    }

    public int getSeconds(){
        return ss;
    }
}