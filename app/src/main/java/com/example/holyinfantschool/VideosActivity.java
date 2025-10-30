package com.example.holyinfantschool;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class VideosActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private boolean isMuted = false;
    private boolean isPausedBySystem = false;
    private ArrayList<String> videoUrls = new ArrayList<>();
    private ArrayList<String> thumbnailUrls = new ArrayList<>();

    private final String[] SAFE_TOPICS = {"nature", "animals", "education", "sports", "technology"};
    private final String PEXELS_API_KEY = "29zjLCRxRLBPHJ5l5E5VahUqCiDshSNWzDVTEyyYu3XUp9ZxIzx7eAla"; // 🔑 Replace with your key

    private ExoPlayer player;
    private PlayerView playerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos);

        // 🎵 Setup background music
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        ImageView backButton = findViewById(R.id.backbtn);
        ImageView settingsButton = findViewById(R.id.settingsButton);

        // 🎬 Player view (hidden initially)
        playerView = findViewById(R.id.player_view);
        playerView.setVisibility(View.GONE);

        // 🔙 Back button
        backButton.setOnClickListener(v -> {
            stopMusic();
            if (player != null) player.release();
            Intent intent = new Intent(VideosActivity.this, Categorypage.class);
            startActivity(intent);
            finish();
        });

        // ⚙️ Settings popup
        settingsButton.setOnClickListener(v -> showSettingsMenu(settingsButton));

        // 🖼️ ImageViews for thumbnails
        ImageView[] thumbnails = {
                findViewById(R.id.video1),
                findViewById(R.id.video2),
                findViewById(R.id.video3),
                findViewById(R.id.video4),
                findViewById(R.id.video5)
        };

        // Fetch 5 safe videos from Pexels
        fetchPexelsVideos(thumbnails);
    }

    private void fetchPexelsVideos(ImageView[] thumbnails) {
        String topic = SAFE_TOPICS[new Random().nextInt(SAFE_TOPICS.length)];
        String url = "https://api.pexels.com/videos/search?query=" + topic + "&per_page=5";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", PEXELS_API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonData = response.body().string();
                    runOnUiThread(() -> parseAndDisplayVideos(jsonData, thumbnails));
                }
            }
        });
    }

    private void parseAndDisplayVideos(String json, ImageView[] thumbnails) {
        try {
            JSONObject root = new JSONObject(json);
            JSONArray videos = root.getJSONArray("videos");

            for (int i = 0; i < videos.length(); i++) {
                JSONObject video = videos.getJSONObject(i);
                JSONArray files = video.getJSONArray("video_files");
                JSONArray pics = video.getJSONArray("video_pictures");

                String videoUrl = files.getJSONObject(0).getString("link");
                String thumbUrl = pics.getJSONObject(0).getString("picture");

                videoUrls.add(videoUrl);
                thumbnailUrls.add(thumbUrl);

                if (i < thumbnails.length) {
                    int index = i;
                    Glide.with(this).load(thumbUrl).centerCrop().into(thumbnails[index]);
                    thumbnails[index].setOnClickListener(v -> playVideo(videoUrls.get(index)));
                }
            }

        } catch (JSONException e) {
            Log.e("PexelsParse", "Error parsing JSON", e);
        }
    }

    private void playVideo(String videoUrl) {
        playerView.setVisibility(View.VISIBLE);

        if (player != null) player.release();
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        MediaItem item = MediaItem.fromUri(videoUrl);
        player.setMediaItem(item);
        player.prepare();
        player.play();
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
                stopMusic();
                if (player != null) player.release();
                finishAffinity();
            }
            return true;
        });

        popupMenu.show();
    }

    // 🔇 Mute background music
    private void muteDevice() {
        if (mediaPlayer != null) mediaPlayer.setVolume(0f, 0f);
    }

    // 🔊 Unmute background music
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
        if (player != null) player.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeMusic();
        if (player != null && !isMuted) player.play();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMusic();
        if (player != null) player.release();
    }
}
