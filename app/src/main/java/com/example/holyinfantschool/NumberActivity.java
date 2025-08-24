package com.example.holyinfantschool;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class NumberActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number);

        // Find the ImageView by its ID
        ImageView nbtn = findViewById(R.id.Nbtn);

        // Set click listener to navigate to Number1Activity
        nbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NumberActivity.this, Number1Activity.class);
                startActivity(intent);
            }
        });
    }
}
