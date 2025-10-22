package com.example.holyinfantschool;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class VideosActivity extends AppCompatActivity {

    String[] videoIds = {
            "9_WBQISVHnw",
            "hTqtGJwsJVE",
            "Si5auXCYWDI",
            "gFuEoxh5hd4",
            "ZcX0gl-NFFg"
    };

    private boolean isMuted = false;
    private MediaPlayer mediaPlayer;
    private boolean isPausedBySystem = false; // 👈 track if music paused by lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos);

        // 🔊 Setup background music
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        ImageView backButton = findViewById(R.id.backButton);
        ImageView settingsButton = findViewById(R.id.settingsButton);

        // Back Button
        backButton.setOnClickListener(v -> {
            stopMusic(); // stop music when leaving
            Intent intent = new Intent(VideosActivity.this, Categorypage.class);
            startActivity(intent);
            finish();
        });

        // Settings Popup
        settingsButton.setOnClickListener(v -> showSettingsMenu(settingsButton));

        // Video Thumbnails
        ImageView video1 = findViewById(R.id.video1);
        ImageView video2 = findViewById(R.id.video2);
        ImageView video3 = findViewById(R.id.video3);
        ImageView video4 = findViewById(R.id.video4);
        ImageView video5 = findViewById(R.id.video5);

        loadThumbnail(video1, videoIds[0]);
        loadThumbnail(video2, videoIds[1]);
        loadThumbnail(video3, videoIds[2]);
        loadThumbnail(video4, videoIds[3]);
        loadThumbnail(video5, videoIds[4]);

        video1.setOnClickListener(v -> openYouTube(videoIds[0]));
        video2.setOnClickListener(v -> openYouTube(videoIds[1]));
        video3.setOnClickListener(v -> openYouTube(videoIds[2]));
        video4.setOnClickListener(v -> openYouTube(videoIds[3]));
        video5.setOnClickListener(v -> openYouTube(videoIds[4]));
    }

    private void loadThumbnail(ImageView imageView, String videoId) {
        String url = "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg";
        Glide.with(this)
                .load(url)
                .centerCrop()
                .into(imageView);
    }

    private void openYouTube(String videoId) {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.youtube.com/watch?v=" + videoId));
        startActivity(intent);
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
                stopMusic();   // release player cleanly
                finishAffinity(); // close app completely
            }
            return true;
        });

        popupMenu.show();
    }

    // 🔇 Mute background music
    private void muteDevice() {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(0f, 0f);
        }
    }

    // 🔊 Unmute background music
    private void unmuteDevice() {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(1f, 1f);
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        }
    }

    // ⏸ Pause background music
    private void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPausedBySystem = true;
        }
    }

    // ▶️ Resume music when coming back
    private void resumeMusic() {
        if (mediaPlayer != null && isPausedBySystem && !isMuted) {
            try {
                mediaPlayer.start();
                isPausedBySystem = false;
            } catch (IllegalStateException ignored) {}
        }
    }

    // 🧭 App lifecycle handling
    @Override
    protected void onPause() {
        super.onPause();
        // App minimized or another activity comes up
        pauseMusic();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // App returns to foreground
        resumeMusic();
    }

    // ⏹️ Stop and release background music
    private void stopMusic() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
            } catch (IllegalStateException ignored) {}
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMusic();
    }
}
