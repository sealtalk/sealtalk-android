/*
    ShengDao Android Client, NToast
    Copyright (c) 2014 ShengDao Tech Company Limited
 */

package cn.rongcloud.im.server.utils;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;


public class NToast {

    public static void shortToast(Context context, int resId) {
        showToast(context, resId, Toast.LENGTH_SHORT);
    }

    public static void shortToast(Context context, String text) {
        if (!TextUtils.isEmpty(text) && !"".equals(text.trim())) {
            showToast(context, text, Toast.LENGTH_SHORT);
        }
    }

    public static void longToast(Context context, int resId) {
        showToast(context, resId, Toast.LENGTH_LONG);
    }

    public static void longToast(Context context, String text) {
        if (!TextUtils.isEmpty(text) && !"".equals(text.trim())) {
            showToast(context, text, Toast.LENGTH_LONG);
        }
    }

    public static void showToast(Context context, int resId, int duration) {
        if (context == null) {
            return;
        }
        if (context != null && context instanceof Activity) {
            if (((Activity) context).isFinishing()) {
                return;
            }
        }
        String text = context.getString(resId);
        showToast(context, text, duration);
    }

    public static void showToast(Context context, String text, int duration) {
        if (context == null) {
            return;
        }
        if (context != null && context instanceof Activity) {
            if (((Activity) context).isFinishing()) {
                return;
            }
        }
        if (!TextUtils.isEmpty(text) && !"".equals(text.trim())) {
            Toast.makeText(context, text, duration).show();
        }
    }
}
