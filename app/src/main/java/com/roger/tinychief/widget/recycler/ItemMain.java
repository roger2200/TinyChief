package com.roger.tinychief.widget.recycler;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**cardview用的adapter*/
public class ItemMain {
    private String mId,mAuthor, mTitle,mUrl;

    public ItemMain(String id, String author, String title, String url) {
        this.mId=id;
        this.mAuthor=author;
        this.mTitle=title;
        this.mUrl=url;
    }

    public String getId() {
        return mId;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getUrl() {
        return mUrl;
    }
}
