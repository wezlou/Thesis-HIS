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
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Shape1Activity extends AppCompatActivity {

    public static int totalCorrect = 0;
    public static int totalIncorrect = 0;

    private ImageView questionShape, option1, option2, option3;
    private TextView questionText;
    private ImageView settingsButton, backButton;
    private ProgressBar gameProgressBar;
    private TextView gameProgressText;

    private int currentLevel = 1;
    private int correctOptionPosition;
    private final Random random = new Random();

    private MediaPlayer mediaPlayer;
    private boolean isMuted = false;

    private final int totalLevels = 15;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

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

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        bindViews();
        setupButtons();

        shuffleQuestions();

        totalCorrect = 0;
        totalIncorrect = 0;
        currentLevel = 1;
        loadLevel(currentLevel);
    }

    private void bindViews() {
        questionShape = findViewById(R.id.b2);
        option1 = findViewById(R.id.circle);
        option2 = findViewById(R.id.triangle);
        option3 = findViewById(R.id.square);
        questionText = findViewById(R.id.question_shape);
        settingsButton = findViewById(R.id.settingsButton);
        backButton = findViewById(R.id.backbtn);
        gameProgressBar = findViewById(R.id.gameProgressBar);
        gameProgressText = findViewById(R.id.gameProgressText);

        gameProgressBar.setMax(totalLevels);

        option1.setOnClickListener(v -> checkAnswer(1));
        option2.setOnClickListener(v -> checkAnswer(2));
        option3.setOnClickListener(v -> checkAnswer(3));
    }

    private void setupButtons() {

        settingsButton.setOnClickListener(v -> showSettingsMenu(settingsButton));

        backButton.setOnClickListener(v -> {
            handleLogoutAndExit(false);
            startActivity(new Intent(this, QuizActivity.class));
            finish();
        });
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

        FirebaseFirestore.getInstance()
                .collection("auth_history")
                .add(data);
    }

    private void handleLogoutAndExit(boolean closeApp) {

        saveLogoutHistory();

        FirebaseAuth.getInstance().signOut();

        getSharedPreferences("HIS_APP", MODE_PRIVATE)
                .edit()
                .remove("session_id")
                .apply();

        stopMusic();

        if (closeApp) {
            finishAffinity();
        }
    }

    private void showSettingsMenu(ImageView anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add(isMuted ? "Unmute 🔊" : "Mute 🔇");
        menu.getMenu().add("Exit ❌");

        menu.setOnMenuItemClickListener(item -> {

            String title = item.getTitle().toString();

            if (title.contains("Mute")) {
                mediaPlayer.setVolume(0f, 0f);
                isMuted = true;
            } else if (title.contains("Unmute")) {
                mediaPlayer.setVolume(1f, 1f);
                isMuted = false;
            } else {
                handleLogoutAndExit(true);
            }
            return true;
        });

        menu.show();
    }

    private void shuffleQuestions() {
        List<int[]> pairs = new ArrayList<>();
        for (int i = 0; i < questionShapes.length; i++) {
            pairs.add(new int[]{questionShapes[i], correctAnswers[i]});
        }
        Collections.shuffle(pairs);

        for (int i = 0; i < pairs.size(); i++) {
            questionShapes[i] = pairs.get(i)[0];
            correctAnswers[i] = pairs.get(i)[1];
        }
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
        if (mediaPlayer != null) mediaPlayer.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null && !isMuted) mediaPlayer.start();
    }

    @Override
    protected void onDestroy() {
        stopMusic();
        super.onDestroy();
    }

    private void loadLevel(int level) {
        if (level > questionShapes.length) {
            Intent intent = new Intent(this, ShapestotalSplash.class);
            startActivity(intent);
            finish();
            return;
        }

        questionText. setText("What shape is this?");
        questionShape. setImageResource(questionShapes[level - 1]);

        int correctShapeType = correctAnswers[level - 1];
        loadOptionsByCode(correctShapeType);

        updateProgressBar(); // 🎯 ADD THIS LINE
    }

    private void updateProgressBar() {
        int progressPercentage = (currentLevel - 1) * 100 / totalLevels;

        if (progressPercentage < 0) progressPercentage = 0;
        if (progressPercentage > 100) progressPercentage = 100;

        gameProgressBar.setProgress(currentLevel - 1);
        gameProgressText.setText(progressPercentage + "%");
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
