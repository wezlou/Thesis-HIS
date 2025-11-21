package com.example.holyinfantschool;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
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
            db.collection("announcements").get().addOnSuccessListener(query -> {
                for (QueryDocumentSnapshot doc : query) {
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
        } else queriesCompleted[0]++;

        if (!filter.equals("text")) {
            db.collection("shared_files").get().addOnSuccessListener(query -> {
                for (QueryDocumentSnapshot doc : query) {
                    String fileName = doc.getString("fileName");
                    String fileUrl = doc.getString("fileUrl");
                    Date timestamp = doc.getDate("timestamp");
                    if (fileUrl == null) continue;

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
        } else queriesCompleted[0]++;
    }

    private void checkEmptyState(boolean hasItems, int queriesCompleted) {
        if (queriesCompleted >= 2)
            emptyMessage.setVisibility(hasItems ? View.GONE : View.VISIBLE);
    }

    private void addAnnouncementItem(String teacherEmail, String title, String content, Date timestamp) {
        CardView card = createModernCard();

        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(40, 30, 40, 30);

        TextView titleView = createTextView(title, 18f, true);
        titleView.setTextColor(ContextCompat.getColor(this, R.color.black));
        TextView teacherView = createTextView("By " + teacherEmail, 14f, false);
        TextView contentView = createTextView(content, 15f, false);
        TextView timeView = createTextView(DATE_FORMAT.format(timestamp), 12f, false);
        timeView.setGravity(Gravity.END);

        contentLayout.addView(titleView);
        contentLayout.addView(teacherView);
        contentLayout.addView(contentView);
        contentLayout.addView(timeView);

        addInlineCommentSection(contentLayout, title);
        card.addView(contentLayout);
        assignedTasksContainer.addView(card);
    }

    private void addTaskItem(String fileName, Uri fileUri, Date timestamp) {
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
        TextView nameView = createTextView(fileName, 16f, true);
        TextView dateView = createTextView(DATE_FORMAT.format(timestamp), 12f, false);
        textHolder.addView(nameView);
        textHolder.addView(dateView);

        header.addView(icon);
        header.addView(textHolder);
        layout.addView(header);

        Button openButton = new Button(this);
        openButton.setText("Open File");
        openButton.setBackgroundResource(R.drawable.btn_round_send);
        openButton.setOnClickListener(v -> openFile(fileUri));
        layout.addView(openButton);

        addInlineCommentSection(layout, fileName);
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

    private void addInlineCommentSection(LinearLayout parent, String taskTitle) {
        // 🔹 Divider line
        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 2));
        divider.setBackgroundColor(ContextCompat.getColor(this, R.color.light_gray));
        parent.addView(divider);

        // 🔹 Section Header
        TextView commentHeader = createTextView("💬 Comments", 15f, true);
        commentHeader.setPadding(0, 10, 0, 10);
        parent.addView(commentHeader);

        // 🔹 Comment List
        LinearLayout commentList = new LinearLayout(this);
        commentList.setOrientation(LinearLayout.VERTICAL);
        commentList.setPadding(16, 8, 16, 8);
        parent.addView(commentList);

        // 🔹 Comment Input Layout
        LinearLayout commentInputLayout = new LinearLayout(this);
        commentInputLayout.setOrientation(LinearLayout.HORIZONTAL);
        commentInputLayout.setGravity(Gravity.CENTER_VERTICAL);
        commentInputLayout.setPadding(8, 8, 8, 8);

        // 🔹 EditText Input
        EditText commentInput = new EditText(this);
        commentInput.setHint("Add a comment...");
        commentInput.setBackgroundResource(R.drawable.input_rounded);
        commentInput.setPadding(32, 20, 32, 20); // ⬅️ Added space inside the oval
        commentInput.setTextSize(14f);

        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        inputParams.setMargins(8, 8, 12, 8);
        commentInput.setLayoutParams(inputParams);
        commentInputLayout.addView(commentInput);

        // 🔹 Send Button (with round background)
        FrameLayout sendContainer = new FrameLayout(this);
        LinearLayout.LayoutParams sendParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        sendContainer.setLayoutParams(sendParams);

        ImageButton sendButton = new ImageButton(this);
        sendButton.setImageResource(R.drawable.ic_send);
        sendButton.setBackgroundResource(R.drawable.btn_round_send);
        sendButton.setContentDescription("Send comment");
        sendContainer.addView(sendButton);

        // 🔹 Loading Spinner (hidden by default)
        ProgressBar loadingSpinner = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
        loadingSpinner.setVisibility(View.GONE);
        loadingSpinner.setIndeterminate(true);
        sendContainer.addView(loadingSpinner);

        commentInputLayout.addView(sendContainer);
        parent.addView(commentInputLayout);

        // 🔹 Listen for comments in Firestore
        db.collection("comments")
                .whereEqualTo("taskTitle", taskTitle)
                .orderBy("timestamp")
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    commentList.removeAllViews();

                    for (DocumentSnapshot doc : value) {
                        String user = doc.getString("user");
                        String text = doc.getString("text");

                        // Bubble style
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

        // 🔹 Send Comment Logic
        sendButton.setOnClickListener(v -> {
            String commentText = commentInput.getText().toString().trim();
            if (commentText.isEmpty()) return;

            // Disable send + show loading
            sendButton.setEnabled(false);
            sendButton.setAlpha(0.5f);
            loadingSpinner.setVisibility(View.VISIBLE);
            sendButton.setVisibility(View.INVISIBLE);

            FirebaseUser currentUser = auth.getCurrentUser();
            String user = (currentUser != null && currentUser.getEmail() != null)
                    ? currentUser.getEmail()
                    : "Anonymous";

            HashMap<String, Object> data = new HashMap<>();
            data.put("taskTitle", taskTitle);
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
                        // Re-enable and reset
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
}
