package com.roger.tinychief.activity;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.roger.tinychief.R;
import com.roger.tinychief.widget.navigation.NavigationViewSetup;
import com.roger.tinychief.widget.recycler.AdapterMain;
import com.roger.tinychief.widget.recycler.ItemMain;
import com.wuxiaolong.pullloadmorerecyclerview.PullLoadMoreRecyclerView;

import java.util.ArrayList;

public class SearchableActivity extends Activity {

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
        setContentView(R.layout.activity_searchable);
        setTitle("查詢食譜");
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        handleIntent(getIntent());
    }
    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
        }
    }
}
