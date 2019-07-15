package cn.rongcloud.im.utils.log;

import android.content.Context;

public interface ISLog {
    void init(Context context);
    void i(String tag, String msg);
    void i(String tag, String msg, Throwable tr);
    void v(String tag, String msg);
    void v(String tag, String msg, Throwable tr);
    void d(String tag, String msg);
    void d(String tag, String msg, Throwable tr);
    void e(String tag, String msg);
    void e(String tag, String msg, Throwable tr);
    void w(String tag, String msg);
    void w(String tag, String msg, Throwable tr);
}
