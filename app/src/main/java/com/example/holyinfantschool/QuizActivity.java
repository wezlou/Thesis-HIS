package com.example.holyinfantschool;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class QuizActivity extends AppCompatActivity {

    private ImageView btnColor, btnShape, btnNum, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // Initialize buttons
        btnColor = findViewById(R.id.btncolor);
        btnShape = findViewById(R.id.btnshape);
        btnNum = findViewById(R.id.btnnum);
        btnBack = findViewById(R.id.btnback); // Assuming imageView12 is the back button

        // Navigate to ColorActivity
        btnColor.setOnClickListener(v -> {
            Intent intent = new Intent(QuizActivity.this, ColorActivity.class);
            startActivity(intent);
        });

        // Navigate to ShapeActivity
        btnShape.setOnClickListener(v -> {
            Intent intent = new Intent(QuizActivity.this, ShapeActivity.class);
            startActivity(intent);
        });

        // Navigate to NumberActivity
        btnNum.setOnClickListener(v -> {
            Intent intent = new Intent(QuizActivity.this, NumberActivity.class);
            startActivity(intent);
        });

        // Go back to CategoryPageActivity
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(QuizActivity.this, Categorypage.class);
            startActivity(intent);
            finish(); // Close the current activity
        });
    }
}
