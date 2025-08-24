package com.example.holyinfantschool;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Homepage extends AppCompatActivity {

    private ImageView playButton, loginButton, volumeOnButton, volumeOffButton, teacherSettingButton;
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

        // Find views by ID
        playButton = findViewById(R.id.play1);
        loginButton = findViewById(R.id.login);
        volumeOnButton = findViewById(R.id.volumeOn);
        volumeOffButton = findViewById(R.id.volumeOff);
        teacherSettingButton = findViewById(R.id.teachersetting);

        // Initialize MediaPlayer (replace with actual music file in your res/raw folder)
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music); // Add your music file here

        // Start the music automatically when the app opens
        if (!isMusicPlaying) {
            mediaPlayer.start();  // Start the music
            isMusicPlaying = true;
        }

        // Initially hide the volume buttons
        volumeOnButton.setVisibility(ImageView.INVISIBLE);
        volumeOffButton.setVisibility(ImageView.INVISIBLE);

        // Navigate to CategoryPage when Play button is clicked
        playButton.setOnClickListener(v -> {
            Intent intent = new Intent(Homepage.this, Categorypage.class);
            startActivity(intent);
        });

        // Check if user is already logged in
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            // If logged in, clicking login goes to TeacherSite
            loginButton.setOnClickListener(v -> {
                Intent intent = new Intent(Homepage.this, TeacherSite.class);
                startActivity(intent);
            });
        } else {
            // If not logged in, clicking login goes to LoginForm
            loginButton.setOnClickListener(v -> {
                Intent intent = new Intent(Homepage.this, LoginForm.class);
                startActivity(intent);
            });
        }

        // Toggle volume buttons when settings button is clicked
        teacherSettingButton.setOnClickListener(v -> {
            if (volumeOnButton.getVisibility() == ImageView.VISIBLE) {
                volumeOnButton.setVisibility(ImageView.INVISIBLE);
                volumeOffButton.setVisibility(ImageView.INVISIBLE);
            } else {
                volumeOnButton.setVisibility(ImageView.VISIBLE);
                volumeOffButton.setVisibility(ImageView.VISIBLE);
            }
        });

        // Start music when volumeOnButton is clicked
        volumeOnButton.setOnClickListener(v -> {
            if (!isMusicPlaying) {
                mediaPlayer.start();  // Start the music
                isMusicPlaying = true;
            }
        });

        // Pause music when volumeOffButton is clicked
        volumeOffButton.setOnClickListener(v -> {
            if (isMusicPlaying) {
                mediaPlayer.pause();  // Pause the music
                isMusicPlaying = false;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause the music if the app is paused or closed
        if (isMusicPlaying) {
            mediaPlayer.pause();
            isMusicPlaying = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release the MediaPlayer when the activity is destroyed
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}
