package com.sougata.sudoku.activities;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.sougata.HelperFunctions;
import com.sougata.sudoku.R;

import java.util.Objects;

public class StrategyActivity extends AppCompatActivity {
    TextView strategyTitle;
    ImageView backButton;
    LinearLayout strategyContainer;
    String baseURL = "https://raw.githubusercontent.com/Sou-gata/sudoku_android_game/refs/heads/main/strategies/";
    int screenWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);

        setContentView(R.layout.activity_strategy);
        Intent intent = getIntent();

        strategyTitle = findViewById(R.id.tv_strategy_title);
        backButton = findViewById(R.id.iv_strategy_back_button);
        strategyContainer = findViewById(R.id.ll_strategy_container);

        strategyTitle.setText(intent.getStringExtra("title"));
        backButton.setOnClickListener(v -> {
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

        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x - HelperFunctions.dpToPx(30);
        generateLayout(Objects.requireNonNull(intent.getStringArrayExtra("descriptions")), intent.getStringArrayExtra("images"));
    }

    private void generateLayout(String[] descriptions, String[] images) {
        strategyContainer.addView(generateTextView(descriptions[0], false));
        if (descriptions.length == 2) {
            for (String image : images) {
                strategyContainer.addView(generateImageView(image));
            }
            strategyContainer.addView(generateTextView(descriptions[1], true));
        } else if (descriptions.length == 3) {
            strategyContainer.addView(generateImageView(images[0]));
            strategyContainer.addView(generateTextView(descriptions[1], true));
            strategyContainer.addView(generateImageView(images[1]));
            strategyContainer.addView(generateTextView(descriptions[2], true));
        }
    }

    private TextView generateTextView(String text, boolean isMargin) {
        TextView tv = new TextView(this);
        if (isMargin) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 20, 0, 20);
            tv.setLayoutParams(layoutParams);
        }
        tv.setText(text);
        tv.setTextSize(16);
        tv.setLineSpacing(10f, 1.2f);
        tv.setTextColor(ContextCompat.getColor(this, R.color.black));
        return tv;
    }

    private ImageView generateImageView(String image) {
        ImageView imageView = new ImageView(this);
        Glide.with(this).load(baseURL + image + ".png").fitCenter().placeholder(R.drawable.placeholder).error(R.drawable.placeholder).into(imageView);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, screenWidth - 40);
        layoutParams.setMargins(20, 20, 20, 20);
        imageView.setLayoutParams(layoutParams);
        return imageView;
    }
}