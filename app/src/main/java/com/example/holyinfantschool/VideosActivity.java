package com.example.holyinfantschool;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VideosActivity extends AppCompatActivity {

    private boolean isMuted = false;
    private MediaPlayer mediaPlayer;
    private boolean isPausedBySystem = false;
    private RequestQueue requestQueue;

    private static final String PEXELS_API_KEY = "29zjLCRxRLBPHJ5l5E5VahUqCiDshSNWzDVTEyyYu3XUp9ZxIzx7eAla";

    private RecyclerView recyclerView;
    private RelativeLayout videoOverlay;
    private ImageView closeOverlayBtn, backButton, settingsButton;
    private PlayerView playerView;
    private ProgressBar videoLoadingSpinner;

    private ExoPlayer exoPlayer;
    private final List<VideoItem> videoList = new ArrayList<>();
    private VideoAdapter adapter;

    private int currentPage = 1;
    private String currentQuery = "kids fun";
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos);

        // 🔹 Initialize views
        videoLoadingSpinner = findViewById(R.id.videoLoading);
        backButton = findViewById(R.id.backbtn);
        settingsButton = findViewById(R.id.settingsButton);
        recyclerView = findViewById(R.id.videoRecyclerView);
        videoOverlay = findViewById(R.id.videoOverlay);
        closeOverlayBtn = findViewById(R.id.closeOverlayBtn);
        playerView = findViewById(R.id.playerView);

        // 🔹 Setup background music
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        requestQueue = Volley.newRequestQueue(this);

        // 🔹 Back button → go to main menu
        backButton.setOnClickListener(v -> {
            stopMusic();
            startActivity(new Intent(VideosActivity.this, Categorypage.class));
            finish();
        });

        // 🔹 Settings menu
        settingsButton.setOnClickListener(v -> showSettingsMenu(settingsButton));

        // 🔹 Recycler setup
        adapter = new VideoAdapter(this, videoList, this::playOverlayVideo);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 🔹 Infinite scroll
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);
                LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();
                if (!isLoading && lm != null && lm.findLastVisibleItemPosition() >= videoList.size() - 2) {
                    loadKidVideos(currentQuery, ++currentPage);
                }
            }
        });

        // 🔹 Overlay close button
        closeOverlayBtn.setOnClickListener(v -> closeVideoOverlay());

        // 🔹 Close overlay on outside tap
        videoOverlay.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                closeVideoOverlay();
                return true;
            }
            return false;
        });

        loadKidVideos(currentQuery, currentPage);
    }

    // 🔹 Load videos from Pexels API
    private void loadKidVideos(String query, int page) {
        isLoading = true;
        String url = "https://api.pexels.com/videos/search?query=" + Uri.encode(query) + "&per_page=8&page=" + page;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray videos = response.getJSONArray("videos");
                        for (int i = 0; i < videos.length(); i++) {
                            JSONObject video = videos.getJSONObject(i);
                            String thumbnail = video.getString("image");
                            String videoUrl = video.getJSONArray("video_files").getJSONObject(0).getString("link");
                            videoList.add(new VideoItem(thumbnail, videoUrl));
                        }
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing videos", Toast.LENGTH_SHORT).show();
                    }
                    isLoading = false;
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Failed to load videos", Toast.LENGTH_SHORT).show();
                    isLoading = false;
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", PEXELS_API_KEY);
                return headers;
            }
        };

        requestQueue.add(request);
    }

    // 🔹 Play video overlay with loading spinner
    @androidx.annotation.OptIn(markerClass = androidx.media3.common.util.UnstableApi.class)
    private void playOverlayVideo(String videoUrl) {
        pauseMusic();

        videoOverlay.setAlpha(0f);
        videoOverlay.setVisibility(View.VISIBLE);
        videoOverlay.animate().alpha(1f).setDuration(300).start();

        videoLoadingSpinner.setVisibility(View.VISIBLE);

        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }

        exoPlayer = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(exoPlayer);

        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));
        exoPlayer.setMediaItem(mediaItem);
        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT);

        // 🔹 Handle player state for loading spinner
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_BUFFERING) {
                    videoLoadingSpinner.setVisibility(View.VISIBLE);
                } else if (state == Player.STATE_READY || state == Player.STATE_ENDED) {
                    videoLoadingSpinner.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                videoLoadingSpinner.setVisibility(View.GONE);
                Toast.makeText(VideosActivity.this, "Error playing video", Toast.LENGTH_SHORT).show();
            }
        });

        exoPlayer.prepare();
        exoPlayer.play();
    }

    // 🔹 Close overlay and stop video
    private void closeVideoOverlay() {
        if (videoOverlay.getVisibility() == View.VISIBLE) {
            videoOverlay.animate().alpha(0f).setDuration(300).withEndAction(() -> {
                videoOverlay.setVisibility(View.GONE);
                if (exoPlayer != null) {
                    exoPlayer.stop();
                    exoPlayer.release();
                    exoPlayer = null;
                }
                resumeMusic();
            }).start();
        }
    }

    // 🔹 Settings popup
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
            try {
                mediaPlayer.stop();
            } catch (IllegalStateException ignored) {}
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseMusic();
        if (exoPlayer != null) exoPlayer.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeMusic();
        if (exoPlayer != null && !exoPlayer.isPlaying()) exoPlayer.play();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMusic();
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (videoOverlay != null && videoOverlay.getVisibility() == View.VISIBLE) {
            closeVideoOverlay();
        } else {
            super.onBackPressed();
        }
    }
}
