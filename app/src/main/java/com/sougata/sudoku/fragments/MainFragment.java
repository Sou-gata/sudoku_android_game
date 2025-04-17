package com.sougata.sudoku.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.sougata.GlobalStore;
import com.sougata.HelperFunctions;
import com.sougata.sudoku.Database;
import com.sougata.sudoku.R;
import com.sougata.sudoku.StartNewGame;
import com.sougata.sudoku.activities.AwardsActivity;
import com.sougata.sudoku.activities.EventActivity;
import com.sougata.sudoku.activities.GameActivity;
import com.sougata.sudoku.activities.SettingsActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MainFragment extends Fragment {

    Button newGame;
    LinearLayout resumeGame;
    TextView resumeStatus, eventTitle, eventTimer, eventPlay, dailyDate;
    ImageView settings, awards, eventIcon, dailyIcon;
    CardView event, daily;
    GlobalStore globalStore = GlobalStore.getInstance();
    Database db;
    StartNewGame startNewGame;
    Context context;
    Timer eventTimerObj;

    int[] cups = {R.drawable.cup_13, R.drawable.cup_14, R.drawable.cup_15, R.drawable.cup_16, R.drawable.cup_17, R.drawable.cup_18, R.drawable.cup_19, R.drawable.cup_20, R.drawable.cup_21, R.drawable.cup_22, R.drawable.cup_23, R.drawable.cup_24};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOngoingDb();
//        loadEventData();
        startTimer();
    }

    @Override
    public void onStart() {
        super.onStart();
        loadOngoingDb();
        loadEventData();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopTimer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTimer();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopTimer();
    }


    private void loadOngoingDb() {
        Cursor c = db.getOngoingMatch();
        c.moveToFirst();
        if (c.getCount() != 0) {
            globalStore.setId(c.getLong(0));
            globalStore.setCurrentLevel(c.getInt(1));
            globalStore.setDifficulty(c.getInt(2));
            globalStore.setDifficultyName(c.getString(3));
            globalStore.setTimer(c.getInt(4));
            globalStore.setCurrentBoardState(HelperFunctions.parseTwoDimArray(c.getString(5)));
            globalStore.setBoard(HelperFunctions.parseTwoDimArray(c.getString(6)));
            globalStore.setSolution(HelperFunctions.parseTwoDimArray(c.getString(7)));
            globalStore.setMistakes(c.getInt(9));
            globalStore.setType(c.getString(10));
            globalStore.setNotes(HelperFunctions.parseThreeDimArr(c.getString(14)));
            resumeGame.setVisibility(View.VISIBLE);
            String resumeStatusText = HelperFunctions.timerToString(globalStore.getTimer()) + " - " + globalStore.getDifficultyName();
            resumeStatus.setText(resumeStatusText);
        } else {
            resumeGame.setVisibility(View.GONE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        context = view.getContext();
        db = new Database(context);
        startNewGame = new StartNewGame(context);
        newGame = view.findViewById(R.id.btn_home_new_game);
        resumeGame = view.findViewById(R.id.ll_home_resume_game);
        resumeStatus = view.findViewById(R.id.tv_main_resume_status);
        settings = view.findViewById(R.id.iv_home_settings);
        awards = view.findViewById(R.id.iv_home_cup);
        event = view.findViewById(R.id.btn_event);
        daily = view.findViewById(R.id.cv_daily);
        eventIcon = view.findViewById(R.id.iv_home_event_icon);
        eventTitle = view.findViewById(R.id.tv_event_title);
        eventTimer = view.findViewById(R.id.tv_event_timer);
        eventPlay = view.findViewById(R.id.tv_home_event_play);
        dailyIcon = view.findViewById(R.id.iv_home_daily_icon);
        dailyDate = view.findViewById(R.id.tv_daily_date);

        daily.setOnClickListener(v -> dailyClicked());
        event.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventActivity.class);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        int screenWidth = HelperFunctions.getScreenWidth(context) - HelperFunctions.dpToPx(50);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) event.getLayoutParams();
        params.width = screenWidth / 3;
        event.setLayoutParams(params);
        params = (LinearLayout.LayoutParams) daily.getLayoutParams();
        params.width = screenWidth / 3;
        params.rightMargin = HelperFunctions.dpToPx(10);
        daily.setLayoutParams(params);

        dailyIcon.setImageResource(cups[Calendar.getInstance().get(Calendar.MONTH)]);
        dailyDate.setText(HelperFunctions.getDailyDate());

        settings.setOnClickListener(view1 -> {
            Intent intent = new Intent(context, SettingsActivity.class);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        awards.setOnClickListener(view1 -> {
            Intent intent = new Intent(context, AwardsActivity.class);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        Intent intent = new Intent(context, GameActivity.class);
        loadOngoingDb();

        newGame.setOnClickListener(view3 -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
            View view1 = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_select_difficulty, null);
            bottomSheetDialog.setContentView(view1);
            bottomSheetDialog.show();
            LinearLayout bs_difficulty_easy = view1.findViewById(R.id.bs_difficulty_easy);
            LinearLayout bs_difficulty_medium = view1.findViewById(R.id.bs_difficulty_medium);
            LinearLayout bs_difficulty_hard = view1.findViewById(R.id.bs_difficulty_hard);
            LinearLayout bs_difficulty_expert = view1.findViewById(R.id.bs_difficulty_expert);
            LinearLayout bs_difficulty_nightmare = view1.findViewById(R.id.bs_difficulty_nightmare);
            LinearLayout bs_restart = view1.findViewById(R.id.bs_difficulty_restart);

            bs_difficulty_easy.setOnClickListener(view2 -> {
                startNewGame.createEasyGame();
                startActivity(intent);
                bottomSheetDialog.cancel();
            });
            bs_difficulty_medium.setOnClickListener(view2 -> {
                startNewGame.createMediumGame();
                startActivity(intent);
                bottomSheetDialog.cancel();
            });
            bs_difficulty_hard.setOnClickListener(view2 -> {
                startNewGame.createHardGame();
                startActivity(intent);
                bottomSheetDialog.cancel();
            });
            bs_difficulty_expert.setOnClickListener(view2 -> {
                startNewGame.createExpertGame();
                startActivity(intent);
                bottomSheetDialog.cancel();

            });
            bs_difficulty_nightmare.setOnClickListener(view2 -> {
                startNewGame.createNightmareGame();
                startActivity(intent);
                bottomSheetDialog.cancel();
            });
            bs_restart.setOnClickListener(view2 -> {
                startNewGame.restartGame();
                startActivity(intent);
                bottomSheetDialog.cancel();
            });
        });

        resumeGame.setOnClickListener(view2 -> startActivity(intent));

        return view;
    }

    private void loadEventData() {
        Thread thread = new Thread(() -> {
            try {
                URL url = new URL(ContextCompat.getString(context, R.string.event_url));
                URLConnection conn = url.openConnection();
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder jsonString = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonString.append(line);
                }
                reader.close();
                JSONObject json = new JSONObject(jsonString.toString());
                String id = json.getString("id").replace("-", "");
                json.put("id", id);
                globalStore.setEventDetails(json);
                String title = json.getString("name");
                JSONObject colors = json.getJSONObject("colors");
                String bgColor = colors.getString("banner_bg_color");
                String textColor = colors.getString("banner_text_color");
                String iconUrl = json.getJSONObject("assets").getString("large_icon");
                requireActivity().runOnUiThread(() -> {
                    eventTitle.setText(title);
                    Glide.with(context).load(iconUrl).into(eventIcon);
                    GradientDrawable drawable = (GradientDrawable) eventPlay.getBackground();
                    drawable.setColor(Color.parseColor(bgColor));
                    eventPlay.setTextColor(Color.parseColor(textColor));
                    event.setVisibility(View.VISIBLE);
                    calculateTime(json);
                    startTimer();
                });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    event.setVisibility(View.GONE);
                });
                globalStore.setEventDetails(null);
            }
        });
        thread.start();
    }

    private class Task extends TimerTask {
        JSONObject json;

        Task(JSONObject json) {
            this.json = json;
        }

        @Override
        public void run() {
            calculateTime(json);
        }
    }


    private void calculateTime(JSONObject json) {
        try {
            Calendar start = Calendar.getInstance();
            start.setTimeInMillis(json.getLong("start_date"));
            Calendar end = Calendar.getInstance();
            end.setTimeInMillis(json.getLong("end_date"));
            Calendar today = Calendar.getInstance();

            long diffMillis;
            if (today.before(start)) {
                diffMillis = start.getTimeInMillis() - today.getTimeInMillis();
                eventPlay.setText("Coming");
            } else if (today.after(start) && today.before(end)) {
                diffMillis = end.getTimeInMillis() - today.getTimeInMillis();
                eventPlay.setText("Play");
            } else {
                diffMillis = -1;
            }
            String time;
            if (diffMillis != -1) {
                long diffSeconds = diffMillis / 1000;
                long diffMinutes = diffSeconds / 60;
                long diffHours = diffMinutes / 60;

                long diffDays = diffHours / 24;

                long remainingHours = diffHours % 24;
                long remainingMinutes = diffMinutes % 60;

                time = (diffDays + "d " + remainingHours + "h " + remainingMinutes + "m");
                eventTimer.setText(time);
            } else {
                event.setVisibility(View.GONE);
            }
        } catch (Exception ignored) {
        }
    }

    private void startTimer() {
        Task task = new Task(globalStore.getEventDetails());
        eventTimerObj = new Timer();
        eventTimerObj.schedule(task, 1000, 1000);
    }

    private void stopTimer() {
        if (eventTimerObj != null) {
            eventTimerObj.cancel();
            eventTimerObj = null;
        }
    }

    private void dailyClicked() {
        Calendar c = HelperFunctions.getCalendar();
        Cursor cursor = db.getDailyMatch(c.getTimeInMillis());
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            if (cursor.getInt(11) == 1) {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
                fragmentTransaction.replace(R.id.ll_tab_container, new DailyFragment());
                fragmentTransaction.commit();
                return;
            }
        }
        globalStore.setDay(c.get(Calendar.DATE));
        globalStore.setMonth(c.get(Calendar.MONTH));
        globalStore.setYear(c.get(Calendar.YEAR));
        new StartNewGame(context).createDailyGame(globalStore.getDay());
        Intent intent = new Intent(context, GameActivity.class);
        intent.putExtra("date", globalStore.getDay());
        context.startActivity(intent);
        if (getActivity() != null) {
            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }

    }
}