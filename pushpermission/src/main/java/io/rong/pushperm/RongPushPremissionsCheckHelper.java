package io.rong.pushperm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;

import java.util.List;

import io.rong.pushperm.config.IntentInfo;
import io.rong.pushperm.config.IntentProvider;
import io.rong.pushperm.log.LLog;
import io.rong.pushperm.perm.PermissionCheck;
import io.rong.pushperm.perm.PermissionStatus;
import io.rong.pushperm.perm.PermissionType;

/**
 * 由于现在各种机型厂商和安卓的限制，为了 Push 可以正常的接受
 * 消息或应用尽量长期在后台，所以要开启手机的某系权限。此模块就是针对此问题进行了机型适配。
 *
 * 主要有几个功能模块。1. 检查权限状态。 具体的 Push 所需权限类型可查看 {@link PermissionType}.
 * 目前通知权限状态可以进行查询； 自启动目前只支持小米机型查询， 其他机型可能在后续添加检查方式。
 * 2. 跳转权限设置界面。权限设置界面的具体各机型的配置信息可在 config.xml 文件中进行查看。由于
 * 各手机厂商的页面不一致。所以需对机型进行配置。代码会通过当前机型去解析对应的配置信息，通过配置信息
 * 去跳转权限设置界面。
 *
 * 注意： 由于有些手机开启自启动权限后，是通过系统的广播拉起应用， 但是由于 android 系统 API 26 及
 * 以上版本对隐式广播做了限制，导致可能收不到系统广播的问题。 则建议 target 版本最好设置 26 以下。
 *
 */
public class RongPushPremissionsCheckHelper {

    private static final String TAG = "PushPremHelper";

    /**
     * 检查权限状态方法。调用此方法后，会对当前的权限进行查询并返回权限的状态结果。
     * 由于有些权限在某些机型上没有适配权限获取的方法。 所以查询的权限在当前机型上
     * 没有适配时，则会返回 {@link PermissionStatus#NO_SUPPORT}.
     *
     * @param context 上下文
     * @param permsType 查询的权限类型 。具体类型可查看 {@link PermissionType}
     * @return 检查的机型自启动圈权限状态的情况。具体类型结果可查看 {@link PermissionStatus}
     *
     * @see PermissionType
     * @see PermissionStatus
     */
    public static PermissionStatus checkPermisson(Context context, PermissionType permsType) {
        return PermissionCheck.checkPerm(context, permsType);
    }


    /**
     * 跳转到当前权限的设置界面。
     * 如果当前权限的设置界面在 config.xml 文件中适配了，并且跳转成功了， 则会返回 true。 假如在 config.xml
     * 没有配置或配置了但是跳转失败，否则返回 false。
     *
     * @param context 上下文
     * @param permsType 查询的权限类型 。具体类型可查看 {@link PermissionType}
     * @return true 跳转成功； false 跳转失败，或没有进行适配
     * @see PermissionType
     */
    public static boolean goToSetting(Context context, PermissionType permsType) {
        IntentProvider intentProvider = new IntentProvider(context);
        IntentInfo intentInfo = getIntentInfo(context, intentProvider, permsType);
        if (intentInfo.intents == null || intentInfo.intents.size() <=0) {
            return false;
        }
        boolean result = false;
        for (Intent intent : intentInfo.intents) {
            result = toSet(context, intent);
            if (result) {
                break;
            }
        }
        return result;
    }

    /**
     * 遍历检测 push 所需权限的状态， 假如没有开启则提示去设置，用户可选择跳转到设置页面进行设置。
     * 1.内部首先会检查权限的状态， 当权限是开启状态， 则会直接通过 {@link ResultCallback#onAreadlyOpened(String)}
     * 返回结果； 如果没有开启或当前权限没有进行检查适配， 则会继续进行判断。2. 然后会检测是否配置了相关的跳转， 如果没有配置，
     * 则会通过 {@link ResultCallback#onFailed(String, ResultCallback.FailedType)} 返回结果，FailedType 值为
     * {@link io.rong.pushperm.ResultCallback.FailedType#NO_SUPPORT} ; 3. 接下来会调用 {@link ResultCallback#onBeforeShowDialog(String)}
     * ，开发着可在dialog 显示之前进行拦截，如果{@link ResultCallback#onBeforeShowDialog(String)} 返回true 即可拦截，并通过
     * {@link ResultCallback#onFailed(String, ResultCallback.FailedType)} 返回结果，FailedType 值为
     * {@link io.rong.pushperm.ResultCallback.FailedType#INTERCEPT} 不会显示提示窗口。所以用户可在 {@link ResultCallback#onBeforeShowDialog(String)}
     * 方法中添加自己的判断规则。4. 前面条件都通过之后则会显示 dialog 提示。假如点击了取消会通过 {@link ResultCallback#onFailed(String, ResultCallback.FailedType)}
     * 返回结果，FailedType 值为 {@link io.rong.pushperm.ResultCallback.FailedType#CANNEL} ，点击确定则会跳转到设置界面， 并会回调
     * {@link ResultCallback#onGoToSetting(String)}
     *
     * @param activity
     * @param callback 回调结果对象
     * @see ResultCallback
     */
    public static void checkPermissionsAndShowDialog(Activity activity, ResultCallback callback) {
        PermissionType[] prems = new PermissionType[]{PermissionType.PERM_AUTO_START, PermissionType.PERM_NOTIFICATION, PermissionType.PERM_NO_CLEAN};
        for (PermissionType perm : prems) {
            checkAndShowDialog(activity, perm,callback);
        }
    }


