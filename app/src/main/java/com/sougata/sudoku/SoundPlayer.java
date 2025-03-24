package com.sougata.sudoku;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.VibrationEffect;
import android.os.Vibrator;

import com.sougata.GlobalStore;

import java.io.IOException;

public class SoundPlayer {
    private static final SoundPlayer instance = new SoundPlayer();
    private final MediaPlayer mediaPlayer = new MediaPlayer();
    GlobalStore globalStore = GlobalStore.getInstance();

    private SoundPlayer() {
    }

    public static SoundPlayer getInstance() {
        return instance;
    }

    public void playErase(Context context) {
        if (globalStore.getSound()) {
            mediaPlayer.reset();
            try {
                AssetFileDescriptor assetFileDescriptor = context.getAssets().openFd("sounds/erase.mp3");
                mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
                assetFileDescriptor.close();
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException ignored) {
            }
        }
    }

    public void playGameComplete(Context context) {
        if (!globalStore.getSound()) return;
        mediaPlayer.reset();
        try {
            AssetFileDescriptor assetFileDescriptor = context.getAssets().openFd("sounds/complete.mp3");
            mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
            assetFileDescriptor.close();
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException ignored) {
        }
    }

    public void playNotePlaced(Context context) {
        if (!globalStore.getSound()) return;
        mediaPlayer.reset();
        try {
            AssetFileDescriptor assetFileDescriptor = context.getAssets().openFd("sounds/note.mp3");
            mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
            assetFileDescriptor.close();
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException ignored) {
        }
    }

    public void playCorrect(Context context) {
        if (globalStore.getSound()) {
            mediaPlayer.reset();
            try {
                AssetFileDescriptor assetFileDescriptor = context.getAssets().openFd("sounds/correct.mp3");
                mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
                assetFileDescriptor.close();
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException ignored) {
            }
        }
        if (globalStore.isVibrate()) {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    public void playInvalid(Context context) {
        if (!globalStore.getSound()) return;
        mediaPlayer.reset();
        try {
            AssetFileDescriptor assetFileDescriptor = context.getAssets().openFd("sounds/invalid.mp3");
            mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
            assetFileDescriptor.close();
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException ignored) {
        }
    }

    public void playNoteSwitch(Context context) {
        if (!globalStore.getSound()) return;
        mediaPlayer.reset();
        try {
            AssetFileDescriptor assetFileDescriptor = context.getAssets().openFd("sounds/note_switch.mp3");
            mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
            assetFileDescriptor.close();
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException ignored) {
        }
    }

    public void playButtonClick(Context context) {
        if (!globalStore.getSound()) return;
        mediaPlayer.reset();
        try {
            AssetFileDescriptor assetFileDescriptor = context.getAssets().openFd("sounds/click_button.mp3");
            mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
            assetFileDescriptor.close();
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException ignored) {
        }
    }
}
