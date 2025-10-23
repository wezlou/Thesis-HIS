package com.example.holyinfantschool;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ShapescoreActivity extends AppCompatActivity {

    private TextView scoreTextView;
    private Button playAgainButton, playAnotherGameButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        scoreTextView = findViewById(R.id.scoreTextView);
        playAgainButton = findViewById(R.id.playAgainButton);
        playAnotherGameButton = findViewById(R.id.playAnotherGameButton);

        int correct = Shape1Activity.totalCorrect;
        int incorrect = Shape1Activity.totalIncorrect;
        int total = correct + incorrect;
        if (total == 0) total = 1;

        scoreTextView.setText(
                "✅ Correct: " + correct + "\n" +
                        "❌ Incorrect: " + incorrect + "\n"
        );

        playAgainButton.setOnClickListener(v -> {
            Shape1Activity.totalCorrect = 0;
            Shape1Activity.totalIncorrect = 0;
            Intent intent = new Intent(this, Shape1Activity.class);
            startActivity(intent);
            finish();
        });

        playAnotherGameButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, QuizActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
