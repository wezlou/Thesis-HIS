package com.example.holyinfantschool;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ManageStoriesActivity extends AppCompatActivity {

    private EditText inputTitle, inputContent;
    private Spinner categorySpinner;
    private Button uploadImageBtn, saveStoryBtn, addStoryBtn, cancelBtn;
    private ImageView previewImage, backBtn;
    private LinearLayout storyListContainer, storyForm;
    private ProgressBar uploadProgress;
    private TextView uploadNote;

    private FirebaseFirestore db;

    private String uploadedImageUrl = null;
    private String editingStoryId = null;

    private final String[] categories = {
            "Animals", "Fairy Tales", "Lessons", "Morals"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_stories);

        db = FirebaseFirestore.getInstance();

        inputTitle = findViewById(R.id.inputTitle);
        inputContent = findViewById(R.id.inputContent);
        categorySpinner = findViewById(R.id.categorySpinner);
        uploadImageBtn = findViewById(R.id.uploadImageBtn);
        saveStoryBtn = findViewById(R.id.saveStoryBtn);
        addStoryBtn = findViewById(R.id.addStoryBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
        previewImage = findViewById(R.id.previewImage);
        storyListContainer = findViewById(R.id.storyListContainer);
        storyForm = findViewById(R.id.storyForm);
        uploadProgress = findViewById(R.id.uploadProgress);
        uploadNote = findViewById(R.id.uploadNote);
        backBtn = findViewById(R.id.backBtn);

        // 🌈 Background
        getWindow().getDecorView().setBackgroundColor(
                getResources().getColor(R.color.candy_bg)
        );

        // 🎨 Buttons
        addStoryBtn.setBackgroundTintList(ColorStateList.valueOf(
                getResources().getColor(R.color.candy_pink)));
        addStoryBtn.setTextColor(Color.WHITE);

        saveStoryBtn.setBackgroundTintList(ColorStateList.valueOf(
                getResources().getColor(R.color.candy_dark)));
        saveStoryBtn.setTextColor(Color.WHITE);

        uploadImageBtn.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        uploadImageBtn.setTextColor(
                getResources().getColor(R.color.candy_pink_dark));

        cancelBtn.setBackgroundTintList(ColorStateList.valueOf(Color.LTGRAY));
        cancelBtn.setTextColor(Color.DKGRAY);

        // 🔒 FORCE BLACK TEXT (FORM)
        forceBlack(inputTitle);
        forceBlack(inputContent);
        uploadNote.setTextColor(Color.BLACK);

        // 🔽 Spinner with forced black text
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                categories
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                ((TextView) v).setTextColor(Color.BLACK);
                return v;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                ((TextView) v).setTextColor(Color.BLACK);
                return v;
            }
        };
        categorySpinner.setAdapter(adapter);

        backBtn.setOnClickListener(v -> finish());

        addStoryBtn.setOnClickListener(v -> {
            storyForm.setVisibility(View.VISIBLE);
            addStoryBtn.setVisibility(View.GONE);
        });

        cancelBtn.setOnClickListener(v -> resetForm());

        uploadImageBtn.setOnClickListener(v -> pickImage());

        saveStoryBtn.setOnClickListener(v -> saveStory());

        loadStories();
    }

    // 🔒 Helper to force readable text
    private void forceBlack(EditText et) {
        et.setTextColor(Color.BLACK);
        et.setHintTextColor(Color.DKGRAY);
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            uploadProgress.setVisibility(View.VISIBLE);
            uploadNote.setVisibility(View.VISIBLE);
            uploadImageBtn.setEnabled(false);

            new Thread(() -> {
                try {
                    uploadedImageUrl = UploadCareUploader.upload(this, imageUri);

                    runOnUiThread(() -> {
                        uploadProgress.setVisibility(View.GONE);
                        uploadNote.setVisibility(View.GONE);
                        uploadImageBtn.setEnabled(true);
                        previewImage.setVisibility(View.VISIBLE);
                        Glide.with(this).load(uploadedImageUrl).into(previewImage);
                    });

                } catch (Exception e) {
                    runOnUiThread(() -> {
                        uploadProgress.setVisibility(View.GONE);
                        uploadNote.setVisibility(View.GONE);
                        uploadImageBtn.setEnabled(true);
                        Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        }
    }

    private void saveStory() {
        String title = inputTitle.getText().toString().trim();
        String content = inputContent.getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString();

        if (title.isEmpty() || content.isEmpty() || uploadedImageUrl == null) {
            Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> story = new HashMap<>();
        story.put("title", title);
        story.put("content", content);
        story.put("category", category);
        story.put("imageUrl", uploadedImageUrl);
        story.put("createdAt", new Date());

        if (editingStoryId == null) {
            db.collection("stories").add(story);
            Toast.makeText(this, "Story added", Toast.LENGTH_SHORT).show();
        } else {
            db.collection("stories").document(editingStoryId).update(story);
            Toast.makeText(this, "Story updated", Toast.LENGTH_SHORT).show();
        }

        resetForm();
        loadStories();
    }

    private void resetForm() {
        inputTitle.setText("");
        inputContent.setText("");
        uploadedImageUrl = null;
        editingStoryId = null;
        previewImage.setVisibility(View.GONE);
        storyForm.setVisibility(View.GONE);
        addStoryBtn.setVisibility(View.VISIBLE);
        saveStoryBtn.setText("Save Story");
    }

    private void loadStories() {
        storyListContainer.removeAllViews();

        db.collection("stories")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(q -> {
                    for (DocumentSnapshot doc : q) {
                        addStoryItem(doc);
                    }
                });
    }

    private void addStoryItem(DocumentSnapshot doc) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setPadding(32, 32, 32, 32);
        item.setBackgroundColor(
                getResources().getColor(R.color.candy_card));
        item.setElevation(10f);

        TextView title = new TextView(this);
        title.setText(doc.getString("title"));
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setTextColor(Color.BLACK); // 🔒 FIXED

        ImageView image = new ImageView(this);
        image.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 300));
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(this).load(doc.getString("imageUrl")).into(image);

        Button editBtn = new Button(this);
        editBtn.setText("✏️ Edit");

        Button deleteBtn = new Button(this);
        deleteBtn.setText("🗑 Delete");

        editBtn.setOnClickListener(v -> editStory(doc));
        deleteBtn.setOnClickListener(v -> deleteStory(doc.getId()));

        item.addView(title);
        item.addView(image);
        item.addView(editBtn);
        item.addView(deleteBtn);

        storyListContainer.addView(item);
    }

    private void editStory(DocumentSnapshot doc) {
        editingStoryId = doc.getId();
        inputTitle.setText(doc.getString("title"));
        inputContent.setText(doc.getString("content"));
        uploadedImageUrl = doc.getString("imageUrl");

        previewImage.setVisibility(View.VISIBLE);
        Glide.with(this).load(uploadedImageUrl).into(previewImage);

        storyForm.setVisibility(View.VISIBLE);
        addStoryBtn.setVisibility(View.GONE);
        saveStoryBtn.setText("Update Story");
    }

    private void deleteStory(String id) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Story")
                .setMessage("Are you sure you want to delete this story?")
                .setPositiveButton("Delete", (d, w) -> {
                    db.collection("stories").document(id).delete();
                    loadStories();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
