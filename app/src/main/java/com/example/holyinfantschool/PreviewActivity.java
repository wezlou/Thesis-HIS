package com.example.holyinfantschool;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

public class PreviewActivity extends AppCompatActivity {

    public static final String EXTRA_URL = "url";
    public static final String EXTRA_DISPLAY_NAME = "displayName";

    private FrameLayout imageContainer, videoContainer, docContainer;
    private ProgressBar previewProgress;

    // IMAGE
    private PhotoView previewImage;

    // PDF
    private WebView previewWebView;

    // VIDEO (ExoPlayer)
    private PlayerView exoPlayerView;
    private ExoPlayer player;
    private SeekBar seekBar;
    private ImageButton playPauseBtn;

    private boolean uiVisible = true;
    private boolean playerReady = false;

    private final Handler handler = new Handler();

    private View topBar, videoTapOverlay;
    private TextView previewTitle;
    private ImageButton closeBtn;

    private GestureDetector gestureDetector;

    private String fileUrl;
    private String fileName;

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Top UI
        topBar = findViewById(R.id.topBar);
        closeBtn = findViewById(R.id.closePreview);
        previewTitle = findViewById(R.id.previewTitle);
        previewProgress = findViewById(R.id.previewProgress);

        // Containers
        imageContainer = findViewById(R.id.imageContainer);
        videoContainer = findViewById(R.id.videoContainer);
        docContainer = findViewById(R.id.docContainer);

        // Image
        previewImage = findViewById(R.id.previewImage);

        // PDF
        previewWebView = findViewById(R.id.previewWebView);

        // Video
        exoPlayerView = findViewById(R.id.exoPlayerView);
        seekBar = findViewById(R.id.previewSeekBar);
        playPauseBtn = findViewById(R.id.playPauseBtn);
        videoTapOverlay = findViewById(R.id.videoTapOverlay);

        // Gesture detector (tap to hide UI)
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onSingleTapConfirmed(MotionEvent e) {
                toggleUI();
                return true;
            }
        });

        findViewById(R.id.previewContainer).setOnTouchListener((v, ev) -> gestureDetector.onTouchEvent(ev));
        videoTapOverlay.setOnTouchListener((v, ev) -> gestureDetector.onTouchEvent(ev));

        closeBtn.setOnClickListener(v -> finishWithAnimation());

        // SeekBar events
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean user) {}
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {
                if (player != null && playerReady) {
                    long pos = (long) ((s.getProgress() / 100f) * player.getDuration());
                    player.seekTo(pos);
                }
            }
        });

        // Play / Pause button
        playPauseBtn.setOnClickListener(v -> {
            if (player == null || !playerReady) return;
            if (player.isPlaying()) {
                player.pause();
                playPauseBtn.setImageResource(android.R.drawable.ic_media_play);
            } else {
                player.play();
                playPauseBtn.setImageResource(android.R.drawable.ic_media_pause);
            }
        });

        // Get data
        fileUrl = getIntent().getStringExtra(EXTRA_URL);
        fileName = getIntent().getStringExtra(EXTRA_DISPLAY_NAME);

        previewTitle.setText(fileName);

        String ext = getExt(fileName);
        if (isImage(ext)) loadImage(fileUrl);
        else if (isVideo(ext)) loadVideo(fileUrl);
        else loadPdf(fileUrl);
    }

    // --------- IMAGE ---------
    private void loadImage(String url) {
        showOnly(imageContainer);
        previewProgress.setVisibility(View.VISIBLE);

        Picasso.get()
                .load(url)
                .fit()
                .centerInside()
                .into(previewImage, new com.squareup.picasso.Callback() {
                    @Override public void onSuccess() {
                        previewProgress.setVisibility(View.GONE);
                    }
                    @Override public void onError(Exception e) {
                        previewProgress.setVisibility(View.GONE);
                        Toast.makeText(PreviewActivity.this, "Image load failed", Toast.LENGTH_SHORT).show();
                    }
                });

        previewImage.setOnClickListener(v -> toggleUI());
    }

    // --------- PDF ---------
    @SuppressLint("SetJavaScriptEnabled")
    private void loadPdf(String url) {
        showOnly(docContainer);
        previewProgress.setVisibility(View.VISIBLE);

        previewWebView.getSettings().setJavaScriptEnabled(true);
        previewWebView.getSettings().setBuiltInZoomControls(true);
        previewWebView.getSettings().setDisplayZoomControls(false);
        previewWebView.setWebChromeClient(new WebChromeClient());
        previewWebView.loadUrl("https://docs.google.com/gview?embedded=true&url=" + url);

        previewProgress.setVisibility(View.GONE);
    }

    // --------- VIDEO ---------
    private void loadVideo(String url) {
        showOnly(videoContainer);
        previewProgress.setVisibility(View.VISIBLE);

        player = new ExoPlayer.Builder(this).build();
        exoPlayerView.setPlayer(player);

        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(url));
        player.setMediaItem(mediaItem);

        player.setPlayWhenReady(true);
        player.prepare();

        player.addListener(new androidx.media3.common.Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == androidx.media3.common.Player.STATE_READY) {
                    previewProgress.setVisibility(View.GONE);
                    playerReady = true;
                    playPauseBtn.setImageResource(android.R.drawable.ic_media_pause);
                    startSeekUpdater();
                }
            }

            @Override
            public void onPlayerError(androidx.media3.common.PlaybackException error) {
                Toast.makeText(PreviewActivity.this, "Cannot play video", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startSeekUpdater() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (player != null && playerReady) {
                    long pos = player.getCurrentPosition();
                    long dur = player.getDuration();
                    if (dur > 0) {
                        int percent = (int) ((pos * 100f) / dur);
                        seekBar.setProgress(percent);
                    }
                }
                handler.postDelayed(this, 300);
            }
        }, 300);
    }

    // --------- UI HELPERS ---------

    private void showOnly(View v) {
        imageContainer.setVisibility(View.GONE);
        videoContainer.setVisibility(View.GONE);
        docContainer.setVisibility(View.GONE);
        v.setVisibility(View.VISIBLE);
    }

    private void toggleUI() {
        uiVisible = !uiVisible;
        if (uiVisible) {
            topBar.animate().alpha(1f).setDuration(180);
            topBar.setVisibility(View.VISIBLE);
        } else {
            topBar.animate().alpha(0f).setDuration(180)
                    .withEndAction(() -> topBar.setVisibility(View.GONE));
        }
    }

    private String getExt(String name) {
        int i = name.lastIndexOf('.');
        return i == -1 ? "" : name.substring(i + 1).toLowerCase();
    }

    private boolean isImage(String e) {
        return e.equals("jpg") || e.equals("jpeg") || e.equals("png")
                || e.equals("gif") || e.equals("webp") || e.equals("bmp") || e.equals("heic");
    }

    private boolean isVideo(String e) {
        return e.equals("mp4") || e.equals("mkv") || e.equals("webm")
                || e.equals("mov") || e.equals("3gp");
    }

    private void finishWithAnimation() {
        View root = findViewById(R.id.previewContainer);
        root.animate()
                .translationY(root.getHeight() * 0.25f)
                .alpha(0f)
                .setDuration(220)
                .withEndAction(() -> {
                    finish();
                    overridePendingTransition(android.R.anim.fade_out, android.R.anim.slide_out_right);
                })
                .start();
    }

    @Override
    protected void onDestroy() {
        if (player != null) {
            player.release();
        }
        super.onDestroy();
    }
}
