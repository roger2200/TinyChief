package com.roger.tinychief.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.roger.tinychief.R;

public class SplashActivity extends AppCompatActivity {

    private static long SPLASH_MILLIS = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //顯示歡迎畫面3秒鐘
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();//讓使用者就算按了返回鍵也回不來這畫面
            }
        }, SPLASH_MILLIS);
    }
}
