package cn.rongcloud.im.db;

/**
 * 此类是重构时新加,Friend,Groups,GroupMember共同的父类
 * 对于Groups,最开始设计时有groupsId的属性
 * 修改后UserInfoBean的id对应为Groups中的groupsId
 * 这种有些不伦不类的折中修改主要是为了尽量少修改现有代码
 */
public class UserInfoBean {
    private String id;
    private String name;
    private String portraitUri;

    public UserInfoBean() {
    }

    public UserInfoBean(String id) {
        this.id = id;
    }

    public UserInfoBean(String id, String name, String portraitUri) {
        this.id = id;
        this.name = name;
        this.portraitUri = portraitUri;
    }

    public String getUserId() {
        return id;
    }

    public void setUserId(String userId) {
        this.id = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPortraitUri() {
        return portraitUri;
    }

    public void setPortraitUri(String uri) {
        this.portraitUri = uri;
    }
}
