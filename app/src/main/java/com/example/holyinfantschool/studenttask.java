package com.example.holyinfantschool;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.card.MaterialCardView;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class studenttask extends AppCompatActivity {

    private LinearLayout assignedTasksContainer;
    private TextView filterAll, filterText, filterImages, filterVideos, filterPdfs, filterDocs;
    private TextView emptyMessage;
    private ImageView backButton, settingsButton;
    private boolean isMuted = false;
    private MediaPlayer mediaPlayer;
    private Map<String, Integer> lastCommentCounts = new HashMap<>();


    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String openId = getIntent().getStringExtra("open_announcement");
        if (openId != null) {
            scrollToAnnouncement(openId);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_studenttask);

        assignedTasksContainer = findViewById(R.id.assignedTasksContainer);
        emptyMessage = findViewById(R.id.emptyMessage);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        backButton = findViewById(R.id.backbtn);
        settingsButton = findViewById(R.id.settingsButton);

        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            }
        } catch (Exception ignored) {}

        backButton.setOnClickListener(v -> {
            stopMusic();
            finish();
        });

        settingsButton.setOnClickListener(v -> showSettingsMenu(settingsButton));

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
        filterImages.setOnClickListener(v -> { highlightSelected(filterImages); loadContent("image"); });
        filterVideos.setOnClickListener(v -> { highlightSelected(filterVideos); loadContent("video"); });
        filterPdfs.setOnClickListener(v -> { highlightSelected(filterPdfs); loadContent("pdf"); });
        filterDocs.setOnClickListener(v -> { highlightSelected(filterDocs); loadContent("doc"); });
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
                finishAffinity();
            }
            return true;
        });
        popupMenu.show();
    }

    private void muteDevice() { if (mediaPlayer != null) mediaPlayer.setVolume(0f, 0f); }
    private void unmuteDevice() { if (mediaPlayer != null) { mediaPlayer.setVolume(1f, 1f); if (!mediaPlayer.isPlaying()) mediaPlayer.start(); } }
    private void stopMusic() { if (mediaPlayer != null) { mediaPlayer.stop(); mediaPlayer.release(); mediaPlayer = null; } }

    private void loadContent(String filter) {
        assignedTasksContainer.removeAllViews();
        emptyMessage.setVisibility(View.GONE);

        final boolean[] hasItems = {false};
        final int[] pendingAnnouncements = {0};

        db.collection("announcements")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {

                    if (query.isEmpty()) {
                        emptyMessage.setVisibility(View.VISIBLE);
                        return;
                    }

                    pendingAnnouncements[0] = query.size();

                    for (DocumentSnapshot doc : query) {

                        String announcementId = doc.getId();
                        String teacherEmail = doc.getString("teacherEmail");
                        String title = doc.getString("title");
                        String content = doc.getString("content");
                        Date timestamp = doc.getDate("timestamp");

                        final boolean filterIsAll = filter.equals("all");
                        final boolean filterIsText = filter.equals("text");

                        // announcement matches immediately if ALL or TEXT
                        final boolean[] announcementMatches = { filterIsAll || filterIsText };

                        db.collection("announcements")
                                .document(announcementId)
                                .collection("sharedFiles")
                                .get()
                                .addOnSuccessListener(files -> {

                                    if (!filterIsAll && !filterIsText) {
                                        // Check files only if filter is not text/all
                                        for (DocumentSnapshot f : files) {
                                            String fname = f.getString("fileName");
                                            if (fname == null) continue;

                                            String ext = getExtension(fname);

                                            if (matchesFilter(ext, filter)) {
                                                announcementMatches[0] = true;
                                                break;
                                            }
                                        }
                                    }

                                    if (announcementMatches[0]) {
                                        hasItems[0] = true;
                                        addAnnouncementItem(announcementId, teacherEmail, title, content, timestamp);
                                    }

                                })
                                .addOnCompleteListener(x -> {
                                    pendingAnnouncements[0]--;
                                    if (pendingAnnouncements[0] == 0) {
                                        if (!hasItems[0]) emptyMessage.setVisibility(View.VISIBLE);
                                    }
                                });
                    }

                });

        // Load files for non-text filters
        if (!filter.equals("text")) {
            db.collectionGroup("sharedFiles")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(query -> {
                        for (DocumentSnapshot doc : query) {
                            String name = doc.getString("fileName");
                            String url = doc.getString("fileUrl");
                            Date ts = doc.getDate("timestamp");

                            if (name == null || url == null) continue;

                            String ext = getExtension(name);

                            if (matchesFilter(ext, filter)) {
                                hasItems[0] = true;
                                addTaskItem(name, url, ts);
                            }
                        }
                    });
        }
    }

    private void checkEmptyState(boolean hasItems, int queriesCompleted) {
        if (queriesCompleted >= 2)
            emptyMessage.setVisibility(hasItems ? View.GONE : View.VISIBLE);
    }

    private void addAnnouncementItem(String announcementId, String teacherEmail, String title, String content, Date timestamp) {
        CardView card = createModernCard();
        card.setTag(announcementId);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 30, 40, 30);

        TextView t1 = createTextView(title, 18f, true);
        TextView t2 = createTextView("By " + teacherEmail, 14f, false);
        TextView t3 = createTextView(content, 15f, false);
        TextView t4 = createTextView(DATE_FORMAT.format(timestamp), 12f, false);
        t4.setGravity(Gravity.END);

        layout.addView(t1);
        layout.addView(t2);
        layout.addView(t3);
        layout.addView(t4);

        LinearLayout filesContainer = new LinearLayout(this);
        filesContainer.setOrientation(LinearLayout.VERTICAL);
        filesContainer.setPadding(0, 12, 0, 12);
        layout.addView(filesContainer);

        db.collection("announcements")
                .document(announcementId)
                .collection("sharedFiles")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(files -> {
                    for (DocumentSnapshot f : files) {
                        String fname = f.getString("fileName");
                        String furl = f.getString("fileUrl");

                        if (fname == null || furl == null) continue;

                        LinearLayout fileRow = new LinearLayout(this);
                        fileRow.setOrientation(LinearLayout.HORIZONTAL);
                        fileRow.setGravity(Gravity.CENTER_VERTICAL);
                        fileRow.setPadding(20, 16, 20, 16);
                        fileRow.setBackgroundResource(R.drawable.gc_file_bg);

                        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        rp.setMargins(0, 8, 0, 8);
                        fileRow.setLayoutParams(rp);

                        ImageView icon = new ImageView(this);
                        icon.setImageResource(R.drawable.ic_file);
                        icon.setColorFilter(ContextCompat.getColor(this, R.color.default_icon));
                        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(70, 70);
                        ip.setMargins(0, 0, 18, 0);
                        icon.setLayoutParams(ip);
                        fileRow.addView(icon);

                        TextView fName = createTextView(fname, 15f, false);
                        fileRow.addView(fName);

                        fileRow.setOnClickListener(v -> {
                            Intent p = new Intent(studenttask.this, PreviewActivity.class);
                            p.putExtra(PreviewActivity.EXTRA_URL, furl);
                            p.putExtra(PreviewActivity.EXTRA_DISPLAY_NAME, fname);
                            startActivity(p);
                        });

                        filesContainer.addView(fileRow);
                    }
                });

        // ---- Comments ----
        addInlineCommentSection(layout, announcementId);

        card.addView(layout);
        assignedTasksContainer.addView(card);
    }

    private void showLocalNotification(String title, String message, String announcementId) {
        String CHANNEL_ID = "local_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Local Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, studenttask.class);
        intent.putExtra("announcementId", announcementId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission if missing
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    101
            );
            return;
        }

        NotificationManagerCompat.from(this).notify((int)System.currentTimeMillis(), builder.build());
    }


    private void addTaskItem(String displayName, String fileUrl, Date timestamp) {
        CardView card = createModernCard();

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 30, 40, 30);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_file);
        icon.setColorFilter(ContextCompat.getColor(this, R.color.default_icon));
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(90, 90);
        iconParams.setMargins(0, 0, 24, 0);
        icon.setLayoutParams(iconParams);
        header.addView(icon);

        LinearLayout textHolder = new LinearLayout(this);
        textHolder.setOrientation(LinearLayout.VERTICAL);

        TextView nameView = createTextView(displayName, 16f, true);
        TextView dateView = createTextView(DATE_FORMAT.format(timestamp), 12f, false);
        textHolder.addView(nameView);
        textHolder.addView(dateView);

        header.addView(textHolder);
        layout.addView(header);

        Button openBtn = new Button(this);
        openBtn.setText("Open File");
        openBtn.setBackgroundResource(R.drawable.btn_round_send);

        openBtn.setOnClickListener(v -> {
            Intent i = new Intent(studenttask.this, PreviewActivity.class);
            i.putExtra(PreviewActivity.EXTRA_URL, fileUrl);
            i.putExtra(PreviewActivity.EXTRA_DISPLAY_NAME, displayName);
            startActivity(i);
        });

        layout.addView(openBtn);

        addInlineCommentSectionForId(layout, "file:" + fileUrl);

        card.addView(layout);
        assignedTasksContainer.addView(card);
    }

    private void addInlineCommentSection(LinearLayout parent, String id) {
        addInlineCommentSectionForId(parent, id);
    }

    private void addInlineCommentSectionForId(LinearLayout parent, String idForComments) {

        // Divider
        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 2));
        divider.setBackgroundColor(Color.parseColor("#D0D0D0"));
        parent.addView(divider);

        // Header
        TextView commentHeader = createTextView("💬 Comments", 15f, true);
        commentHeader.setPadding(0, 10, 0, 10);
        parent.addView(commentHeader);

        // Main container
        LinearLayout commentSection = new LinearLayout(this);
        commentSection.setOrientation(LinearLayout.VERTICAL);
        commentSection.setPadding(16, 8, 16, 8);
        parent.addView(commentSection);

        // MAIN COMMENT INPUT
        addMainCommentInput_student(parent, idForComments);

        // Listen to Firestore
        db.collection("comments")
                .whereEqualTo("announcementId", idForComments)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {

                    if (error != null || value == null) return;

                    // =====================================================
                    // ⚡ AUTO-DETECT NEW COMMENTS OR NEW REPLIES
                    // =====================================================
                    int currentCount = value.size();
                    int previousCount = lastCommentCounts.getOrDefault(idForComments, 0);

                    if (previousCount != 0 && currentCount > previousCount) {

                        // Get latest comment
                        DocumentSnapshot latest = value.getDocuments()
                                .get(value.size() - 1);

                        String user = latest.getString("user");
                        String text = latest.getString("text");
                        String parentId = latest.getString("parentId");

                        if (parentId == null) {
                            // MAIN COMMENT
                            showLocalNotification(
                                    "New Comment",
                                    user + ": " + text,
                                    idForComments
                            );
                        } else {
                            // REPLY
                            showLocalNotification(
                                    "New Reply",
                                    user + " replied: " + text,
                                    idForComments
                            );
                        }
                    }

                    // Save new count
                    lastCommentCounts.put(idForComments, currentCount);

                    // =====================================================
                    // 🔥 NORMAL RENDERING OF COMMENTS BELOW
                    // =====================================================
                    commentSection.removeAllViews();
                    List<DocumentSnapshot> all = value.getDocuments();

                    // MAIN COMMENTS
                    List<DocumentSnapshot> mainComments = new ArrayList<>();
                    for (DocumentSnapshot d : all) {
                        String parentId = d.getString("parentId");
                        if (parentId == null) mainComments.add(d);
                    }

                    for (DocumentSnapshot main : mainComments) {

                        // MAIN COMMENT BUBBLE
                        LinearLayout mainBubble = buildCommentBubble_student(
                                main.getString("user"),
                                main.getString("text"),
                                false
                        );
                        commentSection.addView(mainBubble);

                        // REPLY BUTTON
                        TextView replyBtn = new TextView(this);
                        replyBtn.setText("Reply");
                        replyBtn.setTextColor(Color.parseColor("#29C36A"));
                        replyBtn.setPadding(16, 4, 0, 12);
                        replyBtn.setTextSize(13f);
                        commentSection.addView(replyBtn);

                        // REPLY INPUT AREA
                        LinearLayout replyInput = buildReplyInput_student(main.getId(), idForComments);
                        replyInput.setVisibility(View.GONE);
                        commentSection.addView(replyInput);

                        replyBtn.setOnClickListener(v -> {
                            replyInput.setVisibility(
                                    replyInput.getVisibility() == View.GONE ? View.VISIBLE : View.GONE
                            );
                        });

                        // REPLIES
                        for (DocumentSnapshot replyDoc : all) {
                            String parentId = replyDoc.getString("parentId");

                            if (parentId != null && parentId.equals(main.getId())) {
                                LinearLayout replyBubble = buildCommentBubble_student(
                                        replyDoc.getString("user"),
                                        replyDoc.getString("text"),
                                        true
                                );
                                commentSection.addView(replyBubble);
                            }
                        }
                    }
                });
    }

    private LinearLayout buildCommentBubble_student(String user, String text, boolean isReply) {

        LinearLayout bubble = new LinearLayout(this);
        bubble.setOrientation(LinearLayout.VERTICAL);

        if (isReply) {
            bubble.setBackgroundResource(R.drawable.comment_reply_bubble);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(dpToPx(40), dpToPx(4), dpToPx(4), dpToPx(4));
            bubble.setLayoutParams(params);

        } else {
            bubble.setBackgroundResource(R.drawable.comment_bubble_bg);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
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

    private LinearLayout buildReplyInput_student(String parentCommentId, String announcementId) {

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
            data.put("parentId", parentCommentId);   // reply
            data.put("user", userEmail);
            data.put("text", replyText);
            data.put("timestamp", new Date());

            db.collection("comments").add(data);
            showLocalNotification(
                    "New Reply",
                    userEmail + " replied: " + replyText,
                    announcementId
            );

            input.setText("");
        });

        layout.addView(input);
        layout.addView(send);

        return layout;
    }

    private void addMainCommentInput_student(LinearLayout parent, String announcementId) {

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
            String userEmail = (u != null && u.getEmail() != null) ? u.getEmail() : "Anonymous";

            Map<String, Object> data = new HashMap<>();
            data.put("announcementId", announcementId);
            data.put("parentId", null);
            data.put("user", userEmail);
            data.put("text", text);
            data.put("timestamp", new Date());

            db.collection("comments").add(data);
            showLocalNotification(
                    "New Comment",
                    userEmail + ": " + text,
                    announcementId
            );

            input.setText("");
        });

        layout.addView(input);
        layout.addView(send);

        parent.addView(layout);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
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
        card.setStrokeColor(Color.parseColor("#1F3926"));

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

    private String getExtension(String name) {
        if (name == null) return "";
        int idx = name.lastIndexOf('.');
        if (idx == -1) return "";
        return name.substring(idx + 1).toLowerCase(Locale.ROOT);
    }

    private boolean matchesFilter(String ext, String filter) {
        if (filter.equals("all")) return true;
        if (filter.equals("text")) return false;

        if (filter.equals("image"))
            return ext.matches("jpg|jpeg|png|gif|webp");

        if (filter.equals("video"))
            return ext.matches("mp4|mov|mkv|webm|3gp");

        if (filter.equals("pdf"))
            return ext.equals("pdf");

        if (filter.equals("doc"))
            return ext.matches("doc|docx|txt|odt");

        return false;
    }

    private void scrollToAnnouncement(String announcementId) {
        assignedTasksContainer.post(() -> {
            for (int i = 0; i < assignedTasksContainer.getChildCount(); i++) {
                View card = assignedTasksContainer.getChildAt(i);
                if (card.getTag() != null && card.getTag().equals(announcementId)) {
                    card.requestFocus();
                    assignedTasksContainer.scrollTo(0, card.getTop());
                    break;
                }
            }
        });
    }


    private void highlightSelected(TextView s) {
        filterAll.setBackgroundResource(R.drawable.filter_tab_bg);
        filterText.setBackgroundResource(R.drawable.filter_tab_bg);
        filterImages.setBackgroundResource(R.drawable.filter_tab_bg);
        filterVideos.setBackgroundResource(R.drawable.filter_tab_bg);
        filterPdfs.setBackgroundResource(R.drawable.filter_tab_bg);
        filterDocs.setBackgroundResource(R.drawable.filter_tab_bg);

        s.setBackgroundResource(R.drawable.filter_tab_selected_bg);
    }
}
