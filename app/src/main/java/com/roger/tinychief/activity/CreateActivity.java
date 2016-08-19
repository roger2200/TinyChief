package com.roger.tinychief.activity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.roger.tinychief.R;
import com.roger.tinychief.util.NetworkManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreateActivity extends AppCompatActivity {
    private final String LOGTAG = "CreateActivity";
    private final int REQUEST_PIC = 0, REQUEST_AR_PIC = 1;
    //用list儲存材料和步驟的EditText,方便計算有幾筆材料和步驟
    private ArrayList<EditText> mStepEditTextList = new ArrayList<>();              //要傳給資料庫
    private ArrayList<EditText> mIiEditTextList = new ArrayList<>();                //要傳給資料庫
    private EditText mEditText;                                                    //要傳給資料庫
    private ImageView mImageView;
    private LinearLayout mStepLinearLayout, mIiLinearLayout;
    private Bitmap mImgBitmap, mArImgBitmap;                                    //要傳給資料庫
    private String picPath;                                         //圖片路徑


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        setTitle("創建食譜");
        mStepLinearLayout = (LinearLayout) findViewById(R.id.linearlayout_step);
        mIiLinearLayout = (LinearLayout) findViewById(R.id.linearlayout_ii);
        mImageView = (ImageView) findViewById(R.id.img_create);
        mEditText = (EditText) findViewById(R.id.edittext_title);
        //加材料和加步驟各執行一次,這樣才有「材料1」和「步驟1」
        addIi(null);
        addStep(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PIC) {
                // 取得檔案的 Uri
                Uri uri = data.getData();
                picPath = getRealPathFromURI(uri);
                mImgBitmap = loadBitmap(picPath);
                mImgBitmap = scaleBitmap(mImgBitmap);
                mImageView.setImageBitmap(mImgBitmap);

            } else if (requestCode == REQUEST_AR_PIC) {
                byte[] bitmapData = data.getByteArrayExtra("AR_PIC");
                mArImgBitmap = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
            }
        }
    }

    public void selectPic(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        Intent destIntent = Intent.createChooser(intent, "選擇圖片");
        startActivityForResult(destIntent, REQUEST_PIC);
    }

    public void selectPicAR(View v) {
        Intent intent = new Intent(v.getContext(), ImgprocessActivity.class);
        startActivityForResult(intent, REQUEST_AR_PIC);
    }

    public void addIi(View v) {
        int currentStep = mIiEditTextList.size() + 1;     //當前的list數量+1,就是指現在要加的是第幾步驟,因為index是從0開始的
        EditText edittxt = new EditText(this);
        TextView txtview = new TextView(this);
        txtview.setText("材料" + currentStep + ":");
        mIiEditTextList.add(edittxt);                 //加入到list的尾端
        //加到LinearLayout裡
        mIiLinearLayout.addView(txtview);
        mIiLinearLayout.addView(edittxt);
    }

    public void addStep(View v) {
        int currentStep = mStepEditTextList.size() + 1;     //當前的list數量+1,就是指現在要加的是第幾步驟,因為index是從0開始的
        EditText edittxt = new EditText(this);
        TextView txtview = new TextView(this);
        txtview.setText("步驟" + currentStep + ":");
        mStepEditTextList.add(edittxt);                 //加入到list的尾端
        //加到LinearLayout裡
        mStepLinearLayout.addView(txtview);
        mStepLinearLayout.addView(edittxt);
    }

    public void uploadCookBook(View v) {

        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArrIi = new JSONArray();
        JSONArray jsonArrStep = new JSONArray();
        for (EditText editText : mIiEditTextList)
            jsonArrIi.put(editText.getText());
        for (EditText editText : mStepEditTextList)
            jsonArrStep.put(editText.getText());

        try {
            jsonObject.put("name", mEditText.getText());
            jsonObject.put("materials", jsonArrIi);
            jsonObject.put("materials", jsonArrStep);
        } catch (JSONException e) {
            Log.e(LOGTAG, e.getMessage());
        }

        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.POST, "https://tiny-chief.herokuapp.com/createCookBook", jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(LOGTAG, "response -> " + response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(LOGTAG, error.getMessage(), error);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json; charset=UTF-8");
                return headers;
            }
        };
        //這行是把剛才StringRequest裡的工作放入佇列當中,這是volley的語法被包在NetworkManager中
        NetworkManager.getInstance(this).request(null, jsonRequest);
        Toast.makeText(this, "哈哈哈哈", Toast.LENGTH_LONG).show();
    }

    //將圖片的Uri轉Path
    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        String res = null;
        Cursor actualimagecursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (actualimagecursor != null) {
            int index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            actualimagecursor.moveToFirst();
            res = actualimagecursor.getString(index);
            actualimagecursor.close();
        }
        return res;
    }

    //旋轉圖片
    private Bitmap loadBitmap(String path) {
        Bitmap bm = BitmapFactory.decodeFile(path);
        int digree = 0;
        ExifInterface exif;                 //enif可以讀取照片中的方向,看是正向還是旋轉90度等
        try {
            exif = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
            exif = null;
        }
        if (exif != null) {
            //讀取圖片中相機方向資訊
            int ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            //計算旋轉角度
            switch (ori) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    digree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    digree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    digree = 270;
                    break;
                default:
                    digree = 0;
                    break;
            }
        }
        if (digree != 0) {
            //旋轉圖片
            Matrix m = new Matrix();
            m.postRotate(digree);
            bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
        }
        return bm;
    }

    //縮小圖片,圖片太大沒用
    private Bitmap scaleBitmap(Bitmap bitmap) {
        Bitmap image = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        int oldwidth = image.getWidth();
        int oldheight = image.getHeight();
        android.graphics.Point size = new android.graphics.Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        float scale = size.x / (float) oldwidth;
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        image = Bitmap.createBitmap(image, 0, 0, oldwidth, oldheight, matrix, true);
        return image;
    }
}
