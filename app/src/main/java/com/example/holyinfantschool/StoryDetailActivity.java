package com.example.holyinfantschool;

import android.animation.ObjectAnimator;
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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class StoryDetailActivity extends AppCompatActivity {

    private TextToSpeech tts;
    private String storyContent;
    private MediaPlayer mediaPlayer;
    private boolean isMuted = false;
    private float currentVolume = 1f;
    private final Handler handler = new Handler();

    private Button playButton;
    private Animation bounceAnim;
    private ScrollView scrollView;
    private TextView contentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_detail);

        TextView titleView = findViewById(R.id.storyTitle);
        contentView = findViewById(R.id.storyContent);
        playButton = findViewById(R.id.playButton);
        ImageView backButton = findViewById(R.id.backbtn);
        ImageView settingsButton = findViewById(R.id.settingsButton);
        scrollView = findViewById(R.id.storyScroll);

        // 🍬 Candy-style bounce animation for buttons
        bounceAnim = AnimationUtils.loadAnimation(this, R.anim.button_bounce);

        // ✅ Get story data
        String storyTitle = getIntent().getStringExtra("title");
        storyContent = getIntent().getStringExtra("content");

        titleView.setText(storyTitle);
        contentView.setText(storyContent);

        // ✅ Background music
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        // ✅ Initialize TTS
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.ENGLISH);

                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        runOnUiThread(() -> {
                            fadeOutMusic();
                            playButton.startAnimation(bounceAnim);
                            startBottomUpTextAnimation();
                            Toast.makeText(StoryDetailActivity.this, "🎧 Let’s listen!", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        runOnUiThread(() -> {
                            fadeInMusic();
                            playButton.clearAnimation();
                        });
                    }

                    @Override
                    public void onError(String utteranceId) {
                        runOnUiThread(() -> {
                            fadeInMusic();
                            playButton.clearAnimation();
                        });
                    }
                });
            }
        });

        // ✅ Play / Listen button
        playButton.setOnClickListener(v -> {
            if (tts != null) {
                fadeOutMusic();
                contentView.setText(""); // reset text
                startBottomUpReveal(contentView); // start "bottom-up" reveal
                tts.speak(storyContent, TextToSpeech.QUEUE_FLUSH, null, "STORY_TTS");
            }
        });

        // ✅ Back button
        backButton.setOnClickListener(v -> {
            stopMusic();
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // ✅ Settings
        settingsButton.setOnClickListener(v -> showSettingsMenu(settingsButton));
    }

    // 🍭 Animate text appearing from bottom upwards
    private void startBottomUpTextAnimation() {
        contentView.setAlpha(0f);
        contentView.setTranslationY(300f); // start lower (off-screen-ish)

        // fade + slide up animation
        contentView.animate()
                .translationY(0f)
                .alpha(1f)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(1500)
                .start();

        // after fade-in, start smooth scroll upward like reading
        handler.postDelayed(() -> smoothScrollStory(scrollView), 1500);
    }

    // 🎠 Smooth scroll animation (bottom to top)
    private void smoothScrollStory(ScrollView scrollView) {
        int fullHeight = scrollView.getChildAt(0).getHeight();
        int visibleHeight = scrollView.getHeight();
        int scrollRange = fullHeight - visibleHeight;

        if (scrollRange <= 0) return;

        ValueAnimator animator = ValueAnimator.ofInt(scrollRange, 0);
        animator.setDuration(20000); // 20s scrolling time
        animator.addUpdateListener(animation ->
                scrollView.scrollTo(0, (int) animation.getAnimatedValue()));
        animator.start();
    }

    private void startBottomUpReveal(TextView contentView) {
        String story = storyContent; // your full story text
        contentView.setText("");      // start empty
        final int totalLength = story.length();
        final int delay = 50; // delay per character (ms)

        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            int index = 0;
            @Override
            public void run() {
                if (index < totalLength) {
                    // append next character at bottom
                    contentView.append(String.valueOf(story.charAt(index)));
                    // ensure cursor stays at bottom
                    scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
                    index++;
                    handler.postDelayed(this, delay);
                }
            }
        };
        runnable.run();
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
                Toast.makeText(this, "🔇 Music Off", Toast.LENGTH_SHORT).show();
            } else if (title.contains("Unmute")) {
                unmuteDevice();
                isMuted = false;
                Toast.makeText(this, "🔊 Music On", Toast.LENGTH_SHORT).show();
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
        stopMusic();
        super.onDestroy();
    }
}
