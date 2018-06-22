package com.example.xyzreader.event;

import android.text.Spanned;

/**
 * Created by Antonio Vitiello on 22/06/2018.
 */
public class DetailEvent {
    private String title;
    private Spanned subtitle;
    private String imageurl;

    private DetailEvent(Builder builder) {
        title = builder.title;
        subtitle = builder.subtitle;
        imageurl = builder.imageurl;
    }

    public String getTitle() {
        return title;
    }

    public Spanned getSubtitle() {
        return subtitle;
    }

    public String getImageurl() {
        return imageurl;
    }

    @Override
    public String toString() {
        return "DetailEvent{" +
                "title='" + title + '\'' +
                ", subtitle=" + subtitle +
                ", imageurl='" + imageurl + '\'' +
                '}';
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private String title;
        private Spanned subtitle;
        private String imageurl;

        private Builder() {
        }

        public Builder title(String val) {
            title = val;
            return this;
        }

        public Builder subtitle(Spanned val) {
            subtitle = val;
            return this;
        }

        public Builder imageurl(String val) {
            imageurl = val;
            return this;
        }

        public DetailEvent build() {
            return new DetailEvent(this);
        }
    }
}
