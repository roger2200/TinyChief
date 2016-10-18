package com.roger.tinychief.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.roger.tinychief.R;
import com.roger.tinychief.util.NetworkManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class ValuationActivity extends AppCompatActivity {
    private String[] mStrArrayIiN, mStrArrayIiA;
    private LinearLayout mLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_valuation);
        Bundle bundle = this.getIntent().getExtras();
        mLinearLayout = (LinearLayout) findViewById(R.id.linearlayout_valuation);
        mStrArrayIiN = bundle.getStringArray("NAME");
        mStrArrayIiA = bundle.getStringArray("AMOUNT");

        addIi();
    }

    private void addIi() {
        for (int i = 0; i < mStrArrayIiN.length; i++) {
            TextView txtviewName = new TextView(this);
            TextView txtviewAmount = new TextView(this);
            Spinner spinner = new Spinner(this);
            LinearLayout linearlayout = new LinearLayout(this);
            final ArrayList<String> strSpinItem = new ArrayList<>();

            ArrayAdapter adapter = new ArrayAdapter<String>(ValuationActivity.this, android.R.layout.simple_spinner_item, strSpinItem);
            spinner.setAdapter(adapter);

            txtviewName.setText(mStrArrayIiN[i]);
            txtviewAmount.setText(mStrArrayIiA[i]);
            linearlayout.addView(txtviewName);
            linearlayout.addView(txtviewAmount);
            linearlayout.addView(spinner);
            mLinearLayout.addView(linearlayout);

            char[] charArrayName = mStrArrayIiN[i].toCharArray();
            for (final char c : charArrayName) {
                if (c == '花') break;
                StringRequest request = new StringRequest(Request.Method.GET, "http://m.coa.gov.tw/OpenData/FarmTransData.aspx?" + "Crop=" + c,
                        new Response.Listener<String>() {
                            public void onResponse(String string) {
                                try {
                                    JSONArray jsonArray = new JSONArray(string);
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        String str = jsonArray.getJSONObject(i).getString("作物名稱");
                                        if (!strSpinItem.contains(str))
                                            strSpinItem.add(str);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("Error", error.getMessage());
                            }
                        });
                NetworkManager.getInstance(this).request(null, request);
            }
        }
    }
}
