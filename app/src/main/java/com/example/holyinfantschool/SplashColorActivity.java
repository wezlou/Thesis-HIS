package com.example.holyinfantschool;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashColorActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_color);

        String nextActivity = getIntent().getStringExtra("NEXT_ACTIVITY");

        new Handler().postDelayed(() -> {
            Class<?> targetClass;
            switch (nextActivity) {
                case "secondActivity":
                    targetClass = secondActivity.class;
                    break;
                case "thirdActivity":
                    targetClass = thirdActivity.class;
                    break;
                case "finalScore":
                    targetClass = finalscorecolorsplash.class;
                    break;
                default:
                    finish();
                    return;
            }

            startActivity(new Intent(this, targetClass));
            finish();
        }, 3000);
    }
}