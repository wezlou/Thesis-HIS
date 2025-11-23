package com.example.holyinfantschool;

import android.content.Intent;
import android.os.Bundle;
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

        // ✅ READ FROM Numberquiz (correct)
        int correct = Numberquiz.totalCorrect;
        int incorrect = Numberquiz.totalIncorrect;

        scoreTextView.setText(
                "✅ Correct: " + correct + "\n" +
                        "❌ Incorrect: " + incorrect + "\n"
        );

        playAgainButton.setOnClickListener(v -> {
            // reset values
            Numberquiz.totalCorrect = 0;
            Numberquiz.totalIncorrect = 0;

            startActivity(new Intent(this, Numberquiz.class));
            finish();
        });

        playAnotherGameButton.setOnClickListener(v -> {
            Numberquiz.totalCorrect = 0;
            Numberquiz.totalIncorrect = 0;

            startActivity(new Intent(this, QuizActivity.class));
            finish();
        });
    }
}
