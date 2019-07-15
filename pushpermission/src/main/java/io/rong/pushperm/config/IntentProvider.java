package io.rong.pushperm.config;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import io.rong.pushperm.log.LLog;

/**
 * 提供跳转的 {@link Intent} 的对象。
 */
public class IntentProvider {
    private static final String TAG = "IntentProvider";
    private ModelInfo modelInfo = null;
    public IntentProvider(Context context) {
        String manufacturer = getManufacturer();
        LLog.d(TAG, "manufacturer -> " + manufacturer);
//        manufacturer = "oppo";
        if (TextUtils.isEmpty(manufacturer)) {
            LLog.e(TAG, "manufacturer -> " + manufacturer);
            return ;
        }
        modelInfo = ParseConfig.parse(context, manufacturer);
        LLog.d(TAG, "modelinfo -> " + modelInfo);
    }

    /**
     *  获得设置跳转的类型
     */
    public enum SetType {
        /**
         * 自启动
         */
        AUTO_START,

        /**
         * 锁屏权限
         */
        LOCK_CLEAN,
        /**
         * 通知
         */
        NOTIFICATION,
        /**
         * 浮窗
         */
        FLOAT_WINDOW;
    }


    /**
     * 内部更具不同机型提供定制的 intent ， initent 中已经包装好了跳转信息。
     * @return Intent
     */
    public IntentInfo getIntent(Context context, SetType type) {
        return createIntent(context.getApplicationContext(), type);
    }


    private IntentInfo createIntent(Context context, SetType type) {

        if (modelInfo == null) {
            return null;
        }
        IntentInfo intentInfo = null;
        switch (type) {
            case AUTO_START:
                intentInfo = creatIntentInfo(context, modelInfo.autoStart);
                break;
            case LOCK_CLEAN:
                intentInfo = creatIntentInfo(context, modelInfo.lockClean);
                break;
            case FLOAT_WINDOW:
                intentInfo = creatIntentInfo(context, modelInfo.floatWindow);
                break;
            case NOTIFICATION:
                intentInfo = creatIntentInfo(context, modelInfo.notification);
                break;
            default:
                intentInfo = creatIntentInfo(context, modelInfo.commomPerm);
//                intent = new Intent();
//                intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
//                intent.setData(Uri.fromParts("package", context.getPackageName(), null));
                break;
        }
        return intentInfo;
    }


    //更具配置信息创建并检测组件是否存在，并把存在的返回
    private IntentInfo creatIntentInfo(Context context, List<ModelInfo.Info> infos) {
        Intent intent = null;
        IntentInfo intentInfo = null;
        if (infos != null && infos.size() > 0) {
            //  检查当前的包在不在， 可不可用， 最终返回一个可用的。
            for (ModelInfo.Info info : infos) {
                intent = new Intent();
                if (!TextUtils.isEmpty(info.packageName) && !TextUtils.isEmpty(info.clazzName)) {
                    ComponentName componentName = new ComponentName(info.packageName, info.clazzName);
                    intent.setComponent(componentName);
                }
                if (!TextUtils.isEmpty(info.actionName)) {
                    intent.setAction(info.actionName);
                }
                if (!TextUtils.isEmpty(info.catagoty)) {
                    intent.addCategory(info.catagoty);
                }

                if (info.dataUri!= null && !TextUtils.isEmpty(info.dataUri.key)) {
                    if (ModelInfo.Param.AUTO.equalsIgnoreCase(info.dataUri.value)) {
                        intent.setData(Uri.fromParts(info.dataUri.key, getAutoParamsByKey(context, info.dataUri.key), null));
                    } else {
                        intent.setData(Uri.fromParts(info.dataUri.key, info.dataUri.key, null));
                    }
                }

                if (info.extras != null && info.extras.size() >0) {
                    for (ModelInfo.Param param : info.extras ) {
                        if (param != null && !TextUtils.isEmpty(param.key)) {
                            if (ModelInfo.Param.AUTO.equalsIgnoreCase(param.value)) {
                                intent.putExtra(param.key, getAutoParamsByKey(context, param.key));
                            } else {
                                intent.putExtra(param.key, getAutoParamsByKey(context, param.key));
                            }
                        }
                    }
                }

                if (intent == null) {
                    LLog.e(TAG, "intent == null");
                    continue;
                }

                if (context.getPackageManager().resolveActivity(intent, 0) == null) {
                    // 检查组件是否存在
                    LLog.e(TAG, "Component is not exsit, " +  intent);
                    intent = null;
                    continue;
                }

                // 默认设置上的
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                int uid = context.getApplicationInfo().uid;
                String pkgNamge = context.getPackageName();
               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    //这种方案适用于 API 26, 即8.0（含8.0）以上可以用
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, pkgNamge);
                    intent.putExtra(Settings.EXTRA_CHANNEL_ID, uid);
                } else {
                    //这种方案适用于 API21——25，即 5.0——7.1 之间的版本可以使用
                    intent.putExtra("app_package", pkgNamge);
                    intent.putExtra("app_uid", uid) ;
                }
                if (intentInfo == null) {
                    intentInfo = new IntentInfo();
                }

                if (intentInfo.intents == null) {
                    intentInfo.intents = new ArrayList<>();
                }
                intentInfo.setPath = info.setPath;
                intentInfo.intents.add(intent);
//                // 如果存在跳出循环
//                break;
            }
        }
        return intentInfo;
    }

    private String getAutoParamsByKey(Context context, String key) {
        // 兼容魅族 packageName
        // 兼容小米 extra_pkgname
        // 主要用于兼容
        if ("package".equalsIgnoreCase(key) || "app_package".equalsIgnoreCase(key)
                || "packageName".equalsIgnoreCase(key) || "extra_pkgname".equalsIgnoreCase(key)) {
            return context.getPackageName();
        }

        return "";
    }


    /**
     * 获取手机制造厂商
     * @return 手机厂商
     */
    private  String getManufacturer() {
        String manufacturer = Build.MANUFACTURER;
        if (manufacturer.equals("360")) {
            manufacturer = "M360";
        }
        return manufacturer;
    }
}
