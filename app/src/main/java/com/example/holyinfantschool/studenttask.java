package com.example.holyinfantschool;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupMenu;
import android.widget.Button;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class studenttask extends AppCompatActivity {

    private LinearLayout assignedTasksContainer;
    private TextView filterAll, filterText, filterImages, filterVideos, filterPdfs, filterDocs;
    private TextView emptyMessage;

    private ImageView backButton, settingsButton;

    private boolean isMuted = false;
    private MediaPlayer mediaPlayer;

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_studenttask);

        assignedTasksContainer = findViewById(R.id.assignedTasksContainer);
        emptyMessage = findViewById(R.id.emptyMessage);
        db = FirebaseFirestore.getInstance();

        // New top nav buttons
        backButton = findViewById(R.id.backbtn);
        settingsButton = findViewById(R.id.settingsButton);

        // Background Music
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        // ✅ Back Button
        backButton.setOnClickListener(v -> {
            stopMusic();
            finish(); // close activity
        });

        // ✅ Settings Button → Popup Menu
        settingsButton.setOnClickListener(v -> showSettingsMenu(settingsButton));

        // Filters
        filterAll = findViewById(R.id.filterAll);
        filterText = findViewById(R.id.filterText);
        filterImages = findViewById(R.id.filterImages);
        filterVideos = findViewById(R.id.filterVideos);
        filterPdfs = findViewById(R.id.filterPdfs);
        filterDocs = findViewById(R.id.filterDocs);

        highlightSelected(filterAll);
        loadContent("all");

        filterAll.setOnClickListener(v -> { highlightSelected(filterAll); loadContent("all"); });
        filterText.setOnClickListener(v -> { highlightSelected(filterText); loadContent("text"); });
        filterImages.setOnClickListener(v -> { highlightSelected(filterImages); loadContent("image/"); });
        filterVideos.setOnClickListener(v -> { highlightSelected(filterVideos); loadContent("video/"); });
        filterPdfs.setOnClickListener(v -> { highlightSelected(filterPdfs); loadContent("application/pdf"); });
        filterDocs.setOnClickListener(v -> { highlightSelected(filterDocs); loadContent("application/msword"); });
    }

    private void showSettingsMenu(ImageView anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.getMenu().add(isMuted ? "Unmute 🔊" : "Mute 🔇");
        popupMenu.getMenu().add("Exit ❌");

        popupMenu.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if (title.contains("Mute")) {
                muteDevice();
                isMuted = true;
                Toast.makeText(this, "Muted 🔇", Toast.LENGTH_SHORT).show();
            } else if (title.contains("Unmute")) {
                unmuteDevice();
                isMuted = false;
                Toast.makeText(this, "Unmuted 🔊", Toast.LENGTH_SHORT).show();
            } else if (title.contains("Exit")) {
                stopMusic();
                finishAffinity(); // exit app
            }
            return true;
        });

        popupMenu.show();
    }

    // 🎵 Music Control
    private void muteDevice() {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(0f, 0f);
        }
    }

    private void unmuteDevice() {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(1f, 1f);
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        }
    }

    private void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void loadContent(String filter) {
        assignedTasksContainer.removeAllViews();
        emptyMessage.setVisibility(View.GONE);

        final boolean[] hasItems = {false};
        final int[] queriesCompleted = {0};

        // Announcements
        if (filter.equals("all") || filter.equals("text")) {
            db.collection("announcements").get().addOnSuccessListener(queryDocumentSnapshots -> {
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    hasItems[0] = true;
                    String teacherEmail = doc.getString("teacherEmail");
                    String title = doc.getString("title");
                    String content = doc.getString("content");
                    Date timestamp = doc.getDate("timestamp");
                    addAnnouncementItem(teacherEmail, title, content, timestamp);
                }
                queriesCompleted[0]++;
                checkEmptyState(hasItems[0], queriesCompleted[0]);
            });
        } else {
            queriesCompleted[0]++;
        }

        // Files
        if (!filter.equals("text")) {
            db.collection("shared_files").get().addOnSuccessListener(queryDocumentSnapshots -> {
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    String fileName = doc.getString("fileName");
                    String fileUrl = doc.getString("fileUrl");
                    Date timestamp = doc.getDate("timestamp");

                    Uri fileUri = Uri.parse(fileUrl);
                    String mimeType = getContentResolver().getType(fileUri);

                    if (filter.equals("all") || (mimeType != null && mimeType.startsWith(filter))) {
                        hasItems[0] = true;
                        addTaskItem(fileName, fileUri, timestamp);
                    }
                }
                queriesCompleted[0]++;
                checkEmptyState(hasItems[0], queriesCompleted[0]);
            });
        } else {
            queriesCompleted[0]++;
        }
    }

    private void checkEmptyState(boolean hasItems, int queriesCompleted) {
        if (queriesCompleted >= 2) {
            if (!hasItems && assignedTasksContainer.getChildCount() == 0) {
                emptyMessage.setVisibility(View.VISIBLE);
            } else {
                emptyMessage.setVisibility(View.GONE);
            }
        }
    }

    private void addAnnouncementItem(String teacherEmail, String title, String content, Date timestamp) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(32, 24, 32, 24);
        container.setBackgroundResource(R.drawable.card_announcement_bg);

        TextView titleView = new TextView(this);
        titleView.setText(title != null ? title : "No Title");
        titleView.setTextSize(17f);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setTextColor(ContextCompat.getColor(this, android.R.color.black));

        TextView teacherView = new TextView(this);
        teacherView.setText(teacherEmail != null ? teacherEmail : "Unknown Teacher");
        teacherView.setTextSize(14f);
        teacherView.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));

        TextView contentView = new TextView(this);
        contentView.setText(content != null ? content : "");
        contentView.setTextSize(15f);
        contentView.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        contentView.setMaxLines(2);
        contentView.setEllipsize(android.text.TextUtils.TruncateAt.END);

        TextView timeView = new TextView(this);
        timeView.setText(timestamp != null ? DATE_FORMAT.format(timestamp) : "No date");
        timeView.setTextSize(12f);
        timeView.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        timeView.setGravity(Gravity.END);

        container.addView(titleView);
        container.addView(teacherView);
        container.addView(contentView);
        container.addView(timeView);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 24);
        container.setLayoutParams(params);

        // ✅ Open popup on click
        container.setOnClickListener(v -> showTaskPopup(
                title != null ? title : "No Title",
                teacherEmail != null ? teacherEmail : "Unknown Teacher",
                content != null ? content : "No Content",
                timestamp != null ? DATE_FORMAT.format(timestamp) : "No date",
                null // not a file
        ));

        assignedTasksContainer.addView(container);
    }

    private void addTaskItem(String fileName, Uri fileUri, Date timestamp) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setPadding(24, 24, 24, 24);
        container.setBackgroundResource(R.drawable.card_task_bg);

        ImageView iconView = new ImageView(this);
        iconView.setImageResource(R.drawable.ic_file);
        iconView.setColorFilter(ContextCompat.getColor(this, R.color.default_icon));
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(100, 100);
        iconParams.setMargins(0, 0, 24, 0);
        iconView.setLayoutParams(iconParams);

        LinearLayout textContainer = new LinearLayout(this);
        textContainer.setOrientation(LinearLayout.VERTICAL);

        TextView titleView = new TextView(this);
        titleView.setText(fileName != null ? fileName : "Unnamed File");
        titleView.setTextSize(16f);
        titleView.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView timeView = new TextView(this);
        timeView.setText(timestamp != null ? DATE_FORMAT.format(timestamp) : "No date");
        timeView.setTextSize(12f);
        timeView.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));

        textContainer.addView(titleView);
        textContainer.addView(timeView);

        container.addView(iconView);
        container.addView(textContainer);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 24);
        container.setLayoutParams(params);

        // ✅ Popup for file with "Open File" option
        container.setOnClickListener(v -> showTaskPopup(
                fileName != null ? fileName : "Unnamed File",
                "Shared File",
                "Tap 'Open File' to view this document.",
                timestamp != null ? DATE_FORMAT.format(timestamp) : "No date",
                fileUri
        ));

        assignedTasksContainer.addView(container);
    }

    private void openFile(Uri fileUri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, getContentResolver().getType(fileUri));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No app available to open this file", Toast.LENGTH_SHORT).show();
        }
    }

    private void showTaskPopup(String title, String teacher, String content, String date, Uri fileUri) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);

        // Inflate custom layout
        View popupView = getLayoutInflater().inflate(R.layout.dialog_task_popup, null);

        TextView titleView = popupView.findViewById(R.id.popupTitle);
        TextView teacherView = popupView.findViewById(R.id.popupTeacher);
        TextView contentView = popupView.findViewById(R.id.popupContent);
        TextView dateView = popupView.findViewById(R.id.popupDate);
        Button okButton = popupView.findViewById(R.id.okButton);

        // Set values
        titleView.setText(title != null ? title : "Untitled Task");
        teacherView.setText(teacher != null ? "Teacher: " + teacher : "Teacher: Unknown");
        contentView.setText(content != null ? content : "No content available.");
        dateView.setText(date != null ? "Date: " + date : "Date: N/A");

        // Attach layout
        builder.setView(popupView);

        // Create dialog
        android.app.AlertDialog dialog = builder.create();

        // Handle button
        okButton.setText(fileUri != null ? "Open File" : "OK");
        okButton.setOnClickListener(v -> {
            if (fileUri != null) {
                openFile(fileUri);
            }
            dialog.dismiss();
        });

        dialog.show();
    }


    private void highlightSelected(TextView selected) {
        filterAll.setBackgroundResource(R.drawable.filter_tab_bg);
        filterText.setBackgroundResource(R.drawable.filter_tab_bg);
        filterImages.setBackgroundResource(R.drawable.filter_tab_bg);
        filterVideos.setBackgroundResource(R.drawable.filter_tab_bg);
        filterPdfs.setBackgroundResource(R.drawable.filter_tab_bg);
        filterDocs.setBackgroundResource(R.drawable.filter_tab_bg);

        selected.setBackgroundResource(R.drawable.filter_tab_selected_bg);
    }
}
