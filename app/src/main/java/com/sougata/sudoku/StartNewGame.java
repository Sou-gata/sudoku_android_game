package com.sougata.sudoku;

import android.content.Context;
import android.database.Cursor;

import com.sougata.Constants;
import com.sougata.GlobalStore;
import com.sougata.HelperFunctions;

import java.util.Arrays;
import java.util.Calendar;

public class StartNewGame {
    private final Database db;
    private final GlobalStore globalStore = GlobalStore.getInstance();

    public StartNewGame(Context context) {
        db = new Database(context);
    }

    public void createEasyGame() {
        globalStore.setDifficulty(Constants.LEVEL[0]);
        globalStore.setDifficultyName(Constants.LEVEL_NAME[0]);
        globalStore.setType(Constants.TYPES[0]);
        createGame();
    }

    public void createMediumGame() {
        globalStore.setDifficulty(Constants.LEVEL[1]);
        globalStore.setDifficultyName(Constants.LEVEL_NAME[1]);
        globalStore.setType(Constants.TYPES[0]);
        createGame();
    }

    public void createHardGame() {
        globalStore.setDifficulty(Constants.LEVEL[2]);
        globalStore.setDifficultyName(Constants.LEVEL_NAME[2]);
        globalStore.setType(Constants.TYPES[0]);
        createGame();
    }

    public void createExpertGame() {
        globalStore.setDifficulty(Constants.LEVEL[3]);
        globalStore.setDifficultyName(Constants.LEVEL_NAME[3]);
        globalStore.setType(Constants.TYPES[0]);
        createGame();
    }

    public void createNightmareGame() {
        globalStore.setDifficulty(Constants.LEVEL[4]);
        globalStore.setDifficultyName(Constants.LEVEL_NAME[4]);
        globalStore.setType(Constants.TYPES[0]);
        createGame();
    }

    public void createDailyGame(int day) {
        int difficulty = (int) (Math.random() * 3);
        globalStore.setDifficulty(Constants.LEVEL[difficulty]);
        globalStore.setDifficultyName(Constants.LEVEL_NAME[difficulty]);
        globalStore.setType(Constants.TYPES[1]);
        globalStore.setDay(day);
        createGame();
    }

    private void createGame() {
        Calendar c = HelperFunctions.getCalendar();
        c.set(Calendar.DATE, globalStore.getDay());
        c.set(Calendar.MONTH, globalStore.getMonth());
        c.set(Calendar.YEAR, globalStore.getYear());

        if (globalStore.getType().equals(Constants.TYPES[1])) {
            Cursor cursor = db.getDailyMatch(c.getTimeInMillis());
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                globalStore.setId(cursor.getLong(0));
                globalStore.setCurrentLevel(cursor.getInt(1));
                globalStore.setDifficulty(cursor.getInt(2));
                globalStore.setDifficultyName(cursor.getString(3));
                globalStore.setTimer(cursor.getInt(4));
                globalStore.setCurrentBoardState(HelperFunctions.parseTwoDimArray(cursor.getString(5)));
                globalStore.setBoard(HelperFunctions.parseTwoDimArray(cursor.getString(6)));
                globalStore.setSolution(HelperFunctions.parseTwoDimArray(cursor.getString(7)));
                globalStore.setMistakes(cursor.getInt(9));
                globalStore.setNotes(HelperFunctions.parseThreeDimArr(cursor.getString(14)));
                return;
            }
        }
        Sudoku sudoku = new Sudoku(globalStore.getDifficulty());
        SudokuQuestionAnswer questionAnswer = sudoku.getQuestionAnswer();
        int[][] question = questionAnswer.question;
        int[][] currentBoardState = new int[question.length][question[0].length];
        for (int i = 0; i < question.length; i++) {
            currentBoardState[i] = Arrays.copyOf(question[i], question[i].length);
        }
        globalStore.setBoard(questionAnswer.question);
        globalStore.setSolution(questionAnswer.answer);
        globalStore.setMistakes(0);
        globalStore.setCurrentBoardState(currentBoardState);
        globalStore.setTimer(0);
        globalStore.setNotes(new int[Constants.GRID_SIZE][Constants.GRID_SIZE][Constants.GRID_SIZE]);
        long id = db.addNewGame(String.valueOf(globalStore.getCurrentLevel()), globalStore.getDifficulty(), globalStore.getDifficultyName(), globalStore.getTimer(), HelperFunctions.twoDimArrayToString(globalStore.getCurrentBoardState()), HelperFunctions.twoDimArrayToString(globalStore.getBoard()), HelperFunctions.twoDimArrayToString(globalStore.getSolution()), "0", globalStore.getMistakes(), globalStore.getType(), globalStore.getType().equals(Constants.TYPES[0]) ? 0 : c.getTimeInMillis(), HelperFunctions.threeDimArrayToString(globalStore.getNotes()));
        globalStore.setId(id);
    }

    public void restartGame() {
        int[][] currentBoardState = new int[globalStore.getBoard().length][globalStore.getBoard()[0].length];
        for (int i = 0; i < currentBoardState.length; i++) {
            for (int j = 0; j < currentBoardState[i].length; j++) {
                currentBoardState[i][j] = globalStore.getBoard()[i][j];
            }
        }
        globalStore.setTimer(0);
        globalStore.setMistakes(0);
        globalStore.setCurrentBoardState(currentBoardState);
        globalStore.setNotes(new int[9][9][9]);
        db.restartGame(globalStore.getId(), HelperFunctions.twoDimArrayToString(currentBoardState));
    }
}
