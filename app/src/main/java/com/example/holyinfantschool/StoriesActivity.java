package com.example.holyinfantschool;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class StoriesActivity extends AppCompatActivity {

    private LinearLayout storyContainer;
    private MediaPlayer mediaPlayer;
    private boolean isMuted = false;
    private Animation bounceIn;

    private FirebaseFirestore db;
    private List<DocumentSnapshot> allStories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stories);

        db = FirebaseFirestore.getInstance();

        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        storyContainer = findViewById(R.id.story_container);
        bounceIn = AnimationUtils.loadAnimation(this, R.anim.card_bounce_in);

        ImageView backButton = findViewById(R.id.backbtn);
        ImageView settingsButton = findViewById(R.id.settingsButton);

        backButton.setOnClickListener(v -> {
            stopMusic();
            finish();
        });

        settingsButton.setOnClickListener(v -> showSettingsMenu(settingsButton));

        setupFilters();
        loadStoriesFromFirestore();
    }

    // ================= LOAD STORIES =================

    private void loadStoriesFromFirestore() {
        db.collection("stories")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(q -> {
                    allStories.clear();
                    allStories.addAll(q.getDocuments());
                    displayFilteredStories(null);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load stories", Toast.LENGTH_SHORT).show()
                );
    }

    // ================= DISPLAY =================

    private void displayFilteredStories(String category) {
        storyContainer.removeAllViews();

        for (DocumentSnapshot doc : allStories) {
            String storyCategory = doc.getString("category");

            if (category == null || category.equals(storyCategory)) {
                addStoryCard(
                        doc.getString("title"),
                        doc.getString("content"),
                        doc.getString("imageUrl"),
                        storyCategory
                );
            }
        }
    }

    private void addStoryCard(String title, String content, String imageUrl, String category) {

        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 28);
        card.setLayoutParams(cardParams);
        card.setRadius(40);
        card.setCardElevation(12);
        card.setUseCompatPadding(true);
        card.setBackgroundResource(R.drawable.candy_card_background);

        LinearLayout innerLayout = new LinearLayout(this);
        innerLayout.setOrientation(LinearLayout.VERTICAL);
        innerLayout.setPadding(0, 0, 0, 16);

        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 400));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(this).load(imageUrl).into(imageView);

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextSize(22);
        titleView.setTextColor(ContextCompat.getColor(this, R.color.candy_pink_dark));
        titleView.setPadding(20, 16, 20, 4);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView categoryView = new TextView(this);
        categoryView.setText(category);
        categoryView.setPadding(20, 0, 20, 4);
        categoryView.setTextSize(12);
        categoryView.setTextColor(ContextCompat.getColor(this, R.color.candy_text_soft));

        TextView previewView = new TextView(this);
        previewView.setText(content.length() > 90 ? content.substring(0, 90) + "..." : content);
        previewView.setTextSize(16);
        previewView.setTextColor(ContextCompat.getColor(this, R.color.candy_text_soft));
        previewView.setPadding(20, 0, 20, 16);

        innerLayout.addView(imageView);
        innerLayout.addView(titleView);
        innerLayout.addView(categoryView);
        innerLayout.addView(previewView);
        card.addView(innerLayout);

        card.startAnimation(bounceIn);

        card.setOnClickListener(v -> {
            Intent intent = new Intent(StoriesActivity.this, StoryDetailActivity.class);
            intent.putExtra("title", title);
            intent.putExtra("content", content);
            intent.putExtra("imageUrl", imageUrl);
            startActivity(intent);
        });

        storyContainer.addView(card);
    }

    // ================= FILTERS =================

    private void setupFilters() {
        TextView filterAll = findViewById(R.id.filterAll);
        TextView filterAnimals = findViewById(R.id.filterAnimals);
        TextView filterFairyTales = findViewById(R.id.filterFairyTales);
        TextView filterLessons = findViewById(R.id.filterLessons);
        TextView filterMorals = findViewById(R.id.filterMorals);

        View.OnClickListener listener = v -> {
            resetFilterColors();
            ((TextView) v).setBackgroundResource(R.drawable.candy_filter_tab_selected);

            String selected = ((TextView) v).getText().toString();
            displayFilteredStories(selected.equals("All") ? null : selected);
        };

        filterAll.setOnClickListener(listener);
        filterAnimals.setOnClickListener(listener);
        filterFairyTales.setOnClickListener(listener);
        filterLessons.setOnClickListener(listener);
        filterMorals.setOnClickListener(listener);

        filterAll.setBackgroundResource(R.drawable.candy_filter_tab_selected);
    }

    private void resetFilterColors() {
        int[] ids = {
                R.id.filterAll,
                R.id.filterAnimals,
                R.id.filterFairyTales,
                R.id.filterLessons,
                R.id.filterMorals
        };
        for (int id : ids) {
            ((TextView) findViewById(id))
                    .setBackgroundResource(R.drawable.candy_filter_tab_unselected);
        }
    }

    // ================= SETTINGS =================

    private void showSettingsMenu(ImageView anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.getMenu().add(isMuted ? "Unmute 🔊" : "Mute 🔇");
        popupMenu.getMenu().add("Exit ❌");

        popupMenu.setOnMenuItemClickListener(item -> {
            String t = item.getTitle().toString();
            if (t.contains("Mute")) {
                muteDevice();
                isMuted = true;
            } else if (t.contains("Unmute")) {
                unmuteDevice();
                isMuted = false;
            } else if (t.contains("Exit")) {
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

    private void stopMusic() {
        if (mediaPlayer != null) {
            try { mediaPlayer.stop(); } catch (Exception ignored) {}
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null && !isMuted && !mediaPlayer.isPlaying()) mediaPlayer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMusic();
    }
}
