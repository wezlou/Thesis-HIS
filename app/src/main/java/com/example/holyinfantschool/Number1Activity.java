package com.example.holyinfantschool;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class Number1Activity extends AppCompatActivity {
    public static int correctAnswers = 0;
    public static int incorrectAnswers = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number1);

        TextView textView15 = findViewById(R.id.textView15);
        TextView textView17 = findViewById(R.id.textView17);
        TextView textView18 = findViewById(R.id.textView18);

        textView15.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Correct answer (3 birds)
                correctAnswers++;
                Intent intent = new Intent(Number1Activity.this, NumberCorrectActivity.class);
                intent.putExtra("SELECTED_NUMBER", "3");
                intent.putExtra("SOURCE_ACTIVITY", "Number1Activity");
                startActivity(intent);
                finish();
            }
        });

        textView17.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Incorrect answer
                incorrectAnswers++;
                Intent intent = new Intent(Number1Activity.this, NumberIncorrectActivity.class);
                intent.putExtra("SELECTED_NUMBER", "2");
                intent.putExtra("SOURCE_ACTIVITY", "Number1Activity");
                startActivity(intent);
                finish();
            }
        });

        textView18.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Incorrect answer
                incorrectAnswers++;
                Intent intent = new Intent(Number1Activity.this, NumberIncorrectActivity.class);
                intent.putExtra("SELECTED_NUMBER", "1");
                intent.putExtra("SOURCE_ACTIVITY", "Number1Activity");
                startActivity(intent);
                finish();
            }
        });
    }
}