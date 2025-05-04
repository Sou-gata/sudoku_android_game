package com.sougata.sudoku.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
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
    ArrayList<HistoryItem> items = new ArrayList<>(), allItems = new ArrayList<>();

    ImageView backBtn;
    RecyclerView historyRecyclerView;
    LinearLayout noRecordContainer;
    AppCompatSpinner matchTypes, classicType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);

        setContentView(R.layout.activity_history);

        backBtn = findViewById(R.id.iv_history_back_button);
        historyRecyclerView = findViewById(R.id.rv_history);
        matchTypes = findViewById(R.id.sp_type);
        classicType = findViewById(R.id.sp_classic_type);
        noRecordContainer = findViewById(R.id.ll_no_record_container);

        db = new Database(this);

        loadHistory();

        checkRecord();
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        HistoryRecyclerViewAdapter adapter = new HistoryRecyclerViewAdapter(items, this, this);
        historyRecyclerView.setAdapter(adapter);

        backBtn.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        String[] matchTypesArray = getResources().getStringArray(R.array.match_types);
        ArrayAdapter<String> matchTypeAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, android.R.id.text1, matchTypesArray);
        matchTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        matchTypes.setAdapter(matchTypeAdapter);

        String[] classicTypes = new String[Constants.LEVEL_NAME.length + 1];
        classicTypes[0] = "All";
        System.arraycopy(Constants.LEVEL_NAME, 0, classicTypes, 1, Constants.LEVEL_NAME.length);
        ArrayAdapter<String> classicTypeAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, android.R.id.text1, classicTypes);
        classicTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        classicType.setAdapter(classicTypeAdapter);

        matchTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                if (matchTypesArray[position].equals(matchTypesArray[1])) {
                    classicType.setVisibility(View.VISIBLE);
                } else {
                    classicType.setVisibility(View.INVISIBLE);
                }
                if (position == 0) {
                    items.clear();
                    items.addAll(allItems);
                } else if (position == 1) {
                    items.clear();
                    for (HistoryItem item : allItems) {
                        if (item.getType().equals(Constants.TYPES[0])) {
                            items.add(item);
                        }
                    }
                    classicType.setSelection(0);
                } else if (position == 2) {
                    items.clear();
                    for (HistoryItem item : allItems) {
                        if (item.getType().equals(Constants.TYPES[1])) {
                            items.add(item);
                        }
                    }
                } else if (position == 3) {
                    items.clear();
                    for (HistoryItem item : allItems) {
                        if (item.getType().equals(Constants.TYPES[2])) {
                            items.add(item);
                        }
                    }
                } else if (position == 4) {
                    items.clear();
                    for (HistoryItem item : allItems) {
                        if (item.isCompleted()) {
                            items.add(item);
                        }
                    }
                } else if (position == 5) {
                    items.clear();
                    for (HistoryItem item : allItems) {
                        if (!item.isCompleted()) {
                            items.add(item);
                        }
                    }
                }
                checkRecord();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        classicType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                if (position == 0) {
                    items.clear();
                    for (HistoryItem item : allItems) {
                        if (item.getType().equals(Constants.TYPES[0])) {
                            items.add(item);
                        }
                    }
                } else {
                    addDataToList(db.getDifficultyGame(Constants.LEVEL_NAME[position - 1]));
                }
                checkRecord();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void loadHistory() {
        Cursor cursor = db.getAllGames();
        addDataToList(cursor);
        allItems.clear();
        allItems.addAll(items);
    }

    @Override
    public void onItemButtonClick() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void addDataToList(Cursor cursor) {
        items.clear();
        if (cursor.getCount() == 0) return;
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            long id = cursor.getLong(0);
            String difficulty = cursor.getString(3);
            String timer = HelperFunctions.timerToString(cursor.getInt(4));
            int hint = cursor.getInt(8);
            int mistake = cursor.getInt(9);
            boolean isCompleted = cursor.getInt(11) == 1;
            String type = cursor.getString(10);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(cursor.getLong(13));
            String date = HelperFunctions.padString(calendar.get(Calendar.DATE), 2) + "/" + HelperFunctions.padString(calendar.get(Calendar.MONTH) + 1, 2) + "/" + calendar.get(Calendar.YEAR);
            String time = HelperFunctions.get12hClock(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
            items.add(new HistoryItem(id, difficulty, timer, hint, mistake, date, time, isCompleted, type));
            cursor.moveToNext();
        }
    }

    private void checkRecord(){
        if (items.isEmpty()) {
            noRecordContainer.setVisibility(View.VISIBLE);
            historyRecyclerView.setVisibility(View.GONE);
        } else {
            noRecordContainer.setVisibility(View.GONE);
            historyRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}