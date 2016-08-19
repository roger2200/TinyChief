package com.roger.tinychief.widget.recycler;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * Created by Roger on 7/25/2016.
 */
public class RecyclerViewItem {
    private String mAuthor, mTitle;
    private Bitmap mBitmap;

    public RecyclerViewItem(String author, String title,Bitmap bitmap) {
        this.mAuthor=author;
        this.mTitle=title;
        this.mBitmap=bitmap;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getTitle() {
        return mTitle;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }
}
