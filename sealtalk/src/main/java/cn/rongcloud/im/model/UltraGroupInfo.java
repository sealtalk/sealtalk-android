package cn.rongcloud.im.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class UltraGroupInfo implements Serializable {
    @SerializedName("groupId")
    public String groupId;

    @SerializedName("groupName")
    public String groupName;

    @SerializedName("portraitUri")
    public String portraitUri;

    @SerializedName("creatorId")
    public String creatorId;

    @SerializedName("summary")
    public String summary;
}
