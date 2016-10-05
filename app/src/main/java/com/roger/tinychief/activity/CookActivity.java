package com.roger.tinychief.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.design.widget.CoordinatorLayout;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.roger.tinychief.R;
import com.roger.tinychief.widget.navigation.NavigationViewSetup;

import java.util.List;
import java.util.Locale;

public class CookActivity extends AppCompatActivity implements OnInitListener {
    private static final int REQ_TTS_STATUS_CHECK = 0;
    private static final String TAG = "TTS Demo";

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private NavigationViewSetup mNavigationViewSetup;
    private LinearLayout mLinearLayout;
    private TextToSpeech mTts;
    private SpeechRecognizer recognizer;
    private Intent mIntentSR;
    private String strArraySteps[];
    private int pointerStep = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cook);
        setTitle("開始料理");
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout_cook);
        mLinearLayout = (LinearLayout) findViewById(R.id.linearlayout_step_cook);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        mNavigationViewSetup = new NavigationViewSetup(this, mDrawerLayout, mToolbar);
        mNavigationViewSetup.setNavigationView();

        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(new MyRecognizerListener());

        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, REQ_TTS_STATUS_CHECK);

        mIntentSR = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mIntentSR.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mIntentSR.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getApplication().getPackageName());

        setSteps();
    }

    @Override
    protected void onStop() {
        recognizer.stopListening();
        recognizer.destroy();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        recognizer.stopListening();
        recognizer.destroy();
        mTts.shutdown();
        super.onDestroy();
    }

    //初始化TTS
    @Override
    public void onInit(int status) {
        //TTS Engine初始化完成
        if (status == TextToSpeech.SUCCESS) {
            int result = mTts.setLanguage(Locale.CHINESE);
            //設置發音語言
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)            //判斷語言是否可用
                Log.v(TAG, "Language is not available");
            else {
                Log.v(TAG, "TTS initial success");
                mTts.speak(strArraySteps[pointerStep++], TextToSpeech.QUEUE_ADD, null);
                startRecognizer();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_TTS_STATUS_CHECK)
            switch (resultCode) {
                case TextToSpeech.Engine.CHECK_VOICE_DATA_PASS:                  //這個返回結果表明TTS Engine可以用
                    mTts = new TextToSpeech(this, this);
                    Log.v(TAG, "TTS Engine is installed!");
                    break;
                case TextToSpeech.Engine.CHECK_VOICE_DATA_FAIL:                    //檢查失敗
                    Log.v(TAG, "TTS Engine check failure.");
                    Log.v(TAG, "Need language stuff");
                    Intent dataIntent = new Intent();
                    dataIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(dataIntent);
                    break;
                default:
                    Log.v(TAG, "Got a failure. TTS apparently not available");
                    break;
            }
    }

    private void setSteps() {
        Bundle bundle = this.getIntent().getExtras();
        strArraySteps = bundle.getStringArray("steps");
        for (int i = 0; i < strArraySteps.length; i++) {
            TextView textview = new TextView(CookActivity.this);
            textview.setText("步驟" + (i + 1) + ":\n" + strArraySteps[i] + "\n");
            textview.setTextSize(26.0f);
            mLinearLayout.addView(textview);
        }
    }

    private class MyRecognizerListener implements RecognitionListener {
        private static final String TAG = "RecognitionListener";

        @Override
        public void onResults(Bundle results) {
            List<String> resList = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            analyzeSpeech(resList);
        }

        @Override
        public void onError(int error) {
            Log.d(TAG, "Error: " + error);
            if (error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT || error == SpeechRecognizer.ERROR_NETWORK_TIMEOUT) {
                mIntentSR = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                mIntentSR.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                mIntentSR.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getApplication().getPackageName());
            }
            startRecognizer();
        }

        @Override
        public void onReadyForSpeech(Bundle params) {
        }

        @Override
        public void onBeginningOfSpeech() {
        }

        @Override
        public void onRmsChanged(float rmsdB) {
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
        }

        @Override
        public void onEndOfSpeech() {
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
        }
    }

    private void startRecognizer() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mTts.isSpeaking())
                    startRecognizer();
                else
                    recognizer.startListening(mIntentSR);
            }
        }, 5000);
    }

    private void analyzeSpeech(List<String> resList) {
        for (String str : resList) {
            Log.d(TAG, "onResults -> " + str + "," + pointerStep);
            if (str.contains("重") || str.contains("再")) {
                mTts.speak(strArraySteps[pointerStep], TextToSpeech.QUEUE_ADD, null);
                break;
            }
            if (str.contains("上") || str.contains("前")) {
                if (pointerStep == 0)
                    mTts.speak(strArraySteps[pointerStep], TextToSpeech.QUEUE_ADD, null);
                else
                    mTts.speak(strArraySteps[--pointerStep], TextToSpeech.QUEUE_ADD, null);
                break;
            }
            if (str.contains("下") || str.contains("繼續")) {
                if (pointerStep == strArraySteps.length - 1)
                    mTts.speak(strArraySteps[pointerStep], TextToSpeech.QUEUE_ADD, null);
                else
                    mTts.speak(strArraySteps[++pointerStep], TextToSpeech.QUEUE_ADD, null);
                break;
            }
        }
        startRecognizer();
    }
}
