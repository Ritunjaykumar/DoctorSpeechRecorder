package com.softgyan.doctor.models;

import android.os.Parcel;
import android.os.Parcelable;

public class FeedModel {
    private final String userName;
    private final String userId;
    private final String uploadDate;
    private final String newFeed;
    private final String documentId;

    public FeedModel(String userName, String userId, String uploadDate, String newFeed, String documentId) {
        this.userName = userName;
        this.userId = userId;
        this.uploadDate = uploadDate;
        this.newFeed = newFeed;
        this.documentId = documentId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserId() {
        return userId;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public String getNewFeed() {
        return newFeed;
    }
}
