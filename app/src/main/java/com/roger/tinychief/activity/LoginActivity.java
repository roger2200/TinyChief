package com.roger.tinychief.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
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
import com.roger.tinychief.volleymgr.NetworkManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import com.facebook.FacebookSdk;
//import com.google.android.gms.gcm.GoogleCloudMessaging;

public class LoginActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private Button button01;
    private Button  registerButton;
    private EditText input_ac, input_wd;
    private ProgressDialog mProgress;
    private ImageView img_login;
    CallbackManager callbackManager;
    private AccessToken accessToken;
    //private UserApplication uapp;

    private final String TAG="Login";

    private Listener<String> mResponseListener = new Listener<String>() {
        public void onResponse(String string) {
            try {
                JSONArray ary = new JSONArray(string);
                StringBuilder users = new StringBuilder();
                StringBuilder passwords = new StringBuilder();
                StringBuilder checkEmails = new StringBuilder();
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
                }
                TextView text1 = (TextView) findViewById(R.id.textView1);
                text1.setText(users.toString());
                TextView text2 = (TextView) findViewById(R.id.textView2);
                text2.setText(checkEmails.toString());

                if(text1.getText().equals(""))
                {
                    showMessage("wrong account or password");
                }
                if(text2.getText().equals("OK,"))
                {
                    showMessage("登入成功!");
                    navigationView = (NavigationView) findViewById(R.id.nav_view);
                    View mHeader=navigationView.getHeaderView(0);
                    TextView name = (TextView) mHeader.findViewById(R.id.mUserName);
                    name.setText(text1.getText());
                    SharedPreferences remdname=getPreferences(Activity.MODE_PRIVATE);
                    SharedPreferences.Editor edit=remdname.edit();
                    edit.putString("name", users.toString());
                    edit.putString("pass",passwords.toString());
                    edit.commit();
                    Log.d("error",name.toString());
                }
                if(text1.getText().length()>2&&text2.getText().equals("NO,")){
                    showMessage("要認證信箱唷");
                }
            }
            catch (Exception e)
            {
                Log.d("error:",e.getMessage());
            }
        }
    };

    private ErrorListener mErrorListener = new ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e("Error", error.toString());
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("登入");
        setToolbar();
        setNavigationView();
        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            //登入成功
            @Override
            public void onSuccess(LoginResult loginResult) {
                //accessToken之後或許還會用到 先存起來
                accessToken = loginResult.getAccessToken();
                Log.d("FB","access token got.");
                //send request and call graph api
                GraphRequest request = GraphRequest.newMeRequest(accessToken,new GraphRequest.GraphJSONObjectCallback() {
                            //當RESPONSE回來的時候
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                //讀出姓名 ID FB個人頁面連結
                                Log.d("FB","complete");
                                Log.d("FB",object.optString("name"));
                                Log.d("FB",object.optString("link"));
                                Log.d("FB",object.optString("id"));
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
                Log.d("FB","CANCEL");
                showMessage("Login Cancel");
            }

            //登入失敗
            @Override
            public void onError(FacebookException exception) {
                // App code

                Log.d("FB",exception.toString());
            }

        });
        input_ac = (EditText)findViewById(R.id.editText);
        input_wd = (EditText)findViewById(R.id.editText2);

        registerButton = (Button)findViewById(R.id.registerButton2);
        registerButton.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void onClickUpload(View view) {
        if (input_ac.getText().toString().isEmpty() || input_wd.getText().toString().isEmpty()) {
            showMessage("please fill all");
        } else {
            final String p;
            p = MD5.getMD5(input_wd.getText().toString());
            //下面這行是volley的語法,根據第一個參數,決定要執行甚麼工作,這裡是執行POST
            StringRequest request = new StringRequest(Request.Method.POST, "https://intense-oasis-69003.herokuapp.com/api/test", mResponseListener, mErrorListener) {
                //執行POST時,後面要加上要傳的資料,格式可以是json,ajax或是像下面的Map
                @Override
                public Map<String, String> getParams() {
                    Map<String, String> MyData = new HashMap<String, String>();
                    MyData.put("User", String.valueOf(input_ac.getText()));//用Map放資料,第一個參數是名稱,第二個是值,到時候在server端用名稱去取出值即可
                    MyData.put("Password", p);
                    return MyData;
                }
            };
            //這行是把剛才StringRequest裡的工作放入佇列當中,這是volley的語法被包在NetworkManager中
            NetworkManager.getInstance(this).request(null, request);
            getAccount();
        }

    }

    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void showMessage(String msg){
        Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();
    }

    private void jumpToActivity(Context ct,Class<?> lt){

        Intent intent = new Intent();
        intent.setClass(ct, lt);
        //startActivityForResult(intent,0);
        startActivity(intent);
    }

    private void getAccount() {
        StringRequest request2 = new StringRequest(Request.Method.GET, "https://intense-oasis-69003.herokuapp.com/api/test", mResponseListener, mErrorListener);
        NetworkManager.getInstance(this).request(null, request2);
        request2.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    private void setNavigationView(){
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        // Initializing Drawer Layout and ActionBarToggle
        drawerLayout = (DrawerLayout) findViewById(R.id.login);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.openDrawer, R.string.closeDrawer);
        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                if(!menuItem.isChecked()) menuItem.setChecked(true);
                drawerLayout.closeDrawers();
                switch (menuItem.getItemId()){
                    case R.id.nav_item_hot:
                        Toast.makeText(getApplicationContext(),"nav_item_hot",Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.nav_item_love:
                        Toast.makeText(getApplicationContext(),"nav_item_love",Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.nav_item_create:
                        Toast.makeText(getApplicationContext(),"nav_item_create",Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.nav_item_calendar:
                        Toast.makeText(getApplicationContext(),"nav_item_calendar",Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.nav_item_history:
                        Toast.makeText(getApplicationContext(),"nav_item_history",Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.nav_item_setting:
                        Toast.makeText(getApplicationContext(),"nav_item_setting",Toast.LENGTH_SHORT).show();
                        return true;
                    default:
                        Toast.makeText(getApplicationContext(),"Somethings Wrong",Toast.LENGTH_SHORT).show();
                        return true;
                }
            }
        });
    }
}
