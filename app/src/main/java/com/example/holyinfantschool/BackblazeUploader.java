package com.example.holyinfantschool;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

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
    private static final long MAX_DURATION = 604800L; // 7 days in seconds

    private static JsonObject authorize() throws Exception {
        String credentials = BackblazeConfig.KEY_ID + ":" + BackblazeConfig.APP_KEY;
        String header = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        Request req = new Request.Builder()
                .url("https://api.backblazeb2.com/b2api/v2/b2_authorize_account")
                .header("Authorization", header)
                .build();

        Response res = client.newCall(req).execute();
        String body = res.body() != null ? res.body().string() : "{}";
        Log.i(TAG, "AUTH RESPONSE >>> " + body);
        return JsonParser.parseString(body).getAsJsonObject();
    }

    private static JsonObject getUploadUrl(String apiUrl, String authToken) throws Exception {
        JsonObject payload = new JsonObject();
        payload.addProperty("bucketId", BackblazeConfig.BUCKET_ID);

        RequestBody body = RequestBody.create(payload.toString(), MediaType.parse("application/json"));

        Request req = new Request.Builder()
                .url(apiUrl + "/b2api/v2/b2_get_upload_url")
                .header("Authorization", authToken)
                .post(body)
                .build();

        Response res = client.newCall(req).execute();
        String resp = res.body() != null ? res.body().string() : "{}";
        Log.i(TAG, "UPLOAD URL RESPONSE >>> " + resp);
        return JsonParser.parseString(resp).getAsJsonObject();
    }

    private static byte[] readAllBytes(InputStream in) throws Exception {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int n;
        while ((n = in.read(data)) != -1) buffer.write(data, 0, n);
        return buffer.toByteArray();
    }

    private static String sha1(byte[] d) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(d);
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    /**
     * Upload file -> returns storedFileName (UUID)
     * Blocking: call from a background thread.
     */
    public static String uploadFile(Context ctx, Uri fileUri) throws Exception {
        JsonObject auth = authorize();
        String apiUrl = auth.get("apiUrl").getAsString();
        String authToken = auth.get("authorizationToken").getAsString();

        JsonObject up = getUploadUrl(apiUrl, authToken);
        String uploadUrl = up.get("uploadUrl").getAsString();
        String uploadToken = up.get("authorizationToken").getAsString();

        InputStream in = ctx.getContentResolver().openInputStream(fileUri);
        if (in == null) throw new Exception("Cannot open file stream");

        byte[] bytes = readAllBytes(in);
        String sha1 = sha1(bytes);
        String fileName = UUID.randomUUID().toString();

        RequestBody fileBody = RequestBody.create(bytes, MediaType.parse("application/octet-stream"));

        Request uploadReq = new Request.Builder()
                .url(uploadUrl)
                .header("Authorization", uploadToken)
                .header("X-Bz-File-Name", fileName)
                .header("X-Bz-Content-Sha1", sha1)
                .header("Content-Type", "application/octet-stream")
                .post(fileBody)
                .build();

        Response response = client.newCall(uploadReq).execute();
        String uploadedResp = response.body() != null ? response.body().string() : "{}";
        Log.i(TAG, "UPLOAD RESPONSE >>> " + uploadedResp);

        // return stored file name (UUID) — we will store this in Firestore
        return fileName;
    }

    /**
     * Generate a 7-day valid download URL for a stored file.
     * Blocking: call from background thread.
     */
    public static String generateDownloadUrl(String storedFileName) throws Exception {
        JsonObject auth = authorize();
        String apiUrl = auth.get("apiUrl").getAsString();
        String authToken = auth.get("authorizationToken").getAsString();
        String downloadUrl = auth.get("downloadUrl").getAsString();

        JsonObject payload = new JsonObject();
        payload.addProperty("bucketId", BackblazeConfig.BUCKET_ID);
        payload.addProperty("fileNamePrefix", storedFileName);
        payload.addProperty("validDurationInSeconds", MAX_DURATION);

        RequestBody body = RequestBody.create(payload.toString(), MediaType.parse("application/json"));

        Request req = new Request.Builder()
                .url(apiUrl + "/b2api/v2/b2_get_download_authorization")
                .header("Authorization", authToken)
                .post(body)
                .build();

        Response res = client.newCall(req).execute();
        String resp = res.body() != null ? res.body().string() : "{}";
        Log.i(TAG, "DOWNLOAD AUTH RESPONSE >>> " + resp);

        JsonObject obj = JsonParser.parseString(resp).getAsJsonObject();
        if (!obj.has("authorizationToken")) {
            throw new Exception("Failed to get download authorization: " + resp);
        }

        String downloadAuth = obj.get("authorizationToken").getAsString();
        // authorized URL that includes token as query param
        return downloadUrl + "/file/" + BackblazeConfig.BUCKET_NAME + "/" + storedFileName + "?Authorization=" + downloadAuth;
    }
}
