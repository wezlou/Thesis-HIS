package com.example.holyinfantschool;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide; // if you prefer Glide; otherwise use native

public class ImagePreviewActivity extends AppCompatActivity {
    public static final String EXTRA_URL = "extra_image_url";
    private ImageView imageView;
    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;
    private float scaleFactor = 1.0f;
    private static final float MAX_SCALE = 6.0f;
    private static final float MIN_SCALE = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        imageView = findViewById(R.id.imagePreviewView);
        String url = getIntent().getStringExtra(EXTRA_URL);
        if (url == null) { finish(); return; }

        // Use Glide if added to project; otherwise use Picasso or basic setImageURI.
        // Add Glide to gradle: implementation 'com.github.bumptech.glide:glide:4.12.0'
        try {
            Glide.with(this).load(url).into(imageView);
        } catch (Exception e) {
            imageView.setImageURI(Uri.parse(url));
        }

        scaleDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener(){
            @Override public boolean onScale(ScaleGestureDetector detector) {
                scaleFactor *= detector.getScaleFactor();
                scaleFactor = Math.max(MIN_SCALE, Math.min(scaleFactor, MAX_SCALE));
                imageView.setScaleX(scaleFactor);
                imageView.setScaleY(scaleFactor);
                return true;
            }
        });

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener(){
            @Override public boolean onDoubleTap(android.view.MotionEvent e) {
                if (Math.abs(scaleFactor - 1.0f) < 0.1f) {
                    scaleFactor = 3.0f;
                } else {
                    scaleFactor = 1.0f;
                }
                imageView.setScaleX(scaleFactor);
                imageView.setScaleY(scaleFactor);
                return true;
            }
        });

        imageView.setOnTouchListener((v, event) -> {
            scaleDetector.onTouchEvent(event);
            gestureDetector.onTouchEvent(event);
            return true;
        });
    }
}
