package com.sougata.sudoku;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.sougata.Constants;
import com.sougata.HelperFunctions;

import java.util.Calendar;

public class Database extends SQLiteOpenHelper {
    public static final String DB_NAME = "sudoku_sougata";
    public static final int DB_VERSION = 1;

    public Database(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE games(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " + //0
                "level INTEGER, " + //1
                "difficulty INTEGER, " + //2
                "difficulty_name TEXT, " + //3
                "timer INTEGER, " + //4
                "current_board_state TEXT," + //5
                "question TEXT," + //6
                "answer TEXT," + //7
                "hints INTEGER DEFAULT 0, " + //8
                "mistakes INTEGER, " + //9
                "type TEXT DEFAULT " + Constants.TYPES[0] + "," + //10
                "is_completed INTEGER DEFAULT 0," + //11
                "date INTEGER," + //12
                "created_at INTEGER," + // 13
                "notes TEXT DEFAULT '0'," + // 14
                "event_id TEXT DEFAULT 'none'" + // 15
                ")");
        db.execSQL("CREATE TABLE failed(id INTEGER PRIMARY KEY AUTOINCREMENT, difficulty_name TEXT, level INTEGER, type TEXT DEFAULT 'match')");
        db.execSQL("CREATE TABLE medals(id INTEGER PRIMARY KEY AUTOINCREMENT, event_id TEXT, event_name TEXT, medal_name TEXT, medal_url TEXT, created_at INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS games");
        db.execSQL("DROP TABLE IF EXISTS failed");
        db.execSQL("DROP TABLE IF EXISTS medals");
    }

    public long addNewGame(String level, int difficulty, String difficulty_name, int timer, String current_board_state, String question, String answer, String hints, int mistakes, String type, long date, String notes, String eventId) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("level", level);
        contentValues.put("difficulty", difficulty);
        contentValues.put("difficulty_name", difficulty_name);
        contentValues.put("timer", timer);
        contentValues.put("mistakes", mistakes);
        contentValues.put("type", type);
        contentValues.put("current_board_state", current_board_state);
        contentValues.put("question", question);
        contentValues.put("answer", answer);
        contentValues.put("is_completed", 0);
        contentValues.put("notes", notes);

        contentValues.put("event_id", eventId);
        if (date == 0) {
            contentValues.put("date", String.valueOf(c.getTimeInMillis()));
        } else {
            contentValues.put("date", String.valueOf(date));
        }
        contentValues.put("created_at", String.valueOf(Calendar.getInstance().getTimeInMillis()));
        return db.insert("games", null, contentValues);
    }

    public void makeGameComplete(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("is_completed", 1);
        contentValues.put("notes", "0");
        db.update("games", contentValues, "id=?", new String[]{String.valueOf(id)});
    }

    public void updateOngoing(long id, int timer, String current_board_state, int hints, int mistakes, String notes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("timer", timer);
        contentValues.put("current_board_state", current_board_state);
        contentValues.put("hints", hints);
        contentValues.put("mistakes", mistakes);
        contentValues.put("notes", notes);
        db.update("games", contentValues, "id=?", new String[]{String.valueOf(id)});
    }

    public Cursor getCompleted(String difficulty_name, String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM games WHERE difficulty_name=? AND type=? AND is_completed=?", new String[]{difficulty_name, type, "1"});
    }

    public Cursor getOngoingMatch() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM games WHERE is_completed=? AND type=? ORDER BY id DESC LIMIT 1", new String[]{"0", Constants.TYPES[0]});
    }

    public void updateOngoingTimer(long id, int timer) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("timer", timer);
        db.update("games", contentValues, "id=?", new String[]{String.valueOf(id)});
    }

    public void restartGame(long id, String currentBoardState) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("is_completed", 0);
        contentValues.put("timer", 0);
        contentValues.put("current_board_state", currentBoardState);
        contentValues.put("mistakes", 0);
        contentValues.put("notes", HelperFunctions.threeDimArrayToString(new int[9][9][9]));
        db.update("games", contentValues, "id=?", new String[]{String.valueOf(id)});
    }

    public Cursor getDailyMatch(long milli) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM games WHERE date=? AND type=?", new String[]{String.valueOf(milli), Constants.TYPES[1]});
    }

    public Cursor getEventDetails(int level, String eventId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM games WHERE level=? AND event_id=?", new String[]{String.valueOf(level), eventId});
    }

    public int getEventCompletedLevel(String eventId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MAX(level) FROM games WHERE event_id=? AND is_completed=?", new String[]{eventId, "1"});
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            return cursor.getInt(0);
        }
        return 0;
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
        Cursor cursor = db.rawQuery("SELECT MAX(level) FROM games WHERE difficulty_name = ? AND type = ? AND is_completed = ?", new String[]{difficulty_name, type, "1"});
        int maxLevel = 0;
        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            maxLevel = cursor.getInt(0);
        }
        cursor.close();
        return maxLevel;
    }

    public int getWinWithNoMistakes(String difficulty_name, String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM games WHERE difficulty_name = ? AND mistakes = 0 AND type = ? AND is_completed = 1", new String[]{difficulty_name, type});
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public Cursor getDailyMatch(int month, int year) {
        long startingDate, endingDate;
        Calendar c = HelperFunctions.getCalendar();
        c.set(Calendar.MONTH, month);
        c.set(Calendar.YEAR, year);
        c.set(Calendar.DATE, 1);
        startingDate = c.getTimeInMillis();
        c.add(Calendar.MONTH, 1);
        endingDate = c.getTimeInMillis();

        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT id, date FROM games WHERE date >= ? AND date < ? AND is_completed=? AND type=?", new String[]{String.valueOf(startingDate), String.valueOf(endingDate), "1", Constants.TYPES[1]});
    }

    public int getStartedMatchCount(String difficulty_name, String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM games WHERE difficulty_name = ? AND type = ?", new String[]{difficulty_name, type});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public Cursor getAllGames() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM games ORDER BY id DESC", null);
    }

    public Cursor getDifficultyGame(String difficulty_name) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM games WHERE difficulty_name=? AND type=?", new String[]{difficulty_name, Constants.TYPES[0]});
    }

    public Cursor getGameById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM games WHERE id=?", new String[]{String.valueOf(id)});
    }

    public void addMedal(String eventId, String eventName, String medalName, String medalUrl) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("event_id", eventId);
        contentValues.put("event_name", eventName);
        contentValues.put("medal_name", medalName);
        contentValues.put("medal_url", medalUrl);
        contentValues.put("created_at", String.valueOf(Calendar.getInstance().getTimeInMillis()));
        db.insert("medals", null, contentValues);
    }

    public Cursor getMedals() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM medals ORDER BY created_at DESC", null);
    }

    public Cursor getEventMedal(String eventId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM medals WHERE event_id=?", new String[]{eventId});
    }

    public void updateMedal(long id, String medalName, String medalUrl) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("medal_name", medalName);
        contentValues.put("medal_url", medalUrl);
        contentValues.put("created_at", String.valueOf(Calendar.getInstance().getTimeInMillis()));
        db.update("medals", contentValues, "id=?", new String[]{String.valueOf(id)});
    }

    public void printRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM games", null);

        if (cursor.moveToFirst()) {
            do {
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    System.out.print(cursor.getString(i) + " ");
                }
                System.out.println();
            } while (cursor.moveToNext());
        } else {
            System.out.println("No rows found.");
        }
        cursor.close();
    }
}
