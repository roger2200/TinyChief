package com.roger.tinychief.widget.recycler;

/**
 * Created by Roger on 7/25/2016.
 */
public class ItemComment {
    private String mId,mName, mStar,mComment;

    public ItemComment(String id, String name, String star, String comment) {
        this.mId=id;
        this.mName=name;
        this.mStar=star;
        this.mComment=comment;
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getStar() {
        return mStar;
    }

    public String getComment() {
        return mComment;
    }
}
