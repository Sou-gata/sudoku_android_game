package com.sougata.sudoku.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.sougata.Constants;
import com.sougata.GlobalStore;
import com.sougata.sudoku.R;

public class SettingsActivity extends AppCompatActivity {

    LinearLayout playGuide, statistic, history, llSound, llVibrate, llRemoveNotes, llHighlightNumbers, llHighlightRegion, llMistakeLimit, llAdvanceNote, llStrategies, llHighlightNotes;
    ImageView backButton;
    MaterialSwitch msSound, msVibrate, msRemoveNotes, msHighlightNumbers, msHighlightRegion, msAdvanceNote, msHighlightNotes;
    GlobalStore globalStore = GlobalStore.getInstance();
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);

        setContentView(R.layout.activity_settings);

        editor = getSharedPreferences(Constants.SHARED_PREF_NAME, MODE_PRIVATE).edit();

        playGuide = findViewById(R.id.ll_settings_play_guide);
        backButton = findViewById(R.id.iv_settings_back_button);
        statistic = findViewById(R.id.ll_settings_statistic);
        history = findViewById(R.id.ll_settings_history);
        llSound = findViewById(R.id.ll_settings_sound);
        llVibrate = findViewById(R.id.ll_settings_vibrate);
        llRemoveNotes = findViewById(R.id.ll_settings_remove_notes);
        llHighlightNumbers = findViewById(R.id.ll_settings_number_highlight);
        llHighlightRegion = findViewById(R.id.ll_settings_region_highlight);
        msSound = findViewById(R.id.ms_settings_sound);
        msVibrate = findViewById(R.id.ms_settings_vibrate);
        msRemoveNotes = findViewById(R.id.ms_settings_remove_notes);
        msHighlightNumbers = findViewById(R.id.ms_settings_number_highlight);
        msHighlightRegion = findViewById(R.id.ms_settings_region_highlight);
        llMistakeLimit = findViewById(R.id.ll_settings_mistake_limit);
        llAdvanceNote = findViewById(R.id.ll_settings_advance_note);
        msAdvanceNote = findViewById(R.id.ms_settings_advance_note);
        llStrategies = findViewById(R.id.ll_settings_strategies);
        llHighlightNotes = findViewById(R.id.ll_settings_notes_highlight);
        msHighlightNotes = findViewById(R.id.ms_settings_notes_highlight);

        msSound.setClickable(false);
        msVibrate.setClickable(false);
        msRemoveNotes.setClickable(false);
        msHighlightNumbers.setClickable(false);
        msHighlightRegion.setClickable(false);
        msAdvanceNote.setClickable(false);
        msHighlightNotes.setClickable(false);

        msSound.setChecked(globalStore.getSound());
        msVibrate.setChecked(globalStore.isVibrate());
        msRemoveNotes.setChecked(globalStore.getAutoRemoveNotes());
        msHighlightNumbers.setChecked(globalStore.getNumbersHighlight());
        msHighlightRegion.setChecked(globalStore.getRegionHighlight());
        msAdvanceNote.setChecked(globalStore.isAdvanceNoteEnable());
        msHighlightNotes.setChecked(globalStore.isHighLightNotes());

        backButton.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
        playGuide.setOnClickListener(v -> {
            startActivity(new Intent(this, PlayGuideActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        statistic.setOnClickListener(v -> {
            startActivity(new Intent(this, StatisticsActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        history.setOnClickListener(v -> {
            startActivity(new Intent(this, HistoryActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        llSound.setOnClickListener(v -> {
            boolean isSoundChecked = !msSound.isChecked();
            msSound.setChecked(isSoundChecked);
            editor.putBoolean("sound", isSoundChecked);
            editor.apply();
            globalStore.setSound(isSoundChecked);
        });
        llVibrate.setOnClickListener(v -> {
            boolean isVibrateChecked = !msVibrate.isChecked();
            msVibrate.setChecked(isVibrateChecked);
            editor.putBoolean("vibrate", isVibrateChecked);
            editor.apply();
            globalStore.setVibrate(isVibrateChecked);
        });
        llRemoveNotes.setOnClickListener(v -> {
            boolean isRemoveNotesChecked = !msRemoveNotes.isChecked();
            msRemoveNotes.setChecked(isRemoveNotesChecked);
            editor.putBoolean("removeNotes", isRemoveNotesChecked);
            editor.apply();
            globalStore.setAutoRemoveNotes(isRemoveNotesChecked);
        });
        llHighlightNumbers.setOnClickListener(v -> {
            boolean isHighlightNumbersChecked = !msHighlightNumbers.isChecked();
            msHighlightNumbers.setChecked(isHighlightNumbersChecked);
            editor.putBoolean("numbersHighlight", isHighlightNumbersChecked);
            editor.apply();
            globalStore.setNumbersHighlight(isHighlightNumbersChecked);
        });
        llHighlightNotes.setOnClickListener(v -> {
            boolean isHighlightNotesChecked = !msHighlightNotes.isChecked();
            msHighlightNotes.setChecked(isHighlightNotesChecked);
            editor.putBoolean("highLightNotes", isHighlightNotesChecked);
            editor.apply();
            globalStore.setHighLightNotes(isHighlightNotesChecked);
        });
        llHighlightRegion.setOnClickListener(v -> {
            boolean isHighlightRegionChecked = !msHighlightRegion.isChecked();
            msHighlightRegion.setChecked(isHighlightRegionChecked);
            editor.putBoolean("regionHighlight", isHighlightRegionChecked);
            editor.apply();
            globalStore.setRegionHighlight(isHighlightRegionChecked);
        });
        llAdvanceNote.setOnClickListener(v -> {
            boolean isAdvanceNoteChecked = !msAdvanceNote.isChecked();
            msAdvanceNote.setChecked(isAdvanceNoteChecked);
            editor.putBoolean("advanceNote", isAdvanceNoteChecked);
            editor.apply();
            globalStore.setAdvanceNoteEnable(isAdvanceNoteChecked);
        });
        llMistakeLimit.setOnClickListener(this::openPopup);
        llStrategies.setOnClickListener(v -> {
            startActivity(new Intent(this, StrategiesActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void openPopup(View view) {
        LinearLayout overlay = findViewById(R.id.ll_settings_overlay);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.mistake_limit_popup, null);
        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setAnimationStyle(R.style.popupDialogAnim);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        overlay.setVisibility(View.VISIBLE);

        RadioButton rb3 = popupView.findViewById(R.id.rb_mistake_3);
        RadioButton rb5 = popupView.findViewById(R.id.rb_mistake_5);
        RadioButton rb10 = popupView.findViewById(R.id.rb_mistake_10);
        TextView okBtn = popupView.findViewById(R.id.tv_mistake_limit_popup_ok);

        if (globalStore.getMistakeLimit() == 3) {
            rb3.setChecked(true);
        } else if (globalStore.getMistakeLimit() == 5) {
            rb5.setChecked(true);
        } else {
            rb10.setChecked(true);
        }

        popupWindow.setOnDismissListener(() -> {
            overlay.setVisibility(View.GONE);
        });
        TextView tvCancel = popupView.findViewById(R.id.tv_mistake_limit_popup_cancel);
        tvCancel.setOnClickListener(v -> {
            popupWindow.dismiss();
        });
        okBtn.setOnClickListener(v -> {
            int m;
            if (rb3.isChecked()) {
                editor.putInt("mistakeLimit", 3);
                m = 3;
            } else if (rb5.isChecked()) {
                editor.putInt("mistakeLimit", 5);
                m = 5;
            } else {
                editor.putInt("mistakeLimit", 10);
                m = 10;
            }
            editor.apply();
            globalStore.setMistakeLimit(m);
            popupWindow.dismiss();
        });
    }

}