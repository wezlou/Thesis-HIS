package com.example.holyinfantschool;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Get intent extra
        String redirectTo = getIntent().getStringExtra("redirectTo");

        new Handler().postDelayed(() -> {
            Intent intent;
            if ("TeacherSite".equals(redirectTo)) {
                intent = new Intent(SplashScreen.this, TeacherSite.class);
            } else {
                intent = new Intent(SplashScreen.this, LoginForm.class);
            }
            startActivity(intent);
            finish();
        }, 2000); // 2 seconds delay
    }
}
