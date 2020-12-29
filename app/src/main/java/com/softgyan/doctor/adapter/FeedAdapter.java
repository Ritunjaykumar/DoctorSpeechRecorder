package com.softgyan.doctor.adapter;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.softgyan.doctor.R;
import com.softgyan.doctor.models.FeedModel;
import com.softgyan.doctor.widget.FeedDetailsActivity;

import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder> {

    List<FeedModel> feedModelList;

    public FeedAdapter(List<FeedModel> feedModelList) {
        this.feedModelList = feedModelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //todo for image set using glide
        holder.userName.setText(feedModelList.get(position).getUserName());
        holder.uploadDate.setText(feedModelList.get(position).getUploadDate());
        holder.feedContent.setText(feedModelList.get(position).getNewFeed());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFeedDetailsActivity(holder, position);
            }
        });

    }

    private void openFeedDetailsActivity(ViewHolder holder, int position) {
        try {
            Intent feedIntent = new Intent(holder.itemView.getContext(), FeedDetailsActivity.class);
            FeedModel fm = feedModelList.get(position);
            Bundle bundle = new Bundle();
            bundle.putString("feed", fm.getNewFeed());
            bundle.putString("date", fm.getUploadDate());
            bundle.putString("user_name", fm.getUserName());
            bundle.putBoolean("file", false);
            feedIntent.putExtra("feed_details",bundle);
            holder.itemView.getContext().startActivity(feedIntent);
        } catch (Exception ex) {
            Toast.makeText(holder.itemView.getContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return feedModelList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView userName;
        private final TextView uploadDate;
        private final TextView feedContent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.tv_user_name);
            uploadDate = itemView.findViewById(R.id.tv_upload_date);
            feedContent = itemView.findViewById(R.id.et_feed_container);
        }
    }
}
