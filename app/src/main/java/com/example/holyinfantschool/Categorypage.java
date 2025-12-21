package com.example.holyinfantschool;

import android.app.AlertDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Categorypage extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private boolean isMuted = false;
    private ImageView volumeOn, volumeOff;
    private boolean settingsVisible = false;

    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categorypage);

        ImageView backTeacher = findViewById(R.id.backteacher); // LOGOUT
        ImageView teacherSetting = findViewById(R.id.teachersetting);
        ImageView watchVideos = findViewById(R.id.watch_videos);
        ImageView readStories = findViewById(R.id.read_stories);
        ImageView playQuiz = findViewById(R.id.play_quiz);
        ImageView announcement = findViewById(R.id.announcement);

        volumeOn = findViewById(R.id.volumeOn);
        volumeOff = findViewById(R.id.volumeOff);

        hideSettingsButtons();

        // 🔥 Firebase
        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        // 🎵 Background Music
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        // 🔙 LOGOUT (RATE FIRST)
        backTeacher.setOnClickListener(v -> checkIfRatedThenLogout());

        // ⚙ Settings toggle
        teacherSetting.setOnClickListener(v -> {
            if (settingsVisible) hideSettingsButtons();
            else showSettingsButtons();
            settingsVisible = !settingsVisible;
        });

        // 🔊 Volume controls
        volumeOn.setOnClickListener(v -> {
            mediaPlayer.setVolume(0, 0);
            isMuted = true;
            updateVolumeButtonsVisibility();
        });

        volumeOff.setOnClickListener(v -> {
            mediaPlayer.setVolume(1, 1);
            isMuted = false;
            updateVolumeButtonsVisibility();
        });

        // 📌 Navigation
        watchVideos.setOnClickListener(v -> {
            stopMusic();
            startActivity(new Intent(this, VideosActivity.class));
        });

        readStories.setOnClickListener(v -> {
            stopMusic();
            startActivity(new Intent(this, StoriesActivity.class));
        });

        playQuiz.setOnClickListener(v -> {
            stopMusic();
            startActivity(new Intent(this, QuizActivity.class));
        });

        announcement.setOnClickListener(v -> {
            stopMusic();
            startActivity(new Intent(this, splashstudenttask.class));
        });
    }

    private void checkIfRatedThenLogout() {

        if (userId == null) {
            performLogout();
            return;
        }

        db.collection("app_ratings")
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        performLogout(); // already rated
                    } else {
                        showRateDialog();
                    }
                })
                .addOnFailureListener(e -> performLogout());
    }

    private void showRateDialog() {

        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_rate_app, null);

        RatingBar ratingBar = view.findViewById(R.id.ratingBar);
        EditText commentInput = view.findViewById(R.id.commentInput);

        new AlertDialog.Builder(this)
                .setTitle("Rate Our App ⭐")
                .setView(view)
                .setCancelable(false) // ❌ no cancel, no skip
                .setPositiveButton("Done 👍", (dialog, which) -> {

                    int stars = (int) ratingBar.getRating();
                    if (stars == 0) stars = 5;

                    String comment = commentInput.getText().toString().trim();
                    saveRating(stars, comment);
                })
                .show();
    }

    private void saveRating(int stars, String comment) {

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("stars", stars);
        data.put("comment", comment);
        data.put("createdAt", FieldValue.serverTimestamp());

        db.collection("app_ratings")
                .document(userId)
                .set(data)
                .addOnCompleteListener(task -> performLogout());
    }

    // ======================= LOGOUT =======================

    private void performLogout() {

        FirebaseAuth.getInstance().signOut();

        getSharedPreferences("HIS_APP", MODE_PRIVATE)
                .edit()
                .remove("user_role")
                .apply();

        stopMusic();

        Intent intent = new Intent(Categorypage.this, Homepage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish();
    }

    // ======================= UI & MUSIC =======================

    private void hideSettingsButtons() {
        volumeOn.setVisibility(View.GONE);
        volumeOff.setVisibility(View.GONE);
    }

    private void showSettingsButtons() {
        updateVolumeButtonsVisibility();
    }

    private void updateVolumeButtonsVisibility() {
        volumeOff.setVisibility(isMuted ? View.VISIBLE : View.GONE);
        volumeOn.setVisibility(isMuted ? View.GONE : View.VISIBLE);
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
        if (mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null && !isMuted && !mediaPlayer.isPlaying())
            mediaPlayer.start();
    }

    @Override
    protected void onDestroy() {
        stopMusic();
        super.onDestroy();
    }
}
