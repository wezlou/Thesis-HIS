package com.example.holyinfantschool;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class Categorypage extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private boolean isMuted = false;
    private ImageView volumeOn, volumeOff;
    private boolean settingsVisible = false; // toggle state

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categorypage);

        ImageView backTeacher = findViewById(R.id.backteacher); // arrow icon
        ImageView teacherSetting = findViewById(R.id.teachersetting);
        volumeOn = findViewById(R.id.volumeOn);
        volumeOff = findViewById(R.id.volumeOff);

        hideSettingsButtons();

        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        backTeacher.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
            Intent intent = new Intent(Categorypage.this, Homepage.class);
            startActivity(intent);
            finish();
        });

        teacherSetting.setOnClickListener(v -> {
            if (settingsVisible) {
                hideSettingsButtons();
            } else {
                showSettingsButtons();
            }
            settingsVisible = !settingsVisible;
        });

        volumeOn.setOnClickListener(v -> {
            mediaPlayer.setVolume(0, 0);
            isMuted = true;
            updateVolumeButtonsVisibility();
        });

        volumeOff.setOnClickListener(v -> {
            mediaPlayer.setVolume(1.0f, 1.0f);
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
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
