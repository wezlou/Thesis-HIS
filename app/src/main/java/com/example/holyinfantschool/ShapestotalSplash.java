package com.example.holyinfantschool;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class ShapestotalSplash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shapestotal_splash);

        // Delay before going to score screen
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(this, ShapescoreActivity.class);
            startActivity(intent);
            finish();
        }, 2500); // 2.5 seconds delay
    }
}
