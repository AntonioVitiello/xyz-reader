package com.example.xyzreader.event;

import android.text.Spanned;

/**
 * Created by Antonio Vitiello on 22/06/2018.
 */
public class DetailEvent {
    private String title;
    private Spanned subtitle;
    private String imageurl;
    private int position;
    private String body;

    private DetailEvent(Builder builder) {
        title = builder.title;
        subtitle = builder.subtitle;
        imageurl = builder.imageurl;
        position = builder.position;
        body = builder.body;
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

    public int getPosition() {
        return position;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "DetailEvent{" +
                "title='" + title + '\'' +
                ", subtitle=" + subtitle +
                ", imageurl='" + imageurl + '\'' +
                ", position=" + position +
                ", body='" + body.substring(0, 15) + '\'' +
                '}';
    }


    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private String title;
        private Spanned subtitle;
        private String imageurl;
        private int position;
        private String body;

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

        public Builder position(int val) {
            position = val;
            return this;
        }

        public Builder body(String val) {
            body = val;
            return this;
        }

        public DetailEvent build() {
            return new DetailEvent(this);
        }
    }

}
