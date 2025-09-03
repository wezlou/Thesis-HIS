package com.example.holyinfantschool;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class finalscorecolorsplash extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finalscorecolorsplash);

        new Handler().postDelayed(() -> {
            startActivity(new Intent(this, ScoreActivity.class));
            finish();
        }, 3000);
    }
}