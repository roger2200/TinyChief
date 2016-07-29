package com.roger.tinychief.widget.recycler;

/**
 * Created by Roger on 7/25/2016.
 */
public class RecyclerViewItem {
    private String mAuthor, mTitle;


    public RecyclerViewItem(String author, String title) {
        this.mAuthor=author;
        this.mTitle=title;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getTitle() {
        return mTitle;
    }
}
