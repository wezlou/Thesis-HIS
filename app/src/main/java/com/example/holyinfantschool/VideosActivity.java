package com.example.holyinfantschool;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class VideosActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ImageView backButton, settingsButton;

    private MediaPlayer mediaPlayer;
    private boolean isMuted = false;
    private boolean isPausedBySystem = false;

    private RequestQueue requestQueue;

    // Keep your API key secure in production (don't hardcode).
    private static final String YT_API_KEY =
            "AIzaSyDV4iG86oIUQNhKpNAOw02M11zJA8WwIiI";

    private List<VideoItem> videoList = new ArrayList<>();
    private VideoAdapter adapter;

    private boolean isLoading = false;
    private String nextPageToken = "";
    private String query = "colors for kids";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos);

        recyclerView = findViewById(R.id.videoRecyclerView);
        backButton = findViewById(R.id.backbtn);
        settingsButton = findViewById(R.id.settingsButton);

        requestQueue = Volley.newRequestQueue(this);

        setupMusic();
        setupButtons();
        setupFilters();
        setupRecycler();

        loadYouTubeVideos();
    }

    // -------------------------------------------------------
    // FETCH YOUTUBE VIDEOS
    // -------------------------------------------------------
    private void loadYouTubeVideos() {
        if (isLoading) return;
        isLoading = true;

        String url = "https://www.googleapis.com/youtube/v3/search"
                + "?key=" + YT_API_KEY
                + "&part=snippet"
                + "&type=video"
                + "&videoEmbeddable=true"
                + "&maxResults=10"
                + "&q=" + Uri.encode(query)
                + "&pageToken=" + nextPageToken;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        nextPageToken = response.optString("nextPageToken", "");

                        JSONArray items = response.getJSONArray("items");

                        for (int i = 0; i < items.length(); i++) {
                            JSONObject item = items.getJSONObject(i);
                            JSONObject idObj = item.getJSONObject("id");
                            if (!idObj.has("videoId")) continue;

                            String videoId = idObj.getString("videoId");
                            JSONObject snippet = item.getJSONObject("snippet");
                            String title = snippet.optString("title", "Untitled");
                            // thumbnail fallback safe access
                            String thumb = snippet
                                    .getJSONObject("thumbnails")
                                    .optJSONObject("high") != null
                                    ? snippet.getJSONObject("thumbnails").getJSONObject("high").optString("url", "")
                                    : snippet.getJSONObject("thumbnails").optJSONObject("default") != null
                                    ? snippet.getJSONObject("thumbnails").getJSONObject("default").optString("url", "")
                                    : "";

                            videoList.add(new VideoItem(thumb, videoId, title));
                        }

                        adapter.notifyDataSetChanged();
                        isLoading = false;

                    } catch (Exception e) {
                        isLoading = false;
                        Toast.makeText(this, "Parsing error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    isLoading = false;
                    Toast.makeText(this, "Failed to load videos", Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(request);
    }

    private void setupRecycler() {
        adapter = new VideoAdapter(this, videoList, videoId -> {
            // open dedicated player activity (clean)
            Intent i = new Intent(VideosActivity.this, VideoPlayerActivity.class);
            i.putExtra("videoId", videoId);
            startActivity(i);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);

                LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();

                if (!isLoading && lm != null &&
                        lm.findLastVisibleItemPosition() >= videoList.size() - 2) {
                    loadYouTubeVideos();
                }
            }
        });
    }

    private void setupFilters() {
        findViewById(R.id.filter_colors).setOnClickListener(v -> applyFilter("colors"));
        findViewById(R.id.filter_alphabets).setOnClickListener(v -> applyFilter("alphabets"));
        findViewById(R.id.filter_numbers).setOnClickListener(v -> applyFilter("numbers"));
        findViewById(R.id.filter_animals).setOnClickListener(v -> applyFilter("animals"));
        findViewById(R.id.filter_songs).setOnClickListener(v -> applyFilter("kids songs"));
        findViewById(R.id.filter_cartoons).setOnClickListener(v -> applyFilter("cartoons"));
    }

    private void applyFilter(String keyword) {
        query = keyword + " for kids";
        nextPageToken = "";
        videoList.clear();
        adapter.notifyDataSetChanged();
        loadYouTubeVideos();
    }

    private void setupButtons() {

        backButton.setOnClickListener(v -> {
            stopMusic();
            startActivity(new Intent(VideosActivity.this, Categorypage.class));
            finish();
        });

        settingsButton.setOnClickListener(v -> showSettingsMenu(settingsButton));
    }

    private void showSettingsMenu(ImageView anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.getMenu().add(isMuted ? "Unmute 🔊" : "Mute 🔇");
        popupMenu.getMenu().add("Exit ❌");

        popupMenu.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();

            if (title.contains("Mute")) muteMusic();
            else if (title.contains("Unmute")) unmuteMusic();
            else if (title.contains("Exit")) {
                stopMusic();
                finishAffinity();
            }
            return true;
        });

        popupMenu.show();
    }

    // -------------------------------------------------------
    // MUSIC HANDLING
    // -------------------------------------------------------
    private void setupMusic() {
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    private void muteMusic() {
        isMuted = true;
        if (mediaPlayer != null) mediaPlayer.setVolume(0f, 0f);
    }

    private void unmuteMusic() {
        isMuted = false;
        if (mediaPlayer != null) mediaPlayer.setVolume(1f, 1f);
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
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPausedBySystem = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null && isPausedBySystem && !isMuted) {
            mediaPlayer.start();
            isPausedBySystem = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMusic();
    }
}
