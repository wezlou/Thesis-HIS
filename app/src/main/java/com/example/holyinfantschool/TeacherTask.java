package com.example.holyinfantschool;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TeacherTask extends AppCompatActivity {

    private LinearLayout announcementListContainer;
    private ScrollView announcementScroll, formSection;
    private ImageView backBtn, createAnnouncementBtn;
    private EditText announcementTitleInput, announcementContentInput;
    private LinearLayout uploadedFilesContainer;
    private Button postAnnouncementBtn;
    private ImageView uploadBtn;
    private TextView emptyMessage;

    private ConstraintLayout progressOverlay;
    private LinearLayout progressCard;
    private ProgressBar progressBar;
    private TextView progressText;

    private static final int PICK_FILE_REQUEST_CODE = 101;
    private static final int STORAGE_PERMISSION_CODE = 100;

    private final Map<String, Uri> uploadedFilesMap = new LinkedHashMap<>();

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private boolean isEditing = false;
    private String editingAnnouncementId = null;

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());

    private static final String CHANNEL_ID = "uploads_channel";
    private static final int NOTIFICATION_ID = 1201;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        setContentView(R.layout.activity_teacher_task);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initializeViews();
        setupListeners();
        createNotificationChannel();
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

        // ⭐ MUST MATCH YOUR NEW XML EXACTLY
        progressOverlay = findViewById(R.id.progressOverlay);   // ConstraintLayout
        progressCard = findViewById(R.id.progressCard);         // LinearLayout
        progressBar = findViewById(R.id.progressBar);           // ProgressBar
        progressText = findViewById(R.id.progressText);         // TextView
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
            hideKeyboard();
            lockUI("Please enter a title...");
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
            unlockUI();
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

        // ⭐ Hide keyboard → then show bottom sliding overlay
        hideKeyboard();
        lockUI("Posting announcement...");

        if (isEditing && editingAnnouncementId != null) {
            final String annId = editingAnnouncementId;

            db.collection("announcements")
                    .document(annId)
                    .update(data)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Announcement updated", Toast.LENGTH_SHORT).show();
                        isEditing = false;
                        editingAnnouncementId = null;

                        formSection.setVisibility(View.GONE);
                        announcementScroll.setVisibility(View.VISIBLE);

                        if (!uploadedFilesMap.isEmpty()) {
                            uploadFilesForAnnouncementWithNotification(annId);
                        } else {
                            unlockUI();
                            loadAnnouncements();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to update", Toast.LENGTH_SHORT).show();
                        unlockUI();
                    });

        } else {
            db.collection("announcements")
                    .add(data)
                    .addOnSuccessListener(docRef -> {
                        String announcementId = docRef.getId();
                        Toast.makeText(this, "Announcement posted!", Toast.LENGTH_SHORT).show();

                        announcementTitleInput.setText("");
                        announcementContentInput.setText("");

                        formSection.setVisibility(View.GONE);
                        announcementScroll.setVisibility(View.VISIBLE);

                        if (!uploadedFilesMap.isEmpty()) {
                            uploadFilesForAnnouncementWithNotification(announcementId);
                        } else {
                            unlockUI();
                            loadAnnouncements();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to post announcement", Toast.LENGTH_SHORT).show();
                        unlockUI();
                    });
        }
    }
    private void uploadFilesForAnnouncementWithNotification(String announcementId) {

        // snapshot of entries so we don't mutate while iterating
        List<Map.Entry<String, Uri>> entries = new ArrayList<>(uploadedFilesMap.entrySet());
        if (entries.isEmpty()) {
            unlockUI();
            loadAnnouncements();
            return;
        }

        final int totalFiles = entries.size();
        final AtomicInteger completed = new AtomicInteger(0);
        final AtomicInteger failed = new AtomicInteger(0);

        // Show initial notification (indeterminate for current file)
        showUploadNotification("Starting upload...", 0, totalFiles);

        // Set overlay to 0%
        runOnUiThread(() -> {
            progressText.setText("Uploading files… 0%");
            progressBar.setProgress(0);
        });

        // Run uploads on background thread (sequential)
        new Thread(() -> {
            for (int idx = 0; idx < entries.size(); idx++) {
                Map.Entry<String, Uri> entry = entries.get(idx);
                final String displayName = entry.getKey();
                final Uri fileUri = entry.getValue();

                // Update notification to show which file is uploading (indeterminate)
                updateNotificationIndeterminate("Uploading " + displayName, completed.get(), totalFiles);

                try {
                    // ⭐ Smooth fake progress from 0 → 90% while upload is running
                    runOnUiThread(() -> {
                        progressText.setText("Uploading " + displayName + "...");
                        progressBar.setProgress(0);
                    });

                    Thread fakeProgressThread = new Thread(() -> {
                        int fake = 0;
                        while (fake < 90) {
                            fake++;

                            int finalFake = fake;
                            runOnUiThread(() -> {
                                progressBar.setProgress(finalFake);
                                progressText.setText("Uploading… " + finalFake + "%");
                            });

                            try { Thread.sleep(40); } catch (Exception ignored) {}
                        }
                    });
                    fakeProgressThread.start();

                    // Blocking call to UploadCareUploader (already implemented)
                    String cdnUrl = UploadCareUploader.upload(TeacherTask.this, fileUri);

                    // Add file metadata to Firestore under announcement -> sharedFiles
                    Map<String, Object> fileData = new HashMap<>();
                    fileData.put("fileName", displayName);
                    fileData.put("fileUrl", cdnUrl);
                    fileData.put("timestamp", new Date());

                    // Add to Firestore (fire-and-forget but we'll wait for completion to maintain ordering)
                    final Object lock = new Object();
                    final boolean[] firestoreOk = {false};
                    db.collection("announcements")
                            .document(announcementId)
                            .collection("sharedFiles")
                            .add(fileData)
                            .addOnSuccessListener(docRef -> {
                                firestoreOk[0] = true;
                                synchronized (lock) { lock.notify(); }
                            })
                            .addOnFailureListener(e -> {
                                firestoreOk[0] = false;
                                synchronized (lock) { lock.notify(); }
                            });

                    // Wait up to short time for Firestore write to complete to keep ordering consistent
                    synchronized (lock) {
                        try {
                            lock.wait(5000); // wait up to 5s
                        } catch (InterruptedException ignored) {}
                    }

                    completed.incrementAndGet();

                    // Update determinate overall progress
                    updateNotificationProgress("Uploaded " + completed.get() + " / " + totalFiles,
                            completed.get(), totalFiles);

                    final int cFiles = completed.get();
                    final int percent = (int) ((cFiles / (float) totalFiles) * 100f);

                    // ⭐ Smooth finish from 90 → 100%
                    for (int p = 90; p <= 100; p++) {
                        int finalP = p;
                        runOnUiThread(() -> {
                            progressBar.setProgress(finalP);
                            progressText.setText("Finishing… " + finalP + "%");
                        });

                        try { Thread.sleep(15); } catch (Exception ignored) {}
                    }


                    runOnUiThread(() -> {
                        if (progressOverlay.getVisibility() != View.VISIBLE) {
                            progressOverlay.setVisibility(View.VISIBLE);
                        }

                        progressBar.setProgress(100);
                        progressText.setText("Upload Complete ✓");
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    failed.incrementAndGet();
                    // update notification to reflect failure for this file
                    updateNotificationProgress("Failed: " + displayName, completed.get(), totalFiles);
                    final String errMsg = e.getMessage() != null ? e.getMessage() : "Upload error";
                    runOnUiThread(() -> Toast.makeText(TeacherTask.this,
                            "Upload failed: " + displayName + " — " + errMsg, Toast.LENGTH_LONG).show());
                }
            }

            // All files processed
            boolean anyFailed = failed.get() > 0;
            if (!anyFailed) {
                // success
                finishNotification("All files uploaded (" + completed.get() + "/" + totalFiles + ")", true);
            } else {
                finishNotification("Upload completed with errors (" + completed.get() + "/" + totalFiles + ")", false);
            }

            // Clear local map and UI on main thread and refresh page
            runOnUiThread(() -> {
                uploadedFilesMap.clear();
                uploadedFilesContainer.removeAllViews();
                unlockUI();
                loadAnnouncements(); // refresh UI
            });

        }).start();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "File uploads";
            String description = "Notifications for ongoing file uploads";
            int importance = NotificationManager.IMPORTANCE_LOW; // low to avoid sound
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    private void showUploadNotification(String title, int completedFiles, int totalFiles) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_upload)
                .setContentTitle(title)
                .setContentText("Uploading attachments")
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setProgress(totalFiles, completedFiles, totalFiles == 0); // indeterminate if totalFiles==0

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, builder.build());
    }

    private void updateNotificationIndeterminate(String title, int completedFiles, int totalFiles) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_upload)
                .setContentTitle(title)
                .setContentText("Uploading attachments")
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setProgress(totalFiles, completedFiles, true); // indeterminate for current file
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, builder.build());
    }

    private void updateNotificationProgress(String title, int completedFiles, int totalFiles) {
        int progressPercent = 0;
        if (totalFiles > 0) progressPercent = (int) ((completedFiles / (float) totalFiles) * 100f);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_upload)
                .setContentTitle(title)
                .setContentText("Uploaded " + completedFiles + " of " + totalFiles)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setProgress(100, progressPercent, false);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, builder.build());
    }

    private void finishNotification(String title, boolean success) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(success ? R.drawable.ic_check : R.drawable.ic_error)
                .setContentTitle(title)
                .setContentText(success ? "All uploads finished" : "Some uploads failed")
                .setOnlyAlertOnce(true)
                .setOngoing(false)
                .setProgress(0, 0, false)
                .setAutoCancel(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, builder.build());

        // remove it from shade after short delay (optional)
        final NotificationManagerCompat nm = NotificationManagerCompat.from(this);
        new Thread(() -> {
            try { Thread.sleep(3500); } catch (InterruptedException ignored) {}
            nm.cancel(NOTIFICATION_ID);
        }).start();
    }

    // -------------------------------------------------------
    // rest of your original code (unchanged but adapted to new overlay)
    // -------------------------------------------------------

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
                int idx;
                try {
                    idx = cursor.getColumnIndexOrThrow("_display_name");
                } catch (Exception ex) {
                    idx = cursor.getColumnIndex("_display_name");
                }
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
        titleView.setTextColor(Color.parseColor("#1F9D52"));
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

        // ============ FILES CONTAINER WITH PREVIEW ============
        LinearLayout filesContainer = new LinearLayout(this);
        filesContainer.setOrientation(LinearLayout.VERTICAL);
        filesContainer.setPadding(0, 12, 0, 12);
        contentLayout.addView(filesContainer);

        // Listen for sharedFiles
        db.collection("announcements").document(announcementId).collection("sharedFiles")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((filesSnap, err) -> {
                    if (err != null || filesSnap == null) return;
                    filesContainer.removeAllViews();

                    for (DocumentSnapshot f : filesSnap) {
                        String fname = f.getString("fileName");
                        String furl = f.getString("fileUrl");
                        if (fname == null || furl == null) continue;

                        String ext = getFileExtension(fname).toLowerCase();
                        boolean isImage = isImageFile(ext);

                        if (isImage) {
                            // IMAGE pill-style row
                            LinearLayout fileRow = new LinearLayout(this);
                            fileRow.setOrientation(LinearLayout.HORIZONTAL);
                            fileRow.setGravity(Gravity.CENTER_VERTICAL);
                            fileRow.setPadding(20, 18, 20, 18);
                            LinearLayout.LayoutParams fileRowParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                            fileRowParams.setMargins(0, 8, 0, 8);
                            fileRow.setLayoutParams(fileRowParams);
                            fileRow.setBackgroundResource(R.drawable.gc_file_bg);

                            ImageView fileIcon = new ImageView(this);
                            fileIcon.setImageResource(R.drawable.ic_file);
                            int iconSize = dpToPx(42);
                            LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(iconSize, iconSize);
                            iconLp.setMargins(0, 0, dpToPx(14), 0);
                            fileIcon.setLayoutParams(iconLp);
                            try {
                                fileIcon.setColorFilter(ContextCompat.getColor(this, R.color.image_icon));
                            } catch (Exception e) {
                                fileIcon.setColorFilter(ContextCompat.getColor(this, R.color.teal_700));
                            }

                            TextView fileNameView = new TextView(this);
                            fileNameView.setText(fname);
                            fileNameView.setTextSize(15f);
                            fileNameView.setTextColor(Color.parseColor("#054A2C"));
                            fileNameView.setMaxLines(1);
                            fileNameView.setEllipsize(TextUtils.TruncateAt.END);
                            LinearLayout.LayoutParams nameLp = new LinearLayout.LayoutParams(
                                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                            fileNameView.setLayoutParams(nameLp);

                            TextView extLabel = new TextView(this);
                            extLabel.setText(ext.toUpperCase());
                            extLabel.setTextSize(12f);
                            extLabel.setTextColor(Color.parseColor("#1F9D52"));
                            LinearLayout.LayoutParams extLp = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            extLp.setMargins(dpToPx(8), 0, 0, 0);
                            extLabel.setLayoutParams(extLp);

                            fileRow.addView(fileIcon);
                            fileRow.addView(fileNameView);
                            fileRow.addView(extLabel);

                            final String finalFurlImg = furl;
                            final String finalFnameImg = fname;
                            fileRow.setOnClickListener(v -> {
                                Intent p = new Intent(TeacherTask.this, PreviewActivity.class);
                                p.putExtra(PreviewActivity.EXTRA_URL, finalFurlImg);
                                p.putExtra(PreviewActivity.EXTRA_DISPLAY_NAME, finalFnameImg);
                                startActivity(p);
                            });

                            filesContainer.addView(fileRow);

                        } else {
                            // NON-IMAGE pill-style row
                            LinearLayout fileRow = new LinearLayout(this);
                            fileRow.setOrientation(LinearLayout.HORIZONTAL);
                            fileRow.setGravity(Gravity.CENTER_VERTICAL);
                            fileRow.setPadding(20, 18, 20, 18);
                            LinearLayout.LayoutParams fileRowParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                            fileRowParams.setMargins(0, 8, 0, 8);
                            fileRow.setLayoutParams(fileRowParams);
                            fileRow.setBackgroundResource(R.drawable.gc_file_bg);

                            ImageView fileIcon = new ImageView(this);
                            fileIcon.setImageResource(getFileIcon(ext));
                            int iconSize = dpToPx(42);
                            LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(iconSize, iconSize);
                            iconLp.setMargins(0, 0, dpToPx(14), 0);
                            fileIcon.setLayoutParams(iconLp);

                            int tintColor = ContextCompat.getColor(this, R.color.teal_700);
                            switch (ext) {
                                case "pdf": tintColor = Color.parseColor("#D32F2F"); break;
                                case "ppt": case "pptx": tintColor = Color.parseColor("#EF6C00"); break;
                                case "doc": case "docx": tintColor = Color.parseColor("#1976D2"); break;
                                case "mp4": case "mov": case "mkv": tintColor = Color.parseColor("#6A1B9A"); break;
                                default: tintColor = ContextCompat.getColor(this, R.color.teal_700); break;
                            }
                            fileIcon.setColorFilter(tintColor);

                            TextView fileNameView = new TextView(this);
                            fileNameView.setText(fname);
                            fileNameView.setTextSize(15f);
                            fileNameView.setTextColor(Color.parseColor("#054A2C"));
                            fileNameView.setMaxLines(1);
                            fileNameView.setEllipsize(TextUtils.TruncateAt.END);
                            LinearLayout.LayoutParams nameLp = new LinearLayout.LayoutParams(
                                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                            fileNameView.setLayoutParams(nameLp);

                            TextView extLabel = new TextView(this);
                            extLabel.setText(ext.toUpperCase());
                            extLabel.setTextSize(12f);
                            extLabel.setTextColor(Color.parseColor("#BDBDBD"));
                            LinearLayout.LayoutParams extLp = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            extLp.setMargins(dpToPx(8), 0, 0, 0);
                            extLabel.setLayoutParams(extLp);

                            fileRow.addView(fileIcon);
                            fileRow.addView(fileNameView);
                            fileRow.addView(extLabel);

                            final String finalFurl = furl;
                            final String finalFname = fname;
                            fileRow.setOnClickListener(v -> {
                                Intent p = new Intent(TeacherTask.this, PreviewActivity.class);
                                p.putExtra(PreviewActivity.EXTRA_URL, finalFurl);
                                p.putExtra(PreviewActivity.EXTRA_DISPLAY_NAME, finalFname);
                                startActivity(p);
                            });

                            filesContainer.addView(fileRow);
                        }
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
                                // delete related comments and sharedFiles metadata
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
                                loadAnnouncements();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show());

        card.addView(contentLayout);
        return card;
    }

    // ============ HELPER METHODS ============

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int idx = fileName.lastIndexOf('.');
        return idx > 0 ? fileName.substring(idx + 1) : "";
    }

    private boolean isImageFile(String ext) {
        return ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png")
                || ext.equals("gif") || ext.equals("webp") || ext.equals("bmp") || ext.equals("heic");
    }

    private int getFileIcon(String ext) {
        switch (ext.toLowerCase()) {
            case "pdf": return R.drawable.ic_pdf;
            case "doc":
            case "docx": return R.drawable.ic_doc;
            case "xls":
            case "xlsx": return R.drawable.ic_xls;
            case "ppt":
            case "pptx": return R.drawable.ic_ppt;
            case "txt": return R.drawable.ic_txt;
            case "zip":
            case "rar": return R.drawable.ic_zip;
            case "mp4":
            case "mov":
            case "mkv":
            case "3gp":
            case "webm": return R.drawable.ic_video;
            default: return R.drawable.ic_file;
        }
    }

    private MaterialCardView createModernCard() {
        MaterialCardView card = new MaterialCardView(this);

        card.setCardElevation(12f);
        card.setRadius(22f);
        card.setUseCompatPadding(true);

        // Dark Jungle Background
        card.setCardBackgroundColor(Color.parseColor("#FFFFFF"));

        // Border
        card.setStrokeWidth(3);
        card.setStrokeColor(Color.parseColor("#29C36A"));

        // Ripple
        card.setForeground(ContextCompat.getDrawable(this, R.drawable.ripple_card_bg_dark));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 35);
        card.setLayoutParams(params);

        return card;
    }

    private TextView createTextView(String t, float size, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(t != null ? t : "");
        tv.setTextSize(size);
        if (bold) tv.setTypeface(null, android.graphics.Typeface.BOLD);

        tv.setTextColor(Color.parseColor("#054A2C"));

        return tv;
    }

    // NEW lock/unlock using progressOverlay
    private void lockUI(String message) {
        hideKeyboard();

        if (progressOverlay != null) {
            progressOverlay.setVisibility(View.VISIBLE);
        }

        if (progressCard != null) {
            progressCard.clearAnimation();
            progressCard.startAnimation(
                    AnimationUtils.loadAnimation(this, R.anim.slide_up)
            );
        }

        if (progressText != null) {
            progressText.setText(message != null ? message : "Loading...");
        }

        if (progressBar != null) {
            progressBar.setProgress(0);
        }

        // Disable touches
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        );
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void unlockUI() {
        if (progressOverlay != null) {
            progressOverlay.setVisibility(View.GONE);
        }
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void addInlineCommentSection(LinearLayout parent, String announcementId) {

        // Divider
        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 2));
        divider.setBackgroundColor(Color.parseColor("#D0D0D0"));
        parent.addView(divider);

        // Section Header
        TextView commentHeader = createTextView("💬 Comments", 15f, true);
        commentHeader.setPadding(0, 10, 0, 10);
        parent.addView(commentHeader);

        // Main container for all comments
        LinearLayout commentSection = new LinearLayout(this);
        commentSection.setOrientation(LinearLayout.VERTICAL);
        commentSection.setPadding(16, 8, 16, 8);
        parent.addView(commentSection);

        // MAIN COMMENT INPUT BOX
        addMainCommentInput(parent, announcementId);

        // Firestore listener for ALL comments (main + replies)
        db.collection("comments")
                .whereEqualTo("announcementId", announcementId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {

                    if (error != null || value == null) return;

                    commentSection.removeAllViews();

                    List<DocumentSnapshot> all = value.getDocuments();

                    // Separate "main" comments
                    List<DocumentSnapshot> main = new ArrayList<>();
                    for (DocumentSnapshot d : all) {
                        String parentId = d.getString("parentId");
                        if (parentId == null) main.add(d);
                    }

                    // Render all main comments
                    for (DocumentSnapshot mainComment : main) {

                        // MAIN COMMENT 🔵
                        LinearLayout mainBubble = buildCommentBubble(
                                mainComment.getString("user"),
                                mainComment.getString("text"),
                                false  // not a reply
                        );
                        commentSection.addView(mainBubble);

                        // "Reply" clickable text
                        TextView replyBtn = new TextView(this);
                        replyBtn.setText("Reply");
                        replyBtn.setTextColor(Color.parseColor("#29C36A"));
                        replyBtn.setPadding(16, 4, 0, 12);
                        replyBtn.setTextSize(13f);
                        commentSection.addView(replyBtn);

                        // Hidden reply input
                        LinearLayout replyInput = buildReplyInput(mainComment.getId(), announcementId);
                        replyInput.setVisibility(View.GONE);
                        commentSection.addView(replyInput);

                        replyBtn.setOnClickListener(v -> {
                            replyInput.setVisibility(
                                    replyInput.getVisibility() == View.GONE ? View.VISIBLE : View.GONE
                            );
                        });

                        // Find replies for this comment
                        for (DocumentSnapshot d : all) {
                            String parentId = d.getString("parentId");
                            if (parentId != null && parentId.equals(mainComment.getId())) {

                                // REPLY COMMENT 🟢
                                LinearLayout replyBubble = buildCommentBubble(
                                        d.getString("user"),
                                        d.getString("text"),
                                        true  // reply mode = indented
                                );
                                commentSection.addView(replyBubble);
                            }
                        }
                    }
                });
    }

    private LinearLayout buildCommentBubble(String user, String text, boolean isReply) {

        LinearLayout bubble = new LinearLayout(this);
        bubble.setOrientation(LinearLayout.VERTICAL);

        if (isReply) {
            bubble.setBackgroundResource(R.drawable.comment_reply_bubble);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(dpToPx(40), dpToPx(4), dpToPx(4), dpToPx(4));  // INDENT
            bubble.setLayoutParams(params);
        } else {
            bubble.setBackgroundResource(R.drawable.comment_bubble_bg);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));  // No indent
            bubble.setLayoutParams(params);
        }

        bubble.setPadding(20, 12, 20, 12);

        TextView userView = createTextView(user, 13f, true);
        userView.setTextColor(Color.parseColor("#1F9D52"));

        TextView textView = createTextView(text, 14f, false);
        textView.setTextColor(Color.parseColor("#054A2C"));

        bubble.addView(userView);
        bubble.addView(textView);

        return bubble;
    }

    private LinearLayout buildReplyInput(String parentCommentId, String announcementId) {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(8, 8, 8, 8);

        EditText input = new EditText(this);
        input.setHint("Write a reply...");
        input.setBackgroundResource(R.drawable.input_rounded);
        input.setPadding(24, 12, 24, 12);
        input.setTextSize(14f);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
        );
        lp.setMargins(8, 8, 16, 8);
        input.setLayoutParams(lp);

        ImageButton send = new ImageButton(this);
        send.setImageResource(R.drawable.ic_send);
        send.setBackgroundResource(R.drawable.btn_round_send);

        send.setOnClickListener(v -> {
            String replyText = input.getText().toString().trim();
            if (replyText.isEmpty()) return;

            FirebaseUser u = auth.getCurrentUser();
            String userEmail = (u != null && u.getEmail() != null) ? u.getEmail() : "Anonymous";

            Map<String, Object> data = new HashMap<>();
            data.put("announcementId", announcementId);
            data.put("parentId", parentCommentId);  // This makes it a REPLY
            data.put("user", userEmail);
            data.put("text", replyText);
            data.put("timestamp", new Date());

            db.collection("comments").add(data);
            input.setText("");
        });

        layout.addView(input);
        layout.addView(send);

        return layout;
    }

    private void addMainCommentInput(LinearLayout parent, String announcementId) {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(8, 12, 8, 8);

        EditText input = new EditText(this);
        input.setHint("Add a comment...");
        input.setBackgroundResource(R.drawable.input_rounded);
        input.setPadding(24, 12, 24, 12);
        input.setTextSize(14f);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
        );
        lp.setMargins(8, 8, 16, 8);
        input.setLayoutParams(lp);

        ImageButton send = new ImageButton(this);
        send.setImageResource(R.drawable.ic_send);
        send.setBackgroundResource(R.drawable.btn_round_send);

        send.setOnClickListener(v -> {
            String text = input.getText().toString().trim();
            if (text.isEmpty()) return;

            FirebaseUser u = auth.getCurrentUser();
            String email = (u != null && u.getEmail() != null) ? u.getEmail() : "Anonymous";

            Map<String, Object> data = new HashMap<>();
            data.put("announcementId", announcementId);
            data.put("parentId", null);  // MAIN COMMENT
            data.put("user", email);
            data.put("text", text);
            data.put("timestamp", new Date());

            db.collection("comments").add(data);
            input.setText("");
        });

        layout.addView(input);
        layout.addView(send);

        parent.addView(layout);
    }

}
