package com.example.holyinfantschool;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import com.google.android.material.card.MaterialCardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        // ============================================
        // 🔥 STUDENT FILE PREVIEW (COPY OF TEACHER LOGIC)
        // ============================================

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
        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 2));
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

        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        inputParams.setMargins(8, 8, 12, 8);
        commentInput.setLayoutParams(inputParams);
        commentInputLayout.addView(commentInput);

        FrameLayout sendContainer = new FrameLayout(this);
        ImageButton sendBtn = new ImageButton(this);
        sendBtn.setImageResource(R.drawable.ic_send);
        sendBtn.setBackgroundResource(R.drawable.btn_round_send);
        sendContainer.addView(sendBtn);

        ProgressBar spinner = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
        spinner.setVisibility(View.GONE);
        sendContainer.addView(spinner);

        commentInputLayout.addView(sendContainer);
        parent.addView(commentInputLayout);

        db.collection("comments")
                .whereEqualTo("announcementId", idForComments)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    commentList.removeAllViews();

                    for (DocumentSnapshot doc : value) {
                        String user = doc.getString("user");
                        String text = doc.getString("text");

                        LinearLayout bubble = new LinearLayout(this);
                        bubble.setOrientation(LinearLayout.VERTICAL);
                        bubble.setPadding(20, 12, 20, 12);
                        bubble.setBackgroundResource(R.drawable.comment_bubble_bg);

                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        lp.setMargins(0, 6, 0, 6);
                        bubble.setLayoutParams(lp);

                        TextView userView = createTextView(user, 13f, true);
                        userView.setTextColor(ContextCompat.getColor(this, R.color.teal_700));

                        TextView textView = createTextView(text, 14f, false);

                        bubble.addView(userView);
                        bubble.addView(textView);
                        commentList.addView(bubble);
                    }
                });

        sendBtn.setOnClickListener(v -> {
            String commentText = commentInput.getText().toString().trim();
            if (commentText.isEmpty()) return;

            sendBtn.setEnabled(false);
            sendBtn.setAlpha(0.5f);
            spinner.setVisibility(View.VISIBLE);
            sendBtn.setVisibility(View.INVISIBLE);

            FirebaseUser u = auth.getCurrentUser();
            String userEmail = (u != null && u.getEmail() != null) ? u.getEmail() : "Anonymous";

            Map<String, Object> data = new HashMap<>();
            data.put("announcementId", idForComments);
            data.put("user", userEmail);
            data.put("text", commentText);
            data.put("timestamp", new Date());

            db.collection("comments").add(data)
                    .addOnSuccessListener(unused -> commentInput.setText(""))
                    .addOnCompleteListener(task -> {
                        sendBtn.setEnabled(true);
                        sendBtn.setAlpha(1f);
                        spinner.setVisibility(View.GONE);
                        sendBtn.setVisibility(View.VISIBLE);
                    });
        });
    }

    private MaterialCardView createModernCard() {
        MaterialCardView card = new MaterialCardView(this);

        card.setCardElevation(12f);
        card.setRadius(22f);
        card.setUseCompatPadding(true);

        // Dark Jungle Background
        card.setCardBackgroundColor(Color.parseColor("#0D120D"));

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

        // BRIGHT text for dark jungle cards
        tv.setTextColor(Color.parseColor("#E8FFE8"));

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
