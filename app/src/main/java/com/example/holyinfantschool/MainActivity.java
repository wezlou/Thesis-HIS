package com.example.holyinfantschool;

import android.animation.Animator;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;

public class MainActivity extends AppCompatActivity {

    private LottieAnimationView bellAnimation;
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler(Looper.getMainLooper());

    private ProgressBar progressBar;
    private TextView loadingText;

    private int progressStatus = 0;
    private final int PROGRESS_MAX = 100; // 100%

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        bellAnimation = findViewById(R.id.bell);
        progressBar = findViewById(R.id.start_progress);
        loadingText = findViewById(R.id.loading_text);

        progressBar.setMax(PROGRESS_MAX);

        // Initialize MediaPlayer
        mediaPlayer = MediaPlayer.create(this, R.raw.bell_sound);

        // Start animation listener
        bellAnimation.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                }
                // Start filling progress bar
                simulateProgress();
            }

            @Override public void onAnimationEnd(Animator animation) {}
            @Override public void onAnimationCancel(Animator animation) {}
            @Override public void onAnimationRepeat(Animator animation) {}
        });
    }

    private void simulateProgress() {
        new Thread(() -> {
            while (progressStatus < PROGRESS_MAX) {
                progressStatus++;

                handler.post(() -> {
                    progressBar.setProgress(progressStatus);
                    loadingText.setText("Loading... " + progressStatus + "%");
                });

                try {
                    Thread.sleep(40); // 40ms * 100 = ~4 seconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Once complete, move to Homepage
            handler.post(() -> {
                Intent intent = new Intent(MainActivity.this, Homepage.class);
                startActivity(intent);
                finish();
            });
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacksAndMessages(null);
    }
}
