package cn.rongcloud.im.sp;

import android.content.Context;
import android.content.SharedPreferences;

import cn.rongcloud.im.model.QuietHours;

public class UserConfigCache {
    private static final String SP_NAME = "User_config_cache";
    private static final String SP_NEW_MESSAGE_REMIND = "new_message_remind";
    private static final Object SP_NEW_MESSAGE_QUIET_HOURS_STARTTIME = "new_message_notifi_quiet_hours_start_time";
    private static final String SP_NEW_MESSAGE_QUIET_HOURS_SPANMINUTES = "new_message_notifi_quiet_hours_spanminutes";
    private static final Object SP_NEW_MESSAGE_QUIET_DONOT_DISTRAB = "new_message_notifi_quiet_donot_distrab";
    private static final String SP_CHAT_BACKGROUND = "chat_bg";
    private static final String SP_SCREEN_STATUS = "screen_capture_status";
    private static final String SP_RECEIVE_POKE_MESSAGE = "receive_poke_message";
    private final SharedPreferences sp;

    public UserConfigCache(Context context) {
        sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 新消息通知接受设置
     *
     * @param userId
     * @param remind
     */
    public void setNewMessageRemind(String userId, Boolean remind) {
        sp.edit().putBoolean(SP_NEW_MESSAGE_REMIND + userId, remind).commit();
    }

    /**
     * 获取新消息通知设置
     *
     * @param userId
     * @return
     */
    public Boolean getNewMessageRemind(String userId) {
        return sp.getBoolean(SP_NEW_MESSAGE_REMIND + userId, true);
    }

    /**
     * 设置免打扰时间
     *
     * @param userId
     * @param startTime
     * @param spanMinutes
     */
    public void setNotifiQuietHours(String userId, String startTime, int spanMinutes) {
        sp.edit().putString(SP_NEW_MESSAGE_QUIET_HOURS_STARTTIME + userId, startTime)
                .putInt(SP_NEW_MESSAGE_QUIET_HOURS_SPANMINUTES + userId, spanMinutes).commit();
    }


    /**
     * 获取免打扰时间
     *
     * @param userId
     * @return
     */
    public QuietHours getNotifiQUietHours(String userId) {
        String startTime = sp.getString(SP_NEW_MESSAGE_QUIET_HOURS_STARTTIME + userId, "");
        int spanMinutes = sp.getInt(SP_NEW_MESSAGE_QUIET_HOURS_SPANMINUTES + userId, 0);
        boolean donotDistrab = sp.getBoolean(SP_NEW_MESSAGE_QUIET_DONOT_DISTRAB + userId, false);
        QuietHours quietHours = new QuietHours();
        quietHours.startTime = startTime;
        quietHours.spanMinutes = spanMinutes;
        quietHours.isDonotDistrab = donotDistrab;
        return quietHours;
    }

    /**
     * 设置消息免打扰状态
     *
     * @param userId
     * @param status
     */
    public void setNotifiDonotDistrabStatus(String userId, boolean status) {
        sp.edit().putBoolean(SP_NEW_MESSAGE_QUIET_DONOT_DISTRAB + userId, status).commit();

    }

    /**
     * 获取免打扰设置
     *
     * @param userId
     * @return
     */
    public boolean getNotifiDonotDistrabStatus(String userId) {
        return sp.getBoolean(SP_NEW_MESSAGE_QUIET_DONOT_DISTRAB + userId, false);
    }

    /**
     * 设置背景图片 uri
     *
     * @param uri
     * @return
     */
    public void setChatbgUri(String uri) {
        sp.edit().putString(SP_CHAT_BACKGROUND, uri).commit();
    }

    /**
     * 获取背景图片 uri
     *
     * @return
     */
    public String getChatbgUri() {
        return sp.getString(SP_CHAT_BACKGROUND, "");
    }

    /**
     * 设置是否开启截屏通知功能状态
     *
     * @param status
     */
    public void setScreenCaptureStatus(int status) {
        sp.edit().putInt(SP_SCREEN_STATUS, status).commit();
    }

    /**
     * 获取是否开启截屏通知功能状态
     *
     * @return
     */
    public int getScreenCaptureStatus() {
        return sp.getInt(SP_SCREEN_STATUS, 0);
    }

    /**
     * 设置接收戳一下消息状态
     *
     * @param userId
     * @param isReceive
     */
    public void setReceivePokeMessageStatus(String userId, boolean isReceive) {
        sp.edit().putBoolean(SP_RECEIVE_POKE_MESSAGE + userId, isReceive).apply();
    }

    /**
     * 获取接收戳一下消息状态
     *
     * @param userId
     * @return
     */
    public boolean getReceivePokeMessageStatus(String userId) {
        return sp.getBoolean(SP_RECEIVE_POKE_MESSAGE + userId, true);
    }
}
