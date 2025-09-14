package com.example.holyinfantschool;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

public class FillGenerator {

    // Generate a white silhouette (fill) from an outline image
    public static Bitmap generateFillFromOutline(Context ctx, int outlineRes) {
        Bitmap outline = BitmapFactory.decodeResource(ctx.getResources(), outlineRes);
        if (outline == null) return null;

        Bitmap fill = Bitmap.createBitmap(outline.getWidth(), outline.getHeight(), Bitmap.Config.ARGB_8888);

        for (int x = 0; x < outline.getWidth(); x++) {
            for (int y = 0; y < outline.getHeight(); y++) {
                int pixel = outline.getPixel(x, y);
                // if pixel is visible (non-transparent), mark as white
                if (Color.alpha(pixel) > 0) {
                    fill.setPixel(x, y, Color.WHITE);
                }
            }
        }
        return fill;
    }
}
