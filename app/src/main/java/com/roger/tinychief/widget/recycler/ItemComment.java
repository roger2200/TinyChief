package com.roger.tinychief.widget.recycler;

import android.content.Context;

public class ItemComment {
    private String mId, mName, mComment;
    private int mRate;
    private Context mContext;

    /**
     * @param id      評論者的id
     * @param name    評論者的暱稱
     * @param rate    評分
     * @param comment 評論
     * @param context getResource要用的
     */
    public ItemComment(String id, String name, int rate, String comment, Context context) {
        this.mId = id;
        this.mName = name;
        this.mRate = rate;
        this.mComment = comment;
        this.mContext = context;
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public int getRate() {
        return mRate;
    }

    public String getComment() {
        return mComment;
    }

    public Context getContext() {
        return mContext;
    }
}
