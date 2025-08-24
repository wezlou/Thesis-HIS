package com.example.holyinfantschool;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class CorrectActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_correct);

        ImageView imageView = findViewById(R.id.appleImage);
        int imageRes = getIntent().getIntExtra("IMAGE_RES", R.drawable.plainapple);
        Glide.with(this).load(imageRes).into(imageView);

        new Handler().postDelayed(() -> {
            // This will be handled by GameFlowController
            finish();
        }, 3000);
    }
}