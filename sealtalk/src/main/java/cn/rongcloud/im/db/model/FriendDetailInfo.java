package cn.rongcloud.im.db.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

import java.util.Date;

@Entity
public class FriendDetailInfo implements Parcelable {
    @ColumnInfo(name = "id")
    private String id;
    @ColumnInfo(name = "name")
    private String nickname;
    @ColumnInfo(name = "region")
    private String region;
    @ColumnInfo(name = "phone_number")
    private String phone;
    @ColumnInfo(name = "portrait_uri")
    private String portraitUri;

    @ColumnInfo(name = "order_spelling")
    private String orderSpelling;
    @Ignore
    private String firstCharacter;

    @ColumnInfo(name = "name_spelling")
    private String nameSpelling;

    @Ignore
    private Date createdAt;

    @Ignore
    private Date updatedAt;

    @Ignore
    private long updatedTime;

    @Ignore
    private long createdTime;

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPortraitUri() {
        return portraitUri;
    }

    public void setPortraitUri(String portraitUri) {
        this.portraitUri = portraitUri;
    }

    public void setOrderSpelling(String orderSpelling) {
        this.orderSpelling = orderSpelling;
        if (!TextUtils.isEmpty(orderSpelling)) {
            if (orderSpelling.startsWith("unknown")) {
                // 未知符号提示为 #
                firstCharacter = "#";
            } else {
                firstCharacter = orderSpelling.substring(0, 1).toUpperCase();
            }
        }
    }

    public String getOrderSpelling() {
        return orderSpelling;
    }

    public String getFirstCharacter() {
        return firstCharacter;
    }

    public String getNameSpelling() {
        return nameSpelling;
    }

    public void setNameSpelling(String nameSpelling) {
        this.nameSpelling = nameSpelling;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(long updatedTime) {
        this.updatedTime = updatedTime;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    @Override
    public String toString() {
        return "FriendDetailInfo{" +
                "id='" + id + '\'' +
                ", nickname='" + nickname + '\'' +
                ", region='" + region + '\'' +
                ", phone='" + phone + '\'' +
                ", portraitUri='" + portraitUri + '\'' +
                ", orderSpelling='" + orderSpelling + '\'' +
                ", firstCharacter='" + firstCharacter + '\'' +
                ", nameSpelling='" + nameSpelling + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", updatedTime=" + updatedTime +
                ", createdTime=" + createdTime +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.nickname);
        dest.writeString(this.region);
        dest.writeString(this.phone);
        dest.writeString(this.portraitUri);
        dest.writeString(this.orderSpelling);
        dest.writeString(this.firstCharacter);
        dest.writeString(this.nameSpelling);
        dest.writeLong(this.createdAt != null ? this.createdAt.getTime() : -1);
        dest.writeLong(this.updatedAt != null ? this.updatedAt.getTime() : -1);
        dest.writeLong(this.updatedTime);
        dest.writeLong(this.createdTime);
    }

    public FriendDetailInfo() {
    }

    protected FriendDetailInfo(Parcel in) {
        this.id = in.readString();
        this.nickname = in.readString();
        this.region = in.readString();
        this.phone = in.readString();
        this.portraitUri = in.readString();
        this.orderSpelling = in.readString();
        this.firstCharacter = in.readString();
        this.nameSpelling = in.readString();
        long tmpCreatedAt = in.readLong();
        this.createdAt = tmpCreatedAt == -1 ? null : new Date(tmpCreatedAt);
        long tmpUpdatedAt = in.readLong();
        this.updatedAt = tmpUpdatedAt == -1 ? null : new Date(tmpUpdatedAt);
        this.updatedTime = in.readLong();
        this.createdTime = in.readLong();
    }

    public static final Parcelable.Creator<FriendDetailInfo> CREATOR = new Parcelable.Creator<FriendDetailInfo>() {
        @Override
        public FriendDetailInfo createFromParcel(Parcel source) {
            return new FriendDetailInfo(source);
        }

        @Override
        public FriendDetailInfo[] newArray(int size) {
            return new FriendDetailInfo[size];
        }
    };
}