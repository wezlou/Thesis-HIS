package com.example.holyinfantschool;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class NumberIncorrectActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number_incorrect);

        // Get selected number and source activity
        String selectedNumber = getIntent().getStringExtra("SELECTED_NUMBER");
        String sourceActivity = getIntent().getStringExtra("SOURCE_ACTIVITY");

        // Set the selected number
        TextView selectedNumberView = findViewById(R.id.selectedNumber);
        selectedNumberView.setText(selectedNumber);

        // Set the b2 image from the source activity
        ImageView b2Image = findViewById(R.id.b2);
        if (sourceActivity.equals("Number1Activity")) {
            b2Image.setImageResource(R.drawable.f4);
        } else if (sourceActivity.equals("Number2Activity")) {
            b2Image.setImageResource(R.drawable.m2);
        } else {
            b2Image.setImageResource(R.drawable.f4);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent;
                if (sourceActivity.equals("Number1Activity")) {
                    intent = new Intent(NumberIncorrectActivity.this, NumberSplashActivity.class);
                } else if (sourceActivity.equals("Number2Activity")) {
                    intent = new Intent(NumberIncorrectActivity.this, NumberSplashActivity.class);
                } else {
                    intent = new Intent(NumberIncorrectActivity.this, NumberTotalSplash.class);
                }
                intent.putExtra("SOURCE_ACTIVITY", sourceActivity);
                startActivity(intent);
                finish();
            }
        }, 4000);
    }
}