package cn.rongcloud.im.model;

/**
 * 获取通讯录信息
 */
public class GetContactInfoResult {
    private int registered;     // 0 未注册 1 已注册
    private int relationship;   // 0 非好友 1 好友
    private String stAccount;   // sealtalk 号
    private String phone;       // 电话号码
    private String id;          // 用户 id
    private String nickname;    // 用户昵称
    private String portraitUri; // 用户头像

    public int getRegistered() {
        return registered;
    }

    public void setRegistered(int registered) {
        this.registered = registered;
    }

    public int getRelationship() {
        return relationship;
    }

    public void setRelationship(int relationship) {
        this.relationship = relationship;
    }

    public String getStAccount() {
        return stAccount;
    }

    public void setStAccount(String stAccount) {
        this.stAccount = stAccount;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPortraitUri() {
        return portraitUri;
    }

    public void setPortraitUri(String portraitUri) {
        this.portraitUri = portraitUri;
    }
}
