package com.sougata.sudoku.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieCompositionFactory;
import com.airbnb.lottie.LottieDrawable;
import com.sougata.GlobalStore;
import com.sougata.sudoku.R;
import com.sougata.sudoku.adapters.CalendarDayAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

public class DailyFragment extends Fragment {
    private final String[] days = new String[]{"S", "M", "T", "W", "T", "F", "S"};
    private final String[] months = new String[]{"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
    ArrayList<String> dayList = new ArrayList<>();

    ImageView previousMonth, nextMonth;
    TextView monthYear;
    LottieAnimationView animationView;
    private final GlobalStore globalStore = GlobalStore.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_daily, container, false);

        previousMonth = view.findViewById(R.id.iv_prev_month);
        nextMonth = view.findViewById(R.id.iv_next_month);
        monthYear = view.findViewById(R.id.tv_month_year);
        animationView = view.findViewById(R.id.lv_challenge_anim);

        LottieDrawable drawable = new LottieDrawable();
        drawable.setImagesAssetsFolder("images/");

        LottieCompositionFactory.fromAsset(requireContext(), "challenge.json")
                .addListener(composition -> {
                    drawable.setComposition(composition);
                    animationView.setImageDrawable(drawable);
                    drawable.setRepeatCount(LottieDrawable.INFINITE);
                    drawable.playAnimation();
                });

        Calendar c = Calendar.getInstance();
        AtomicInteger year = new AtomicInteger(c.get(Calendar.YEAR));
        AtomicInteger month = new AtomicInteger(c.get(Calendar.MONTH));

        globalStore.setMonth(month.get());
        globalStore.setYear(year.get());

        generateCalender(month.get(), year.get());
        CalendarDayAdapter adapter = new CalendarDayAdapter(requireContext(), dayList);
        GridView gridView = view.findViewById(R.id.calendar_grid);
        gridView.setAdapter(adapter);

        Calendar calendar = Calendar.getInstance();

        if (calendar.get(Calendar.MONTH) == c.get(Calendar.MONTH) && calendar.get(Calendar.YEAR) == c.get(Calendar.YEAR)) {
            nextMonth.setVisibility(View.GONE);
        } else {
            nextMonth.setVisibility(View.VISIBLE);
        }

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
            String monthYearText = months[month.get()] + " " + year.get();
            monthYear.setText(monthYearText);
            if (calendar.get(Calendar.MONTH) == c.get(Calendar.MONTH) && calendar.get(Calendar.YEAR) == c.get(Calendar.YEAR)) {
                nextMonth.setVisibility(View.GONE);
            } else {
                nextMonth.setVisibility(View.VISIBLE);
            }
        });

        nextMonth.setOnClickListener(view1 -> {
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
            String monthYearText = months[month.get()] + " " + year.get();
            monthYear.setText(monthYearText);
            if (calendar.get(Calendar.MONTH) == c.get(Calendar.MONTH) && calendar.get(Calendar.YEAR) == c.get(Calendar.YEAR)) {
                nextMonth.setVisibility(View.GONE);
            } else {
                nextMonth.setVisibility(View.VISIBLE);
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
    }
}