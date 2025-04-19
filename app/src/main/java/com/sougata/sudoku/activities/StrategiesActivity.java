package com.sougata.sudoku.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.sougata.HelperFunctions;
import com.sougata.sudoku.R;
import com.sougata.sudoku.Strategy;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class StrategiesActivity extends AppCompatActivity {
    ArrayList<Strategy> strategies = new ArrayList<>();
    ImageView backButton;
    LinearLayout llStrategiesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);

        setContentView(R.layout.activity_strategies);

        loadJsonFromAssets();
        backButton = findViewById(R.id.iv_strategies_back_button);
        llStrategiesContainer = findViewById(R.id.ll_strategies_container);

        backButton.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
        for (int i = 0; i < strategies.size(); i++) {
            llStrategiesContainer.addView(generateCardView(strategies.get(i)));
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
    }

    private void loadJsonFromAssets() {
        try {
            InputStream inputStream = getAssets().open("strategies.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            reader.close();
            JSONArray jsonArray = new JSONArray(jsonString.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                int id = obj.getInt("id");
                String title = obj.getString("title");
                JSONArray descriptionArr = obj.getJSONArray("description");
                JSONArray imagesArr = obj.getJSONArray("images");
                String[] description = new String[descriptionArr.length()];
                String[] images = new String[imagesArr.length()];
                for (int j = 0; j < descriptionArr.length(); j++) {
                    description[j] = descriptionArr.getString(j);
                }
                for (int j = 0; j < imagesArr.length(); j++) {
                    images[j] = imagesArr.getString(j);
                }
                strategies.add(new Strategy(id, title, description, images));
            }
        } catch (Exception ignored) {
        }
    }

    private CardView generateCardView(Strategy strategy) {
        CardView cardView = new CardView(this);
        LayoutParams cardParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(15, 10, 15, 10);
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(25);
        cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.statistic_card_bg));

        LinearLayout linearLayout = new LinearLayout(this);
        LayoutParams linearLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, HelperFunctions.dpToPx(70));
        linearLayout.setLayoutParams(linearLayoutParams);
        linearLayout.setPadding(50, 20, 50, 20);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(Gravity.CENTER_VERTICAL);

        TextView textView = new TextView(this);
        LayoutParams textParams = new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        textView.setLayoutParams(textParams);
        textView.setText(strategy.getTitle());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        textView.setTextColor(ContextCompat.getColor(this, R.color.black));

        TextView button = new TextView(this);
        LayoutParams buttonParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        button.setPadding(40, 20, 40, 20);
        button.setLayoutParams(buttonParams);
        button.setText(ContextCompat.getString(this, R.string.learn));
        button.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        button.setBackgroundResource(R.drawable.learn_btn_bg);

        linearLayout.setOnClickListener(v->{
            Intent intent = new Intent(this, StrategyActivity.class);
            intent.putExtra("title", strategy.getTitle());
            intent.putExtra("descriptions", strategy.getDescription());
            intent.putExtra("images", strategy.getImages());
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        linearLayout.addView(textView);
        linearLayout.addView(button);
        cardView.addView(linearLayout);

        return cardView;
    }
}