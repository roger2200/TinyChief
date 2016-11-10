package com.roger.tinychief.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.Spinner;
import android.widget.TextView;

import com.roger.tinychief.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class DatepickerDialogActivity extends AppCompatActivity {
    private CalendarView mCalendarView;
    private TextView mTextView;
    private Spinner mSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datepicker);

        mCalendarView = (CalendarView) findViewById(R.id.calendarview_datepicker);
        mTextView = (TextView) findViewById(R.id.txtview_date_datepicker);
        mSpinner = (Spinner) findViewById(R.id.spinner_time_datepicker);

        ArrayList<String> spinitem = new ArrayList<>(Arrays.asList("早餐", "中餐", "晚餐"));
        ArrayAdapter adapter = new ArrayAdapter<>(DatepickerDialogActivity.this, android.R.layout.simple_spinner_item, spinitem);
        mSpinner.setAdapter(adapter);

        mCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth) {
                mTextView.setText(year + "." + (month + 1) + "." + dayOfMonth);
            }
        });
        mTextView.setText(new SimpleDateFormat("yyyy.MM.dd", Locale.TAIWAN).format(mCalendarView.getDate()));
    }

    public void addCalendar(View view) {
        Intent intent = new Intent();
        intent.putExtra("RESULT", true);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void Cancel(View view) {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }
}
