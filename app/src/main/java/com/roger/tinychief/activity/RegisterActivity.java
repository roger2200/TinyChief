package com.roger.tinychief.activity;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.roger.tinychief.R;
import com.roger.tinychief.util.MD5;
import com.roger.tinychief.util.NetworkManager;
import com.roger.tinychief.widget.navigation.NavigationViewSetup;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private NavigationViewSetup mNavigationViewSetup;
    private EditText input_account, input_password, input_email,input_nickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setTitle("註冊");
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout_register);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        mNavigationViewSetup = new NavigationViewSetup(this, mDrawerLayout, mToolbar);
        mNavigationView = mNavigationViewSetup.setNavigationView();

        input_email = (EditText) findViewById(R.id.input_email);
        input_account = (EditText) findViewById(R.id.input_ac2);
        input_password = (EditText) findViewById(R.id.input_wd2);
        input_nickname = (EditText) findViewById(R.id.edittxt_nickname);
    }

    private Listener<String> mResponseListener = new Listener<String>() {
        @Override
        public void onResponse(String string) {
            try {
            } catch (Exception e) {
                Log.d("onResponse Error", e.getMessage());
            }
        }
    };

    private ErrorListener mErrorListener = new ErrorListener() {

        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e("Error", error.toString());
        }
    };

    public void onClickToRegister(View v) {
        final String p;
        p = MD5.getMD5(input_password.getText().toString());
        //下面這行是volley的語法,根據第一個參數,決定要執行甚麼工作,這裡是執行POST
        StringRequest request = new StringRequest(Request.Method.POST, "https://tiny-chief.herokuapp.com/register",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String string) {
                        try {
                            JSONArray ary = new JSONArray(string);
                            StringBuilder users = new StringBuilder();
                            StringBuilder passwords = new StringBuilder();
                            StringBuilder emails = new StringBuilder();
                            for (int i = 0; i < ary.length(); i++) {
                                JSONObject json = ary.getJSONObject(i);
                                String user = json.getString("user");
                                users.append(user);
                                users.append(",");
                                String password = json.getString("password");
                                passwords.append(password);
                                passwords.append(",");
                                String email = json.getString("email");
                                emails.append(email);
                                emails.append(",");
                            }

                            if (users.toString().length() > 3) {
                                showMessage("帳號已被使用!");
                            } else {
                                showMessage("請去認證信箱！");
                                sendemail();
                                checkVerified();
                                finish();
                            }
                        } catch (Exception e) {
                            Log.d("error:", e.getMessage());
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
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("User", String.valueOf(input_account.getText()));//用Map放資料,第一個參數是名稱,第二個是值,到時候在server端用名稱去取出值即可
                MyData.put("Password", p);
                MyData.put("myEmail", String.valueOf(input_email.getText()));
                MyData.put("nickName", String.valueOf(input_nickname.getText()));
                return MyData;
            }
        };
        NetworkManager.getInstance(this).request(null, request);
        request.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private void showMessage(String msg) {
        Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_LONG).show();
    }

    private void sendemail() {
        StringRequest request3 = new StringRequest(Request.Method.POST, "https://tiny-chief.herokuapp.com/send",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String string) {
                        try {

                        } catch (Exception e) {
                            Log.d("error:", e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.e("Error", String.valueOf(volleyError));
                    }
                });
        NetworkManager.getInstance(this).request(null, request3);
        request3.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private void checkVerified() {
        StringRequest request = new StringRequest(Request.Method.POST, "https://tiny-chief.herokuapp.com/verify",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String string) {
                        try {

                        } catch (Exception e) {
                            Log.d("error:", e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.e("Error", String.valueOf(volleyError));
                    }
                });
        NetworkManager.getInstance(this).request(null, request);
        request.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }
}
