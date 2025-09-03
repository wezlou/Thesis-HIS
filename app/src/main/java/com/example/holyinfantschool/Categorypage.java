package com.example.holyinfantschool;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Categorypage extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private boolean isMusicPlaying = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categorypage);


        ImageView backTeacher = findViewById(R.id.backteacher);

        mediaPlayer = MediaPlayer.create(this, R.raw.background_music); // Background music
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        // STORY - Go to StoriesActivity
        ImageView story = findViewById(R.id.story);
        story.setOnClickListener(v -> {
            Intent intent = new Intent(Categorypage.this, splashstories.class);
            startActivity(intent);
        });

        // WATCH VIDEOS - Go to VideosActivity
        ImageView watchVideos = findViewById(R.id.wv);
        watchVideos.setOnClickListener(v -> {
            Intent intent = new Intent(Categorypage.this, VideosActivity.class);
            startActivity(intent);
        });

        // Pencil - Go to QuizActivity
        ImageView pencil = findViewById(R.id.pencil);
        pencil.setOnClickListener(v -> {
            Intent intent = new Intent(Categorypage.this, QuizActivity.class);
            startActivity(intent);
        });

        // TASK - Go to splashstudenttask
        ImageView task = findViewById(R.id.task);
        task.setOnClickListener(v -> {
            Intent intent = new Intent(Categorypage.this, splashstudenttask.class);
            startActivity(intent);
        });

        

        // SETTINGS (teachersetting) - Toggle volume on/off
        ImageView teachersetting = findViewById(R.id.teachersetting);
        ImageView volumeOn = findViewById(R.id.volumeOn);
        ImageView volumeOff = findViewById(R.id.volumeOff);

        // Initially hide the volume buttons
        volumeOn.setVisibility(ImageView.INVISIBLE);
        volumeOff.setVisibility(ImageView.INVISIBLE);

        teachersetting.setOnClickListener(v -> {
            volumeOn.setVisibility(volumeOn.getVisibility() == ImageView.VISIBLE ? ImageView.GONE : ImageView.VISIBLE);
            volumeOff.setVisibility(volumeOff.getVisibility() == ImageView.VISIBLE ? ImageView.GONE : ImageView.VISIBLE);
        });

        // Volume ON - Turn music on
        volumeOn.setOnClickListener(v -> {
            if (!isMusicPlaying) {
                mediaPlayer.start();
                isMusicPlaying = true;
            }
        });

        // Volume OFF - Turn music off
        volumeOff.setOnClickListener(v -> {
            if (isMusicPlaying) {
                mediaPlayer.pause();
                isMusicPlaying = false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release(); // Release the media player when the activity is destroyed
        }
    }
}