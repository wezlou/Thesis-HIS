package com.example.holyinfantschool;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class secondActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        ImageView redColor = findViewById(R.id.imageView17);
        ImageView blueColor = findViewById(R.id.imageView18);
        ImageView greenColor = findViewById(R.id.imageView16);

        redColor.setOnClickListener(v ->
                GameFlowController.navigateToResult(this, false, R.drawable.redmango, "thirdActivity"));

        blueColor.setOnClickListener(v ->
                GameFlowController.navigateToResult(this, false, R.drawable.bluemango, "thirdActivity"));

        greenColor.setOnClickListener(v ->
                GameFlowController.navigateToResult(this, true, R.drawable.mango, "thirdActivity"));
    }
}