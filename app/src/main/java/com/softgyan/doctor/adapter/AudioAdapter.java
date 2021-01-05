package com.softgyan.doctor.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.UploadTask;
import com.softgyan.doctor.R;
import com.softgyan.doctor.util.NetworkManagerCustom;
import com.softgyan.doctor.util.TimeFormat;
import com.softgyan.doctor.util.UploadAudioFile;
import com.softgyan.doctor.util.UserInfo;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.ViewHolder> {

    private final File[] audioFiles;
    private final OnItemClickListener itemClickListener;
    private View previousView = null;
    private final Context mContext;
    private final ProgressDialog progressDialog;

    public AudioAdapter(Context context, File[] audioFiles, OnItemClickListener itemClickListener) {
        this.mContext = context;
        this.audioFiles = audioFiles;
        this.itemClickListener = itemClickListener;
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.audio_file_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.fileName.setText(audioFiles[position].getName());
        holder.date.setText(TimeFormat.getTimeAgo(audioFiles[position].lastModified()));
        holder.ibUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkManagerCustom.isOnline(mContext)) {
                    Toast.makeText(mContext, audioFiles[position].getName(), Toast.LENGTH_SHORT).show();
                    uploadFile(audioFiles[position], audioFiles[position].getName());
                }else {
                    Toast.makeText(mContext, "No Internet Connection", Toast.LENGTH_SHORT).show();
                }
            }
        });
        holder.itemView.setOnClickListener(v -> {
            if (previousView == null) {
                previousView = holder.itemView;
                holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.secondary_text));
                itemClickListener.onClick(audioFiles[position]);
            } else if (previousView.equals(holder.itemView)) {
                previousView = holder.itemView;
            } else {
                previousView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.accent));
                holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.secondary_text));
                itemClickListener.onClick(audioFiles[position]);
                previousView = holder.itemView;
            }
        });
    }

    private void uploadFile(File audioFile, String fileName) {
        Uri fileUri = Uri.fromFile(audioFile);
        progressDialog.setTitle("Uploading.. " + audioFile.getName());
        progressDialog.setProgress(0);
        if (fileUri != null) {
            progressDialog.show();
            UploadAudioFile uploadAudioFile = new UploadAudioFile(mContext, new UploadAudioFile.OnUploadAudioListener() {
                @Override
                public void onUploadFailed(Exception ex) {
                    Toast.makeText(mContext, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }

                @Override
                public void onUploadSuccess(String downloadUrl, String path) {
                    HashMap<String, Object> uploadFile = new HashMap<>();
                    uploadFile.put("download_url", downloadUrl);
                    uploadFile.put("user_id", UserInfo.getInstance(mContext).getUserId());
                    uploadFile.put("date", getCurrentDate());
                    uploadFile.put("file_name", path);
                    uploadFile.put("user_name", UserInfo.getInstance(mContext).getUserName());
                    FirebaseFirestore.getInstance().collection("Media")
                            .add(uploadFile)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(mContext, "file uploaded successful", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(mContext, "Uploading Failed", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            });
                }

                @Override
                public void onUploadProgress(UploadTask.TaskSnapshot snapshot) {
                    int currentProgress = (int) (100 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                    progressDialog.setProgress(currentProgress);
                }
            });
            uploadAudioFile.uploadAudioFile(fileUri,fileName);
        }

    }

    private String getCurrentDate() {
        Date currentDate = new Date();
        String dateToStr = DateFormat.getInstance().format(currentDate);
        return dateToStr;
    }

    @Override
    public int getItemCount() {
        return audioFiles.length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView fileName;
        private final TextView date;
        private final ImageButton ibUpload;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.file_name);
            date = itemView.findViewById(R.id.date);
            ibUpload = itemView.findViewById(R.id.ib_upload);
        }
    }

    public interface OnItemClickListener {
        void onClick(File file);
    }
}
