package com.roger.tinychief.widget.navigation;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.roger.tinychief.R;
import com.roger.tinychief.activity.CreateActivity;
import com.roger.tinychief.activity.DetailActivity;
import com.roger.tinychief.activity.LoginActivity;
import com.roger.tinychief.activity.MainActivity;

/**
 * Created by Roger on 7/27/2016.
 */
public class NavigationViewSetup {

    private AppCompatActivity mActivity;
    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    private Toolbar mToolbar;

    public NavigationViewSetup(AppCompatActivity activity, DrawerLayout drawerLayout, Toolbar toolbar) {
        this.mActivity = activity;
        this.mDrawerLayout = drawerLayout;
        this.mToolbar = toolbar;
    }

    public NavigationView setNavigationView() {
        mActivity.setSupportActionBar(mToolbar);
        mNavigationView = (android.support.design.widget.NavigationView) mActivity.findViewById(R.id.nav_view);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(mActivity, mDrawerLayout, mToolbar, R.string.openDrawer, R.string.closeDrawer);
        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
        View mHeader=mNavigationView.getHeaderView(0);
        Button headerLogin = (Button)mHeader.findViewById(R.id.headerLoginButton);
        headerLogin.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(),LoginActivity.class);
                mActivity.startActivity(intent);
            }

        });
        mNavigationView.setNavigationItemSelectedListener(new android.support.design.widget.NavigationView.OnNavigationItemSelectedListener() {
            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                if (!menuItem.isChecked()) menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();
                switch (menuItem.getItemId()) {
                    case R.id.nav_item_hot:
                        Toast.makeText(mActivity.getApplicationContext(), "nav_item_hot", Toast.LENGTH_SHORT).show();
                        jumpToActivity(mActivity, MainActivity.class);
                        return true;
                    case R.id.nav_item_love:
                        Toast.makeText(mActivity.getApplicationContext(), "nav_item_love", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.nav_item_create:
                        Toast.makeText(mActivity.getApplicationContext(), "nav_item_create", Toast.LENGTH_SHORT).show();
                        jumpToActivity(mActivity, CreateActivity.class);
                        return true;
                    case R.id.nav_item_calendar:
                        Toast.makeText(mActivity.getApplicationContext(), "nav_item_calendar", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.nav_item_history:
                        Toast.makeText(mActivity.getApplicationContext(), "nav_item_history", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.nav_item_setting:
                        Toast.makeText(mActivity.getApplicationContext(), "nav_item_setting", Toast.LENGTH_SHORT).show();
                        return true;
                    default:
                        Toast.makeText(mActivity.getApplicationContext(), "Somethings Wrong", Toast.LENGTH_SHORT).show();
                        return true;
                }
            }
        });
        return mNavigationView;
    }

    private void jumpToActivity(Context ct, Class lt) {
        Intent intent = new Intent();
        intent.setClass(ct, lt);
        //startActivityForResult(intent,0);
        mActivity.startActivity(intent);
    }
}
