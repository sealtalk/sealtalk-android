package io.rong.callkit.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.AppOpsManagerCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import java.math.BigDecimal;

import io.rong.callkit.R;

/**
 * Created by dengxudong on 2018/5/17.
 */

public class CallKitUtils {

    /**
     * 拨打true or 接听false
     */
    public static boolean isDial = true;
    public static boolean shouldShowFloat;
    /**
     * 是否已经建立通话连接
     * 默认没有，为了修改接听之后将情景模式切换成震动 在通话界面一直震动的问题
     */
    public static boolean callConnected = false;
    /**
     * true:响铃中，false：响铃已结束
     */
    //public static boolean RINGSTATE=false;
    /**
     * 当前 免提 是否打开的状态 true：打开中
     */
    public static boolean speakerphoneState = false;
    public static StringBuffer stringBuffer = null;

    public static Drawable BackgroundDrawable(int drawable, Context context) {
        return ContextCompat.getDrawable(context, drawable);
    }

    public static int dp2px(float dpVal, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }

    /**
     * 关闭软键盘
     *
     * @param activity
     * @param view
     */
    public static void closeKeyBoard(Activity activity, View view) {
        IBinder token;
        if (view == null || view.getWindowToken() == null) {
            if (null == activity) {
                return;
            }
            Window window = activity.getWindow();
            if (window == null) {
                return;
            }
            View v = window.peekDecorView();
            if (v == null) {
                return;
            }
            token = v.getWindowToken();
        } else {
            token = view.getWindowToken();
        }
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(token, 0);
    }

    /**
     * 提供（相对）精确的除法运算。
     *
     * @param vl1 被除数
     * @param vl2 除数
     * @return 商
     */
    public static double div(double vl1, double vl2) {

        BigDecimal b1 = new BigDecimal(vl1);
        BigDecimal b2 = new BigDecimal(vl2);
        //4 表示表示需要精确到小数点以后几位。当发生除不尽的情况时，参数指定精度，以后的数字四舍五入。
        return b1.divide(b2, 4, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 四舍五入把double转化int整型
     *
     * @param number
     * @return
     */
    public static int getInt(double number) {
        BigDecimal bd = new BigDecimal(number).setScale(0, BigDecimal.ROUND_HALF_UP);
        return Integer.parseInt(bd.toString());
    }

    public static void textViewShadowLayer(TextView text, Context context) {
        if (null == text) {
            return;
        }
        text.setShadowLayer(16F, 0F, 2F, context.getApplicationContext().getResources().getColor(R.color.callkit_shadowcolor));
    }


    public static boolean checkPermissions(Context context, @NonNull String[] permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        if (permissions == null || permissions.length == 0) {
            return true;
        }
        for (String permission : permissions) {
            if (!hasPermission(context, permission)) {
                return false;
            }
        }
        return true;
    }


    private static boolean hasPermission(Context context, String permission) {
        String opStr = AppOpsManagerCompat.permissionToOp(permission);
        if (opStr == null) {
            return true;
        }
        boolean bool = context.checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        return bool;
    }

    public static String[] getCallpermissions() {
        String[] permissions = new String[]{Manifest.permission.PROCESS_OUTGOING_CALLS, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET, Manifest.permission.READ_PHONE_STATE, Manifest.permission.MODIFY_AUDIO_SETTINGS, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN};
        return permissions;
    }

    /**
     * 获取字符串指定拼接内容
     *
     * @param val1
     * @param val2
     * @return
     */
    public static String getStitchedContent(String val1, String val2) {
        if (TextUtils.isEmpty(val1)) {
            val1 = "";
        }
        if (TextUtils.isEmpty(val2)) {
            val2 = "";
        }
        if (stringBuffer == null) {
            stringBuffer = new StringBuffer();
        } else {
            stringBuffer.setLength(0);
        }
        stringBuffer.append(val1);
        stringBuffer.append(val2);
        return stringBuffer.toString();
    }
}
