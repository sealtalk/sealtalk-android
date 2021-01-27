package cn.rongcloud.im.db.model;

/**
 * 好友关系
 */
public enum FriendStatus {
    /**
     * 发出了好友邀请
     */
    SEND_REQUEST(10),
    /**
     * 收到了好友邀请
     */
    RECEIVE_REQUEST(11),
    /**
     * 忽略好友邀请
     */
    IGNORE_REQUEST(21),
    /**
     * 已是好友
     */
    IS_FRIEND(20),
    /**
     * 删除了好友关系
     */
    DELETE_FRIEND(30),
    /**
     * 在黑名单中
     */
    IN_BLACK_LIST(31),

    NONE(0);

    int statusCode;

    FriendStatus(int statusCode) {
        this.statusCode = statusCode;
    }

    public static FriendStatus getStatus(int statusCode) {
        FriendStatus[] values = FriendStatus.values();
        for (FriendStatus status : values) {
            if (status.statusCode == statusCode) {
                return status;
            }
        }
        return NONE;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
