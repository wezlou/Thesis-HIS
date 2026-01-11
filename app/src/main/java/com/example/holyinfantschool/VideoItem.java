package com.example.holyinfantschool;

public class VideoItem {

    public static final int TYPE_YOUTUBE = 0;
    public static final int TYPE_UPLOADED = 1;

    private final int type;
    private final String title;
    private final String thumbnail;
    private final String videoId;
    private final String videoUrl;

    public VideoItem(
            int type,
            String title,
            String thumbnail,
            String videoId,
            String videoUrl
    ) {
        this.type = type;
        this.title = title == null ? "" : title;
        this.thumbnail = thumbnail == null ? "" : thumbnail;
        this.videoId = videoId == null ? "" : videoId;
        this.videoUrl = videoUrl == null ? "" : videoUrl;
    }

    public int getType() { return type; }
    public String getTitle() { return title; }
    public String getThumbnail() { return thumbnail; }
    public String getVideoId() { return videoId; }
    public String getVideoUrl() { return videoUrl; }

    public boolean isYouTube() { return type == TYPE_YOUTUBE; }
    public boolean isUploaded() { return type == TYPE_UPLOADED; }
}
