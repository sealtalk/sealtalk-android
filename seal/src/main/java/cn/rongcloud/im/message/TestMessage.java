package cn.rongcloud.im.message;

import android.os.Parcel;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.rong.common.ParcelUtils;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MentionedInfo;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UserInfo;

/**
 * Created by Beyond on 2016/12/5.
 */

@MessageTag(value = "RCD:TstMsg", flag = MessageTag.ISCOUNTED | MessageTag.ISPERSISTED)
public class TestMessage extends MessageContent {
    private final static String TAG = "TestMessage";

    private String content;
    protected String extra;

    /**
     * 将本地消息对象序列化为消息数据。
     *
     * @return 消息数据。
     */
    @Override
    public byte[] encode() {

        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("content", getEmotion(getContent()));

            if (!TextUtils.isEmpty(getExtra()))
                jsonObj.put("extra", getExtra());

            if (getJSONUserInfo() != null)
                jsonObj.putOpt("user", getJSONUserInfo());

            if (getJsonMentionInfo() != null) {
                jsonObj.putOpt("mentionedInfo", getJsonMentionInfo());
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException " + e.getMessage());
        }

        try {
            return jsonObj.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Android 的表情unicode跟ios不一样，为了做到两个平台统一，android 这边设置了表情映射，根据unicode来映射具体的图片。
     *
     */
    private String getEmotion(String content) {

        Pattern pattern = Pattern.compile("\\[/u([0-9A-Fa-f]+)\\]");
        Matcher matcher = pattern.matcher(content);

        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            int inthex = Integer.parseInt(matcher.group(1), 16);
            matcher.appendReplacement(sb, String.valueOf(Character.toChars(inthex)));
        }

        matcher.appendTail(sb);

        return sb.toString();
    }

    public TestMessage() {
    }

    public static TestMessage obtain(String text) {
        TestMessage model = new TestMessage();
        model.setContent(text);
        return model;
    }

    public TestMessage(byte[] data) {
        String jsonStr = null;
        try {
            jsonStr = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            JSONObject jsonObj = new JSONObject(jsonStr);

            if (jsonObj.has("content"))
                setContent(jsonObj.optString("content"));

            if (jsonObj.has("extra"))
                setExtra(jsonObj.optString("extra"));

            if (jsonObj.has("user")) {
                setUserInfo(parseJsonToUserInfo(jsonObj.getJSONObject("user")));
            }

            if (jsonObj.has("mentionedInfo")) {
                setMentionedInfo(parseJsonToMentionInfo(jsonObj.getJSONObject("mentionedInfo")));
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException " + e.getMessage());
        }

    }

    /**
     * 描述了包含在 Parcelable 对象排列信息中的特殊对象的类型。
     *
     * @return 一个标志位，表明Parcelable对象特殊对象类型集合的排列。
     */
    public int describeContents() {
        return 0;
    }

    /**
     * 将类的数据写入外部提供的 Parcel 中。
     *
     * @param dest  对象被写入的 Parcel。
     * @param flags 对象如何被写入的附加标志，可能是 0 或 PARCELABLE_WRITE_RETURN_VALUE。
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelUtils.writeToParcel(dest, getExtra());
        ParcelUtils.writeToParcel(dest, content);
        ParcelUtils.writeToParcel(dest, getUserInfo());
        ParcelUtils.writeToParcel(dest, getMentionedInfo());
    }

    /**
     * 构造函数。
     *
     * @param in 初始化传入的 Parcel。
     */
    public TestMessage(Parcel in) {
        setExtra(ParcelUtils.readFromParcel(in));
        setContent(ParcelUtils.readFromParcel(in));
        setUserInfo(ParcelUtils.readFromParcel(in, UserInfo.class));
        setMentionedInfo(ParcelUtils.readFromParcel(in, MentionedInfo.class));
    }

    /**
     * 读取接口，目的是要从Parcel中构造一个实现了Parcelable的类的实例处理。
     */
    public static final Creator<TestMessage> CREATOR = new Creator<TestMessage>() {

        @Override
        public TestMessage createFromParcel(Parcel source) {
            return new TestMessage(source);
        }

        @Override
        public TestMessage[] newArray(int size) {
            return new TestMessage[size];
        }
    };

    /**
     * 构造函数。
     *
     *
     * @param content 文字消息的内容。
     */
    public TestMessage(String content) {
        this.setContent(content);
    }

    /**
     * 获取文字消息的内容。
     *
     * @return 文字消息的内容。
     */
    public String getContent() {
        return content;
    }

    /**
     * 设置文字消息的内容。
     *
     * @param content 文字消息的内容。
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 获取消息扩展信息
     * @return 扩展信息
     */
    public String getExtra() {
        return extra;
    }

    /**
     * 设置消息扩展信息
     * @param extra 扩展信息
     */
    public void setExtra(String extra) {
        this.extra = extra;
    }

    @Override
    public List<String> getSearchableWord() {
        List<String> words = new ArrayList<>();
        words.add(content);
        return words;
    }
}
