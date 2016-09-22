package com.roger.tinychief.widget.recycler;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.roger.tinychief.R;
import com.roger.tinychief.activity.DetailActivity;

import java.util.ArrayList;

/**
 * Created by Roger on 4/24/2016.
 */
//要讓RecycleView顯示出資料,必須先使用Adapter將個別資料的內容處理好
public class AdapterComment extends RecyclerView.Adapter<AdapterComment.ViewHolder> implements OnClickListener {
    private ArrayList<ItemComment> mData;
    private OnRecyclerViewItemClickListener mOnItemClickListener = null;

    public AdapterComment(ArrayList<ItemComment> data) {
        mData = data;
    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(String v);
    }

    //自訂的holder,負責處理每個item裡的元素
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mNameTextView, mCommentView;
        public ImageView mRateImageView;

        public ViewHolder(View v) {
            super(v);
            mNameTextView = (TextView) v.findViewById(R.id.txtview_name_comment);
            mCommentView = (TextView) v.findViewById(R.id.txtview_comment_comment);
            mRateImageView = (ImageView) v.findViewById(R.id.img_rate_comment);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recy_item_comment, parent, false);
        ViewHolder vh = new ViewHolder(v);
        v.setOnClickListener(this); //註冊onclick事件
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mNameTextView.setText(mData.get(position).getName());
        holder.mCommentView.setText(mData.get(position).getComment());
        holder.itemView.setTag(mData.get(position).getId());
        holder.mRateImageView.setImageBitmap(drawRate(mData.get(position).getRate(), mData.get(position).getContext()));
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(v.getTag().toString());
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    //設置一個fumction,提供外部使用listener
    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    private Bitmap drawRate(int rate, Context context) {
        Bitmap rateF = BitmapFactory.decodeResource(context.getResources(), R.drawable.rate_star);
        Bitmap rateN = BitmapFactory.decodeResource(context.getResources(), R.drawable.rate_star_n);
        int picWidth = rateF.getWidth();
        Bitmap bitmap = Bitmap.createBitmap(picWidth * 5, rateF.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(bitmap);
        for (int i = 0; i < rate; i++)
            cv.drawBitmap(rateF, picWidth * i, 0, null);
        for (int i = 0; i < 5 - rate; i++)
            cv.drawBitmap(rateN, picWidth * rate + picWidth * i, 0, null);
        cv.save(Canvas.ALL_SAVE_FLAG);
        cv.restore();
        return bitmap;
    }
}