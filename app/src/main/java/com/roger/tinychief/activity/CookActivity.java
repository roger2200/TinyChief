package com.roger.tinychief.activity;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    private String[] mStrArraySteps;
    private String[][] mStrArrayCheck = new String[3][];
    private String[] mStrRepeat = new String[]{"重", "再", "在", "站", "戰", "蟲", "寵", "崇", "衝", "暫","從","叢","3","三","片","變","成","充","沖","船"};
    private String[] mStrPrevious = new String[]{"前", "錢", "潛", "乾", "上", "尚","賽","散","帥","千","全"};
    private String[] mStrNext = new String[]{"下", "嚇", "夏", "廈", "霞", "向", "項", "像", "巷", "相","少","小","算"};
    private int pointerStep = 0;
    private boolean isCreate = false;

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
        mStrArrayCheck[0] = mStrRepeat;
        mStrArrayCheck[1] = mStrPrevious;
        mStrArrayCheck[2] = mStrNext;

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
    protected void onResume() {
        if (isCreate) {
            mTts = new TextToSpeech(this, this);
            recognizer = SpeechRecognizer.createSpeechRecognizer(this);
            recognizer.setRecognitionListener(new MyRecognizerListener());
        } else
            isCreate = true;
        super.onResume();
    }

    @Override
    protected void onStop() {
        mTts.stop();
        mTts.shutdown();
        recognizer.cancel();
        recognizer.stopListening();
        recognizer.destroy();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mTts.stop();
        mTts.shutdown();
        recognizer.cancel();
        recognizer.stopListening();
        recognizer.destroy();
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
                mTts.speak(mStrArraySteps[pointerStep], TextToSpeech.QUEUE_ADD, null);
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
        mStrArraySteps = bundle.getStringArray("steps");
        for (int i = 0; i < mStrArraySteps.length; i++) {
            TextView textview = new TextView(CookActivity.this);
            textview.setText("步驟" + (i + 1) + ":\n" + mStrArraySteps[i] + "\n");
            textview.setTextSize(26.0f);
            mLinearLayout.addView(textview);
        }
    }

    private class MyRecognizerListener implements RecognitionListener {
        private static final String TAG = "RecognitionListener";

        @Override
        public void onResults(Bundle results) {
            List<String> resList = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            speak(resList);
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

    private void speak(List<String> resList) {
        switch (analyzeSpeech(resList)) {
            case 0:
                mTts.speak(mStrArraySteps[pointerStep], TextToSpeech.QUEUE_ADD, null);
                break;
            case 1:
                if (pointerStep == 0)
                    mTts.speak(mStrArraySteps[pointerStep], TextToSpeech.QUEUE_ADD, null);
                else
                    mTts.speak(mStrArraySteps[--pointerStep], TextToSpeech.QUEUE_ADD, null);
                break;
            case 2:
                if (pointerStep == mStrArraySteps.length - 1)
                    mTts.speak(mStrArraySteps[pointerStep], TextToSpeech.QUEUE_ADD, null);
                else
                    mTts.speak(mStrArraySteps[++pointerStep], TextToSpeech.QUEUE_ADD, null);
                break;
        }
        Log.d(TAG, "onResults -> " + resList + "," + pointerStep);
        startRecognizer();
    }

    private int analyzeSpeech(List<String> resList) {
        for (String strList : resList)
            for (int i = 0; i < mStrArrayCheck.length; i++)
                for (String strSpeak : mStrArrayCheck[i])
                    if (strList.contains(strSpeak))
                        return i;
        return -1;
    }
}
