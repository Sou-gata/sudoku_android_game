package com.sougata.sudoku.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.sougata.GlobalStore;
import com.sougata.HelperFunctions;
import com.sougata.sudoku.Database;
import com.sougata.sudoku.R;
import com.sougata.sudoku.StartNewGame;
import com.sougata.sudoku.activities.GameActivity;

public class MainFragment extends Fragment {

    Button newGame;
    LinearLayout resumeGame;
    TextView resumeStatus;
    GlobalStore globalStore;
    Database db;
    StartNewGame startNewGame;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOngoingDb();
    }

    @Override
    public void onStart() {
        super.onStart();
        loadOngoingDb();
    }

    private void loadOngoingDb() {
        Cursor c = db.getOngoing();
        c.moveToFirst();
        if (!c.getString(3).equals("none")) {
            globalStore.setCurrentLevel(c.getInt(1));
            globalStore.setDifficulty(c.getInt(2));
            globalStore.setDifficultyName(c.getString(3));
            globalStore.setTimer(c.getInt(4));
            globalStore.setCurrentBoardState(HelperFunctions.parseTwoDimArray(c.getString(5)));
            globalStore.setBoard(HelperFunctions.parseTwoDimArray(c.getString(6)));
            globalStore.setSolution(HelperFunctions.parseTwoDimArray(c.getString(7)));
            globalStore.setMistakes(c.getInt(9));
            globalStore.setType(c.getString(10));

            resumeGame.setVisibility(View.VISIBLE);
            String resumeStatusText;
            if (!globalStore.getType().equals("daily")) {
                resumeStatusText = HelperFunctions.timerToString(globalStore.getTimer()) + " - " + globalStore.getDifficultyName();
            } else {
                resumeStatusText = HelperFunctions.timerToString(globalStore.getTimer()) + " - Daily challenge";
            }
            resumeStatus.setText(resumeStatusText);
        } else {
            resumeGame.setVisibility(View.GONE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        globalStore = GlobalStore.getInstance();
        db = new Database(getContext());
        startNewGame = new StartNewGame(getContext());
        Context context = getContext();
        newGame = view.findViewById(R.id.btn_home_new_game);
        resumeGame = view.findViewById(R.id.ll_home_resume_game);
        resumeStatus = view.findViewById(R.id.tv_main_resume_status);

        Intent intent = new Intent(context, GameActivity.class);
        db.createFirstOngoing();
        loadOngoingDb();

        newGame.setOnClickListener(view3 -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
            View view1 = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_select_difficulty, null);
            bottomSheetDialog.setContentView(view1);
            bottomSheetDialog.show();
            LinearLayout bs_difficulty_easy = view1.findViewById(R.id.bs_difficulty_easy);
            LinearLayout bs_difficulty_medium = view1.findViewById(R.id.bs_difficulty_medium);
            LinearLayout bs_difficulty_hard = view1.findViewById(R.id.bs_difficulty_hard);
            LinearLayout bs_difficulty_expert = view1.findViewById(R.id.bs_difficulty_expert);
            LinearLayout bs_difficulty_nightmare = view1.findViewById(R.id.bs_difficulty_nightmare);
            bs_difficulty_easy.setOnClickListener(view2 -> {
                startNewGame.createEasyGame();
                startActivity(intent);
                bottomSheetDialog.cancel();
            });
            bs_difficulty_medium.setOnClickListener(view2 -> {
                startNewGame.createMediumGame();
                startActivity(intent);
                bottomSheetDialog.cancel();
            });
            bs_difficulty_hard.setOnClickListener(view2 -> {
                startNewGame.createHardGame();
                startActivity(intent);
                bottomSheetDialog.cancel();
            });
            bs_difficulty_expert.setOnClickListener(view2 -> {
                startNewGame.createExpertGame();
                startActivity(intent);
                bottomSheetDialog.cancel();

            });
            bs_difficulty_nightmare.setOnClickListener(view2 -> {
                startNewGame.createNightmareGame();
                startActivity(intent);
                bottomSheetDialog.cancel();
            });
        });

        resumeGame.setOnClickListener(view2 ->
                startActivity(intent)
        );

        return view;
    }
}