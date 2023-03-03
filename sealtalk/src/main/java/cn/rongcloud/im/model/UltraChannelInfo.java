package cn.rongcloud.im.model;

import com.google.gson.annotations.SerializedName;

public class UltraChannelInfo {
    @SerializedName("channelId")
    public String channelId;

    @SerializedName("channelName")
    public String channelName;

    @SerializedName("createdAt")
    public String createdAt;

    @SerializedName("type")
    public int type;

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
