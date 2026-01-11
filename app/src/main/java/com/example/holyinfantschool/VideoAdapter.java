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

    // 🎨 Kid-friendly colors
    private static final int[] KIDS_COLORS = {
            0xFF42A5F5, // Blue
            0xFFEF5350, // Red
            0xFF66BB6A, // Green
            0xFFFFCA28, // Yellow
            0xFFAB47BC, // Purple
            0xFFFF7043  // Orange
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

        // ===============================
        // YOUTUBE VIDEO
        // ===============================
        if (it.isYouTube()) {

            Glide.with(ctx)
                    .load(it.getThumbnail())
                    .centerCrop()
                    .placeholder(createNumberPlaceholder(position + 1))
                    .error(createNumberPlaceholder(position + 1))
                    .into(holder.thumbnail);

            holder.itemView.setOnClickListener(v ->
                    ytListener.onVideoClick(it.getVideoId())
            );

        }
        // ===============================
        // TEACHER UPLOADED VIDEO
        // ===============================
        else {

            Drawable placeholder = createNumberPlaceholder(position + 1);

            Glide.with(ctx)
                    .load("") // no remote thumbnail
                    .centerCrop()
                    .placeholder(placeholder)
                    .error(placeholder)
                    .into(holder.thumbnail);

            holder.itemView.setOnClickListener(v -> {
                Intent i = new Intent(ctx, UploadedVideoPlayerActivity.class);
                i.putExtra("videoUrl", it.getVideoUrl());
                ctx.startActivity(i);
            });
        }

        // Optional badge logic
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

    // =====================================================
    // 🎨 CREATE COLORFUL NUMBER PLACEHOLDER
    // =====================================================
    private Drawable createNumberPlaceholder(int number) {

        int size = 600;

        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Background color (cycle)
        Paint bgPaint = new Paint();
        bgPaint.setAntiAlias(true);
        bgPaint.setColor(KIDS_COLORS[number % KIDS_COLORS.length]);
        canvas.drawRect(0, 0, size, size, bgPaint);

        // Number paint
        Paint textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(220f);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setTextAlign(Paint.Align.CENTER);

        // Center vertically
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float y = size / 2f - (fm.ascent + fm.descent) / 2f;

        canvas.drawText(String.valueOf(number), size / 2f, y, textPaint);

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
