package cn.rongcloud.im.im.message;

import android.os.Parcel;

import io.rong.imlib.MessageTag;
import io.rong.message.ContactNotificationMessage;

@MessageTag(value = "ST:ContactNtf", flag = MessageTag.ISPERSISTED)
public class SealContactNotificationMessage extends ContactNotificationMessage {
    public SealContactNotificationMessage(Parcel in) {
        super(in);
    }

    public SealContactNotificationMessage(byte[] data) {
        super(data);
    }
}
