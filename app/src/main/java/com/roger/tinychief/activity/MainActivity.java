package com.roger.tinychief.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.roger.tinychief.R;
import com.roger.tinychief.util.NetworkManager;
import com.roger.tinychief.widget.navigation.NavigationViewSetup;
import com.roger.tinychief.widget.recycler.AdapterMain;
import com.roger.tinychief.widget.recycler.AdapterMain.OnRecyclerViewItemClickListener;
import com.roger.tinychief.widget.recycler.ItemMain;
import com.wuxiaolong.pullloadmorerecyclerview.PullLoadMoreRecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final int LOAD_COUNT = 10;//每次讀取的資料筆數,要和server相同
    public static String USER_NAME;
    public static String USER_ID;
    private Toolbar mToolbar;
    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    private PullLoadMoreRecyclerView mRecyclerView;
    private AdapterMain mAdapter;
    private NavigationViewSetup mNavigationViewSetup;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private int skipCount = 1;
    ArrayList<ItemMain> mDataset = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("熱門食譜");
        setInitData();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                return true;
            }
        });
        // Inflate a menu to be displayed in the toolbar
        mToolbar.inflateMenu(R.menu.toolbar_main);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.open, R.string.close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

            }
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        mActionBarDrawerToggle.syncState();
        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
        mNavigationViewSetup = new NavigationViewSetup(this, mDrawerLayout, mToolbar);
        mNavigationView = mNavigationViewSetup.setNavigationView();
        mNavigationView.getMenu().getItem(0).setChecked(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNavigationView.getMenu().getItem(0).setChecked(true);
        if(USER_NAME!=null) {
            View header = mNavigationView.getHeaderView(0);
            TextView name = (TextView) header.findViewById(R.id.txtview_name_header);
            name.setText(USER_NAME);
            Button button=(Button) header.findViewById(R.id.btn_login_header);
            button.setVisibility(View.INVISIBLE);
        }
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_main, menu);

        MenuItem menuSearchItem = menu.findItem(R.id.my_search);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        android.support.v7.widget.SearchView searchView = (android.support.v7.widget.SearchView) menuSearchItem.getActionView();

        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        // 這邊讓icon可以還原到搜尋的icon
        searchView.setIconifiedByDefault(true);
        return true;
    }
    @Override
    protected void onRestart(){
        super.onRestart();
    }

    public void getDataFromSever() {
        StringRequest request = new StringRequest(Request.Method.POST, "https://tiny-chief.herokuapp.com/cookbook/simple",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String string) {
                        try {
                            JSONArray array = new JSONArray(string);
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject json = array.getJSONObject(i);
                                mDataset.add(new ItemMain(json.getString("_id"),json.getJSONObject("author").getString("name"), json.getString("title"), json.getString("image")));
                            }
                            mRecyclerView.setPullLoadMoreCompleted();
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
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("SkipCount", String.valueOf(skipCount++));
                return MyData;
            }
        };
        NetworkManager.getInstance(this).request(null, request);
    }

    private void setRecycleView() {
        mRecyclerView = (PullLoadMoreRecyclerView) findViewById(R.id.recyview_main);
        mRecyclerView.setLinearLayout();
        mRecyclerView.setPullRefreshEnable(false);
        mRecyclerView.animate();
        mRecyclerView.setFooterViewText("讀取中...");

        mRecyclerView.setOnPullLoadMoreListener(new PullLoadMoreRecyclerView.PullLoadMoreListener() {
            @Override
            public void onRefresh() {
            }
            @Override
            public void onLoadMore() {
                getDataFromSever();
            }
        });

        mAdapter = new AdapterMain(mDataset);
        mAdapter.setOnItemClickListener(new OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(String string) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("ID", string);
                startActivity(intent);
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }

    //取得並設定SplashActivity傳來的資料
    private void setInitData() {
        Bundle bundle = this.getIntent().getExtras();
        String[][] data = new String[LOAD_COUNT][];
        for (int i = 0; i < data.length; i++) {
            data[i] = bundle.getStringArray("DATA" + i);
            mDataset.add(new ItemMain(data[i][0], data[i][1], data[i][2],data[i][3]));
        }
        setRecycleView();
    }
}
