package com.softgyan.doctor.util;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class UploadAudioFile {
    private final Context mContext;
    private final StorageReference mStorageRef;
    private final OnUploadAudioListener audioListener;

    public UploadAudioFile(Context context, OnUploadAudioListener audioListener) {
        this.mContext = context;
        this.audioListener = audioListener;
        mStorageRef = FirebaseStorage.getInstance().getReference();
    }

    public void uploadAudioFile(Uri audioUri) {
        String path = UserInfo.getInstance(mContext).getUserId() + System.currentTimeMillis() + ".mp3";
        StorageReference riversRef = mStorageRef.child("audio/" + path);
        riversRef.putFile(audioUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful()) ;
                    Uri downloadUri = uriTask.getResult();
                    String downloadUrl = String.valueOf(downloadUri);
                    audioListener.onUploadSuccess(downloadUrl, path);
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        audioListener.onUploadFailed(e);
                    }
                })
                .addOnProgressListener(snapshot -> audioListener.onUploadProgress(snapshot));
    }


    public interface OnUploadAudioListener {
        void onUploadFailed(Exception ex);

        void onUploadSuccess(String downloadUrl, String path);

        void onUploadProgress(UploadTask.TaskSnapshot snapshot);
    }

}
