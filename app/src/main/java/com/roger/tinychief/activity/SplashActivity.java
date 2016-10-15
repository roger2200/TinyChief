package com.roger.tinychief.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.roger.tinychief.R;
import com.roger.tinychief.util.MyHelper;
import com.roger.tinychief.util.NetworkManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

//歡迎畫面,會在背景從server讀取資料
public class SplashActivity extends AppCompatActivity {
    private static final int LOAD_MOUNT = 10;//每次讀取的資料筆數,要和server相同
    private static final int CAMERA_REQUEST_CODE = 0;
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;
    private boolean mFlagReady = false;

    private String[][] mData;//要傳給MainActivity的資料

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ImageView mImageView = (ImageView) findViewById(R.id.img_splash);
        Bitmap bitmap = BitmapFactory.decodeResource(getApplication().getResources(), R.drawable.splash_img);
        mImageView.setImageBitmap(MyHelper.scaleBitmap(bitmap, this));
        mData = new String[LOAD_MOUNT][];
        getData();

        //檢查權限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        else
            countDown();
    }

    //檢查權限之後的動作
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case WRITE_EXTERNAL_STORAGE_REQUEST_CODE:
                //檢查相機權限
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED)
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
                else
                    countDown();
                break;
            case CAMERA_REQUEST_CODE:
                countDown();
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                countDown();
        }
    }

    private void getData() {
        StringRequest request = new StringRequest(Request.Method.POST, "https://tiny-chief.herokuapp.com/cookbook/simple",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String string) {
                        try {
                            JSONArray array = new JSONArray(string);
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject json = array.getJSONObject(i);
                                mData[i] = new String[]{json.getString("_id"), json.getJSONObject("author").getString("name"), json.getString("title"), json.getString("image")};
                            }
                            if (mFlagReady)
                                endActivity();
                            else
                                mFlagReady = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.e("Error", String.valueOf(volleyError));
                        getData(); //失敗重連
                    }
                }) {
            @Override
            public Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("SkipCount", String.valueOf(0));
                return MyData;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        NetworkManager.getInstance(this).request(null, request);
    }

    /**
     * 結束Activity並傳幾筆資料給MainActivity
     */
    private void endActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        for (int i = 0; i < mData.length; i++)
            intent.putExtra("DATA" + i, mData[i]);
        startActivity(intent);
        finish();//讓使用者就算按了返回鍵也回不來這畫面
    }

    /**
     * 倒數三秒切畫面
     */
    private void countDown() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mFlagReady)
                    endActivity();
                else
                    mFlagReady = true;
            }
        }, 3000);
    }
}
