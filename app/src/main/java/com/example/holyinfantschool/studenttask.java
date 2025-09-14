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
    private ImageView backTeacher, teacherSetting, volumeOn, volumeOff;

    private boolean settingsVisible = false;
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

        // Buttons
        backTeacher = findViewById(R.id.backteacher);
        teacherSetting = findViewById(R.id.teachersetting);
        volumeOn = findViewById(R.id.volumeOn);
        volumeOff = findViewById(R.id.volumeOff);

        hideSettingsButtons();

        // Background Music
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        // Back → Homepage
        backTeacher.setOnClickListener(v -> {
            stopMusic();
            Intent intent = new Intent(studenttask.this, Homepage.class);
            startActivity(intent);
            finish();
        });

        // Settings toggle
        teacherSetting.setOnClickListener(v -> {
            if (settingsVisible) {
                hideSettingsButtons();
            } else {
                showSettingsButtons();
            }
            settingsVisible = !settingsVisible;
        });

        // Volume mute
        volumeOn.setOnClickListener(v -> {
            mediaPlayer.setVolume(0, 0);
            isMuted = true;
            updateVolumeButtonsVisibility();
        });

        // Volume unmute
        volumeOff.setOnClickListener(v -> {
            mediaPlayer.setVolume(1.0f, 1.0f);
            isMuted = false;
            updateVolumeButtonsVisibility();
        });

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

    private void hideSettingsButtons() {
        volumeOn.setVisibility(View.GONE);
        volumeOff.setVisibility(View.GONE);
    }

    private void showSettingsButtons() {
        updateVolumeButtonsVisibility();
    }

    private void updateVolumeButtonsVisibility() {
        if (isMuted) {
            volumeOn.setVisibility(View.GONE);
            volumeOff.setVisibility(View.VISIBLE);
        } else {
            volumeOn.setVisibility(View.VISIBLE);
            volumeOff.setVisibility(View.GONE);
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

        container.setOnClickListener(v -> openFile(fileUri));

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
