package com.roger.tinychief.activity;

import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.CalendarView;
import android.widget.TextView;

import com.roger.tinychief.R;
import com.roger.tinychief.widget.navigation.NavigationViewSetup;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    private CoordinatorLayout mCoordinatorLayout;
    private TextView mTextView;
    private CalendarView mCalendarView;
    private NavigationViewSetup mNavigationViewSetup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorlayout_calendar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout_calendar);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTextView = (TextView) findViewById(R.id.txtview_date_calendar);
        mCalendarView = (CalendarView) findViewById(R.id.calendarview_calendar);

        mNavigationViewSetup = new NavigationViewSetup(this, mDrawerLayout, mToolbar);
        mNavigationView = mNavigationViewSetup.setNavigationView();
        mNavigationView.getMenu().getItem(2).setChecked(true);

        mCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView calendarView, int year, int month, int dayOfMonth) {
                mTextView.setText(year + "." + (month + 1) + "." + dayOfMonth);
            }
        });

        mTextView.setText(new SimpleDateFormat("yyyy.MM.dd", Locale.TAIWAN).format(mCalendarView.getDate()));

    }

    @Override
    protected void onResume() {
        super.onResume();
        mNavigationView = mNavigationViewSetup.setNavigationView();
        mNavigationView.getMenu().getItem(2).setChecked(true);
    }
}
