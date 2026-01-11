package com.example.holyinfantschool;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.*;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.io.ByteArrayOutputStream;
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
    private String uploadedThumbnailUrl = null;
    private String editingVideoId = null;

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

        getWindow().getDecorView().setBackgroundColor(
                getResources().getColor(R.color.candy_bg)
        );

        addVideoBtn.setBackgroundTintList(ColorStateList.valueOf(
                getResources().getColor(R.color.candy_pink)));
        addVideoBtn.setTextColor(Color.WHITE);

        saveVideoBtn.setBackgroundTintList(ColorStateList.valueOf(
                getResources().getColor(R.color.candy_dark)));
        saveVideoBtn.setTextColor(Color.WHITE);

        uploadVideoBtn.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        uploadVideoBtn.setTextColor(
                getResources().getColor(R.color.candy_pink_dark));

        cancelBtn.setBackgroundTintList(ColorStateList.valueOf(Color.LTGRAY));
        cancelBtn.setTextColor(Color.DKGRAY);

        inputTitle.setTextColor(Color.BLACK);
        inputTitle.setHintTextColor(Color.DKGRAY);
        uploadNote.setTextColor(Color.BLACK);

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
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
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
                    Bitmap thumb = createVideoThumbnail(selectedVideoUri);
                    Uri thumbUri = bitmapToUri(thumb);

                    uploadedThumbnailUrl = UploadCareUploader.upload(this, thumbUri);
                    uploadedVideoUrl = UploadCareUploader.upload(this, selectedVideoUri);

                    runOnUiThread(() -> {
                        uploadProgress.setVisibility(View.GONE);
                        uploadNote.setVisibility(View.GONE);

                        editThumbnail.setVisibility(View.VISIBLE);
                        Glide.with(this)
                                .load(uploadedThumbnailUrl)
                                .into(editThumbnail);

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

        if (title.isEmpty() || uploadedVideoUrl == null || uploadedThumbnailUrl == null) {
            Toast.makeText(this,
                    "Please complete all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> video = new HashMap<>();
        video.put("title", title);
        video.put("videoUrl", uploadedVideoUrl);
        video.put("thumbnailUrl", uploadedThumbnailUrl);
        video.put("uploaderId", auth.getCurrentUser().getUid());
        video.put("createdAt", new Date());

        if (editingVideoId == null) {
            db.collection("videos").add(video);
            Toast.makeText(this, "Video added", Toast.LENGTH_SHORT).show();
        } else {
            db.collection("videos").document(editingVideoId).update(video);
            Toast.makeText(this, "Video updated", Toast.LENGTH_SHORT).show();
        }

        resetForm();
        loadVideos();
    }

    private void resetForm() {
        inputTitle.setText("");
        uploadedVideoUrl = null;
        uploadedThumbnailUrl = null;
        editingVideoId = null;
        editThumbnail.setVisibility(View.GONE);
        videoForm.setVisibility(View.GONE);
        addVideoBtn.setVisibility(View.VISIBLE);
        saveVideoBtn.setText("Save Video");
    }

    private void loadVideos() {
        videoListContainer.removeAllViews();

        db.collection("videos")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(q -> {
                    for (DocumentSnapshot doc : q) {
                        addVideoCard(doc);
                    }
                });
    }

    private void addVideoCard(DocumentSnapshot doc) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(0, 0, 0, 24);
        card.setBackgroundResource(R.drawable.card_forest);
        card.setElevation(8f);

        ImageView thumbnail = new ImageView(this);
        thumbnail.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 450));
        thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Glide.with(this)
                .load(doc.getString("thumbnailUrl"))
                .into(thumbnail);

        TextView title = new TextView(this);
        title.setText(doc.getString("title"));
        title.setTextSize(16);
        title.setPadding(16, 16, 16, 8);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.BLACK);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.END);

        Button playBtn = new Button(this);
        playBtn.setText("▶ Play");
        playBtn.setTextColor(Color.BLACK);

        Button editBtn = new Button(this);
        editBtn.setText("✏️ Edit");
        editBtn.setTextColor(Color.BLACK);

        Button deleteBtn = new Button(this);
        deleteBtn.setText("🗑 Delete");
        deleteBtn.setTextColor(Color.BLACK);

        playBtn.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(Uri.parse(doc.getString("videoUrl")), "video/*");
            startActivity(i);
        });

        editBtn.setOnClickListener(v -> {
            editingVideoId = doc.getId();
            uploadedVideoUrl = doc.getString("videoUrl");
            uploadedThumbnailUrl = doc.getString("thumbnailUrl");
            inputTitle.setText(doc.getString("title"));

            editThumbnail.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(uploadedThumbnailUrl)
                    .into(editThumbnail);

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

    private Bitmap createVideoThumbnail(Uri videoUri) throws Exception {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(this, videoUri);
        Bitmap bmp = retriever.getFrameAtTime(1_000_000);
        retriever.release();
        return bmp;
    }

    private Uri bitmapToUri(Bitmap bmp) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 85, bytes);
        String path = MediaStore.Images.Media.insertImage(
                getContentResolver(), bmp, "thumb", null);
        return Uri.parse(path);
    }
}
