/*
    ShengDao Android Client, NLog
    Copyright (c) 2014 ShengDao Tech Company Limited
 */

package cn.rongcloud.im.server.utils;

import android.util.Log;


public class NLog {

    private static final String LOG_FORMAT = "%1$s\n%2$s";
    private static boolean isDebug = false;

    public static void d(String tag, Object... args) {
        log(Log.DEBUG, null, tag, args);
    }

    public static void i(String tag, Object... args) {
        log(Log.INFO, null, tag, args);
    }

    public static void w(String tag, Object... args) {
        log(Log.WARN, null, tag, args);
    }

    public static void e(Throwable ex) {
        log(Log.ERROR, ex, null);
    }

    public static void e(String tag, Object... args) {
        log(Log.ERROR, null, tag, args);
    }

    public static void e(Throwable ex, String tag, Object... args) {
        log(Log.ERROR, ex, tag, args);
    }

    private static void log(int priority, Throwable ex, String tag, Object... args) {

        if (isDebug == false) return;

        String log = "";
        if (ex == null) {
            if (args != null && args.length > 0) {
                for (Object obj : args) {
                    log += String.valueOf(obj);
                }
            }
        } else {
            String logMessage = ex.getMessage();
            String logBody = Log.getStackTraceString(ex);
            log = String.format(LOG_FORMAT, logMessage, logBody);
        }
        Log.println(priority, tag, log);
    }

    public static boolean isDebug() {
        return isDebug;
    }

    public static void setDebug(boolean isDebug) {
        NLog.isDebug = isDebug;
    }

}
