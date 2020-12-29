package com.softgyan.doctor.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.softgyan.doctor.R;
import com.softgyan.doctor.models.AudioModel;
import com.softgyan.doctor.util.NetworkManagerCustom;

import java.util.List;

public class DownloadAudioAdapter extends RecyclerView.Adapter<DownloadAudioAdapter.ViewHolder> {
    private List<AudioModel> audioModels;
    private final Context mContext;
    private final OnDownloadListener downloadListener;

    public DownloadAudioAdapter(List<AudioModel> audioModels, Context mContext,
                                OnDownloadListener downloadListener) {
        this.downloadListener = downloadListener;
        this.audioModels = audioModels;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.layout_audio_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AudioModel audioModel = audioModels.get(position);
        holder.tvUserName.setText(audioModel.getUserName());
        holder.tvFileName.setText(audioModel.getFileName());
        holder.tvDate.setText(audioModel.getDate());
        holder.ibDownload.setOnClickListener(view->{
            if(NetworkManagerCustom.isOnline(mContext)) {
                downloadListener.onDownload(audioModel.getDownloadUrl(), audioModel.getFileName());
            }else {
                Toast.makeText(mContext, "No internet Connection", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return audioModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView tvUserName;
        private final TextView tvFileName;
        private final TextView tvDate;
        private final ImageButton ibDownload;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvFileName = itemView.findViewById(R.id.tv_file_name);
            tvDate = itemView.findViewById(R.id.tv_upload_date);
            ibDownload = itemView.findViewById(R.id.ib_download);
        }
    }

    public interface OnDownloadListener{
        void onDownload(String downloadUrl, String fileName);
    }
}
