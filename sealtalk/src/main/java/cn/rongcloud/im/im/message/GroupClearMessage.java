package cn.rongcloud.im.im.message;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import io.rong.common.ParcelUtils;
import io.rong.common.RLog;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;

@MessageTag(value = "ST:MsgClear", flag = MessageTag.NONE)
public class GroupClearMessage extends MessageContent {

    private long clearTime;

    private GroupClearMessage() {
    }

    public GroupClearMessage(Parcel in) {
        this.clearTime = ParcelUtils.readLongFromParcel(in);
    }

    public GroupClearMessage(byte[] data) {
        String jsonStr = null;
        try {
            jsonStr = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException var5) {
            RLog.e("GroupNotificationMessage", "UnsupportedEncodingException ", var5);
        }

        try {
            JSONObject jsonObj = new JSONObject(jsonStr);
            this.setClearTime(jsonObj.optLong("clearTime"));
        } catch (JSONException var4) {
            RLog.e("GroupNotificationMessage", "JSONException " + var4.getMessage());
        }

    }

    public static final Creator<GroupClearMessage> CREATOR = new Creator<GroupClearMessage>() {
        public GroupClearMessage createFromParcel(Parcel source) {
            return new GroupClearMessage(source);
        }

        public GroupClearMessage[] newArray(int size) {
            return new GroupClearMessage[size];
        }
    };

    public static GroupClearMessage obtain(String operatorUserId, String operation, long clearTime, String message) {
        GroupClearMessage obj = new GroupClearMessage();
        obj.clearTime = clearTime;
        return obj;
    }

    public long getClearTime() {
        return clearTime;
    }

    public void setClearTime(long clearTime) {
        this.clearTime = clearTime;
    }

    @Override
    public byte[] encode() {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("clearTime",this.clearTime);
        } catch (JSONException var4) {
            RLog.e("GroupNotificationMessage", "JSONException " + var4.getMessage());
        }
        try {
            return jsonObj.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException var3) {
            RLog.e("GroupNotificationMessage", "UnsupportedEncodingException ", var3);
            return null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelUtils.writeToParcel(dest, this.clearTime);
    }

    @Override
    public String toString() {
        return "GroupClearMessage{" +
                ", clearTime=" + clearTime +
                '}';
    }
}
