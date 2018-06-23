package com.example.xyzreader.handler;

import android.database.Cursor;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;

import com.example.xyzreader.data.ArticleLoader;

/**
 * Created by Antonio Vitiello on 23/06/2018.
 */
public class BodyBuilder implements Worker {
    private Cursor cursor;
    private final WorkerHandler.OnReady onReady;
    private String bodyTxt;
    private Spanned spanned;

    public BodyBuilder(Cursor cursor, WorkerHandler.OnReady onReady) {
        this.cursor = cursor;
        this.onReady = onReady;
    }

    public BodyBuilder(String bodyTxt, WorkerHandler.OnReady onReady) {
        this.bodyTxt = bodyTxt;
        this.onReady = onReady;
    }

    @Override
    public void inBackground() {
        if(TextUtils.isEmpty(bodyTxt)){
            bodyTxt = cursor.getString(ArticleLoader.Query.BODY);
        }
        spanned = Html.fromHtml(bodyTxt.replaceAll("(\r\n|\n)", "<br />"));
    }

    @Override
    public void onMainThread() {
        onReady.onReady(this);
    }

    public Spanned getSpanned() {
        return spanned;
    }

}
