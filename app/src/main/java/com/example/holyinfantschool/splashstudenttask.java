package com.example.holyinfantschool;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class splashstudenttask extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 4000;
    private ProgressBar progressBar;
    private TextView loadingText;

    private Handler handler = new Handler();
    private String[] messages = {"Loading...", "Almost there...", "Preparing Task..."};
    private int messageIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashstudenttask);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setIndeterminate(true);

        loadingText = findViewById(R.id.loadingText);

        handler.post(updateTextRunnable);

        new Handler().postDelayed(() -> {
            handler.removeCallbacks(updateTextRunnable);

            Intent intent = new Intent(splashstudenttask.this, studenttask.class);
            startActivity(intent);
            finish();
        }, SPLASH_TIME_OUT);
    }

    private Runnable updateTextRunnable = new Runnable() {
        @Override
        public void run() {
            if (loadingText != null) {
                loadingText.setText(messages[messageIndex]);
                messageIndex = (messageIndex + 1) % messages.length;
                handler.postDelayed(this, 1500);
            }
        }
    };
}
