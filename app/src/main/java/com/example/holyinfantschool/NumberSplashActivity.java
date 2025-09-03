package com.example.holyinfantschool;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class NumberSplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number_splash);

        String lastActivity = getIntent().getStringExtra("SOURCE_ACTIVITY");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent;
                if (lastActivity.equals("Number1Activity")) {
                    intent = new Intent(NumberSplashActivity.this, Number2Activity.class);
                } else {
                    intent = new Intent(NumberSplashActivity.this, Number3Activity.class);
                }
                startActivity(intent);
                finish();
            }
        }, 3000);
    }
}