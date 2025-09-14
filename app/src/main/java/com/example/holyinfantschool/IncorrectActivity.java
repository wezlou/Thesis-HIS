package com.example.holyinfantschool;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

public class IncorrectActivity extends AppCompatActivity {

    private static final long AUTO_NEXT_DURATION_MS = 1500; // 1.5s

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incorrect);

        ImageView wrongImage = findViewById(R.id.apple);
        ProgressBar progressBar = findViewById(R.id.progressBar);

        int imageRes = getIntent().getIntExtra("IMAGE_RES", -1);
        if (imageRes != -1) {
            wrongImage.setImageResource(imageRes);
        }

        progressBar.setMax(100);
        progressBar.setProgress(0);
        progressBar.setVisibility(View.VISIBLE);

        ObjectAnimator animator = ObjectAnimator.ofInt(progressBar, "progress", 0, 100);
        animator.setDuration(AUTO_NEXT_DURATION_MS);
        animator.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animation) {}
            @Override public void onAnimationEnd(Animator animation) { navigateNext(); }
            @Override public void onAnimationCancel(Animator animation) { navigateNext(); }
            @Override public void onAnimationRepeat(Animator animation) {}
        });
        animator.start();

        findViewById(android.R.id.content).setOnClickListener(v -> {
            animator.cancel();
            navigateNext();
        });
    }

    private void navigateNext() {
        String nextActivity = getIntent().getStringExtra("NEXT_ACTIVITY");
        int nextIndex = getIntent().getIntExtra("NEXT_INDEX", 0);

        try {
            Class<?> clazz = Class.forName("com.example.holyinfantschool." + nextActivity);
            Intent intent = new Intent(this, clazz);
            intent.putExtra("CURRENT_INDEX", nextIndex);
            startActivity(intent);
            finish();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            startActivity(new Intent(this, Categorypage.class));
            finish();
        }
    }
}
