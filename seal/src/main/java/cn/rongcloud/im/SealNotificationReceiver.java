package cn.rongcloud.im;

import android.content.Context;

import io.rong.push.notification.PushNotificationMessage;
import io.rong.push.notification.PushMessageReceiver;


public class SealNotificationReceiver extends PushMessageReceiver {

    @Override
    public boolean onNotificationMessageArrived(Context context, PushNotificationMessage message) {
        return false;
    }

    @Override
    public boolean onNotificationMessageClicked(Context context, PushNotificationMessage message) {
        return false;
    }

}
