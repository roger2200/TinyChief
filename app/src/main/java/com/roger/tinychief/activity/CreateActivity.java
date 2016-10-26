package com.roger.tinychief.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.database.DatabaseUtilsCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;

public class CreateActivity extends AppCompatActivity {
    private final String LOGTAG = "CreateActivity";
    private final int REQUEST_PIC = 0, REQUEST_AR_PIC = 1;
    //用list儲存材料和步驟的EditText,方便計算有幾筆材料和步驟
    private ArrayList<TableRow> mIiTableRowList = new ArrayList<>();
    private ArrayList<EditText> mStepEditTextList = new ArrayList<>();
    private EditText mTitleEditText, mServingEditText, mNoteEditText;
    private ImageView mImageView, mArImageView;
    private LinearLayout mStepLinearLayout, mLinearlayout;
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
    private ProgressDialog mProgressDialog;


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
        mLinearlayout = (LinearLayout) findViewById(R.id.linearlayout_create);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("上傳食譜中...");

        mLinearlayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
    public void onDestroy() {
        if (mImgFile != null)
            mImgFile.delete();
        if (mArFile != null)
            mArFile.delete();
        super.onDestroy();
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
                mImgBitmap = MyHelper.scaleBitmap(mImgBitmap, this, true);
                mImageView.setImageBitmap(mImgBitmap);


            } else if (requestCode == REQUEST_AR_PIC) {
                String arPath = data.getStringExtra("AR_PIC");
                mArFile = new File(arPath);
                mArBitmap = MyHelper.rotationBitmap(arPath);
                mArBitmap = MyHelper.scaleBitmap(mArBitmap, this, true);
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
        EditText edittxtAmount = new EditText(this);
        EditText edittxtUnit = new EditText(this);
        TableRow tablerow = new TableRow(this);
        final Spinner spinner = new Spinner(this);
        final EditText edittxtName = new EditText(this);
        final ArrayList<String> strSpinItem = new ArrayList<>();

        strSpinItem.add("請選擇");
        strSpinItem.add("調味料");
        strSpinItem.add("肉類");
        spinner.setSelection(0);
        ArrayAdapter adapter = new ArrayAdapter<>(CreateActivity.this, android.R.layout.simple_spinner_item, strSpinItem);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i > 2)
                    edittxtName.setText(adapterView.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        edittxtName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (strSpinItem.contains(editable.toString()))
                    return;
                strSpinItem.clear();
                strSpinItem.add("請選擇");
                strSpinItem.add("調味料");
                strSpinItem.add("肉類");
                String strCropName = edittxtName.getText().toString();
                if (!strCropName.equals(""))
                    getOpendata(strCropName, strSpinItem, spinner);
            }
        });

        tablerow.addView(edittxtName);
        tablerow.addView(spinner);
        tablerow.addView(edittxtAmount);
        tablerow.addView(edittxtUnit);

        //加到LinearLayout裡
        mIiTableLayout.addView(tablerow);
        mIiTableRowList.add(tablerow);
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
        for (TableRow tablerow : mIiTableRowList) {
            if (((Spinner) tablerow.getVirtualChildAt(1)).getSelectedItemPosition() == 0) {
                Snackbar snackbar = Snackbar.make(mCoordinatorLayout, "請選擇材料種類", Snackbar.LENGTH_LONG);
                MyHelper.setSnackbarMessageTextColor(snackbar, android.graphics.Color.WHITE);
                snackbar.show();
                return;
            }
        }
        if (mImgBitmap != null) {
            mProgressDialog.show();
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
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    private void getOpendata(String parCrop, ArrayList<String> parSpinItem, Spinner parSpinner) {
        final String strCrop = parCrop;
        final ArrayList<String> strSpinItem = parSpinItem;
        final Spinner spinner = parSpinner;

        strSpinItem.clear();
        strSpinItem.add("請選擇");
        strSpinItem.add("調味料");
        strSpinItem.add("肉類");

        Calendar calendar = Calendar.getInstance();
        int intYear1 = calendar.get(Calendar.YEAR) - 1911;
        CharSequence strDay1 = DateFormat.format(".MM.dd", calendar);
        calendar.add(Calendar.MONTH, -1);
        int intYear2 = calendar.get(Calendar.YEAR) - 1911;
        CharSequence strDay2 = DateFormat.format(".MM.dd", calendar);

        String strURL = "http://m.coa.gov.tw/OpenData/FarmTransData.aspx?"
                + "StartDate=" + intYear2 + strDay2 + "&EndDate=" + intYear1 + strDay1 + "&Crop=" + strCrop;
        Log.d("Request Url", strURL);
        StringRequest request = new StringRequest(Request.Method.GET, strURL,
                new Response.Listener<String>() {
                    public void onResponse(String string) {
                        try {
                            spinner.clearFocus();
                            JSONArray jsonArray = new JSONArray(string);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                String str = jsonArray.getJSONObject(i).getString("作物名稱");
                                if (!(strSpinItem.contains(str) || str.equals("休市"))) {
                                    strSpinItem.add(str);
                                    spinner.setSelection(0);
                                }
                            }
                            if (strSpinItem.size() < 4)
                                if (strCrop.length() > 1)
                                    getOpendata(strCrop.substring(0, strCrop.length() - 1), strSpinItem, spinner);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error", error.toString());
                    }
                });
        request.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        NetworkManager.getInstance(CreateActivity.this).request(null, request);
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
                } else
                    upload2Server();
            }
        }

        @Override
        public void failure(RetrofitError error) {
            //Assume we have no connection, since error is null
            if (error == null)
                Log.e("RetrofitError", "No internet connection");
            else
                Log.e("RetrofitError", error.getMessage());
            mProgressDialog.dismiss();
            Snackbar snackbar = Snackbar.make(mCoordinatorLayout, "上傳失敗", Snackbar.LENGTH_LONG);
            MyHelper.setSnackbarMessageTextColor(snackbar, android.graphics.Color.WHITE);
            snackbar.show();
        }
    }

    private void upload2Server() {
        JSONObject jsonObjectMain = new JSONObject();
        JSONObject jsonObjectAuthor = new JSONObject();
        JSONArray jsonArrIi = new JSONArray();
        JSONArray jsonArrStep = new JSONArray();
        try {
            for (TableRow tablerow : mIiTableRowList) {
                JSONObject jsonIi = new JSONObject();
                jsonIi.put("name", ((EditText) tablerow.getVirtualChildAt(0)).getText());
                jsonIi.put("amount", ((EditText) tablerow.getVirtualChildAt(2)).getText());
                jsonIi.put("unit", ((EditText) tablerow.getVirtualChildAt(3)).getText());
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
            if (mArUrl != null)
                jsonObjectMain.put("image_ar", mArUrl);
            else
                jsonObjectMain.put("image_ar", "");
            jsonObjectMain.put("servings", mServingEditText.getText());
            jsonObjectMain.put("note", mNoteEditText.getText());
            jsonObjectMain.put("ingredients", jsonArrIi);
            jsonObjectMain.put("steps", jsonArrStep);
            jsonObjectMain.put("comment", new JSONArray());
            Log.d(LOGTAG, jsonObjectMain.toString());
        } catch (JSONException e) {
            Snackbar snackbar = Snackbar.make(mCoordinatorLayout, "有空格還沒填", Snackbar.LENGTH_LONG);
            MyHelper.setSnackbarMessageTextColor(snackbar, android.graphics.Color.WHITE);
            snackbar.show();
            e.printStackTrace();
        }

        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.POST, "https://tinny-chief.herokuapp.com/upload/cookbook", jsonObjectMain,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        mProgressDialog.dismiss();
                        Snackbar snackbar = Snackbar.make(mCoordinatorLayout, "上傳完成", Snackbar.LENGTH_LONG);
                        MyHelper.setSnackbarMessageTextColor(snackbar, android.graphics.Color.WHITE);
                        snackbar.show();
                        Log.d(LOGTAG, "Create Response" + response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mProgressDialog.dismiss();
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
