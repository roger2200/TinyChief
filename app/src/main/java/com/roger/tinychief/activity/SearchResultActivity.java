package com.roger.tinychief.activity;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.roger.tinychief.R;
import com.roger.tinychief.util.NetworkManager;
import com.roger.tinychief.widget.navigation.NavigationViewSetup;
import com.roger.tinychief.widget.recycler.AdapterMain;
import com.roger.tinychief.widget.recycler.ItemMain;
import com.wuxiaolong.pullloadmorerecyclerview.PullLoadMoreRecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SearchResultActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private NavigationViewSetup mNavigationViewSetup;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private PullLoadMoreRecyclerView mRecyclerView;
    private AdapterMain mAdapter;
    ArrayList<ItemMain> mDataset = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        setTitle("查詢食譜");
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout_searchResult);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mNavigationViewSetup = new NavigationViewSetup(this, mDrawerLayout, mToolbar);
        mNavigationView = mNavigationViewSetup.setNavigationView();
        mNavigationView.getMenu().getItem(0).setChecked(true);
        mRecyclerView = (PullLoadMoreRecyclerView) findViewById(R.id.recyview_searchResult);
        mRecyclerView.setLinearLayout();
        mRecyclerView.setPullRefreshEnable(false);
        mRecyclerView.animate();
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        setRecycleView();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            final String query = intent.getStringExtra(SearchManager.QUERY);
            Log.d("test",query);
            StringRequest request = new StringRequest(Request.Method.POST, "https://tiny-chief.herokuapp.com/search/result",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String string) {
                            try {
                                JSONArray array = new JSONArray(string);
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject json = array.getJSONObject(i);
                                    mDataset.add(new ItemMain(json.getString("_id"),json.getJSONObject("author").getString("name"), json.getString("title"), json.getString("image")));
                                    setRecycleView();
                                }
                                mRecyclerView.setPullLoadMoreCompleted();
                            } catch (Exception e) {
                                Log.d("error:", e.getMessage());
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
                    Map<String, String> MyData = new HashMap<>();
                    MyData.put("SearchTitle", String.valueOf(query));
                    return MyData;
                }
            };
            NetworkManager.getInstance(this).request(null, request);
            request.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        }
    }
    private void setRecycleView() {
        mRecyclerView = (PullLoadMoreRecyclerView) findViewById(R.id.recyview_searchResult);
        mRecyclerView.setLinearLayout();
        mRecyclerView.setRefreshing(false);
        mRecyclerView.setPullRefreshEnable(false);
        mRecyclerView.animate();
        mRecyclerView.setFooterViewText("讀取中...");

        mRecyclerView.setOnPullLoadMoreListener(new PullLoadMoreRecyclerView.PullLoadMoreListener() {
            @Override
            public void onRefresh() {
            }
            @Override
            public void onLoadMore() {
                mRecyclerView.setPullLoadMoreCompleted();
            }
        });

        mAdapter = new AdapterMain(mDataset);
        mAdapter.setOnItemClickListener(new AdapterMain.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(String string) {
                Intent intent = new Intent(SearchResultActivity.this, DetailActivity.class);
                intent.putExtra("ID", string);
                startActivity(intent);
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }
}
