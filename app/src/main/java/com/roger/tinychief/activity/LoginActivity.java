package com.roger.tinychief.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.roger.tinychief.R;
import com.roger.tinychief.util.MD5;
import com.roger.tinychief.util.NetworkManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.facebook.FacebookSdk;
import com.roger.tinychief.widget.navigation.NavigationViewSetup;
//import com.google.android.gms.gcm.GoogleCloudMessaging;

public class LoginActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    private Button mRegisterButton;
    private NavigationViewSetup mNavigationViewSetup;
    private EditText input_ac, input_wd;
    CallbackManager callbackManager;
    private AccessToken accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("登入");

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout_login);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        mNavigationViewSetup = new NavigationViewSetup(this, mDrawerLayout, mToolbar);
        mNavigationView = mNavigationViewSetup.setNavigationView();

        callbackManager = CallbackManager.Factory.create();
        //FB登入的按鈕
        LoginButton loginFbBtn = (LoginButton) findViewById(R.id.login_button_fb);
        loginFbBtn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            //登入成功
            @Override
            public void onSuccess(LoginResult loginResult) {
                //accessToken之後或許還會用到 先存起來
                accessToken = loginResult.getAccessToken();
                Log.d("FB", "access token got.");
                //send request and call graph api
                GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                    //當RESPONSE回來的時候
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        //讀出姓名 ID FB個人頁面連結
                        Log.d("FB", object.optString("name"));
                        Log.d("FB", object.optString("link"));
                        Log.d("FB", object.optString("id"));
                        MainActivity.USER_NAME = object.optString("name");
                        MainActivity.USER_ID = object.optString("id");
                        insertFBInfo();
                        //將登入資料寫入手機記憶體
                        String strLoginData = MainActivity.USER_NAME + "," + MainActivity.USER_ID;
                        try {
                            FileOutputStream fos = openFileOutput("tf_login_data", Context.MODE_PRIVATE);
                            fos.write(strLoginData.getBytes());
                            fos.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        finish();
                    }
                });
                //包入你想要得到的資料 送出request
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,link");
                request.setParameters(parameters);
                request.executeAsync();
            }

            //登入取消
            @Override
            public void onCancel() {
                // App code
                Log.d("FB", "CANCEL");
                Toast.makeText(LoginActivity.this, "Login Cancel", Toast.LENGTH_LONG).show();
            }

            //登入失敗
            @Override
            public void onError(FacebookException exception) {
                // App code
                Log.d("FB", exception.toString());
            }

        });
        input_ac = (EditText) findViewById(R.id.edittxt_account_login);
        input_wd = (EditText) findViewById(R.id.edittxt_password_login);
        //按下註冊鈕
        mRegisterButton = (Button) findViewById(R.id.btn_register_login);
        mRegisterButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                //跳轉到RegisterActivity
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void onClickUpload(View view) {
        if (input_ac.getText().toString().isEmpty() || input_wd.getText().toString().isEmpty())
            Toast.makeText(LoginActivity.this, "請輸入完整", Toast.LENGTH_LONG).show();
        else {
            final String p;
            //MD5非對稱加密
            p = MD5.getMD5(input_wd.getText().toString());
            //下面這行是volley的語法,根據第一個參數,決定要執行甚麼工作,這裡是執行POST
            StringRequest request = new StringRequest(Request.Method.POST, "https://tiny-chief.herokuapp.com/login",
                    new Response.Listener<String>() {
                        //處理傳回來的JSON字串
                        @Override
                        public void onResponse(String string) {
                            try {
                                JSONArray ary = new JSONArray(string);
                                StringBuilder users = new StringBuilder();
                                StringBuilder passwords = new StringBuilder();
                                StringBuilder checkEmails = new StringBuilder();
                                String nickname = "";
                                String userID = "";
                                //將資料一一拆解並放入變數裡
                                for (int i = 0; i < ary.length(); i++) {
                                    JSONObject json = ary.getJSONObject(i);
                                    String user = json.getString("user");
                                    users.append(user);
                                    users.append(",");
                                    String password = json.getString("password");
                                    passwords.append(password);
                                    passwords.append(",");
                                    String checkEmail = json.getString("checkEmail");
                                    checkEmails.append(checkEmail);
                                    checkEmails.append(",");
                                    nickname = json.getString("nickname");
                                    userID = json.getString("_id");
                                }
                                //若為空代表server找不到資料
                                if (users.toString().equals("")) {
                                    Toast.makeText(LoginActivity.this, "帳號或密碼輸入錯誤", Toast.LENGTH_LONG).show();
                                }
                                if (checkEmails.toString().equals("OK,")) {
                                    Toast.makeText(LoginActivity.this, "登入成功", Toast.LENGTH_LONG).show();
                                    //跳轉頁面
                                    Intent intent = new Intent();
                                    intent.setClass(LoginActivity.this, MainActivity.class);
                                    Log.d("name", nickname);
                                    Log.d("ID", userID);
                                    //將使用者資料放在static變數以便日後存取
                                    MainActivity.USER_ID = userID;
                                    MainActivity.USER_NAME = nickname;
                                    insertFBInfo();
                                    //將登入資料寫入手機記憶體
                                    String strLoginData = MainActivity.USER_NAME + "," + MainActivity.USER_ID;
                                    FileOutputStream fos = openFileOutput("tf_login_data", Context.MODE_PRIVATE);
                                    fos.write(strLoginData.getBytes());
                                    fos.close();

                                    finish();
                                }
                                //還沒認證信箱
                                if (users.toString().length() > 2 && checkEmails.toString().equals("NO,")) {
                                    Toast.makeText(LoginActivity.this, "要認證信箱唷", Toast.LENGTH_LONG).show();
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
                //將欲傳送之資料放入MyData
                @Override
                public Map<String, String> getParams() {
                    Map<String, String> MyData = new HashMap<>();
                    MyData.put("User", String.valueOf(input_ac.getText()));//用Map放資料,第一個參數是名稱,第二個是值,到時候在server端用名稱去取出值即可
                    MyData.put("Password", p);
                    return MyData;
                }
            };
            NetworkManager.getInstance(this).request(null, request);
            request.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        }
    }

    public void onClickCancel(View view) {
        finish();
    }
    //插入臉書資料
    private void insertFBInfo() {

        StringRequest request3 = new StringRequest(Request.Method.POST, "https://tiny-chief.herokuapp.com/inserFBInfo",
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
                }) {
            @Override
            public Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("UserID", MainActivity.USER_ID);//用Map放資料,第一個參數是名稱,第二個是值,到時候在server端用名稱去取出值即可
                MyData.put("FBName", MainActivity.USER_NAME);
                return MyData;
            }
        };
        NetworkManager.getInstance(this).request(null, request3);
        request3.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }
}
