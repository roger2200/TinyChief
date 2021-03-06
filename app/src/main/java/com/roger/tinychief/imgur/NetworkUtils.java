package com.roger.tinychief.imgur;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by AKiniyalocts on 1/15/15.
 * <p/>
 * Basic network utils
 */
public class NetworkUtils {
    public static final String TAG = NetworkUtils.class.getSimpleName();

    public static boolean isConnected(Context mContext) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            }
        } catch (Exception ex) {
            aLog.w(TAG, ex.getMessage());
        }
        return false;
    }
}
