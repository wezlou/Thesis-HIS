package com.example.holyinfantschool;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CircleProgressView extends View {

    private Paint bgPaint;
    private Paint progressPaint;
    private Paint textPaint;
    private RectF arcRect = new RectF();

    private int progress = 0;

    public CircleProgressView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);

        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(0xFFE0E0E0);
        bgPaint.setStrokeWidth(22);
        bgPaint.setStyle(Paint.Style.STROKE);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(0xFF00E4D0); // Aqua color
        progressPaint.setStrokeWidth(22);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFF000000);
        textPaint.setTextSize(60);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();
        float radius = Math.min(w, h) / 2f - 40;
        float cx = w / 2f;
        float cy = h / 2f;

        arcRect.set(cx - radius, cy - radius, cx + radius, cy + radius);

        canvas.drawArc(arcRect, 0, 360, false, bgPaint);

        float sweep = (progress / 100f) * 360f;
        canvas.drawArc(arcRect, -90, sweep, false, progressPaint);

        canvas.drawText(progress + "%", cx, cy + 20, textPaint);
    }

    public void setProgress(int p) {
        progress = Math.max(0, Math.min(p, 100));
        invalidate();
    }
}
