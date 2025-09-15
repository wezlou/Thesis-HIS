package com.example.holyinfantschool;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class StoryDetailActivity extends AppCompatActivity {

    private TextToSpeech tts;
    private String storyContent;
    private MediaPlayer mediaPlayer;
    private boolean isMuted = false; // track mute state
    private float currentVolume = 1f; // 0.0 - 1.0
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_detail);

        TextView titleView = findViewById(R.id.storyTitle);
        TextView contentView = findViewById(R.id.storyContent);
        Button playButton = findViewById(R.id.playButton);
        ImageView backButton = findViewById(R.id.backButton);
        ImageView settingsButton = findViewById(R.id.settingsButton);

        // ✅ Get story data from intent
        String storyTitle = getIntent().getStringExtra("title");
        storyContent = getIntent().getStringExtra("content");

        titleView.setText(storyTitle);
        contentView.setText(storyContent);

        // ✅ Setup background music
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        // ✅ Initialize TTS
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.ENGLISH);

                // Listen for TTS progress
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        runOnUiThread(() -> fadeOutMusic()); // fade out when TTS starts
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        runOnUiThread(() -> fadeInMusic()); // fade in when TTS finishes
                    }

                    @Override
                    public void onError(String utteranceId) {
                        runOnUiThread(() -> fadeInMusic()); // also fade in on error
                    }
                });
            }
        });

        // ✅ Play button (TTS reading)
        playButton.setOnClickListener(v -> {
            if (tts != null) {
                fadeOutMusic(); // fade out immediately before speaking
                tts.speak(storyContent, TextToSpeech.QUEUE_FLUSH, null, "STORY_TTS");
            }
        });

        // ✅ Back Button
        backButton.setOnClickListener(v -> {
            stopMusic(); // stop music when leaving
            finish();    // go back
        });

        // ✅ Settings Button Popup
        settingsButton.setOnClickListener(v -> showSettingsMenu(settingsButton));
    }

    // ==============================
    // 🔧 Settings Menu
    // ==============================
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

    // ==============================
    // 🔊 Music Control
    // ==============================
    private void muteDevice() {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(0f, 0f);
        }
    }

    private void unmuteDevice() {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(1f, 1f);
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        }
    }

    private void fadeOutMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying() && !isMuted) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (currentVolume > 0.1f) {
                        currentVolume -= 0.1f;
                        mediaPlayer.setVolume(currentVolume, currentVolume);
                        handler.postDelayed(this, 100); // every 100ms
                    } else {
                        mediaPlayer.pause();
                        currentVolume = 1f; // reset for next play
                    }
                }
            }, 100);
        }
    }

    private void fadeInMusic() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying() && !isMuted) {
            mediaPlayer.start();
            currentVolume = 0f;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (currentVolume < 1f) {
                        currentVolume += 0.1f;
                        mediaPlayer.setVolume(currentVolume, currentVolume);
                        handler.postDelayed(this, 100); // every 100ms
                    }
                }
            }, 100);
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
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        stopMusic();
        super.onDestroy();
    }
}
