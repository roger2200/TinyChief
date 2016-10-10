package com.roger.tinychief.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.roger.tinychief.R;
import com.roger.tinychief.imgur.ImageResponse;
import com.roger.tinychief.imgur.Upload;
import com.roger.tinychief.imgur.UploadService;
import com.roger.tinychief.util.MyHelper;
import com.roger.tinychief.util.NetworkManager;
import com.roger.tinychief.widget.navigation.NavigationViewSetup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;

public class CreateActivity extends AppCompatActivity {
    private final String LOGTAG = "CreateActivity";
    private final int REQUEST_PIC = 0, REQUEST_AR_PIC = 1;
    //用list儲存材料和步驟的EditText,方便計算有幾筆材料和步驟
    private ArrayList<TableRow> mIiEditTextList = new ArrayList<>();
    private ArrayList<EditText> mStepEditTextList = new ArrayList<>();
    private EditText mTitleEditText, mServingEditText, mNoteEditText;
    private ImageView mImageView, mArImageView;
    private LinearLayout mStepLinearLayout,mLinearlayout;
    private TableLayout mIiTableLayout;
    private Bitmap mImgBitmap, mArBitmap;
    private Upload mUpload; // Upload object containging image and meta data
    private File mImgFile, mArFile;
    private String mImgUrl, mArUrl;
    private DrawerLayout mDrawerLayout;
    private Toolbar mToolbar;
    private NavigationView mNavigationView;
    private NavigationViewSetup mNavigationViewSetup;
    private CoordinatorLayout mCoordinatorLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        setTitle("創建食譜");

