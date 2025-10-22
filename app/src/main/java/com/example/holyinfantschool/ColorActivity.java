package com.example.holyinfantschool;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class ColorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color);

        ImageView stbtn2 = findViewById(R.id.stbtn2);

        stbtn2.setOnClickListener(v -> {
            // 🔀 Shuffle questions before moving to the next activity
            List<Question> shuffledQuestions = QuestionBank.getShuffledQuestions();
            // 🟢 Then open your next activity
            Intent intent = new Intent(ColorActivity.this, firstActivity.class);
            startActivity(intent);
        });
    }
}
