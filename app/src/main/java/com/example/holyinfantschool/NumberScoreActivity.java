package com.example.holyinfantschool;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class NumberScoreActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number_score);

        TextView scoreTextView = findViewById(R.id.scoreTextView);
        Button playAgainButton = findViewById(R.id.playAgainButton);
        Button playAnotherGameButton = findViewById(R.id.playAnotherGameButton);

        scoreTextView.setText("Your score: " + Number1Activity.correctAnswers + " correct, " +
                Number1Activity.incorrectAnswers + " incorrect");

        playAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reset counters
                Number1Activity.correctAnswers = 0;
                Number1Activity.incorrectAnswers = 0;

                Intent intent = new Intent(NumberScoreActivity.this, Number1Activity.class);
                startActivity(intent);
                finish();
            }
        });

        playAnotherGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reset counters
                Number1Activity.correctAnswers = 0;
                Number1Activity.incorrectAnswers = 0;

                Intent intent = new Intent(NumberScoreActivity.this, QuizActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}