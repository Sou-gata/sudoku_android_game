package com.sougata.sudoku.fragments;

import static com.sougata.HelperFunctions.dpToPx;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.sougata.Constants;
import com.sougata.GlobalStore;
import com.sougata.HelperFunctions;
import com.sougata.sudoku.Database;
import com.sougata.sudoku.R;
import com.sougata.sudoku.activities.HomeActivity;

import java.util.Calendar;

public class DailyAwardFragment extends Fragment {
    Context context;
    LinearLayout dailyAwardsContainer;
    Database db;
    int[] trophyImages = {
            R.drawable.cup_01, R.drawable.cup_02, R.drawable.cup_03, R.drawable.cup_04, R.drawable.cup_05, R.drawable.cup_06, R.drawable.cup_07, R.drawable.cup_08, R.drawable.cup_09, R.drawable.cup_10, R.drawable.cup_11, R.drawable.cup_12,
            R.drawable.cup_13, R.drawable.cup_14, R.drawable.cup_15, R.drawable.cup_16, R.drawable.cup_17, R.drawable.cup_18, R.drawable.cup_19, R.drawable.cup_20, R.drawable.cup_21, R.drawable.cup_22, R.drawable.cup_23, R.drawable.cup_24
    };
    public DailyAwardFragment(Context context) {
        this.context = context;
        db = new Database(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_daily_award, container, false);
        dailyAwardsContainer = view.findViewById(R.id.ll_daily_awards_container);
        generateAwardsList();
        return view;
    }

    private void generateAwardsList() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        dailyAwardsContainer.addView(generateTopText(year));

        int rowNum = (calendar.get(Calendar.MONTH) / 3) + 1;
        int curMonth = calendar.get(Calendar.MONTH);
        for (int i = 0; i < rowNum; i++) {
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i == 0) {
                params.setMargins(0, dpToPx(10), 0, 0);
            } else if (i == rowNum - 1) {
                params.setMargins(0, 0, 0, dpToPx(10));
            }
            row.setLayoutParams(params);
            for (int j = 0; j < 3; j++) {
                LinearLayout cell = createMonth(curMonth, year);
                row.addView(cell);
                curMonth--;
                if (curMonth == -1) {
                    curMonth = 11;
                    break;
                }
            }
            dailyAwardsContainer.addView(row);
        }
        dailyAwardsContainer.addView(generateTopText(year - 1));
        for (int i = 0; i < 4; i++) {
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i == 0) {
                params.setMargins(0, dpToPx(10), 0, 0);
            } else if (i == 3) {
                params.setMargins(0, 0, 0, dpToPx(10));
            }
            row.setLayoutParams(params);
            for (int j = 0; j < 3; j++) {
                LinearLayout cell = createMonth(curMonth, year - 1);
                row.addView(cell);
                curMonth--;
                if (curMonth == -1) break;

            }
            dailyAwardsContainer.addView(row);
        }
        dailyAwardsContainer.addView(generateTopText(year - 2));
        int numMonths = 12 - (calendar.get(Calendar.MONTH) + 1);
        int rowCount = ((numMonths - 1) / 3) + 1;
        for (int i = 0; i < rowCount; i++) {
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i == 0) {
                params.setMargins(0, dpToPx(10), 0, 0);
            } else if (i == rowCount - 1) {
                params.setMargins(0, 0, 0, dpToPx(10));
            }
            row.setLayoutParams(params);
            for (int j = 0; j < 3; j++) {
                LinearLayout cell = createMonth(numMonths - 1, year - 2);
                row.addView(cell);
                numMonths--;
                if (numMonths == 0) break;

            }
            dailyAwardsContainer.addView(row);
        }
    }

    private LinearLayout createMonth(int month, int year) {
        int totalDays = getTotalDays(month, year);
        int completedDays = getCompleteCount(month, year);
        LinearLayout cell = new LinearLayout(context);
        cell.setOrientation(LinearLayout.VERTICAL);
        int cellWidth = HelperFunctions.getScreenWidth(context) / 3;
        cell.setLayoutParams(new LinearLayout.LayoutParams(cellWidth, LinearLayout.LayoutParams.WRAP_CONTENT));
        FrameLayout trophyContainer = new FrameLayout(context);
        trophyContainer.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ImageView trophy = new ImageView(context);
        int dim = dpToPx(125);
        trophy.setLayoutParams(new FrameLayout.LayoutParams(cellWidth, cellWidth));
        trophy.setImageResource(trophyImages[month + (completedDays == totalDays ? 12 : 0)]);
        trophyContainer.addView(trophy);

        TextView wins = new TextView(context);
        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(cellWidth, FrameLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, 0, 0, (int) (dim * 0.085));
        p.gravity = Gravity.BOTTOM;
        wins.setLayoutParams(p);
        String text = completedDays + "/" + totalDays;
        wins.setText(text);
        wins.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        wins.setTextColor(ContextCompat.getColor(context, GlobalStore.getInstance().isDarkMode() ? R.color.white: R.color.black));
        wins.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        trophyContainer.addView(wins);
        cell.addView(trophyContainer);
        TextView monthName = new TextView(context);
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params1.setMargins(0, dpToPx(10), 0, 0);
        monthName.setLayoutParams(params1);
        monthName.setText(Constants.FULL_MONTHS[month]);
        monthName.setTextColor(ContextCompat.getColor(context, R.color.black));
        monthName.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        cell.addView(monthName);

        cell.setOnClickListener(v->{
            Intent intent = new Intent(context, HomeActivity.class);
            intent.putExtra("isDaily", true);
            intent.putExtra("month", month);
            intent.putExtra("year", year);
            startActivity(intent);
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            requireActivity().finish();
        });

        return cell;
    }

    private CardView generateTopText(int year) {
        CardView card = new CardView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(dpToPx(10), 0, dpToPx(10), 0);
        card.setLayoutParams(params);
        card.setRadius(dpToPx(5));
        card.setCardElevation(5);
        card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white));

        TextView yearTextView = new TextView(context);
        yearTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        yearTextView.setPadding(0, dpToPx(3), 0, dpToPx(5));
        yearTextView.setText(String.valueOf(year));
        yearTextView.setTextColor(ContextCompat.getColor(context, R.color.black));
        yearTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        yearTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        yearTextView.setTypeface(null, Typeface.BOLD);
        card.addView(yearTextView);
        return card;
    }

    private int getTotalDays(int month, int year) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month + 1);
        c.set(Calendar.DATE, 1);
        c.add(Calendar.DATE, -1);
        return c.get(Calendar.DATE);
    }

    private int getCompleteCount(int month, int year) {
        Cursor cursor = db.getDailyMatch(month, year);
        return cursor.getCount();
    }
}