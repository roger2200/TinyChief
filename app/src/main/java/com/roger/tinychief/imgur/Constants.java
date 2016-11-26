package com.roger.tinychief.imgur;

public class Constants {
    public static final boolean LOGGING = false;
    public static final String MY_IMGUR_CLIENT_ID = "6d61b29074e0de0fdd41af65cfbd157c939954cf";

    public static String getClientAuth() {
        return "Bearer " + MY_IMGUR_CLIENT_ID;
    }

}
