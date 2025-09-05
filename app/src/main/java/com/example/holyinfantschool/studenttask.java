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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class studenttask extends AppCompatActivity {

    private LinearLayout assignedTasksContainer;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_studenttask);

        assignedTasksContainer = findViewById(R.id.assignedTasksContainer);
        db = FirebaseFirestore.getInstance();

        loadContent();
    }

    private void loadContent() {
        assignedTasksContainer.removeAllViews();

        // 🔹 Load announcements from Firestore
        db.collection("announcements")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String teacherEmail = doc.getString("teacherEmail");
                        String announcementText = doc.getString("announcementText");
                        Date timestamp = doc.getDate("timestamp");

                        addAnnouncementItem(teacherEmail, announcementText, timestamp);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load announcements", Toast.LENGTH_SHORT).show();
                });

        // 🔹 Load shared files from Firestore
        db.collection("shared_files")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        TextView emptyView = new TextView(this);
                        emptyView.setText("No announcements or tasks yet");
                        emptyView.setTextSize(16);
                        emptyView.setPadding(16, 16, 16, 16);
                        assignedTasksContainer.addView(emptyView);
                    } else {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            String fileName = doc.getString("fileName");
                            String fileUrl = doc.getString("fileUrl");
                            Date timestamp = doc.getDate("timestamp");

                            addTaskItem(fileName, Uri.parse(fileUrl), timestamp);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load tasks", Toast.LENGTH_SHORT).show();
                });
    }

    private void addAnnouncementItem(String teacherEmail, String announcementText, Date timestamp) {
        View announcementView = LayoutInflater.from(this)
                .inflate(R.layout.item_announcement, assignedTasksContainer, false);

        TextView emailView = announcementView.findViewById(R.id.teacherEmail);
        TextView textView = announcementView.findViewById(R.id.announcementText);
        TextView timeView = announcementView.findViewById(R.id.timestamp);

        emailView.setText(teacherEmail);
        textView.setText(announcementText);
        timeView.setText(timestamp != null ? DATE_FORMAT.format(timestamp) : "No date");

        assignedTasksContainer.addView(announcementView);
    }

    private void addTaskItem(String fileName, Uri fileUri, Date timestamp) {
        View taskView = LayoutInflater.from(this)
                .inflate(R.layout.item_task, assignedTasksContainer, false);

        TextView taskNameView = taskView.findViewById(R.id.taskName);
        TextView timeView = taskView.findViewById(R.id.timestamp);
        ImageView iconView = taskView.findViewById(R.id.fileIcon);

        taskNameView.setText(fileName);
        timeView.setText(timestamp != null ? DATE_FORMAT.format(timestamp) : "No date");

        // Set icon based on file type
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
