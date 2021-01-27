package cn.rongcloud.im.utils.log;

import android.content.Context;

public class SLog {
    public static void init(Context context){
        SLogCreator.sInstance.init(context);
    }

    public static void i(String tag, String msg){
        SLogCreator.sInstance.i(tag, msg);
    }

    public static void i(String tag, String msg, Throwable tr){
        SLogCreator.sInstance.i(tag, msg, tr);
    }

    public static void v(String tag, String msg){
        SLogCreator.sInstance.v(tag, msg);
    }

    public static void v(String tag, String msg, Throwable tr){
        SLogCreator.sInstance.v(tag, msg, tr);
    }
    public static void d(String tag, String msg){
        SLogCreator.sInstance.d(tag, msg);
    }

    public static void d(String tag, String msg, Throwable tr){
        SLogCreator.sInstance.d(tag, msg, tr);
    }
    public static void w(String tag, String msg){
        SLogCreator.sInstance.w(tag, msg);
    }

    public static void w(String tag, String msg, Throwable tr){
        SLogCreator.sInstance.w(tag, msg, tr);
    }

    public static void e(String tag, String msg){
        SLogCreator.sInstance.e(tag, msg);
    }

    public static void e(String tag, String msg, Throwable tr){
        SLogCreator.sInstance.e(tag, msg, tr);
    }

    private static class SLogCreator{
        // 使用其他Log请替换此实现
        public final static ISLog sInstance = new SimpleDebugSLog();
    }
}
