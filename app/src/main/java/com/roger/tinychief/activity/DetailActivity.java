package com.roger.tinychief.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.roger.tinychief.IiDetailDialogActivity;
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
    private final int REQUEST_COM = 0, REQUEST_DATE = 1;

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private CoordinatorLayout mCoordinatorLayout;
    private LinearLayout mStepLinearLayout;
    private TableLayout mIiTableLayout;
    private TextView mTitleTextView;
    private ImageView mImageView;
    private RecyclerView mRecyclerView;
    private AdapterComment mAdapter;
    private Bitmap mArBitmap;
    private NavigationViewSetup mNavigationViewSetup;
    private NavigationView mNavigationView;
    private String[] mStrArrayStep;
    private String mStrID, mStrTitle;
    private ArrayList<ItemComment> mDataset = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Bundle bundle = this.getIntent().getExtras();
        mStrID = bundle.getString("ID");


        mTitleTextView = (TextView) findViewById(R.id.txtview_title_detail);
        mImageView = (ImageView) findViewById(R.id.img_detail);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorlayout_detail);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout_detail);
        mIiTableLayout = (TableLayout) findViewById(R.id.tablelayout_ii_detail);
        mStepLinearLayout = (LinearLayout) findViewById(R.id.linearlayout_step_detail);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        mNavigationViewSetup = new NavigationViewSetup(this, mDrawerLayout, mToolbar);
        mNavigationView = mNavigationViewSetup.setNavigationView();

        setRecycleView();
        getCookbook();
        setTitle(mStrTitle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNavigationView = mNavigationViewSetup.setNavigationView();
        for (int i = 0; i < mNavigationView.getMenu().size(); i++)
            mNavigationView.getMenu().getItem(i).setChecked(false);
    }

    @Override
    protected void onDestroy() {
        NetworkManager.getInstance(this).cancelRequest("normal");
        NetworkManager.getInstance(this).cancelRequest("official");
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Snackbar snackbar = Snackbar.make(mCoordinatorLayout, "", Snackbar.LENGTH_LONG);
            MyHelper.setSnackbarMessageTextColor(snackbar, android.graphics.Color.WHITE);
            switch (requestCode) {
                case REQUEST_COM:
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
                    break;
                case REQUEST_DATE:
                    if (data.getBooleanExtra("RESULT", false)) {
                        snackbar.setText("已加入食譜日曆");
                    } else
                        snackbar.setText("加入日曆失敗");
                    snackbar.show();
                    break;
            }
        }
    }

    public void onClickStartCook(View view) {
        Intent intent = new Intent(view.getContext(), CookActivity.class);
        intent.putExtra("STEP", mStrArrayStep);
        startActivity(intent);
    }

    public void onClickWriteComment(View view) {
        if (MainActivity.USER_NAME == null) {
            Toast.makeText(this, "請先登入", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, CommentDialogActivity.class);
        intent.putExtra("ID", mStrID);
        startActivityForResult(intent, REQUEST_COM);
    }

    public void onClickOpenAR(View view) {
        if (mArBitmap == null)
            return;
        Intent intent = new Intent(view.getContext(), ArActivity.class);
        intent.putExtra("IMAGE", MyHelper.convertBitmap2Bytes(mArBitmap));
        startActivity(intent);
    }

    public void onClickAddCalendar(View view) {
        if (MainActivity.USER_NAME == null) {
            Toast.makeText(this, "請先登入", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, DatepickerDialogActivity.class);
        intent.putExtra("ID", mStrID);
        intent.putExtra("TITLE", mStrTitle);
        startActivityForResult(intent, REQUEST_DATE);
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
                            if (!json.getString("image_ar").equals("")) {
                                findViewById(R.id.btn_openar_detail).setEnabled(true);
                                new AsyncTask<Integer, Integer, Integer>() {
                                    @Override
                                    protected Integer doInBackground(Integer... parm) {
                                        try {
                                            mArBitmap = Glide.with(DetailActivity.this).load(json.getString("image_ar")).asBitmap().into(-1, -1).get();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        return null;
                                    }
                                }.execute();
                            }
                            Glide.with(DetailActivity.this).load(json.getString("image")).asBitmap().into(mImageView);

                            mStrTitle = json.getString("title");
                            mTitleTextView.setText(mStrTitle);
                            setTitle(mStrTitle);

                            for (int i = 0; i < jsonArrayIi.length(); i++)
                                addIi(jsonArrayIi.getJSONObject(i).getString("name"),
                                        jsonArrayIi.getJSONObject(i).getDouble("amount"),
                                        jsonArrayIi.getJSONObject(i).getString("unit"),
                                        jsonArrayIi.getJSONObject(i).getInt("class"));

                            mStrArrayStep = new String[jsonArrayStep.length()];
                            for (int i = 0; i < jsonArrayStep.length(); i++) {
                                mStrArrayStep[i] = jsonArrayStep.getString(i);
                                LinearLayout linearLayout = new LinearLayout(DetailActivity.this);
                                TextView textViewStepCount = new TextView(DetailActivity.this);
                                TextView textViewStep = new TextView(DetailActivity.this);

                                textViewStep.setTextSize(22.0f);
                                textViewStepCount.setTextSize(22.0f);

                                textViewStepCount.setText((i + 1) + ".  ");
                                textViewStep.setText(jsonArrayStep.getString(i) + "\n");

                                linearLayout.addView(textViewStepCount);
                                linearLayout.addView(textViewStep);
                                mStepLinearLayout.addView(linearLayout);
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
                        Log.e(TAG, String.valueOf(volleyError));
                    }
                }) {
            @Override
            public Map<String, String> getParams() {
                Map<String, String> param = new HashMap<>();
                param.put("id", mStrID);
                return param;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        NetworkManager.getInstance(this).request("normal", request);
    }

    private void setRecycleView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recyview_comment_detail);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new AdapterComment(mDataset);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void addIi(final String name, double amount, String unit, final int intClass) {
        Drawable drawable;
        TextView textViewPrice = new TextView(DetailActivity.this);
        TextView textViewAmount = new TextView(DetailActivity.this);
        TextView textViewName = new TextView(DetailActivity.this);
        TableRow tablerow = new TableRow(DetailActivity.this);
        TableLayout.LayoutParams lp = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,
                TableLayout.LayoutParams.WRAP_CONTENT);

        lp.setMargins(5, 5, 5, 5);
        tablerow.setLayoutParams(lp);
        tablerow.setPadding(10, 10, 10, 10);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            drawable = DetailActivity.this.getResources().getDrawable(R.drawable.background_rect_radius, getTheme());
            tablerow.setBackground(drawable);
        } else {
            drawable = DetailActivity.this.getResources().getDrawable(R.drawable.background_rect_radius);
            tablerow.setBackgroundDrawable(drawable);
        }

        tablerow.setClickable(true);
        tablerow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), IiDetailDialogActivity.class);
                intent.putExtra("NAME", name);
                intent.putExtra("CLASS", intClass);
                startActivity(intent);
            }
        });
        textViewName.setTextSize(24.0f);
        textViewAmount.setTextSize(24.0f);
        textViewPrice.setTextSize(24.0f);

        textViewName.setText(name);
        if (name.equals("雞蛋") || name.equals("鴨蛋"))
            textViewAmount.setText((amount * 10) + "  " + unit);
        else
            textViewAmount.setText(amount + "  " + unit);

        tablerow.addView(textViewName);
        tablerow.addView(textViewAmount);
        mIiTableLayout.addView(tablerow);
    }
}
