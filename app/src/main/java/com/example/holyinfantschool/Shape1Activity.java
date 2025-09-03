package com.example.holyinfantschool;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class Shape1Activity extends AppCompatActivity {
    public static int totalCorrect = 0;
    public static int totalIncorrect = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shape1);

        ImageView circle = findViewById(R.id.circle);
        ImageView triangle = findViewById(R.id.triangle);
        ImageView square = findViewById(R.id.square);

        circle.setOnClickListener(v -> navigateToActivity(shapecorrect.class, R.drawable.img_53, R.drawable.img_47, true));
        triangle.setOnClickListener(v -> navigateToActivity(shapeincorrect.class, R.drawable.img_52, R.drawable.img_47, false));
        square.setOnClickListener(v -> navigateToActivity(shapeincorrect.class, R.drawable.img_50, R.drawable.img_47, false));
    }

    private void navigateToActivity(Class<?> targetActivity, int shapeResId, int b1ResId, boolean isCorrect) {
        if (isCorrect) {
            totalCorrect++;
        } else {
            totalIncorrect++;
        }

        Intent intent = new Intent(this, targetActivity);
        intent.putExtra("shape_image", shapeResId);
        intent.putExtra("b1_image", b1ResId);
        startActivity(intent);

        new Handler().postDelayed(() -> {
            Intent nextIntent = new Intent(this, shapesplash.class);
            nextIntent.putExtra("next_activity", "Shape2Activity");
            startActivity(nextIntent);
            finish();
        }, 3000);
    }
}