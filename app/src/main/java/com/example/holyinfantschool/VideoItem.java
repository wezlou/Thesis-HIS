package com.example.holyinfantschool;

public class VideoItem {
    private final String thumbnail;
    private final String videoId;

    public VideoItem(String thumbnail, String videoId) {
        this.thumbnail = thumbnail;
        this.videoId = videoId;
    }

    public String getThumbnail() { return thumbnail; }
    public String getVideoId() { return videoId; }
}
