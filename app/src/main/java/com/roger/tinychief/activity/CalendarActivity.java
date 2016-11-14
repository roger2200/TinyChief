package com.roger.tinychief.activity;

import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.roger.tinychief.R;
import com.roger.tinychief.util.NetworkManager;
import com.roger.tinychief.widget.navigation.NavigationViewSetup;


import org.json.JSONArray;

import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CalendarActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    private CoordinatorLayout mCoordinatorLayout;
    private LinearLayout mLinearLayoutMorning, mLinearLayoutNoon, mLinearLayoutNight;
    private TextView mTextView;
    private CalendarView mCalendarView;
    private NavigationViewSetup mNavigationViewSetup;
    private JSONArray mJSONArray;
    private Date mDateCurrentSelect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorlayout_calendar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout_calendar);
        mLinearLayoutMorning = (LinearLayout) findViewById(R.id.linearlayout_morning_calendar);
        mLinearLayoutNoon = (LinearLayout) findViewById(R.id.linearlayout_noon_calendar);
        mLinearLayoutNight = (LinearLayout) findViewById(R.id.linearlayout_night_calendar);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTextView = (TextView) findViewById(R.id.txtview_date_calendar);
        mCalendarView = (CalendarView) findViewById(R.id.calendarview_calendar);

        mNavigationViewSetup = new NavigationViewSetup(this, mDrawerLayout, mToolbar);
        mNavigationView = mNavigationViewSetup.setNavigationView();
        mNavigationView.getMenu().getItem(2).setChecked(true);

        mCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth) {
                mTextView.setText(year + "." + (month + 1) + "." + dayOfMonth);

                mLinearLayoutMorning.removeAllViews();
                mLinearLayoutNoon.removeAllViews();
                mLinearLayoutNight.removeAllViews();

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                mDateCurrentSelect = calendar.getTime();
                checkDate();
            }
        });

        mTextView.setText(new SimpleDateFormat("yyyy.MM.dd", Locale.TAIWAN).format(mCalendarView.getDate()));
        mDateCurrentSelect = new Date(mCalendarView.getDate());

        getData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNavigationView = mNavigationViewSetup.setNavigationView();
        mNavigationView.getMenu().getItem(2).setChecked(true);
    }

    private void getData() {
        StringRequest request = new StringRequest(Request.Method.POST, "https://tinny-chief.herokuapp.com/calendar/get",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String string) {
                        try {
                            mJSONArray = new JSONArray(string);
                            checkDate();
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
                Map<String, String> MyData = new HashMap<>();
                MyData.put("id", MainActivity.USER_ID);
                return MyData;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        NetworkManager.getInstance(this).request(null, request);
    }

    private void checkDate() {
        SimpleDateFormat simpleDf = new SimpleDateFormat("yyyy-MM-dd", Locale.TAIWAN);
        String strDate = simpleDf.format(mDateCurrentSelect);
        boolean haveMorning = false, haveNoon = false, haveNight = false;
        try {
            if (mJSONArray != null)
                for (int i = 0; i < mJSONArray.length(); i++) {
                    Date jsonDate = simpleDf.parse(mJSONArray.getJSONObject(i).getString("date"));
                    if (simpleDf.format(jsonDate).equals(strDate)) {
                        TextView txtview = new TextView(CalendarActivity.this);
                        txtview.setText(mJSONArray.getJSONObject(i).getString("title"));
                        switch (mJSONArray.getJSONObject(i).getString("time")) {
                            case "早餐":
                                if (!haveMorning) {
                                    TextView txtviewT = new TextView(CalendarActivity.this);
                                    txtviewT.setText("早餐");
                                    mLinearLayoutMorning.addView(txtviewT);
                                    haveMorning = true;
                                }
                                mLinearLayoutMorning.addView(txtview);
                                break;
                            case "中餐":
                                if (!haveNoon) {
                                    TextView txtviewT = new TextView(CalendarActivity.this);
                                    txtviewT.setText("中餐");
                                    mLinearLayoutNoon.addView(txtviewT);
                                    haveNoon = true;
                                }
                                mLinearLayoutNoon.addView(txtview);
                                break;
                            case "晚餐":
                                if (!haveNight) {
                                    TextView txtviewT = new TextView(CalendarActivity.this);
                                    txtviewT.setText("晚餐");
                                    mLinearLayoutNight.addView(txtviewT);
                                    haveNight = true;
                                }
                                mLinearLayoutNight.addView(txtview);
                                break;
                        }
                    }
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
