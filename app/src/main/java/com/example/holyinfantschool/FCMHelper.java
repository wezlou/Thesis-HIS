package com.example.holyinfantschool;

import org.json.JSONObject;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class FCMHelper {

    // ⭐ IMPORTANT: Replace with your Firebase Server Key
    private static final String SERVER_KEY = "BHYWJ56iM-jvTavHmx9ILRCnXausLAjDY59hzw43uWbPjAvfDNCGbjpcmVuVgM77h7102uKNc1pzNaFkixlQjZ8";

    public static void sendToTopic(String topic, String title, String body) {
        new Thread(() -> {
            try {
                URL url = new URL("https://fcm.googleapis.com/fcm/send");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setUseCaches(false);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "key=" + SERVER_KEY);

                JSONObject json = new JSONObject();
                json.put("to", "/topics/" + topic);

                JSONObject notify = new JSONObject();
                notify.put("title", title);
                notify.put("body", body);
                notify.put("sound", "default");

                json.put("notification", notify);

                JSONObject data = new JSONObject();
                data.put("type", "announcement");
                data.put("title", title);
                data.put("body", body);
                json.put("data", data);

                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write(json.toString());
                writer.flush();
                writer.close();

                conn.getInputStream();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void sendToToken(String token, String title, String body, String type, String announcementId) {
        new Thread(() -> {
            try {
                URL url = new URL("https://fcm.googleapis.com/fcm/send");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setUseCaches(false);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "key=" + SERVER_KEY);

                JSONObject json = new JSONObject();
                json.put("to", token);

                JSONObject notify = new JSONObject();
                notify.put("title", title);
                notify.put("body", body);
                notify.put("sound", "default");

                json.put("notification", notify);

                JSONObject data = new JSONObject();
                data.put("type", type);
                data.put("announcementId", announcementId);
                json.put("data", data);

                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write(json.toString());
                writer.flush();
                writer.close();

                conn.getInputStream();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
