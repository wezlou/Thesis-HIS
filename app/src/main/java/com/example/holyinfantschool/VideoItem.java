package com.example.holyinfantschool;

public class VideoItem {
    private final String thumbnail;
    private final String videoUrl;

    public VideoItem(String thumbnail, String videoUrl) {
        this.thumbnail = thumbnail;
        this.videoUrl = videoUrl;
    }

    public String getThumbnail() { return thumbnail; }
    public String getVideoUrl() { return videoUrl; }
}
