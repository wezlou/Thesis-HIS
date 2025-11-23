package com.example.holyinfantschool;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import okhttp3.*;

public class UploadCareUploader {

    private static final String TAG = "UploadCareUploader";

    public static String upload(Context ctx, Uri fileUri) throws Exception {

        String publicKey = UploadcareConfig.PUBLIC_KEY;

        OkHttpClient client = new OkHttpClient();

        // Get original filename
        String fileName = getFileName(ctx, fileUri);
        if (fileName == null) {
            fileName = "file_" + System.currentTimeMillis();
        }

        // Read bytes safely
        InputStream inputStream = ctx.getContentResolver().openInputStream(fileUri);
        byte[] fileBytes = readAllBytesCompat(inputStream);

        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("UPLOADCARE_PUB_KEY", publicKey)
                .addFormDataPart("UPLOADCARE_STORE", "auto")
                .addFormDataPart("file", fileName,
                        RequestBody.create(fileBytes, MediaType.parse("application/octet-stream")))
                .build();

        Request request = new Request.Builder()
                .url("https://upload.uploadcare.com/base/")
                .post(requestBody)
                .build();

        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new Exception("Uploadcare error: " + response.code() + " → " + response.message());
        }

        String res = response.body().string();
        Log.i(TAG, "Uploadcare response: " + res);

        String uuid = parseUuid(res);
        if (uuid == null) throw new Exception("Failed to parse UUID");

        // THIS IS THE FIX!!!
        return "https://ucarecdn.com/" + uuid + "/" + fileName;
    }

    private static String getFileName(Context ctx, Uri uri) {
        Cursor cursor = ctx.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            try {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (cursor.moveToFirst()) {
                    return cursor.getString(nameIndex);
                }
            } finally {
                cursor.close();
            }
        }
        return null;
    }

    private static byte[] readAllBytesCompat(InputStream input) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] temp = new byte[4096];
        int n;
        while ((n = input.read(temp)) != -1) buffer.write(temp, 0, n);
        return buffer.toByteArray();
    }

    private static String parseUuid(String json) {
        json = json.replace("{", "").replace("}", "").replace("\"", "");
        String[] parts = json.split(":");
        return parts.length > 1 ? parts[1] : null;
    }
}
