// Full file — matches the TeacherTask you already had but wires in advanced preview + BackblazeUploader
package com.example.holyinfantschool;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class TeacherTask extends AppCompatActivity {
    private LinearLayout announcementListContainer;
    private ScrollView announcementScroll, formSection;
    private ImageView backBtn, createAnnouncementBtn;
    private EditText announcementTitleInput, announcementContentInput;
    private LinearLayout uploadedFilesContainer;
    private Button uploadBtn, postAnnouncementBtn;
    private TextView emptyMessage;
    private static final int PICK_FILE_REQUEST_CODE = 101;
    private static final int STORAGE_PERMISSION_CODE = 100;
    private final Map<String, Uri> uploadedFilesMap = new LinkedHashMap<>();
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private boolean isEditing = false;
    private String editingAnnouncementId = null;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_teacher_task);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        // --- initialize views (same ids as earlier) ---
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

        createAnnouncementBtn.setOnClickListener(v -> showCreateForm(false, null, null, null));
        backBtn.setOnClickListener(v -> {
            if (formSection.getVisibility() == View.VISIBLE) {
                formSection.setVisibility(View.GONE);
                announcementScroll.setVisibility(View.VISIBLE);
                isEditing = false; editingAnnouncementId = null;
            } else {
                startActivity(new Intent(this, TeacherSite.class));
                finish();
            }
        });
        uploadBtn.setOnClickListener(v -> checkStoragePermission());
        postAnnouncementBtn.setOnClickListener(v -> postAnnouncement());
        loadAnnouncements();
    }

    private void showCreateForm(boolean editing, String id, String title, String content) {
        isEditing = editing; editingAnnouncementId = id;
        announcementScroll.setVisibility(View.GONE);
        formSection.setVisibility(View.VISIBLE);
        if (editing) {
            announcementTitleInput.setText(title != null ? title : "");
            announcementContentInput.setText(content != null ? content : "");
            postAnnouncementBtn.setText("Update Announcement");
        } else {
            announcementTitleInput.setText(""); announcementContentInput.setText("");
            uploadedFilesMap.clear(); uploadedFilesContainer.removeAllViews();
            postAnnouncementBtn.setText("Post Announcement");
        }
    }

    private void postAnnouncement() {
        String title = announcementTitleInput.getText().toString().trim();
        String content = announcementContentInput.getText().toString().trim();
        if (title.isEmpty()) { Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show(); return; }
        FirebaseUser user = auth.getCurrentUser();
        String teacherEmail = user != null && user.getEmail() != null ? user.getEmail() : "unknown";

        Map<String,Object> data = new HashMap<>();
        data.put("title", title);
        data.put("content", content);
        data.put("teacherEmail", teacherEmail);
        data.put("timestamp", new Date());

        if (isEditing && editingAnnouncementId != null) {
            final String annId = editingAnnouncementId;
            db.collection("announcements").document(annId).update(data)
                    .addOnSuccessListener(a -> {
                        Toast.makeText(this,"Announcement updated",Toast.LENGTH_SHORT).show();
                        isEditing=false; editingAnnouncementId=null;
                        formSection.setVisibility(View.GONE); announcementScroll.setVisibility(View.VISIBLE);
                        if (!uploadedFilesMap.isEmpty()) uploadFilesForAnnouncement(annId);
                    }).addOnFailureListener(e -> Toast.makeText(this,"Failed to update",Toast.LENGTH_SHORT).show());
        } else {
            db.collection("announcements").add(data)
                    .addOnSuccessListener(docRef -> {
                        String announcementId = docRef.getId();
                        Toast.makeText(this,"Announcement posted!",Toast.LENGTH_SHORT).show();
                        uploadedFilesContainer.removeAllViews(); announcementTitleInput.setText(""); announcementContentInput.setText("");
                        formSection.setVisibility(View.GONE); announcementScroll.setVisibility(View.VISIBLE);
                        if (!uploadedFilesMap.isEmpty()) uploadFilesForAnnouncement(announcementId);
                    }).addOnFailureListener(e -> Toast.makeText(this,"Failed to post announcement",Toast.LENGTH_SHORT).show());
        }
    }

    private void uploadFilesForAnnouncement(String announcementId) {
        for (Map.Entry<String, Uri> e : uploadedFilesMap.entrySet()) {
            final String displayName = e.getKey();
            final Uri fileUri = e.getValue();
            new Thread(() -> {
                try {
                    String stored = BackblazeUploader.uploadFile(this, fileUri);
                    Map<String,Object> fileData = new HashMap<>();
                    fileData.put("fileName", displayName);
                    fileData.put("storedFileName", stored);
                    fileData.put("timestamp", new Date());
                    db.collection("announcements").document(announcementId).collection("sharedFiles").add(fileData);
                    runOnUiThread(() -> Toast.makeText(TeacherTask.this, "Uploaded: " + displayName, Toast.LENGTH_SHORT).show());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(TeacherTask.this, "Upload failed: " + displayName + " — " + ex.getMessage(), Toast.LENGTH_LONG).show());
                }
            }).start();
        }
        uploadedFilesMap.clear(); runOnUiThread(() -> uploadedFilesContainer.removeAllViews());
    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) openFilePicker();
            else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, STORAGE_PERMISSION_CODE);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED)
                openFilePicker();
            else ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},STORAGE_PERMISSION_CODE);
        }
    }

    private void openFilePicker() { Intent i = new Intent(Intent.ACTION_GET_CONTENT); i.setType("*/*"); startActivityForResult(i, PICK_FILE_REQUEST_CODE); }

    @Override protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            String fname = getFileName(fileUri);
            if (fname == null) fname = "file_" + System.currentTimeMillis();
            String displayName = fname; int i=1;
            while (uploadedFilesMap.containsKey(displayName)) { displayName = fname + " (" + i + ")"; i++; }
            uploadedFilesMap.put(displayName, fileUri);
            addFileToUploadsList(displayName, fileUri);
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        try (android.database.Cursor c = getContentResolver().query(uri, null, null, null, null)) {
            if (c != null && c.moveToFirst()) { int idx = c.getColumnIndexOrThrow("_display_name"); result = c.getString(idx); }
        } catch (Exception ignored) {}
        if (result == null && uri != null) result = uri.getLastPathSegment();
        return result;
    }

    private void addFileToUploadsList(String fileName, Uri uri) {
        View item = LayoutInflater.from(this).inflate(R.layout.item_uploaded_file, uploadedFilesContainer, false);
        TextView nameView = item.findViewById(R.id.fileName);
        ImageView deleteBtn = item.findViewById(R.id.deleteBtn);
        nameView.setText(fileName);
        deleteBtn.setOnClickListener(v -> { uploadedFilesMap.remove(fileName); uploadedFilesContainer.removeView(item); });
        uploadedFilesContainer.addView(item);
    }

    private void loadAnnouncements() {
        announcementListContainer.removeAllViews(); emptyMessage.setVisibility(View.GONE);
        FirebaseUser currentUser = auth.getCurrentUser();
        String teacherEmail = (currentUser != null && currentUser.getEmail() != null) ? currentUser.getEmail() : "";

        db.collection("announcements").whereEqualTo("teacherEmail", teacherEmail).orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) { Toast.makeText(this, "Failed to load announcements", Toast.LENGTH_SHORT).show(); return; }
                    announcementListContainer.removeAllViews();
                    if (value == null || value.isEmpty()) { emptyMessage.setVisibility(View.VISIBLE); return; }
                    emptyMessage.setVisibility(View.GONE);
                    for (DocumentSnapshot doc : value) {
                        String title = doc.getString("title");
                        String teacher = doc.getString("teacherEmail");
                        String content = doc.getString("content");
                        Date ts = doc.getDate("timestamp");
                        String announcementId = doc.getId();
                        View card = buildAnnouncementCard(announcementId, title, teacher, content, ts);
                        announcementListContainer.addView(card);
                    }
                });
    }

    private View buildAnnouncementCard(String announcementId, String title, String teacherEmail, String content, Date timestamp) {
        CardView card = new CardView(this); card.setCardElevation(10f); card.setRadius(24f); card.setUseCompatPadding(true);
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white));
        LinearLayout contentLayout = new LinearLayout(this); contentLayout.setOrientation(LinearLayout.VERTICAL); contentLayout.setPadding(40,30,40,30);

        LinearLayout topRow = new LinearLayout(this); topRow.setOrientation(LinearLayout.HORIZONTAL); topRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView titleView = createTextView(title,18f,true); titleView.setTextColor(ContextCompat.getColor(this, R.color.black));
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f); titleView.setLayoutParams(titleParams);

        ImageButton editBtn = new ImageButton(this); editBtn.setImageResource(R.drawable.ic_edit); editBtn.setBackground(null);
        ImageButton deleteBtn = new ImageButton(this); deleteBtn.setImageResource(R.drawable.ic_delete); deleteBtn.setBackground(null);

        topRow.addView(titleView); topRow.addView(editBtn); topRow.addView(deleteBtn);
        contentLayout.addView(topRow);

        TextView teacherView = createTextView("By " + teacherEmail, 14f, false);
        TextView contentView = createTextView(content, 15f, false);
        TextView timeView = createTextView(DATE_FORMAT.format(timestamp!=null?timestamp:new Date()),12f,false); timeView.setGravity(Gravity.END);
        contentLayout.addView(teacherView); contentLayout.addView(contentView); contentLayout.addView(timeView);

        LinearLayout filesContainer = new LinearLayout(this); filesContainer.setOrientation(LinearLayout.VERTICAL); filesContainer.setPadding(0,12,0,12);
        contentLayout.addView(filesContainer);

        db.collection("announcements").document(announcementId).collection("sharedFiles").orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((filesSnap, err)-> {
                    if (err!=null || filesSnap==null) return;
                    filesContainer.removeAllViews();
                    for (DocumentSnapshot f : filesSnap) {
                        String fname = f.getString("fileName");
                        String stored = f.getString("storedFileName");
                        if (fname==null || stored==null) continue;
                        TextView fileView = createTextView(fname,14f,false);
                        fileView.setOnClickListener(v -> {
                            new Thread(() -> {
                                try {
                                    String url = BackblazeUploader.generateDownloadUrl(stored);
                                    Intent i = new Intent(Intent.ACTION_VIEW);
                                    i.setData(Uri.parse(url));
                                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(i);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    runOnUiThread(() -> Toast.makeText(TeacherTask.this, "Cannot open file: " + ex.getMessage(), Toast.LENGTH_SHORT).show());
                                }
                            }).start();
                        });
                        filesContainer.addView(fileView);
                    }
                });

        addInlineCommentSection(contentLayout, announcementId);

        editBtn.setOnClickListener(v -> showCreateForm(true, announcementId, title, content));
        deleteBtn.setOnClickListener(v -> new AlertDialog.Builder(this).setTitle("Delete announcement").setMessage("Are you sure you want to delete this announcement?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("announcements").document(announcementId).delete().addOnSuccessListener(aVoid->{
                        db.collection("comments").whereEqualTo("announcementId", announcementId).get().addOnSuccessListener(q->{
                            for (DocumentSnapshot c : q) db.collection("comments").document(c.getId()).delete();
                        });
                        db.collection("announcements").document(announcementId).collection("sharedFiles").get().addOnSuccessListener(q->{
                            for (DocumentSnapshot f : q) db.collection("announcements").document(announcementId).collection("sharedFiles").document(f.getId()).delete();
                        });
                        Toast.makeText(this,"Deleted",Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e->Toast.makeText(this,"Failed to delete",Toast.LENGTH_SHORT).show());
                }).setNegativeButton("Cancel",null).show());

        card.addView(contentLayout); return card;
    }

    private TextView createTextView(String text, float size, boolean bold) {
        TextView tv = new TextView(this); tv.setText(text!=null?text:""); tv.setTextSize(size); if (bold) tv.setTypeface(null, android.graphics.Typeface.BOLD);
        tv.setTextColor(ContextCompat.getColor(this, R.color.black)); return tv;
    }

    private void addInlineCommentSection(LinearLayout parent, String announcementId) {
        View divider = new View(this); divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,2)); divider.setBackgroundColor(ContextCompat.getColor(this,R.color.light_gray)); parent.addView(divider);
        TextView commentHeader = createTextView("💬 Comments",15f,true); commentHeader.setPadding(0,10,0,10); parent.addView(commentHeader);
        LinearLayout commentList = new LinearLayout(this); commentList.setOrientation(LinearLayout.VERTICAL); commentList.setPadding(16,8,16,8); parent.addView(commentList);
        LinearLayout commentInputLayout = new LinearLayout(this); commentInputLayout.setOrientation(LinearLayout.HORIZONTAL); commentInputLayout.setGravity(Gravity.CENTER_VERTICAL); commentInputLayout.setPadding(8,8,8,8);
        EditText commentInput = new EditText(this); commentInput.setHint("Add a comment..."); commentInput.setBackgroundResource(R.drawable.input_rounded); commentInput.setPadding(32,20,32,20); commentInput.setTextSize(14f);
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1f); inputParams.setMargins(8,8,12,8); commentInput.setLayoutParams(inputParams); commentInputLayout.addView(commentInput);
        FrameLayout sendContainer = new FrameLayout(this); LinearLayout.LayoutParams sendParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT); sendContainer.setLayoutParams(sendParams);
        ImageButton sendButton = new ImageButton(this); sendButton.setImageResource(R.drawable.ic_send); sendButton.setBackgroundResource(R.drawable.btn_round_send); sendButton.setContentDescription("Send comment"); sendContainer.addView(sendButton);
        ProgressBar loadingSpinner = new ProgressBar(this,null,android.R.attr.progressBarStyleSmall); loadingSpinner.setVisibility(View.GONE); loadingSpinner.setIndeterminate(true); sendContainer.addView(loadingSpinner);
        commentInputLayout.addView(sendContainer); parent.addView(commentInputLayout);

        db.collection("comments").whereEqualTo("announcementId", announcementId).orderBy("timestamp", Query.Direction.ASCENDING).addSnapshotListener((value,error)->{
            if (error!=null || value==null) return;
            commentList.removeAllViews();
            for (DocumentSnapshot doc : value) {
                String user = doc.getString("user"); String text = doc.getString("text");
                LinearLayout bubble = new LinearLayout(this); bubble.setOrientation(LinearLayout.VERTICAL); bubble.setBackgroundResource(R.drawable.comment_bubble_bg); bubble.setPadding(20,12,20,12);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT); lp.setMargins(0,6,0,6); bubble.setLayoutParams(lp);
                TextView userView = createTextView(user,13f,true); userView.setTextColor(ContextCompat.getColor(this,R.color.teal_700));
                TextView textView = createTextView(text,14f,false); textView.setTextColor(ContextCompat.getColor(this,R.color.black));
                bubble.addView(userView); bubble.addView(textView); commentList.addView(bubble);
            }
        });

        sendButton.setOnClickListener(v-> {
            String commentText = commentInput.getText().toString().trim(); if (commentText.isEmpty()) return;
            sendButton.setEnabled(false); sendButton.setAlpha(0.5f); loadingSpinner.setVisibility(View.VISIBLE); sendButton.setVisibility(View.INVISIBLE);
            FirebaseUser cu = auth.getCurrentUser(); String user = (cu!=null && cu.getEmail()!=null)? cu.getEmail() : "Anonymous";
            Map<String,Object> data = new HashMap<>(); data.put("announcementId", announcementId); data.put("user", user); data.put("text", commentText); data.put("timestamp", new Date());
            db.collection("comments").add(data).addOnSuccessListener(unused-> { commentInput.setText(""); Toast.makeText(this,"Comment sent 💬",Toast.LENGTH_SHORT).show(); })
                    .addOnFailureListener(e-> Toast.makeText(this,"Failed to send comment ❌",Toast.LENGTH_SHORT).show())
                    .addOnCompleteListener(task -> { sendButton.setEnabled(true); sendButton.setAlpha(1f); loadingSpinner.setVisibility(View.GONE); sendButton.setVisibility(View.VISIBLE); });
        });
    }
}
