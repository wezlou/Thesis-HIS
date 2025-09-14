package com.example.holyinfantschool;

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FileRepository {

    private static final String TAG = "FileRepository";
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final FirebaseStorage storage = FirebaseStorage.getInstance();

    public interface AnnouncementCallback {
        void onSuccess(String announcementId);
    }

    public static void addAnnouncement(String teacherEmail, String title, String content, AnnouncementCallback callback) {
        Map<String, Object> announcement = new HashMap<>();
        announcement.put("teacherEmail", teacherEmail);
        announcement.put("title", title);
        announcement.put("content", content);
        announcement.put("timestamp", new Date());

        db.collection("announcements")
                .add(announcement)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Announcement saved with ID: " + documentReference.getId());
                    if (callback != null) callback.onSuccess(documentReference.getId());
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error saving announcement", e));
    }

    public static void addSharedFile(String announcementId, String fileName, Uri fileUri) {
        StorageReference storageRef = storage.getReference().child("shared_files/" + announcementId + "/" + fileName);
        UploadTask uploadTask = storageRef.putFile(fileUri);

        uploadTask.addOnSuccessListener(taskSnapshot ->
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            Map<String, Object> fileData = new HashMap<>();
                            fileData.put("fileName", fileName);
                            fileData.put("fileUrl", uri.toString());
                            fileData.put("timestamp", new Date());

                            db.collection("announcements")
                                    .document(announcementId)
                                    .collection("sharedFiles")
                                    .add(fileData)
                                    .addOnSuccessListener(docRef -> Log.d(TAG, "File info saved"))
                                    .addOnFailureListener(e -> Log.e(TAG, "Error saving file info", e));
                        }))
                .addOnFailureListener(e -> Log.e(TAG, "File upload failed", e));
    }
}
