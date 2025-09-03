package com.example.holyinfantschool;

import android.animation.Animator;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;

public class MainActivity extends AppCompatActivity {

    private LottieAnimationView bellAnimation;
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the bell animation
        bellAnimation = findViewById(R.id.bell);

        // Initialize MediaPlayer with the bell sound
        mediaPlayer = MediaPlayer.create(this, R.raw.bell_sound);

        // Play bell sound when animation starts
        bellAnimation.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (mediaPlayer != null) {
                    mediaPlayer.start(); // Play the sound
                }

                // Delay for 4 seconds, then navigate to Homepage
                handler.postDelayed(() -> {
                    Intent intent = new Intent(MainActivity.this, Homepage.class);
                    startActivity(intent);
                    finish(); // Close splash screen
                }, 4000);
            }

            @Override
            public void onAnimationEnd(Animator animation) {}

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release(); // Free up media resources
            mediaPlayer = null;
        }
        handler.removeCallbacksAndMessages(null); // Clear pending handlers
    }
}
