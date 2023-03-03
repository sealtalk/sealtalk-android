package cn.rongcloud.im.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class UserGroupInfo implements Serializable {

    @SerializedName("userGroupId")
    public String userGroupId;

    @SerializedName("userGroupName")
    public String userGroupName;

    @SerializedName("memberCount")
    public int memberCount;

    public UserGroupInfo() {}

    @Override
    public String toString() {
        return "UserGroupInfo{"
                + "userGroupId='"
                + userGroupId
                + '\''
                + ", userGroupName='"
                + userGroupName
                + '\''
                + ", memberCount='"
                + memberCount
                + '}';
    }
}
