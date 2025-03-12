package com.sougata.sudoku;

import com.sougata.Constants;

public class Sudoku {

    private final SudokuQuestionAnswer sudokuQuestionAnswer;

    public Sudoku(int level) {
        sudokuQuestionAnswer = sudokuGen(level);
    }

    public static boolean isColSafe(int[][] grid, int col, int value) {
        for (int row = 0; row < Constants.GRID_SIZE; row++) {
            if (grid[row][col] == value) {
                return false;
            }
        }
        return true;
    }

    public static boolean isRowSafe(int[][] grid, int row, int value) {
        for (int col = 0; col < Constants.GRID_SIZE; col++) {
            if (grid[row][col] == value) {
                return false;
            }
        }
        return true;
    }

    public static boolean isBoxSafe(int[][] grid, int boxRow, int boxCol, int value) {
        for (int row = 0; row < Constants.BOX_SIZE; row++) {
            for (int col = 0; col < Constants.BOX_SIZE; col++) {
                if (grid[row + boxRow][col + boxCol] == value) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isSafe(int[][] grid, int row, int col, int value) {
        int gridRow = row - (row % 3);
        int gridCol = col - (col % 3);
        return isColSafe(grid, col, value) && isRowSafe(grid, row, value) && isBoxSafe(grid, gridRow, gridCol, value);
    }

    private boolean findUnassignedPos(int[][] grid, Pos pos) {
        for (int row = 0; row < Constants.GRID_SIZE; row++) {
            for (int col = 0; col < Constants.GRID_SIZE; col++) {
                if (grid[row][col] == Constants.UNASSIGNED) {
                    pos.row = row;
                    pos.col = col;
                    return false;
                }
            }
        }
        return true;
    }

    private int[] shuffleArray() {
        int[] shuffledArray = new int[Constants.NUMBERS.length];
        System.arraycopy(Constants.NUMBERS, 0, shuffledArray, 0, Constants.NUMBERS.length);
        int currIndex = Constants.NUMBERS.length;
        while (currIndex != 0) {
            int randIndex = (int) (Math.floor(Math.random() * currIndex));
            currIndex--;

            int temp = shuffledArray[currIndex];
            shuffledArray[currIndex] = shuffledArray[randIndex];
            shuffledArray[randIndex] = temp;
        }
        return shuffledArray;
    }

    public boolean isGridFull(int[][] grid) {
        for (int[] row : grid) {
            for (int element : row) {
                if (element == Constants.UNASSIGNED) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean sudokuCreate(int[][] grid) {
        Pos unassignedPos = new Pos(0, 0);
        if (findUnassignedPos(grid, unassignedPos)) {
            return true;
        }
        int[] numberList = shuffleArray();

        int row = unassignedPos.row;
        int col = unassignedPos.col;

        for (int num : numberList) {
            if (isSafe(grid, row, col, num)) {
                grid[row][col] = num;
                if (isGridFull(grid)) {
                    return true;
                } else {
                    if (sudokuCreate(grid)) {
                        return true;
                    }
                }
                grid[row][col] = Constants.UNASSIGNED;
            }
        }
        return isGridFull(grid);
    }

    private int rand() {
        return (int) Math.floor(Math.random() * Constants.GRID_SIZE);
    }

    private int[][] removeCells(int[][] grid, int level) {
        int[][] res = new int[grid.length][grid.length];
        for (int i = 0; i < grid.length; i++) {
            System.arraycopy(grid[i], 0, res[i], 0, grid[0].length);
        }
        int attempts = level;
        while (attempts > 0) {
            int row = rand();
            int col = rand();
            while (res[row][col] == 0) {
                row = rand();
                col = rand();
            }
            res[row][col] = Constants.UNASSIGNED;
            attempts--;
        }
        return res;
    }

    public SudokuQuestionAnswer sudokuGen(int level) {
        int[][] sudoku = new int[Constants.GRID_SIZE][Constants.GRID_SIZE];
        sudokuCreate(sudoku);
        int[][] question = removeCells(sudoku, level);
        return new SudokuQuestionAnswer(question, sudoku);
    }

    public SudokuQuestionAnswer getQuestionAnswer() {
        return sudokuQuestionAnswer;
    }
}
