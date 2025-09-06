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

        // Initialize views
        ImageView backTeacher = findViewById(R.id.backteacher);
        ImageView teacherSetting = findViewById(R.id.teachersetting);
        ImageView watchVideos = findViewById(R.id.watch_videos);
        ImageView readStories = findViewById(R.id.read_stories);
        ImageView playQuiz = findViewById(R.id.play_quiz);
        ImageView announcement = findViewById(R.id.announcement);
        volumeOn = findViewById(R.id.volumeOn);
        volumeOff = findViewById(R.id.volumeOff);

        // Hide volume buttons at start
        hideSettingsButtons();

        // Setup music
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        // Back button → Homepage
        backTeacher.setOnClickListener(v -> {
            stopMusic();
            Intent intent = new Intent(Categorypage.this, Homepage.class);
            startActivity(intent);
            finish();
        });

        // Settings button
        teacherSetting.setOnClickListener(v -> {
            if (settingsVisible) {
                hideSettingsButtons();
            } else {
                showSettingsButtons();
            }
            settingsVisible = !settingsVisible;
        });

        // Volume On → mute
        volumeOn.setOnClickListener(v -> {
            mediaPlayer.setVolume(0, 0);
            isMuted = true;
            updateVolumeButtonsVisibility();
        });

        // Volume Off → unmute
        volumeOff.setOnClickListener(v -> {
            mediaPlayer.setVolume(1.0f, 1.0f);
            isMuted = false;
            updateVolumeButtonsVisibility();
        });

        // --- Other functions ---
        watchVideos.setOnClickListener(v -> {
            stopMusic();
            startActivity(new Intent(Categorypage.this, VideosActivity.class));
        });

        readStories.setOnClickListener(v -> {
            stopMusic();
            startActivity(new Intent(Categorypage.this, StoriesActivity.class));
        });

        playQuiz.setOnClickListener(v -> {
            stopMusic();
            startActivity(new Intent(Categorypage.this, QuizActivity.class));
        });

        announcement.setOnClickListener(v -> {
            stopMusic();
            startActivity(new Intent(Categorypage.this, splashstudenttask.class));
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
