package com.roger.tinychief.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.Button;
import android.widget.ImageView;

import com.roger.tinychief.R;
import com.roger.tinychief.util.MyHelper;
import com.roger.tinychief.widget.navigation.NavigationViewSetup;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ImgprocessActivity extends AppCompatActivity {
    final String TAG = "ImgprocessActivity";
    private Toolbar mToolbar;
    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    private NavigationViewSetup mNavigationViewSetup;
    private ImageView mImageView;
    private Bitmap mBitmap = null;
    private Rect mRect;
    private Snackbar mSnackbar;
    private CoordinatorLayout mCoordinatorLayout;
    private BaseLoaderCallback mLoaderCallback;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imgprocess);

        mImageView = (ImageView) findViewById(R.id.img_imgprocess);
        mImageView.setOnTouchListener(getOnTouchListener());
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout_imgprocess);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorlayout_imgprocess);

        mSnackbar = Snackbar.make(mCoordinatorLayout, "", Snackbar.LENGTH_LONG);
        MyHelper.setSnackbarMessageTextColor(mSnackbar, android.graphics.Color.WHITE);

        mNavigationViewSetup = new NavigationViewSetup(this, mDrawerLayout, mToolbar);
        mNavigationView = mNavigationViewSetup.setNavigationView();

        mLoaderCallback = getBaseLoaderCallback();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("處理圖片中...");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // 取得檔案的 Uri
            Uri uri = data.getData();
            if (uri != null) {
                //把uri當中的圖片縮小符合手機螢幕寬度放入全域變數mBitmapImage
                String imgPath = MyHelper.getRealPathFromURI(uri, this);
                mBitmap = BitmapFactory.decodeFile(imgPath);
                mBitmap = MyHelper.scaleBitmap(mBitmap, this, true);
                mImageView.setImageBitmap(mBitmap);
            }
        }
    }

    private BaseLoaderCallback getBaseLoaderCallback() {
        return new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS:
                        Log.i("OpenCV", "OpenCV loaded successfully");
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }
            }
        };
    }

    private View.OnTouchListener getOnTouchListener() {
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (mBitmap == null)
                    return false;
                switch (motionEvent.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        mRect = new Rect((int) motionEvent.getX(), (int) motionEvent.getY(), 1, 1);
                        return true;
                    case MotionEvent.ACTION_UP:
                        mRect.width = (int) motionEvent.getX() - mRect.x;
                        mRect.height = (int) motionEvent.getY() - mRect.y;
                        if (motionEvent.getX() < mRect.x) {
                            mRect.width *= -1;
                            mRect.x = (int) motionEvent.getX();
                        }
                        if (motionEvent.getY() < mRect.y) {
                            mRect.height *= -1;
                            mRect.y = (int) motionEvent.getY();
                        }
                        Log.d("Current Rect", "" + mRect);
                        return true;
                    case MotionEvent.ACTION_MOVE:  // 拖曳移動
                        Paint rectPaint = new Paint();
                        rectPaint.setColor(Color.RED);
                        rectPaint.setStyle(Paint.Style.STROKE);
                        rectPaint.setStrokeWidth(5);
                        Bitmap tmpBm = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
                        Canvas tmpCanvas = new Canvas(tmpBm);
                        tmpCanvas.drawBitmap(mBitmap, 0, 0, null);
                        tmpCanvas.drawRect(new RectF((float) mRect.x, (float) mRect.y, motionEvent.getX(), motionEvent.getY()), rectPaint);
                        mImageView.setImageBitmap(tmpBm);
                        return true;
                }
                return false;
            }
        };
    }

    private class ProcessImageTask extends AsyncTask<Integer/*傳入值*/, Integer/*更新進度*/, Integer/*回傳值*/> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.show();
        }

        @Override
        protected Integer doInBackground(Integer... integers) {
            mBitmap = removeBackground(mBitmap);
            mBitmap = makeBlackTransparent(mBitmap);
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            mImageView.setImageBitmap(mBitmap);
            mProgressDialog.dismiss();
        }
    }

    //button-選擇圖片的onclick
    public void selectImg(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        Intent destIntent = Intent.createChooser(intent, "選擇圖片");
        startActivityForResult(destIntent, 0);
    }

    //button-處理圖片的onclick
    public void proccessImg(View v) {
        if (mBitmap == null) {
            mSnackbar.setText("請先選擇一張圖片");
            mSnackbar.show();
            return;
        }
        if(mRect==null)
        {
            mSnackbar.setText("請選取圖片中要保留的部分");
            mSnackbar.show();
            return;
        }
        ProcessImageTask task = new ProcessImageTask();
        task.execute();
    }

    public void endActivity(View v) {
        try {
            mBitmap = MyHelper.scaleBitmap(mBitmap, this, false);
            // 路徑
            String path = Environment.getExternalStorageDirectory().toString() + "/Tiny Chief/";

            // 開啟檔案
            File dir = new File(path);
            if (!dir.exists())
                dir.mkdirs();

            path += "tmparImg.png";
            File file = new File(path);
            file.createNewFile();
            file.setWritable(Boolean.TRUE);

            // 將 Bitmap壓縮成指定格式的圖片並寫入檔案串流
            FileOutputStream out = new FileOutputStream(file);
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

            // 刷新並關閉檔案串流
            out.flush();
            out.close();

            Intent intent = new Intent();
            intent.putExtra("AR_PIC", path);
            setResult(RESULT_OK, intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap removeBackground(Bitmap bitmap) {
        Mat matImage = new Mat();
        Mat matMask = new Mat();
        Mat matBgdModel = new Mat();
        Mat matFgdModel = new Mat();

        Utils.bitmapToMat(bitmap, matImage);
        Imgproc.cvtColor(matImage, matImage, Imgproc.COLOR_RGBA2RGB);

        Imgproc.grabCut(matImage, matMask, mRect, matBgdModel, matFgdModel, 5, Imgproc.GC_INIT_WITH_RECT);

        Mat matSource = new Mat(matImage.size(), CvType.CV_8U, new Scalar(Imgproc.GC_PR_FGD));
        Core.compare(matMask, matSource, matMask, Core.CMP_EQ);

        Mat matForeground = new Mat(matImage.size(), CvType.CV_8UC4);
        matImage.copyTo(matForeground, matMask);

        Utils.matToBitmap(matForeground, bitmap);
        bitmap = makeBlackTransparent(bitmap);

        bitmap = Bitmap.createBitmap(bitmap, mRect.x, mRect.y, mRect.width, mRect.height);

        return bitmap;
    }

    //將去背後圖片的背景設為透明
    private Bitmap makeBlackTransparent(Bitmap image) {
        // convert image to matrix
        Mat src = new Mat(image.getWidth(), image.getHeight(), CvType.CV_8UC4);
        Utils.bitmapToMat(image, src);

        // init new matrices
        Mat dst = new Mat(image.getWidth(), image.getHeight(), CvType.CV_8UC4);
        Mat tmp = new Mat(image.getWidth(), image.getHeight(), CvType.CV_8UC4);
        Mat alpha = new Mat(image.getWidth(), image.getHeight(), CvType.CV_8UC4);

        // convert image to grayscale
        Imgproc.cvtColor(src, tmp, Imgproc.COLOR_BGR2GRAY);

        // threshold the image to create alpha channel with complete transparency in black background region and zero transparency in foreground object region.
        Imgproc.threshold(tmp, alpha, 0, 255, Imgproc.THRESH_BINARY);

        // split the original image into three single channel.
        List<Mat> rgb = new ArrayList<Mat>(3);
        Core.split(src, rgb);

        // Create the final result by merging three single channel and alpha(BGRA order)
        List<Mat> rgba = new ArrayList<Mat>(4);
        rgba.add(rgb.get(0));
        rgba.add(rgb.get(1));
        rgba.add(rgb.get(2));
        rgba.add(alpha);
        Core.merge(rgba, dst);

        // convert matrix to output bitmap
        Bitmap output = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dst, output);
        return output;
    }
}
