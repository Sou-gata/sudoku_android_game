package com.sougata.sudoku.activities;

import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sougata.Constants;
import com.sougata.GlobalStore;
import com.sougata.sudoku.R;
import com.sougata.sudoku.fragments.DailyFragment;
import com.sougata.sudoku.fragments.MainFragment;
import com.sougata.sudoku.fragments.StatisticsFragment;

public class HomeActivity extends AppCompatActivity {
    LinearLayout tabContainer;
    int currentPosition = 0;
    GlobalStore globalStore = GlobalStore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);

        setContentView(R.layout.activity_home);
        tabContainer = findViewById(R.id.ll_tab_container);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);

        ViewTreeObserver viewTreeObserver = bottomNavigationView.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Display display = getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    DisplayMetrics metrics = new DisplayMetrics();
                    display.getRealMetrics(metrics);
                    int realScreenHeight = metrics.heightPixels;
                    bottomNavigationView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    int height = bottomNavigationView.getHeight();
                    LinearLayout.LayoutParams childParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, realScreenHeight - height);
                    tabContainer.setLayoutParams(childParams);
                }
            });
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // TODO: open exit confirm popup
            }
        });

        loadSettings();

        replaceFragment(new MainFragment(), currentPosition);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int position = getMenuItemPosition(bottomNavigationView, item.getItemId());
            if (item.getItemId() == R.id.bm_main) {
                replaceFragment(new MainFragment(), position);
            } else if (item.getItemId() == R.id.bm_daily) {
                replaceFragment(new DailyFragment(), position);
            } else if (item.getItemId() == R.id.bm_statistic) {
                replaceFragment(new StatisticsFragment(), position);
            }
            return true;
        });
    }

    private void replaceFragment(Fragment fragment, int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (position > currentPosition) {
            fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
        } else if (position < currentPosition) {
            fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_left);
        }

        fragmentTransaction.replace(R.id.ll_tab_container, fragment);
        fragmentTransaction.commit();

        currentPosition = position;
    }


    private int getMenuItemPosition(BottomNavigationView bottomNavigationView, int itemId) {
        Menu menu = bottomNavigationView.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            if (menu.getItem(i).getItemId() == itemId) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onRestart() {
        super.onRestart();
        loadSettings();
    }

    private void loadSettings() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREF_NAME, MODE_PRIVATE);
        globalStore.setMistakeLimit(sharedPreferences.getInt("mistakeLimit", Constants.ALLOWED_MISTAKES));
        globalStore.setSound(sharedPreferences.getBoolean("sound", Constants.SOUND));
        globalStore.setVibrate(sharedPreferences.getBoolean("vibrate", Constants.VIBRATE));
        globalStore.setAutoRemoveNotes(sharedPreferences.getBoolean("removeNotes", Constants.REMOVE_NOTES));
        globalStore.setNumbersHighlight(sharedPreferences.getBoolean("numbersHighlight", Constants.HIGHLIGHT_NUMBERS));
        globalStore.setRegionHighlight(sharedPreferences.getBoolean("regionHighlight", Constants.HIGHLIGHT_REGION));
        globalStore.setAdvanceNoteEnable(sharedPreferences.getBoolean("advanceNote", true));
    }
}