package com.example.holyinfantschool;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ShapescoreActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shapescore);

        int correct = getIntent().getIntExtra("correct_count", 0);
        int incorrect = getIntent().getIntExtra("incorrect_count", 0);

        TextView scoreTextView = findViewById(R.id.scoreTextView);
        scoreTextView.setText(String.format("Correct: %d\nIncorrect: %d", correct, incorrect));

        Button playAgainButton = findViewById(R.id.playAgainButton);
        Button playAnotherGameButton = findViewById(R.id.playAnotherGameButton);

        playAgainButton.setOnClickListener(v -> {
            Shape1Activity.totalCorrect = 0;
            Shape1Activity.totalIncorrect = 0;
            startActivity(new Intent(this, Shape1Activity.class));
            finish();
        });

        playAnotherGameButton.setOnClickListener(v -> {
            Shape1Activity.totalCorrect = 0;
            Shape1Activity.totalIncorrect = 0;
            startActivity(new Intent(this, QuizActivity.class));
            finish();
        });
    }
}