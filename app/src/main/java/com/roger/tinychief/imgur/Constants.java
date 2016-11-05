package com.roger.tinychief.imgur;

public class Constants {
    public static final boolean LOGGING = false;
    public static final String MY_IMGUR_CLIENT_ID = "fe3935a2abe49cc6411232cdfbedc283dec159fa";

    public static String getClientAuth() {
        return "Bearer " + MY_IMGUR_CLIENT_ID;
    }

}
