package cn.rongcloud.im.model.qrcode;

/**
 * QR 中群组信息
 */
public class QRGroupInfo {
    private String groupId;
    private String sharedUserId;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getSharedUserId() {
        return sharedUserId;
    }

    public void setSharedUserId(String sharedUserId) {
        this.sharedUserId = sharedUserId;
    }
}
