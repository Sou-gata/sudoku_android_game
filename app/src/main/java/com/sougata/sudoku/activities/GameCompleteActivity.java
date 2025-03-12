
package com.sougata.sudoku.activities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.sougata.Constants;
import com.sougata.GlobalStore;
import com.sougata.HelperFunctions;
import com.sougata.sudoku.Database;
import com.sougata.sudoku.R;
import com.sougata.sudoku.StartNewGame;

import java.io.OutputStream;
import java.util.Calendar;


public class GameCompleteActivity extends AppCompatActivity {

    StartNewGame startNewGame = new StartNewGame(this);
    Button newGame;
    LinearLayout goToHome, saveToGallery;
    TextView gameDifficulty, gameTime, gameBestTime, gameLevel, gameMistake;
    Database db;
    GlobalStore globalStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game_complete);

        db = new Database(this);
        globalStore = GlobalStore.getInstance();

        newGame = findViewById(R.id.btn_new_game);
        goToHome = findViewById(R.id.ll_go_home);
        gameDifficulty = findViewById(R.id.tv_game_difficulty);
        gameTime = findViewById(R.id.tv_game_timer);
        gameBestTime = findViewById(R.id.tv_game_best_time);
        gameLevel = findViewById(R.id.tv_game_level);
        gameMistake = findViewById(R.id.tv_game_mistake);

        loadData();

        newGame.setOnClickListener(view -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(GameCompleteActivity.this);
            View view1 = LayoutInflater.from(GameCompleteActivity.this).inflate(R.layout.bottom_sheet_select_difficulty, null);
            bottomSheetDialog.setContentView(view1);
            bottomSheetDialog.show();
            LinearLayout bs_difficulty_easy = view1.findViewById(R.id.bs_difficulty_easy);
            LinearLayout bs_difficulty_medium = view1.findViewById(R.id.bs_difficulty_medium);
            LinearLayout bs_difficulty_hard = view1.findViewById(R.id.bs_difficulty_hard);
            LinearLayout bs_difficulty_expert = view1.findViewById(R.id.bs_difficulty_expert);
            LinearLayout bs_difficulty_nightmare = view1.findViewById(R.id.bs_difficulty_nightmare);

            Intent intent = new Intent(GameCompleteActivity.this, GameActivity.class);
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

        goToHome.setOnClickListener(view -> {
            Intent intent = new Intent(GameCompleteActivity.this, HomeActivity.class);
            startActivity(intent);
            globalStore.emptyCurrentState();
        });
        saveToGallery = findViewById(R.id.ll_save);
        saveToGallery.setOnClickListener(view1 -> {
            View rootView = findViewById(R.id.fl_congratulation_screen);
            Calendar c = Calendar.getInstance();
            String imageName = globalStore.getDifficultyName() + "_" +
                    HelperFunctions.padString(globalStore.getCurrentLevel(), 4) + "_" +
                    c.get(Calendar.YEAR) + "_" +
                    HelperFunctions.padString(c.get(Calendar.MONTH), 2) + "_" +
                    HelperFunctions.padString(c.get(Calendar.DAY_OF_MONTH), 2) + "_" +
                    HelperFunctions.padString(c.get(Calendar.HOUR), 2) + "_" +
                    HelperFunctions.padString(c.get(Calendar.MINUTE), 2) + "_" +
                    HelperFunctions.padString(c.get(Calendar.SECOND), 2) + "_" +
                    HelperFunctions.padString(c.get(Calendar.MILLISECOND), 3);
            Bitmap image = getScreenShot(rootView);
            saveImageUsingMediaStore(image, imageName, getContentResolver());
        });
    }

    public void loadData() {
        Cursor cursor = db.getCompleted(globalStore.getDifficultyName(), globalStore.getType());
        int minTimer = Integer.MAX_VALUE;
        if (cursor.getCount() == 0) {
            minTimer = globalStore.getTimer();
        } else {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                if (minTimer > cursor.getInt(4)) {
                    minTimer = cursor.getInt(4);
                }
                cursor.moveToNext();
            }
        }
        gameDifficulty.setText(globalStore.getDifficultyName());
        gameTime.setText(HelperFunctions.timerToString(globalStore.getTimer()));
        gameBestTime.setText(HelperFunctions.timerToString(minTimer));
        if (globalStore.getType().equals(Constants.TYPES[0])) {
            gameLevel.setText(String.valueOf(globalStore.getCurrentLevel()));
        } else {
            gameLevel.setText(getString(R.string.daily_challenge));
        }
        gameMistake.setText(String.valueOf(globalStore.getMistakes()));
    }

    public static Bitmap getScreenShot(View view) {
        View screenView = view.getRootView();
        screenView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
        screenView.setDrawingCacheEnabled(false);
        return bitmap;
    }

    public void saveImageUsingMediaStore(Bitmap bitmap, String fileName, ContentResolver resolver) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName + ".png");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Constants.SAVE_LOCATION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.IS_PENDING, 1);
        }

        try {
            android.net.Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (imageUri != null) {
                OutputStream fos = resolver.openOutputStream(imageUri);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear();
                    values.put(MediaStore.Images.Media.IS_PENDING, 0);
                    resolver.update(imageUri, values, null, null);
                    Toast.makeText(this, "Image saved to gallery", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Field to save image", Toast.LENGTH_SHORT).show();
        }
    }
}