package com.roger.tinychief.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.roger.tinychief.R;
import com.roger.tinychief.util.NetworkManager;

import java.util.HashMap;
import java.util.Map;

public class CommentDialogActivity extends AppCompatActivity {
    private TextView mTextView;
    private EditText mEditText;
    private ImageView mImageView;
    private int rate = 5;
    Bitmap mRateFBitmap, mRateNBitmap, mRateBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_dialog);
        mRateFBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.rate_star);
        mRateNBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.rate_star_n);

        mTextView = (TextView) findViewById(R.id.txtview_name_comment_dialog);
        mEditText = (EditText) findViewById(R.id.edittxt_comment_dialog);
        mImageView = (ImageView) findViewById(R.id.img_rate_comment_dialog);
        mImageView.setOnTouchListener(getOnTouchListener());

        drawRate();
    }

    private View.OnTouchListener getOnTouchListener() {
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                rate = (int) motionEvent.getX() / (mImageView.getWidth() / 5) + 1;
                switch (motionEvent.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_MOVE:
                        drawRate();
                        return true;
                }
                return false;
            }
        };
    }

    protected void uploadComment(final View view) {
        StringRequest request = new StringRequest(Request.Method.POST, "http://10.0.2.2:5000/upload/comment",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String string) {
                        Intent intent = new Intent();
                        intent.putExtra("RESULT", true);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.e("Error", String.valueOf(volleyError));
                        Intent intent = new Intent();
                        intent.putExtra("RESULT", false);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }) {
            @Override
            public Map<String, String> getParams() {
                Map<String, String> param = new HashMap<>();
                param.put("id_cb", CommentDialogActivity.this.getIntent().getExtras().getString("ID"));
                param.put("name", mTextView.getText().toString());
                param.put("id_usr", "5787a635e07c9e0300237873");
                param.put("rate", String.valueOf(rate));
                param.put("msg", mEditText.getText().toString());
                Log.d("Map", param.toString());
                return param;
            }
        };
        NetworkManager.getInstance(this).request(null, request);
    }

    private void drawRate() {
        if (rate <= 0) rate = 1;
        mRateBitmap = Bitmap.createBitmap(mRateFBitmap.getWidth() * 5, mRateFBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(mRateBitmap);
        for (int i = 0; i < rate; i++)
            cv.drawBitmap(mRateFBitmap, mRateFBitmap.getWidth() * i, 0, null);
        for (int i = 0; i < 5 - rate; i++)
            cv.drawBitmap(mRateNBitmap, mRateFBitmap.getWidth() * rate + mRateFBitmap.getWidth() * i, 0, null);
        cv.save(Canvas.ALL_SAVE_FLAG);
        cv.restore();
        mImageView.setImageBitmap(mRateBitmap);
    }
}
