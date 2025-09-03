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

        int correct = getIntent().getIntExtra("correct_count", 0);
        int incorrect = getIntent().getIntExtra("incorrect_count", 0);

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(this, ShapescoreActivity.class);
            intent.putExtra("correct_count", correct);
            intent.putExtra("incorrect_count", incorrect);
            startActivity(intent);
            finish();
        }, 3000);
    }
}