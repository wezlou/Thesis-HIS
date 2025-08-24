package com.example.holyinfantschool;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ScoreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        // Initialize views
        TextView scoreTextView = findViewById(R.id.scoreTextView);
        Button playAgainButton = findViewById(R.id.playAgainButton);
        Button playAnotherGameButton = findViewById(R.id.playAnotherGameButton);

        // Set score text
        scoreTextView.setText(String.format("Correct: %d\nIncorrect: %d",
                GameScore.getCorrect(), GameScore.getIncorrect()));

        // Play Again button - returns to FirstActivity and resets score
        playAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GameScore.reset(); // Reset the score
                Intent intent = new Intent(ScoreActivity.this, firstActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish(); // Close current activity
            }
        });

        // Play Another Game button - goes to QuizActivity
        playAnotherGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GameScore.reset(); // Reset the score
                Intent intent = new Intent(ScoreActivity.this, QuizActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish(); // Close current activity
            }
        });
    }
}