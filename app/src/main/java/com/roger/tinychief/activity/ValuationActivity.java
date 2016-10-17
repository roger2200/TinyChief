package com.roger.tinychief.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.roger.tinychief.R;

public class ValuationActivity extends AppCompatActivity {
    private String[] mStrArrayIi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_valuation);
        Bundle bundle = this.getIntent().getExtras();
        mStrArrayIi = bundle.getStringArray("II");
    }
}
