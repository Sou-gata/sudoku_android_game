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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.sougata.Constants;
import com.sougata.GlobalStore;
import com.sougata.HelperFunctions;
import com.sougata.sudoku.R;
import com.sougata.sudoku.adapters.CalendarDayAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

public class DailyFragment extends Fragment {
    private final String[] days = new String[]{"S", "M", "T", "W", "T", "F", "S"};
    ArrayList<String> dayList = new ArrayList<>();

    ImageView previousMonth, nextMonth, bottomBorder, dailyCup;
    TextView tvMonth, tvYear, completeCount, playBtnDate;
    LinearLayout playButton;
    int currentMonthDaysCount;
    private GestureDetector gestureDetector;
    boolean isNextMontDisabled = true;
    int m = -1, y = -1;
    int[] cups = {
            R.drawable.cup_01, R.drawable.cup_02, R.drawable.cup_03, R.drawable.cup_04, R.drawable.cup_05, R.drawable.cup_06, R.drawable.cup_07, R.drawable.cup_08, R.drawable.cup_09, R.drawable.cup_10, R.drawable.cup_11, R.drawable.cup_12,
            R.drawable.cup_13, R.drawable.cup_14, R.drawable.cup_15, R.drawable.cup_16, R.drawable.cup_17, R.drawable.cup_18, R.drawable.cup_19, R.drawable.cup_20, R.drawable.cup_21, R.drawable.cup_22, R.drawable.cup_23, R.drawable.cup_24
    };

    public DailyFragment() {
    }

    public DailyFragment(int month, int year) {
        this.m = month;
        this.y = year;
    }

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
        dailyCup = view.findViewById(R.id.iv_daily_cup);
        playButton = view.findViewById(R.id.ll_daily_play);
        playBtnDate = view.findViewById(R.id.tv_daily_play_date);

        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) bottomBorder.getLayoutParams();
        params.height = (int) (displayMetrics.widthPixels * 0.064f);
        bottomBorder.setLayoutParams(params);
        params = (FrameLayout.LayoutParams) dailyCup.getLayoutParams();
        params.bottomMargin = (int) (displayMetrics.widthPixels * 0.063f);

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

        if (m != -1) {
            month.set(m);
            year.set(y);
        }

        globalStore.setMonth(month.get());
        globalStore.setYear(year.get());

        generateCalender(month.get(), year.get());
        CalendarDayAdapter adapter = new CalendarDayAdapter(requireContext(), dayList);
        gridView.setAdapter(adapter);

        Calendar calendar = Calendar.getInstance();
        String date = HelperFunctions.padString(calendar.get(Calendar.DATE),2)+" "+Constants.FULL_MONTHS[calendar.get(Calendar.MONTH)]+", "+calendar.get(Calendar.YEAR);
        playBtnDate.setText(date);

        if (calendar.get(Calendar.MONTH) == c.get(Calendar.MONTH) && calendar.get(Calendar.YEAR) == c.get(Calendar.YEAR)) {
            nextMonth.setVisibility(View.GONE);
        } else {
            nextMonth.setVisibility(View.VISIBLE);
        }

        tvYear.setText(String.valueOf(year.get()));
        tvMonth.setText(Constants.FULL_MONTHS[month.get()]);
        String complete = globalStore.getDailyCompleted() + "/" + currentMonthDaysCount;
        completeCount.setText(complete);

        dailyCup.setImageResource(cups[month.get() + (globalStore.getDailyCompleted() == currentMonthDaysCount ? 12 : 0)]);

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
            dailyCup.animate()
                    .translationX(gridView.getWidth())
                    .setDuration(150)
                    .withEndAction(() -> {
                        dailyCup.setTranslationX(-gridView.getWidth());
                        dailyCup.setImageResource(cups[month.get() + (globalStore.getDailyCompleted() == currentMonthDaysCount ? 12 : 0)]);
                        dailyCup.animate().translationX(0).setDuration(150);
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
            dailyCup.animate()
                    .translationX(-gridView.getWidth())
                    .setDuration(150)
                    .withEndAction(() -> {
                        dailyCup.setTranslationX(gridView.getWidth());
                        dailyCup.setImageResource(cups[month.get() + (globalStore.getDailyCompleted() == currentMonthDaysCount ? 12 : 0)]);
                        dailyCup.animate().translationX(0).setDuration(150);
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
        playButton.setOnClickListener(v -> {
            Calendar c2 = Calendar.getInstance();
            CalendarDayAdapter.dayOnClick(c2.get(Calendar.DATE), c2.get(Calendar.MONTH), c2.get(Calendar.YEAR), requireContext());
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
            } catch (Exception ignored) {
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