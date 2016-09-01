package com.roger.tinychief.imgur;

import android.content.Context;
import android.util.Log;

import java.lang.ref.WeakReference;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

/**
 * Created by AKiniyalocts on 1/12/15.
 * <p/>
 * Our upload service. This creates our restadapter, uploads our image, and notifies us of the response.
 */
public class UploadService {
    public final static String TAG = UploadService.class.getSimpleName();

    private WeakReference<Context> mContext;

    public UploadService(Context context) {
        this.mContext = new WeakReference<>(context);
    }

    public void Execute(Upload upload, Callback<ImageResponse> callback) {
        final Callback<ImageResponse> cb = callback;

        if (!NetworkUtils.isConnected(mContext.get())) {
            //Callback will be called, so we prevent a unnecessary notification
            cb.failure(null);
            return;
        }

        RestAdapter restAdapter = buildRestAdapter();

        restAdapter.create(ImgurAPI.class).postImage(
                Constants.getClientAuth(),
                upload.title,
                upload.description,
                upload.albumId,
                null,
                new TypedFile("image/*", upload.image),
                new Callback<ImageResponse>() {
                    @Override
                    public void success(ImageResponse imageResponse, Response response) {
                        if (cb != null) cb.success(imageResponse, response);
                        if (response == null) {
                            //Notify image was NOT uploaded successfully
                            Log.e(TAG, "Upload fail "+imageResponse);
                            return;
                        }
                        //Notify image was uploaded successfully
                        if (imageResponse.success)
                            Log.d(TAG, "Upload success");
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (cb != null) cb.failure(error);
                        Log.e(TAG, "Upload fail "+error);
                    }
                });
    }

    private RestAdapter buildRestAdapter() {
        RestAdapter imgurAdapter = new RestAdapter.Builder()
                .setEndpoint(ImgurAPI.server)
                .build();

        //Set rest adapter logging if we're already logging
        if (Constants.LOGGING)
            imgurAdapter.setLogLevel(RestAdapter.LogLevel.FULL);
        return imgurAdapter;
    }
}
