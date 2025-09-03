package com.example.holyinfantschool;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class firstActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        ImageView redColor = findViewById(R.id.imageView17);
        ImageView blueColor = findViewById(R.id.imageView18);
        ImageView greenColor = findViewById(R.id.imageView16);

        redColor.setOnClickListener(v ->
                GameFlowController.navigateToResult(this, true, R.drawable.plainapple, "secondActivity"));

        blueColor.setOnClickListener(v ->
                GameFlowController.navigateToResult(this, false, R.drawable.bx, "secondActivity"));

        greenColor.setOnClickListener(v ->
                GameFlowController.navigateToResult(this, false, R.drawable.pinkx, "secondActivity"));
    }
}