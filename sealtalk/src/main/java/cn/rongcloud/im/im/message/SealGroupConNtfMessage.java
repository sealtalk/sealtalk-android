package cn.rongcloud.im.im.message;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import io.rong.common.ParcelUtils;
import io.rong.common.RLog;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;

@MessageTag(value = "ST:ConNtf", flag = MessageTag.ISPERSISTED)
public class SealGroupConNtfMessage extends MessageContent {

    private String operatorUserId;
    private String operation;

    private SealGroupConNtfMessage() {
    }

    public SealGroupConNtfMessage(byte[] data) {
        String jsonStr = null;

        try {
            jsonStr = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException var5) {
            RLog.e("GroupNotificationMessage", "UnsupportedEncodingException ", var5);
        }

        try {
            JSONObject jsonObj = new JSONObject(jsonStr);
            this.setOperatorUserId(jsonObj.optString("operatorUserId"));
            this.setOperation(jsonObj.optString("operation"));
        } catch (JSONException var4) {
            RLog.e("GroupNotificationMessage", "JSONException " + var4.getMessage());
        }

    }

    public static final Creator<SealGroupConNtfMessage> CREATOR = new Creator<SealGroupConNtfMessage>() {
        public SealGroupConNtfMessage createFromParcel(Parcel source) {
            return new SealGroupConNtfMessage(source);
        }

        public SealGroupConNtfMessage[] newArray(int size) {
            return new SealGroupConNtfMessage[size];
        }
    };

    public static SealGroupConNtfMessage obtain(String operatorUserId, String operation) {
        SealGroupConNtfMessage obj = new SealGroupConNtfMessage();
        obj.operatorUserId = operatorUserId;
        obj.operation = operation;
        return obj;
    }

    public SealGroupConNtfMessage(Parcel in) {
        this.operatorUserId = ParcelUtils.readFromParcel(in);
        this.operation = ParcelUtils.readFromParcel(in);
    }

    public String getOperatorUserId() {
        return operatorUserId;
    }

    public void setOperatorUserId(String operatorUserId) {
        this.operatorUserId = operatorUserId;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    @Override
    public byte[] encode() {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("operatorUserId", this.operatorUserId);
            jsonObj.put("operation", this.operation);
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
        ParcelUtils.writeToParcel(dest, this.operatorUserId);
        ParcelUtils.writeToParcel(dest, this.operation);
    }

    @Override
    public String toString() {
        return "SealGroupConNtfMessage{" +
                "operatorUserId='" + operatorUserId + '\'' +
                ", operation='" + operation + '\'' +
                '}';
    }
}
