package com.roger.tinychief.widget.recycler;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * Created by Roger on 7/25/2016.
 */
public class RecyclerViewItem {
    private String mAuthor, mTitle,mUrl;

    public RecyclerViewItem(String author, String title,String url) {
        this.mAuthor=author;
        this.mTitle=title;
        this.mUrl=url;
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
