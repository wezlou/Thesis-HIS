package com.example.holyinfantschool;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class shapeincorrect extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shapeincorrect);

        ImageView questionView = findViewById(R.id.b2);
        ImageView answerView = findViewById(R.id.circle);

        // 🔹 Retrieve the passed images
        byte[] questionBytes = getIntent().getByteArrayExtra("question_shape");
        byte[] answerBytes = getIntent().getByteArrayExtra("answer_shape");

        if (questionBytes != null) {
            Bitmap questionBitmap = BitmapFactory.decodeByteArray(questionBytes, 0, questionBytes.length);
            questionView.setImageBitmap(questionBitmap);
        }
        if (answerBytes != null) {
            Bitmap answerBitmap = BitmapFactory.decodeByteArray(answerBytes, 0, answerBytes.length);
            answerView.setImageBitmap(answerBitmap);
        }

        // ⏱ Go back to game after 1.5s
        new Handler().postDelayed(this::finish, 1500);
    }
}
