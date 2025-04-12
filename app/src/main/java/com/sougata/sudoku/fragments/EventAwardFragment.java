package com.sougata.sudoku.fragments;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.sougata.HelperFunctions;
import com.sougata.sudoku.Database;
import com.sougata.sudoku.R;

public class EventAwardFragment extends Fragment {
    Context context;
    Database db;
    LinearLayout medalsContainer;
    int screenWidth;

    public EventAwardFragment() {
    }

    public EventAwardFragment(Context context) {
        this.context = context;
        db = new Database(context);
        screenWidth = HelperFunctions.getScreenWidth(context) - HelperFunctions.dpToPx(30);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_award, container, false);

        medalsContainer = view.findViewById(R.id.ll_medals_container);

        createLayout();
        return view;
    }

    private void createLayout() {
        Cursor cursor = db.getMedals();
        int medalsCount = cursor.getCount();
        medalsContainer.removeAllViews();
        int w = screenWidth / 3;
        if (medalsCount > 0) {
            cursor.moveToFirst();
            int rowNumber = ((medalsCount - 1) / 3) + 1;
            for (int i = 0; i < rowNumber; i++) {
                LinearLayout row = new LinearLayout(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 0, HelperFunctions.dpToPx(10));
                row.setLayoutParams(params);
                row.setOrientation(LinearLayout.HORIZONTAL);
                for (int j = 1; j <= 3; j++) {
                    String eventId = cursor.getString(1);
                    String eventName = cursor.getString(2);
                    String medalName = cursor.getString(3);
                    String medalUrl = cursor.getString(4);
                    long date = cursor.getLong(5);

                    LinearLayout cell = new LinearLayout(context);
                    LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams(w, ViewGroup.LayoutParams.WRAP_CONTENT);
                    cell.setLayoutParams(cellParams);
                    cell.setOrientation(LinearLayout.VERTICAL);
                    cell.setGravity(Gravity.CENTER);
                    ImageView medalImage = new ImageView(context);
                    LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(w - 20, w - 20);
                    medalImage.setLayoutParams(imageParams);
                    Glide.with(context).load(medalUrl).signature(new ObjectKey(eventId)).into(medalImage);
                    cell.addView(medalImage);

                    TextView medalType = new TextView(context);
                    LinearLayout.LayoutParams medalTypeParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    medalTypeParams.setMargins(0, HelperFunctions.dpToPx(5), 0, 0);
                    medalType.setLayoutParams(medalTypeParams);
                    medalType.setText(medalName);
                    medalType.setTextSize(16);
                    medalType.setTextColor(ContextCompat.getColor(context, R.color.gray));
                    cell.addView(medalType);

                    TextView eventNameText = new TextView(context);
                    LinearLayout.LayoutParams eventNameParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    eventNameParams.setMargins(0, HelperFunctions.dpToPx(3), 0, 0);
                    eventNameText.setLayoutParams(eventNameParams);
                    eventNameText.setText(eventName);
                    eventNameText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                    eventNameText.setTextColor(ContextCompat.getColor(context, R.color.black));
                    eventNameText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    eventNameText.setTypeface(eventNameText.getTypeface(), Typeface.BOLD);
                    cell.addView(eventNameText);

                    TextView eventWinDate = new TextView(context);
                    LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    dateParams.topMargin = HelperFunctions.dpToPx(3);
                    eventWinDate.setLayoutParams(dateParams);
                    eventWinDate.setText(HelperFunctions.millisToDate(date));
                    eventWinDate.setTextColor(ContextCompat.getColor(context, R.color.gray));
                    eventWinDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                    eventWinDate.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    cell.addView(eventWinDate);
                    row.addView(cell);

                    if (i * 3 + j == medalsCount) break;
                    else cursor.moveToNext();
                }
                medalsContainer.addView(row);
            }
        }
    }
}