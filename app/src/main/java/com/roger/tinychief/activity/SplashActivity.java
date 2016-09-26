package com.roger.tinychief.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.roger.tinychief.R;
import com.roger.tinychief.util.NetworkManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

//歡迎畫面,會在背景從server讀取資料
public class SplashActivity extends AppCompatActivity {
    private static final int LOAD_MOUNT = 10;//每次讀取的資料筆數,要和server相同

    private String[][] mData;//要傳給MainActivity的資料

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mData = new String[LOAD_MOUNT][];
        getData();
    }

    private void getData() {
        StringRequest request = new StringRequest(Request.Method.POST, "https://tinny-chief.herokuapp.com/cookbook/simple",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String string) {
                        try {
                            JSONArray array = new JSONArray(string);
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject json = array.getJSONObject(i);
                                mData[i] = new String[]{json.getString("_id"),json.getJSONObject("author").getString("name"), json.getString("title"), json.getString("image")};
                            }
                            endActivity();
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

    //結束Activity並傳幾筆資料給MainActivity
    private void endActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        for(int i=0;i<mData.length;i++)
            intent.putExtra("DATA"+i, mData[i]);
        startActivity(intent);
        finish();//讓使用者就算按了返回鍵也回不來這畫面
    }
}
