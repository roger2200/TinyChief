package com.roger.tinychief.activity;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.roger.tinychief.R;
import com.roger.tinychief.widget.navigation.NavigationViewSetup;

public class SearchResultActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private NavigationViewSetup mNavigationViewSetup;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        setTitle("查詢食譜");
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout_searchResult);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mNavigationViewSetup = new NavigationViewSetup(this, mDrawerLayout, mToolbar);
        mNavigationView = mNavigationViewSetup.setNavigationView();
        setSupportActionBar(mToolbar);

        handleIntent(getIntent());
    }
    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //use the query to search your data somehow
        }
    }
}
