package com.example.holyinfantschool;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class Number3Activity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number3);

        TextView textView15 = findViewById(R.id.textView15);
        TextView textView17 = findViewById(R.id.textView17);
        TextView textView18 = findViewById(R.id.textView18);

        textView15.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Incorrect answer
                Number1Activity.incorrectAnswers++;
                Intent intent = new Intent(Number3Activity.this, NumberIncorrectActivity.class);
                intent.putExtra("SELECTED_NUMBER", "3");
                intent.putExtra("SOURCE_ACTIVITY", "Number3Activity");
                startActivity(intent);
                finish();
            }
        });

        textView17.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Correct answer (4 frogs)
                Number1Activity.correctAnswers++;
                Intent intent = new Intent(Number3Activity.this, NumberCorrectActivity.class);
                intent.putExtra("SELECTED_NUMBER", "4");
                intent.putExtra("SOURCE_ACTIVITY", "Number3Activity");
                startActivity(intent);
                finish();
            }
        });

        textView18.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Incorrect answer
                Number1Activity.incorrectAnswers++;
                Intent intent = new Intent(Number3Activity.this, NumberIncorrectActivity.class);
                intent.putExtra("SELECTED_NUMBER", "1");
                intent.putExtra("SOURCE_ACTIVITY", "Number3Activity");
                startActivity(intent);
                finish();
            }
        });
    }
}