package cn.rongcloud.im.utils;

import android.content.Context;
import android.os.Vibrator;


/**
 *  手机震动相关工具
 */
public class VibratorUtils {
    /**
     * 开始震动
     * @param context
     * @param pattern 震动规则
     * @param repeat 循环次数
     */
    public static void startVibrator(Context context, long[] pattern, int repeat) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.cancel();
        vibrator.vibrate(pattern, repeat);
    }

    /**
     * 关闭震动
     *
     * @param context
     */
    public static void cancelVibrator(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.cancel();
    }
}
