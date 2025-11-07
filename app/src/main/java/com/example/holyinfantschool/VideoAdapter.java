package com.example.holyinfantschool;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VH> {
    public interface OnVideoClick {
        void onVideoClick(String videoUrl);
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
        Glide.with(ctx).load(it.getThumbnail()).centerCrop().into(holder.thumbnail);

        // ensure uniform height (just in case)
        holder.thumbnail.getLayoutParams().height = (int) (200 * ctx.getResources().getDisplayMetrics().density);
        holder.thumbnail.requestLayout();

        holder.itemView.setOnClickListener(v -> listener.onVideoClick(it.getVideoUrl()));
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        VH(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.videoThumbnail);
        }
    }
}
