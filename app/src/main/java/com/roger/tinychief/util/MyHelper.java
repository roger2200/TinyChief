package com.roger.tinychief.util;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;

import com.roger.tinychief.activity.CreateActivity;

import java.io.IOException;

/**
 * Created by Roger on 9/1/2016.
 */
//放一些基本的處理程式碼
public class MyHelper {

    //將圖片的Uri轉Path
    public static String getRealPathFromURI(Uri uri, Context context) {
        String[] proj = {MediaStore.Images.Media.DATA};
        String res = null;
        Cursor actualimagecursor = context.getContentResolver().query(uri, proj, null, null, null);
        if (actualimagecursor != null) {
            int index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            actualimagecursor.moveToFirst();
            res = actualimagecursor.getString(index);
            actualimagecursor.close();
        }
        return res;
    }

    //縮小圖片尺寸以符合螢幕寬度
    public static Bitmap scaleBitmap(Bitmap bitmap,Activity activity) {
        Bitmap image = bitmap.copy(Bitmap.Config.ARGB_4444, true);
        int oldwidth = image.getWidth();
        int oldheight = image.getHeight();
        android.graphics.Point size = new android.graphics.Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        float scale = size.x / (float) oldwidth;
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        image = Bitmap.createBitmap(image, 0, 0, oldwidth, oldheight, matrix, true);
        return image;
    }

    //旋轉圖片
    public static Bitmap rotationBitmap(String path) {
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
            Matrix m = new Matrix();
            m.postRotate(digree);
            bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
        }
        return bm;
    }
}
