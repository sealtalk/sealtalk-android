package cn.rongcloud.im.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import cn.rongcloud.im.ui.activity.MainActivity;

/**
 * 机型适配: 处理三星 note2  note3 按 Home 键退至后台的花屏问题
 * Created by AMing on 16/8/24.
 * Company RongCloud
 */
public class HomeWatcherReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "HomeReceiver";
    private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
    private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(LOG_TAG, "onReceive: action: " + action);
        if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
            String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
            if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
                if (context instanceof MainActivity) {
                    MainActivity activity = (MainActivity) context;
                    activity.moveTaskToBack(true);
                }
            }
        }
    }
}
