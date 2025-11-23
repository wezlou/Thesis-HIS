package com.example.holyinfantschool;

import android.animation.ValueAnimator;
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
import java.util.Locale;

public class StoryDetailActivity extends AppCompatActivity {

    private TextToSpeech tts;
    private String storyContent;
    private MediaPlayer mediaPlayer;
    private boolean isMuted = false;
    private float currentVolume = 1f;
    private final Handler handler = new Handler();

    private Button playStopButton;
    private Animation bounceAnim;
    private ScrollView scrollView;
    private TextView contentView;
    private ProgressBar loadingBar;

    private Runnable revealRunnable;
    private Handler revealHandler = new Handler();
    private boolean isRevealing = false;
    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_detail);

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
                tts.setLanguage(Locale.ENGLISH);
                tts.setSpeechRate(0.9f);
                tts.setPitch(1.1f);

                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        if (isFinishing() || isDestroyed()) return;
                        runOnUiThread(() -> {
                            fadeOutMusic();
                            playStopButton.startAnimation(bounceAnim);
                            startTextEntranceAnimation();
                        });
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        if (isFinishing() || isDestroyed()) return;
                        runOnUiThread(() -> stopPlayState());
                    }

                    @Override
                    public void onError(String utteranceId) {
                        if (isFinishing() || isDestroyed()) return;
                        runOnUiThread(() -> stopPlayState());
                    }
                });
            }
        });

        playStopButton.setOnClickListener(v -> {
            if (isPlaying) stopReading();
            else startReading();
        });

        backButton.setOnClickListener(v -> {
            stopReading();
            stopMusic();
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        settingsButton.setOnClickListener(v -> showSettingsMenu(settingsButton));
    }

    private void startReading() {
        isPlaying = true;
        playStopButton.setText("⏹ Stop");

        if (isRevealing && revealRunnable != null)
            revealHandler.removeCallbacks(revealRunnable);

        isRevealing = false;
        contentView.setText("");
        loadingBar.setVisibility(View.VISIBLE);
        startRevealAnimation();

        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "STORY_TTS");
        tts.speak(storyContent, TextToSpeech.QUEUE_FLUSH, params, "STORY_TTS");
    }

    private void stopReading() {
        isPlaying = false;
        playStopButton.setText("🎧 Listen");

        if (tts != null) tts.stop();

        if (isRevealing && revealRunnable != null)
            revealHandler.removeCallbacks(revealRunnable);

        isRevealing = false;
        playStopButton.clearAnimation();
        loadingBar.setVisibility(View.INVISIBLE);
        fadeInMusic();
    }

    private void stopPlayState() {
        if (isFinishing() || isDestroyed()) return;

        isPlaying = false;
        playStopButton.setText("🎧 Listen");
        playStopButton.clearAnimation();
        loadingBar.setVisibility(View.INVISIBLE);
        fadeInMusic();
    }

    private void startTextEntranceAnimation() {
        contentView.setAlpha(0f);
        contentView.setTranslationY(300f);
        contentView.animate()
                .translationY(0f)
                .alpha(1f)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(1200)
                .start();

        handler.postDelayed(() -> smoothScroll(scrollView), 1200);
    }

    private void smoothScroll(ScrollView scrollView) {
        if (scrollView.getChildAt(0) == null) return;

        int fullHeight = scrollView.getChildAt(0).getHeight();
        int visibleHeight = scrollView.getHeight();
        int range = fullHeight - visibleHeight;
        if (range <= 0) return;

        ValueAnimator animator = ValueAnimator.ofInt(range, 0);
        animator.setDuration(20000);
        animator.addUpdateListener(animation ->
                scrollView.scrollTo(0, (int) animation.getAnimatedValue()));
        animator.start();
    }

    private void startRevealAnimation() {
        if (isRevealing && revealRunnable != null)
            revealHandler.removeCallbacks(revealRunnable);

        isRevealing = true;
        String text = storyContent;
        contentView.setText("");
        final int len = text.length();
        final int delay = 30;

        revealRunnable = new Runnable() {
            int index = 0;
            @Override
            public void run() {
                if (isFinishing() || isDestroyed()) return;

                if (index < len) {
                    contentView.append(String.valueOf(text.charAt(index)));
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

    private void showSettingsMenu(ImageView anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add(isMuted ? "Unmute 🔊" : "Mute 🔇");
        menu.getMenu().add("Exit ❌");

        menu.setOnMenuItemClickListener(item -> {
            String t = item.getTitle().toString();

            if (t.contains("Mute")) {
                muteDevice();
                isMuted = true;
            } else if (t.contains("Unmute")) {
                unmuteDevice();
                isMuted = false;
            } else if (t.contains("Exit")) {
                stopReading();
                stopMusic();
                finishAffinity();
            }
            return true;
        });

        menu.show();
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

    private void fadeOutMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying() && !isMuted) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isFinishing() || isDestroyed()) return;

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
    }

    private void fadeInMusic() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying() && !isMuted) {
            mediaPlayer.start();
            currentVolume = 0f;

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isFinishing() || isDestroyed()) return;

                    if (currentVolume < 1f) {
                        currentVolume += 0.1f;
                        mediaPlayer.setVolume(currentVolume, currentVolume);
                        handler.postDelayed(this, 120);
                    }
                }
            }, 120);
        }
    }

    private void stopMusic() {
        if (mediaPlayer != null) {
            try { mediaPlayer.stop(); } catch (Exception ignored) {}
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

        if (isRevealing && revealRunnable != null)
            revealHandler.removeCallbacks(revealRunnable);

        stopMusic();
        super.onDestroy();
    }
}
