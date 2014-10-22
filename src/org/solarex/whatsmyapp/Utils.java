package org.solarex.whatsmyapp;

import android.util.Log;

public class Utils {
    private static final String TAG = "TestAPP";
    private Utils(){}
    public static void log(String msg){
        Log.d(TAG, msg != null ? msg : "null");
    }
}
