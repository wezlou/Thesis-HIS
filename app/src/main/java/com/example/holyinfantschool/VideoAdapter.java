package com.example.holyinfantschool;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VH> {
    public interface OnVideoClick {
        void onVideoClick(String videoId);
    }

    private final Context ctx;
    private final List<VideoItem> list;
    private final OnVideoClick listener;

    public VideoAdapter(Context ctx, List<VideoItem> list, OnVideoClick listener) {
        this.ctx = ctx;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_video, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        VideoItem it = list.get(position);

        // load thumbnail safely
        Glide.with(ctx)
                .load(it.getThumbnail())
                .centerCrop()
                .placeholder(android.R.color.darker_gray) // safe placeholder
                .into(holder.thumbnail);

        holder.title.setText(it.getTitle());

        // Optional: show premium pill if title contains "premium"
        if (it.getTitle().toLowerCase().contains("premium")) {
            holder.pill.setVisibility(View.VISIBLE);
        } else {
            holder.pill.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onVideoClick(it.getVideoId()));
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView title;
        TextView pill;

        VH(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.videoThumbnail);
            title = itemView.findViewById(R.id.videoTitle);
            pill = itemView.findViewById(R.id.premiumBadge);
        }
    }
}
