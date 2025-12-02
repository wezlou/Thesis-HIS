package com.example.holyinfantschool;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class Categorypage extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private boolean isMuted = false;
    private ImageView volumeOn, volumeOff;
    private boolean settingsVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categorypage);

        ImageView backTeacher = findViewById(R.id.backteacher); // LOGOUT
        ImageView teacherSetting = findViewById(R.id.teachersetting);
        ImageView watchVideos = findViewById(R.id.watch_videos);
        ImageView readStories = findViewById(R.id.read_stories);
        ImageView playQuiz = findViewById(R.id.play_quiz);
        ImageView announcement = findViewById(R.id.announcement);
        volumeOn = findViewById(R.id.volumeOn);
        volumeOff = findViewById(R.id.volumeOff);

        hideSettingsButtons();

        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        // LOGOUT BUTTON
        backTeacher.setOnClickListener(v -> {

            // 1. Firebase signout
            FirebaseAuth.getInstance().signOut();

            // 2. Remove saved role
            getSharedPreferences("HIS_APP", MODE_PRIVATE)
                    .edit()
                    .remove("user_role")
                    .apply();

            // 3. Stop music
            stopMusic();

            // 4. Redirect to homepage
            Intent intent = new Intent(Categorypage.this, Homepage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish();
        });

        // Settings toggle
        teacherSetting.setOnClickListener(v -> {
            if (settingsVisible) {
                hideSettingsButtons();
            } else {
                showSettingsButtons();
            }
            settingsVisible = !settingsVisible;
        });

        // Volume control
        volumeOn.setOnClickListener(v -> {
            mediaPlayer.setVolume(0, 0);
            isMuted = true;
            updateVolumeButtonsVisibility();
        });

        volumeOff.setOnClickListener(v -> {
            mediaPlayer.setVolume(1, 1);
            isMuted = false;
            updateVolumeButtonsVisibility();
        });

        // Navigation buttons
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
