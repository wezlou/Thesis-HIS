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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VideosActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ImageView backButton, settingsButton;

    private MediaPlayer mediaPlayer;
    private boolean isMuted = false;
    private boolean isPausedBySystem = false;

    private RequestQueue requestQueue;

    private static final String YT_API_KEY = "AIzaSyDV4iG86oIUQNhKpNAOw02M11zJA8WwIiI";

    private List<VideoItem> videoList = new ArrayList<>();
    private VideoAdapter adapter;

    private boolean isLoading = false;
    private String nextPageToken = "";
    private String query = "colors for kids";

    private static final int MODE_YOUTUBE = 0;
    private static final int MODE_TEACHERS = 1;
    private int currentMode = MODE_YOUTUBE;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

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

    private void saveLogoutAndExit() {

        String uid = auth.getCurrentUser() != null
                ? auth.getCurrentUser().getUid()
                : getSharedPreferences("HIS_APP", MODE_PRIVATE).getString("last_uid", null);

        String email = auth.getCurrentUser() != null
                ? auth.getCurrentUser().getEmail()
                : getSharedPreferences("HIS_APP", MODE_PRIVATE).getString("last_email", null);

        if (uid == null) {
            FirebaseAuth.getInstance().signOut();
            stopMusic();
            finishAffinity();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("action", "logout");
        data.put("uid", uid);
        data.put("email", email);
        data.put("role", getSharedPreferences("HIS_APP", MODE_PRIVATE).getString("user_role", "student"));
        data.put("loginType", "firebase");
        data.put("device", "Android");
        data.put("timestamp", FieldValue.serverTimestamp());

        db.collection("auth_history")
                .add(data)
                .addOnCompleteListener(task -> {
                    FirebaseAuth.getInstance().signOut();
                    getSharedPreferences("HIS_APP", MODE_PRIVATE)
                            .edit()
                            .remove("session_id")
                            .remove("user_role")
                            .apply();
                    stopMusic();
                    finishAffinity();
                });
    }

    private void loadYouTubeVideos() {
        if (isLoading || currentMode != MODE_YOUTUBE) return;
        isLoading = true;

        String url = "https://www.googleapis.com/youtube/v3/search"
                + "?key=" + YT_API_KEY
                + "&part=snippet"
                + "&type=video"
                + "&videoEmbeddable=true"
                + "&maxResults=10"
                + "&q=" + Uri.encode(query)
                + "&pageToken=" + nextPageToken;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
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

                            String thumb = snippet.getJSONObject("thumbnails")
                                    .getJSONObject("high")
                                    .optString("url", "");

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

    private void loadTeacherVideos() {
        FirebaseFirestore.getInstance()
                .collection("videos")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(qs -> {
                    for (DocumentSnapshot doc : qs) {
                        String title = doc.getString("title");
                        String url = doc.getString("videoUrl");
                        if (url != null && !url.isEmpty()) {
                            videoList.add(new VideoItem(title, url));
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void setupRecycler() {
        adapter = new VideoAdapter(this, videoList, videoId -> {
            Intent i = new Intent(this, VideoPlayerActivity.class);
            i.putExtra("videoId", videoId);
            startActivity(i);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();
                if (!isLoading && currentMode == MODE_YOUTUBE &&
                        lm != null &&
                        lm.findLastVisibleItemPosition() >= videoList.size() - 2) {
                    loadYouTubeVideos();
                }
            }
        });
    }

    private void setupFilters() {
        findViewById(R.id.filter_teachers).setOnClickListener(v -> {
            currentMode = MODE_TEACHERS;
            videoList.clear();
            adapter.notifyDataSetChanged();
            loadTeacherVideos();
        });

        findViewById(R.id.filter_colors).setOnClickListener(v -> applyYouTubeFilter("colors"));
        findViewById(R.id.filter_alphabets).setOnClickListener(v -> applyYouTubeFilter("alphabets"));
        findViewById(R.id.filter_numbers).setOnClickListener(v -> applyYouTubeFilter("numbers"));
        findViewById(R.id.filter_animals).setOnClickListener(v -> applyYouTubeFilter("animals"));
        findViewById(R.id.filter_songs).setOnClickListener(v -> applyYouTubeFilter("kids songs"));
        findViewById(R.id.filter_cartoons).setOnClickListener(v -> applyYouTubeFilter("cartoons"));
    }

    private void applyYouTubeFilter(String keyword) {
        currentMode = MODE_YOUTUBE;
        query = keyword + " for kids";
        nextPageToken = "";
        videoList.clear();
        adapter.notifyDataSetChanged();
        loadYouTubeVideos();
    }

    private void setupButtons() {
        backButton.setOnClickListener(v -> {
            stopMusic();
            startActivity(new Intent(this, Categorypage.class));
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
            else saveLogoutAndExit();
            return true;
        });
        popupMenu.show();
    }

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
