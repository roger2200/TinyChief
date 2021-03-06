package com.roger.tinychief.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
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
import android.widget.Toast;

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
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
/**創建食譜*/
public class CreateActivity extends AppCompatActivity {
    private final String LOGTAG = "CreateActivity";
    private final int REQUEST_PIC = 0, REQUEST_AR_PIC = 1;
    //用list儲存材料和步驟的EditText,方便計算有幾筆材料和步驟
    private ArrayList<TableRow> mIiTableRowList = new ArrayList<>();
    private ArrayList<EditText> mStepEditTextList = new ArrayList<>();
    private EditText mTitleEditText;
    private ImageView mImageView, mArImageView;
    private LinearLayout mStepLinearLayout, mLinearlayout, mHintArLinearLayout, mHintRLinearLayout;
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
        mHintArLinearLayout = (LinearLayout) findViewById(R.id.linearlayout_hintar_create);
        mHintRLinearLayout = (LinearLayout) findViewById(R.id.linearlayout_hintr_create);
        mImageView = (ImageView) findViewById(R.id.img_create);
        mArImageView = (ImageView) findViewById(R.id.img_ar);
        mTitleEditText = (EditText) findViewById(R.id.edittext_title);
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
        mNavigationView.getMenu().getItem(1).setChecked(true);
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

                if (mHintArLinearLayout.getChildCount() == 0) {
                    View view1 = new View(CreateActivity.this);
                    view1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
                    view1.setBackgroundColor(Color.BLACK);

                    View view2 = new View(CreateActivity.this);
                    view2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
                    view2.setBackgroundColor(Color.BLACK);

                    TextView textViewB = new TextView(CreateActivity.this);
                    textViewB.setText("食譜照片");
                    mHintRLinearLayout.addView(textViewB);
                    mHintRLinearLayout.addView(view1);

                    TextView textViewAR = new TextView(CreateActivity.this);
                    textViewAR.setText("AR照片");
                    mHintArLinearLayout.addView(textViewAR);
                    mHintArLinearLayout.addView(view2);
                }

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
        TableRow tablerow = new TableRow(this);
        final Spinner spinner = new Spinner(this);
        final EditText edittxtName = new EditText(this);
        final EditText edittxtUnit = new EditText(this);
        final ArrayList<String> strSpinItem = new ArrayList<>();

        tablerow.setPadding(0, 20, 0, 20);

        strSpinItem.add("請選擇");
        strSpinItem.add("雞肉");
        strSpinItem.add("豬肉");
        strSpinItem.add("牛肉");
        strSpinItem.add("鴨肉");
        strSpinItem.add("鵝肉");
        strSpinItem.add("雞蛋");
        strSpinItem.add("鴨蛋");
        strSpinItem.add("調味料");
        strSpinItem.add("其他");

