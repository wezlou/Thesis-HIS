package com.example.holyinfantschool;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class UploadCareUploader {

    private static final String TAG = "UploadCareUploader";

    // FINAL: Use your project CDN domain (Option B)
    private static final String CDN_BASE = "https://azgb2gxzjh.ucarecd.net/";

    // Correct Uploadcare endpoint (Signed Uploads OFF)
    private static final String UPLOAD_ENDPOINT = "https://upload.uploadcare.com/base/";

    private static final int TIMEOUT = 300;

    public static String upload(Context ctx, Uri fileUri) throws Exception {

        if (ctx == null || fileUri == null)
            throw new IllegalArgumentException("Context or fileUri is null");

        String publicKey = UploadcareConfig.PUBLIC_KEY;

        if (publicKey == null || publicKey.isEmpty())
            throw new IllegalStateException("UploadcareConfig.PUBLIC_KEY is missing!");

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                .build();

        // Get filename
        String fileName = getFileName(ctx, fileUri);
        if (fileName == null)
            fileName = "file_" + System.currentTimeMillis();

        Log.d(TAG, "Uploading: " + fileName);

        // MIME type
        String mime = ctx.getContentResolver().getType(fileUri);
        if (mime == null) mime = "application/octet-stream";

        // Open stream (USED FOR STREAMING)
        InputStream in = ctx.getContentResolver().openInputStream(fileUri);
        if (in == null)
            throw new Exception("Cannot open stream: " + fileUri);

        // ✅ ONLY CHANGE: stream instead of byte[]
        RequestBody fileBody =
                new StreamRequestBody(in, MediaType.parse(mime));

        // Prepare multipart
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("UPLOADCARE_PUB_KEY", publicKey)
                .addFormDataPart("UPLOADCARE_STORE", "auto")
                .addFormDataPart("file", fileName, fileBody)
                .build();

        Request request = new Request.Builder()
                .url(UPLOAD_ENDPOINT)
                .post(body)
                .build();

        Log.d(TAG, "Sending to: " + UPLOAD_ENDPOINT);

        String json;

        try (Response response = client.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                String err = response.body() != null ? response.body().string() : "no body";
                throw new Exception("Uploadcare upload failed: "
                        + response.code() + " — " + err);
            }

            json = response.body() != null ? response.body().string() : null;
        }

        Log.d(TAG, "Upload response: " + json);

        // Extract UUID
        String uuid = parseUuid(json);
        if (uuid == null)
            throw new Exception("Failed to parse UUID");

        Log.d(TAG, "UUID: " + uuid);

        // Determine extension
        String ext = getExt(fileName).toLowerCase();

        // Images → force JPG via Uploadcare transform
        if (isImage(ext)) {
            String cdn = CDN_BASE + uuid + "/-/format/jpg/";
            Log.d(TAG, "CDN IMAGE URL: " + cdn);
            return cdn;
        }

        // Other files (including VIDEO) → append original filename
        String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                .replace("+", "%20");

        String cdn = CDN_BASE + uuid + "/" + encodedName;
        Log.d(TAG, "CDN FILE URL: " + cdn);

        return cdn;
    }

    // ================= HELPERS =================

    private static String getFileName(Context ctx, Uri uri) {
        try {
            Cursor c = ctx.getContentResolver().query(uri, null, null, null, null);
            if (c != null) {
                int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (c.moveToFirst()) {
                    String name = c.getString(idx);
                    c.close();
                    return name;
                }
                c.close();
            }
        } catch (Exception ignored) { }
        return uri.getLastPathSegment();
    }

    private static String parseUuid(String json) {
        if (json == null) return null;
        try {
            int f = json.indexOf("\"file\"");
            int colon = json.indexOf(":", f);
            int q1 = json.indexOf("\"", colon + 1);
            int q2 = json.indexOf("\"", q1 + 1);
            return json.substring(q1 + 1, q2);
        } catch (Exception e) {
            Log.e(TAG, "UUID parse error", e);
            return null;
        }
    }

    private static String getExt(String name) {
        int i = name.lastIndexOf(".");
        return (i == -1) ? "" : name.substring(i + 1);
    }

    private static boolean isImage(String ext) {
        return ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png")
                || ext.equals("gif") || ext.equals("webp") || ext.equals("bmp")
                || ext.equals("heic");
    }

    // ================= STREAM BODY (VIDEO FIX) =================

    private static class StreamRequestBody extends RequestBody {

        private final InputStream inputStream;
        private final MediaType contentType;

        StreamRequestBody(InputStream inputStream, MediaType contentType) {
            this.inputStream = inputStream;
            this.contentType = contentType;
        }

        @Override
        public MediaType contentType() {
            return contentType;
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            byte[] buffer = new byte[8192];
            int read;
            try {
                while ((read = inputStream.read(buffer)) != -1) {
                    sink.write(buffer, 0, read);
                }
            } finally {
                inputStream.close();
            }
        }
    }
}
