package com.sougata.sudoku.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.sougata.Constants;
import com.sougata.GlobalStore;
import com.sougata.HelperFunctions;
import com.sougata.sudoku.Database;
import com.sougata.sudoku.R;
import com.sougata.sudoku.StartNewGame;
import com.sougata.sudoku.activities.GameActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class CalendarDayAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<String> days;
    GlobalStore globalStore = GlobalStore.getInstance();
    Database db;


    ArrayList<String> completedDays = new ArrayList<>();

    public CalendarDayAdapter(@NonNull Context context, ArrayList<String> days) {
        this.context = context;
        this.days = days;
        db = new Database(context);
        this.completedDays = getDateList();
    }

    @Override
    public int getCount() {
        return days.size();
    }

    @Override
    public Object getItem(int position) {
        return days.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public void notifyDataSetChanged() {
        this.completedDays = getDateList();
        super.notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.calender_days, parent, false);
        }
        ArrayList<String> d = new ArrayList<>(Arrays.asList("S", "M", "T", "W", "T", "F", "S"));
        LinearLayout dayLayout = convertView.findViewById(R.id.ll_calendar_day);
        TextView dayText = convertView.findViewById(R.id.tv_calendar_day);
        String day = days.get(position);

        Calendar c = Calendar.getInstance();
        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE), 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        int today = 0;
        if (!d.contains(day) && !day.isEmpty()) {
            Calendar c2 = Calendar.getInstance();
            c2.set(globalStore.getYear(), globalStore.getMonth(), Integer.parseInt(day), 0, 0, 0);
            c2.set(Calendar.MILLISECOND, 0);
            globalStore.setDay(Integer.parseInt(day));
            if (c2.before(c) || c2.equals(c)) {
                dayText.setOnClickListener(view1 -> dayOnClick(Integer.parseInt(day), globalStore.getMonth(), globalStore.getYear(), context));

                dayText.setClickable(true);
                if (c2.equals(c)) {
                    today = 1;
                    dayText.setTextColor(ContextCompat.getColor(context, R.color.fill_btn_text));
                    dayLayout.setBackgroundResource(R.drawable.btn_bg_solid);
                } else {
                    dayText.setTextColor(ContextCompat.getColor(context, R.color.black));
                }
            } else {
                dayText.setOnClickListener(null);
                dayText.setClickable(false);
                dayText.setTextColor(ContextCompat.getColor(context, R.color.gray));
            }
        } else if (d.contains(day)) {
            dayText.setTextColor(ContextCompat.getColor(context, R.color.gray));
        }

        if (completedDays.contains(day)) {
            dayLayout.setBackgroundResource(R.drawable.ic_medal);
            dayText.setText("");
        } else if (today != 1) {
            dayLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            dayText.setText(day);
        } else {
            dayText.setText(day);
        }
        return convertView;
    }

    private ArrayList<String> getDateList() {
        ArrayList<String> dateList = new ArrayList<>();
        Cursor cursor = db.getDailyMatch(globalStore.getMonth(), globalStore.getYear());
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            do {
                long d = cursor.getLong(1);
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(d);
                dateList.add(String.valueOf(c.get(Calendar.DATE)));
            } while (cursor.moveToNext());
        }
        globalStore.setDailyCompleted(dateList.size());
        return dateList;
    }

    public static void dayOnClick(int day, int month, int year, Context context) {
        Database db = new Database(context);
        GlobalStore globalStore = GlobalStore.getInstance();
        Calendar calendar = HelperFunctions.getCalendar();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DATE, day);
        Intent intent = new Intent(context, GameActivity.class);
        Cursor cursor = db.getDailyMatch(calendar.getTimeInMillis());
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            int isCompleted = cursor.getInt(11);
            globalStore.setId(cursor.getLong(0));
            globalStore.setCurrentBoardState(HelperFunctions.parseTwoDimArray(cursor.getString(6)));

            intent.putExtra("date", calendar.get(Calendar.DATE));

            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
            View v = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_daily, null);
            bottomSheetDialog.setContentView(v);
            bottomSheetDialog.show();
            TextView cancel = v.findViewById(R.id.tv_bs_cancel);
            TextView continueTxt = v.findViewById(R.id.tv_bs_continue);
            TextView restart = v.findViewById(R.id.tv_bs_restart);

            restart.setOnClickListener(view2 -> {
                db.restartGame(globalStore.getId(), cursor.getString(6));
                globalStore.setTimer(0);
                globalStore.setMistakes(0);
                globalStore.setBoard(HelperFunctions.parseTwoDimArray(cursor.getString(6)));
                globalStore.setSolution(HelperFunctions.parseTwoDimArray(cursor.getString(7)));
                globalStore.setDifficultyName(cursor.getString(3));
                globalStore.setDifficulty(cursor.getInt(2));
                globalStore.setCurrentLevel(cursor.getInt(1));
                globalStore.setType(Constants.TYPES[1]);
                globalStore.setDay(day);
                globalStore.setNotes(new int[9][9][9]);
                bottomSheetDialog.cancel();
                context.startActivity(intent);
            });
            if (isCompleted == 0) {
                continueTxt.setVisibility(View.VISIBLE);
                continueTxt.setOnClickListener(view2 -> {
                    new StartNewGame(context).createDailyGame(day);
                    bottomSheetDialog.cancel();
                    context.startActivity(intent);
                });
            } else {
                continueTxt.setVisibility(View.GONE);
            }
            cancel.setOnClickListener(view2 -> {
                bottomSheetDialog.cancel();
            });
        } else {
            new StartNewGame(context).createDailyGame(day);
            intent.putExtra("date", day);
            context.startActivity(intent);
            if (context instanceof Activity) {
                ((Activity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        }
    }
}
