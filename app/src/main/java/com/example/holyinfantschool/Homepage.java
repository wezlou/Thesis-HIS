package com.example.holyinfantschool;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class Homepage extends AppCompatActivity {

    private Button playButton, loginButton, exitButton;
    private ImageView volumeOnButton, volumeOffButton, teacherSettingButton;
    private FirebaseAuth firebaseAuth;
    private MediaPlayer mediaPlayer;
    private boolean isMusicPlaying = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        // Initialize FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();

        // ✅ Force sign out every time the app opens
        firebaseAuth.signOut();

        // Find views by ID (Buttons)
        playButton = findViewById(R.id.play1);
        loginButton = findViewById(R.id.login);
        exitButton = findViewById(R.id.exit);

        // Find views by ID (ImageViews)
        volumeOnButton = findViewById(R.id.volumeOn);
        volumeOffButton = findViewById(R.id.volumeOff);
        teacherSettingButton = findViewById(R.id.teachersetting);

        // Initialize MediaPlayer
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);

        // Start background music automatically
        if (!isMusicPlaying) {
            mediaPlayer.start();
            isMusicPlaying = true;
        }

        // Initially hide volume buttons
        volumeOnButton.setVisibility(ImageView.INVISIBLE);
        volumeOffButton.setVisibility(ImageView.INVISIBLE);

        // ==============================
        // BUTTON ACTIONS
        // ==============================

        // Play button → Go to CategoryPage
        playButton.setOnClickListener(v -> {
            animateButton(playButton);
            Intent intent = new Intent(Homepage.this, Categorypage.class);
            startActivity(intent);
        });

        // Exit button → Close the app (sign out handled on next startup)
        exitButton.setOnClickListener(v -> {
            animateButton(exitButton);
            finishAffinity(); // Close all activities
            System.exit(0);   // Kill app process
        });

        // Login button → Always go to LoginForm
        loginButton.setOnClickListener(v -> {
            animateButton(loginButton);
            startActivity(new Intent(Homepage.this, LoginForm.class));
        });

        teacherSettingButton.setOnClickListener(v -> {
            if (volumeOnButton.getVisibility() == ImageView.VISIBLE) {
                volumeOnButton.setVisibility(ImageView.INVISIBLE);
                volumeOffButton.setVisibility(ImageView.INVISIBLE);
            } else {
                volumeOnButton.setVisibility(ImageView.VISIBLE);
                volumeOffButton.setVisibility(ImageView.VISIBLE);
            }
        });

        volumeOnButton.setOnClickListener(v -> {
            if (!isMusicPlaying) {
                mediaPlayer.start();
                isMusicPlaying = true;
            }
        });

        volumeOffButton.setOnClickListener(v -> {
            if (isMusicPlaying) {
                mediaPlayer.pause();
                isMusicPlaying = false;
            }
        });
    }

    private void animateButton(Button button) {
        button.animate().scaleX(1.1f).scaleY(1.1f).setDuration(120)
                .withEndAction(() ->
                        button.animate().scaleX(1f).scaleY(1f).setDuration(120));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isMusicPlaying) {
            mediaPlayer.pause();
            isMusicPlaying = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}
