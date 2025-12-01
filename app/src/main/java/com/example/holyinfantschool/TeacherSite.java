package com.example.holyinfantschool;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class TeacherSite extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private boolean isMuted = false;
    private FirebaseAuth mAuth;
    private ImageView volumeOn, volumeOff;
    private boolean settingsVisible = false; // track settings button toggle state

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_site);

        // Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Views
        ImageView taskBtn = findViewById(R.id.taskbtn);
        ImageView backTeacher = findViewById(R.id.backteacher); // now logout button
        ImageView teacherSetting = findViewById(R.id.teachersetting);
        volumeOn = findViewById(R.id.volumeOn);
        volumeOff = findViewById(R.id.volumeOff);

        hideSettingsButtons();

        // Background Music
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        // Go to Tasks
        taskBtn.setOnClickListener(v -> {
            startActivity(new Intent(TeacherSite.this, TaskSplash.class));
            finish();
        });

        // LOGOUT BUTTON
        backTeacher.setOnClickListener(v -> {

            // 1. Firebase logout
            FirebaseAuth.getInstance().signOut();

            // 2. Remove saved role
            getSharedPreferences("HIS_APP", MODE_PRIVATE)
                    .edit()
                    .remove("user_role")
                    .apply();

            // 3. Stop music
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }

            // 4. Redirect to Homepage
            Intent intent = new Intent(TeacherSite.this, Homepage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
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

        // Volume buttons
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
