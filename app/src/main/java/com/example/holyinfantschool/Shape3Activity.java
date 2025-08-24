package com.example.holyinfantschool;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class Shape3Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shape3);

        // Initialize your ImageViews based on your XML
        ImageView b1 = findViewById(R.id.b2);
        ImageView circle = findViewById(R.id.circle);
        ImageView star = findViewById(R.id.star);
        ImageView heart = findViewById(R.id.heart);

        // Set click listeners for each shape
        circle.setOnClickListener(v -> navigateToActivity(shapeincorrect.class, R.drawable.img_53, R.drawable.shape3, false));
        star.setOnClickListener(v -> navigateToActivity(shapeincorrect.class, R.drawable.star2, R.drawable.shape3, false));
        heart.setOnClickListener(v -> navigateToActivity(shapecorrect.class, R.drawable.heart, R.drawable.shape3, true));
    }

    private void navigateToActivity(Class<?> targetActivity, int shapeResId, int b1ResId, boolean isCorrect) {
        // Update counters
        if (isCorrect) {
            Shape1Activity.totalCorrect++;
        } else {
            Shape1Activity.totalIncorrect++;
        }

        Intent intent = new Intent(this, targetActivity);
        intent.putExtra("shape_image", shapeResId);
        intent.putExtra("b1_image", b1ResId);
        startActivity(intent);

        new Handler().postDelayed(() -> {
            Intent nextIntent = new Intent(this, ShapestotalSplash.class);
            nextIntent.putExtra("correct_count", Shape1Activity.totalCorrect);
            nextIntent.putExtra("incorrect_count", Shape1Activity.totalIncorrect);
            startActivity(nextIntent);
            finish();
        }, 3000);
    }
}