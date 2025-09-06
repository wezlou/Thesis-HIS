package com.example.holyinfantschool;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class QuizActivity extends AppCompatActivity {

    private ImageView btnColor, btnShape, btnNum, btnBack;
    private ImageView quizSetting, volumeOn, volumeOff;
    private MediaPlayer mediaPlayer;
    private boolean isMuted = false;
    private boolean settingsVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // Initialize buttons
        btnColor = findViewById(R.id.btncolor);
        btnShape = findViewById(R.id.btnshape);
        btnNum = findViewById(R.id.btnnum);
        btnBack = findViewById(R.id.btnback);

        // Settings & volume
        quizSetting = findViewById(R.id.quizsetting);
        volumeOn = findViewById(R.id.volumeOn);
        volumeOff = findViewById(R.id.volumeOff);

        hideSettingsButtons();

        // Setup background music
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        // Navigate to ColorActivity
        btnColor.setOnClickListener(v -> {
            stopMusic();
            startActivity(new Intent(QuizActivity.this, ColorActivity.class));
        });

        // Navigate to ShapeActivity
        btnShape.setOnClickListener(v -> {
            stopMusic();
            startActivity(new Intent(QuizActivity.this, ShapeActivity.class));
        });

        // Navigate to NumberActivity
        btnNum.setOnClickListener(v -> {
            stopMusic();
            startActivity(new Intent(QuizActivity.this, NumberActivity.class));
        });

        // Go back to CategoryPageActivity
        btnBack.setOnClickListener(v -> {
            stopMusic();
            startActivity(new Intent(QuizActivity.this, Categorypage.class));
            finish();
        });

        // Settings toggle
        quizSetting.setOnClickListener(v -> {
            if (settingsVisible) {
                hideSettingsButtons();
            } else {
                showSettingsButtons();
            }
            settingsVisible = !settingsVisible;
        });

        // Mute
        volumeOn.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(0, 0);
            }
            isMuted = true;
            updateVolumeButtonsVisibility();
        });

        // Unmute
        volumeOff.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(1.0f, 1.0f);
            }
            isMuted = false;
            updateVolumeButtonsVisibility();
        });
    }

    private void hideSettingsButtons() {
        volumeOn.setVisibility(View.GONE);
        volumeOff.setVisibility(View.GONE);
    }

    private void showSettingsButtons() {
        updateVolumeButtonsVisibility();
    }

    private void updateVolumeButtonsVisibility() {
        if (isMuted) {
            volumeOff.setVisibility(View.VISIBLE);
            volumeOn.setVisibility(View.GONE);
        } else {
            volumeOn.setVisibility(View.VISIBLE);
            volumeOff.setVisibility(View.GONE);
        }
    }

    private void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null && !isMuted && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMusic();
    }
}
