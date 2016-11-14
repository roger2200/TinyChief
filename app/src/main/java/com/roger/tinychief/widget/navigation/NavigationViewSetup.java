package com.roger.tinychief.widget.navigation;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.roger.tinychief.R;
import com.roger.tinychief.activity.CalendarActivity;
import com.roger.tinychief.activity.CreateActivity;
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
    private TextView mTextView;
    private View mView;
    private Button mButton;

    public NavigationViewSetup(AppCompatActivity activity, DrawerLayout drawerLayout, Toolbar toolbar) {
        this.mActivity = activity;
        this.mDrawerLayout = drawerLayout;
        this.mToolbar = toolbar;

        mNavigationView = (NavigationView) mActivity.findViewById(R.id.nav_view);
        mView = mNavigationView.getHeaderView(0);
        mButton = (Button) mView.findViewById(R.id.btn_login_header);
        mTextView = (TextView) mView.findViewById(R.id.txtview_name_header);
    }

    public NavigationView setNavigationView() {
        mActivity.setSupportActionBar(mToolbar);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(mActivity, mDrawerLayout, mToolbar, R.string.drawer_close, R.string.drawer_open);
        actionBarDrawerToggle.syncState();
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);

        setButton();

        mNavigationView.setNavigationItemSelectedListener(new android.support.design.widget.NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                mDrawerLayout.closeDrawers();
                switch (menuItem.getItemId()) {
                    case R.id.nav_item_hot:
                        jumpToActivity(mActivity, MainActivity.class);
                        return true;
                    case R.id.nav_item_create:
                        if (MainActivity.USER_NAME == null)
                            Toast.makeText(mActivity, "請先登入", Toast.LENGTH_SHORT).show();
                        else
                            jumpToActivity(mActivity, CreateActivity.class);
                        return true;
                    case R.id.nav_item_calendar:
                        if (MainActivity.USER_NAME == null)
                            Toast.makeText(mActivity, "請先登入", Toast.LENGTH_SHORT).show();
                        else
                            jumpToActivity(mActivity, CalendarActivity.class);
                        return true;
                    default:
                        return false;
                }
            }
        });
        return mNavigationView;
    }

    private void jumpToActivity(Context ct, Class lt) {
        Intent intent = new Intent();
        intent.setClass(ct, lt);
        mActivity.startActivity(intent);
    }

    private void setButton() {
        if (MainActivity.USER_NAME == null) {
            mButton.setText("登入");
            mButton.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), LoginActivity.class);
                    mDrawerLayout.closeDrawers();
                    mActivity.startActivity(intent);
                }
            });
        } else {
            mTextView.setText(MainActivity.USER_NAME);
            mButton.setText("登出");
            mButton.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LoginManager.getInstance().logOut();
                    v.getContext().deleteFile("tf_login_data");
                    MainActivity.USER_NAME = null;
                    MainActivity.USER_ID = null;
                    mTextView.setText("未登入");
                    mDrawerLayout.closeDrawers();
                    jumpToActivity(mActivity, MainActivity.class);
                    Toast.makeText(mActivity, "已登出", Toast.LENGTH_SHORT).show();
                    setButton();
                }
            });
        }
    }
}
