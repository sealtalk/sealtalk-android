package cn.rongcloud.im.db.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

/**
 * 邀请者信息
 */
@Entity
public class GroupNoticeReceiverInfo implements Parcelable {
    @ColumnInfo(name = "id")
    private String id;

    @ColumnInfo(name = "nickname")
    private String nickname;

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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.nickname);
    }

    public GroupNoticeReceiverInfo() {
    }

    protected GroupNoticeReceiverInfo(Parcel in) {
        this.id = in.readString();
        this.nickname = in.readString();
    }

    public static final Parcelable.Creator<GroupNoticeReceiverInfo> CREATOR = new Parcelable.Creator<GroupNoticeReceiverInfo>() {
        @Override
        public GroupNoticeReceiverInfo createFromParcel(Parcel source) {
            return new GroupNoticeReceiverInfo(source);
        }

        @Override
        public GroupNoticeReceiverInfo[] newArray(int size) {
            return new GroupNoticeReceiverInfo[size];
        }
    };

    @Override
    public String toString() {
        return "GroupNoticeReceiverInfo{" +
                "id='" + id + '\'' +
                ", nickname='" + nickname + '\'' +
                '}';
    }
}
