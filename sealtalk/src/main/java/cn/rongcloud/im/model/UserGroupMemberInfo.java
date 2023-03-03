package cn.rongcloud.im.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class UserGroupMemberInfo implements Serializable {

    @SerializedName("id")
    public String id; // 用户id

    @SerializedName("nickname")
    public String nickname; // 用户昵称

    @SerializedName("region")
    public String region; // "86"

    @SerializedName("phone")
    public String phone; // 手机号

    @SerializedName("portraitUri")
    public String portraitUri; // 头像

    @SerializedName("gender")
    public String gender;

    @SerializedName("stAccount")
    public String stAccount;

    public UserGroupMemberInfo() {}

    public UserGroupMemberInfo(String id, String nickname, String portraitUri) {
        this.id = id;
        this.nickname = nickname;
        this.portraitUri = portraitUri;
    }

    @Override
    public String toString() {
        return "UserGroupMemberInfo{"
                + "id='"
                + id
                + '\''
                + ", nickname='"
                + nickname
                + '\''
                + ", portraitUri='"
                + portraitUri
                + '\''
                + '}';
    }
}
