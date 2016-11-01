package com.roger.tinychief.widget.navigation;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.roger.tinychief.R;
import com.roger.tinychief.activity.CreateActivity;
import com.roger.tinychief.activity.LoginActivity;
import com.roger.tinychief.activity.MainActivity;
import com.roger.tinychief.util.MyHelper;

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
        View header = mNavigationView.getHeaderView(0);
        Button headerLogin = (Button) header.findViewById(R.id.btn_login_header);
        if (MainActivity.USER_NAME != null) {
            TextView name = (TextView) header.findViewById(R.id.txtview_name_header);
            name.setText(MainActivity.USER_NAME);
            headerLogin.setVisibility(View.INVISIBLE);
        }
        headerLogin.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), LoginActivity.class);
                mDrawerLayout.closeDrawers();
                mActivity.startActivity(intent);
            }

        });
        mNavigationView.setNavigationItemSelectedListener(new android.support.design.widget.NavigationView.OnNavigationItemSelectedListener() {
            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                mDrawerLayout.closeDrawers();
                switch (menuItem.getItemId()) {
                    case R.id.nav_item_hot:
                        jumpToActivity(mActivity, MainActivity.class,menuItem);
                        return true;
                    case R.id.nav_item_create:
                        if (MainActivity.USER_NAME == null)
                            Toast.makeText(mActivity, "請先登入", Toast.LENGTH_SHORT).show();
                        else
                            jumpToActivity(mActivity, CreateActivity.class,menuItem);
                        return true;
                    case R.id.nav_item_calendar:
                        return true;
                    case R.id.nav_item_setting:
                        return true;
                    default:
                        return true;
                }
            }
        });
        return mNavigationView;
    }

    private void jumpToActivity(Context ct, Class lt,MenuItem menuItem) {
        Intent intent = new Intent();
        intent.setClass(ct, lt);
        mActivity.startActivity(intent);
    }
}
