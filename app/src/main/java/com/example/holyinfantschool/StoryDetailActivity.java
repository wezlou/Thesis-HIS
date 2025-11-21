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
                        runOnUiThread(() -> {
                            fadeOutMusic();
                            playStopButton.startAnimation(bounceAnim);
                            startBottomUpTextAnimation();
                        });
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        runOnUiThread(() -> {
                            stopPlayingState();
                        });
                    }

                    @Override
                    public void onError(String utteranceId) {
                        runOnUiThread(() -> stopPlayingState());
                    }
                });
            }
        });

        playStopButton.setOnClickListener(v -> {
            if (isPlaying) {
                stopReading();
            } else {
                startReading();
            }
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

        if (isRevealing && revealRunnable != null) {
            revealHandler.removeCallbacks(revealRunnable);
            isRevealing = false;
        }

        contentView.setText("");
        loadingBar.setVisibility(View.VISIBLE);
        startBottomUpReveal(contentView);

        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "STORY_TTS");
        tts.speak(storyContent, TextToSpeech.QUEUE_FLUSH, params, "STORY_TTS");
    }

    private void stopReading() {
        isPlaying = false;
        playStopButton.setText("🎧 Listen");

        if (tts != null) tts.stop();
        if (isRevealing && revealRunnable != null) revealHandler.removeCallbacks(revealRunnable);
        isRevealing = false;
        playStopButton.clearAnimation();
        loadingBar.setVisibility(View.INVISIBLE);
        fadeInMusic();
    }

    private void stopPlayingState() {
        isPlaying = false;
        playStopButton.setText("🎧 Listen");
        playStopButton.clearAnimation();
        loadingBar.setVisibility(View.INVISIBLE);
        fadeInMusic();
    }

    private void startBottomUpTextAnimation() {
        contentView.setAlpha(0f);
        contentView.setTranslationY(300f);

        contentView.animate()
                .translationY(0f)
                .alpha(1f)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(1500)
                .start();

        handler.postDelayed(() -> smoothScrollStory(scrollView), 1500);
    }

    private void smoothScrollStory(ScrollView scrollView) {
        int fullHeight = scrollView.getChildAt(0).getHeight();
        int visibleHeight = scrollView.getHeight();
        int scrollRange = fullHeight - visibleHeight;
        if (scrollRange <= 0) return;

        ValueAnimator animator = ValueAnimator.ofInt(scrollRange, 0);
        animator.setDuration(20000);
        animator.addUpdateListener(animation ->
                scrollView.scrollTo(0, (int) animation.getAnimatedValue()));
        animator.start();
    }

    private void startBottomUpReveal(TextView contentView) {
        if (isRevealing && revealRunnable != null) {
            revealHandler.removeCallbacks(revealRunnable);
        }

        isRevealing = true;
        String story = storyContent;
        contentView.setText("");
        final int totalLength = story.length();
        final int delay = 30;

        revealRunnable = new Runnable() {
            int index = 0;
            @Override
            public void run() {
                if (index < totalLength) {
                    contentView.append(String.valueOf(story.charAt(index)));
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
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.getMenu().add(isMuted ? "Unmute 🔊" : "Mute 🔇");
        popupMenu.getMenu().add("Exit ❌");

        popupMenu.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if (title.contains("Mute")) {
                muteDevice();
                isMuted = true;
            } else if (title.contains("Unmute")) {
                unmuteDevice();
                isMuted = false;
            } else if (title.contains("Exit")) {
                stopReading();
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

    private void fadeOutMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying() && !isMuted) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (currentVolume > 0.1f) {
                        currentVolume -= 0.1f;
                        mediaPlayer.setVolume(currentVolume, currentVolume);
                        handler.postDelayed(this, 100);
                    } else {
                        mediaPlayer.pause();
                        currentVolume = 1f;
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
                        handler.postDelayed(this, 100);
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

        if (isRevealing && revealRunnable != null) revealHandler.removeCallbacks(revealRunnable);

        stopMusic();
        super.onDestroy();
    }
}
