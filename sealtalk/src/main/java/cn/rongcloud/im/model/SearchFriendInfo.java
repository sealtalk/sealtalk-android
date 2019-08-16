package cn.rongcloud.im.model;

public class SearchFriendInfo {
    private String id;
    private String nickname;
    private String portraitUri;
    private String stAccount;
    private String gender;


    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    public void setPortraitUri(String portraitUri) {
        this.portraitUri = portraitUri;
    }

    public String getPortraitUri() {
        return portraitUri;
    }

    public String getStAccount() {
        return stAccount;
    }

    public void setStAccount(String stAccount) {
        this.stAccount = stAccount;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
