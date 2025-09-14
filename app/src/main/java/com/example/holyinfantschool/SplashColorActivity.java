package com.example.holyinfantschool;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashColorActivity extends AppCompatActivity {
    private static final int SPLASH_DURATION = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_color);

        String nextActivity = getIntent().getStringExtra("NEXT_ACTIVITY");
        int nextIndex = getIntent().getIntExtra("NEXT_INDEX", 0);

        new Handler().postDelayed(() -> {
            try {
                Class<?> clazz = Class.forName("com.example.holyinfantschool." + nextActivity);
                Intent intent = new Intent(SplashColorActivity.this, clazz);
                intent.putExtra("CURRENT_INDEX", nextIndex);
                startActivity(intent);
                finish();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }, SPLASH_DURATION);
    }
}
