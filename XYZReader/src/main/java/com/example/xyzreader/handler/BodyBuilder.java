package com.example.xyzreader.handler;

import android.database.Cursor;
import android.text.Html;
import android.text.Spanned;

import com.example.xyzreader.data.ArticleLoader;

/**
 * Created by Antonio Vitiello on 23/06/2018.
 */
public class BodyBuilder implements Worker {
    private final Cursor cursor;
    private final WorkerHandler.OnReady onReady;
    private Spanned spanned;

    public BodyBuilder(Cursor cursor, WorkerHandler.OnReady onReady) {
        this.cursor = cursor;
        this.onReady = onReady;
    }

    @Override
    public void inBackground() {
        String body = cursor.getString(ArticleLoader.Query.BODY);
        spanned = Html.fromHtml(body.replaceAll("(\r\n|\n)", "<br />"));
    }

    @Override
    public void onMainThread() {
        onReady.onReady(this);
    }

    public Spanned getSpanned() {
        return spanned;
    }

}
