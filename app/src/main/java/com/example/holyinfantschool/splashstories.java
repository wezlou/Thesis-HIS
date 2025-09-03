package com.example.holyinfantschool;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class splashstories extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashstories);

        // Delay for 4 seconds, then transition to StoriesMain activity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(splashstories.this, StoriesActivity.class);
                startActivity(intent);
                finish(); // Close this activity
            }
        }, 4000); // 4 seconds delay
    }
}
