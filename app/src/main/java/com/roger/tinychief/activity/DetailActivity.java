package com.roger.tinychief.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.roger.tinychief.R;
import com.roger.tinychief.util.MyHelper;
import com.roger.tinychief.util.NetworkManager;
import com.roger.tinychief.widget.navigation.NavigationViewSetup;
import com.roger.tinychief.widget.recycler.AdapterComment;
import com.roger.tinychief.widget.recycler.ItemComment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DetailActivity extends AppCompatActivity {
    private static final String TAG = "DetailActivity";
    private final int REQUEST_COM = 0;

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private CoordinatorLayout mCoordinatorLayout;
    private LinearLayout mIiLinearLayout, mStepLinearLayout;
    private TextView mTitleTextView;
    private ImageView mImageView;
    private RecyclerView mRecyclerView;
    private AdapterComment mAdapter;
    private Bitmap mArBitmap;
    private NavigationViewSetup mNavigationViewSetup;
    private String[] strArraySteps;
    private ArrayList<ItemComment> mDataset = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mTitleTextView = (TextView) findViewById(R.id.txtview_title_detail);
        mImageView = (ImageView) findViewById(R.id.img_detail);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorlayout_detail);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout_detail);
        mIiLinearLayout = (LinearLayout) findViewById(R.id.linearlayout_ii_detail);
        mStepLinearLayout = (LinearLayout) findViewById(R.id.linearlayout_step_detail);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        mNavigationViewSetup = new NavigationViewSetup(this, mDrawerLayout, mToolbar);
        mNavigationViewSetup.setNavigationView();

        setRecycleView();

        Bundle bundle = this.getIntent().getExtras();
        setTitle(bundle.getString("TITLE"));
        getCookbook();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_COM) {
                Snackbar snackbar = Snackbar.make(mCoordinatorLayout, "", Snackbar.LENGTH_LONG);
                MyHelper.setSnackbarMessageTextColor(snackbar, android.graphics.Color.WHITE);
                if (data.getBooleanExtra("RESULT", false)) {
                    snackbar.setText("上傳評論完成");
                    mDataset.add(new ItemComment(data.getStringExtra("id_usr")
                            , data.getStringExtra("name")
                            , data.getIntExtra("rate", 1)
                            , data.getStringExtra("msg")
                            , getApplicationContext()));
                    mAdapter.notifyItemInserted(mDataset.size());
                } else
                    snackbar.setText("上傳評論失敗");
                snackbar.show();
            }
        }
    }

    public void letCook(View view) {
        Intent intent = new Intent(view.getContext(), CookActivity.class);
        intent.putExtra("steps",strArraySteps);
        startActivity(intent);
    }

    public void writeComment(View view) {
        Intent intent = new Intent(this, CommentDialogActivity.class);
        intent.putExtra("ID", DetailActivity.this.getIntent().getExtras().getString("ID"));
        startActivityForResult(intent, REQUEST_COM);
    }

    public void openAR(View view) {
        Intent intent = new Intent(view.getContext(), ArActivity.class);
        intent.putExtra("IMAGE", MyHelper.convertBitmap2Bytes(mArBitmap));
        startActivity(intent);
    }

    public void getCookbook() {
        StringRequest request = new StringRequest(Request.Method.POST, "https://tiny-chief.herokuapp.com/cookbook/detail",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String string) {
                        try {
                            final JSONObject json = new JSONObject(string);
                            JSONArray jsonArrayIi = json.getJSONArray("ingredients");
                            JSONArray jsonArrayStep = json.getJSONArray("steps");
                            //讀圖片
                            Glide.with(DetailActivity.this).load(json.getString("image")).asBitmap().into(mImageView);
                            new AsyncTask<Integer, Integer, Integer>() {
                                @Override
                                protected Integer doInBackground(Integer... parm) {
                                    try {
                                        mArBitmap = Glide.with(DetailActivity.this).load(json.getString("image_ar")).asBitmap().into(-1, -1).get();
                                    } catch (Exception e) {
                                        Log.e(TAG, e.getMessage());
                                    }
                                    return null;
                                }
                            }.execute();

                            mTitleTextView.setText(json.getString("title"));
                            setTitle(json.getString("title"));
                            for (int i = 0; i < jsonArrayIi.length(); i++) {
                                TextView textViewName = new TextView(DetailActivity.this);
                                TextView textViewAmount = new TextView(DetailActivity.this);
                                LinearLayout linearLayout = new LinearLayout(DetailActivity.this);
                                LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

                                textViewName.setTextSize(26.0f);
                                textViewAmount.setTextSize(26.0f);
                                textViewAmount.setLayoutParams(layoutParams);
                                textViewAmount.setGravity(Gravity.END);

                                textViewName.setText(jsonArrayIi.getJSONObject(i).getString("name"));
                                textViewAmount.setText(jsonArrayIi.getJSONObject(i).getInt("amount") + "\t" + jsonArrayIi.getJSONObject(i).getString("unit"));

                                linearLayout.addView(textViewName);
                                linearLayout.addView(textViewAmount);
                                mIiLinearLayout.addView(linearLayout);
                            }

                            strArraySteps = new String[jsonArrayStep.length()];
                            for (int i = 0; i < jsonArrayStep.length(); i++) {
                                TextView textViewStep = new TextView(DetailActivity.this);
                                textViewStep.setTextSize(26.0f);
                                textViewStep.setText(jsonArrayStep.getString(i) + "\n");
                                strArraySteps[i] = jsonArrayStep.getString(i);
                                mStepLinearLayout.addView(textViewStep);
                            }
                            if (json.getJSONArray("comment") != null) {
                                JSONArray jsonArrayComment = json.getJSONArray("comment");
                                for (int i = 0; i < jsonArrayComment.length(); i++) {
                                    mDataset.add(new ItemComment(jsonArrayComment.getJSONObject(i).getString("id")
                                            , jsonArrayComment.getJSONObject(i).getString("name")
                                            , jsonArrayComment.getJSONObject(i).getInt("rate")
                                            , jsonArrayComment.getJSONObject(i).getString("message")
                                            , getApplicationContext()));
                                    mAdapter.notifyItemInserted(mDataset.size());
                                }
                            }
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
        request.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        NetworkManager.getInstance(this).request(null, request);
    }

    private void setRecycleView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recyview_comment_detail);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new AdapterComment(mDataset);
        mAdapter.setOnItemClickListener(new AdapterComment.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(String string) {
                Toast.makeText(DetailActivity.this, string, Toast.LENGTH_SHORT).show();
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }
}
