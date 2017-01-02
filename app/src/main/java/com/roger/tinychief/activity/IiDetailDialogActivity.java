package com.roger.tinychief.activity;

import android.content.Intent;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.roger.tinychief.R;
import com.roger.tinychief.util.NetworkManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;
/**懸浮視窗-食材價格*/
public class IiDetailDialogActivity extends AppCompatActivity {
    private final String TAG = "IiDetailDialogActivity";

    private TextView mNameTextView, mPriceTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ii_detail_dialog);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = size.y / 3;
        params.width = (int) (size.x / 1.2);

        this.getWindow().setAttributes(params);

        mNameTextView = (TextView) findViewById(R.id.txtview_name_detail_dialog);
        mPriceTextView = (TextView) findViewById(R.id.txtview_price_detail_dialog);

        getPrice();
    }

    public void onClickBuy(View view) {
        Bundle bundle = this.getIntent().getExtras();
        String strName = bundle.getString("NAME");

        Intent intent = new Intent(view.getContext(), WebviewActivity.class);
        intent.putExtra("URL", "http://www.happy-shopping.com.tw/search_list.aspx?k=" + strName.split("-")[0]);
        startActivity(intent);
    }

    public void onClickEnd(View view) {
        finish();
    }

    private void getPrice() {
        Bundle bundle = this.getIntent().getExtras();
        final String strName = bundle.getString("NAME"), strURL;
        int intClass = bundle.getInt("CLASS");
        StringRequest request = null;

        mNameTextView.setText(strName);

        switch (intClass) {
            case 0:
                strURL = "http://m.coa.gov.tw/OpenData/PoultryTransBoiledChickenData.aspx";
                request = new StringRequest(Request.Method.GET, strURL,
                        new Response.Listener<String>() {
                            public void onResponse(String string) {
                                try {
                                    JSONArray jsonArray = new JSONArray(string);
                                    if (strName.equals("雞蛋"))
                                        mPriceTextView.setText(jsonArray.getJSONObject(0).getString("雞蛋(產地)") + "元/台斤");
                                    else
                                        mPriceTextView.setText(jsonArray.getJSONObject(0).getString("白肉雞(門市價高屏)") + "元/台斤");
                                    Log.d("Response", string);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(TAG, error.toString());
                            }
                        });
                break;
            case 1:
                strURL = "http://m.coa.gov.tw/OpenData/PoultryTransGooseDuckData.aspx";
                request = new StringRequest(Request.Method.GET, strURL,
                        new Response.Listener<String>() {
                            public void onResponse(String string) {
                                try {
                                    JSONArray jsonArray = new JSONArray(string);
                                    switch (strName) {
                                        case "鴨蛋":
                                            mPriceTextView.setText(jsonArray.getJSONObject(0).getString("鴨蛋(新蛋)(台南)") + "元/台斤");
                                            break;
                                        case "鴨肉":
                                            mPriceTextView.setText(jsonArray.getJSONObject(0).getString("正番鴨(公)") + "元/台斤");
                                            break;
                                        default:
                                            mPriceTextView.setText(jsonArray.getJSONObject(0).getString("肉鵝(白羅曼)") + "元/台斤");
                                            break;
                                    }
                                    Log.d("Response", string);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(TAG, error.toString());
                            }
                        });
                break;
            case 2:
                strURL = "https://tinny-chief.herokuapp.com/get/price";
                request = new StringRequest(Request.Method.GET, strURL,
                        new Response.Listener<String>() {
                            public void onResponse(String string) {
                                try {
                                    JSONObject jsonObject = new JSONObject(string);
                                    if (strName.equals("豬肉"))
                                        mPriceTextView.setText(jsonObject.getString("pork") + "元/台斤");
                                    else
                                        mPriceTextView.setText(jsonObject.getString("beef") + "元/台斤");
                                    Log.d("Response", string);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(TAG, error.toString());
                            }
                        });
                break;
            case 3:
                mPriceTextView.setText("無資料");
                break;
            case 4:
                Calendar calendar = Calendar.getInstance();
                int intYear1 = calendar.get(Calendar.YEAR) - 1911;
                CharSequence strDay1 = DateFormat.format(".MM.dd", calendar);
                calendar.add(Calendar.MONTH, -1);
                int intYear2 = calendar.get(Calendar.YEAR) - 1911;
                CharSequence strDay2 = DateFormat.format(".MM.dd", calendar);

                strURL = "http://m.coa.gov.tw/OpenData/FarmTransData.aspx?"
                        + "StartDate=" + intYear2 + strDay2 + "&EndDate=" + intYear1 + strDay1 + "&Crop=" + strName;
                request = new StringRequest(Request.Method.GET, strURL,
                        new Response.Listener<String>() {
                            public void onResponse(String string) {
                                try {
                                    JSONArray jsonArray = new JSONArray(string);
                                    mPriceTextView.setText("無資料");
                                    for (int i = 0; i < jsonArray.length(); i++)
                                        if (!jsonArray.getJSONObject(i).getString("平均價").equals("0")) {
                                            mPriceTextView.setText(jsonArray.getJSONObject(i).getString("平均價") + "元/公斤");
                                            break;
                                        }
                                    Log.d("Response", string);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(TAG, error.toString());
                            }
                        });
                break;
        }
        if (request != null) {
            Log.d("Get data from ", request.getUrl());
            request.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            NetworkManager.getInstance(IiDetailDialogActivity.this).request("official", request);
        }
    }
}
