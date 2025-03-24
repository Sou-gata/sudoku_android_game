package com.sougata.sudoku.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sougata.Constants;
import com.sougata.HelperFunctions;
import com.sougata.sudoku.ButtonClickListener;
import com.sougata.sudoku.Database;
import com.sougata.sudoku.HistoryItem;
import com.sougata.sudoku.R;
import com.sougata.sudoku.adapters.HistoryRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.Calendar;

public class HistoryActivity extends AppCompatActivity implements ButtonClickListener {

    Database db;
    ArrayList<HistoryItem> items = new ArrayList<>();
    ImageView backBtn;
    RecyclerView historyRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);

        setContentView(R.layout.activity_history);

        backBtn = findViewById(R.id.iv_history_back_button);
        historyRecyclerView = findViewById(R.id.rv_history);

        db = new Database(this);

        loadHistory();

        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        HistoryRecyclerViewAdapter adapter = new HistoryRecyclerViewAdapter(items, this, this);
        historyRecyclerView.setAdapter(adapter);

        backBtn.setOnClickListener(v -> finish());
    }

    private void loadHistory() {
        Cursor cursor = db.getAllGames();
        if (cursor.getCount() == 0) return;
        cursor.moveToFirst();
        items.clear();
        while (!cursor.isAfterLast()) {
            long id = cursor.getLong(0);
            String difficulty = cursor.getString(3);
            String timer = HelperFunctions.timerToString(cursor.getInt(4));
            int hint = cursor.getInt(8);
            int mistake = cursor.getInt(9);
            boolean isCompleted = cursor.getInt(11) == 1;
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(cursor.getLong(13));
            String date = HelperFunctions.padString(calendar.get(Calendar.DATE), 2) + " " + Constants.MONTHS[calendar.get(Calendar.MONTH)] + ", " + calendar.get(Calendar.YEAR);
            String time = HelperFunctions.get12hClock(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
            items.add(new HistoryItem(id, difficulty, timer, hint, mistake, date, time, isCompleted));
            cursor.moveToNext();
        }
    }

    @Override
    public void onItemButtonClick() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}