package com.example.holyinfantschool;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;

public class splashstudenttask extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 4000; // 4 seconds
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashstudenttask);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setIndeterminate(true); // Make the progress bar spin continuously

        new Handler().postDelayed(() -> {
            // Go to studenttask activity after 4 seconds
            Intent intent = new Intent(splashstudenttask.this, studenttask.class);
            startActivity(intent);
            finish(); // Finish splash screen activity
        }, SPLASH_TIME_OUT);
    }
}
