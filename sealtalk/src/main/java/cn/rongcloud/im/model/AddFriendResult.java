package cn.rongcloud.im.model;

public class AddFriendResult {
    /**
     * Added: 已添加 None: 在对方黑名单中 Sent: 请求已发送
     */
    private String action;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
