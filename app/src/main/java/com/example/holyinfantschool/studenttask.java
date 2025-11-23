package com.example.holyinfantschool;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
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

        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

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
        final int[] queriesCompleted = {0};

        if (filter.equals("all") || filter.equals("text")) {
            db.collection("announcements").orderBy("timestamp", Query.Direction.DESCENDING)
                    .get().addOnSuccessListener(query -> {
                        for (QueryDocumentSnapshot doc : query) {
                            hasItems[0] = true;
                            String teacherEmail = doc.getString("teacherEmail");
                            String title = doc.getString("title");
                            String content = doc.getString("content");
                            Date timestamp = doc.getDate("timestamp");
                            String announcementId = doc.getId();
                            addAnnouncementItem(announcementId, teacherEmail, title, content, timestamp);
                        }
                        queriesCompleted[0]++;
                        checkEmptyState(hasItems[0], queriesCompleted[0]);
                    }).addOnFailureListener(e -> {
                        queriesCompleted[0]++;
                        checkEmptyState(hasItems[0], queriesCompleted[0]);
                    });
        } else {
            queriesCompleted[0]++;
        }

        if (!filter.equals("text")) {
            db.collectionGroup("sharedFiles")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(query -> {
                        for (QueryDocumentSnapshot doc : query) {
                            String displayName = doc.getString("fileName");
                            String storedName = doc.getString("storedFileName");
                            Date timestamp = doc.getDate("timestamp");
                            String parentAnnouncementId = doc.getReference().getParent().getParent().getId();
                            if (displayName == null || storedName == null) continue;

                            String ext = getExtension(displayName);
                            if (matchesFilter(ext, filter)) {
                                hasItems[0] = true;
                                addTaskItem(displayName, storedName, timestamp, parentAnnouncementId);
                            }
                        }
                        queriesCompleted[0]++;
                        checkEmptyState(hasItems[0], queriesCompleted[0]);
                    })
                    .addOnFailureListener(e -> {
                        queriesCompleted[0]++;
                        checkEmptyState(hasItems[0], queriesCompleted[0]);
                    });
        } else {
            queriesCompleted[0]++;
        }
    }

    private void checkEmptyState(boolean hasItems, int queriesCompleted) {
        if (queriesCompleted >= 2)
            emptyMessage.setVisibility(hasItems ? View.GONE : View.VISIBLE);
    }

    private void addAnnouncementItem(String announcementId, String teacherEmail, String title, String content, Date timestamp) {
        CardView card = createModernCard();

        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(40, 30, 40, 30);

        TextView titleView = createTextView(title, 18f, true);
        titleView.setTextColor(ContextCompat.getColor(this, R.color.black));
        TextView teacherView = createTextView("By " + teacherEmail, 14f, false);
        TextView contentView = createTextView(content, 15f, false);
        TextView timeView = createTextView(timestamp != null ? DATE_FORMAT.format(timestamp) : "", 12f, false);
        timeView.setGravity(Gravity.END);

        contentLayout.addView(titleView);
        contentLayout.addView(teacherView);
        contentLayout.addView(contentView);
        contentLayout.addView(timeView);

        addInlineCommentSection(contentLayout, announcementId);

        card.addView(contentLayout);
        assignedTasksContainer.addView(card);
    }

    private void addTaskItem(String displayName, String storedName, Date timestamp, String parentAnnouncementId) {
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

        LinearLayout textHolder = new LinearLayout(this);
        textHolder.setOrientation(LinearLayout.VERTICAL);
        TextView nameView = createTextView(displayName, 16f, true);
        TextView dateView = createTextView(timestamp != null ? DATE_FORMAT.format(timestamp) : "", 12f, false);
        textHolder.addView(nameView);
        textHolder.addView(dateView);

        header.addView(icon);
        header.addView(textHolder);
        layout.addView(header);

        Button openButton = new Button(this);
        openButton.setText("Open File");
        openButton.setBackgroundResource(R.drawable.btn_round_send);
        openButton.setOnClickListener(v -> {
            ProgressBar pb = new ProgressBar(studenttask.this, null, android.R.attr.progressBarStyleSmall);
            pb.setIndeterminate(true);
            layout.addView(pb);

            new Thread(() -> {
                try {
                    String authorizedUrl = BackblazeUploader.generateDownloadUrl(storedName);

                    runOnUiThread(() -> {
                        layout.removeView(pb);

                        Intent p = new Intent(studenttask.this, PreviewActivity.class);
                        p.putExtra(PreviewActivity.EXTRA_STORED_NAME, storedName);
                        p.putExtra(PreviewActivity.EXTRA_DISPLAY_NAME, displayName);
                        startActivity(p);
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        layout.removeView(pb);
                        Toast.makeText(studenttask.this, "Failed to open file", Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        });

        layout.addView(openButton);

        String fileCommentKey = "file:" + storedName;
        addInlineCommentSectionForId(layout, fileCommentKey);

        card.addView(layout);
        assignedTasksContainer.addView(card);
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
        addInlineCommentSectionForId(parent, announcementId);
    }

    private void addInlineCommentSectionForId(LinearLayout parent, String idForComments) {
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
            data.put("announcementId", idForComments);
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

    private String getExtension(String name) {
        if (name == null) return "";
        int idx = name.lastIndexOf('.');
        if (idx == -1) return "";
        return name.substring(idx + 1).toLowerCase(Locale.ROOT);
    }

    private boolean matchesFilter(String ext, String filter) {
        if (filter.equals("all")) return true;
        if (filter.equals("text")) return false;
        if (filter.equals("image")) {
            return ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png") || ext.equals("gif") || ext.equals("webp");
        }
        if (filter.equals("video")) {
            return ext.equals("mp4") || ext.equals("mov") || ext.equals("mkv") || ext.equals("webm") || ext.equals("3gp");
        }
        if (filter.equals("pdf")) return ext.equals("pdf");
        if (filter.equals("doc")) return ext.equals("doc") || ext.equals("docx") || ext.equals("txt") || ext.equals("odt");
        return false;
    }
}
