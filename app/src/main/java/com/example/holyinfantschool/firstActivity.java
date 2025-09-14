package com.example.holyinfantschool;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class firstActivity extends AppCompatActivity {

    private List<Question> questions;
    private int currentIndex = 0;

    private TextView levelText, questionText;
    private ImageView artImage;
    private LinearLayout choicesLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        // UI refs
        levelText = findViewById(R.id.levelText);
        questionText = findViewById(R.id.questionText);
        artImage = findViewById(R.id.appleImage);
        choicesLayout = findViewById(R.id.choicesLayout);

        // Buttons
        ImageView backBtn = findViewById(R.id.backButton);
        ImageView settingsBtn = findViewById(R.id.settingsButton);

        // Back → Category page
        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, Categorypage.class);
            startActivity(intent);
            finish();
        });

        // Settings (placeholder)
        settingsBtn.setOnClickListener(v -> {
            // TODO: Open settings page if you create one
        });

        // Get currentIndex
        currentIndex = getIntent().getIntExtra("CURRENT_INDEX", 0);

        // Load question bank once at the start
        if (GameSession.getQuestions() == null) {
            GameSession.setQuestions(QuestionBank.getQuestions());
        }
        questions = GameSession.getQuestions();

        showQuestion();
    }

    private void showQuestion() {
        if (currentIndex >= questions.size()) {
            // Finished all questions → go to final splash
            Intent intent = new Intent(this, finalscorecolorsplash.class);
            startActivity(intent);
            finish();
            return;
        }

        Question q = questions.get(currentIndex);

        levelText.setText("LEVEL " + (currentIndex + 1));
        questionText.setText(q.getText());
        artImage.setImageResource(q.getImageRes());

        // Clear old choices
        choicesLayout.removeAllViews();

        for (Question.Answer ans : q.getAnswers()) {
            ImageView btn = new ImageView(this);
            btn.setLayoutParams(new LinearLayout.LayoutParams(220, 220));
            btn.setPadding(16, 16, 16, 16);
            btn.setImageResource(ans.getImageRes());

            btn.setOnClickListener(v -> {
                GameFlowController.navigateToResult(
                        firstActivity.this,
                        ans.isCorrect(),
                        q.getImageRes(),
                        "firstActivity",
                        currentIndex + 1
                );
            });

            choicesLayout.addView(btn);
        }
    }
}
