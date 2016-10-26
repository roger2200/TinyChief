package com.roger.tinychief.imgur;

public class Constants {
    public static final boolean LOGGING = false;
    public static final String MY_IMGUR_CLIENT_ID = "b55ca1b8a204a2ef282dcdc19c91cc96eb2ac508";

    public static String getClientAuth() {
        return "Bearer " + MY_IMGUR_CLIENT_ID;
    }

}
