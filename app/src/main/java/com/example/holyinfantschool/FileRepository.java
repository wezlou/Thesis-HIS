package com.example.holyinfantschool;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
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

    /**
     * Save an announcement to Firestore
     *
     */
    public static void addAnnouncement(String teacherEmail, String announcementText) {
        Map<String, Object> announcement = new HashMap<>();
        announcement.put("teacherEmail", teacherEmail);
        announcement.put("announcementText", announcementText);
        announcement.put("timestamp", new Date());

        db.collection("announcements")
                .add(announcement)
                .addOnSuccessListener(documentReference ->
                        Log.d(TAG, "Announcement saved with ID: " + documentReference.getId()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error saving announcement", e));
    }

    /**
     * Upload file to Firebase Storage and save reference in Firestore
     */
    public static void addSharedFile(String fileName, Uri fileUri) {
        StorageReference storageRef = storage.getReference()
                .child("shared_files/" + fileName);

        UploadTask uploadTask = storageRef.putFile(fileUri);

        uploadTask.addOnSuccessListener(taskSnapshot ->
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // Save file info in Firestore
                    Map<String, Object> fileData = new HashMap<>();
                    fileData.put("fileName", fileName);
                    fileData.put("fileUrl", uri.toString());
                    fileData.put("timestamp", new Date());

                    db.collection("shared_files")
                            .add(fileData)
                            .addOnSuccessListener(documentReference ->
                                    Log.d(TAG, "File info saved with ID: " + documentReference.getId()))
                            .addOnFailureListener(e ->
                                    Log.e(TAG, "Error saving file info", e));
                })
        ).addOnFailureListener(e ->
                Log.e(TAG, "File upload failed", e));
    }

    /**
     * Delete file from Firebase Storage and Firestore
     */
    public static void removeSharedFile(String fileName) {
        StorageReference fileRef = storage.getReference().child("shared_files/" + fileName);

        // Delete from Storage
        fileRef.delete()
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "File deleted from Storage: " + fileName))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error deleting file from Storage", e));

        // Delete metadata from Firestore
        db.collection("shared_files")
                .whereEqualTo("fileName", fileName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    queryDocumentSnapshots.getDocuments().forEach(doc ->
                            db.collection("shared_files").document(doc.getId()).delete());
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error deleting file info from Firestore", e));
    }
}
