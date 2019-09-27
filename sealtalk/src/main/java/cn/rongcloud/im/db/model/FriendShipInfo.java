/**
 * Copyright 2019 bejson.com
 */
package cn.rongcloud.im.db.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

import java.util.Date;

public class FriendShipInfo implements Parcelable {

    @ColumnInfo(name = "alias")
    private String displayName;
    @ColumnInfo(name = "message")
    private String message;
    @ColumnInfo(name = "friend_status")
    private int status;
    @ColumnInfo(name = "updateAt")
    private Date updatedAt;
    @Embedded
    private FriendDetailInfo user;
    @ColumnInfo(name = "alias_spelling")
    private String disPlayNameSpelling;
    @ColumnInfo(name= "nickname")
    private String groupDisplayName;
    @ColumnInfo(name= "nickname_spelling")
    private String groupDisplayNameSpelling;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }


    public FriendDetailInfo getUser() {
        return user;
    }

    public void setUser(FriendDetailInfo user) {
        this.user = user;
    }

    public String getDisPlayNameSpelling() {
        return disPlayNameSpelling;
    }

    public void setDisPlayNameSpelling(String disPlayNameSpelling) {
        this.disPlayNameSpelling = disPlayNameSpelling;
    }

    public String getGroupDisplayName() {
        return groupDisplayName;
    }

    public void setGroupDisplayName(String groupDisplayName) {
        this.groupDisplayName = groupDisplayName;
    }

    public String getGroupDisplayNameSpelling() {
        return groupDisplayNameSpelling;
    }

    public void setGroupDisplayNameSpelling(String groupDisplayNameSpelling) {
        this.groupDisplayNameSpelling = groupDisplayNameSpelling;
    }

    @Override
    public String toString() {
        return "FriendShipInfo{" +
                "displayName='" + displayName + '\'' +
                ", message='" + message + '\'' +
                ", status=" + status +
                ", updatedAt=" + updatedAt +
                ", user=" + user +
                ", disPlayNameSpelling='" + disPlayNameSpelling + '\'' +
                ", groupDisplayName='" + groupDisplayName + '\'' +
                ", groupDisplayNameSpelling='" + groupDisplayNameSpelling + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.displayName);
        dest.writeString(this.message);
        dest.writeInt(this.status);
        dest.writeLong(this.updatedAt != null ? this.updatedAt.getTime() : -1);
        dest.writeParcelable(this.user, flags);
        dest.writeString(this.disPlayNameSpelling);
        dest.writeString(this.groupDisplayName);
        dest.writeString(this.groupDisplayNameSpelling);
    }

    public FriendShipInfo() {
    }

    protected FriendShipInfo(Parcel in) {
        this.displayName = in.readString();
        this.message = in.readString();
        this.status = in.readInt();
        long tmpUpdatedAt = in.readLong();
        this.updatedAt = tmpUpdatedAt == -1 ? null : new Date(tmpUpdatedAt);
        this.user = in.readParcelable(FriendDetailInfo.class.getClassLoader());
        this.disPlayNameSpelling = in.readString();
        this.groupDisplayName = in.readString();
        this.groupDisplayNameSpelling = in.readString();
    }

    public static final Parcelable.Creator<FriendShipInfo> CREATOR = new Parcelable.Creator<FriendShipInfo>() {
        @Override
        public FriendShipInfo createFromParcel(Parcel source) {
            return new FriendShipInfo(source);
        }

        @Override
        public FriendShipInfo[] newArray(int size) {
            return new FriendShipInfo[size];
        }
    };
}