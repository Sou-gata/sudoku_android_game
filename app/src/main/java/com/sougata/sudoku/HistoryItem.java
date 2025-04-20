package com.sougata.sudoku;

public class HistoryItem {
    private String difficulty, date, time, timer, type;
    private int hint, mistake;
    private long id;
    private boolean isCompleted;

    public HistoryItem(long id, String difficulty, String timer, int hint, int mistake, String date, String time, boolean isCompleted, String type) {
        this.id = id;
        this.difficulty = difficulty;
        this.date = date;
        this.time = time;
        this.timer = timer;
        this.hint = hint;
        this.mistake = mistake;
        this.isCompleted = isCompleted;
        this.type = type;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTimer() {
        return timer;
    }

    public void setTimer(String timer) {
        this.timer = timer;
    }

    public int getHint() {
        return hint;
    }

    public void setHint(int hint) {
        this.hint = hint;
    }

    public int getMistake() {
        return mistake;
    }

    public void setMistake(int mistake) {
        this.mistake = mistake;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
