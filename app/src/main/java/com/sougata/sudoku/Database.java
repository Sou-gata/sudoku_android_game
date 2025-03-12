package com.sougata.sudoku;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.sougata.Constants;

public class Database extends SQLiteOpenHelper {
    public static final String DB_NAME = "sudoku_sougata";
    public static final int DB_VERSION = 1;

    public Database(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE completed(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "level TEXT, " +
                "difficulty INTEGER, " +
                "difficulty_name TEXT, " +
                "timer INTEGER, " +
                "mistakes INTEGER, " +
                "type TEXT DEFAULT 'match'" +
                ")");
        db.execSQL("CREATE TABLE ongoing(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "level TEXT, " +
                "difficulty INTEGER, " +
                "difficulty_name TEXT, " +
                "timer INTEGER, " +
                "current_board_state TEXT," +
                "question TEXT," +
                "answer TEXT," +
                "hints TEXT DEFAULT '0', " +
                "mistakes INTEGER, " +
                "type TEXT DEFAULT 'match'" +
                ")");
        db.execSQL("CREATE TABLE failed(id INTEGER PRIMARY KEY AUTOINCREMENT, difficulty_name TEXT, level INTEGER, type TEXT DEFAULT 'match')");
        db.execSQL("CREATE TABLE started(id INTEGER PRIMARY KEY AUTOINCREMENT, difficulty_name TEXT, type TEXT DEFAULT 'match')");
        db.execSQL("CREATE TABLE daily(id INTEGER PRIMARY KEY AUTOINCREMENT, date INTEGER, month INTEGER, year INTEGER, timer INTEGER, mistakes INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS completed");
        db.execSQL("DROP TABLE IF EXISTS ongoing");
        db.execSQL("DROP TABLE IF EXISTS failed");
        db.execSQL("DROP TABLE IF EXISTS started");
        db.execSQL("DROP Table IF EXISTS daily");
    }

    public void addCompleted(String level, int difficulty, String difficulty_name, int timer, int mistakes, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("level", level);
        contentValues.put("difficulty", difficulty);
        contentValues.put("difficulty_name", difficulty_name);
        contentValues.put("timer", timer);
        contentValues.put("mistakes", mistakes);
        contentValues.put("type", type);
        db.insert("completed", null, contentValues);
    }

    public void updateOngoing(String level, int difficulty, String difficulty_name, int timer, String current_board_state, String question, String answer, String hints, int mistakes, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("level", level);
        contentValues.put("difficulty", difficulty);
        contentValues.put("difficulty_name", difficulty_name);
        contentValues.put("timer", timer);
        contentValues.put("current_board_state", current_board_state);
        contentValues.put("question", question);
        contentValues.put("answer", answer);
        contentValues.put("hints", hints);
        contentValues.put("mistakes", mistakes);
        contentValues.put("type", type);
        db.update("ongoing", contentValues, "id=?", new String[]{"1"});
    }

    public Cursor getCompleted(String difficulty_name, String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM completed WHERE difficulty_name=? AND type=?", new String[]{difficulty_name, type});
    }

    public Cursor getOngoing() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM ongoing", null);
    }

    public void emptyOnGoing() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("level", "0");
        contentValues.put("difficulty", 0);
        contentValues.put("difficulty_name", "none");
        contentValues.put("timer", 0);
        contentValues.put("current_board_state", "none");
        contentValues.put("question", "none");
        contentValues.put("answer", "none");
        contentValues.put("hints", "none");
        contentValues.put("mistakes", 0);
        contentValues.put("type", Constants.TYPES[0]);
        db.update("ongoing", contentValues, "id=?", new String[]{"1"});
    }

    public void createFirstOngoing() {
        Cursor cursor = getOngoing();
        if (cursor.getCount() == 0) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("level", "0");
            contentValues.put("difficulty", 0);
            contentValues.put("difficulty_name", "none");
            contentValues.put("timer", 0);
            contentValues.put("current_board_state", "none");
            contentValues.put("question", "none");
            contentValues.put("answer", "none");
            contentValues.put("hints", "none");
            db.insert("ongoing", null, contentValues);
        }
    }

    public void updateOngoingTimer(int timer) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("timer", timer);
        db.update("ongoing", contentValues, "id=?", new String[]{"1"});
    }

    public void updateOngoing(String level, int difficulty, String difficulty_name, int timer, String current_board_state, String hints, int mistakes, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("level", level);
        contentValues.put("difficulty", difficulty);
        contentValues.put("difficulty_name", difficulty_name);
        contentValues.put("timer", timer);
        contentValues.put("current_board_state", current_board_state);
//        contentValues.put("hints", hints);
        contentValues.put("mistakes", mistakes);
        contentValues.put("type", type);
        db.update("ongoing", contentValues, "id=?", new String[]{"1"});
    }

    public void addFailedLevel(String difficulty_name, String level, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("level", level);
        contentValues.put("difficulty_name", difficulty_name);
        contentValues.put("type", type);
        db.insert("failed", null, contentValues);
    }

    public Cursor getFailedLevels(String difficulty_name, String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM failed WHERE difficulty_name=? AND type=?", new String[]{difficulty_name, type});
    }

    public int getMaxFailedLevel(String difficulty_name, String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MAX(level) FROM failed WHERE difficulty_name = ? AND type = ?", new String[]{difficulty_name, type});

        int maxLevel = 0;
        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            maxLevel = cursor.getInt(0);
        }
        cursor.close();
        return maxLevel;
    }

    public int getMaxLevel(String difficulty_name, String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MAX(level) FROM completed WHERE difficulty_name = ? AND type = ?", new String[]{difficulty_name, type});
        int maxLevel = 0;
        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            maxLevel = cursor.getInt(0);
        }
        cursor.close();
        return maxLevel;
    }

    public int getWinWithNoMistakes(String difficulty_name, String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM completed WHERE difficulty_name = ? AND mistakes = 0 AND type = ?", new String[]{difficulty_name, type});
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public void addDailyMatch(int date, int month, int year) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("date", date);
        contentValues.put("month", month);
        contentValues.put("year", year);
        db.insert("daily", null, contentValues);
    }

    public Cursor getDailyMatch(int month, int year) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM daily WHERE month = ? AND year = ?", new String[]{String.valueOf(month), String.valueOf(year)});
    }

    public int getStartedMatchCount(String difficulty_name, String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM started WHERE difficulty_name = ? AND type = ?", new String[]{difficulty_name, type});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public void addStartedMatch(String difficulty_name, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("difficulty_name", difficulty_name);
        contentValues.put("type", type);
        db.insert("started", null, contentValues);
    }
}
