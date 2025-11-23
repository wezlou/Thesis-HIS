package com.example.holyinfantschool;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BackblazeUploader {
    private static final OkHttpClient client = new OkHttpClient();
    private static final String TAG = "BackblazeUploader";

    // Authorize account; returns JSON auth object
    private static JsonObject authorize() throws Exception {
        String credentials = BackblazeConfig.KEY_ID + ":" + BackblazeConfig.APP_KEY;
        String header = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        Request req = new Request.Builder()
                .url("https://api.backblazeb2.com/b2api/v2/b2_authorize_account")
                .header("Authorization", header)
                .build();

        try (Response res = client.newCall(req).execute()) {
            String body = res.body() != null ? res.body().string() : "{}";
            Log.i(TAG, "AUTH RESPONSE >>> " + body);
            return JsonParser.parseString(body).getAsJsonObject();
        }
    }

    // Get upload URL for bucket
    private static JsonObject getUploadUrl(String apiUrl, String authToken) throws Exception {
        JsonObject json = new JsonObject();
        json.addProperty("bucketId", BackblazeConfig.BUCKET_ID);

        RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
        Request req = new Request.Builder()
                .url(apiUrl + "/b2api/v2/b2_get_upload_url")
                .header("Authorization", authToken)
                .post(body)
                .build();

        try (Response res = client.newCall(req).execute()) {
            String resp = res.body() != null ? res.body().string() : "{}";
            Log.i(TAG, "UPLOAD URL RESPONSE >>> " + resp);
            return JsonParser.parseString(resp).getAsJsonObject();
        }
    }

    // Read InputStream fully
    private static byte[] readAllBytes(InputStream in) throws Exception {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
        return out.toByteArray();
    }

    // SHA-1 as hex (Backblaze requires content sha1 header)
    private static String sha1(byte[] d) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(d);
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    // Detect MIME (contentResolver first, fallback to extension)
    private static String detectMime(Context ctx, Uri uri) {
        try {
            String mime = ctx.getContentResolver().getType(uri);
            if (mime != null && !mime.isEmpty()) return mime;
        } catch (Exception ignored) {}

        String ext = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        if (ext != null && !ext.isEmpty()) {
            String m = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase());
            if (m != null) return m;
        }

        return "application/octet-stream";
    }

    /**
     * Uploads a file to Backblaze and returns the stored UUID filename (storedFileName).
     * Caller should persist the returned storedFileName in Firestore along with the display name.
     */
    public static String uploadFile(Context ctx, Uri fileUri) throws Exception {
        String mime = detectMime(ctx, fileUri);
        Log.i(TAG, "Detected MIME: " + mime);

        JsonObject auth = authorize();
        String apiUrl = auth.get("apiUrl").getAsString();
        String authToken = auth.get("authorizationToken").getAsString();

        JsonObject up = getUploadUrl(apiUrl, authToken);
        String uploadUrl = up.get("uploadUrl").getAsString();
        String uploadToken = up.get("authorizationToken").getAsString();

        InputStream in = ctx.getContentResolver().openInputStream(fileUri);
        if (in == null) throw new Exception("Cannot open file input stream");

        byte[] bytes = readAllBytes(in);
        String sha1 = sha1(bytes);
        String storedName = UUID.randomUUID().toString();

        RequestBody body = RequestBody.create(bytes, MediaType.parse(mime));

        Request req = new Request.Builder()
                .url(uploadUrl)
                .header("Authorization", uploadToken)
                .header("X-Bz-File-Name", storedName)
                .header("X-Bz-Content-Sha1", sha1)
                .header("Content-Type", mime)
                .post(body)
                .build();

        try (Response res = client.newCall(req).execute()) {
            String resp = res.body() != null ? res.body().string() : "{}";
            Log.i(TAG, "UPLOAD RESPONSE >>> " + resp);
            if (!res.isSuccessful()) {
                throw new Exception("Upload failed: " + res.code() + " " + resp);
            }
            return storedName;
        }
    }

    /**
     * Generate a 7-day (or configured) authorized download URL for a stored filename.
     * Returns a URL like: https://<downloadUrl>/file/<BUCKET_NAME>/<storedName>?Authorization=<token>
     */
    public static String generateDownloadUrl(String storedName) throws Exception {
        JsonObject auth = authorize();
        String apiUrl = auth.get("apiUrl").getAsString();
        String authToken = auth.get("authorizationToken").getAsString();
        String downloadUrl = auth.get("downloadUrl").getAsString();

        JsonObject json = new JsonObject();
        json.addProperty("bucketId", BackblazeConfig.BUCKET_ID);
        json.addProperty("fileNamePrefix", storedName);
        json.addProperty("validDurationInSeconds", BackblazeConfig.DOWNLOAD_AUTH_DURATION_SECONDS);

        RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));

        Request req = new Request.Builder()
                .url(apiUrl + "/b2api/v2/b2_get_download_authorization")
                .header("Authorization", authToken)
                .post(body)
                .build();

        try (Response res = client.newCall(req).execute()) {
            String resp = res.body() != null ? res.body().string() : "{}";
            Log.i(TAG, "DOWNLOAD AUTH RESPONSE >>> " + resp);
            JsonObject obj = JsonParser.parseString(resp).getAsJsonObject();
            if (!obj.has("authorizationToken")) {
                throw new Exception("Download authorization failed: " + resp);
            }
            String token = obj.get("authorizationToken").getAsString();
            return downloadUrl + "/file/" + BackblazeConfig.BUCKET_NAME + "/" + storedName + "?Authorization=" + token;
        }
    }
}
