package com.example.holyinfantschool;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class VideosActivity extends AppCompatActivity {

    String[] videoIds = {
            "9_WBQISVHnw",
            "hTqtGJwsJVE",
            "Si5auXCYWDI",
            "gFuEoxh5hd4",
            "ZcX0gl-NFFg"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos);

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(VideosActivity.this, Categorypage.class);
            startActivity(intent);
            finish();
        });

        ImageView video1 = findViewById(R.id.video1);
        ImageView video2 = findViewById(R.id.video2);
        ImageView video3 = findViewById(R.id.video3);
        ImageView video4 = findViewById(R.id.video4);
        ImageView video5 = findViewById(R.id.video5);

        loadThumbnail(video1, videoIds[0]);
        loadThumbnail(video2, videoIds[1]);
        loadThumbnail(video3, videoIds[2]);
        loadThumbnail(video4, videoIds[3]);
        loadThumbnail(video5, videoIds[4]);

        video1.setOnClickListener(v -> openYouTube(videoIds[0]));
        video2.setOnClickListener(v -> openYouTube(videoIds[1]));
        video3.setOnClickListener(v -> openYouTube(videoIds[2]));
        video4.setOnClickListener(v -> openYouTube(videoIds[3]));
        video5.setOnClickListener(v -> openYouTube(videoIds[4]));
    }

    private void loadThumbnail(ImageView imageView, String videoId) {
        String url = "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg";
        Glide.with(this)
                .load(url)
                .centerCrop()
                .into(imageView);
    }

    private void openYouTube(String videoId) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + videoId));
        startActivity(intent);
    }
}
