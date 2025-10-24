package com.example.holyinfantschool;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ScoreActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private boolean isMuted = false;

    private ImageView settingsButton;
    private LinearLayout settingsDropdown;
    private ImageView btnMute, btnUnmute, btnQuit;
    private TextView scoreTextView;
    private Button playAgainButton, playAnotherGameButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        // Initialize Views (IDs must match activity_score.xml)
        settingsButton = findViewById(R.id.settingsButton);
        settingsDropdown = findViewById(R.id.settingsDropdown);
        btnMute = findViewById(R.id.btnMute);
        btnUnmute = findViewById(R.id.btnUnmute);
        btnQuit = findViewById(R.id.btnQuit);
        scoreTextView = findViewById(R.id.scoreTextView);
        playAgainButton = findViewById(R.id.playAgainButton);
        playAnotherGameButton = findViewById(R.id.playAnotherGameButton);

        // Hide dropdown at start
        settingsDropdown.setVisibility(View.GONE);
        btnUnmute.setVisibility(View.GONE);

        // Background music for ScoreActivity
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        // Show score
        String scoreText = String.format("Correct: %d\nIncorrect: %d",
                GameScore.getCorrect(), GameScore.getIncorrect());
        scoreTextView.setText(scoreText);

        // Toggle dropdown
        settingsButton.setOnClickListener(v -> {
            if (settingsDropdown.getVisibility() == View.VISIBLE) {
                settingsDropdown.setVisibility(View.GONE);
            } else {
                // Update mute/unmute visibility
                btnMute.setVisibility(isMuted ? View.GONE : View.VISIBLE);
                btnUnmute.setVisibility(isMuted ? View.VISIBLE : View.GONE);
                settingsDropdown.setVisibility(View.VISIBLE);
            }
        });

        // Mute
        btnMute.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(0f, 0f);
                isMuted = true;
                btnMute.setVisibility(View.GONE);
                btnUnmute.setVisibility(View.VISIBLE);
            }
            settingsDropdown.setVisibility(View.GONE);
        });

        // Unmute
        btnUnmute.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(1f, 1f);
                isMuted = false;
                btnUnmute.setVisibility(View.GONE);
                btnMute.setVisibility(View.VISIBLE);
            }
            settingsDropdown.setVisibility(View.GONE);
        });

        // Quit → back to QuizActivity
        btnQuit.setOnClickListener(v -> {
            GameScore.reset();
            stopMusic();
            Intent intent = new Intent(ScoreActivity.this, QuizActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // Play Again → firstActivity
        playAgainButton.setOnClickListener(v -> {
            GameScore.reset();
            stopMusic();

            // 🔀 Shuffle questions before restarting the game
            GameSession.setQuestions(QuestionBank.getShuffledQuestions());

            Intent intent = new Intent(ScoreActivity.this, firstActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // Play Another Game → QuizActivity
        playAnotherGameButton.setOnClickListener(v -> {
            GameScore.reset();
            stopMusic();
            Intent intent = new Intent(ScoreActivity.this, QuizActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void stopMusic() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
            } catch (Exception ignored) { }
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
