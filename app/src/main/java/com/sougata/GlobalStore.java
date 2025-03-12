package com.sougata;

public class GlobalStore {
    private static final GlobalStore instance = new GlobalStore();
    private int difficulty, mistakes, currentLevel, day, month, year;
    private String difficultyName, type;
    private int[][] board, solution, currentBoardState;

    private int timer;

    private GlobalStore() {
    }

    public String getDifficultyName() {
        return difficultyName;
    }

    public void setDifficultyName(String difficultyName) {
        this.difficultyName = difficultyName;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public static GlobalStore getInstance() {
        return instance;
    }

    public int getMistakes() {
        return mistakes;
    }

    public void setMistakes(int mistakes) {
        this.mistakes = mistakes;
    }

    public int[][] getBoard() {
        return board;
    }

    public void setBoard(int[][] board) {
        this.board = board;
    }

    public int[][] getSolution() {
        return solution;
    }

    public void setSolution(int[][] solution) {
        this.solution = solution;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }

    public int[][] getCurrentBoardState() {
        return currentBoardState;
    }

    public void setCurrentBoardState(int[][] currentBoardState) {
        this.currentBoardState = currentBoardState;
    }

    public int getTimer() {
        return timer;
    }

    public void setTimer(int timer) {
        this.timer = timer;
    }

    public void emptyCurrentState() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                currentBoardState[i][j] = 0;
                board[i][j] = 0;
                solution[i][j] = 0;
            }
        }
        mistakes = 0;
        timer = 0;
        currentLevel = 0;
        difficulty = 81;
        difficultyName = "none";
        type = Constants.TYPES[0];
        day = 0;
        month = -1;
        year = 0;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
