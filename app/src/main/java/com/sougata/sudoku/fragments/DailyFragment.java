package com.sougata.sudoku.fragments;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.sougata.Constants;
import com.sougata.GlobalStore;
import com.sougata.sudoku.R;
import com.sougata.sudoku.adapters.CalendarDayAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

public class DailyFragment extends Fragment {
    private final String[] days = new String[]{"S", "M", "T", "W", "T", "F", "S"};
    ArrayList<String> dayList = new ArrayList<>();

    ImageView previousMonth, nextMonth, bottomBorder;
    TextView tvMonth, tvYear, completeCount;
    int currentMonthDaysCount;
    private GestureDetector gestureDetector;
    boolean isNextMontDisabled = true;
    private final GlobalStore globalStore = GlobalStore.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_daily, container, false);

        GridView gridView = view.findViewById(R.id.calendar_grid);
        previousMonth = view.findViewById(R.id.iv_prev_month);
        nextMonth = view.findViewById(R.id.iv_next_month);
        tvMonth = view.findViewById(R.id.tv_month);
        tvYear = view.findViewById(R.id.tv_year);
        completeCount = view.findViewById(R.id.tv_complete_count);
        bottomBorder = view.findViewById(R.id.iv_banner_bottom_border);
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)bottomBorder.getLayoutParams();
        params.height = (int) (displayMetrics.widthPixels * 0.064f);
        bottomBorder.setLayoutParams(params);

        gestureDetector = new GestureDetector(requireContext(), new GestureListener());
        gridView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                if (event.getAction() == MotionEvent.ACTION_UP && !gestureDetector.onTouchEvent(event)) {
                    view.performClick();
                }
                return true;
            }
        });


        Calendar c = Calendar.getInstance();
        AtomicInteger year = new AtomicInteger(c.get(Calendar.YEAR));
        AtomicInteger month = new AtomicInteger(c.get(Calendar.MONTH));

        globalStore.setMonth(month.get());
        globalStore.setYear(year.get());

        generateCalender(month.get(), year.get());
        CalendarDayAdapter adapter = new CalendarDayAdapter(requireContext(), dayList);
        gridView.setAdapter(adapter);

        Calendar calendar = Calendar.getInstance();

        if (calendar.get(Calendar.MONTH) == c.get(Calendar.MONTH) && calendar.get(Calendar.YEAR) == c.get(Calendar.YEAR)) {
            nextMonth.setVisibility(View.GONE);
        } else {
            nextMonth.setVisibility(View.VISIBLE);
        }

        tvYear.setText(String.valueOf(year.get()));
        tvMonth.setText(Constants.FULL_MONTHS[month.get()]);
        String complete = globalStore.getDailyCompleted() + "/" + currentMonthDaysCount;
        completeCount.setText(complete);

        previousMonth.setOnClickListener(view1 -> {
            c.add(Calendar.MONTH, -1);
            year.set(c.get(Calendar.YEAR));
            month.set(c.get(Calendar.MONTH));
            generateCalender(month.get(), year.get());
            gridView.animate()
                    .translationX(gridView.getWidth())
                    .setDuration(150)
                    .withEndAction(() -> {
                        adapter.notifyDataSetChanged();
                        gridView.setTranslationX(-gridView.getWidth());
                        gridView.animate().translationX(0).setDuration(150);
                    });


            globalStore.setMonth(month.get());
            globalStore.setYear(year.get());
            tvYear.setText(String.valueOf(year.get()));
            tvMonth.setText(Constants.FULL_MONTHS[month.get()]);
            String completeTxt = globalStore.getDailyCompleted() + "/" + currentMonthDaysCount;
            completeCount.setText(completeTxt);
            if (calendar.get(Calendar.MONTH) == c.get(Calendar.MONTH) && calendar.get(Calendar.YEAR) == c.get(Calendar.YEAR)) {
                nextMonth.setVisibility(View.GONE);
                isNextMontDisabled = true;
            } else {
                nextMonth.setVisibility(View.VISIBLE);
                isNextMontDisabled = false;
            }
        });

        nextMonth.setOnClickListener(view1 -> {
            if (isNextMontDisabled) return;
            c.add(Calendar.MONTH, 1);
            year.set(c.get(Calendar.YEAR));
            month.set(c.get(Calendar.MONTH));
            generateCalender(month.get(), year.get());
            gridView.animate()
                    .translationX(-gridView.getWidth())
                    .setDuration(150)
                    .withEndAction(() -> {
                        adapter.notifyDataSetChanged();
                        gridView.setTranslationX(gridView.getWidth());
                        gridView.animate().translationX(0).setDuration(150);
                    });
            globalStore.setMonth(month.get());
            globalStore.setYear(year.get());
            tvYear.setText(String.valueOf(year.get()));
            tvMonth.setText(Constants.FULL_MONTHS[month.get()]);
            String completeTxt = globalStore.getDailyCompleted() + "/" + currentMonthDaysCount;
            completeCount.setText(completeTxt);
            if (calendar.get(Calendar.MONTH) == c.get(Calendar.MONTH) && calendar.get(Calendar.YEAR) == c.get(Calendar.YEAR)) {
                nextMonth.setVisibility(View.GONE);
                isNextMontDisabled = true;
            } else {
                nextMonth.setVisibility(View.VISIBLE);
                isNextMontDisabled = false;
            }
        });

        return view;
    }

    private void generateCalender(int month, int year) {
        dayList.clear();
        dayList.addAll(Arrays.asList(days));
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DATE, 1);
        int blankBox = c.get(Calendar.DAY_OF_WEEK) - 1;
        c.set(Calendar.MONTH, month + 1);
        c.set(Calendar.DATE, 1);
        c.add(Calendar.DATE, -1);
        int lastDate = c.get(Calendar.DATE);
        for (int i = 0; i < blankBox; i++) {
            dayList.add("");
        }
        for (int i = 1; i <= lastDate; i++) {
            dayList.add(String.valueOf(i));
        }
        currentMonthDaysCount = lastDate;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                        result = true;
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }

    private void onSwipeLeft() {
        nextMonth.performClick();
    }

    private void onSwipeRight() {
        previousMonth.performClick();
    }
}