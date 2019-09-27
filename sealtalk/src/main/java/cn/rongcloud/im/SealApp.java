package cn.rongcloud.im;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.multidex.MultiDexApplication;

import com.facebook.stetho.Stetho;
import com.tencent.bugly.crashreport.CrashReport;

import cn.rongcloud.im.common.ErrorCode;
import cn.rongcloud.im.contact.PhoneContactManager;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.ui.activity.MainActivity;
import cn.rongcloud.im.utils.SearchUtils;
import cn.rongcloud.im.wx.WXManager;
import io.rong.imlib.ipc.RongExceptionHandler;

import static io.rong.imkit.utils.SystemUtils.getCurProcessName;

public class SealApp extends MultiDexApplication {
    private static SealApp appInstance;
    /**
     * 应用是否在后台
     */
    private boolean isAppInForeground;
    private String lastVisibleActivityName;
    private Intent nextOnForegroundIntent;
    private boolean isMainActivityIsCreated;

    @Override
    public void onCreate() {
        super.onCreate();

        appInstance = this;

        // 初始化 bugly BUG 统计
        //CrashReport.initCrashReport(getApplicationContext());

        ErrorCode.init(this);

        /*
         * 以上部分在所有进程中会执行
         */
        if (!getApplicationInfo().packageName.equals(getCurProcessName(getApplicationContext()))) {
            return;
        }
        /*
         * 以下部分仅在主进程中进行执行
         */
        // 初始化融云IM SDK，初始化 SDK 仅需要在主进程中初始化一次
        IMManager.getInstance().init(this);
        Stetho.initializeWithDefaults(this);

        SearchUtils.init(this);

        Thread.setDefaultUncaughtExceptionHandler(new RongExceptionHandler(this));

        // 微信分享初始化
        WXManager.getInstance().init(this);

        PhoneContactManager.getInstance().init(this);

        // 监听 App 前后台变化
        observeAppInBackground();
    }

    public static SealApp getApplication() {
        return appInstance;
    }

    /**
     * 监听应用是否转为后台
     */
    private void observeAppInBackground() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                if(activity instanceof MainActivity){
                    isMainActivityIsCreated = true;
                }
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
                // 当切换为前台时启动预设的优先显示界面
                if (isMainActivityIsCreated && !isAppInForeground && nextOnForegroundIntent != null) {
                    activity.startActivity(nextOnForegroundIntent);
                    nextOnForegroundIntent = null;
                }

                lastVisibleActivityName = activity.getClass().getSimpleName();
                isAppInForeground = true;
            }

            @Override
            public void onActivityPaused(Activity activity) {
                String pauseActivityName = activity.getClass().getSimpleName();
                /*
                 * 介于 Activity 生命周期在切换画面时现进行要跳转画面的 onResume，
                 * 再进行当前画面 onPause，所以当用户且到后台时肯定会为当前画面直接进行 onPause，
                 * 同过此来判断是否应用在前台
                 */
                if (pauseActivityName.equals(lastVisibleActivityName)) {
                    isAppInForeground = false;
                }
            }

            @Override
            public void onActivityStopped(Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                if(activity instanceof MainActivity){
                    isMainActivityIsCreated = false;
                }
            }
        });
    }

    /**
     * 当前 App 是否在前台
     *
     * @return
     */
    public boolean isAppInForeground() {
        return isAppInForeground;
    }

    /**
     * 获取最后在前台的 Activity 名称
     *
     * @return
     */
    public String getLastVisibleActivityName() {
        return lastVisibleActivityName;
    }

    /**
     * 设置当 App 切换为前台时启动的 intent，该 intent 在启动后情况
     *
     * @param intent
     */
    public void setOnAppForegroundStartIntent(Intent intent) {
        nextOnForegroundIntent = intent;
    }

    /**
     * 获取最近设置的未触发的启动 intent
     *
     * @return
     */
    public Intent getLastOnAppForegroundStartIntent() {
        return nextOnForegroundIntent;
    }

    /**
     * 判断是否进入到了主界面
     * @return
     */
    public boolean isMainActivityCreated(){
        return isMainActivityIsCreated;
    }

}
