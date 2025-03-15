package com.sougata;

import android.content.res.Resources;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class HelperFunctions {
    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static String timerToString(int time) {
        int minutes = time / 60;
        int seconds = time % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    public static boolean isSafe(int row, int col, int value) {
        int[][] ans = GlobalStore.getInstance().getSolution();
        return ans[row][col] == value;
    }

    public static boolean isRowSafe(int[][] grid, int row, int col, int value) {
        for (int i = 0; i < Constants.GRID_SIZE; i++) {
            if (grid[row][i] == value && i != col) {
                return false;
            }
        }
        return true;
    }

    public static boolean isColSafe(int[][] grid, int row, int col, int value) {
        for (int i = 0; i < Constants.GRID_SIZE; i++) {
            if (grid[i][col] == value && i != row) {
                return false;
            }
        }
        return true;
    }

    public static boolean isBoxSafe(int[][] grid, int row, int col, int value) {
        int boxRow = row / Constants.BOX_SIZE;
        int boxCol = col / Constants.BOX_SIZE;
        for (int r = boxRow * Constants.BOX_SIZE; r < boxRow * Constants.BOX_SIZE + Constants.BOX_SIZE; r++) {
            for (int c = boxCol * Constants.BOX_SIZE; c < boxCol * Constants.BOX_SIZE + Constants.BOX_SIZE; c++) {
                if (grid[r][c] == value) {
                    if (row != r && col != c) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static boolean isSafe(int[][] grid, int row, int col, int value) {
        return isRowSafe(grid, row, col, value) && isColSafe(grid, row, col, value) && isBoxSafe(grid, row, col, value);
    }

    public static ArrayList<int[]> getEmptyCells(int[][] grid) {
        ArrayList<int[]> emptyCells = new ArrayList<>();
        for (int i = 0; i < Constants.GRID_SIZE; i++) {
            for (int j = 0; j < Constants.GRID_SIZE; j++) {
                if (grid[i][j] == 0) {
                    emptyCells.add(new int[]{i, j});
                }
            }
        }
        return emptyCells;
    }

    public static String singleDimArrayToString(int[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            sb.append(arr[i]);
            if (i < arr.length - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    public static String twoDimArrayToString(int[][] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            sb.append(singleDimArrayToString(arr[i]));
            if (i < arr.length - 1) {
                sb.append("|");
            }
        }
        return sb.toString();
    }

    public static int[] parseSingleDimArray(String str) {
        String[] parts = str.split(",");
        int[] arr = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            arr[i] = Integer.parseInt(parts[i]);
        }
        return arr;
    }

    public static int[][] parseTwoDimArray(String str) {
        String[] rows = str.split("\\|");
        int[][] arr = new int[rows.length][];
        for (int i = 0; i < rows.length; i++) {
            arr[i] = parseSingleDimArray(rows[i]);
        }
        return arr;
    }

    public static String padString(int num, int length) {
        String str = String.valueOf(num);
        if (str.length() == length) return str;
        StringBuilder s = new StringBuilder(str);
        while (s.length() < length) {
            s.insert(0, "0");
        }
        return s.toString();
    }

    public static Calendar getCalendar(){
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }
}