        spinner.setSelection(0);
        ArrayAdapter adapter = new ArrayAdapter<>(CreateActivity.this, android.R.layout.simple_spinner_item, strSpinItem);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 6 || i == 7)
                    edittxtName.setText(adapterView.getSelectedItem().toString());
                else if (i > 0 && i < 6)
                    edittxtName.setText(adapterView.getSelectedItem().toString());
                else if (i > 9)
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
                String strCropName = edittxtName.getText().toString();
                if (strCropName.contains("高麗")) strCropName = "萵苣";
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
        if (mTitleEditText.getText().toString().equals("")) {
            Snackbar snackbar = Snackbar.make(mCoordinatorLayout, "請輸入食譜的名稱", Snackbar.LENGTH_LONG);
            MyHelper.setSnackbarMessageTextColor(snackbar, android.graphics.Color.WHITE);
            snackbar.show();
            return;
        }
        for (TableRow tablerow : mIiTableRowList) {
            if (((Spinner) tablerow.getVirtualChildAt(1)).getSelectedItemPosition() == 0) {
                Snackbar snackbar = Snackbar.make(mCoordinatorLayout, "請選擇材料的種類", Snackbar.LENGTH_LONG);
                MyHelper.setSnackbarMessageTextColor(snackbar, android.graphics.Color.WHITE);
                snackbar.show();
                return;
            }
            if (((EditText) tablerow.getVirtualChildAt(0)).getText().toString().equals("")) {
                Snackbar snackbar = Snackbar.make(mCoordinatorLayout, "請輸入材料的名稱", Snackbar.LENGTH_LONG);
                MyHelper.setSnackbarMessageTextColor(snackbar, android.graphics.Color.WHITE);
                snackbar.show();
                return;
            }
            if (((EditText) tablerow.getVirtualChildAt(2)).getText().toString().equals("")) {
                Snackbar snackbar = Snackbar.make(mCoordinatorLayout, "請輸入材料的數量", Snackbar.LENGTH_LONG);
                MyHelper.setSnackbarMessageTextColor(snackbar, android.graphics.Color.WHITE);
                snackbar.show();
                return;
            }
            if (((EditText) tablerow.getVirtualChildAt(3)).getText().toString().equals("")) {
                Snackbar snackbar = Snackbar.make(mCoordinatorLayout, "請輸入材料的單位", Snackbar.LENGTH_LONG);
                MyHelper.setSnackbarMessageTextColor(snackbar, android.graphics.Color.WHITE);
                snackbar.show();
                return;
            }
        }
        for (EditText edittext : mStepEditTextList)
            if (edittext.getText().toString().equals("")) {
                Snackbar snackbar = Snackbar.make(mCoordinatorLayout, "請輸入步驟", Snackbar.LENGTH_LONG);
                MyHelper.setSnackbarMessageTextColor(snackbar, android.graphics.Color.WHITE);
                snackbar.show();
                return;
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
        } else {
            Snackbar snackbar = Snackbar.make(mCoordinatorLayout, "請選擇一張食譜的照片", Snackbar.LENGTH_LONG);
            MyHelper.setSnackbarMessageTextColor(snackbar, android.graphics.Color.WHITE);
            snackbar.show();
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
        if (parCrop.equals(""))
            return;
        final String strCrop = parCrop;
        final ArrayList<String> strSpinItem = parSpinItem;
        final Spinner spinner = parSpinner;

        strSpinItem.clear();
        strSpinItem.add("請選擇");
        strSpinItem.add("雞肉");
        strSpinItem.add("豬肉");
        strSpinItem.add("牛肉");
        strSpinItem.add("鴨肉");
        strSpinItem.add("鵝肉");
        strSpinItem.add("雞蛋");
        strSpinItem.add("鴨蛋");
        strSpinItem.add("調味料");
        strSpinItem.add("其他");

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
                            if (strSpinItem.size() < 11)
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
                int intClass;
                int intSpinnerPos = ((Spinner) tablerow.getVirtualChildAt(1)).getSelectedItemPosition();
                double intAmount = Integer.parseInt(((EditText) tablerow.getVirtualChildAt(2)).getText().toString());

                if (intSpinnerPos == 1 || intSpinnerPos == 6)// 雞
                    intClass = 0;
                else if (intSpinnerPos == 4 || intSpinnerPos == 5 || intSpinnerPos == 7)//鴨 鵝
                    intClass = 1;
                else if (intSpinnerPos == 2 || intSpinnerPos == 3)//豬 牛
                    intClass = 2;
                else if (intSpinnerPos == 8 || intSpinnerPos == 9)//調味料 其他
                    intClass = 3;
                else                                    //open data
                    intClass = 4;

                JSONObject jsonIi = new JSONObject();
                jsonIi.put("name", ((EditText) tablerow.getVirtualChildAt(0)).getText());
                jsonIi.put("amount", intAmount);
                jsonIi.put("unit", ((EditText) tablerow.getVirtualChildAt(3)).getText());
                jsonIi.put("class", intClass);
                jsonArrIi.put(jsonIi);
            }
            for (EditText editText : mStepEditTextList)
                jsonArrStep.put(editText.getText());

            jsonObjectAuthor.put("name", MainActivity.USER_NAME);
            jsonObjectAuthor.put("id", MainActivity.USER_ID);
            jsonObjectMain.put("author", jsonObjectAuthor);
            jsonObjectMain.put("title", mTitleEditText.getText());
            jsonObjectMain.put("image", mImgUrl);
            if (mArUrl != null)
                jsonObjectMain.put("image_ar", mArUrl);
            else
                jsonObjectMain.put("image_ar", "");
            jsonObjectMain.put("ingredients", jsonArrIi);
            jsonObjectMain.put("steps", jsonArrStep);
            jsonObjectMain.put("comment", new JSONArray());
            jsonObjectMain.put("rate_avg", 5);
            Log.d(LOGTAG, jsonObjectMain.toString());
        } catch (Exception e) {
            Snackbar snackbar = Snackbar.make(mCoordinatorLayout, "有地方填錯了呦", Snackbar.LENGTH_LONG);
            MyHelper.setSnackbarMessageTextColor(snackbar, android.graphics.Color.WHITE);
            snackbar.show();
            e.printStackTrace();
        }

        JsonRequest<JSONObject> jsonRequest = new JsonObjectRequest(Request.Method.POST, "https://tiny-chief.herokuapp.com/upload/cookbook", jsonObjectMain,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        mProgressDialog.dismiss();
                        Toast.makeText(CreateActivity.this, "上傳完成", Toast.LENGTH_LONG).show();
                        Log.d(LOGTAG, "Create Response" + response.toString());
                        MainActivity.NEED_REINIT = true;
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mProgressDialog.dismiss();
                        Toast.makeText(CreateActivity.this, "上傳失敗", Toast.LENGTH_LONG).show();
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
        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        NetworkManager.getInstance(this).request(null, jsonRequest);
    }
}
