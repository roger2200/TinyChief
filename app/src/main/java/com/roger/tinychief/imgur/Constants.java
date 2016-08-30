package com.roger.tinychief.imgur;

/**
 * Created by AKiniyalocts on 2/23/15.
 */
public class Constants {
    /*
      Logging flag
     */
    public static final boolean LOGGING = true;

    /*
      Your imgur client id. You need this to upload to imgur.

      More here: https://api.imgur.com/
     */
    public static final String MY_IMGUR_CLIENT_ID = "b21d13644cee35ba797945f231a95309bd6e1bea";
    public static final String MY_IMGUR_CLIENT_SECRET = "364d00a8451562d485cfd10e32932c5b266e3496";

    /*
      Redirect URL for android.
     */
    public static final String MY_IMGUR_REDIRECT_URL = "http://android";

    /*
      Client Auth
     */
    public static String getClientAuth() {
        return "Bearer " + MY_IMGUR_CLIENT_ID;
    }

}
