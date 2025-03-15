package com.sougata.sudoku.fragments;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.sougata.Constants;
import com.sougata.HelperFunctions;
import com.sougata.sudoku.Database;
import com.sougata.sudoku.R;

public class DetailsStatisticsFragment extends Fragment {
    Database db;

    int shortestTime = 0, avgTime = 0, gameStarted = 0, gameOwn = 0, winRate = 0, winWithMistake = 0;

    public static DetailsStatisticsFragment newInstance(String difficultyName, String type) {
        DetailsStatisticsFragment fragment = new DetailsStatisticsFragment();
        Bundle args = new Bundle();
        args.putString("difficultyName", difficultyName);
        args.putString("type", type);
        fragment.setArguments(args);
        return fragment;

    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details_statistics, container, false);
        if (getArguments() != null) {
            db = new Database(requireContext());
            String difficultyName = getArguments().getString("difficultyName");
            String type = getArguments().getString("type");
            calculateStatus(difficultyName, type);

            int longestStreak = getCurrentStreak(difficultyName, type);
            int currentStreak = getLongestStreak(difficultyName, type);

            TextView gameStarted = view.findViewById(R.id.tv_status_game_started);
            TextView gameOwn = view.findViewById(R.id.tv_status_game_own);
            TextView winRate = view.findViewById(R.id.tv_status_win_rate);
            TextView winWithMistake = view.findViewById(R.id.tv_status_win_with_no_mistake);
            TextView bestTime = view.findViewById(R.id.tv_status_best_time);
            TextView averageTime = view.findViewById(R.id.tv_status_average_time);
            TextView currentStreakTextView = view.findViewById(R.id.tv_status_current_win_streak);
            TextView longestStreakTextView = view.findViewById(R.id.tv_status_longest_win_streak);

            gameStarted.setText(String.valueOf(this.gameStarted));
            gameOwn.setText(String.valueOf(this.gameOwn));
            winRate.setText(this.winRate + "%");
            winWithMistake.setText(String.valueOf(this.winWithMistake));
            bestTime.setText(HelperFunctions.timerToString(this.shortestTime));
            averageTime.setText(HelperFunctions.timerToString(this.avgTime));
            currentStreakTextView.setText(String.valueOf(currentStreak));
            longestStreakTextView.setText(String.valueOf(longestStreak));
        }
        return view;
    }

    private int getCurrentStreak(String difficultyName, String type) {
        int currentLevel = db.getMaxLevel(difficultyName, type);
        int lastFailedLevel = db.getMaxFailedLevel(difficultyName, type);
        if (currentLevel > lastFailedLevel && lastFailedLevel != 0) {
            return currentLevel - lastFailedLevel + 1;
        } else if (currentLevel > lastFailedLevel) {
            return currentLevel;
        } else if (lastFailedLevel == currentLevel && currentLevel != 0) {
            return 1;
        } else {
            return 0;
        }
    }

    private int getLongestStreak(String difficultyName, String type) {
        Cursor failed = db.getFailedLevels(difficultyName, type);
        if (failed.getCount() == 0) {
            return db.getMaxLevel(difficultyName, Constants.TYPES[0]);
        }
        int longestStreak = 0;
        int lastFailedLevel = 0;
        failed.moveToFirst();
        do {
            int currentLevel = failed.getInt(2);
            int streak = currentLevel - lastFailedLevel;
            lastFailedLevel = currentLevel;
            if (streak > longestStreak) {
                longestStreak = streak;
            }
        } while (failed.moveToNext());
        return longestStreak;
    }

    private void calculateStatus(String difficultyName, String type) {
        Cursor completed = db.getCompleted(difficultyName, type);
        if (completed.getCount() > 0) {
            completed.moveToFirst();
            int totalTime = 0;
            int smallestTime = completed.getInt(4);
            do {
                int time = completed.getInt(4);
                totalTime += time;
                if (time < smallestTime) {
                    smallestTime = time;
                }

            } while (completed.moveToNext());
            shortestTime = smallestTime;
            avgTime = totalTime / completed.getCount();
            gameOwn = completed.getCount();
        }
        gameStarted = db.getStartedMatchCount(difficultyName, type);
        if (gameStarted > 0) {
            winRate = (int) (((float) gameOwn / (float) gameStarted) * 100);
        }
        winWithMistake = db.getWinWithNoMistakes(difficultyName, type);
    }
}