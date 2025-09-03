package com.example.holyinfantschool;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;

public class TeacherTask extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_CODE = 100;
    private static final int PICK_FILE_REQUEST_CODE = 101;

    private EditText announcementInput;
    private Button postAnnouncementBtn;
    private ImageView uploadBtn;
    private LinearLayout uploadedFilesContainer;
    private Map<String, Uri> uploadedFilesMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_task);
        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        announcementInput = findViewById(R.id.announcementInput);
        postAnnouncementBtn = findViewById(R.id.postAnnouncementBtn);
        uploadBtn = findViewById(R.id.uploadBtn);
        uploadedFilesContainer = findViewById(R.id.uploadedFilesContainer);
    }

    private void setupClickListeners() {
        uploadBtn.setOnClickListener(v -> checkStoragePermission());
        postAnnouncementBtn.setOnClickListener(v -> postAnnouncement());
    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                openFilePicker();
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, STORAGE_PERMISSION_CODE);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openFilePicker();
            } else {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE
                );
            }
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE);
    }

    private void postAnnouncement() {
        String announcement = announcementInput.getText().toString().trim();
        if (!announcement.isEmpty()) {
            // Get the currently logged in teacher's email from Firebase
            String teacherEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

            // Save announcement
            FileRepository.addAnnouncement(teacherEmail, announcement);

            if (!uploadedFilesMap.isEmpty()) {
                for (Map.Entry<String, Uri> entry : uploadedFilesMap.entrySet()) {
                    FileRepository.addSharedFile(entry.getKey(), entry.getValue());
                }
                Toast.makeText(this, "Announcement and files shared with students!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Announcement posted!", Toast.LENGTH_SHORT).show();
            }
            announcementInput.setText("");
        } else {
            Toast.makeText(this, "Please write an announcement first", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                String fileName = getFileName(fileUri);
                if (!uploadedFilesMap.containsKey(fileName)) {
                    uploadedFilesMap.put(fileName, fileUri);
                    addFileToUploadsList(fileName, fileUri);
                    Toast.makeText(this, fileName + " added", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, fileName + " already exists", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow("_display_name"));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void addFileToUploadsList(String fileName, Uri fileUri) {
        View fileItemView = LayoutInflater.from(this).inflate(R.layout.item_uploaded_file, uploadedFilesContainer, false);

        TextView fileNameView = fileItemView.findViewById(R.id.fileName);
        ImageView deleteBtn = fileItemView.findViewById(R.id.deleteBtn);

        fileNameView.setText(fileName);
        fileNameView.setOnClickListener(v -> openFile(fileUri));

        deleteBtn.setOnClickListener(v -> {
            uploadedFilesMap.remove(fileName);
            FileRepository.removeSharedFile(fileName);
            uploadedFilesContainer.removeView(fileItemView);
            Toast.makeText(this, fileName + " removed", Toast.LENGTH_SHORT).show();
        });

        uploadedFilesContainer.addView(fileItemView);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openFilePicker();
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}