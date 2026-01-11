package com.example.holyinfantschool;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class firstActivity extends AppCompatActivity {

    private List<Question> questions;
    private int currentIndex = 0;

    private TextView questionText;
    private ImageView artImage;
    private LinearLayout choicesLayout;
    private ProgressBar gameProgressBar;
    private TextView gameProgressText;
    private final int totalLevels = 15;

    private MediaPlayer mediaPlayer;
    private boolean isMuted = false;
    private boolean isPausedBySystem = false;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        questionText = findViewById(R.id.questionText);
        artImage = findViewById(R.id.appleImage);
        choicesLayout = findViewById(R.id.choicesLayout);
        gameProgressBar = findViewById(R.id.gameProgressBar);
        gameProgressText = findViewById(R.id.gameProgressText);

        gameProgressBar.setMax(totalLevels);
        updateProgressBar();

        ImageView backBtn = findViewById(R.id.backbtn);
        ImageView settingsBtn = findViewById(R.id.settingsButton);

        setupMusic();

        backBtn.setOnClickListener(v -> {
            stopMusic();
            GameSession.setQuestions(QuestionBank.getShuffledQuestions());
            startActivity(new Intent(this, QuizActivity.class));
            finish();
        });

        settingsBtn.setOnClickListener(v -> showSettingsMenu(settingsBtn));

        currentIndex = getIntent().getIntExtra("CURRENT_INDEX", 0);

        if (GameSession.getQuestions() == null) {
            GameSession.setQuestions(QuestionBank.getQuestions());
        }
        questions = GameSession.getQuestions();

        showQuestion();
    }

    private void saveLogoutHistory() {

        String uid = getSharedPreferences("HIS_APP", MODE_PRIVATE)
                .getString("last_uid", null);

        String email = getSharedPreferences("HIS_APP", MODE_PRIVATE)
                .getString("last_email", null);

        if (uid == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("action", "logout");
        data.put("uid", uid);
        data.put("email", email);
        data.put("role", "student");
        data.put("loginType", "firebase");
        data.put("device", "Android");
        data.put("timestamp", FieldValue.serverTimestamp());

        db.collection("auth_history").add(data);
    }

    private void showQuestion() {
        if (currentIndex >= questions.size()) {
            stopMusic();
            startActivity(new Intent(this, finalscorecolorsplash.class));
            finish();
            return;
        }

        updateProgressBar();
        Question q = questions.get(currentIndex);

        questionText.setText(q.getText());
        artImage.setImageResource(q.getImageRes());
        artImage.clearColorFilter();

        choicesLayout.removeAllViews();

        for (int i = 0; i < q.getAnswers().size(); i++) {
            Question.Answer ans = q.getAnswers().get(i);
            ImageView btn = new ImageView(this);

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            params.setMargins(16, 16, 16, 16);

            btn.setLayoutParams(params);
            btn.setAdjustViewBounds(true);
            btn.setScaleType(ImageView.ScaleType.FIT_CENTER);
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

    private void updateProgressBar() {
        int progressPercentage = (currentIndex * 100) / totalLevels;
        gameProgressBar.setProgress(currentIndex);
        gameProgressText.setText(progressPercentage + "%");
    }

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

    private void showSettingsMenu(ImageView anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.getMenu().add(isMuted ? "Unmute 🔊" : "Mute 🔇");
        popupMenu.getMenu().add("Exit ❌");

        popupMenu.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();

            if (title.contains("Mute")) muteMusic();
            else if (title.contains("Unmute")) unmuteMusic();
            else if (title.contains("Exit")) {

                saveLogoutHistory();

                FirebaseAuth.getInstance().signOut();

                getSharedPreferences("HIS_APP", MODE_PRIVATE)
                        .edit()
                        .remove("session_id")
                        .remove("user_role")
                        .apply();

                stopMusic();
                finishAffinity();
            }
            return true;
        });

        popupMenu.show();
    }
}
