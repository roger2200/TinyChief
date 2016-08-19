package com.roger.tinychief.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.roger.tinychief.R;
import com.roger.tinychief.util.NetworkManager;
import com.roger.tinychief.widget.navigation.NavigationViewSetup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
//import com.vuforia.ImageTarget;

public class DetailActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private NavigationViewSetup mNavigationViewSetup;
    private TextView mTitleTextView,mIiTextView,mStepTextView;
    private ImageView mImageView;
    private String mStrIi,mStrStep;
    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.detail_drawerlayout);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mNavigationViewSetup = new NavigationViewSetup(this, mDrawerLayout, mToolbar);
        mNavigationViewSetup.setNavigationView();

        mTitleTextView=(TextView)findViewById(R.id.title_txtview);
        mIiTextView=(TextView)findViewById(R.id.ii_txtview);
        mStepTextView=(TextView)findViewById(R.id.step_txtview);
        mImageView=(ImageView)findViewById(R.id.detail_imageview);

        Bundle bundle = this.getIntent().getExtras();
        setTitle(bundle.getString("TITLE"));
        getCookbook();
    }

    public void letCook(View view) {
        Intent intent = new Intent(view.getContext(), CookActivity.class);
        startActivity(intent);
    }

    public void openAR(View view) {
        Intent i = new Intent(view.getContext(), ArActivity.class);
        //i.putExtra("IMAGE_PATH", path);
        startActivity(i);
    }

    public void getCookbook() {
        StringRequest request = new StringRequest(Request.Method.POST, "https://tiny-chief.herokuapp.com/getDetailCookBook",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String string) {
                        try {
                            String str="";
                            JSONObject json = new JSONArray(string).getJSONObject(0);
                            JSONArray arrayIi= new JSONArray(json.getString("ingredients"));
                            JSONArray arrayStep= new JSONArray(json.getString("steps"));
                            mBitmap=getImageString(json.getString("image"));
                            mImageView.setImageBitmap(mBitmap);
                            mTitleTextView.setText(json.getString("title")+"\n");
                            for(int i=0;i<arrayIi.length();i++)
                                str+=arrayIi.getString(i)+"\n";
                            mIiTextView.setText(str);
                            str="";
                            for(int i=0;i<arrayStep.length();i++)
                                str+=arrayStep.getString(i)+"\n\n";
                            mStepTextView.setText(str);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.e("Error", String.valueOf(volleyError));
                    }
                }) {
            @Override
            public Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("title", (String) getTitle());
                return MyData;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        NetworkManager.getInstance(this).request(null, request);
    }

    //將Base64字串轉圖片
    public Bitmap getImageString(String str) {
        byte[] decodedBytes = Base64.decode(str, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}
