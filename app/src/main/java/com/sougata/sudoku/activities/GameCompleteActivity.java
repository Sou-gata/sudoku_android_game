package com.sougata.sudoku.activities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.sougata.Constants;
import com.sougata.GlobalStore;
import com.sougata.HelperFunctions;
import com.sougata.sudoku.Database;
import com.sougata.sudoku.R;
import com.sougata.sudoku.StartNewGame;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Calendar;


public class GameCompleteActivity extends AppCompatActivity {

    StartNewGame startNewGame = new StartNewGame(this);
    Button newGame;
    LinearLayout goToHome, saveToGallery, shareBtn;
    FrameLayout screenContainer;
    TextView gameDifficulty, gameTime, gameBestTime, gameLevel, gameMistake;
    Database db;
    GlobalStore globalStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);

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
        screenContainer = findViewById(R.id.fl_congratulation_screen);
        shareBtn = findViewById(R.id.ll_share);

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
            LinearLayout bs_restart = view1.findViewById(R.id.bs_difficulty_restart);

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
            bs_restart.setVisibility(View.GONE);
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
            }
        });

        goToHome.setOnClickListener(view -> {
            Intent intent;
            intent = new Intent(GameCompleteActivity.this, HomeActivity.class);
            if (globalStore.getType().equals(Constants.TYPES[1])) {
                intent.putExtra("isDaily", true);
                intent.putExtra("month", globalStore.getMonth());
                intent.putExtra("year", globalStore.getYear());
            } else if (globalStore.getType().equals(Constants.TYPES[2])) {
                intent = new Intent(GameCompleteActivity.this, EventActivity.class);
            }
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
            globalStore.emptyCurrentState();
        });
        saveToGallery = findViewById(R.id.ll_save);
        saveToGallery.setOnClickListener(view1 -> {
            LinearLayout board = generateBoard();
            screenContainer.addView(board, 0);
            board.post(() -> {
                Bitmap img = getBitmapFromView(board, board.getWidth(), board.getHeight());
                screenContainer.removeViewAt(0);
                Calendar c = Calendar.getInstance();
                String imageName = globalStore.getDifficultyName() + "_" + HelperFunctions.padString(globalStore.getCurrentLevel(), 4) + "_" + c.get(Calendar.YEAR) + "_" + HelperFunctions.padString(c.get(Calendar.MONTH), 2) + "_" + HelperFunctions.padString(c.get(Calendar.DAY_OF_MONTH), 2) + "_" + HelperFunctions.padString(c.get(Calendar.HOUR), 2) + "_" + HelperFunctions.padString(c.get(Calendar.MINUTE), 2) + "_" + HelperFunctions.padString(c.get(Calendar.SECOND), 2) + "_" + HelperFunctions.padString(c.get(Calendar.MILLISECOND), 3);
                saveImageUsingMediaStore(img, imageName, this.getContentResolver());
            });
        });
        shareBtn.setOnClickListener(v -> shareImage());
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
        if (!globalStore.getType().equals(Constants.TYPES[1])) {
            gameLevel.setText(String.valueOf(globalStore.getCurrentLevel()));
        } else {
            gameLevel.setText(getString(R.string.daily_challenge));
        }
        if (globalStore.getType().equals(Constants.TYPES[2])) {
            findViewById(R.id.ll_best_time).setVisibility(View.GONE);
        }
        gameMistake.setText(String.valueOf(globalStore.getMistakes()));
    }

    private void shareImage() {
        LinearLayout board = generateBoard();
        screenContainer.addView(board, 0);
        board.post(() -> {
            Bitmap img = getBitmapFromView(board, board.getWidth(), board.getHeight());
            screenContainer.removeViewAt(0);
            try {
                File cachePath = new File(getCacheDir(), "images");
                cachePath.mkdirs();
                Calendar c = Calendar.getInstance();
                String imageName = globalStore.getDifficultyName() + "_" + HelperFunctions.padString(globalStore.getCurrentLevel(), 4) + "_" + c.get(Calendar.YEAR) + "_" + HelperFunctions.padString(c.get(Calendar.MONTH), 2) + "_" + HelperFunctions.padString(c.get(Calendar.DAY_OF_MONTH), 2) + "_" + HelperFunctions.padString(c.get(Calendar.HOUR), 2) + "_" + HelperFunctions.padString(c.get(Calendar.MINUTE), 2) + "_" + HelperFunctions.padString(c.get(Calendar.SECOND), 2) + "_" + HelperFunctions.padString(c.get(Calendar.MILLISECOND), 3);
                File file = new File(cachePath, imageName + ".png");
                FileOutputStream stream = new FileOutputStream(file);
                img.compress(Bitmap.CompressFormat.PNG, 100, stream);
                stream.close();
                Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileProvider", file);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/png");
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.putExtra(Intent.EXTRA_TEXT, ContextCompat.getString(this, R.string.share_message));
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, "Share Image"));
            } catch (Exception ignored) {

            }
        });
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

    private LinearLayout generateBoard() {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int childHeight = (screenWidth - HelperFunctions.dpToPx(30) - 28) / 9;
        int childWidth = (screenWidth - HelperFunctions.dpToPx(30) - 28) / 9;

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout topContainer = new LinearLayout(this);
        topContainer.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, LinearLayout.LayoutParams.WRAP_CONTENT));
        topContainer.setBackgroundResource(R.drawable.share_board_bg);
        topContainer.setGravity(Gravity.CENTER);
        topContainer.setOrientation(LinearLayout.VERTICAL);
        int padding = HelperFunctions.dpToPx(15);
        topContainer.setPadding(0, padding, 0, padding);

        LinearLayout boardContainer = new LinearLayout(this);
        int boardWidth = screenWidth - HelperFunctions.dpToPx(30);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(boardWidth, boardWidth);
        params.setMargins(6, 0, 0, 0);
        boardContainer.setLayoutParams(params);
        boardContainer.setOrientation(LinearLayout.VERTICAL);
        int[][] board = globalStore.getBoard();

        for (int i = 0; i < 9; i++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            for (int j = 0; j < 9; j++) {
                LinearLayout child = new LinearLayout(this);
                LinearLayout.LayoutParams childParams = new LinearLayout.LayoutParams(childWidth, childHeight);
                child.setGravity(Gravity.CENTER);
                child.setBackground(createBorderDrawable());
                TextView textView = new TextView(this);
                textView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                if (board[i][j] != 0) {
                    textView.setText(String.valueOf(board[i][j]));
                }
                textView.setTextColor(Color.BLACK);
                int margin = 2;
                if (i % 3 == 0 && j % 3 == 0) {
                    childParams.setMargins(margin, margin, 0, 0);
                } else if (i % 3 == 2 && j % 3 == 2) {
                    childParams.setMargins(0, 0, margin, margin);
                } else if (i % 3 == 0 && j % 3 == 2) {
                    childParams.setMargins(0, margin, margin, 0);
                } else if (i % 3 == 2 && j % 3 == 0) {
                    childParams.setMargins(margin, 0, 0, margin);
                } else if (i % 3 == 1 && j % 3 == 0) {
                    childParams.setMargins(margin, 0, 0, 0);
                } else if (i % 3 == 0 && j % 3 == 1) {
                    childParams.setMargins(0, margin, 0, 0);
                } else if (i % 3 == 2 && j % 3 == 1) {
                    childParams.setMargins(0, 0, 0, margin);
                } else if (i % 3 == 1 && j % 3 == 2) {
                    childParams.setMargins(0, 0, margin, 0);
                }
                child.setLayoutParams(childParams);
                child.addView(textView);
                row.addView(child);
            }
            boardContainer.addView(row);
        }
        topContainer.addView(boardContainer);

        TextView shareNote = new TextView(this);
        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 10, 0, 0);
        shareNote.setLayoutParams(params);
        shareNote.setText(R.string.share_note);
        shareNote.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        shareNote.setTextColor(ContextCompat.getColor(this, R.color.white));
        shareNote.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

        topContainer.addView(shareNote);
        root.addView(topContainer);

        LinearLayout bottomContainer = new LinearLayout(this);
        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bottomContainer.setLayoutParams(params);
        bottomContainer.setBackgroundResource(R.drawable.share_board_bg_bottom);
        bottomContainer.setGravity(Gravity.CENTER_VERTICAL);
        bottomContainer.setOrientation(LinearLayout.HORIZONTAL);
        int paddingHorizontal = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
        int paddingVertical = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        bottomContainer.setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical);

        TextView bottomText = new TextView(this);
        params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        bottomText.setLayoutParams(params);
        bottomText.setText(R.string.keep_your_mind_sharp);
        bottomText.setTextColor(ContextCompat.getColor(this, R.color.white));
        bottomText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        bottomText.setTypeface(bottomText.getTypeface(), Typeface.BOLD);

        bottomContainer.addView(bottomText);

        ImageView logo = new ImageView(this);
        int dim = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
        params = new LinearLayout.LayoutParams(dim, dim);
        logo.setLayoutParams(params);
        logo.setContentDescription(getString(R.string.app_name));
        logo.setImageResource(R.drawable.app_icon);
        bottomContainer.addView(logo);
        root.addView(bottomContainer);

        return root;
    }

    private GradientDrawable createBorderDrawable() {
        GradientDrawable border2 = new GradientDrawable();
        border2.setStroke(1, ContextCompat.getColor(this, R.color.box_border_color));
        border2.setColor(Color.WHITE);
        return border2;
    }

    private Bitmap getBitmapFromView(View view, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

}