package com.sougata.sudoku.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.sougata.Constants;
import com.sougata.GlobalStore;
import com.sougata.HelperFunctions;
import com.sougata.sudoku.Database;
import com.sougata.sudoku.Pos;
import com.sougata.sudoku.R;
import com.sougata.sudoku.SoundPlayer;
import com.sougata.sudoku.StartNewGame;
import com.sougata.sudoku.Sudoku;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends AppCompatActivity {

    int difficulty, mistakes, currentLevel, timer, hintCount, prevSelectedNum = -1;
    float advanceNoteCount;
    String difficultyName;
    int[][] answer, question, currentBoardState;
    int[][][] notes;
    boolean isPopupOpened = false, isNotesEnabled = false, isGameOver = false;
    LinearLayout gameBoard, hintButton, pauseGame, numberRow, noteButton, notesStatus, advanceNote;
    TextView gameTimer, gameDifficulty, gameMistakes, currentLevelText, notesStatusText, hintsCountText, advanceNoteCountText;
    ImageView backBtn, pauseResumeIcon;
    PopupWindow popupWindow;

    GlobalStore globalStore = GlobalStore.getInstance();
    Database db;
    private final StartNewGame startNewGame = new StartNewGame(this);

    Pos currSelectedCell = new Pos(-1, -1);
    SoundPlayer soundPlayer;
    Timer gameTimerObj;
    HashMap<Integer, Integer> numberCounts = new HashMap<>();
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);

        setContentView(R.layout.activity_game);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
            }
        });

        db = new Database(this);

        globalStore.setPaused(false);
        answer = globalStore.getSolution();
        question = globalStore.getBoard();
        notes = globalStore.getNotes();
        difficulty = globalStore.getDifficulty();
        difficultyName = globalStore.getDifficultyName();
        mistakes = globalStore.getMistakes();
        currentBoardState = globalStore.getCurrentBoardState();
        timer = globalStore.getTimer();
        currentLevel = globalStore.getCurrentLevel();
        hintCount = globalStore.getHintsCount();
        advanceNoteCount = globalStore.getAdvanceNoteCount();

        soundPlayer = SoundPlayer.getInstance();

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
        noteButton = findViewById(R.id.ll_note);
        notesStatus = findViewById(R.id.ll_notes_status);
        notesStatusText = findViewById(R.id.tv_notes_status);
        advanceNote = findViewById(R.id.ll_advanced_note);
        hintsCountText = findViewById(R.id.tv_hints_count);
        advanceNoteCountText = findViewById(R.id.tv_advance_note_count);

        editor = getSharedPreferences(Constants.SHARED_PREF_NAME, MODE_PRIVATE).edit();

        countNumbers();
        generateGameBoard();
        createNumberInputRow();

        LinearLayout ll_erase = findViewById(R.id.ll_erase);
        ll_erase.setOnClickListener(view -> eraseClicked());

        Task task = new Task();
        gameTimerObj = new Timer();
        gameTimerObj.schedule(task, 1000, 1000);

        String mistakeString = "Mistake: " + mistakes + "/" + globalStore.getMistakeLimit();
        gameMistakes.setText(mistakeString);

        if (!globalStore.getType().equals(Constants.TYPES[1])) {
            String curLevelStr = "Level " + (currentLevel == 0 ? "1" : currentLevel);
            currentLevelText.setText(curLevelStr);
            gameDifficulty.setText(difficultyName);
        } else {
            Intent intent = getIntent();
            String date = HelperFunctions.padString(intent.getIntExtra("date", 0), 2) + " " + Constants.MONTHS[globalStore.getMonth()];
            gameDifficulty.setText(date);
            currentLevelText.setText(getString(R.string.daily_challenge));
        }

        advanceNote.setVisibility(globalStore.isAdvanceNoteEnable() ? View.VISIBLE : View.GONE);

        hintsCountText.setText(String.valueOf(hintCount));
        advanceNoteCountText.setText(String.valueOf((int) advanceNoteCount));

        hintButton.setOnClickListener(view -> hintClicked());
        backBtn.setOnClickListener(view -> {
            if (popupWindow != null) {
                popupWindow.dismiss();
            }
            globalStore.setPaused(true);
            finish();
        });
        pauseGame.setOnClickListener(view -> {
            showPausePopup(view);
            pauseGame();
        });
        noteButton.setOnClickListener(view -> {
            notesClicked();
        });
        advanceNote.setOnClickListener((v) -> {
            advanceNoteClicked();
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
        if (!hasFocus && !isPopupOpened && !isGameOver) {
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
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int childHeight = (screenWidth - HelperFunctions.dpToPx(30) - 12) / 9;
            int childWidth = (screenWidth - HelperFunctions.dpToPx(30) - 12) / 9;
            for (int j = 0; j < 9; j++) {
                LinearLayout.LayoutParams childParams = new LinearLayout.LayoutParams(childWidth, childHeight);
                FrameLayout cellContainerLayout = new FrameLayout(this);
                LinearLayout childLayout = new LinearLayout(this);
                cellContainerLayout.setLayoutParams(childParams);
                cellContainerLayout.setBackground(createBorderDrawable("#FFFFFF"));
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
                childLayout.setLayoutParams(childParams);
                childLayout.setGravity(Gravity.CENTER);

                int finalI = i;
                int finalJ = j;
                cellContainerLayout.setOnClickListener(view -> cellClicked(finalI, finalJ));

                TextView textView = new TextView(this);
                textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                LinearLayout parentHint = new LinearLayout(this);
                parentHint.setLayoutParams(childParams);
                parentHint.setPadding(4, 4, 4, 4);
                parentHint.setOrientation(LinearLayout.VERTICAL);
                if (currentBoardState[i][j] != 0) {
                    textView.setText(String.valueOf(currentBoardState[i][j]));
                } else {
                    for (int k = 0; k < Constants.BOX_SIZE; k++) {
                        LinearLayout row = new LinearLayout(this);
                        row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
                        row.setOrientation(LinearLayout.HORIZONTAL);
                        for (int l = 0; l < Constants.BOX_SIZE; l++) {
                            int idx = k * Constants.BOX_SIZE + l;
                            TextView num = new TextView(this);
                            if (notes[i][j][idx] != 0) {
                                num.setText(String.valueOf(idx + 1));
                            } else {
                                num.setText("");
                            }
                            num.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                            num.setGravity(Gravity.CENTER);
                            num.setTextColor(ContextCompat.getColor(this, R.color.gray));
                            num.setTextSize(9.5f);
                            row.addView(num);
                        }
                        parentHint.addView(row);
                    }
                }
                if (question[i][j] == 0) {
                    textView.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                } else {
                    textView.setTextColor(ContextCompat.getColor(this, R.color.boardText));
                }
                textView.setTextSize(25);
                textView.setGravity(Gravity.CENTER);

                Animation animation = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
                animation.setStartOffset(((i + j) * 100) + 100);
                textView.startAnimation(animation);

                Animation animation2 = AnimationUtils.loadAnimation(this, R.anim.zoom_out_two);
                animation2.setStartOffset(1500);
                parentHint.startAnimation(animation2);

                childLayout.addView(textView);
                cellContainerLayout.addView(childLayout);
                cellContainerLayout.addView(parentHint);
                parentLayout.addView(cellContainerLayout);
            }
            parentLayout.setBackgroundColor(Color.BLACK);
            gameBoard.addView(parentLayout);
        }
    }

    private GradientDrawable createBorderDrawable(String background) {
        GradientDrawable border2 = new GradientDrawable();
        border2.setStroke(1, ContextCompat.getColor(this, R.color.box_border_color));
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
        FrameLayout cellContainerLayout = (FrameLayout) rowLayout.getChildAt(col);
        LinearLayout cellLayout = (LinearLayout) cellContainerLayout.getChildAt(0);
        TextView textView = (TextView) cellLayout.getChildAt(0);
        for (int i = 0; i < childCount; i++) {
            LinearLayout rowLayout1 = (LinearLayout) gameBoard.getChildAt(i);
            for (int j = 0; j < colCount; j++) {
                int currBoxRow = i / 3;
                int currBoxCol = j / 3;

                FrameLayout cellContainerLayout1 = (FrameLayout) rowLayout1.getChildAt(j);
                LinearLayout cellLayout1 = (LinearLayout) cellContainerLayout1.getChildAt(0);
                String txt = textView.getText().toString();
                if (globalStore.isHighLightNotes()) {
                    LinearLayout parentHint = (LinearLayout) cellContainerLayout1.getChildAt(1);
                    if (question[i][j] == 0 && prevSelectedNum != -1) {
                        int rowIdx = (prevSelectedNum - 1) / 3;
                        int colIdx = (prevSelectedNum - 1) % 3;
                        LinearLayout r = (LinearLayout) parentHint.getChildAt(rowIdx);
                        if (r != null) {
                            TextView tv = (TextView) r.getChildAt(colIdx);
                            tv.setTextColor(ContextCompat.getColor(this, R.color.gray));
                            tv.setBackground(null);
                        }
                    }
                    if (!txt.isEmpty() && question[i][j] == 0) {
                        int idx = Integer.parseInt(txt) - 1;
                        LinearLayout hintRow = (LinearLayout) parentHint.getChildAt(idx / 3);
                        if (hintRow != null) {
                            TextView hintCell = (TextView) hintRow.getChildAt(idx % 3);
                            if (hintCell.getText().toString().equals(txt)) {
                                hintCell.setTextColor(ContextCompat.getColor(this, R.color.white));
                                hintCell.setBackgroundResource(R.drawable.btn_bg_solid);
                            }
                        }
                    }
                }
                if (!txt.isEmpty() && currentBoardState[i][j] == Integer.parseInt(txt)) {
                    if (globalStore.getNumbersHighlight()) {
                        cellLayout1.setBackground(createBorderDrawable("#C6CBE1"));
                    } else {
                        cellLayout1.setBackground(createBorderDrawable("#FFFFFF"));
                    }
                } else {
                    if (globalStore.getRegionHighlight()) {
                        if (selectedBoxRow == currBoxRow && selectedBoxCol == currBoxCol) {
                            cellLayout1.setBackground(createBorderDrawable("#E7EAF3"));
                        } else {
                            cellLayout1.setBackground(createBorderDrawable("#FFFFFF"));
                        }
                        if (i == row || j == col) {
                            cellLayout1.setBackground(createBorderDrawable("#E7EAF3"));
                        }
                    } else {
                        cellLayout1.setBackground(createBorderDrawable("#FFFFFF"));
                    }
                }
            }
        }
        prevSelectedNum = currentBoardState[row][col] != 0 ? currentBoardState[row][col] : -1;
        updateNumberRowUI();
        cellLayout.setBackground(createBorderDrawable("#c1d2fe"));
    }

    private void createNumberInputRow() {
        for (int i = 1; i <= 9; i++) {
            TextView textView = new TextView(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            layoutParams.setMargins(5, 0, 5, 0);
            linearLayout.setPadding(10, 10, 10, 10);
            linearLayout.setLayoutParams(layoutParams);
            linearLayout.setBackgroundResource(R.drawable.number_bg);
            linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setText(String.valueOf(i));
            textView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
            textView.setTextColor(ContextCompat.getColor(this, R.color.game_number_row_text));
            textView.setTextSize(35);
            textView.setClickable(false);
            TextView textView1 = new TextView(this);
            textView1.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            textView1.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
            textView1.setTextColor(ContextCompat.getColor(this, R.color.game_number_row_text));
            textView1.setClickable(false);
            int fi = i;
            linearLayout.setOnClickListener(view -> {
                if (!globalStore.isPaused()) {
                    numberClicked(fi);
                }
            });
            textView1.setText(String.valueOf(9 - numberCounts.get(i)));
            if (numberCounts.containsKey(i) && numberCounts.get(i) == 9) {
                textView.setText("");
                linearLayout.setClickable(false);
                textView1.setText("");
                linearLayout.setBackground(null);
            }
            linearLayout.addView(textView);
            linearLayout.addView(textView1);
            numberRow.addView(linearLayout);
        }
    }

    private void numberClicked(int num) {
        int row = currSelectedCell.row;
        int col = currSelectedCell.col;
        if (isNotesEnabled && row != -1 && col != -1 && currentBoardState[row][col] == 0) {
            LinearLayout selectedRow = (LinearLayout) gameBoard.getChildAt(row);
            FrameLayout selectedCellContainer = (FrameLayout) selectedRow.getChildAt(col);
            LinearLayout selectedCell = (LinearLayout) selectedCellContainer.getChildAt(1);
            LinearLayout cellRow = (LinearLayout) selectedCell.getChildAt((num - 1) / 3);
            TextView textView = (TextView) cellRow.getChildAt((num - 1) % 3);
            if (textView.getText().toString().isEmpty() && Sudoku.isSafe(currentBoardState, row, col, num)) {
                textView.setText(String.valueOf(num));
                notes[row][col][num - 1] = 1;
                soundPlayer.playNotePlaced(this);
            } else {
                notes[row][col][num - 1] = 0;
                textView.setText("");
            }
            updateOngoingDb();
            updateNumberRowUI();
            return;
        }
        if (row == -1 || col == -1 || question[row][col] != 0 || currentBoardState[row][col] == num)
            return;
        if (question[row][col] == 0) {
            currentBoardState[row][col] = num;
        }
        LinearLayout selectedRow = (LinearLayout) gameBoard.getChildAt(row);
        FrameLayout selectedCellContainer = (FrameLayout) selectedRow.getChildAt(col);
        LinearLayout selectedCell = (LinearLayout) selectedCellContainer.getChildAt(0);
        TextView textView = (TextView) selectedCell.getChildAt(0);
        textView.setText(String.valueOf(num));

        LinearLayout selectedNotesLayout = (LinearLayout) selectedCellContainer.getChildAt(1);
        for (int i = 0; i < selectedNotesLayout.getChildCount(); i++) {
            LinearLayout r = (LinearLayout) selectedNotesLayout.getChildAt(i);
            for (int j = 0; j < r.getChildCount(); j++) {
                TextView tv = (TextView) r.getChildAt(j);
                tv.setText("");
            }
        }

        if (!HelperFunctions.isSafe(row, col, num)) {
            textView.setTextColor(ContextCompat.getColor(this, R.color.danger));
            mistakes++;
            soundPlayer.playInvalid(this);
            globalStore.setMistakes(mistakes);
            String mistakeString = "Mistake: " + mistakes + "/" + globalStore.getMistakeLimit();
            gameMistakes.setText(mistakeString);
            if (mistakes == globalStore.getMistakeLimit()) {
                isGameOver = true;
                gameOver();
            }
        } else {
            updateOngoingDb();
            soundPlayer.playCorrect(this);
            if (globalStore.getAutoRemoveNotes()) {
                removeNotes(num);
            }
            cellClicked(row, col);
            textView.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
            numberCounts.put(num, numberCounts.getOrDefault(num, 0) + 1);
            if (isGameCompleted()) {
                onGameComplete();
            }
            LinearLayout ll = (LinearLayout) numberRow.getChildAt(num - 1);
            if (numberCounts.get(num) == 9) {
                TextView tv = (TextView) ll.getChildAt(0);
                TextView tv1 = (TextView) ll.getChildAt(1);
                ll.setClickable(false);
                tv.setText("");
                tv1.setText("");
                ll.setBackground(null);
            } else {
                TextView tv = (TextView) ll.getChildAt(1);
                tv.setText(String.valueOf(9 - numberCounts.get(num)));
            }
        }
    }

    private class Task extends TimerTask {
        public void run() {
            if (!globalStore.isPaused()) {
                timer++;
                globalStore.setTimer(timer);
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
        if (currentBoardState[row][col] == 0) {
            return;
        }

        soundPlayer.playErase(this);
        if (currentBoardState[row][col] == answer[row][col]) {
            numberCounts.put(currentBoardState[row][col], numberCounts.get(currentBoardState[row][col]) - 1);
        }
        if (numberCounts.get(currentBoardState[row][col]) < 9) {
            LinearLayout ll = (LinearLayout) numberRow.getChildAt(currentBoardState[row][col] - 1);
            TextView tv = (TextView) ll.getChildAt(0);
            TextView tv1 = (TextView) ll.getChildAt(1);
            ll.setClickable(true);
            tv.setText(String.valueOf(currentBoardState[row][col]));
            tv1.setText(String.valueOf(9 - numberCounts.get(currentBoardState[row][col])));
        }
        currentBoardState[row][col] = 0;
        LinearLayout selectedRow = (LinearLayout) gameBoard.getChildAt(row);
        FrameLayout selectedCellContainer = (FrameLayout) selectedRow.getChildAt(col);
        LinearLayout selectedCell = (LinearLayout) selectedCellContainer.getChildAt(0);
        TextView textView = (TextView) selectedCell.getChildAt(0);
        textView.setText("");
        LinearLayout selectedNotesLayout = (LinearLayout) selectedCellContainer.getChildAt(1);
        for (int i = 0; i < selectedNotesLayout.getChildCount(); i++) {
            LinearLayout r = (LinearLayout) selectedNotesLayout.getChildAt(i);
            for (int j = 0; j < r.getChildCount(); j++) {
                TextView tv = (TextView) r.getChildAt(j);
                tv.setText("");
                notes[row][col][i * Constants.BOX_SIZE + j] = 0;
            }
        }
        updateNumberRowUI();
        updateOngoingDb();
    }

    private void hintClicked() {
        if (hintCount <= 0) {
            Toast.makeText(this, ContextCompat.getString(this, R.string.no_hint_toast_message), Toast.LENGTH_SHORT).show();
            return;
        }
        hintCount--;
        globalStore.setHintsCount(hintCount);
        editor.putInt("hintsCount", hintCount);
        editor.apply();
        hintsCountText.setText(String.valueOf(hintCount));

        globalStore.setHints(globalStore.getHints() + 1);
        ArrayList<int[]> zeroList = HelperFunctions.getEmptyCells(currentBoardState);
        if (zeroList.isEmpty()) return;
        int idx = (int) (Math.floor(Math.random() * zeroList.size()));
        int row = zeroList.get(idx)[0];
        int col = zeroList.get(idx)[1];
        currentBoardState[row][col] = answer[row][col];
        globalStore.setCurrentBoardState(currentBoardState);
        LinearLayout boardRow = (LinearLayout) gameBoard.getChildAt(row);
        FrameLayout boardCellContainer = (FrameLayout) boardRow.getChildAt(col);
        LinearLayout boardCell = (LinearLayout) boardCellContainer.getChildAt(0);
        TextView textView = (TextView) boardCell.getChildAt(0);
        textView.setText(String.valueOf(answer[row][col]));
        LinearLayout selectedNotesLayout = (LinearLayout) boardCellContainer.getChildAt(1);
        for (int i = 0; i < selectedNotesLayout.getChildCount(); i++) {
            LinearLayout r = (LinearLayout) selectedNotesLayout.getChildAt(i);
            for (int j = 0; j < r.getChildCount(); j++) {
                TextView tv = (TextView) r.getChildAt(j);
                tv.setText("");
                notes[row][col][i * Constants.BOX_SIZE + j] = 0;
            }
        }
        int tempR = currSelectedCell.row;
        int tempC = currSelectedCell.col;
        currSelectedCell.setPos(row, col);
        if (globalStore.getAutoRemoveNotes()) {
            removeNotes(answer[row][col]);
        }
        currSelectedCell.setPos(tempR, tempC);
        updateOngoingDb();
        if (isGameCompleted()) {
            onGameComplete();
        }
        try {
            numberCounts.put(currentBoardState[row][col], numberCounts.get(currentBoardState[row][col]) + 1);
            LinearLayout ll = (LinearLayout) numberRow.getChildAt(currentBoardState[row][col] - 1);
            TextView tv1 = (TextView) ll.getChildAt(1);
            if (numberCounts.get(currentBoardState[row][col]) == 9) {
                TextView tv = (TextView) ll.getChildAt(0);
                ll.setClickable(false);
                tv.setText("");
                tv1.setText("");
                ll.setBackground(null);
            } else {
                tv1.setText(String.valueOf(9 - numberCounts.get(currentBoardState[row][col])));
            }
        } catch (NullPointerException ignored) {
        }
    }

    private void updateOngoingDb() {
        db.updateOngoing(globalStore.getId(), globalStore.getTimer(), HelperFunctions.twoDimArrayToString(globalStore.getCurrentBoardState()), globalStore.getHints(), globalStore.getMistakes(), HelperFunctions.threeDimArrayToString(notes));
    }

    private void onGameComplete() {
        try {
            gameTimerObj.cancel();
            db.makeGameComplete(globalStore.getId());

            if (globalStore.getType().equals(Constants.TYPES[2])) {
                JSONObject json = globalStore.getEventDetails();
                String id = json.getString("id");
                String eventName = json.getString("name");
                JSONArray lvl = json.getJSONArray("medal_levels");
                JSONArray mdl = json.getJSONArray("medal_names");
                int[] levels = new int[lvl.length()];
                String[] medals = new String[mdl.length()];
                for (int i = 0; i < levels.length; i++) {
                    levels[i] = lvl.getInt(i);
                    medals[i] = mdl.getString(i);
                }
                int pos = isContain(globalStore.getCurrentLevel(), levels);
                if (pos != -1) {
                    Cursor c = db.getEventMedal(id);
                    String medalUrl = json.getJSONObject("assets").getString(medals[pos]);
                    String medalName = HelperFunctions.getMadelName(medals[pos]);
                    if (c.getCount() > 0) {
                        c.moveToFirst();
                        db.updateMedal(c.getLong(0), medalName, medalUrl);
                    } else {
                        db.addMedal(id, eventName, medalName, medalUrl);
                    }
                }
            }

            Intent intent = new Intent(this, GameCompleteActivity.class);
            startActivity(intent);
            soundPlayer.playGameComplete(this);
            editor.putInt("hintsCount", hintCount + Constants.HINTS_PER_LEVEL);
            editor.putFloat("advanceNoteCount", advanceNoteCount + Constants.ADVANCE_NOTE_PER_LEVEL);
            editor.apply();
            finish();
            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
        } catch (Exception ignore) {
            finish();
        }
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
        db.updateOngoingTimer(globalStore.getId(), timer);
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
        String mistakeLimit = globalStore.getMistakes() + "/" + globalStore.getMistakeLimit();
        mistakes.setText(mistakeLimit);
        TextView difficulty = popupView.findViewById(R.id.tv_pause_popup_difficulty);
        difficulty.setText(globalStore.getDifficultyName());
        ImageView tipsIcon = popupView.findViewById(R.id.iv_pause_popup_icon);
        TextView tipsTitle = popupView.findViewById(R.id.tv_pause_popup_title);
        TextView tipsDescription = popupView.findViewById(R.id.tv_pause_popup_description);
        String[] title = getResources().getStringArray(R.array.pause_popup_titles);
        String[] description = getResources().getStringArray(R.array.pause_popup_description);
        TypedArray icons = getResources().obtainTypedArray(R.array.pause_popup_icon);
        int[] iconIds = new int[icons.length()];
        for (int i = 0; i < icons.length(); i++) {
            iconIds[i] = icons.getResourceId(i, -1);
        }
        icons.recycle();

        int random = (int) (Math.floor(Math.random() * iconIds.length));
        tipsIcon.setImageResource(iconIds[random]);
        tipsTitle.setText(title[random]);
        tipsDescription.setText(description[random]);

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
                FrameLayout containerLayout = (FrameLayout) rowLayout.getChildAt(j);
                LinearLayout cellLayout = (LinearLayout) containerLayout.getChildAt(0);
                cellLayout.getChildAt(0).setVisibility(View.VISIBLE);
                containerLayout.getChildAt(1).setVisibility(View.VISIBLE);
            }
        }
    }

    private void restartGame() {
        startNewGame.restartGame();
        timer = globalStore.getTimer();
        currentBoardState = globalStore.getCurrentBoardState();
        mistakes = globalStore.getMistakes();
        notes = globalStore.getNotes();

        String mistakeString = "Mistake: " + mistakes + "/" + globalStore.getMistakeLimit();
        gameMistakes.setText(mistakeString);
        gameTimer.setText(HelperFunctions.timerToString(timer));
        globalStore.setPaused(false);
        gameBoard.removeAllViews();
        generateGameBoard();
        countNumbers();
        for (int i = 0; i < 9; i++) {
            if (numberCounts.get(i + 1) < 9) {
                LinearLayout ll = (LinearLayout) numberRow.getChildAt(i);
                TextView tv = (TextView) ll.getChildAt(0);
                TextView tv1 = (TextView) ll.getChildAt(1);
                tv.setText(String.valueOf(i + 1));
                ll.setClickable(true);
                tv1.setText((9 - numberCounts.get(i + 1)) + "");
                ll.setBackgroundResource(R.drawable.number_bg);
            }
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

        TextView gameOverMessage = popupView.findViewById(R.id.tv_game_over_message);
        String msg = "You lost the game because you made" + globalStore.getMistakeLimit() + "mistakes";
        gameOverMessage.setText(msg);
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
                FrameLayout containerLayout = (FrameLayout) rowLayout.getChildAt(j);
                LinearLayout cellLayout = (LinearLayout) containerLayout.getChildAt(0);
                cellLayout.getChildAt(0).setVisibility(View.GONE);
                containerLayout.getChildAt(1).setVisibility(View.GONE);
            }
        }
    }

    private void countNumbers() {
        for (int i = 1; i <= 9; i++) {
            numberCounts.put(i, 0);
        }
        for (int i = 0; i < Constants.GRID_SIZE; i++) {
            for (int j = 0; j < Constants.GRID_SIZE; j++) {
                int cellValue = currentBoardState[i][j];
                if (cellValue != 0) {
                    numberCounts.put(cellValue, numberCounts.get(cellValue) + 1);
                }
            }
        }
    }

    private void notesClicked() {
        soundPlayer.playNoteSwitch(this);
        ImageView enableEditing = findViewById(R.id.iv_edit_enabled);
        if (isNotesEnabled) {
            notesStatus.setBackgroundResource(R.drawable.btn_bg_deactivate);
            notesStatusText.setText(R.string.off);
            for (int i = 0; i < numberRow.getChildCount(); i++) {
                if (numberCounts.get(i + 1) < 9) {
                    LinearLayout ll = (LinearLayout) numberRow.getChildAt(i);
                    ll.setBackgroundResource(R.drawable.number_bg);
                    TextView tv = (TextView) ll.getChildAt(0);
                    TextView tv1 = (TextView) ll.getChildAt(1);
                    tv1.setTextColor(ContextCompat.getColor(this, R.color.game_number_row_text));
                    tv.setTextColor(ContextCompat.getColor(this, R.color.game_number_row_text));
                }
            }
            enableEditing.setVisibility(View.GONE);
        } else {
            notesStatus.setBackgroundResource(R.drawable.btn_bg_solid);
            notesStatusText.setText(R.string.on);
            for (int i = 0; i < numberRow.getChildCount(); i++) {
                if (numberCounts.get(i + 1) < 9) {
                    LinearLayout ll = (LinearLayout) numberRow.getChildAt(i);
                    TextView tv = (TextView) ll.getChildAt(0);
                    TextView tv1 = (TextView) ll.getChildAt(1);
                    tv.setTextColor(ContextCompat.getColor(this, com.google.android.material.R.color.material_dynamic_neutral70));
                    tv1.setTextColor(ContextCompat.getColor(this, com.google.android.material.R.color.material_dynamic_neutral70));
                    ll.setBackgroundResource(R.drawable.number_row_btn_bg);
                }
            }
            enableEditing.setVisibility(View.VISIBLE);
        }
        isNotesEnabled = !isNotesEnabled;
        updateNumberRowUI();
    }

    private void updateNumberRowUI() {
        int r = currSelectedCell.row;
        int c = currSelectedCell.col;
        if (r == -1 || c == -1) return;

        int[] numbs = notes[r][c];
        for (int i = 0; i < numbs.length; i++) {
            LinearLayout ll = (LinearLayout) numberRow.getChildAt(i);
            TextView t = (TextView) ll.getChildAt(0);
            TextView t1 = (TextView) ll.getChildAt(1);
            boolean isEmpty = numberCounts.get(i + 1) < 9;

            if (isNotesEnabled) {
                if (numbs[i] == 1) {
                    t.setTextColor(ContextCompat.getColor(this, com.google.android.material.R.color.material_dynamic_neutral50));
                    t1.setTextColor(ContextCompat.getColor(this, com.google.android.material.R.color.material_dynamic_neutral50));
                    if (isEmpty) {
                        ll.setBackgroundResource(R.drawable.number_row_btn_bg_selected);
                    } else {
                        ll.setBackground(null);
                    }
                } else {
                    t.setTextColor(ContextCompat.getColor(this, com.google.android.material.R.color.material_dynamic_neutral70));
                    t1.setTextColor(ContextCompat.getColor(this, com.google.android.material.R.color.material_dynamic_neutral70));
                    if (isEmpty) {
                        ll.setBackgroundResource(R.drawable.number_row_btn_bg);
                    } else {
                        ll.setBackground(null);
                    }
                }
            } else {
                t.setTextColor(ContextCompat.getColor(this, R.color.game_number_row_text));
                t1.setTextColor(ContextCompat.getColor(this, R.color.game_number_row_text));
                if (isEmpty) {
                    ll.setBackgroundResource(R.drawable.number_bg);
                } else {
                    ll.setBackground(null);
                }
            }
        }
    }

    private void removeNotes(int num) {
        int r = currSelectedCell.row;
        int c = currSelectedCell.col;
        if (r == -1 || c == -1) return;
        int boxRow = r / 3;
        int boxCol = c / 3;
        int numRow = (num - 1) / 3;
        int numCol = (num - 1) % 3;


        LinearLayout boardRow = (LinearLayout) gameBoard.getChildAt(r);
        for (int i = 0; i < Constants.GRID_SIZE; i++) {
            FrameLayout container = (FrameLayout) boardRow.getChildAt(i);
            LinearLayout notesLayout = (LinearLayout) container.getChildAt(1);
            if (notesLayout.getChildCount() > 0) {
                LinearLayout notesRow = (LinearLayout) notesLayout.getChildAt(numRow);
                TextView textView = (TextView) notesRow.getChildAt(numCol);
                textView.setText("");
                notes[r][i][num - 1] = 0;
            }
        }
        for (int i = 0; i < Constants.GRID_SIZE; i++) {
            LinearLayout bRow = (LinearLayout) gameBoard.getChildAt(i);
            FrameLayout container = (FrameLayout) bRow.getChildAt(c);
            LinearLayout notesLayout = (LinearLayout) container.getChildAt(1);
            if (notesLayout.getChildCount() > 0) {
                LinearLayout notesRow = (LinearLayout) notesLayout.getChildAt(numRow);
                TextView textView = (TextView) notesRow.getChildAt(numCol);
                textView.setText("");
                notes[i][c][num - 1] = 0;
            }
        }
        for (int i = boxRow * 3; i < boxRow * 3 + 3; i++) {
            LinearLayout bRow = (LinearLayout) gameBoard.getChildAt(i);
            for (int j = boxCol * 3; j < boxCol * 3 + 3; j++) {
                FrameLayout container = (FrameLayout) bRow.getChildAt(j);
                LinearLayout notesLayout = (LinearLayout) container.getChildAt(1);
                if (notesLayout.getChildCount() > 0) {
                    LinearLayout notesRow = (LinearLayout) notesLayout.getChildAt(numRow);
                    TextView textView = (TextView) notesRow.getChildAt(numCol);
                    textView.setText("");
                    notes[i][j][num - 1] = 0;
                }
            }
        }
        updateOngoingDb();
    }

    private void advanceNoteClicked() {
        if (advanceNoteCount < 1) {
            Toast.makeText(this, ContextCompat.getString(this, R.string.no_advance_note_toast_message), Toast.LENGTH_SHORT).show();
            return;
        }
        advanceNoteCount -= 1;
        globalStore.setAdvanceNoteCount(advanceNoteCount);
        editor.putFloat("advanceNoteCount", advanceNoteCount);
        editor.apply();
        advanceNoteCountText.setText(String.valueOf((int) advanceNoteCount));

        notes = HelperFunctions.generateAdvanceNote(currentBoardState);
        globalStore.setNotes(notes);
        for (int i = 0; i < currentBoardState.length; i++) {
            LinearLayout rowLayout = (LinearLayout) gameBoard.getChildAt(i);
            for (int j = 0; j < currentBoardState[0].length; j++) {
                if (currentBoardState[i][j] != 0) continue;
                FrameLayout containerLayout = (FrameLayout) rowLayout.getChildAt(j);
                LinearLayout notesLayout = (LinearLayout) containerLayout.getChildAt(1);
                for (int k = 0; k < 9; k++) {
                    int nR = k / 3;
                    int nC = k % 3;
                    LinearLayout notesRow = (LinearLayout) notesLayout.getChildAt(nR);
                    TextView textView = (TextView) notesRow.getChildAt(nC);
                    if (notes[i][j][k] == 1) {
                        textView.setText(String.valueOf(k + 1));
                    } else {
                        textView.setText("");
                    }
                }
            }
        }
        updateOngoingDb();
    }

    private int isContain(int value, int[] array) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) return i;
        }
        return -1;
    }
}
