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
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.roger.tinychief.R;

import java.io.IOException;
import java.util.ArrayList;

public class CreateActivity extends AppCompatActivity {
    private final String LOGTAG = "CreateActivity";
    //用list儲存材料和步驟的EditText,方便計算有幾筆材料和步驟
    private ArrayList<EditText> mStepEditTextList = new ArrayList<>();
    private ArrayList<EditText> mIiEditTextList = new ArrayList<>();
    private ImageView mImageView;
    private LinearLayout mStepLinearLayout, mIiLinearLayout;
    private String picPath;                                         //圖片路徑


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        setTitle("創建食譜");
        mStepLinearLayout = (LinearLayout) findViewById(R.id.linearlayout_step);
        mIiLinearLayout = (LinearLayout) findViewById(R.id.linearlayout_ii);
        mImageView = (ImageView) findViewById(R.id.img_create);
        //加材料和加步驟各執行一次,這樣才有「材料1」和「步驟1」
        addIi(null);
        addStep(null);
    }

    public void selectPic(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        Intent destIntent = Intent.createChooser(intent, "選擇圖片");
        startActivityForResult(destIntent, 0);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // 取得檔案的 Uri
            Uri uri = data.getData();
            if (uri != null) {
                picPath = getRealPathFromURI(uri);
                mImageView.setImageBitmap(loadBitmap(picPath,true));
            }
        }
    }

    //旋轉圖片
    private Bitmap loadBitmap(String path) {
        return BitmapFactory.decodeFile(path);
    }

    private Bitmap loadBitmap(String path, boolean adjustOritation) {
        if (!adjustOritation) {
            return loadBitmap(path);
        } else {
            Bitmap bm = loadBitmap(path);
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
    }
}
