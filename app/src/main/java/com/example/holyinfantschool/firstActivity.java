package com.example.holyinfantschool;

import android.content.Intent;
import android.graphics.PorterDuff;
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
        artImage.clearColorFilter();

        // Clear old choices
        choicesLayout.removeAllViews();

        for (int i = 0; i < q.getAnswers().size(); i++) {
            Question.Answer ans = q.getAnswers().get(i);
            ImageView btn = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(220, 220);
            params.setMargins(16, 16, 16, 16);
            btn.setLayoutParams(params);
            btn.setPadding(16, 16, 16, 16);
            btn.setImageResource(ans.getImageRes());

            btn.setOnClickListener(v -> {
                if (ans.isCorrect()) {
                    btn.setBackgroundResource(R.drawable.answer_outline_correct);
                    btn.setColorFilter(getResources().getColor(getColorFromRes(ans.getImageRes())),
                            PorterDuff.Mode.SRC_ATOP);

                    artImage.setColorFilter(getResources().getColor(getColorFromRes(ans.getImageRes())),
                            PorterDuff.Mode.SRC_ATOP);
                } else {
                    btn.setBackgroundResource(R.drawable.answer_outline_wrong);
                    btn.setColorFilter(getResources().getColor(getColorFromRes(ans.getImageRes())),
                            PorterDuff.Mode.SRC_ATOP);

                    for (int j = 0; j < choicesLayout.getChildCount(); j++) {
                        Question.Answer checkAns = q.getAnswers().get(j);
                        if (checkAns.isCorrect() && choicesLayout.getChildAt(j) instanceof ImageView) {
                            ImageView correctBtn = (ImageView) choicesLayout.getChildAt(j);
                            correctBtn.setBackgroundResource(R.drawable.answer_outline_correct);
                        }
                    }
                }

                // ✅ Pass both outline + color to result
                GameFlowController.navigateToResult(
                        firstActivity.this,
                        ans.isCorrect(),
                        q.getImageRes(),                           // outline image
                        getColorFromRes(ans.getImageRes()),        // fill color
                        "firstActivity",
                        currentIndex + 1
                );
            });

            choicesLayout.addView(btn);
        }
    }

    // Map button image resources to actual Android colors
    private int getColorFromRes(int resId) {
        if (resId == R.drawable.img_34) {
            return android.R.color.holo_red_dark; // red
        } else if (resId == R.drawable.img_38) {
            return android.R.color.holo_blue_dark; // blue
        } else if (resId == R.drawable.btnmango) {
            return R.color.bananaYellow; // yellow
        } else if (resId == R.drawable.img_33) {
            return android.R.color.holo_purple; // pink
        } else if (resId == R.drawable.img_66) {
            return android.R.color.holo_green_dark; // green
        } else if (resId == R.drawable.purplebtn) {
            return android.R.color.holo_purple; // purple
        } else if (resId == R.drawable.orangebtn) {
            return android.R.color.holo_orange_dark; // orange
        } else if (resId == R.drawable.brownbtn) {
            return R.color.brownColor; // custom brown
        } else {
            return android.R.color.black; // default
        }
    }
}
