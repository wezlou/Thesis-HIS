package com.example.holyinfantschool;

import android.net.Uri;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileRepository {
    private static final List<SharedFile> sharedFiles = new ArrayList<>();
    private static final List<Announcement> announcements = new ArrayList<>();

    public static void addSharedFile(String fileName, Uri fileUri) {
        removeSharedFile(fileName);
        sharedFiles.add(new SharedFile(fileName, fileUri, new Date()));
    }

    public static void removeSharedFile(String fileName) {
        sharedFiles.removeIf(file -> file.getFileName().equals(fileName));
    }

    public static List<SharedFile> getSharedFiles() {
        return new ArrayList<>(sharedFiles);
    }

    public static void addAnnouncement(String teacherEmail, String announcementText) {
        announcements.add(new Announcement(teacherEmail, announcementText, new Date()));
    }

    public static List<Announcement> getAnnouncements() {
        return new ArrayList<>(announcements);
    }

    public static class SharedFile {
        private final String fileName;
        private final Uri fileUri;
        private final Date timestamp;

        public SharedFile(String fileName, Uri fileUri, Date timestamp) {
            this.fileName = fileName;
            this.fileUri = fileUri;
            this.timestamp = timestamp;
        }

        public String getFileName() {
            return fileName;
        }

        public Uri getFileUri() {
            return fileUri;
        }

        public Date getTimestamp() {
            return timestamp;
        }
    }

    public static class Announcement {
        private final String teacherEmail;
        private final String announcementText;
        private final Date timestamp;

        public Announcement(String teacherEmail, String announcementText, Date timestamp) {
            this.teacherEmail = teacherEmail;
            this.announcementText = announcementText;
            this.timestamp = timestamp;
        }

        public String getTeacherEmail() {
            return teacherEmail;
        }

        public String getAnnouncementText() {
            return announcementText;
        }

        public Date getTimestamp() {
            return timestamp;
        }
    }
}