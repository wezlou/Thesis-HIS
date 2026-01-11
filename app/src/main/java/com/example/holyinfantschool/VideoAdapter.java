package com.example.holyinfantschool;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
    private final OnVideoClick ytListener;

    private static final int[] KIDS_COLORS = {
            0xFF42A5F5,
            0xFFEF5350,
            0xFF66BB6A,
            0xFFFFCA28,
            0xFFAB47BC,
            0xFFFF7043
    };

    public VideoAdapter(Context ctx, List<VideoItem> list, OnVideoClick ytListener) {
        this.ctx = ctx;
        this.list = list;
        this.ytListener = ytListener;
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

        holder.title.setText(it.getTitle());

        Drawable placeholder = createNumberPlaceholder(position + 1);

        Glide.with(ctx)
                .load(it.getThumbnail())
                .centerCrop()
                .placeholder(placeholder)
                .error(placeholder)
                .into(holder.thumbnail);

        if (it.isYouTube()) {
            holder.itemView.setOnClickListener(v ->
                    ytListener.onVideoClick(it.getVideoId())
            );
        } else {
            holder.itemView.setOnClickListener(v -> {
                Intent i = new Intent(ctx, UploadedVideoPlayerActivity.class);
                i.putExtra("videoUrl", it.getVideoUrl());
                ctx.startActivity(i);
            });
        }

        if (it.getTitle().toLowerCase().contains("premium")) {
            holder.pill.setVisibility(View.VISIBLE);
        } else {
            holder.pill.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private Drawable createNumberPlaceholder(int number) {
        int size = 600;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint bg = new Paint();
        bg.setAntiAlias(true);
        bg.setColor(KIDS_COLORS[number % KIDS_COLORS.length]);
        canvas.drawRect(0, 0, size, size, bg);

        Paint text = new Paint();
        text.setAntiAlias(true);
        text.setColor(Color.WHITE);
        text.setTextSize(220f);
        text.setTypeface(Typeface.DEFAULT_BOLD);
        text.setTextAlign(Paint.Align.CENTER);

        Paint.FontMetrics fm = text.getFontMetrics();
        float y = size / 2f - (fm.ascent + fm.descent) / 2f;

        canvas.drawText(String.valueOf(number), size / 2f, y, text);

        return new BitmapDrawable(ctx.getResources(), bitmap);
    }

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
