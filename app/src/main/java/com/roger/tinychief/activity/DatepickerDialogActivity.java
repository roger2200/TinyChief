package com.roger.tinychief.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.roger.tinychief.R;
import com.roger.tinychief.util.NetworkManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
/**懸浮視窗-日期選擇*/
public class DatepickerDialogActivity extends AppCompatActivity {
    private CalendarView mCalendarView;
    private TextView mTextView;
    private Spinner mSpinner;
    private ProgressDialog mProgressDialog;
    private int mIntYear, mIntMonth, mIntDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datepicker);

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int)(size.x / 1.1), (int)(size.y / 1.5));

        mCalendarView = (CalendarView) findViewById(R.id.calendarview_datepicker);
        mTextView = (TextView) findViewById(R.id.txtview_date_datepicker);
        mSpinner = (Spinner) findViewById(R.id.spinner_time_datepicker);

        mCalendarView.setLayoutParams(params);

        final Calendar calendar = Calendar.getInstance();
        mIntYear = calendar.get(Calendar.YEAR);
        mIntMonth = calendar.get(Calendar.MONTH);
        mIntDay = calendar.get(Calendar.DAY_OF_MONTH);

        ArrayList<String> spinitem = new ArrayList<>(Arrays.asList("早餐", "中餐", "晚餐"));
        ArrayAdapter adapter = new ArrayAdapter<>(DatepickerDialogActivity.this, android.R.layout.simple_spinner_item, spinitem);
        mSpinner.setAdapter(adapter);

        mCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth) {
                mTextView.setText(year + "." + (month + 1) + "." + dayOfMonth);
                mIntYear = year;
                mIntMonth = month;
                mIntDay = dayOfMonth;
            }
        });
        mTextView.setText(new SimpleDateFormat("yyyy.MM.dd", Locale.TAIWAN).format(mCalendarView.getDate()));
        mCalendarView.setMinDate(mCalendarView.getDate());

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("加入日曆中...");
    }

    public void addCalendar(View view) {
        mProgressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, "https://tiny-chief.herokuapp.com/calendar/upload",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String string) {
                        Intent intent = new Intent();
                        intent.putExtra("RESULT", true);
                        setResult(RESULT_OK, intent);
                        mProgressDialog.dismiss();
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.e("Error", String.valueOf(volleyError));
                        Intent intent = new Intent();
                        intent.putExtra("RESULT", false);
                        setResult(RESULT_OK, intent);
                        mProgressDialog.dismiss();
                        finish();
                    }
                }) {
            @Override
            public Map<String, String> getParams() {
                Map<String, String> param = new HashMap<>();
                param.put("id_cb", DatepickerDialogActivity.this.getIntent().getExtras().getString("ID"));
                param.put("id_usr", MainActivity.USER_ID);
                param.put("year", String.valueOf(mIntYear));
                param.put("month", String.valueOf(mIntMonth));
                param.put("day", String.valueOf(mIntDay));
                param.put("time", mSpinner.getSelectedItem().toString());
                param.put("title", DatepickerDialogActivity.this.getIntent().getExtras().getString("TITLE"));
                Log.d("Upload Calendar", param.toString());
                return param;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        NetworkManager.getInstance(this).request(null, request);
    }

    public void Cancel(View view) {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }
}
