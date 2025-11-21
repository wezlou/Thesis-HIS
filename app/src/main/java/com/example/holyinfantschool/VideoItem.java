package com.example.holyinfantschool;

public class VideoItem {
    private final String thumbnail;
    private final String videoId;
    private final String title;

    public VideoItem(String thumbnail, String videoId, String title) {
        this.thumbnail = thumbnail == null ? "" : thumbnail;
        this.videoId = videoId == null ? "" : videoId;
        this.title = title == null ? "" : title;
    }

    public String getThumbnail() { return thumbnail; }
    public String getVideoId() { return videoId; }
    public String getTitle() { return title; }
}
