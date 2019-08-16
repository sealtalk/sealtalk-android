package cn.rongcloud.im.db.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

/**
 * 邀请者信息
 */
@Entity
public class GroupNoticeGroupInfo implements Parcelable {
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

    public GroupNoticeGroupInfo() {
    }

    protected GroupNoticeGroupInfo(Parcel in) {
        this.id = in.readString();
        this.nickname = in.readString();
    }

    public static final Creator<GroupNoticeGroupInfo> CREATOR = new Creator<GroupNoticeGroupInfo>() {
        @Override
        public GroupNoticeGroupInfo createFromParcel(Parcel source) {
            return new GroupNoticeGroupInfo(source);
        }

        @Override
        public GroupNoticeGroupInfo[] newArray(int size) {
            return new GroupNoticeGroupInfo[size];
        }
    };

    @Override
    public String toString() {
        return "GroupNoticeRequesterInfo{" +
                "id='" + id + '\'' +
                ", nickname='" + nickname + '\'' +
                '}';
    }
}
