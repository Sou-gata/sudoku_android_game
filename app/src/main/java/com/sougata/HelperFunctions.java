package com.sougata;

import android.content.Context;
import android.content.res.Resources;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.view.WindowMetrics;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

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

    public static String threeDimArrayToString(int[][][] arr) {
        StringBuilder sb = new StringBuilder();
        for (int[][] row : arr) {
            sb.append(twoDimArrayToString(row));
            sb.append(";");
        }
        return sb.toString();
    }

    public static int[][][] parseThreeDimArr(String str) {
        if (str == null || str.length() < 100) return new int[9][9][9];
        String[] rows = str.split(";");
        int[][][] arr = new int[rows.length][][];
        for (int i = 0; i < rows.length; i++) {
            arr[i] = parseTwoDimArray(rows[i]);
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

    public static Calendar getCalendar() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }

    public static String get12hClock(int h, int m) {
        String meridian = "AM";
        if (h >= 12) meridian = "PM";
        if (h > 12) h -= 12;
        if (h == 0) h = 12;
        return padString(h, 2) + ":" + padString(m, 2) + " " + meridian;
    }

    public static int[][][] generateAdvanceNote(int[][] board) {
        int[][][] notes = new int[9][9][9];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j] == 0) {
                    for (int k = 1; k <= 9; k++) {
                        if (isSafe(board, i, j, k)) {
                            notes[i][j][k - 1] = 1;
                        }
                    }
                }
            }
        }
        return notes;
    }

    public static int getScreenWidth(Context context) {
        WindowManager windowManager = context.getSystemService(WindowManager.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            WindowMetrics metrics = windowManager.getCurrentWindowMetrics();
            return metrics.getBounds().width();
        } else {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(displayMetrics);
            return displayMetrics.widthPixels;
        }
    }

    public static int getScreenHeight(Context context) {
        WindowManager windowManager = context.getSystemService(WindowManager.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            WindowMetrics metrics = windowManager.getCurrentWindowMetrics();
            return metrics.getBounds().width();
        } else {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(displayMetrics);
            return displayMetrics.heightPixels;
        }
    }

    public static String encrypt(String text, String key) {
        try {
            String KEY = key.substring(0, 16);
            String IV = key.substring(16, 32);
            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8));
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(encrypted, Base64.DEFAULT);
        } catch (Exception ignored) {
            return "";
        }
    }

    public static String decrypt(String text, String key) {
        try {
            String KEY = key.substring(0, 16);
            String IV = key.substring(16, 32);
            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8));
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            text = text.replace("\\n", "").replace("\n", "").replace("\r", "").trim();
            byte[] decoded = Base64.decode(text, Base64.DEFAULT);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception ignore) {
            return "";
        }
    }

    public static String getMadelName(String name) {
        String[] arr = name.split("_");
        StringBuilder sb = new StringBuilder();
        for (String s : arr) {
            sb.append(s.substring(0, 1).toUpperCase()).append(s.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    public static String millisToDate(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        return HelperFunctions.padString(c.get(Calendar.DATE), 2) + "-" + HelperFunctions.padString(c.get(Calendar.MONTH) + 1, 2) + "-" + c.get(Calendar.YEAR);
    }
}

