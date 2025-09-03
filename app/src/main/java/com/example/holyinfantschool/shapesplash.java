package com.example.holyinfantschool;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class shapesplash extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shapesplash);

        String nextActivity = getIntent().getStringExtra("next_activity");

        new Handler().postDelayed(() -> {
            Intent intent;
            switch (nextActivity) {
                case "Shape2Activity":
                    intent = new Intent(this, Shape2Activity.class);
                    break;
                case "Shape3Activity":
                    intent = new Intent(this, Shape3Activity.class);
                    break;
                default:
                    intent = new Intent(this, Shape2Activity.class);
            }
            startActivity(intent);
            finish();
        }, 5000);
    }
}