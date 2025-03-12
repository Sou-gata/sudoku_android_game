package com.sougata.sudoku.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.sougata.GlobalStore;
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
        if (!d.contains(day) && !day.isEmpty()) {
            Calendar c2 = Calendar.getInstance();
            c2.set(globalStore.getYear(), globalStore.getMonth(), Integer.parseInt(day), 0, 0, 0);
            c2.set(Calendar.MILLISECOND, 0);
            if (c2.before(c) || c2.equals(c)) {
                dayText.setOnClickListener(view1 -> {
                    new StartNewGame(context).createDailyGame(Integer.parseInt(day));
                    Intent intent = new Intent(context, GameActivity.class);
                    context.startActivity(intent);

                });
            } else {
                dayText.setOnClickListener(null);
                dayText.setClickable(false);
            }

            if (c.get(Calendar.DATE) == Integer.parseInt(day) && c.get(Calendar.MONTH) == globalStore.getMonth() && c.get(Calendar.YEAR) == globalStore.getYear()) {
                dayText.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            } else {
                dayText.setTextColor(ContextCompat.getColor(context, R.color.black));
            }
        } else if (d.contains(day)) {
            dayText.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            dayText.setTypeface(Typeface.DEFAULT_BOLD);
        }



        if (completedDays.contains(day)) {
            dayLayout.setBackgroundResource(R.drawable.cups_daily);
            dayText.setText("");
        } else {
            dayLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            dayText.setText(day);
        }
        return convertView;
    }

    private ArrayList<String> getDateList() {
        ArrayList<String> dateList = new ArrayList<>();
        Cursor cursor = db.getDailyMatch(globalStore.getMonth(), globalStore.getYear());
        dateList.clear();
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            do {
                dateList.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }
            Log.d("getView", cursor.getCount()+"");
        return dateList;
    }
}
