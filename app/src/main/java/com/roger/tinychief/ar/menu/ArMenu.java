/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package com.roger.tinychief.ar.menu;

import android.app.Activity;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.roger.tinychief.R;
import com.roger.tinychief.ar.ArRenderer;

import java.util.ArrayList;


// Handles the sample apps menu settings
public class ArMenu {
    protected static final String SwipeSettingsInterface = null;
    private static final String LOGTAG = "ArMenu";

    private GestureListener mGestureListener;
    private GestureDetector mGestureDetector;
    private ArMenuAnimator mMenuAnimator;
    private Activity mActivity;
    private ArMenuInterface mMenuInterface;
    private GLSurfaceView mMovableView;
    private ArMenuView mParentMenuView;
    private LinearLayout mMovableListView;
    private ArrayList<ArMenuGroup> mSettingsItems = new ArrayList<ArMenuGroup>();
    private ArRenderer mRenderer;

    private ArrayList<View> mAdditionalViews;
    private float mInitialAdditionalViewsX[];
    private int mScreenWidth;
    private int mListViewWidth = 0;
    private double oldDist = 0;

    // True if dragging and displaying the menu
    boolean mSwipingMenu = false;

    boolean multipleTouch;
    // True if menu is showing and docked
    boolean mStartMenuDisplaying = false;

    float mGingerbreadMenuClipping = 0;

    private static float SETTINGS_MENU_SCREEN_PERCENTAGE = .80f;
    private static float SETTINGS_MENU_SCREEN_MIN_PERCENTAGE_TO_SHOW = .1f;
    boolean mIsBelowICS = Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH;


    // Parameters:
    // menuInterface - Reference to the object which will be handling the
    // processes from the menu entries
    // activity - The activity where the swipe menu will be used
    // menuTitle - Title to be displayed
    // movableView - SurfaceView where the OpenGL rendering is done
    // listView - Parent view where the settings layout will be attached
    // additionalViewToHide - Additional view to move with openGl view
    public ArMenu(ArMenuInterface menuInterface, Activity activity, String menuTitle, GLSurfaceView movableView, RelativeLayout parentView, ArrayList<View> additionalViewsToHide, ArRenderer renderer) {
        mMenuInterface = menuInterface;
        mActivity = activity;
        mMovableView = movableView;
        mAdditionalViews = additionalViewsToHide;
        mRenderer = renderer;

        LayoutInflater inflater = LayoutInflater.from(mActivity);
        mParentMenuView = (ArMenuView) inflater.inflate(R.layout.menu_layer, null, false);
        parentView.addView(mParentMenuView);

        mMovableListView = (LinearLayout) mParentMenuView.findViewById(R.id.settings_menu);
        mMovableListView.setBackgroundColor(Color.WHITE);

        TextView title = (TextView) mMovableListView.findViewById(R.id.settings_menu_title);
        title.setText(menuTitle);

        mMovableView.setVisibility(View.VISIBLE);

        if (mAdditionalViews != null && mAdditionalViews.size() > 0)
            mInitialAdditionalViewsX = new float[mAdditionalViews.size()];

        mGestureListener = new GestureListener();
        mGestureDetector = new GestureDetector(mActivity, mGestureListener);

        if (!mIsBelowICS)
            mMenuAnimator = new ArMenuAnimator(this);

        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;

        // Used to set the listView length depending on the glView width
        ViewTreeObserver vto = mMovableView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                int menuWidth = Math.min(mMovableView.getWidth(), mMovableView.getHeight());
                mListViewWidth = (int) (menuWidth * SETTINGS_MENU_SCREEN_PERCENTAGE);

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mListViewWidth, RelativeLayout.LayoutParams.MATCH_PARENT);

                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                mParentMenuView.setLayoutParams(params);

                setMenuDisplaying(false);
                mGestureListener.setMaxSwipe(mListViewWidth);

                LinearLayout.LayoutParams groupParams = new LinearLayout.LayoutParams(mListViewWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
                for (ArMenuGroup group : mSettingsItems)
                    group.getMenuLayout().setLayoutParams(groupParams);

                mMovableView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }

