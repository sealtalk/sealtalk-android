package cn.rongcloud.im.db.model;


import cn.rongcloud.im.model.BlackListUser;

public class FriendBlackInfo {
    private BlackListUser user;

    public void setUser(BlackListUser user) {
        this.user = user;
    }

    public BlackListUser getUser() {
        return user;
    }

}