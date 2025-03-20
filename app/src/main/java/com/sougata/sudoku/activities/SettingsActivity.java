package com.sougata.sudoku.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.sougata.sudoku.R;

public class SettingsActivity extends AppCompatActivity {

    LinearLayout playGuide, statistic, history;
    ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);

        setContentView(R.layout.activity_settings);

        playGuide = findViewById(R.id.ll_settings_play_guide);
        backButton = findViewById(R.id.iv_settings_back_button);
        statistic = findViewById(R.id.ll_settings_statistic);
        history = findViewById(R.id.ll_settings_history);


        backButton.setOnClickListener(v -> {
            finish();
        });
        playGuide.setOnClickListener(v -> {
            startActivity(new Intent(this, PlayGuideActivity.class));
        });
        statistic.setOnClickListener(v -> {
            startActivity(new Intent(this, StatisticsActivity.class));
        });
        history.setOnClickListener(v -> {
            startActivity(new Intent(this, HistoryActivity.class));
        });

    }
}