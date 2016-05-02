package com.roger.tinychief.activity;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
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
import android.widget.TextView;
import android.widget.Toast;

import com.roger.tinychief.R;

import java.util.Locale;

public class CookActivity extends AppCompatActivity implements OnInitListener {
    private Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    private TextView inputText1 = null;
    private Button speakBtn1 = null;
    private TextView inputText2 = null;
    private Button speakBtn2 = null;
    private static final int REQ_TTS_STATUS_CHECK = 0;
    private static final String TAG = "TTS Demo";
    private TextToSpeech mTts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cook);

        setTitle("開始料理");
        setToolbar();
        setNavigationView();

        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, REQ_TTS_STATUS_CHECK);

        speakBtn1 = (Button)findViewById(R.id.button_1);
        inputText1 = (TextView)findViewById(R.id.text_view);
        speakBtn1.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                mTts.speak(inputText1.getText().toString(), TextToSpeech.QUEUE_ADD, null);
                //朗讀輸入框裏的內容
            }
        });
        speakBtn2 = (Button)findViewById(R.id.button_2);
        inputText2 = (TextView)findViewById(R.id.edit_text);
        speakBtn2.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                mTts.speak(inputText2.getText().toString(), TextToSpeech.QUEUE_ADD, null);
                //朗讀輸入框裏的內容
            }
        });
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    //初始化TTS
    @Override
    public void onInit(int status) {
        //TTS Engine初始化完成
        if(status == TextToSpeech.SUCCESS)
        {
            int result = mTts.setLanguage(Locale.CHINESE);
            //設置發音語言
            if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
            //判斷語言是否可用
            {
                Log.v(TAG, "Language is not available");
            }
            else
            {
                mTts.speak("語音功能初始化成功.", TextToSpeech.QUEUE_ADD, null);
            }
        }
    }


    protected  void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQ_TTS_STATUS_CHECK)
        {
            switch (resultCode) {
                case TextToSpeech.Engine.CHECK_VOICE_DATA_PASS:
                    //這個返回結果表明TTS Engine可以用
                {
                    mTts = new TextToSpeech(this, this);
                    Log.v(TAG, "TTS Engine is installed!");

                }

                break;
                case TextToSpeech.Engine.CHECK_VOICE_DATA_BAD_DATA:
                    //需要的語音數據已損壞
                case TextToSpeech.Engine.CHECK_VOICE_DATA_MISSING_DATA:
                    //缺少需要語言的語音數據
                case TextToSpeech.Engine.CHECK_VOICE_DATA_MISSING_VOLUME:
                    //缺少需要語言的發音數據
                {
                    //這三種情況都表明數據有錯,重新下載安裝需要的數據
                    Log.v(TAG, "Need language stuff:"+resultCode);
                    Intent dataIntent = new Intent();
                    dataIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(dataIntent);

                }
                break;
                case TextToSpeech.Engine.CHECK_VOICE_DATA_FAIL:
                    //檢查失敗
                default:
                    Log.v(TAG, "Got a failure. TTS apparently not available");
                    break;
            }
        }
        else
        {
            //其他Intent返回的結果
        }
    }


    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setNavigationView(){
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        // Initializing Drawer Layout and ActionBarToggle
        drawerLayout = (DrawerLayout) findViewById(R.id.cook_drawer_layout);
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
