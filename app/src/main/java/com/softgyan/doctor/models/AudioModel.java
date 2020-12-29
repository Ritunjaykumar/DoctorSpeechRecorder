package com.softgyan.doctor.models;

public class AudioModel {
    private final String userId;
    private final String userName;
    private final String date;
    private final String downloadUrl;
    private final String fileName;

    public AudioModel(String userId, String userName, String date, String downloadUrl, String fileName) {
        this.userId = userId;
        this.userName = userName;
        this.date = date;
        this.downloadUrl = downloadUrl;
        this.fileName = fileName;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getDate() {
        return date;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getFileName() {
        return fileName;
    }
}
