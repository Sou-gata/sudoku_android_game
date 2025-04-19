package com.sougata.sudoku.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.sougata.sudoku.R;
import com.sougata.sudoku.fragments.DailyAwardFragment;
import com.sougata.sudoku.fragments.EventAwardFragment;

public class AwardsActivity extends AppCompatActivity {

    ImageView backButton;
    LinearLayout dailyAwards, eventAwards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);

        setContentView(R.layout.activity_awards);

        backButton = findViewById(R.id.iv_awards_back_button);
        dailyAwards = findViewById(R.id.ll_awards_daily);
        eventAwards = findViewById(R.id.ll_awards_event);

        backButton.setOnClickListener((View view) -> {
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

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, new DailyAwardFragment(this));
        fragmentTransaction.commit();

        dailyAwards.setOnClickListener(v -> {
            dailyAwards.setBackgroundResource(R.drawable.awards_toggle_bg);
            eventAwards.setBackground(null);
            TextView tv = (TextView) eventAwards.getChildAt(0);
            tv.setTextColor(ContextCompat.getColor(this, R.color.gray));
            tv = (TextView) dailyAwards.getChildAt(0);
            tv.setTextColor(ContextCompat.getColor(this, R.color.black));

            FragmentTransaction fragmentTransaction2 = getSupportFragmentManager().beginTransaction();
            fragmentTransaction2.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
            fragmentTransaction2.replace(R.id.fragment_container, new DailyAwardFragment(this));
            fragmentTransaction2.commit();
        });

        eventAwards.setOnClickListener(v -> {
            eventAwards.setBackgroundResource(R.drawable.awards_toggle_bg);
            dailyAwards.setBackground(null);
            TextView tv = (TextView) dailyAwards.getChildAt(0);
            tv.setTextColor(ContextCompat.getColor(this, R.color.gray));
            tv = (TextView) eventAwards.getChildAt(0);
            tv.setTextColor(ContextCompat.getColor(this, R.color.black));

            FragmentTransaction fragmentTransaction2 = getSupportFragmentManager().beginTransaction();
            fragmentTransaction2.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
            fragmentTransaction2.replace(R.id.fragment_container, new EventAwardFragment(this));
            fragmentTransaction2.commit();
        });
    }
}