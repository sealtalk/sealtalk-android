package cn.rongcloud.im.db.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "user")
public class UserInfo {
    @PrimaryKey
    @NonNull
    private String id;
    @ColumnInfo(name = "portrait_uri")
    private String portraitUri;
    @ColumnInfo(name = "name")
    @SerializedName(value = "name", alternate = {"nickname"})
    private String name;
    @ColumnInfo(name = "name_spelling")
    private String nameSpelling;
    @ColumnInfo(name = "name_spelling_initial")
    private String nameSpellingInitial;
    @ColumnInfo(name = "alias")
    private String alias;
    @ColumnInfo(name = "alias_spelling")
    private String aliasSpelling;

    @ColumnInfo(name = "alias_spelling_initial")
    private String aliasSpellingInitial;
    @ColumnInfo(name = "region")
    private String region;
    @ColumnInfo(name = "phone_number")
    private String phoneNumber;
    @ColumnInfo(name = "friend_status")
    private int friendStatus;
    @ColumnInfo(name = "order_spelling")
    private String orderSpelling;
    @ColumnInfo(name = "st_account")
    private String stAccount;
    @ColumnInfo(name = "gender")
    private String gender;

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getStAccount() {
        return stAccount;
    }

    public void setStAccount(String stAccount) {
        this.stAccount = stAccount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public void setPortraitUri(String portraitUri) {
        this.portraitUri = portraitUri;
    }

    public String getNameSpelling() {
        return nameSpelling;
    }

    public void setNameSpelling(String nameSpelling) {
        this.nameSpelling = nameSpelling;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAliasSpelling() {
        return aliasSpelling;
    }

    public void setAliasSpelling(String aliasSpelling) {
        this.aliasSpelling = aliasSpelling;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getFriendStatus() {
        return friendStatus;
    }

    public void setFriendStatus(int friendStatus) {
        this.friendStatus = friendStatus;
    }

    public void setOrderSpelling(String orderSpelling) {
        this.orderSpelling = orderSpelling;
    }

    public String getOrderSpelling() {
        return orderSpelling;
    }

    public String getNameSpellingInitial() {
        return nameSpellingInitial;
    }

    public void setNameSpellingInitial(String nameSpellingInitial) {
        this.nameSpellingInitial = nameSpellingInitial;
    }

    public String getAliasSpellingInitial() {
        return aliasSpellingInitial;
    }

    public void setAliasSpellingInitial(String aliasSpellingInitial) {
        this.aliasSpellingInitial = aliasSpellingInitial;
    }


    @Override
    public String toString() {
        return "UserInfo{" +
                "id='" + id + '\'' +
                ", portraitUri='" + portraitUri + '\'' +
                ", name='" + name + '\'' +
                ", nameSpelling='" + nameSpelling + '\'' +
                ", nameSpellingInitial='" + nameSpellingInitial + '\'' +
                ", alias='" + alias + '\'' +
                ", aliasSpelling='" + aliasSpelling + '\'' +
                ", aliasSpellingInitial='" + aliasSpellingInitial + '\'' +
                ", region='" + region + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", friendStatus=" + friendStatus +
                ", orderSpelling='" + orderSpelling + '\'' +
                ", stAccount='" + stAccount + '\'' +
                ", gender='" + gender + '\'' +
                '}';
    }
}
