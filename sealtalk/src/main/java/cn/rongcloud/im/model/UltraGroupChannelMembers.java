package cn.rongcloud.im.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/** @author gusd @Date 2022/06/20 */
public class UltraGroupChannelMembers {
    @SerializedName("users")
    private List<String> users;

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }
}
