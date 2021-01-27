package cn.rongcloud.im.im.message;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import io.rong.common.ParcelUtils;
import io.rong.common.RLog;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;

@MessageTag(value = "ST:GrpApply", flag = MessageTag.ISPERSISTED)
public class GroupApplyMessage extends MessageContent {

    private String operatorUserId;
    private String operation;
    private String data;

    private GroupApplyMessage() {
    }

    public GroupApplyMessage(Parcel in) {
        this.operatorUserId = ParcelUtils.readFromParcel(in);
        this.operation = ParcelUtils.readFromParcel(in);
        this.data = ParcelUtils.readFromParcel(in);
    }

    public GroupApplyMessage(byte[] data) {
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
            this.setData(jsonObj.optString("data"));
        } catch (JSONException var4) {
            RLog.e("GroupNotificationMessage", "JSONException " + var4.getMessage());
        }

    }

    public static final Creator<GroupApplyMessage> CREATOR = new Creator<GroupApplyMessage>() {
        public GroupApplyMessage createFromParcel(Parcel source) {
            return new GroupApplyMessage(source);
        }

        public GroupApplyMessage[] newArray(int size) {
            return new GroupApplyMessage[size];
        }
    };

    public static GroupApplyMessage obtain(String operatorUserId, String operation, String data, String message) {
        GroupApplyMessage obj = new GroupApplyMessage();
        obj.operatorUserId = operatorUserId;
        obj.operation = operation;
        obj.data = data;
        return obj;
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

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public byte[] encode() {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("operatorUserId", this.operatorUserId);
            jsonObj.put("operation", this.operation);
            jsonObj.put("data",this.data);
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
        ParcelUtils.writeToParcel(dest, this.data);
    }
}
