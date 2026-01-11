package com.example.holyinfantschool;

import android.content.Intent;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ManageVideosActivity extends AppCompatActivity {

    private ScrollView scrollView;

    private EditText inputTitle;
    private Button uploadVideoBtn, saveVideoBtn, addVideoBtn, cancelBtn;
    private LinearLayout videoListContainer, videoForm;
    private ProgressBar uploadProgress;
    private TextView uploadNote;
    private ImageView backBtn, editThumbnail;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private Uri selectedVideoUri;
    private String uploadedVideoUrl = null;
    private String editingVideoId = null;

    private static final String CDN_BASE = "https://azgb2gxzjh.ucarecd.net/";

    private static final int[] KIDS_COLORS = {
            0xFF42A5F5,
            0xFFEF5350,
            0xFF66BB6A,
            0xFFFFCA28,
            0xFFAB47BC,
            0xFFFF7043
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);

        scrollView = findViewById(R.id.scrollView);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        inputTitle = findViewById(R.id.inputTitle);
        uploadVideoBtn = findViewById(R.id.uploadVideoBtn);
        saveVideoBtn = findViewById(R.id.saveVideoBtn);
        addVideoBtn = findViewById(R.id.addVideoBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
        videoListContainer = findViewById(R.id.videoListContainer);
        videoForm = findViewById(R.id.videoForm);
        uploadProgress = findViewById(R.id.uploadProgress);
        uploadNote = findViewById(R.id.uploadNote);
        backBtn = findViewById(R.id.backBtn);
        editThumbnail = findViewById(R.id.editThumbnail);

        addVideoBtn.setOnClickListener(v -> showForm());
        cancelBtn.setOnClickListener(v -> resetForm());
        uploadVideoBtn.setOnClickListener(v -> pickVideo());
        saveVideoBtn.setOnClickListener(v -> saveVideo());
        backBtn.setOnClickListener(v -> finish());

        loadVideos();
    }

    private void showForm() {
        videoForm.setVisibility(View.VISIBLE);
        addVideoBtn.setVisibility(View.GONE);
        scrollView.post(() ->
                scrollView.smoothScrollTo(0, videoForm.getTop())
        );
    }

    private void pickVideo() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("video/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, 201);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 201 && resultCode == RESULT_OK && data != null) {
            selectedVideoUri = data.getData();

            uploadProgress.setVisibility(View.VISIBLE);
            uploadNote.setVisibility(View.VISIBLE);

            new Thread(() -> {
                try {
                    uploadedVideoUrl = UploadCareUploader.upload(this, selectedVideoUri);

                    runOnUiThread(() -> {
                        uploadProgress.setVisibility(View.GONE);
                        uploadNote.setVisibility(View.GONE);

                        editThumbnail.setVisibility(View.VISIBLE);
                        editThumbnail.setImageDrawable(
                                createNumberPlaceholder(0)
                        );

                        showForm();
                        Toast.makeText(this,
                                "Video uploaded. Enter title.",
                                Toast.LENGTH_SHORT).show();
                    });

                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(this,
                                    "Video upload failed",
                                    Toast.LENGTH_SHORT).show());
                }
            }).start();
        }
    }

    private void saveVideo() {
        String title = inputTitle.getText().toString().trim();

        if (title.isEmpty() || uploadedVideoUrl == null) {
            Toast.makeText(this,
                    "Upload video and enter title",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> video = new HashMap<>();
        video.put("title", title);
        video.put("videoUrl", uploadedVideoUrl);
        video.put("uploaderId", auth.getCurrentUser().getUid());
        video.put("createdAt", new Date());

        if (editingVideoId == null) {
            db.collection("videos").add(video);
        } else {
            db.collection("videos").document(editingVideoId).update(video);
        }

        resetForm();
        loadVideos();
    }

    private void resetForm() {
        inputTitle.setText("");
        uploadedVideoUrl = null;
        editingVideoId = null;
        editThumbnail.setVisibility(View.GONE);
        videoForm.setVisibility(View.GONE);
        addVideoBtn.setVisibility(View.VISIBLE);
    }

    private void loadVideos() {
        videoListContainer.removeAllViews();

        db.collection("videos")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(q -> {
                    int index = 1;
                    for (DocumentSnapshot doc : q) {
                        addVideoCard(doc, index++);
                    }
                });
    }

    private void addVideoCard(DocumentSnapshot doc, int index) {

        String videoUrl = doc.getString("videoUrl");

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(0, 0, 0, 24);
        card.setBackgroundResource(R.drawable.card_forest);
        card.setElevation(8f);

        ImageView thumbnail = new ImageView(this);
        thumbnail.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 450));
        thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);

        thumbnail.setImageDrawable(createNumberPlaceholder(index));

        TextView title = new TextView(this);
        title.setText(doc.getString("title"));
        title.setTextSize(16);
        title.setPadding(16, 16, 16, 8);
        title.setTypeface(null, Typeface.BOLD);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.END);

        Button playBtn = new Button(this);
        playBtn.setText("▶ Play");

        Button editBtn = new Button(this);
        editBtn.setText("✏️ Edit");

        Button deleteBtn = new Button(this);
        deleteBtn.setText("🗑 Delete");

        playBtn.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(Uri.parse(videoUrl), "video/*");
            startActivity(i);
        });

        editBtn.setOnClickListener(v -> {
            editingVideoId = doc.getId();
            uploadedVideoUrl = videoUrl;
            inputTitle.setText(doc.getString("title"));

            editThumbnail.setVisibility(View.VISIBLE);
            editThumbnail.setImageDrawable(
                    createNumberPlaceholder(index)
            );

            showForm();
        });

        deleteBtn.setOnClickListener(v -> deleteVideo(doc.getId()));

        actions.addView(playBtn);
        actions.addView(editBtn);
        actions.addView(deleteBtn);

        card.addView(thumbnail);
        card.addView(title);
        card.addView(actions);

        videoListContainer.addView(card);
    }

    private void deleteVideo(String id) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Video")
                .setMessage("Are you sure?")
                .setPositiveButton("Delete", (d, w) -> {
                    db.collection("videos").document(id).delete();
                    loadVideos();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private Drawable createNumberPlaceholder(int number) {

        int size = 600;
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);

        Paint bg = new Paint();
        bg.setColor(KIDS_COLORS[number % KIDS_COLORS.length]);
        canvas.drawRect(0, 0, size, size, bg);

        Paint text = new Paint();
        text.setColor(Color.WHITE);
        text.setTextSize(220f);
        text.setTypeface(Typeface.DEFAULT_BOLD);
        text.setTextAlign(Paint.Align.CENTER);

        Paint.FontMetrics fm = text.getFontMetrics();
        float y = size / 2f - (fm.ascent + fm.descent) / 2f;

        canvas.drawText(String.valueOf(number), size / 2f, y, text);

        return new BitmapDrawable(getResources(), bmp);
    }
}
