/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.


Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package com.roger.tinychief.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.roger.tinychief.ar.ArControl;
import com.roger.tinychief.ar.ArException;
import com.roger.tinychief.ar.ArRenderer;
import com.roger.tinychief.ar.ArSession;
import com.roger.tinychief.ar.menu.ArMenu;
import com.roger.tinychief.ar.menu.ArMenuGroup;
import com.roger.tinychief.ar.menu.ArMenuInterface;
import com.roger.tinychief.ar.utils.LoadingDialogHandler;
import com.roger.tinychief.ar.utils.ArGLView;
import com.roger.tinychief.ar.utils.Texture;
import com.vuforia.CameraDevice;
import com.vuforia.DataSet;
import com.vuforia.ObjectTracker;
import com.vuforia.STORAGE_TYPE;
import com.vuforia.State;
import com.vuforia.Trackable;
import com.vuforia.Tracker;
import com.vuforia.TrackerManager;
import com.vuforia.Vuforia;

import com.roger.tinychief.R;

import java.util.ArrayList;
import java.util.Vector;

public class ArActivity extends Activity implements ArControl, ArMenuInterface {
    private static final String LOGTAG = "ImageTargets";
    private static final int CAMERA_REQUEST_CODE = 1;

    ArSession vuforiaAppSession;
    private DataSet mCurrentDataset;
    private int mCurrentDatasetSelectionIndex = 0;
    private int mStartDatasetsIndex = 0;
    private int mDatasetsNumber = 0;
    private ArrayList<String> mDatasetStrings = new ArrayList<String>();

    // Our OpenGL view:
    private ArGLView mGlView;
    // Our renderer:
    private ArRenderer mRenderer;
    private GestureDetector mGestureDetector;
    // The textures we will use for rendering:
    private Vector<Texture> mTextures;
    private boolean mSwitchDatasetAsap = false;
    private boolean mFlash = false;
    private boolean mContAutofocus = false;
    private boolean mExtendedTracking = false;
    private View mFlashOptionView;
    private RelativeLayout mUILayout;
    private ArMenu mArMenu;
    public LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(this);
    // Alert Dialog used to display SDK errors
    private AlertDialog mErrorDialog;
    boolean mIsDroidDevice = false;

    private String path;

    // Called when the activity first starts or the user navigates back to an
    // activity.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOGTAG, "onCreate");
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getIntent().getExtras();
        path=bundle.getString("IMAGE_PATH");
        vuforiaAppSession = new ArSession(this);

        startLoadingAnimation();

        mDatasetStrings.add("StonesAndChips.xml");
        mDatasetStrings.add("Tarmac.xml");
        mDatasetStrings.add("Demo.xml");

        //檢查相機使用權限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        else
            vuforiaAppSession.initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mGestureDetector = new GestureDetector(this, new GestureListener());

        // Load any sample specific textures:
        mTextures = new Vector<>();
        loadTextures();

