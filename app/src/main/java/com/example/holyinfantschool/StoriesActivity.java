package com.example.holyinfantschool;

<<<<<<< HEAD
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import android.widget.TextView;

public class StoriesActivity extends AppCompatActivity {

    private LinearLayout storyContainer;

    // Example stories (expand up to 20)
    private String[] storyTitles = {
            "The Little Apple",
            "The Blue Bird",
            "The Green Mango",
            "The Brave Turtle",
            "The Rainbow’s Lesson"
    };

    private String[] storyContents = {
            "Once upon a time, there was a little apple who wanted to shine brighter than the sun...",
            "A kind blue bird helped the flowers bloom in the spring season...",
            "The green mango dreamed of becoming the sweetest fruit in the market...",
            "A brave little turtle crossed the river to visit his family...",
            "The rainbow taught the children that differences make life colorful..."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stories);

        storyContainer = findViewById(R.id.story_container);

        for (int i = 0; i < storyTitles.length; i++) {
            addStoryCard(storyTitles[i], storyContents[i], i);
        }
    }

    private void addStoryCard(String title, String content, int index) {
        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 24);
        card.setLayoutParams(cardParams);
        card.setRadius(20);
        card.setCardElevation(8);
        card.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
        card.setContentPadding(32, 32, 32, 32);
        card.setUseCompatPadding(true);

        // Title
        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextSize(20);
        titleView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark));

        // Preview (first 50 chars)
        TextView previewView = new TextView(this);
        previewView.setText(content.length() > 50 ? content.substring(0, 50) + "..." : content);
        previewView.setTextSize(16);
        previewView.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));

        // Add to card
        LinearLayout innerLayout = new LinearLayout(this);
        innerLayout.setOrientation(LinearLayout.VERTICAL);
        innerLayout.addView(titleView);
        innerLayout.addView(previewView);

        card.addView(innerLayout);

        // Click → open StoryDetailActivity
        card.setOnClickListener(v -> {
            Intent intent = new Intent(StoriesActivity.this, StoryDetailActivity.class);
            intent.putExtra("title", title);
            intent.putExtra("content", content);
            startActivity(intent);
        });

        storyContainer.addView(card);
    }
}
=======
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class StoriesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stories);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
>>>>>>> 6f63b6750e8185cb7c19947b17e291caa98c3e7b
