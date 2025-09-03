package com.example.holyinfantschool;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class stdentmail extends AppCompatActivity {
    private FirebaseFirestore db;
    private TextView notificationCount;
    private ImageView mailIcon;
    private boolean isNotificationVisible = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stdentmail);

        db = FirebaseFirestore.getInstance();
        notificationCount = findViewById(R.id.notificationCount);
        mailIcon = findViewById(R.id.Mail);

        mailIcon.setOnClickListener(v -> toggleNotification());
        checkUnreadAnnouncements();
    }

    private void toggleNotification() {
        if (isNotificationVisible) {
            // Hide announcement
            findViewById(R.id.announcementText).setVisibility(View.GONE);
            findViewById(R.id.teacherText).setVisibility(View.GONE);
            isNotificationVisible = false;
        } else {
            // Show announcement
            loadLatestAnnouncement();
            isNotificationVisible = true;
        }
    }

    private void checkUnreadAnnouncements() {
        db.collection("tasks")
                .whereEqualTo("type", "announcement")
                .whereEqualTo("isRead", false)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int unreadCount = task.getResult().size();
                        if (unreadCount > 0) {
                            notificationCount.setText(String.valueOf(unreadCount));
                            notificationCount.setVisibility(View.VISIBLE);
                        } else {
                            notificationCount.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void loadLatestAnnouncement() {
        db.collection("tasks")
                .whereEqualTo("type", "announcement")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String message = document.getString("content");
                        String teacherEmail = document.getString("teacherEmail");

                        TextView announcementText = findViewById(R.id.announcementText);
                        TextView teacherText = findViewById(R.id.teacherText);

                        announcementText.setText(message);
                        teacherText.setText("From: " + teacherEmail);

                        announcementText.setVisibility(View.VISIBLE);
                        teacherText.setVisibility(View.VISIBLE);

                        // Mark as read
                        document.getReference().update("isRead", true);
                        notificationCount.setVisibility(View.GONE);
                    }
                });
    }
}