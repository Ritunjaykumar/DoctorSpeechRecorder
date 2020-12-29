package com.softgyan.doctor.models;

import android.os.Parcel;
import android.os.Parcelable;

public class FeedModel {
    private final String userName;
    private final String userId;
    private final String uploadDate;
    private final String newFeed;

    public FeedModel(String userName, String userId, String uploadDate, String newFeed) {
        this.userName = userName;
        this.userId = userId;
        this.uploadDate = uploadDate;
        this.newFeed = newFeed;
    }

    protected FeedModel(Parcel in) {
        userName = in.readString();
        userId = in.readString();
        uploadDate = in.readString();
        newFeed = in.readString();
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
