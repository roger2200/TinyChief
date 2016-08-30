package com.roger.tinychief.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.roger.tinychief.R;
import com.roger.tinychief.util.NetworkManager;
import com.roger.tinychief.widget.navigation.NavigationViewSetup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class DetailActivity extends AppCompatActivity {
    private static final String TAG = "DetailActivity";

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private NavigationViewSetup mNavigationViewSetup;
    private TextView mTitleTextView, mIiTextView, mStepTextView;
    private ImageView mImageView;
    private String mStrIi, mStrStep;
    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.detail_drawerlayout);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mNavigationViewSetup = new NavigationViewSetup(this, mDrawerLayout, mToolbar);
        mNavigationViewSetup.setNavigationView();

        mTitleTextView = (TextView) findViewById(R.id.title_txtview);
        mIiTextView = (TextView) findViewById(R.id.ii_txtview);
        mStepTextView = (TextView) findViewById(R.id.step_txtview);
        mImageView = (ImageView) findViewById(R.id.detail_imageview);

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
        i.putExtra("IMAGE", convertBitmap2Bytes(mBitmap));
        startActivity(i);
    }

    public void getCookbook() {
        StringRequest request = new StringRequest(Request.Method.POST, "https://tinny-chief.herokuapp.com/getDetailCookBook",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String string) {
                        try {
                            final JSONObject json = new JSONObject(string);
                            setTitle(json.getString("title"));
                            new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected Void doInBackground(Void... params) {
                                    try {
                                        mBitmap = Glide.with(DetailActivity.this).load(json.getString("image")).asBitmap().into(-1, -1).get();
                                    } catch (final Exception e) {
                                        Log.e(TAG, e.getMessage());
                                    }
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Void dummy) {
                                    if (mBitmap != null)
                                        mImageView.setImageBitmap(mBitmap);
                                }
                            }.execute();
                            mTitleTextView.setText(json.getString("title"));
                            mIiTextView.setText(json.get("ingredients").toString());
                            mStepTextView.setText(json.get("steps").toString());
                        } catch (Exception e) {
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
                Map<String, String> param = new HashMap<String, String>();
                param.put("id", DetailActivity.this.getIntent().getExtras().getString("ID"));
                return param;
            }
        };
        NetworkManager.getInstance(this).request(null, request);
    }

    private byte[] convertBitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
}
