package com.roger.tinychief.widget.recycler;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.roger.tinychief.R;

import java.util.List;

/**
 * Created by Roger on 4/24/2016.
 */
//要讓RecycleView顯示出資料,必須先使用Adapter將個別資料的內容處理好
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> implements OnClickListener {
    private List<String> mData;
    private OnRecyclerViewItemClickListener mOnItemClickListener = null;

    public RecyclerViewAdapter(List<String> data) {
        mData = data;
    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view , String data);
    }

    //自訂的holder,負責處理每個item裡的元素
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;
        public ViewHolder(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.recy_author_txt);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recy_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        v.setOnClickListener(this); //註冊onclick事件
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mTextView.setText(mData.get(position));
        holder.itemView.setTag(mData.get(position));
    }

    @Override
    public void onClick(View v){
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(v,(String)v.getTag());
        }
    }

    //設置一個fumction,提供外部使用listener
    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}