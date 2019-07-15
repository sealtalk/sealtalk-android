package io.rong.pushperm.log;

import android.util.Log;

/**
 * Log
 */
public class LLog {

    private static boolean isDebug = true;
    private static final String TAG = "PushPermission=>";

    private static boolean isDebug() {
        return isDebug;
    }

    public static  void i (String tag, String msg) {
        Log.i(TAG, "[" + tag + "]" + msg);
    }

    public static  void d (String tag, String msg) {
        if (isDebug()) {
            Log.d(TAG, "[" + tag + "]" + msg);
        }
    }

    public static void e (String tag, String msg) {
        Log.e(TAG, "[" + tag + "]" + msg);
    }

}
