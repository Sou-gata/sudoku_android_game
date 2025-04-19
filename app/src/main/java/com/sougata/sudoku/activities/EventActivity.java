package com.sougata.sudoku.activities;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.sougata.Constants;
import com.sougata.GlobalStore;
import com.sougata.HelperFunctions;
import com.sougata.sudoku.Database;
import com.sougata.sudoku.Pos;
import com.sougata.sudoku.R;
import com.sougata.sudoku.StartNewGame;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class EventActivity extends AppCompatActivity {

    ImageView eventBg, backButton;
    LinearLayout eventPlayContainer;
    ScrollView svEvent;
    FrameLayout levelContainer;
    TextView eventTitle, eventTimer;
    Button playButton;
    GlobalStore globalStore = GlobalStore.getInstance();
    String id;
    int completedLevel = 0, maxLevel = 0;
    Timer eventTimerObj;
    Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);

        setContentView(R.layout.activity_event);

        db = new Database(this);

        backButton = findViewById(R.id.iv_awards_back_button);
        eventBg = findViewById(R.id.iv_event_bg);
        eventPlayContainer = findViewById(R.id.ll_event_play_container);
        svEvent = findViewById(R.id.sv_event);
        levelContainer = findViewById(R.id.fl_lvl_container);
        eventTitle = findViewById(R.id.tv_event_title);
        eventTimer = findViewById(R.id.tv_event_timer);
        playButton = findViewById(R.id.btn_event_play);

        backButton.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        int screenWidth = HelperFunctions.getScreenWidth(this);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) eventBg.getLayoutParams();
        params.width = screenWidth;
        params.height = (int) (screenWidth * 8.032);

        int w = 1125, h = 9036;
        float wr = (float) screenWidth / (float) w;
        float hr = (screenWidth * 8.032f) / (float) h;

        int scrollY = loadJson(wr, hr);
        svEvent.post(() -> svEvent.smoothScrollTo(0, (int) ((scrollY * 0.96) - ((float) HelperFunctions.getScreenHeight(this) / 2.0) - HelperFunctions.dpToPx(65))));
        String btnText;
        if (completedLevel == maxLevel) {
            btnText = "COMPLETED";
        } else {
            btnText = "LEVEL " + (completedLevel + 1);
        }
        playButton.setText(btnText);
        if (completedLevel < maxLevel) {
            playButton.setOnClickListener(v -> levelClicked(completedLevel));
        }
    }

    private void createLevels(ArrayList<Pos> poses, String currentColor, String lockColor) {
        int outer = HelperFunctions.dpToPx(50);
        int inner = HelperFunctions.dpToPx(40);
        for (int i = 0; i < poses.size(); i++) {
            LinearLayout ll = new LinearLayout(this);
            Pos pos = poses.get(i);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(outer, outer);
            params.setMargins(pos.row, pos.col, 0, 0);
            ll.setLayoutParams(params);
            ll.setGravity(Gravity.CENTER);
            ll.setBackgroundResource(R.drawable.event_level_bg_white);
            if (i <= completedLevel) {
                LinearLayout ll2 = new LinearLayout(this);
                LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(inner, inner);
                ll2.setLayoutParams(params2);
                ll2.setGravity(Gravity.CENTER);
                GradientDrawable d = (GradientDrawable) AppCompatResources.getDrawable(this, R.drawable.event_level_color_bg);
                if (d != null) {
                    d.setColor(Color.parseColor(currentColor));
                    ll2.setBackground(d);
                }
                TextView tv = new TextView(this);
                tv.setText(String.valueOf(i + 1));
                tv.setTextColor(ContextCompat.getColor(this, R.color.fill_btn_text));
                tv.setTextSize(20);
                ll2.addView(tv);
                ll.addView(ll2);
                int finalI = i;
                ll.setOnClickListener((v) -> levelClicked(finalI));
            } else {
                ImageView iv = new ImageView(this);
                iv.setImageResource(R.drawable.ic_lock);
                iv.setColorFilter(Color.parseColor(lockColor));
                int lockDim = HelperFunctions.dpToPx(25);
                iv.setLayoutParams(new FrameLayout.LayoutParams(lockDim, lockDim));
                ll.addView(iv);
            }
            levelContainer.addView(ll);
        }
    }

    private int loadJson(float wr, float hr) {
        try {
            JSONObject json = globalStore.getEventDetails();
            if (json == null) {
                finish();
                return 0;
            }
            id = json.getString("id");
            completedLevel = db.getEventCompletedLevel(id);
            JSONObject assets = json.getJSONObject("assets");
            String name = json.getString("name");
            JSONArray positions = json.getJSONArray("level_origin");
            JSONObject colors = json.getJSONObject("colors");
            String currentColor = colors.getString("current_color");
            String lockColor = colors.getString("lock_color");

            Glide.with(this).load(assets.getString("activity_background")).signature(new ObjectKey(json.getString("id"))).into(eventBg);
            ArrayList<Pos> poses = new ArrayList<>();
            maxLevel = positions.length();
            for (int i = 0; i < maxLevel; i++) {
                JSONObject obj = positions.getJSONObject(i);
                int x = (int) (obj.getInt("x") * wr);
                int y = (int) (obj.getInt("y") * hr);
                poses.add(new Pos(x, y));
            }
            Task task = new Task();
            eventTimerObj = new Timer();
            eventTimerObj.schedule(task, 1000, 1000);

            calculateTime();
            GradientDrawable drawable = (GradientDrawable) playButton.getBackground();
            drawable.setStroke(HelperFunctions.dpToPx(1), Color.parseColor(colors.getString("banner_text_color")));
            playButton.setTextColor(Color.parseColor(colors.getString("banner_text_color")));
            eventPlayContainer.setBackgroundColor(Color.parseColor(colors.getString("banner_bg_color")));
            runOnUiThread(() -> {
                createLevels(poses, currentColor, lockColor);
                eventTitle.setText(name);
            });
            int scrollY;
            if (completedLevel < maxLevel) {
                scrollY = positions.getJSONObject(completedLevel).getInt("y");
            } else {
                scrollY = positions.getJSONObject(maxLevel - 1).getInt("y");
            }
            return scrollY;
        } catch (Exception ignored) {
            finish();
            return 0;
        }
    }

    private class Task extends TimerTask {
        @Override
        public void run() {
            calculateTime();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (eventTimerObj != null) {
            eventTimerObj.cancel();
        }
    }

    private void calculateTime() {
        JSONObject json = globalStore.getEventDetails();
        try {
            Calendar start = Calendar.getInstance();
            start.setTimeInMillis(json.getLong("start_date"));
            Calendar end = Calendar.getInstance();
            end.setTimeInMillis(json.getLong("end_date"));
            Calendar today = Calendar.getInstance();

            long diffMillis = 0;
            if (today.before(start)) {
                diffMillis = start.getTimeInMillis() - today.getTimeInMillis();
            } else if (today.after(start) && today.before(end)) {
                diffMillis = end.getTimeInMillis() - today.getTimeInMillis();
            }
            if (diffMillis <= 0) {
                finish();
                return;
            }
            long diffSeconds = diffMillis / 1000;
            long diffMinutes = diffSeconds / 60;
            long diffHours = diffMinutes / 60;

            long diffDays = diffHours / 24;

            long remainingHours = diffHours % 24;
            long remainingMinutes = diffMinutes % 60;
            String time = (diffDays + "d " + remainingHours + "h " + remainingMinutes + "m");
            runOnUiThread(() -> eventTimer.setText(time));
        } catch (Exception ignored) {
        }
    }

    private void levelClicked(int index) {
        StartNewGame newGame = new StartNewGame(this);
        globalStore.setCurrentLevel(index + 1);
        globalStore.setType(Constants.TYPES[2]);
        Cursor c = db.getEventDetails(index + 1, id);
        Intent intent = new Intent(this, GameActivity.class);
        if (c.getCount() > 0) {
            c.moveToFirst();
            int isCompleted = c.getInt(11);
            globalStore.setId(c.getLong(0));
            globalStore.setCurrentBoardState(HelperFunctions.parseTwoDimArray(c.getString(6)));

            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
            View v = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_daily, null);
            bottomSheetDialog.setContentView(v);
            bottomSheetDialog.show();
            TextView cancel = v.findViewById(R.id.tv_bs_cancel);
            TextView continueTxt = v.findViewById(R.id.tv_bs_continue);
            TextView restart = v.findViewById(R.id.tv_bs_restart);

            restart.setOnClickListener(view -> {
                db.restartGame(globalStore.getId(), c.getString(6));
                globalStore.setTimer(0);
                globalStore.setMistakes(0);
                globalStore.setBoard(HelperFunctions.parseTwoDimArray(c.getString(6)));
                globalStore.setSolution(HelperFunctions.parseTwoDimArray(c.getString(7)));
                globalStore.setDifficultyName(c.getString(3));
                globalStore.setDifficulty(c.getInt(2));
                globalStore.setCurrentLevel(c.getInt(1));
                globalStore.setType(Constants.TYPES[2]);
                globalStore.setNotes(new int[9][9][9]);
                bottomSheetDialog.cancel();
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            });
            if (isCompleted == 0) {
                continueTxt.setVisibility(View.VISIBLE);
                continueTxt.setOnClickListener(view -> {
                    newGame.createEventGame(index + 1, id);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    bottomSheetDialog.cancel();
                });
            } else {
                continueTxt.setVisibility(View.GONE);
            }
            cancel.setOnClickListener(view -> bottomSheetDialog.cancel());
        } else {
            newGame.createEventGame(index + 1, id);
            globalStore.setCurrentLevel(index + 1);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        }
    }
}