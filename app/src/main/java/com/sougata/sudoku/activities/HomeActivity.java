package com.sougata.sudoku.activities;

import android.os.Bundle;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sougata.sudoku.R;
import com.sougata.sudoku.fragments.DailyFragment;
import com.sougata.sudoku.fragments.MainFragment;
import com.sougata.sudoku.fragments.StatisticsFragment;

public class HomeActivity extends AppCompatActivity {
    LinearLayout tabContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        View decorView = getWindow().getDecorView();
//        decorView.setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home);
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        tabContainer = findViewById(R.id.ll_tab_container);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);

        ViewTreeObserver viewTreeObserver = bottomNavigationView.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    bottomNavigationView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    int height = bottomNavigationView.getHeight();
                    LinearLayout.LayoutParams childParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, screenHeight - height);
                    tabContainer.setLayoutParams(childParams);
                }
            });
        }

        replaceFragment(new MainFragment());
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bm_main) {
                replaceFragment(new MainFragment());
            } else if (item.getItemId() == R.id.bm_daily) {
                replaceFragment(new DailyFragment());
            } else if (item.getItemId() == R.id.bm_statistic) {
                replaceFragment(new StatisticsFragment());
            }
            return true;
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
        fragmentTransaction.replace(R.id.ll_tab_container, fragment);
        fragmentTransaction.commit();
    }
}