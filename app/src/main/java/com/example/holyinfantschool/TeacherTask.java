package com.example.holyinfantschool;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class TeacherTask extends AppCompatActivity {

    private LinearLayout announcementListContainer;
    private ScrollView announcementScroll, formSection;
    private ImageView backBtn, createAnnouncementBtn;
    private EditText announcementTitleInput, announcementContentInput;
    private LinearLayout uploadedFilesContainer;
    private Button uploadBtn, postAnnouncementBtn;
    private TextView emptyMessage;

    private static final int PICK_FILE_REQUEST_CODE = 101;
    private static final int STORAGE_PERMISSION_CODE = 100;

    // map: displayName -> Uri
    private final Map<String, Uri> uploadedFilesMap = new LinkedHashMap<>();

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private boolean isEditing = false;
    private String editingAnnouncementId = null;

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_task);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initializeViews();
        setupListeners();

        loadAnnouncements();
    }

    private void initializeViews() {
        announcementScroll = findViewById(R.id.announcementScroll);
        formSection = findViewById(R.id.formSection);
        announcementListContainer = findViewById(R.id.announcementListContainer);
        backBtn = findViewById(R.id.backToTeacherSite);
        createAnnouncementBtn = findViewById(R.id.createAnnouncementBtn);
        announcementTitleInput = findViewById(R.id.announcementTitleInput);
        announcementContentInput = findViewById(R.id.announcementContentInput);
        uploadedFilesContainer = findViewById(R.id.uploadedFilesContainer);
        uploadBtn = findViewById(R.id.uploadBtn);
        postAnnouncementBtn = findViewById(R.id.postAnnouncementBtn);
        emptyMessage = findViewById(R.id.emptyMessageTeacher);
    }

    private void setupListeners() {
        createAnnouncementBtn.setOnClickListener(v -> showCreateForm(false, null, null, null));

        backBtn.setOnClickListener(v -> {
            if (formSection.getVisibility() == View.VISIBLE) {
                formSection.setVisibility(View.GONE);
                announcementScroll.setVisibility(View.VISIBLE);
                isEditing = false;
                editingAnnouncementId = null;
            } else {
                Intent i = new Intent(TeacherTask.this, TeacherSite.class);
                startActivity(i);
                finish();
            }
        });

        uploadBtn.setOnClickListener(v -> checkStoragePermission());
        postAnnouncementBtn.setOnClickListener(v -> postAnnouncement());
    }

    private void showCreateForm(boolean editing, String announcementId, String title, String content) {
        isEditing = editing;
        editingAnnouncementId = announcementId;

        announcementScroll.setVisibility(View.GONE);
        formSection.setVisibility(View.VISIBLE);

        if (editing) {
            announcementTitleInput.setText(title != null ? title : "");
            announcementContentInput.setText(content != null ? content : "");
            postAnnouncementBtn.setText("Update Announcement");
        } else {
            announcementTitleInput.setText("");
            announcementContentInput.setText("");
            uploadedFilesMap.clear();
            uploadedFilesContainer.removeAllViews();
            postAnnouncementBtn.setText("Post Announcement");
        }
    }

    private void postAnnouncement() {
        String title = announcementTitleInput.getText().toString().trim();
        String content = announcementContentInput.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = auth.getCurrentUser();
        String teacherEmail = (currentUser != null && currentUser.getEmail() != null)
                ? currentUser.getEmail() : "unknown";

        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("content", content);
        data.put("teacherEmail", teacherEmail);
        data.put("timestamp", new Date());

        if (isEditing && editingAnnouncementId != null) {
            final String annId = editingAnnouncementId;
            db.collection("announcements").document(annId)
                    .update(data)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Announcement updated", Toast.LENGTH_SHORT).show();
                        isEditing = false;
                        editingAnnouncementId = null;
                        formSection.setVisibility(View.GONE);
                        announcementScroll.setVisibility(View.VISIBLE);

                        if (!uploadedFilesMap.isEmpty()) {
                            uploadFilesForAnnouncement(annId);
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to update", Toast.LENGTH_SHORT).show());
        } else {
            db.collection("announcements")
                    .add(data)
                    .addOnSuccessListener(docRef -> {
                        String announcementId = docRef.getId();
                        Toast.makeText(this, "Announcement posted!", Toast.LENGTH_SHORT).show();
                        uploadedFilesContainer.removeAllViews();
                        announcementTitleInput.setText("");
                        announcementContentInput.setText("");
                        formSection.setVisibility(View.GONE);
                        announcementScroll.setVisibility(View.VISIBLE);

                        if (!uploadedFilesMap.isEmpty()) {
                            uploadFilesForAnnouncement(announcementId);
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to post announcement", Toast.LENGTH_SHORT).show());
        }
    }

    private void uploadFilesForAnnouncement(String announcementId) {
        for (Map.Entry<String, Uri> entry : uploadedFilesMap.entrySet()) {
            final String displayName = entry.getKey();
            final Uri fileUri = entry.getValue();

            new Thread(() -> {
                try {
                    String storedFileName = BackblazeUploader.uploadFile(TeacherTask.this, fileUri);

                    Map<String, Object> fileData = new HashMap<>();
                    fileData.put("fileName", displayName);
                    fileData.put("storedFileName", storedFileName);
                    fileData.put("timestamp", new Date());

                    db.collection("announcements")
                            .document(announcementId)
                            .collection("sharedFiles")
                            .add(fileData);

                    runOnUiThread(() ->
                            Toast.makeText(TeacherTask.this, "Uploaded: " + displayName, Toast.LENGTH_SHORT).show()
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(TeacherTask.this, "Upload failed: " + displayName + " — " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }).start();
        }

        uploadedFilesMap.clear();
        runOnUiThread(() -> uploadedFilesContainer.removeAllViews());
    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                openFilePicker();
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, STORAGE_PERMISSION_CODE);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                openFilePicker();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE);
            }
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            String fileName = getFileName(fileUri);
            if (fileName == null) fileName = "file_" + System.currentTimeMillis();

            String displayName = fileName;
            int i = 1;
            while (uploadedFilesMap.containsKey(displayName)) {
                displayName = fileName + " (" + i + ")";
                i++;
            }

            uploadedFilesMap.put(displayName, fileUri);
            addFileToUploadsList(displayName, fileUri);
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndexOrThrow("_display_name");
                result = cursor.getString(idx);
            }
        } catch (Exception ignored) {}
        if (result == null && uri != null) result = uri.getLastPathSegment();
        return result;
    }

    private void addFileToUploadsList(String fileName, Uri uri) {
        View item = LayoutInflater.from(this).inflate(R.layout.item_uploaded_file, uploadedFilesContainer, false);
        TextView nameView = item.findViewById(R.id.fileName);
        ImageView deleteBtn = item.findViewById(R.id.deleteBtn);

        nameView.setText(fileName);
        deleteBtn.setOnClickListener(v -> {
            uploadedFilesMap.remove(fileName);
            uploadedFilesContainer.removeView(item);
        });

        uploadedFilesContainer.addView(item);
    }

    private void loadAnnouncements() {
        announcementListContainer.removeAllViews();
        emptyMessage.setVisibility(View.GONE);

        FirebaseUser currentUser = auth.getCurrentUser();
        String teacherEmail = (currentUser != null && currentUser.getEmail() != null) ? currentUser.getEmail() : "";

        db.collection("announcements")
                .whereEqualTo("teacherEmail", teacherEmail)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Failed to load announcements", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    announcementListContainer.removeAllViews();

                    if (value == null || value.isEmpty()) {
                        emptyMessage.setVisibility(View.VISIBLE);
                        return;
                    }

                    emptyMessage.setVisibility(View.GONE);

                    for (DocumentSnapshot doc : value) {
                        String title = doc.getString("title");
                        String teacher = doc.getString("teacherEmail");
                        String content = doc.getString("content");
                        Date timestamp = doc.getDate("timestamp");
                        String announcementId = doc.getId();

                        View card = buildAnnouncementCard(announcementId, title, teacher, content, timestamp);
                        announcementListContainer.addView(card);
                    }
                });
    }

    private View buildAnnouncementCard(String announcementId, String title, String teacherEmail, String content, Date timestamp) {
        CardView card = createModernCard();
        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(40, 30, 40, 30);

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView titleView = createTextView(title, 18f, true);
        titleView.setTextColor(ContextCompat.getColor(this, R.color.black));
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        titleView.setLayoutParams(titleParams);

        ImageButton editBtn = new ImageButton(this);
        editBtn.setImageResource(R.drawable.ic_edit);
        editBtn.setBackground(null);
        editBtn.setContentDescription("Edit announcement");

        ImageButton deleteBtn = new ImageButton(this);
        deleteBtn.setImageResource(R.drawable.ic_delete);
        deleteBtn.setBackground(null);
        deleteBtn.setContentDescription("Delete announcement");

        topRow.addView(titleView);
        topRow.addView(editBtn);
        topRow.addView(deleteBtn);

        contentLayout.addView(topRow);

        TextView teacherView = createTextView("By " + teacherEmail, 14f, false);
        TextView contentView = createTextView(content, 15f, false);
        TextView timeView = createTextView(DATE_FORMAT.format(timestamp != null ? timestamp : new Date()), 12f, false);
        timeView.setGravity(Gravity.END);

        contentLayout.addView(teacherView);
        contentLayout.addView(contentView);
        contentLayout.addView(timeView);

        LinearLayout filesContainer = new LinearLayout(this);
        filesContainer.setOrientation(LinearLayout.VERTICAL);
        filesContainer.setPadding(0, 12, 0, 12);
        contentLayout.addView(filesContainer);

        db.collection("announcements").document(announcementId).collection("sharedFiles")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((filesSnap, err) -> {
                    if (err != null || filesSnap == null) return;
                    filesContainer.removeAllViews();
                    for (DocumentSnapshot f : filesSnap) {
                        String fname = f.getString("fileName");
                        String stored = f.getString("storedFileName");
                        if (fname == null || stored == null) continue;

                        TextView fileView = createTextView(fname, 14f, false);
                        fileView.setOnClickListener(v -> {
                            new Thread(() -> {
                                try {
                                    String url = BackblazeUploader.generateDownloadUrl(stored);
                                    // open preview activity in-app
                                    Intent p = new Intent(TeacherTask.this, PreviewActivity.class);
                                    p.putExtra(PreviewActivity.EXTRA_URL, url);
                                    p.putExtra(PreviewActivity.EXTRA_DISPLAY_NAME, fname);
                                    startActivity(p);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    runOnUiThread(() -> Toast.makeText(TeacherTask.this, "Cannot open file: " + ex.getMessage(), Toast.LENGTH_SHORT).show());
                                }
                            }).start();
                        });
                        filesContainer.addView(fileView);
                    }
                });

        addInlineCommentSection(contentLayout, announcementId);

        editBtn.setOnClickListener(v -> showCreateForm(true, announcementId, title, content));

        deleteBtn.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Delete announcement")
                .setMessage("Are you sure you want to delete this announcement?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("announcements").document(announcementId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                db.collection("comments").whereEqualTo("announcementId", announcementId).get()
                                        .addOnSuccessListener(query -> {
                                            for (DocumentSnapshot c : query) {
                                                db.collection("comments").document(c.getId()).delete();
                                            }
                                        });
                                db.collection("announcements").document(announcementId).collection("sharedFiles")
                                        .get().addOnSuccessListener(q -> {
                                            for (DocumentSnapshot f : q) {
                                                db.collection("announcements").document(announcementId)
                                                        .collection("sharedFiles").document(f.getId()).delete();
                                            }
                                        });

                                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show());

        card.addView(contentLayout);
        return card;
    }

    private CardView createModernCard() {
        CardView card = new CardView(this);
        card.setCardElevation(10f);
        card.setRadius(24f);
        card.setUseCompatPadding(true);
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 40);
        card.setLayoutParams(params);
        card.setClickable(true);
        card.setFocusable(true);
        card.setForeground(ContextCompat.getDrawable(this, R.drawable.ripple_card_bg));
        return card;
    }

    private TextView createTextView(String text, float size, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(text != null ? text : "");
        tv.setTextSize(size);
        if (bold) tv.setTypeface(null, android.graphics.Typeface.BOLD);
        tv.setTextColor(ContextCompat.getColor(this, R.color.black));
        return tv;
    }

    private void addInlineCommentSection(LinearLayout parent, String announcementId) {
        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2));
        divider.setBackgroundColor(ContextCompat.getColor(this, R.color.light_gray));
        parent.addView(divider);

        TextView commentHeader = createTextView("💬 Comments", 15f, true);
        commentHeader.setPadding(0, 10, 0, 10);
        parent.addView(commentHeader);

        LinearLayout commentList = new LinearLayout(this);
        commentList.setOrientation(LinearLayout.VERTICAL);
        commentList.setPadding(16, 8, 16, 8);
        parent.addView(commentList);

        LinearLayout commentInputLayout = new LinearLayout(this);
        commentInputLayout.setOrientation(LinearLayout.HORIZONTAL);
        commentInputLayout.setGravity(Gravity.CENTER_VERTICAL);
        commentInputLayout.setPadding(8, 8, 8, 8);

        EditText commentInput = new EditText(this);
        commentInput.setHint("Add a comment...");
        commentInput.setBackgroundResource(R.drawable.input_rounded);
        commentInput.setPadding(32, 20, 32, 20);
        commentInput.setTextSize(14f);

        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        inputParams.setMargins(8, 8, 12, 8);
        commentInput.setLayoutParams(inputParams);
        commentInputLayout.addView(commentInput);

        FrameLayout sendContainer = new FrameLayout(this);
        LinearLayout.LayoutParams sendParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        sendContainer.setLayoutParams(sendParams);

        ImageButton sendButton = new ImageButton(this);
        sendButton.setImageResource(R.drawable.ic_send);
        sendButton.setBackgroundResource(R.drawable.btn_round_send);
        sendButton.setContentDescription("Send comment");
        sendContainer.addView(sendButton);

        ProgressBar loadingSpinner = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
        loadingSpinner.setVisibility(View.GONE);
        loadingSpinner.setIndeterminate(true);
        sendContainer.addView(loadingSpinner);

        commentInputLayout.addView(sendContainer);
        parent.addView(commentInputLayout);

        db.collection("comments")
                .whereEqualTo("announcementId", announcementId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    commentList.removeAllViews();

                    for (DocumentSnapshot doc : value) {
                        String user = doc.getString("user");
                        String text = doc.getString("text");

                        LinearLayout bubble = new LinearLayout(this);
                        bubble.setOrientation(LinearLayout.VERTICAL);
                        bubble.setBackgroundResource(R.drawable.comment_bubble_bg);
                        bubble.setPadding(20, 12, 20, 12);

                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        lp.setMargins(0, 6, 0, 6);
                        bubble.setLayoutParams(lp);

                        TextView userView = createTextView(user, 13f, true);
                        userView.setTextColor(ContextCompat.getColor(this, R.color.teal_700));

                        TextView textView = createTextView(text, 14f, false);
                        textView.setTextColor(ContextCompat.getColor(this, R.color.black));

                        bubble.addView(userView);
                        bubble.addView(textView);
                        commentList.addView(bubble);
                    }
                });

        sendButton.setOnClickListener(v -> {
            String commentText = commentInput.getText().toString().trim();
            if (commentText.isEmpty()) return;

            sendButton.setEnabled(false);
            sendButton.setAlpha(0.5f);
            loadingSpinner.setVisibility(View.VISIBLE);
            sendButton.setVisibility(View.INVISIBLE);

            FirebaseUser currentUser = auth.getCurrentUser();
            String user = (currentUser != null && currentUser.getEmail() != null)
                    ? currentUser.getEmail()
                    : "Anonymous";

            Map<String, Object> data = new HashMap<>();
            data.put("announcementId", announcementId);
            data.put("user", user);
            data.put("text", commentText);
            data.put("timestamp", new Date());

            db.collection("comments").add(data)
                    .addOnSuccessListener(unused -> {
                        commentInput.setText("");
                        Toast.makeText(this, "Comment sent 💬", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to send comment ❌", Toast.LENGTH_SHORT).show();
                    })
                    .addOnCompleteListener(task -> {
                        sendButton.setEnabled(true);
                        sendButton.setAlpha(1f);
                        loadingSpinner.setVisibility(View.GONE);
                        sendButton.setVisibility(View.VISIBLE);
                    });
        });
    }
}
