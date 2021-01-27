package cn.rongcloud.im.db.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "group_notice")
public class GroupNoticeInfo implements Parcelable {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;
    /**
     * 0: 忽略、1: 同意、2: 等待
     */
    @ColumnInfo(name = "status")
    private int status;
    /**
     * 1: 待被邀请者处理、2: 待管理员处理
     */
    @ColumnInfo(name = "type")
    private int type;

    @ColumnInfo(name = "createdAt")
    private String createdAt;

    @ColumnInfo(name = "createdTime")
    private String createdTime;

    @ColumnInfo(name = "requester_id")
    private String requesterId;

    @ColumnInfo(name = "requester_nick_name")
    private String requesterNickName;

    @ColumnInfo(name = "receiver_id")
    private String receiverId;

    @ColumnInfo(name = "receiver_nick_name")
    private String receiverNickName;

    @ColumnInfo(name = "group_id")
    private String groupId;

    @ColumnInfo(name = "group_nick_name")
    private String groupNickName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public String getRequesterNickName() {
        return requesterNickName;
    }

    public void setRequesterNickName(String requesterNickName) {
        this.requesterNickName = requesterNickName;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverNickName() {
        return receiverNickName;
    }

    public void setReceiverNickName(String receiverNickName) {
        this.receiverNickName = receiverNickName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupNickName() {
        return groupNickName;
    }

    public void setGroupNickName(String groupNickName) {
        this.groupNickName = groupNickName;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeInt(this.status);
        dest.writeInt(this.type);
        dest.writeString(this.createdAt);
        dest.writeString(this.createdTime);
        dest.writeString(this.requesterId);
        dest.writeString(this.requesterNickName);
        dest.writeString(this.receiverId);
        dest.writeString(this.receiverNickName);
        dest.writeString(this.groupId);
        dest.writeString(this.groupNickName);
    }

    public GroupNoticeInfo() {
    }

    protected GroupNoticeInfo(Parcel in) {
        this.id = in.readString();
        this.status = in.readInt();
        this.type = in.readInt();
        this.createdAt = in.readString();
        this.createdTime = in.readString();
        this.requesterId = in.readString();
        this.requesterNickName = in.readString();
        this.receiverId = in.readString();
        this.receiverNickName = in.readString();
        this.groupId = in.readString();
        this.groupNickName = in.readString();
    }

    public static final Creator<GroupNoticeInfo> CREATOR = new Creator<GroupNoticeInfo>() {
        @Override
        public GroupNoticeInfo createFromParcel(Parcel source) {
            return new GroupNoticeInfo(source);
        }

        @Override
        public GroupNoticeInfo[] newArray(int size) {
            return new GroupNoticeInfo[size];
        }
    };

    @Override
    public String toString() {
        return "GroupNoticeInfo{" +
                "id='" + id + '\'' +
                ", status=" + status +
                ", type=" + type +
                ", createdAt='" + createdAt + '\'' +
                ", createdTime='" + createdTime + '\'' +
                ", requesterId='" + requesterId + '\'' +
                ", requesterNickName='" + requesterNickName + '\'' +
                ", receiverId='" + receiverId + '\'' +
                ", receiverNickName='" + receiverNickName + '\'' +
                ", groupId='" + groupId + '\'' +
                ", groupNickName='" + groupNickName + '\'' +
                '}';
    }
}