    public boolean processEvent(MotionEvent event) {
        boolean result = false;
        if (!multipleTouch)
            result = mGestureDetector.onTouchEvent(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_UP:
                if (!mGestureDetector.onTouchEvent(event)) {
                    setSwipingMenu(false);
                    // Hides the menu if it is not docked when releasing
                    if (!isMenuDisplaying() || getViewX(mMovableView) < (mScreenWidth * SETTINGS_MENU_SCREEN_PERCENTAGE))
                        if (isMenuDisplaying() || getViewX(mMovableView) < (mScreenWidth * SETTINGS_MENU_SCREEN_MIN_PERCENTAGE_TO_SHOW))
                            hideMenu();
                        else
                            showMenu();
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                multipleTouch = false;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                multipleTouch = true;
                break;
            case MotionEvent.ACTION_MOVE:
                if (multipleTouch) {
                    double newDist = spacing(event);
                    if (newDist > 10f) {
                        mRenderer.resizeFood(newDist / oldDist);
                        oldDist=newDist;
                    }
                }
                break;
        }
        return result;
    }

    private double spacing(MotionEvent event) {
        double x = event.getX(0) - event.getX(1);
        double y = event.getY(0) - event.getY(1);
        return Math.sqrt(x * x + y * y);
    }

    private void startViewsAnimation(boolean display) {
        float targetX = display ? mGestureListener.getMaxSwipe() : 0;

        mMenuAnimator.setStartEndX(getViewX(mMovableView), targetX);
        mMenuAnimator.start();

        if (mAdditionalViews != null)
            for (int i = 0; i < mAdditionalViews.size(); i++)
                setViewX(mAdditionalViews.get(i), mInitialAdditionalViewsX[i] + targetX);
    }

    public void setSwipingMenu(boolean isSwiping) {
        mSwipingMenu = isSwiping;
    }

    public boolean isMenuDisplaying() {
        return mStartMenuDisplaying;
    }

    public void setMenuDisplaying(boolean isMenuDisplaying) {
        // This is used to avoid the ListView to consume the incoming event when
        // the menu is not displayed.
        mParentMenuView.setFocusable(isMenuDisplaying);
        mParentMenuView.setFocusableInTouchMode(isMenuDisplaying);
        mParentMenuView.setClickable(isMenuDisplaying);
        mParentMenuView.setEnabled(isMenuDisplaying);

        mStartMenuDisplaying = isMenuDisplaying;
    }

    public void hide() {
        setViewX(mMovableView, 0);

        mParentMenuView.setHorizontalClipping(0);
        mParentMenuView.setVisibility(View.GONE);

        if (mAdditionalViews != null && !mIsBelowICS)
            for (int i = 0; i < mAdditionalViews.size(); i++)
                setViewX(mAdditionalViews.get(i), mInitialAdditionalViewsX[i]);
    }


    private void setViewX(View view, float x) {
        if (!mIsBelowICS)
            view.setX(x);
        else
            mGingerbreadMenuClipping = x;
    }


    private float getViewX(View view) {
        float x;
        if (!mIsBelowICS)
            x = view.getX();
        else
            x = mGingerbreadMenuClipping;
        return x;
    }


    public void showMenu() {
        if (!mIsBelowICS)
            startViewsAnimation(true);
        else {
            setAnimationX(mGestureListener.getMaxSwipe());
            setMenuDisplaying(true);
        }
    }


    public void hideMenu() {
        if (!mIsBelowICS) {
            if (!mMenuAnimator.isRunning()) {
                startViewsAnimation(false);
                setMenuDisplaying(false);
            }
        } else {
            hide();
            setMenuDisplaying(false);
        }
    }


    public ArMenuGroup addGroup(String string, boolean hasTitle) {
        ArMenuGroup newGroup = new ArMenuGroup(mMenuInterface, mActivity, this, hasTitle, string, 700);
        mSettingsItems.add(newGroup);
        return mSettingsItems.get(mSettingsItems.size() - 1);
    }


    public void attachMenu() {
        for (ArMenuGroup group : mSettingsItems)
            mMovableListView.addView(group.getMenuLayout());

        View newView = new View(mActivity);
        newView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        newView.setBackgroundColor(Color.parseColor("#000000"));
        mMovableListView.addView(newView);
        hide();
        setMenuDisplaying(false);
    }


    public void setAnimationX(float animtationX) {
        mParentMenuView.setVisibility(View.VISIBLE);
        setViewX(mMovableView, animtationX);

        mParentMenuView.setHorizontalClipping((int) animtationX);

        if (mAdditionalViews != null)
            for (int i = 0; i < mAdditionalViews.size(); i++)
                setViewX(mAdditionalViews.get(i), mInitialAdditionalViewsX[i] + animtationX);
    }


    public void setDockMenu(boolean isDocked) {
        setMenuDisplaying(isDocked);
        if (!isDocked)
            hideMenu();
    }

    // Process the gestures to handle the menu
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        // Minimum distance to start displaying the menu
        int DISTANCE_TRESHOLD = 10;
        // Minimum velocity to display the menu upon fling
        int VELOCITY_TRESHOLD = 2000;

        // Maximum x to dock the menu
        float mMaxXSwipe;

        // Called when dragging
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (Math.abs(distanceX) > DISTANCE_TRESHOLD && !mSwipingMenu) {
                mSwipingMenu = true;
                mParentMenuView.setVisibility(View.VISIBLE);

                if (mAdditionalViews != null && !mIsBelowICS && !mStartMenuDisplaying)
                    for (int i = 0; i < mAdditionalViews.size(); i++)
                        mInitialAdditionalViewsX[i] = getViewX(mAdditionalViews.get(i));
            }

            if (mSwipingMenu && mMovableView != null && (getViewX(mMovableView) - distanceX > 0)) {
                float deltaX = Math.min(mMaxXSwipe, getViewX(mMovableView) - distanceX);

                setViewX(mMovableView, deltaX);

                mParentMenuView.setHorizontalClipping((int) deltaX);

                if (mAdditionalViews != null && !mIsBelowICS)
                    for (int i = 0; i < mAdditionalViews.size(); i++)
                        setViewX(mAdditionalViews.get(i), mInitialAdditionalViewsX[i] + deltaX);
            }

            if (mMaxXSwipe <= getViewX(mMovableView)) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        showMenu();
                    }

                }, 100L);
            }
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (velocityX > VELOCITY_TRESHOLD && !isMenuDisplaying())
                showMenu();
            return false;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            boolean consumeTapUp = isMenuDisplaying();
            hideMenu();

            return consumeTapUp;
        }


        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (!isMenuDisplaying()) {
                if (!mIsBelowICS) {
                    startViewsAnimation(true);
                } else {
                    setAnimationX(mMaxXSwipe);
                    setMenuDisplaying(true);
                }
            }
            return true;
        }


        // Percentage of the screen to display and maintain the menu
        public void setMaxSwipe(float maxXSwipe) {
            mMaxXSwipe = maxXSwipe;
            if (!mIsBelowICS) {
                mMenuAnimator.setMaxX(mMaxXSwipe);
                mMenuAnimator.setStartEndX(0.0f, mMaxXSwipe);
            }
        }


        public float getMaxSwipe() {
            return mMaxXSwipe;
        }

    }

}
