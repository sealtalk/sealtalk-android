package cn.rongcloud.im.model;

import androidx.room.ColumnInfo;

public class PhoneContactInfo {
    @ColumnInfo(name = "user_id")
    private String userId;
    @ColumnInfo(name = "phone_number")
    private String phone;
    @ColumnInfo(name = "name")
    private String nickName;
    @ColumnInfo(name = "name_spelling")
    private String nickNameSpelling;
    @ColumnInfo(name = "portrait_uri")
    private String portraitUrl;
    @ColumnInfo(name = "is_friend")
    private int isFriend;
    @ColumnInfo(name = "st_account")
    private String stAccount;
    @ColumnInfo(name = "contact_name")
    private String contactName;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getPortraitUrl() {
        return portraitUrl;
    }

    public void setPortraitUrl(String portraitUrl) {
        this.portraitUrl = portraitUrl;
    }

    public int isFriend() {
        return isFriend;
    }

    public void setFriend(int friend) {
        isFriend = friend;
    }

    public String getStAccount() {
        return stAccount;
    }

    public void setStAccount(String stAccount) {
        this.stAccount = stAccount;
    }

    public String getNickNameSpelling() {
        return nickNameSpelling;
    }

    public void setNickNameSpelling(String nickNameSpelling) {
        this.nickNameSpelling = nickNameSpelling;
    }

    public void setIsFriend(int isFriend) {
        this.isFriend = isFriend;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }
}
