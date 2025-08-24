package com.example.holyinfantschool;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class Shape2Activity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shape2);

        ImageView circle = findViewById(R.id.circle);
        ImageView star = findViewById(R.id.star);
        ImageView square = findViewById(R.id.square);

        circle.setOnClickListener(v -> navigateToActivity(shapeincorrect.class, R.drawable.img_53, R.drawable.shape1, false));
        star.setOnClickListener(v -> navigateToActivity(shapecorrect.class, R.drawable.star2, R.drawable.shape1, true));
        square.setOnClickListener(v -> navigateToActivity(shapeincorrect.class, R.drawable.img_50, R.drawable.shape1, false));
    }

    private void navigateToActivity(Class<?> targetActivity, int shapeResId, int b1ResId, boolean isCorrect) {
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
            Intent nextIntent = new Intent(this, shapesplash.class);
            nextIntent.putExtra("next_activity", "Shape3Activity");
            startActivity(nextIntent);
            finish();
        }, 3000);
    }
}