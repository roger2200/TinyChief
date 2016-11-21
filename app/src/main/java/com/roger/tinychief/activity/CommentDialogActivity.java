package com.roger.tinychief.activity;

import android.app.ProgressDialog;
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

import com.android.volley.DefaultRetryPolicy;
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
    private Bitmap mRateFBitmap, mRateNBitmap, mRateBitmap;
    private int rate = 5;
    private ProgressDialog mProgressDialog;

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
        mTextView.setText(MainActivity.USER_NAME);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("上傳評論中...");

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

    public void uploadComment(View view) {
        mProgressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, "https://tiny-chief.herokuapp.com/calendar/upload",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String string) {
                        Log.d("Response", string);
                        Intent intent = new Intent();
                        if (string.equals("success"))
                            intent.putExtra("RESULT", true);
                        else
                            intent.putExtra("RESULT", false);
                        intent.putExtra("id_usr", MainActivity.USER_ID);
                        intent.putExtra("name", mTextView.getText().toString());
                        intent.putExtra("rate", rate);
                        intent.putExtra("msg", mEditText.getText().toString());
                        setResult(RESULT_OK, intent);
                        mProgressDialog.dismiss();
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
                        mProgressDialog.dismiss();
                        finish();
                    }
                }) {
            @Override
            public Map<String, String> getParams() {
                Map<String, String> param = new HashMap<>();
                param.put("id_cb", CommentDialogActivity.this.getIntent().getExtras().getString("ID"));
                param.put("name", mTextView.getText().toString());
                param.put("id_usr", MainActivity.USER_ID);
                param.put("rate", String.valueOf(rate));
                param.put("msg", mEditText.getText().toString());
                Log.d("Upload Comment", param.toString());
                return param;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        NetworkManager.getInstance(this).request(null, request);
    }

    public void Cancel(View view) {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
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
