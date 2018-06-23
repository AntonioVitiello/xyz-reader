package com.example.xyzreader.handler;

import android.database.Cursor;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateUtils;

import com.example.xyzreader.data.ArticleLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import timber.log.Timber;

/**
 * Created by Antonio Vitiello on 23/06/2018.
 */
public class SubtitleBuilder implements Worker {
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);
    private final Cursor cursor;
    private final WorkerHandler.OnReady onReady;
    private Spanned spanned;


    public SubtitleBuilder(Cursor cursor, WorkerHandler.OnReady onReady) {
        this.cursor = cursor;
        this.onReady = onReady;
    }

    @Override
    public void inBackground() {
        Date publishedDate = parsePublishedDate();
        if (!publishedDate.before(START_OF_EPOCH.getTime())) {
            spanned = Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            publishedDate.getTime(),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by <font color='#ffffff'>"
                            + cursor.getString(ArticleLoader.Query.AUTHOR)
                            + "</font>");
        } else {
            // If date is before 1902, just show the string
            spanned = Html.fromHtml(
                    outputFormat.format(publishedDate) + " by <font color='#ffffff'>"
                            + cursor.getString(ArticleLoader.Query.AUTHOR)
                            + "</font>");
        }
    }

    @Override
    public void onMainThread() {
        onReady.onReady(this);
    }

    private Date parsePublishedDate() {
        try {
            String date = cursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            return dateFormat.parse(date);
        } catch (ParseException exc) {
            Timber.e(exc, "passing today's date");
            return new Date();
        }
    }

    public Spanned getSpanned() {
        return spanned;
    }

}
