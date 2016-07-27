package com.roger.tinychief.util;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class NetworkManager {

    private static NetworkManager sInstance;

    private RequestQueue mQueue;

    private NetworkManager(Context context) {
        mQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public static NetworkManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new NetworkManager(context);
        }
        return sInstance;
    }

    public void request(String tag, Request<?> request) {
        if (tag != null) {
            request.setTag(tag);
        }
        mQueue.add(request);
    }

    public void cancelRequest(String tag) {
        if (tag != null) {
            mQueue.cancelAll(tag);
        }
    }

    public void stop() {
        mQueue.stop();
    }
    public void check(){
        System.out.println("create get in network");
    }
    public void checkRes(){
        System.out.println("onResume get in network");
    }

}
