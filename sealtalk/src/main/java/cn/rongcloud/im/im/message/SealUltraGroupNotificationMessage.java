package cn.rongcloud.im.im.message;

import android.os.Parcel;
import io.rong.imlib.MessageTag;
import io.rong.message.GroupNotificationMessage;

@MessageTag(value = "ST:UltraGrpNtf", flag = MessageTag.ISPERSISTED)
public class SealUltraGroupNotificationMessage extends GroupNotificationMessage {

    public SealUltraGroupNotificationMessage(Parcel in) {
        super(in);
    }

    public SealUltraGroupNotificationMessage(byte[] data) {
        super(data);
    }
}
