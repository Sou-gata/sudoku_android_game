package com.sougata.sudoku;

public class Pos {
    public int row, col;

    public Pos(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public void setPos(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return this.row;
    }

    public int getCol() {
        return this.col;
    }
}