        mIiTableLayout = (TableLayout) findViewById(R.id.tablelayout_ii_create);
        mStepLinearLayout = (LinearLayout) findViewById(R.id.linearlayout_step_create);
        mImageView = (ImageView) findViewById(R.id.img_create);
        mArImageView = (ImageView) findViewById(R.id.img_ar);
        mTitleEditText = (EditText) findViewById(R.id.edittext_title);
        mServingEditText = (EditText) findViewById(R.id.edittext_servings);
        mNoteEditText = (EditText) findViewById(R.id.edittext_note);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout_create);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorlayout_create);
        mLinearlayout=(LinearLayout)findViewById(R.id.linearlayout_create);

        mLinearlayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                InputMethodManager imm = (InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(),0);
                return false;
            }
        });

        mNavigationViewSetup = new NavigationViewSetup(this, mDrawerLayout, mToolbar);
        mNavigationView = mNavigationViewSetup.setNavigationView();

        //加材料和加步驟各執行一次,這樣才有「材料1」和「步驟1」
        addIi(null);
        addStep(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNavigationView.getMenu().getItem(2).setChecked(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PIC) {
                // 取得檔案的 Uri
                Uri uri = data.getData();
                String imgPath = MyHelper.getRealPathFromURI(uri, this);
                mImgBitmap = MyHelper.rotationBitmap(imgPath);
                mImgBitmap = MyHelper.scaleBitmap(mImgBitmap, this,true);
                try {
                    // 路徑
                    String path = Environment.getExternalStorageDirectory().toString() + "/Tiny Chief/";

                    // 開啟檔案
                    File dir = new File(path);
                    if (!dir.exists())
                        dir.mkdirs();

                    path += "tmpImg.png";
                    File file = new File(path);
                    file.createNewFile();
                    file.setWritable(Boolean.TRUE);

                    // 將 Bitmap壓縮成指定格式的圖片並寫入檔案串流
                    FileOutputStream out = new FileOutputStream(file);
                    mImgBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

                    // 刷新並關閉檔案串流
                    out.flush();
                    out.close();

                    mImgFile = new File(path);
                    mImageView.setImageBitmap(mImgBitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (requestCode == REQUEST_AR_PIC) {
                String arPath = data.getStringExtra("AR_PIC");
                mArFile = new File(arPath);
                mArBitmap = MyHelper.rotationBitmap(arPath);
                mArBitmap = MyHelper.scaleBitmap(mArBitmap, this,true);
                mArImageView.setImageBitmap(mArBitmap);
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
        EditText edittxtName = new EditText(this);
        EditText edittxtAmount = new EditText(this);
        EditText edittxtUnit = new EditText(this);
        TableRow tablerow = new TableRow(this);

        tablerow.addView(edittxtName);
        tablerow.addView(edittxtAmount);
        tablerow.addView(edittxtUnit);
        //加到LinearLayout裡
        mIiTableLayout.addView(tablerow);
        mIiEditTextList.add(tablerow);
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
        if (mImgFile != null) {
            createUpload(mImgFile, "Image");
            new UploadService(this).Execute(mUpload, new UiCallback());
        }
    }

    private void createUpload(File image, String descript) {
        mUpload = new Upload();

        mUpload.image = image;
        mUpload.title = mTitleEditText.getText().toString();
        mUpload.description = descript;
        mUpload.albumId = "2lLX3";
    }

    private class UiCallback implements Callback<ImageResponse> {

        @Override
        public void success(ImageResponse imageResponse, retrofit.client.Response response) {
            Log.d("ImageResponse", imageResponse.data.description);
            if (imageResponse.data.description.equals("Ar")) {
                mArUrl = imageResponse.data.link;
                if (!(mArUrl == null || mImgUrl == null))
                    upload2Server();
            } else if (imageResponse.data.description.equals("Image")) {
                mImgUrl = imageResponse.data.link;
                if (mArFile != null) {
                    createUpload(mArFile, "Ar");
                    new UploadService(CreateActivity.this).Execute(mUpload, new UiCallback());
                }
            }
        }

        @Override
        public void failure(RetrofitError error) {
            //Assume we have no connection, since error is null
            if (error == null)
                Log.e("RetrofitError", "No internet connection");
            else
                Log.e("RetrofitError", error.getMessage());
        }
    }

    private void upload2Server() {
        JSONObject jsonObjectMain = new JSONObject();
        JSONObject jsonObjectAuthor = new JSONObject();
        JSONArray jsonArrIi = new JSONArray();
        JSONArray jsonArrStep = new JSONArray();
        try {
            for (TableRow tablerow : mIiEditTextList) {
                JSONObject jsonIi = new JSONObject();
                jsonIi.put("name", ((EditText)tablerow.getVirtualChildAt(0)).getText());
                jsonIi.put("amount", ((EditText)tablerow.getVirtualChildAt(1)).getText());
                jsonIi.put("unit", ((EditText)tablerow.getVirtualChildAt(2)).getText());
                jsonArrIi.put(jsonIi);
            }
            for (EditText editText : mStepEditTextList)
                jsonArrStep.put(editText.getText());

            //這兩行之後要換掉
            jsonObjectAuthor.put("name", "Roger");
            jsonObjectAuthor.put("id", "5787a635e07c9e0300237873");

            jsonObjectMain.put("author", jsonObjectAuthor);
            jsonObjectMain.put("title", mTitleEditText.getText());
            jsonObjectMain.put("image", mImgUrl);
            jsonObjectMain.put("image_ar", mArUrl);
            jsonObjectMain.put("servings", mServingEditText.getText());
            jsonObjectMain.put("note", mNoteEditText.getText());
            jsonObjectMain.put("ingredients", jsonArrIi);
            jsonObjectMain.put("steps", jsonArrStep);
            Log.d(LOGTAG, jsonObjectMain.toString());
        } catch (JSONException e) {
            Log.e(LOGTAG, e.getMessage());
        }

        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.POST, "https://tiny-chief.herokuapp.com/upload/cookbook", jsonObjectMain,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Snackbar snackbar = Snackbar.make(mCoordinatorLayout, "上傳完成", Snackbar.LENGTH_LONG);
                        MyHelper.setSnackbarMessageTextColor(snackbar, android.graphics.Color.WHITE);
                        snackbar.show();
                        Log.d(LOGTAG, "Create Response" + response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Snackbar snackbar = Snackbar.make(mCoordinatorLayout, "上傳失敗", Snackbar.LENGTH_LONG);
                        MyHelper.setSnackbarMessageTextColor(snackbar, android.graphics.Color.WHITE);
                        snackbar.show();
                        Log.e("Error", String.valueOf(error));
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
        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        NetworkManager.getInstance(this).request(null, jsonRequest);
    }
}
