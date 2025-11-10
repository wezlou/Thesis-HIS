package com.example.holyinfantschool;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Shape1Activity extends AppCompatActivity {

    public static int totalCorrect = 0;
    public static int totalIncorrect = 0;

    private ImageView questionShape, option1, option2, option3;
    private TextView levelText, questionText;
    private ImageView settingsButton, backButton;

    private int currentLevel = 1;
    private int correctOptionPosition;
    private final Random random = new Random();

    private MediaPlayer mediaPlayer;
    private boolean isMuted = false;
    private boolean isPausedBySystem = false;

    private final int totalLevels = 15;

    // 🎨 Question and Answer Setup
    private int[] questionShapes = {
            R.drawable.shape_q1, R.drawable.shape_q2, R.drawable.shape_q3,
            R.drawable.shape_q4, R.drawable.shape_q5, R.drawable.shape_q6,
            R.drawable.shape_q7, R.drawable.shape_q8, R.drawable.shape_q9,
            R.drawable.shape_q10, R.drawable.shape_q11, R.drawable.shape_q12,
            R.drawable.shape_q13, R.drawable.shape_q14, R.drawable.shape_q15
    };

    private int[] correctAnswers = {
            1, 2, 3, 4, 5, 6, 7, 8, 1, 1, 9, 10, 11, 12, 13
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shape1);

        // 🎵 Start background music
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        // 🔗 Bind views
        questionShape = findViewById(R.id.b2);
        option1 = findViewById(R.id.circle);
        option2 = findViewById(R.id.triangle);
        option3 = findViewById(R.id.square);
        levelText = findViewById(R.id.level);
        questionText = findViewById(R.id.question_shape);
        settingsButton = findViewById(R.id.settingsButton);
        backButton = findViewById(R.id.backbtn);

        settingsButton.setOnClickListener(v -> showSettingsMenu(settingsButton));

        backButton.setOnClickListener(v -> {
            stopMusic();
            Intent intent = new Intent(this, QuizActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // 🎲 Shuffle questions
        List<int[]> questionPairs = new ArrayList<>();
        for (int i = 0; i < questionShapes.length; i++) {
            questionPairs.add(new int[]{questionShapes[i], correctAnswers[i]});
        }
        Collections.shuffle(questionPairs);

        for (int i = 0; i < questionPairs.size(); i++) {
            questionShapes[i] = questionPairs.get(i)[0];
            correctAnswers[i] = questionPairs.get(i)[1];
        }

        totalCorrect = 0;
        totalIncorrect = 0;
        currentLevel = 1;
        loadLevel(currentLevel);

        // 🧩 Option click listeners
        option1.setOnClickListener(v -> checkAnswer(1));
        option2.setOnClickListener(v -> checkAnswer(2));
        option3.setOnClickListener(v -> checkAnswer(3));
    }

    // ⚙️ Settings menu
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
                finishAffinity(); // close app
            }
            return true;
        });

        popupMenu.show();
    }

    // 🔇 / 🔊 Sound controls
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

    // 🧠 Game logic
    private void loadLevel(int level) {
        if (level > questionShapes.length) {
            Intent intent = new Intent(this, ShapestotalSplash.class);
            startActivity(intent);
            finish();
            return;
        }

        questionText.setText("What shape is this?");
        levelText.setText("Level " + level);
        questionShape.setImageResource(questionShapes[level - 1]);

        int correctShapeType = correctAnswers[level - 1];
        loadOptionsByCode(correctShapeType);
    }

    private void loadOptionsByCode(int correctShapeType) {
        List<Integer> shapeTypes = new ArrayList<>();
        shapeTypes.add(correctShapeType);

        while (shapeTypes.size() < 3) {
            int randomType = 1 + random.nextInt(13);
            if (!shapeTypes.contains(randomType)) shapeTypes.add(randomType);
        }

        Collections.shuffle(shapeTypes);

        option1.setImageBitmap(createShapeBitmap(shapeTypes.get(0)));
        option2.setImageBitmap(createShapeBitmap(shapeTypes.get(1)));
        option3.setImageBitmap(createShapeBitmap(shapeTypes.get(2)));

        correctOptionPosition = shapeTypes.indexOf(correctShapeType) + 1;
    }

    private void checkAnswer(int selectedOption) {
        boolean isCorrect = (selectedOption == correctOptionPosition);

        questionShape.setDrawingCacheEnabled(true);
        Bitmap questionBitmap = Bitmap.createBitmap(questionShape.getDrawingCache());
        questionShape.setDrawingCacheEnabled(false);

        Bitmap chosenShape;
        if (selectedOption == 1) chosenShape = ((BitmapDrawable) option1.getDrawable()).getBitmap();
        else if (selectedOption == 2) chosenShape = ((BitmapDrawable) option2.getDrawable()).getBitmap();
        else chosenShape = ((BitmapDrawable) option3.getDrawable()).getBitmap();

        Intent intent;
        if (isCorrect) {
            totalCorrect++;
            intent = new Intent(this, shapecorrect.class);
        } else {
            totalIncorrect++;
            intent = new Intent(this, shapeincorrect.class);
        }

        intent.putExtra("question_shape", bitmapToByteArray(questionBitmap));
        intent.putExtra("answer_shape", bitmapToByteArray(chosenShape));
        startActivity(intent);

        new android.os.Handler().postDelayed(() -> {
            currentLevel++;
            loadLevel(currentLevel);
        }, 1500);
    }

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    // 🎨 Shape drawing methods
    private Bitmap createShapeBitmap(int type) {
        int color = generateVisibleColor();
        switch (type) {
            case 1: return drawHexagon(color);
            case 2: return drawDiamond(color);
            case 3: return drawPentagon(color);
            case 4: return drawSquare(color);
            case 5: return drawCross(color);
            case 6: return drawOval(color);
            case 7: return drawTriangle(color);
            case 8: return drawStar(color);
            case 9: return drawArrow(color);
            case 10: return drawHeart(color);
            case 11: return drawRectangle(color);
            case 12: return drawCrescent(color);
            case 13: return drawCircle(color);
            default: return drawSquare(color);
        }
    }

    private int generateVisibleColor() {
        int r, g, b;
        do {
            r = random.nextInt(256);
            g = random.nextInt(256);
            b = random.nextInt(256);
        } while ((g > r + 40 && g > b + 40) || (r + g + b < 150) || (r + g + b > 720));
        return Color.rgb(r, g, b);
    }

    private Bitmap drawCircle(int color) {
        int s = 150;
        Bitmap b = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        c.drawCircle(s / 2f, s / 2f, s / 2f - 10, p);
        return b;
    }

    private Bitmap drawTriangle(int color) {
        int s = 150;
        Bitmap b = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        Path path = new Path();
        path.moveTo(s / 2f, 10);
        path.lineTo(s - 10, s - 10);
        path.lineTo(10, s - 10);
        path.close();
        c.drawPath(path, p);
        return b;
    }

    private Bitmap drawSquare(int color) {
        int s = 150;
        Bitmap b = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        c.drawRect(10, 10, s - 10, s - 10, p);
        return b;
    }

    private Bitmap drawHexagon(int color) {
        int s = 150;
        Bitmap b = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        float cx = s / 2f, cy = s / 2f, r = s / 2f - 10;
        Path path = new Path();
        for (int i = 0; i < 6; i++) {
            double a = Math.toRadians(60 * i - 30);
            float x = (float) (cx + r * Math.cos(a));
            float y = (float) (cy + r * Math.sin(a));
            if (i == 0) path.moveTo(x, y); else path.lineTo(x, y);
        }
        path.close();
        c.drawPath(path, p);
        return b;
    }

    private Bitmap drawDiamond(int color) {
        int s = 150;
        Bitmap b = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        Path path = new Path();
        path.moveTo(s / 2f, 10);
        path.lineTo(s - 10, s / 2f);
        path.lineTo(s / 2f, s - 10);
        path.lineTo(10, s / 2f);
        path.close();
        c.drawPath(path, p);
        return b;
    }

    private Bitmap drawPentagon(int color) {
        int s = 150;
        Bitmap b = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        float cx = s / 2f, cy = s / 2f, r = s / 2f - 10;
        Path path = new Path();
        for (int i = 0; i < 5; i++) {
            double a = Math.toRadians(72 * i - 90);
            float x = (float) (cx + r * Math.cos(a));
            float y = (float) (cy + r * Math.sin(a));
            if (i == 0) path.moveTo(x, y); else path.lineTo(x, y);
        }
        path.close();
        c.drawPath(path, p);
        return b;
    }

    private Bitmap drawCross(int color) {
        int s = 150;
        Bitmap b = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        int w = 40;
        c.drawRect(10, s / 2f - w / 2f, s - 10, s / 2f + w / 2f, p);
        c.drawRect(s / 2f - w / 2f, 10, s / 2f + w / 2f, s - 10, p);
        return b;
    }

    private Bitmap drawOval(int color) {
        int s = 150;
        Bitmap b = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        c.drawOval(20, 40, s - 20, s - 40, p);
        return b;
    }

    private Bitmap drawStar(int color) {
        int s = 150;
        Bitmap b = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        float cx = s / 2f, cy = s / 2f;
        float rOuter = s / 2f - 10, rInner = rOuter / 2.5f;
        Path path = new Path();
        for (int i = 0; i < 10; i++) {
            double a = Math.toRadians(i * 36 - 90);
            float r = (i % 2 == 0) ? rOuter : rInner;
            float x = (float) (cx + r * Math.cos(a));
            float y = (float) (cy + r * Math.sin(a));
            if (i == 0) path.moveTo(x, y); else path.lineTo(x, y);
        }
        path.close();
        c.drawPath(path, p);
        return b;
    }

    private Bitmap drawArrow(int color) {
        int s = 150;
        Bitmap b = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        p.setStyle(Paint.Style.FILL);

        Path path = new Path();

        float shaftStart = s * 0.15f;
        float shaftEnd = s * 0.65f;
        float centerY = s / 2f;
        float headWidth = s * 0.25f;
        float shaftHeight = s * 0.25f;

        path.moveTo(shaftStart, centerY - shaftHeight / 2f);
        path.lineTo(shaftEnd, centerY - shaftHeight / 2f);
        path.lineTo(shaftEnd, centerY - shaftHeight);
        path.lineTo(s - shaftStart, centerY);
        path.lineTo(shaftEnd, centerY + shaftHeight);
        path.lineTo(shaftEnd, centerY + shaftHeight / 2f);
        path.lineTo(shaftStart, centerY + shaftHeight / 2f);
        path.close();

        c.drawPath(path, p);
        return b;
    }


    private Bitmap drawHeart(int color) {
        int s = 150;
        Bitmap b = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        p.setStyle(Paint.Style.FILL);

        Path path = new Path();

        float mid = s / 2f;
        float top = s / 5f;
        float bottom = s - 20;
        float left = s / 6f;
        float right = s - left;

        path.moveTo(mid, bottom);

        path.cubicTo(left - 10, s * 3f / 4f, left, top, mid, s / 3f);

        path.cubicTo(right, top, right + 10, s * 3f / 4f, mid, bottom);

        path.close();

        c.drawPath(path, p);
        return b;
    }


    private Bitmap drawRectangle(int color) {
        int s = 150;
        Bitmap b = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        c.drawRect(20, 40, s - 20, s - 40, p);
        return b;
    }

    private Bitmap drawCrescent(int color) {
        int s = 150;
        Bitmap b = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);

        // Draw the main circle
        c.drawCircle(s / 2f, s / 2f, s / 2f - 10, p);

        // Erase part of it to make the crescent
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        c.drawCircle(s / 2f + 20, s / 2f, s / 2f - 20, p);

        p.setXfermode(null);
        return b;
    }
}
