package io.rong.pushperm.perm;

import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationManagerCompat;

/**
 * 用于权限的检查.
 * 检查自动动权限的设置状态.目前只是适配了小米
 */
public class PermissionCheck {
    private static final String CHECK_OP_NO_THROW = "checkOpNoThrow";
    private static final String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";
    /**
     *  检查自启动权限， 内部会适配部分机型的自启动权限的检查。 如果当前机型没有适配，
     *  即 没有此机型的检查方法， 则会返回 -1. 当有相关的检查方法并且权限是开启状态的则
     *  返回为 0. 否则返回 1.
     * @param context 上下文
     * @return PermissionStatus {@link PermissionStatus}
     */
    public static  PermissionStatus checkPerm(Context context, PermissionType type) {
        if (type == PermissionType.PERM_AUTO_START) {
            int i = checkAutoStartPerm(context);
            return i == 0? PermissionStatus.OPENED : i == 1 ? PermissionStatus.CLOSED : PermissionStatus.NO_SUPPORT;
        } else if (type == PermissionType.PERM_NOTIFICATION) {
            boolean notifiResult =  isNotificationEnabled(context);
            return notifiResult ? PermissionStatus.OPENED : PermissionStatus.CLOSED;
        } else if (type == PermissionType.PERM_NO_CLEAN) {
            return PermissionStatus.NO_SUPPORT;
        }
        return PermissionStatus.NO_SUPPORT;
    }

    /**
     *  检查自启动权限， 内部会适配部分机型的自启动权限的检查。 如果当前机型没有适配，
     *  即 没有此机型的检查方法， 则会返回 -1. 当有相关的检查方法并且权限是开启状态的则
     *  返回为 0. 否则返回 1.
     * @param context 上下文
     * @return -1 没有适配；  0 开启; 1 没有开启，
     */
    private static int  checkAutoStartPerm(Context context) {
        IModelCheckPermmission modelCheckPerm = getModelCheckPerm();
        if (modelCheckPerm == null) {
            return -1;
        }
        return  modelCheckPerm.checkAutoStartPermission(context);
    }


    private static IModelCheckPermmission getModelCheckPerm () {
        String manufacturer = Build.MANUFACTURER;
        if (manufacturer.equalsIgnoreCase("XIAOMI")) {
            return new MiCheckPermission();
        }
        return null;
    }


    /**
     *  通知权限检查
     * @param context
     * @return
     */
    private static boolean isNotificationEnabled(Context context) {

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {
            return false;
        }
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        // areNotificationsEnabled方法的有效性官方只最低支持到API 19，低于19的仍可调用此方法不过只会返回true，即默认为用户已经开启了通知。
        boolean isOpened = manager.areNotificationsEnabled();
        return isOpened;

        // 目前有替代方法，但不保证完全适用， 故保留
//        AppOpsManager mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
//        ApplicationInfo appInfo = context.getApplicationInfo();
//        String pkg = context.getApplicationContext().getPackageName();
//        int uid = appInfo.uid;
//        Class appOpsClass = null;
//        /* Context.APP_OPS_MANAGER */
//        try {
//            appOpsClass = Class.forName(AppOpsManager.class.getName());
//            Method checkOpNoThrowMethod = appOpsClass.getMethod(CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE,                        String.class);
//            Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);
//            int value = (Integer) opPostNotificationValue.get(Integer.class);
//            int result =  (Integer)checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg);
//            return result == AppOpsManager.MODE_ALLOWED;
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        } catch (NoSuchFieldException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
//        return false;
    }
}
