package com.sougata.sudoku.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.sougata.Constants;
import com.sougata.GlobalStore;
import com.sougata.HelperFunctions;
import com.sougata.sudoku.Database;
import com.sougata.sudoku.Pos;
import com.sougata.sudoku.R;
import com.sougata.sudoku.StartNewGame;
import com.sougata.sudoku.fragments.DailyFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends AppCompatActivity {

    int difficulty, mistakes, currentLevel;
    String difficultyName;
    int[][] answer, question, currentBoardState;
    boolean isPopupOpened = false;
    LinearLayout gameBoard, hintButton, pauseGame, numberRow;
    TextView gameTimer, gameDifficulty, gameMistakes, currentLevelText;
    ImageView backBtn, pauseResumeIcon;
    PopupWindow popupWindow;

    GlobalStore globalStore = GlobalStore.getInstance();
    Database db;
    private final StartNewGame startNewGame = new StartNewGame(this);

    Pos currSelectedCell = new Pos(-1, -1);

    int timer;
    Timer gameTimerObj;
    HashMap<Integer, Integer> numberCounts = new HashMap<>();
    String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game);

        db = new Database(this);

        Cursor c = db.getCompleted(globalStore.getDifficultyName(), globalStore.getType());
        if (c.getCount() == 0) {
            globalStore.setCurrentLevel(1);
        } else {
            globalStore.setCurrentLevel(c.getCount() + 1);
        }
        globalStore.setPaused(false);
        answer = globalStore.getSolution();
        question = globalStore.getBoard();
        difficulty = globalStore.getDifficulty();
        difficultyName = globalStore.getDifficultyName();
        mistakes = globalStore.getMistakes();
        currentBoardState = globalStore.getCurrentBoardState();
        timer = globalStore.getTimer();
        currentLevel = globalStore.getCurrentLevel();

        gameBoard = findViewById(R.id.ll_game_board);
        gameTimer = findViewById(R.id.tv_game_timer);
        gameDifficulty = findViewById(R.id.tv_game_difficulty);
        gameMistakes = findViewById(R.id.tv_game_mistakes);
        currentLevelText = findViewById(R.id.tv_game_level);
        hintButton = findViewById(R.id.ll_hint);
        backBtn = findViewById(R.id.back_button);
        pauseGame = findViewById(R.id.ll_game_timer_pause);
        pauseResumeIcon = findViewById(R.id.iv_pause_resume_icon);
        numberRow = findViewById(R.id.ll_number_input);

        countNumbers();
        generateGameBoard();
        createNumberInputRow();

        LinearLayout ll_erase = findViewById(R.id.ll_erase);
        ll_erase.setOnClickListener(view -> eraseClicked());

        Task task = new Task();
        gameTimerObj = new Timer();
        gameTimerObj.schedule(task, 1000, 1000);

        String mistakeString = "Mistake: " + mistakes + "/3";
        gameMistakes.setText(mistakeString);

        if (!globalStore.getType().equals("daily")) {
            String curLevelStr = "Level " + (currentLevel == 0 ? "1" : currentLevel);
            currentLevelText.setText(curLevelStr);
            gameDifficulty.setText(difficultyName);
        } else {
            Intent intent = getIntent();
            String date = HelperFunctions.padString(intent.getIntExtra("date", 0), 2) + " " + months[globalStore.getMonth()];
            gameDifficulty.setText(date);
            currentLevelText.setText(getString(R.string.daily_challenge));
        }

        hintButton.setOnClickListener(view -> hitCLicked());
        backBtn.setOnClickListener(view -> {
            globalStore.setPaused(true);
            if (popupWindow != null) {
                popupWindow.dismiss();
            }
            finish();
        });
        pauseGame.setOnClickListener(view -> {
            showPausePopup(view);
            pauseGame();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        popupWindow.dismiss();
        globalStore.setPaused(true);
        gameTimerObj.cancel();
        popupWindow = null;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus && !isPopupOpened) {
            globalStore.setPaused(true);
            showPausePopup(new View(this));
            pauseGame();
        }
    }

    private void generateGameBoard() {
        LinearLayout boardRow = new LinearLayout(this);
        boardRow.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        boardRow.setOrientation(LinearLayout.HORIZONTAL);

        for (int i = 0; i < 9; i++) {
            LinearLayout parentLayout = new LinearLayout(this);
            parentLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            parentLayout.setOrientation(LinearLayout.HORIZONTAL);
            for (int j = 0; j < 9; j++) {
                LinearLayout childLayout = new LinearLayout(this);
                int screenWidth = getResources().getDisplayMetrics().widthPixels;
                int childHeight = (screenWidth - HelperFunctions.dpToPx(30) - 12) / 9;
                int childWidth = (screenWidth - HelperFunctions.dpToPx(30) - 12) / 9;
                LinearLayout.LayoutParams childParams = new LinearLayout.LayoutParams(childWidth, childHeight);
                childLayout.setLayoutParams(childParams);
                childLayout.setBackground(createBorderDrawable("#FFFFFF"));
                int margin = 2;
                if (i % 3 == 0 && j % 3 == 0) {
                    childParams.setMargins(margin, margin, 0, 0);
                } else if (i % 3 == 2 && j % 3 == 2) {
                    childParams.setMargins(0, 0, margin, margin);
                } else if (i % 3 == 0 && j % 3 == 2) {
                    childParams.setMargins(0, margin, margin, 0);
                } else if (i % 3 == 2 && j % 3 == 0) {
                    childParams.setMargins(margin, 0, 0, margin);
                } else if (i % 3 == 1 && j % 3 == 0) {
                    childParams.setMargins(margin, 0, 0, 0);
                } else if (i % 3 == 0 && j % 3 == 1) {
                    childParams.setMargins(0, margin, 0, 0);
                } else if (i % 3 == 2 && j % 3 == 1) {
                    childParams.setMargins(0, 0, 0, margin);
                } else if (i % 3 == 1 && j % 3 == 2) {
                    childParams.setMargins(0, 0, margin, 0);
                }
                childLayout.setGravity(Gravity.CENTER);

                int finalI = i;
                int finalJ = j;
                childLayout.setOnClickListener(view -> cellClicked(finalI, finalJ));

                TextView textView = new TextView(this);
                textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                if (currentBoardState[i][j] != 0) {
                    textView.setText(String.valueOf(currentBoardState[i][j]));
                }
                if (question[i][j] == 0) {
                    textView.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                } else {
                    textView.setTextColor(ContextCompat.getColor(this, R.color.boardText));
                }
                textView.setTextSize(25);
                textView.setGravity(Gravity.CENTER);

                childLayout.addView(textView);
                parentLayout.setBackgroundColor(Color.BLACK);
                parentLayout.addView(childLayout);
            }
            gameBoard.addView(parentLayout);
        }
    }

    private GradientDrawable createBorderDrawable(String background) {
        GradientDrawable border2 = new GradientDrawable();
        border2.setStroke(1, Color.parseColor("#888888"));
        border2.setColor(Color.parseColor(background));
        return border2;
    }

    private void cellClicked(int row, int col) {
        currSelectedCell.setPos(row, col);
        int selectedBoxRow = row / 3;
        int selectedBoxCol = col / 3;
        int childCount = gameBoard.getChildCount();
        LinearLayout rowLayout = (LinearLayout) gameBoard.getChildAt(row);
        int colCount = rowLayout.getChildCount();
        LinearLayout cellLayout = (LinearLayout) rowLayout.getChildAt(col);
        TextView textView = (TextView) cellLayout.getChildAt(0);
        for (int i = 0; i < childCount; i++) {
            LinearLayout rowLayout1 = (LinearLayout) gameBoard.getChildAt(i);
            for (int j = 0; j < colCount; j++) {
                int currBoxRow = i / 3;
                int currBoxCol = j / 3;

                LinearLayout cellLayout1 = (LinearLayout) rowLayout1.getChildAt(j);
                String txt = textView.getText().toString();
                if (!txt.isEmpty() && currentBoardState[i][j] == Integer.parseInt(txt)) {
                    cellLayout1.setBackground(createBorderDrawable("#c6cbe1"));
                } else {
                    if (selectedBoxRow == currBoxRow && selectedBoxCol == currBoxCol) {
                        cellLayout1.setBackground(createBorderDrawable("#e7eaf3"));
                    } else {
                        cellLayout1.setBackground(createBorderDrawable("#FFFFFF"));
                    }
                    if (i == row || j == col) {
                        cellLayout1.setBackground(createBorderDrawable("#e7eaf3"));
                    }
                }
            }
        }
        cellLayout.setBackground(createBorderDrawable("#c1d2fe"));
    }

    private void createNumberInputRow() {
        for (int i = 1; i <= 9; i++) {
            TextView textView = new TextView(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            textView.setLayoutParams(layoutParams);
            textView.setText(String.valueOf(i));
            textView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
            textView.setTextColor(Color.parseColor("#3f589b"));
            textView.setTextSize(35);
            int fi = i;
            textView.setOnClickListener(view -> {
                if (!globalStore.isPaused()) {
                    numberClicked(fi);
                }
            });
            numberRow.addView(textView);
        }
    }

    private void numberClicked(int num) {
        int row = currSelectedCell.row;
        int col = currSelectedCell.col;
        if (question[row][col] == 0) {
            currentBoardState[row][col] = num;
        }
        LinearLayout selectedRow = (LinearLayout) gameBoard.getChildAt(row);
        LinearLayout selectedCell = (LinearLayout) selectedRow.getChildAt(col);
        TextView textView = (TextView) selectedCell.getChildAt(0);
        textView.setText(String.valueOf(num));

        if (!HelperFunctions.isSafe(row, col, num)) {
            textView.setTextColor(ContextCompat.getColor(this, R.color.danger));
            mistakes++;
            GlobalStore.getInstance().setMistakes(mistakes);
            String mistakeString = "Mistake: " + mistakes + "/3";
            gameMistakes.setText(mistakeString);
            if (mistakes == 3) {
                gameOver();
            }
        } else {
            updateOngoingDb();
            numberCounts.put(num, numberCounts.getOrDefault(num, 0) + 1);
            if (isGameCompleted()) {
                onGameComplete();
            }
            if (numberCounts.get(num) == 9) {
                TextView tv = (TextView) numberRow.getChildAt(num - 1);
                tv.setClickable(false);
                tv.setText("");
            }
        }
    }

    private class Task extends TimerTask {
        public void run() {
            if (!globalStore.isPaused()) {
                timer++;
                globalStore.setTimer(timer);
                db.updateOngoingTimer(globalStore.getId(), timer);
                ((Activity) gameTimer.getContext()).runOnUiThread(() -> gameTimer.setText(HelperFunctions.timerToString(timer)));
            }
        }
    }

    private void eraseClicked() {
        int row = currSelectedCell.getRow();
        int col = currSelectedCell.getCol();

        if (row == -1 || col == -1) {
            return;
        }
        if (question[row][col] != 0) {
            return;
        }
        currentBoardState[row][col] = 0;
        LinearLayout selectedRow = (LinearLayout) gameBoard.getChildAt(row);
        LinearLayout selectedCell = (LinearLayout) selectedRow.getChildAt(col);
        TextView textView = (TextView) selectedCell.getChildAt(0);
        textView.setText("");
        updateOngoingDb();
    }

    private void hitCLicked() {
        ArrayList<int[]> zeroList = HelperFunctions.getEmptyCells(currentBoardState);
        if (zeroList.isEmpty()) return;
        int idx = (int) (Math.floor(Math.random() * zeroList.size()));
        int row = zeroList.get(idx)[0];
        int col = zeroList.get(idx)[1];
        currentBoardState[row][col] = answer[row][col];
        globalStore.setCurrentBoardState(currentBoardState);
        LinearLayout boardRow = (LinearLayout) gameBoard.getChildAt(row);
        LinearLayout boardCell = (LinearLayout) boardRow.getChildAt(col);
        TextView textView = (TextView) boardCell.getChildAt(0);
        textView.setText(String.valueOf(answer[row][col]));
        updateOngoingDb();
        if (isGameCompleted()) {
            onGameComplete();
        }
        numberCounts.put(currentBoardState[row][col], numberCounts.get(currentBoardState[row][col]) + 1);
        if (numberCounts.get(currentBoardState[row][col]) == 9) {
            TextView tv = (TextView) numberRow.getChildAt(currentBoardState[row][col] - 1);
            tv.setClickable(false);
            tv.setText("");
        }
    }

    private void updateOngoingDb() {
        db.updateOngoing(globalStore.getId(), globalStore.getTimer(), HelperFunctions.twoDimArrayToString(globalStore.getCurrentBoardState()), "0", globalStore.getMistakes());
    }

    private void onGameComplete() {
        gameTimerObj.cancel();
        db.makeGameComplete(globalStore.getId());
        Intent intent = new Intent(this, GameCompleteActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
    }

    private boolean isGameCompleted() {
        for (int i = 0; i < Constants.GRID_SIZE; i++) {
            for (int j = 0; j < Constants.GRID_SIZE; j++) {
                if (globalStore.getCurrentBoardState()[i][j] != globalStore.getSolution()[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    public void showPausePopup(View view) {
        isPopupOpened = true;
        LinearLayout popupBg = findViewById(R.id.ll_popup_bg);
        popupBg.setVisibility(View.VISIBLE);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View popupView = inflater.inflate(R.layout.pause_popup_window, null);

        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setAnimationStyle(R.style.popupDialogAnim);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);
        TextView timer = popupView.findViewById(R.id.tv_pause_popup_timer);
        timer.setText(HelperFunctions.timerToString(globalStore.getTimer()));
        TextView mistakes = popupView.findViewById(R.id.tv_pause_popup_mistakes);
        mistakes.setText(globalStore.getMistakes() + "/3");
        TextView difficulty = popupView.findViewById(R.id.tv_pause_popup_difficulty);
        difficulty.setText(globalStore.getDifficultyName());

        popupWindow.setOnDismissListener(() -> {
            popupBg.setVisibility(View.GONE);
            resumeGame();
        });

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        LinearLayout resumeGame = popupView.findViewById(R.id.ll_pause_resume);
        resumeGame.setOnClickListener(v -> {
            popupWindow.dismiss();
            popupBg.setVisibility(View.GONE);
            resumeGame();
        });

        TextView restartGame = popupView.findViewById(R.id.tv_pase_restart);
        restartGame.setOnClickListener(v -> {
            popupWindow.dismiss();
            popupBg.setVisibility(View.GONE);
            restartGame();
        });
    }

    private void pauseGame() {
        globalStore.setPaused(true);
        pauseResumeIcon.setImageResource(R.drawable.ic_resume);
        hideBoard();
    }

    private void resumeGame() {
        globalStore.setPaused(false);
        isPopupOpened = false;
        pauseResumeIcon.setImageResource(R.drawable.ic_pause);
        int childCount = gameBoard.getChildCount();
        for (int i = 0; i < childCount; i++) {
            LinearLayout rowLayout = (LinearLayout) gameBoard.getChildAt(i);
            int colCount = rowLayout.getChildCount();
            for (int j = 0; j < colCount; j++) {
                LinearLayout cellLayout = (LinearLayout) rowLayout.getChildAt(j);
                TextView textView = (TextView) cellLayout.getChildAt(0);
                textView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void restartGame() {
        startNewGame.restartGame();
        timer = globalStore.getTimer();
        currentBoardState = globalStore.getCurrentBoardState();
        mistakes = globalStore.getMistakes();

        String mistakeString = "Mistake: " + mistakes + "/3";
        gameMistakes.setText(mistakeString);
        gameTimer.setText(HelperFunctions.timerToString(timer));
        globalStore.setPaused(false);
        gameBoard.removeAllViews();
        generateGameBoard();
        for (int i = 0; i < 9; i++) {
            TextView tv = (TextView) numberRow.getChildAt(i);
            tv.setText(String.valueOf(i + 1));
            tv.setClickable(true);
        }
    }

    private void gameOver() {
        hideBoard();
        LinearLayout popupBg = findViewById(R.id.ll_popup_bg);
        popupBg.setVisibility(View.VISIBLE);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View popupView = inflater.inflate(R.layout.gameover_popup_window, null);

        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, false);
        popupWindow.setAnimationStyle(R.style.popupDialogAnim);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(false);
        popupWindow.showAtLocation(gameBoard, Gravity.CENTER, 0, 0);

        LinearLayout restartGame = popupView.findViewById(R.id.ll_game_over_restart);
        restartGame.setOnClickListener(v -> {
            popupWindow.dismiss();
            popupBg.setVisibility(View.GONE);
            globalStore.setPaused(true);
            restartGame();
        });
        LinearLayout goToHome = popupView.findViewById(R.id.ll_game_over_home);
        goToHome.setOnClickListener(v -> {
            globalStore.emptyCurrentState();
            finish();
        });
        db.addFailedLevel(globalStore.getDifficultyName(), String.valueOf(globalStore.getCurrentLevel()), globalStore.getType());
    }

    private void hideBoard() {
        int childCount = gameBoard.getChildCount();
        for (int i = 0; i < childCount; i++) {
            LinearLayout rowLayout = (LinearLayout) gameBoard.getChildAt(i);
            int colCount = rowLayout.getChildCount();
            for (int j = 0; j < colCount; j++) {
                LinearLayout cellLayout = (LinearLayout) rowLayout.getChildAt(j);
                TextView textView = (TextView) cellLayout.getChildAt(0);
                textView.setVisibility(View.GONE);
            }
        }
    }

    private void countNumbers() {
        for (int i = 0; i < Constants.GRID_SIZE; i++) {
            for (int j = 0; j < Constants.GRID_SIZE; j++) {
                int cellValue = currentBoardState[i][j];
                if (cellValue != 0) {
                    if (numberCounts.containsKey(cellValue)) {
                        numberCounts.put(cellValue, numberCounts.get(cellValue) + 1);
                    } else {
                        numberCounts.put(cellValue, 1);
                    }
                }
            }
        }
    }
}
