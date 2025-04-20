package com.sougata.sudoku.activities;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieCompositionFactory;
import com.airbnb.lottie.LottieDrawable;
import com.sougata.Constants;
import com.sougata.GlobalStore;
import com.sougata.sudoku.R;

@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends AppCompatActivity {
    LinearLayout parent;
    GlobalStore globalStore = GlobalStore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);

        loadSettings();

        setContentView(R.layout.activity_splash_screen);

        if (globalStore.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        parent = findViewById(R.id.ll_splash_screen);

        LottieDrawable drawable = new LottieDrawable();
        drawable.setImagesAssetsFolder("images/");
        LottieAnimationView animationView = findViewById(R.id.lv_splash_screen);

        LottieCompositionFactory.fromAsset(this, "app_icon_anim.json")
                .addListener(composition -> {
                    drawable.setComposition(composition);
                    animationView.setImageDrawable(drawable);
                    drawable.setSpeed(0.75f);
                    drawable.playAnimation();
                });

        parent.post(() -> startZoomInAnimation(1));

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }, Constants.SPLASH_DELAY);
    }

    private void startZoomInAnimation(int idx) {
        final int[] currentIndex = {1};
        if (currentIndex[0] < parent.getChildCount()) {
            Animation zoomIn = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
            zoomIn.setDuration(750);

            zoomIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (idx + 1 < parent.getChildCount()) {
                        startZoomInAnimation(idx + 1);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            TextView textView = (TextView) parent.getChildAt(idx);
            textView.setVisibility(View.VISIBLE);
            textView.startAnimation(zoomIn);
        }
    }

    private void loadSettings() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREF_NAME, MODE_PRIVATE);
        globalStore.setMistakeLimit(sharedPreferences.getInt("mistakeLimit", Constants.ALLOWED_MISTAKES));
        globalStore.setSound(sharedPreferences.getBoolean("sound", Constants.SOUND));
        globalStore.setVibrate(sharedPreferences.getBoolean("vibrate", Constants.VIBRATE));
        globalStore.setAutoRemoveNotes(sharedPreferences.getBoolean("removeNotes", Constants.REMOVE_NOTES));
        globalStore.setNumbersHighlight(sharedPreferences.getBoolean("numbersHighlight", Constants.HIGHLIGHT_NUMBERS));
        globalStore.setRegionHighlight(sharedPreferences.getBoolean("regionHighlight", Constants.HIGHLIGHT_REGION));
        globalStore.setAdvanceNoteEnable(sharedPreferences.getBoolean("advanceNote", Constants.ADVANCE_NOTE));
        globalStore.setHighLightNotes(sharedPreferences.getBoolean("highLightNotes", Constants.HIGHLIGHT_NOTES));
        globalStore.setHintsCount(sharedPreferences.getInt("hintsCount", 2));
        globalStore.setAdvanceNoteCount(sharedPreferences.getFloat("advanceNoteCount", 1));
        globalStore.setDarkMode(sharedPreferences.getBoolean("isDarkMode", Constants.DARK_MODE));
    }
}