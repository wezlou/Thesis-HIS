package com.example.holyinfantschool;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupMenu;
import android.animation.ValueAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Numberquiz extends AppCompatActivity {

    private FrameLayout animalsContainer, animalsLayer, feedbackOverlay;
    private Button option1, option2, option3;
    private ImageView backgroundScene, backBtn, settingsButton;
    private TextView questionText;

    private final Random random = new Random();
    private int currentLevel = 1;
    private int correctAnswer;

    public static int totalCorrect = 0;
    public static int totalIncorrect = 0;

    private MediaPlayer mediaPlayer;
    private MediaPlayer correctSound, incorrectSound;
    private boolean isMuted = false;
    private boolean isPausedBySystem = false;

    private int[] backgrounds = {
            R.drawable.farm_bg, R.drawable.jungle_bg, R.drawable.ocean_bg,
            R.drawable.desert_bg, R.drawable.arctic_bg
    };

    private int[] animals = {
            R.drawable.cows, R.drawable.pigs, R.drawable.chickens,
            R.drawable.lions, R.drawable.elephants, R.drawable.m2,
            R.drawable.fish, R.drawable.crabs, R.drawable.turtles,
            R.drawable.camels, R.drawable.penguins, R.drawable.horses,
            R.drawable.dog, R.drawable.cat, R.drawable.sheep
    };

    private String[] animalNames = {
            "cows", "pigs", "chickens",
            "lions", "elephants", "monkeys",
            "fish", "crabs", "turtles",
            "camels", "penguins", "horses",
            "dogs", "cats", "sheep"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.number_quiz);

        // 🎵 Background Music
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        // 🔊 Feedback sounds
        correctSound = MediaPlayer.create(this, R.raw.correct);
        incorrectSound = MediaPlayer.create(this, R.raw.incorrect);

        animalsContainer = findViewById(R.id.animalsContainer);
        animalsLayer = findViewById(R.id.animalsLayer);
        feedbackOverlay = findViewById(R.id.feedbackOverlay);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        backgroundScene = findViewById(R.id.backgroundScene);
        questionText = findViewById(R.id.questionText);
        backBtn = findViewById(R.id.backbtn);
        settingsButton = findViewById(R.id.settingsButton);

        totalCorrect = 0;
        totalIncorrect = 0;

        loadLevel(currentLevel);

        // 🔙 Back button
        backBtn.setOnClickListener(v -> {
            stopMusic();
            Intent intent = new Intent(this, QuizActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // ⚙️ Settings menu
        settingsButton.setOnClickListener(v -> showSettingsMenu(settingsButton));
    }


    private void loadLevel(int level) {
        if (level > 15) {
            stopMusic();
            Intent intent = new Intent(this, NumberScoreActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        animalsLayer.removeAllViews();

        int animalIndex = level - 1;
        int backgroundIndex = animalIndex / 3;

        backgroundScene.setImageResource(backgrounds[backgroundIndex]);
        int animalRes = animals[animalIndex];
        String animalName = animalNames[animalIndex];

        correctAnswer = 3 + random.nextInt(6);
        questionText.setText("How many " + animalName + " are here?");

        // ⚡ Spawn after layout is measured
        animalsLayer.post(() -> {

            animalsLayer.removeAllViews();

            int containerW = animalsLayer.getWidth();
            int containerH = animalsLayer.getHeight();

            if (containerW == 0 || containerH == 0) {
                containerW = 800;
                containerH = 800;
            }

            int animalSize = 200;

            // Center spawn logic
            int centerW = containerW / 2;
            int centerH = containerH / 2;

            int spreadX = containerW / 4;  // controlled random movement
            int spreadY = containerH / 4;

            for (int i = 0; i < correctAnswer; i++) {

                int x = centerW + random.nextInt(spreadX) - spreadX / 2;
                int y = centerH + random.nextInt(spreadY) - spreadY / 2;

                // Clamp to screen
                x = Math.max(0, Math.min(x, containerW - animalSize));
                y = Math.max(0, Math.min(y, containerH - animalSize));

                ImageView animal = new ImageView(this);
                animal.setImageResource(animalRes);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(animalSize, animalSize);
                animal.setLayoutParams(params);
                animal.setX(x);
                animal.setY(y);

                animalsLayer.addView(animal);
            }

        });

        // OPTIONS
        List<Integer> options = new ArrayList<>();
        options.add(correctAnswer);
        while (options.size() < 3) {
            int num = 2 + random.nextInt(9);
            if (!options.contains(num)) options.add(num);
        }
        Collections.shuffle(options);

        option1.setText(String.valueOf(options.get(0)));
        option2.setText(String.valueOf(options.get(1)));
        option3.setText(String.valueOf(options.get(2)));

        option1.setOnClickListener(v -> checkAnswer(options.get(0)));
        option2.setOnClickListener(v -> checkAnswer(options.get(1)));
        option3.setOnClickListener(v -> checkAnswer(options.get(2)));
    }

    private void checkAnswer(int selected) {
        boolean isCorrect = (selected == correctAnswer);
        showFeedback(isCorrect);

        if (isCorrect) totalCorrect++;
        else totalIncorrect++;

        new android.os.Handler().postDelayed(() -> {
            currentLevel++;
            loadLevel(currentLevel);
        }, 2000);
    }

    private void showFeedback(boolean isCorrect) {
        feedbackOverlay.setVisibility(FrameLayout.VISIBLE);
        feedbackOverlay.bringToFront();

        // 🎵 Sound
        if (!isMuted) {
            if (isCorrect && correctSound != null) correctSound.start();
            else if (!isCorrect && incorrectSound != null) incorrectSound.start();
        }

        // 🌈 Glow color animation
        int glowColor = isCorrect ? Color.parseColor("#AA00FF00") : Color.parseColor("#AAFF0000");

        GradientDrawable gradient = new GradientDrawable();
        gradient.setShape(GradientDrawable.RECTANGLE);
        gradient.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        gradient.setGradientCenter(0.5f, 0.5f);
        gradient.setColors(new int[]{glowColor, Color.TRANSPARENT});
        feedbackOverlay.setBackground(gradient);

        ValueAnimator animator = ValueAnimator.ofFloat(1.5f, 0f);
        animator.setDuration(1200);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();

            float radius = 1200f * progress + 400f;
            gradient.setGradientRadius(radius);

            feedbackOverlay.setAlpha(1f - progress * 0.8f);
        });

        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                feedbackOverlay.setVisibility(FrameLayout.GONE);
            }
        });

        animator.start();
    }


    private void showSettingsMenu(ImageView anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.getMenu().add(isMuted ? "Unmute 🔊" : "Mute 🔇");
        popupMenu.getMenu().add("Exit ❌");

        popupMenu.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if (title.contains("Mute")) {
                muteDevice();
                isMuted = true;
                Toast.makeText(this, "Muted 🔇", Toast.LENGTH_SHORT).show();
            } else if (title.contains("Unmute")) {
                unmuteDevice();
                isMuted = false;
                Toast.makeText(this, "Unmuted 🔊", Toast.LENGTH_SHORT).show();
            } else if (title.contains("Exit")) {
                stopMusic();
                finishAffinity();
            }
            return true;
        });

        popupMenu.show();
    }


    private void muteDevice() {
        if (mediaPlayer != null) mediaPlayer.setVolume(0f, 0f);
    }

    private void unmuteDevice() {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(1f, 1f);
            if (!mediaPlayer.isPlaying()) mediaPlayer.start();
        }
    }

    private void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPausedBySystem = true;
        }
    }

    private void resumeMusic() {
        if (mediaPlayer != null && isPausedBySystem && !isMuted) {
            try {
                mediaPlayer.start();
                isPausedBySystem = false;
            } catch (IllegalStateException ignored) {}
        }
    }

    private void stopMusic() {
        if (mediaPlayer != null) {
            try { mediaPlayer.stop(); } catch (IllegalStateException ignored) {}
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (correctSound != null) correctSound.release();
        if (incorrectSound != null) incorrectSound.release();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseMusic();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeMusic();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMusic();
    }
}