        mIsDroidDevice = android.os.Build.MODEL.toLowerCase().startsWith("droid");
    }

    // Process Single Tap event to trigger autofocus
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        // Used to set autofocus one second after a manual focus is triggered
        private final Handler autofocusHandler = new Handler();

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            // Generates a Handler to trigger autofocus
            // after 1 second
            autofocusHandler.postDelayed(new Runnable() {
                public void run() {
                    boolean result = CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO);
                    if (!result)
                        Log.e("SingleTapUp", "Unable to trigger focus");
                }
            }, 1000L);
            return true;
        }
    }

    // We want to load specific textures from the APK, which we will later use for rendering.
    private void loadTextures() {
        loadImage();
        mTextures.add(Texture.loadTextureFromApk("TextureTeapotBrass.png", getAssets()));
        mTextures.add(Texture.loadTextureFromApk("TextureTeapotBlue.png", getAssets()));
        mTextures.add(Texture.loadTextureFromApk("TextureTeapotRed.png", getAssets()));
        mTextures.add(Texture.loadTextureFromIntBuffer(imgData, width, height));
        mTextures.add(Texture.loadTextureFromApk("texturepizza.png", getAssets()));
    }

    int[] imgData;
    int width;
    int height;

    private void loadImage() {
        try {
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inSampleSize = 6;
            Bitmap bitmap = BitmapFactory.decodeFile(path,options);
            width=bitmap.getWidth();
            height=bitmap.getHeight();
            imgData=new int[width*height * 4];
            bitmap.getPixels(imgData,0,width,0,0,width,height);
            bitmap.recycle();
            System.gc();
        } catch (Exception e) {
        }
    }

    // Called when the activity will start interacting with the user.
    @Override
    protected void onResume() {
        Log.d(LOGTAG, "onResume");
        super.onResume();

        // This is needed for some Droid devices to force portrait
        if (mIsDroidDevice) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        try {
            vuforiaAppSession.resumeAR();
        } catch (ArException e) {
            Log.e(LOGTAG, e.getString());
        }

        // Resume the GL view:
        if (mGlView != null) {
            mGlView.setVisibility(View.VISIBLE);
            mGlView.onResume();
        }
    }

    // Callback for configuration changes the activity handles itself
    @Override
    public void onConfigurationChanged(Configuration config) {
        Log.d(LOGTAG, "onConfigurationChanged");
        super.onConfigurationChanged(config);

        vuforiaAppSession.onConfigurationChanged();
    }

    // Called when the system is about to start resuming a previous activity.
    @Override
    protected void onPause() {
        Log.d(LOGTAG, "onPause");
        super.onPause();

        if (mGlView != null) {
            mGlView.setVisibility(View.INVISIBLE);
            mGlView.onPause();
        }

        // Turn off the flash
        if (mFlashOptionView != null && mFlash) {
            // OnCheckedChangeListener is called upon changing the checked state
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                ((Switch) mFlashOptionView).setChecked(false);
            } else {
                ((CheckBox) mFlashOptionView).setChecked(false);
            }
        }

        try {
            vuforiaAppSession.pauseAR();
        } catch (ArException e) {
            Log.e(LOGTAG, e.getString());
        }
    }

    // The final call you receive before your activity is destroyed.
    @Override
    protected void onDestroy() {
        Log.d(LOGTAG, "onDestroy");
        super.onDestroy();

        try {
            vuforiaAppSession.stopAR();
        } catch (ArException e) {
            Log.e(LOGTAG, e.getString());
        }

        // Unload texture:
        mTextures.clear();
        mTextures = null;

        System.gc();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                vuforiaAppSession.initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    // Initializes AR application components.
    private void initApplicationAR() {
        // Create OpenGL ES view:
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();

        mGlView = new ArGLView(this);
        mGlView.init(translucent, depthSize, stencilSize);

        mRenderer = new ArRenderer(this, vuforiaAppSession);
        mRenderer.setTextures(mTextures);
        mGlView.setRenderer(mRenderer);

    }

    //讀取畫面
    private void startLoadingAnimation() {
        mUILayout = (RelativeLayout) View.inflate(this, R.layout.camera_overlay, null);

        mUILayout.setVisibility(View.VISIBLE);
        mUILayout.setBackgroundColor(Color.BLACK);

        // Gets a reference to the loading dialog
        loadingDialogHandler.mLoadingDialogContainer = mUILayout.findViewById(R.id.loading_indicator);

        // Shows the loading indicator at start
        loadingDialogHandler.sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);

        // Adds the inflated layout to the view
        addContentView(mUILayout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    // Methods to load and destroy tracking data.
    @Override
    public boolean doLoadTrackersData() {
        TrackerManager tManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) tManager.getTracker(ObjectTracker.getClassType());
        if (objectTracker == null)
            return false;
        if (mCurrentDataset == null)
            mCurrentDataset = objectTracker.createDataSet();
        if (mCurrentDataset == null)
            return false;
        if (!mCurrentDataset.load(mDatasetStrings.get(mCurrentDatasetSelectionIndex), STORAGE_TYPE.STORAGE_APPRESOURCE))
            return false;
        if (!objectTracker.activateDataSet(mCurrentDataset))
            return false;
        int numTrackables = mCurrentDataset.getNumTrackables();
        for (int count = 0; count < numTrackables; count++) {
            Trackable trackable = mCurrentDataset.getTrackable(count);
            if (isExtendedTrackingActive())
                trackable.startExtendedTracking();

            String name = "Current Dataset : " + trackable.getName();
            trackable.setUserData(name);
            Log.d(LOGTAG, "UserData:Set the following user data " + (String) trackable.getUserData());
        }
        return true;
    }

    @Override
    public boolean doUnloadTrackersData() {
        // Indicate if the trackers were unloaded correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) tManager.getTracker(ObjectTracker.getClassType());
        if (objectTracker == null)
            return false;

        if (mCurrentDataset != null && mCurrentDataset.isActive()) {
            if (objectTracker.getActiveDataSet().equals(mCurrentDataset) && !objectTracker.deactivateDataSet(mCurrentDataset))
                result = false;
            else if (!objectTracker.destroyDataSet(mCurrentDataset))
                result = false;

            mCurrentDataset = null;
        }
        return result;
    }

    @Override
    public void onInitARDone(ArException exception) {

        if (exception == null) {
            initApplicationAR();

            mRenderer.mIsActive = true;

            // Now add the GL surface view. It is important
            // that the OpenGL ES surface view gets added
            // BEFORE the camera is started and video
            // background is configured.
            addContentView(mGlView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

            // Sets the UILayout to be drawn in front of the camera
            mUILayout.bringToFront();

            // Sets the layout background to transparent
            mUILayout.setBackgroundColor(Color.TRANSPARENT);

            try {
                vuforiaAppSession.startAR(CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT);
            } catch (ArException e) {
                Log.e(LOGTAG, e.getString());
            }

            boolean result = CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);

            if (result)
                mContAutofocus = true;
            else
                Log.e(LOGTAG, "Unable to enable continuous autofocus");

            mArMenu = new ArMenu(this, this, "Image Targets", mGlView, mUILayout, null, mRenderer);
            setSampleAppMenuSettings();

        } else {
            Log.e(LOGTAG, exception.getString());
            showInitializationErrorMessage(exception.getString());
        }
    }

    // Shows initialization error messages as System dialogs
    public void showInitializationErrorMessage(String message) {
        final String errorMessage = message;
        runOnUiThread(new Runnable() {
            public void run() {
                if (mErrorDialog != null)
                    mErrorDialog.dismiss();

                // Generates an Alert Dialog to show the error message
                AlertDialog.Builder builder = new AlertDialog.Builder(ArActivity.this);
                builder.setMessage(errorMessage).setTitle(getString(R.string.INIT_ERROR)).setCancelable(false).setIcon(0).setPositiveButton(getString(R.string.button_OK), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });

                mErrorDialog = builder.create();
                mErrorDialog.show();
            }
        });
    }

    @Override
    public void onVuforiaUpdate(State state) {
        if (mSwitchDatasetAsap) {
            mSwitchDatasetAsap = false;
            TrackerManager tm = TrackerManager.getInstance();
            ObjectTracker ot = (ObjectTracker) tm.getTracker(ObjectTracker.getClassType());
            if (ot == null || mCurrentDataset == null || ot.getActiveDataSet() == null) {
                Log.d(LOGTAG, "Failed to swap datasets");
                return;
            }

            doUnloadTrackersData();
            doLoadTrackersData();
        }
    }

    @Override
    public boolean doInitTrackers() {
        // Indicate if the trackers were initialized correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        Tracker tracker;

        // Trying to initialize the image tracker
        tracker = tManager.initTracker(ObjectTracker.getClassType());
        if (tracker == null) {
            Log.e(LOGTAG, "Tracker not initialized. Tracker already initialized or the camera is already started");
            result = false;
        } else {
            Log.i(LOGTAG, "Tracker successfully initialized");
        }
        return result;
    }

    @Override
    public boolean doStartTrackers() {
        // Indicate if the trackers were started correctly
        boolean result = true;

        Tracker objectTracker = TrackerManager.getInstance().getTracker(ObjectTracker.getClassType());
        if (objectTracker != null)
            objectTracker.start();

        return result;
    }

    @Override
    public boolean doStopTrackers() {
        // Indicate if the trackers were stopped correctly
        boolean result = true;

        Tracker objectTracker = TrackerManager.getInstance().getTracker(ObjectTracker.getClassType());
        if (objectTracker != null)
            objectTracker.stop();

        return result;
    }

    @Override
    public boolean doDeinitTrackers() {
        // Indicate if the trackers were deinitialized correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        tManager.deinitTracker(ObjectTracker.getClassType());

        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Process the Gestures
        if (mArMenu != null && mArMenu.processEvent(event))
            return true;
        return mGestureDetector.onTouchEvent(event);
    }

    boolean isExtendedTrackingActive() {
        return mExtendedTracking;
    }

    final public static int CMD_BACK = -1;
    final public static int CMD_EXTENDED_TRACKING = 1;
    final public static int CMD_AUTOFOCUS = 2;
    final public static int CMD_FLASH = 3;
    final public static int CMD_CAMERA_FRONT = 4;
    final public static int CMD_CAMERA_REAR = 5;
    final public static int CMD_DATASET_START_INDEX = 6;

    // This method sets the menu's settings
    private void setSampleAppMenuSettings() {
        ArMenuGroup group;

        group = mArMenu.addGroup("", false);
        group.addTextItem(getString(R.string.menu_back), -1);

        group = mArMenu.addGroup("", true);
        group.addSelectionItem(getString(R.string.menu_extended_tracking), CMD_EXTENDED_TRACKING, false);
        group.addSelectionItem(getString(R.string.menu_contAutofocus), CMD_AUTOFOCUS, mContAutofocus);
        mFlashOptionView = group.addSelectionItem(getString(R.string.menu_flash), CMD_FLASH, false);

        CameraInfo ci = new CameraInfo();
        boolean deviceHasFrontCamera = false;
        boolean deviceHasBackCamera = false;
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, ci);
            if (ci.facing == CameraInfo.CAMERA_FACING_FRONT)
                deviceHasFrontCamera = true;
            else if (ci.facing == CameraInfo.CAMERA_FACING_BACK)
                deviceHasBackCamera = true;
        }

        if (deviceHasBackCamera && deviceHasFrontCamera) {
            group = mArMenu.addGroup(getString(R.string.menu_camera), true);
            group.addRadioItem(getString(R.string.menu_camera_front), CMD_CAMERA_FRONT, false);
            group.addRadioItem(getString(R.string.menu_camera_back), CMD_CAMERA_REAR, true);
        }

        group = mArMenu.addGroup(getString(R.string.menu_datasets), true);
        mStartDatasetsIndex = CMD_DATASET_START_INDEX;
        mDatasetsNumber = mDatasetStrings.size();
        //加按鈕
        group.addRadioItem("Roger", mStartDatasetsIndex + 2, true);
        group.addRadioItem("Stones & Chips", mStartDatasetsIndex, false);
        group.addRadioItem("Tarmac", mStartDatasetsIndex + 1, false);

        mArMenu.attachMenu();
    }


    @Override
    public boolean menuProcess(int command) {

        boolean result = true;

        switch (command) {
            case CMD_BACK:
                finish();
                break;

            case CMD_FLASH:
                result = CameraDevice.getInstance().setFlashTorchMode(!mFlash);

                if (result) {
                    mFlash = !mFlash;
                } else {
                    showToast(getString(mFlash ? R.string.menu_flash_error_off : R.string.menu_flash_error_on));
                    Log.e(LOGTAG,
                            getString(mFlash ? R.string.menu_flash_error_off : R.string.menu_flash_error_on));
                }
                break;

            case CMD_AUTOFOCUS:

                if (mContAutofocus) {
                    result = CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_NORMAL);

                    if (result) {
                        mContAutofocus = false;
                    } else {
                        showToast(getString(R.string.menu_contAutofocus_error_off));
                        Log.e(LOGTAG, getString(R.string.menu_contAutofocus_error_off));
                    }
                } else {
                    result = CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);

                    if (result) {
                        mContAutofocus = true;
                    } else {
                        showToast(getString(R.string.menu_contAutofocus_error_on));
                        Log.e(LOGTAG, getString(R.string.menu_contAutofocus_error_on));
                    }
                }

                break;

            case CMD_CAMERA_FRONT:
            case CMD_CAMERA_REAR:

                // Turn off the flash
                if (mFlashOptionView != null && mFlash) {
                    // OnCheckedChangeListener is called upon changing the checked state
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                        ((Switch) mFlashOptionView).setChecked(false);
                    else
                        ((CheckBox) mFlashOptionView).setChecked(false);

                }

                vuforiaAppSession.stopCamera();

                try {
                    vuforiaAppSession.startAR(command == CMD_CAMERA_FRONT ? CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_FRONT : CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_BACK);
                } catch (ArException e) {
                    showToast(e.getString());
                    Log.e(LOGTAG, e.getString());
                    result = false;
                }
                doStartTrackers();
                break;

            case CMD_EXTENDED_TRACKING:
                for (int tIdx = 0; tIdx < mCurrentDataset.getNumTrackables(); tIdx++) {
                    Trackable trackable = mCurrentDataset.getTrackable(tIdx);

                    if (!mExtendedTracking) {
                        if (!trackable.startExtendedTracking()) {
                            Log.e(LOGTAG, "Failed to start extended tracking target");
                            result = false;
                        } else {
                            Log.d(LOGTAG, "Successfully started extended tracking target");
                        }
                    } else {
                        if (!trackable.stopExtendedTracking()) {
                            Log.e(LOGTAG, "Failed to stop extended tracking target");
                            result = false;
                        } else {
                            Log.d(LOGTAG, "Successfully started extended tracking target");
                        }
                    }
                }

                if (result)
                    mExtendedTracking = !mExtendedTracking;

                break;

            default:
                if (command >= mStartDatasetsIndex && command < mStartDatasetsIndex + mDatasetsNumber) {
                    mSwitchDatasetAsap = true;
                    mCurrentDatasetSelectionIndex = command - mStartDatasetsIndex;
                }
                break;
        }

        return result;
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}