    /**
     * 检测当前权限的状态， 并进行提示设置，可跳转到设置页面。根据机型默认适配。
     * 1.内部首先会检查权限的状态， 当权限是开启状态， 则会直接通过 {@link ResultCallback#onAreadlyOpened(String)}
     * 返回结果； 如果没有开启或当前权限没有进行检查适配， 则会继续进行判断。2. 然后会检测是否配置了相关的跳转， 如果没有配置，
     * 则会通过 {@link ResultCallback#onFailed(String, ResultCallback.FailedType)} 返回结果，FailedType 值为
     * {@link io.rong.pushperm.ResultCallback.FailedType#NO_SUPPORT} ; 3. 接下来会调用 {@link ResultCallback#onBeforeShowDialog(String)}
     * ，开发着可在dialog 显示之前进行拦截，如果{@link ResultCallback#onBeforeShowDialog(String)} 返回true 即可拦截，并通过
     * {@link ResultCallback#onFailed(String, ResultCallback.FailedType)} 返回结果，FailedType 值为
     * {@link io.rong.pushperm.ResultCallback.FailedType#INTERCEPT} 不会显示提示窗口。所以用户可在 {@link ResultCallback#onBeforeShowDialog(String)}
     * 方法中添加自己的判断规则。4. 前面条件都通过之后则会显示 dialog 提示。假如点击了取消会通过 {@link ResultCallback#onFailed(String, ResultCallback.FailedType)}
     * 返回结果，FailedType 值为 {@link io.rong.pushperm.ResultCallback.FailedType#CANNEL} ，点击确定则会跳转到设置界面， 并会回调
     * {@link ResultCallback#onGoToSetting(String)}
     *
     * @param activity
     * @param permsType 权限
     * @param callback 设置回调
     * @see PermissionType
     * @see ResultCallback
     */
    public static void checkAndShowDialog(final Activity activity, final PermissionType permsType, final ResultCallback callback) {
        IntentProvider provider = new IntentProvider(activity);
        int needToSet = isNeedToSet(activity, provider, permsType);
        if ( needToSet == 0) {
            boolean isIntercept = false;
            if (callback != null) {
                isIntercept = callback.onBeforeShowDialog(permsType.getValue());
            }
            if (isIntercept) {
                LLog.d(TAG, "intercept set action");
                if (callback != null) {
                    callback.onFailed(permsType.getValue(), ResultCallback.FailedType.INTERCEPT);
                }
                return ;
            }
            final IntentInfo intentInfo = getIntentInfo(activity,provider, permsType);

            // 显示dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(getDialogTitle(activity, permsType));
            builder.setMessage(getDialogMessage(activity, getDialogTitle(activity, permsType)) );
            builder.setPositiveButton(getString(activity, "dialog_btn_yes"), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    List<Intent> intents = intentInfo.intents;
                    if (intents == null || intents.size() <= 0) {
                        if (callback != null) {
                            callback.onFailed(permsType.getValue(), ResultCallback.FailedType.NO_SUPPORT);
                        }
                        return;
                    }
                    boolean result = false;

                    for (Intent intent : intents) {
                        result = toSet(activity, intent);
                        if (result) {
                            break;
                        }
                    }
                    if (result) {
                        if (callback != null) {
                            callback.onGoToSetting(permsType.getValue());
                        }
                    } else {
                        showManualSetDialog(activity, permsType);
                    }
                }
            });
            builder.setNegativeButton(getString(activity, "dialog_btn_no"), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (callback != null) {
                        callback.onFailed(permsType.getValue(), ResultCallback.FailedType.CANNEL);
                    }
                }
            });
            builder.setCancelable(false);
            builder.show();
        } else {
                //0  去设置； 1 不去设置； 2 需要设置但， 没有配置跳转项
                if (callback != null) {
                    if (needToSet == 1) {
                        callback.onAreadlyOpened(permsType.getValue());
                    } else  if (needToSet == 2) {
                        callback.onFailed(permsType.getValue(), ResultCallback.FailedType.NO_SUPPORT);
                    }
                }

        }
    }

    private static void showManualSetDialog(final Activity activity, final PermissionType permsType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(getManualSetDialogTitle(activity));
        builder.setMessage(getManualSetDialogContent(activity, permsType) );
        builder.setPositiveButton(getString(activity, "dialog_btn_yes"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setCancelable(false);
        builder.show();
    }


    /*
     * 检查当前权限是否需要去设置页面设置。
     * 注意： 由于自启动权限在此机型上没有配置时， 则会返回为 false。
     * @return -1 错误； 0  去设置； 1 不去设置； 2 需要设置但， 没有配置跳转项
     */
    private static int isNeedToSet(Context context, IntentProvider provider, PermissionType permsType) {
        PermissionStatus status = checkPermisson(context, permsType);
        if (status == PermissionStatus.OPENED ) { // 开启了或不支持
            LLog.d(TAG, "Permission is opened," + permsType);
            return 1;
        }
        //  没有适配跳转
        IntentInfo intentInfo =  getIntentInfo(context, provider, permsType);
        if (intentInfo == null) {
            LLog.e(TAG, "Intent is null, This model is not suitable," + permsType);
            return 2;
        }

        return 0;
    }



    // 去设置自动开启页面
    private static boolean toSet(Context context, Intent intent) {
        try {
            if (intent == null) {
                LLog.d(TAG, "intent is null");
                return false;
            }
            context.startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static IntentInfo getIntentInfo(Context context, IntentProvider intentProvider, PermissionType permsType) {
        IntentInfo intentInfo = null;
        if (permsType == PermissionType.PERM_AUTO_START) {
            intentInfo = intentProvider.getIntent(context, IntentProvider.SetType.AUTO_START);
        } else if (permsType == PermissionType.PERM_NOTIFICATION) {
            intentInfo = intentProvider.getIntent(context, IntentProvider.SetType.NOTIFICATION);
        } else if (permsType == PermissionType.PERM_NO_CLEAN) {
            intentInfo = intentProvider.getIntent(context, IntentProvider.SetType.LOCK_CLEAN);
        }
        return intentInfo;
    }


    private static String getManualSetDialogTitle(Context context) {
        int dialog_message = getStrignResId(context, "dialog_to_set_failed");
        if (dialog_message <= 0) {
            return "";
        }
        return context.getResources().getString(dialog_message);
    }

    private static String getManualSetDialogContent(Context context, PermissionType permType) {
        int dialog_message = getStrignResId(context, "dialog_manual_set");
        int strignResId = getStrignResId(context, permType.getName());
        int pathResId = getStrignResId(context, Build.MANUFACTURER.toLowerCase() + "_" + permType.getName());
        if (dialog_message <= 0) {
            return "";
        }
        String path = "";
        String name = "";
        if (strignResId > 0) {
            name = context.getResources().getString(strignResId);
        }
        if (pathResId > 0) {
            path = context.getResources().getString(pathResId);
        }
        return context.getResources().getString(dialog_message, path, name);
    }

    private static String getDialogTitle(Context context, PermissionType permType) {
        int dialog_title = getStrignResId(context, "dialog_title");
        int strignResId = getStrignResId(context, permType.getName());
        if (strignResId <=0 || dialog_title <= 0) {
            return "";
        }
        String name = context.getResources().getString(strignResId);
        return context.getResources().getString(dialog_title, name);
    }

    private static String getDialogMessage(Context context, String content) {
        int dialog_message = getStrignResId(context, "dialog_message");
        if (dialog_message <= 0) {
            return "";
        }
        return context.getResources().getString(dialog_message, content);
    }



    private static String getString(Context context, String idName) {
        int resId = getStrignResId(context, idName);
        if (resId <= 0) {
            return "";
        }
        return context.getResources().getString(resId);
    }

    private static int getStrignResId (Context context, String resName) {
       return context.getResources().getIdentifier(resName, "string", context.getPackageName());
    }

}
