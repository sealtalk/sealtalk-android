package cn.rongcloud.im.im.message;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

import cn.rongcloud.im.common.LogTag;
import cn.rongcloud.im.utils.log.SLog;
import io.rong.common.ParcelUtils;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;

/**
 * 戳一下消息
 */
@MessageTag(value = "ST:PokeMsg", flag = MessageTag.ISPERSISTED | MessageTag.ISCOUNTED)
public class PokeMessage extends MessageContent {
    /**
     * 戳一下内容
     */
    private String content;

    private PokeMessage() {
    }

    public PokeMessage(byte[] data) {
        String jsonStr = null;
        try {
            jsonStr = new String(data, StandardCharsets.UTF_8);
            JSONObject jsonObj = new JSONObject(jsonStr);
            content = jsonObj.optString("content");
        } catch (Exception e) {
            SLog.e(LogTag.IM, "PokeMessage parse error:" + e.toString());
        }
    }

    public PokeMessage(Parcel in) {
        content = ParcelUtils.readFromParcel(in);
    }


    public String getContent() {
        return content;
    }

    @Override
    public byte[] encode() {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("content", this.content);
            return jsonObj.toString().getBytes(StandardCharsets.UTF_8);
        } catch (JSONException e) {
            e.printStackTrace();
            SLog.e(LogTag.IM, "PokeMessage encode error:" + e.toString());
        }

        return null;
    }

    public static PokeMessage obtain(String content) {
        PokeMessage pokeMessage = new PokeMessage();
        pokeMessage.content = content;
        return pokeMessage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelUtils.writeToParcel(dest, this.content);
    }

    public static final Creator<PokeMessage> CREATOR = new Creator<PokeMessage>() {
        public PokeMessage createFromParcel(Parcel source) {
            return new PokeMessage(source);
        }

        public PokeMessage[] newArray(int size) {
            return new PokeMessage[size];
        }
    };

}
