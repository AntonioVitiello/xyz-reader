package com.example.xyzreader.handler;

import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;

/**
 * Created by Antonio Vitiello on 23/06/2018.
 */
public class PaletteBuilder implements Worker {
    private final Bitmap bitmap;
    private final WorkerHandler.OnReady onReady;
    private final int defaultColor;
    private int mutedColor;

    public PaletteBuilder(Bitmap bitmap, int defaultColor, WorkerHandler.OnReady onReady) {
        this.bitmap = bitmap;
        this.defaultColor = defaultColor;
        this.onReady = onReady;
    }

    @Override
    public void inBackground() {
        if (bitmap != null) {
            Palette palette = Palette.from(bitmap).generate();
            mutedColor = palette.getDarkVibrantColor(defaultColor);
        }
    }

    @Override
    public void onMainThread() {
        onReady.onReady(this);
    }

    public int getMutedColor() {
        return mutedColor;
    }

}
