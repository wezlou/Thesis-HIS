package com.example.holyinfantschool;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class shapeincorrect extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shapeincorrect);

        ImageView b1 = findViewById(R.id.b2);
        ImageView shapeImage = findViewById(R.id.circle);

        Intent intent = getIntent();
        b1.setImageResource(intent.getIntExtra("b1_image", 0));
        shapeImage.setImageResource(intent.getIntExtra("shape_image", 0));
    }
}