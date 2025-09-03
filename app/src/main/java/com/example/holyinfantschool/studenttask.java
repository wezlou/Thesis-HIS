package com.example.holyinfantschool;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class studenttask extends AppCompatActivity {

    private LinearLayout assignedTasksContainer;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_studenttask);

        assignedTasksContainer = findViewById(R.id.assignedTasksContainer);
        loadContent();
    }

    private void loadContent() {
        assignedTasksContainer.removeAllViews();

        // Load announcements
        List<FileRepository.Announcement> announcements = FileRepository.getAnnouncements();
        for (FileRepository.Announcement announcement : announcements) {
            addAnnouncementItem(
                    announcement.getTeacherEmail(),
                    announcement.getAnnouncementText(),
                    announcement.getTimestamp()
            );
        }

        // Load files
        List<FileRepository.SharedFile> sharedFiles = FileRepository.getSharedFiles();
        if (announcements.isEmpty() && sharedFiles.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("No announcements or tasks yet");
            emptyView.setTextSize(16);
            emptyView.setPadding(16, 16, 16, 16);
            assignedTasksContainer.addView(emptyView);
        } else {
            for (FileRepository.SharedFile file : sharedFiles) {
                addTaskItem(
                        file.getFileName(),
                        file.getFileUri(),
                        file.getTimestamp()
                );
            }
        }
    }

    private void addAnnouncementItem(String teacherEmail, String announcementText, Date timestamp) {
        View announcementView = LayoutInflater.from(this)
                .inflate(R.layout.item_announcement, assignedTasksContainer, false);

        TextView emailView = announcementView.findViewById(R.id.teacherEmail);
        TextView textView = announcementView.findViewById(R.id.announcementText);
        TextView timeView = announcementView.findViewById(R.id.timestamp);

        emailView.setText(teacherEmail);
        textView.setText(announcementText);
        timeView.setText(DATE_FORMAT.format(timestamp));

        assignedTasksContainer.addView(announcementView);
    }

    private void addTaskItem(String fileName, Uri fileUri, Date timestamp) {
        View taskView = LayoutInflater.from(this)
                .inflate(R.layout.item_task, assignedTasksContainer, false);

        TextView taskNameView = taskView.findViewById(R.id.taskName);
        TextView timeView = taskView.findViewById(R.id.timestamp);
        ImageView iconView = taskView.findViewById(R.id.fileIcon);

        taskNameView.setText(fileName);
        timeView.setText(DATE_FORMAT.format(timestamp));

        // Set appropriate icon based on file type
        String mimeType = getMimeType(fileUri);
        if (mimeType != null) {
            if (mimeType.startsWith("image/")) {
                iconView.setImageResource(R.drawable.ic_image);
                iconView.setColorFilter(ContextCompat.getColor(this, R.color.image_icon));
            } else if (mimeType.startsWith("video/")) {
                iconView.setImageResource(R.drawable.ic_video);
                iconView.setColorFilter(ContextCompat.getColor(this, R.color.video_icon));
            } else if (mimeType.equals("application/pdf")) {
                iconView.setImageResource(R.drawable.ic_pdf);
                iconView.setColorFilter(ContextCompat.getColor(this, R.color.pdf_icon));
            } else {
                iconView.setImageResource(R.drawable.ic_file);
                iconView.setColorFilter(ContextCompat.getColor(this, R.color.default_icon));
            }
        } else {
            iconView.setImageResource(R.drawable.ic_file);
            iconView.setColorFilter(ContextCompat.getColor(this, R.color.default_icon));
        }

        taskView.setOnClickListener(v -> openFile(fileUri));
        assignedTasksContainer.addView(taskView);
    }

    private void openFile(Uri fileUri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, getMimeType(fileUri));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No app available to open this file", Toast.LENGTH_SHORT).show();
        }
    }

    private String getMimeType(Uri uri) {
        ContentResolver cR = getContentResolver();
        return cR.getType(uri);
    }
}