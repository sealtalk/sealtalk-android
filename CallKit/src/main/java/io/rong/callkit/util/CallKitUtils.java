package io.rong.callkit.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
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
    public static boolean isDial=true;
    public static boolean shouldShowFloat;

    public static Drawable BackgroundDrawable(int drawable, Context context){
        return ContextCompat.getDrawable(context, drawable);
    }

    public static int dp2px(float dpVal,Context context) {
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

    public static void textViewShadowLayer(TextView text, Context context){
        if(null==text){return;}
        text.setShadowLayer(16F, 0F, 2F, context.getApplicationContext().getResources().getColor(R.color.callkit_shadowcolor));
    }
}
