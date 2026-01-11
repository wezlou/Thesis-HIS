package com.example.holyinfantschool;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class StoryDetailActivity extends AppCompatActivity {

    private TextToSpeech tts;
    private boolean isTtsReady = false;

    private String storyContent;
    private MediaPlayer mediaPlayer;
    private boolean isMuted = false;
    private float currentVolume = 1f;

    private Button playStopButton;
    private ScrollView scrollView;
    private TextView contentView;
    private ProgressBar loadingBar;
    private Animation bounceAnim;

    private final Handler handler = new Handler();
    private final Handler revealHandler = new Handler();
    private Runnable revealRunnable;

    private boolean isRevealing = false;
    private boolean isPlaying = false;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_detail);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        TextView titleView = findViewById(R.id.storyTitle);
        contentView = findViewById(R.id.storyContent);
        playStopButton = findViewById(R.id.playStopButton);
        loadingBar = findViewById(R.id.loadingBar);
        ImageView backButton = findViewById(R.id.backbtn);
        ImageView settingsButton = findViewById(R.id.settingsButton);
        scrollView = findViewById(R.id.storyScroll);

        bounceAnim = AnimationUtils.loadAnimation(this, R.anim.button_bounce);

        String storyTitle = getIntent().getStringExtra("title");
        storyContent = getIntent().getStringExtra("content");

        titleView.setText(storyTitle);
        contentView.setText(storyContent);
        loadingBar.setVisibility(View.INVISIBLE);

        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true;
                tts.setLanguage(Locale.ENGLISH);
                tts.setSpeechRate(0.9f);
                tts.setPitch(1.1f);

                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        if (isFinishing() || isDestroyed()) return;
                        runOnUiThread(() -> {
                            fadeOutMusic();
                            loadingBar.setVisibility(View.VISIBLE);
                            playStopButton.startAnimation(bounceAnim);
                            startRevealAnimation();
                            startTextEntranceAnimation();
                        });
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        if (isFinishing() || isDestroyed()) return;
                        runOnUiThread(StoryDetailActivity.this::stopPlayState);
                    }

                    @Override
                    public void onError(String utteranceId) {
                        if (isFinishing() || isDestroyed()) return;
                        runOnUiThread(StoryDetailActivity.this::stopPlayState);
                    }
                });
            }
        });

        playStopButton.setOnClickListener(v -> {
            playStopButton.setEnabled(false);
            playStopButton.postDelayed(() -> playStopButton.setEnabled(true), 600);
            if (isPlaying) stopReading();
            else startReading();
        });

        backButton.setOnClickListener(v -> {
            if (revealRunnable != null) {
                revealHandler.removeCallbacks(revealRunnable);
                revealRunnable = null;
            }
            if (tts != null) {
                tts.stop();
                tts.setOnUtteranceProgressListener(null);
            }
            stopMusic();
            isPlaying = false;
            isRevealing = false;
            handler.post(() -> {
                Intent intent = new Intent(this, StoriesActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        });

        settingsButton.setOnClickListener(v -> showSettingsMenu(settingsButton));
    }

    private void startReading() {
        if (!isTtsReady) return;
        isPlaying = true;
        playStopButton.setText("⏹ Stop");
        if (isRevealing && revealRunnable != null) revealHandler.removeCallbacks(revealRunnable);
        contentView.setText("");
        loadingBar.setVisibility(View.VISIBLE);
        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "STORY");
        tts.speak(storyContent, TextToSpeech.QUEUE_FLUSH, params, "STORY");
    }

    private void stopReading() {
        isPlaying = false;
        playStopButton.setText("🎧 Listen");
        if (tts != null) tts.stop();
        if (isRevealing && revealRunnable != null) revealHandler.removeCallbacks(revealRunnable);
        loadingBar.setVisibility(View.INVISIBLE);
        playStopButton.clearAnimation();
        fadeInMusic();
    }

    private void stopPlayState() {
        isPlaying = false;
        playStopButton.setText("🎧 Listen");
        loadingBar.setVisibility(View.INVISIBLE);
        playStopButton.clearAnimation();
        fadeInMusic();
    }

    private void startRevealAnimation() {
        isRevealing = true;
        contentView.setText("");
        final int delay = 30;
        final int len = storyContent.length();
        revealRunnable = new Runnable() {
            int index = 0;
            @Override
            public void run() {
                if (index < len) {
                    contentView.append(String.valueOf(storyContent.charAt(index)));
                    scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
                    index++;
                    revealHandler.postDelayed(this, delay);
                } else {
                    isRevealing = false;
                    loadingBar.setVisibility(View.INVISIBLE);
                }
            }
        };
        revealRunnable.run();
    }

    private void startTextEntranceAnimation() {
        contentView.setAlpha(0f);
        contentView.setTranslationY(300f);
        contentView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(1200)
                .setInterpolator(new DecelerateInterpolator())
                .start();
        handler.postDelayed(() -> smoothScroll(scrollView), 1200);
    }

    private void smoothScroll(ScrollView scrollView) {
        if (scrollView.getChildAt(0) == null) return;
        int range = scrollView.getChildAt(0).getHeight() - scrollView.getHeight();
        if (range <= 0) return;
        ValueAnimator animator = ValueAnimator.ofInt(0, range);
        animator.setDuration(20000);
        animator.addUpdateListener(a -> scrollView.scrollTo(0, (int) a.getAnimatedValue()));
        animator.start();
    }

    private void fadeOutMusic() {
        if (mediaPlayer == null || isMuted) return;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentVolume > 0.1f) {
                    currentVolume -= 0.1f;
                    mediaPlayer.setVolume(currentVolume, currentVolume);
                    handler.postDelayed(this, 120);
                } else {
                    mediaPlayer.pause();
                    currentVolume = 1f;
                }
            }
        }, 120);
    }

    private void fadeInMusic() {
        if (mediaPlayer == null || isMuted || mediaPlayer.isPlaying()) return;
        mediaPlayer.start();
        currentVolume = 0f;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentVolume < 1f) {
                    currentVolume += 0.1f;
                    mediaPlayer.setVolume(currentVolume, currentVolume);
                    handler.postDelayed(this, 120);
                }
            }
        }, 120);
    }

    private void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void saveLogoutAndExit() {
        if (auth.getCurrentUser() == null) {
            stopMusic();
            finishAffinity();
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        String email = auth.getCurrentUser().getEmail();
        String role = getSharedPreferences("HIS_APP", MODE_PRIVATE)
                .getString("user_role", "student");

        Map<String, Object> data = new HashMap<>();
        data.put("action", "logout");
        data.put("uid", uid);
        data.put("email", email);
        data.put("role", role);
        data.put("loginType", "firebase");
        data.put("device", "Android");
        data.put("timestamp", FieldValue.serverTimestamp());

        db.collection("auth_history")
                .add(data)
                .addOnCompleteListener(task -> {
                    auth.signOut();
                    getSharedPreferences("HIS_APP", MODE_PRIVATE)
                            .edit()
                            .remove("session_id")
                            .remove("user_role")
                            .apply();
                    stopMusic();
                    finishAffinity();
                });
    }

    private void showSettingsMenu(ImageView anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add(isMuted ? "Unmute 🔊" : "Mute 🔇");
        menu.getMenu().add("Exit ❌");

        menu.setOnMenuItemClickListener(item -> {
            if (item.getTitle().toString().contains("Mute")) {
                isMuted = true;
                if (mediaPlayer != null) mediaPlayer.setVolume(0f, 0f);
            } else if (item.getTitle().toString().contains("Unmute")) {
                isMuted = false;
                fadeInMusic();
            } else {
                saveLogoutAndExit();
            }
            return true;
        });

        menu.show();
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (revealRunnable != null) revealHandler.removeCallbacks(revealRunnable);
        stopMusic();
        super.onDestroy();
    }
}
