package cn.rongcloud.im.utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.LocationManager;
import android.net.Uri;
import android.text.TextUtils;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.rongcloud.im.model.SealSearchConversationResult;
import io.rong.imlib.model.SearchConversationResult;

/**
 * Created by bob on 2015/2/2.
 */
public class CommonUtils {

    /**
     * 得到屏幕高度
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Activity context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int height = wm.getDefaultDisplay().getHeight();
        return height;

    }

    /**
     * 得到屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Activity context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        return width;
    }

    /**
     * 得到状态栏高度
     *
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Activity context) {
        int statusHeight = 0;
        Rect frame = new Rect();
        context.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        statusHeight = frame.top;
        if (0 == statusHeight) {
            Class<?> localClass;
            try {
                localClass = Class.forName("com.android.internal.R$dimen");
                Object localObject = localClass.newInstance();
                int i5 = Integer.parseInt(localClass.getField("status_bar_height").get(localObject).toString());
                statusHeight = context.getResources().getDimensionPixelSize(i5);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return statusHeight;
    }

    /**
     * 获得当前的版本信息
     *
     * @return
     */
    public static String[] getVersionInfo(Context context) {
        String[] version = new String[2];

        PackageManager packageManager = context.getPackageManager();

        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            version[0] = String.valueOf(packageInfo.versionCode);
            version[1] = packageInfo.versionName;
            return version;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return version;
    }

    /**
     * 邮箱格式是否正确
     *
     * @param email
     * @return
     */
    public static boolean isEmail(String email) {

        if (TextUtils.isEmpty(email))
            return false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);

        return matcher.matches();

    }

    /**
     * 是否开启 GPS
     *
     * @param context
     * @return
     */
    public static boolean isOpenGPS(Context context) {

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNPEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        return isGPSEnable || isNPEnable;


    }

    public static void openGPS(Context context) {

        Intent GPSIntent = new Intent();

        GPSIntent.setClassName("com.android.settings",
                "com.android.settings.widget.SettingsAppWidgetProvider");

        GPSIntent.addCategory("android.intent.category.ALTERNATIVE");
        GPSIntent.setData(Uri.parse("custom:3"));

        try {
            PendingIntent.getBroadcast(context, 0, GPSIntent, 0).send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    public static List<SealSearchConversationResult> convertSearchResult(List<SearchConversationResult> results) {
        List<SealSearchConversationResult> list = new ArrayList<>();
        for (SearchConversationResult result : results) {
            SealSearchConversationResult sealSearchConversationResult = new SealSearchConversationResult();
            sealSearchConversationResult.setConversation(result.getConversation());
            sealSearchConversationResult.setMatchCount(result.getMatchCount());
            list.add(sealSearchConversationResult);
        }
        return list;
    }

    public static int dip2pix(Context context, int dips) {
        int densityDpi = context.getResources().getDisplayMetrics().densityDpi;
        return (dips * densityDpi) / 160;
    }

}
