package com.example.holyinfantschool;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class firstActivity extends AppCompatActivity {

    private List<Question> questions;
    private int currentIndex = 0;

    private TextView questionText;
    private ImageView artImage;
    private LinearLayout choicesLayout;

    // 🎵 Music
    private MediaPlayer mediaPlayer;
    private boolean isMuted = false;
    private boolean isPausedBySystem = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        questionText = findViewById(R.id.questionText);
        artImage = findViewById(R.id.appleImage);
        choicesLayout = findViewById(R.id.choicesLayout);

        ImageView backBtn = findViewById(R.id.backbtn);
        ImageView settingsBtn = findViewById(R.id.settingsButton);

        // 🎵 Start music
        setupMusic();

        // 🔙 Back button
        backBtn.setOnClickListener(v -> {
            stopMusic();
            GameSession.setQuestions(QuestionBank.getShuffledQuestions());
            Intent intent = new Intent(this, QuizActivity.class);
            startActivity(intent);
            finish();
        });

        // ⚙️ Settings menu
        settingsBtn.setOnClickListener(v -> showSettingsMenu(settingsBtn));

        // Load question index
        currentIndex = getIntent().getIntExtra("CURRENT_INDEX", 0);

        if (GameSession.getQuestions() == null) {
            GameSession.setQuestions(QuestionBank.getQuestions());
        }
        questions = GameSession.getQuestions();

        showQuestion();
    }

    private void showQuestion() {
        if (currentIndex >= questions.size()) {
            stopMusic();
            Intent intent = new Intent(this, finalscorecolorsplash.class);
            startActivity(intent);
            finish();
            return;
        }

        Question q = questions.get(currentIndex);

        questionText.setText(q.getText());
        artImage.setImageResource(q.getImageRes());
        artImage.clearColorFilter();

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

                GameFlowController.navigateToResult(
                        firstActivity.this,
                        ans.isCorrect(),
                        q.getImageRes(),
                        getColorFromRes(ans.getImageRes()),
                        "firstActivity",
                        currentIndex + 1
                );
            });

            choicesLayout.addView(btn);
        }
    }

    // 🎨 Color Mapping
    private int getColorFromRes(int resId) {
        if (resId == R.drawable.img_34) return android.R.color.holo_red_dark;
        else if (resId == R.drawable.img_38) return android.R.color.holo_blue_dark;
        else if (resId == R.drawable.btnmango) return R.color.bananaYellow;
        else if (resId == R.drawable.img_33) return android.R.color.holo_purple;
        else if (resId == R.drawable.img_66) return android.R.color.holo_green_dark;
        else if (resId == R.drawable.purplebtn) return android.R.color.holo_purple;
        else if (resId == R.drawable.orangebtn) return android.R.color.holo_orange_dark;
        else if (resId == R.drawable.brownbtn) return R.color.brownColor;
        else return android.R.color.black;
    }

    // -------------------------------------------------------
    // 🎵 MUSIC HANDLING
    // -------------------------------------------------------
    private void setupMusic() {
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    private void muteMusic() {
        isMuted = true;
        if (mediaPlayer != null) mediaPlayer.setVolume(0f, 0f);
    }

    private void unmuteMusic() {
        isMuted = false;
        if (mediaPlayer != null) mediaPlayer.setVolume(1f, 1f);
    }

    private void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPausedBySystem = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null && isPausedBySystem && !isMuted) {
            mediaPlayer.start();
            isPausedBySystem = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMusic();
    }

    // -------------------------------------------------------
    // ⚙️ SETTINGS MENU
    // -------------------------------------------------------
    private void showSettingsMenu(ImageView anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.getMenu().add(isMuted ? "Unmute 🔊" : "Mute 🔇");
        popupMenu.getMenu().add("Exit ❌");

        popupMenu.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();

            if (title.contains("Mute")) muteMusic();
            else if (title.contains("Unmute")) unmuteMusic();
            else if (title.contains("Exit")) {
                stopMusic();
                finishAffinity();
            }
            return true;
        });

        popupMenu.show();
    }
}
