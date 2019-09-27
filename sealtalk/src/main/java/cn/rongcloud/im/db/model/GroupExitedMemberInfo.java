package cn.rongcloud.im.db.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "group_exited")
public class GroupExitedMemberInfo implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int id;

    @ColumnInfo(name = "quitUserId")
    private String quitUserId;

    @ColumnInfo(name = "quitNickname")
    private String quitNickname;

    @ColumnInfo(name = "quitPortraitUri")
    private String quitPortraitUri;
    /**
     * 退群原因
     * 0 被群主 移除群聊、 1 被管理员 移除、 2 主动退出
     */
    @ColumnInfo(name = "quitReason")
    private int quitReason;

    @ColumnInfo(name = "quitTime")
    private String quitTime;

    @ColumnInfo(name = "operatorId")
    private String operatorId;

    @ColumnInfo(name = "operatorName")
    private String operatorName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuitUserId() {
        return quitUserId;
    }

    public void setQuitUserId(String quitUserId) {
        this.quitUserId = quitUserId;
    }

    public String getQuitNickname() {
        return quitNickname;
    }

    public void setQuitNickname(String quitNickname) {
        this.quitNickname = quitNickname;
    }

    public String getQuitPortraitUri() {
        return quitPortraitUri;
    }

    public void setQuitPortraitUri(String quitPortraitUri) {
        this.quitPortraitUri = quitPortraitUri;
    }

    public int getQuitReason() {
        return quitReason;
    }

    public void setQuitReason(int quitReason) {
        this.quitReason = quitReason;
    }

    public String getQuitTime() {
        return quitTime;
    }

    public void setQuitTime(String quitTime) {
        this.quitTime = quitTime;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    @Override
    public String toString() {
        return "GroupExitedMemberInfo{" +
                "id='" + id + '\'' +
                ", quitUserId='" + quitUserId + '\'' +
                ", quitNickname='" + quitNickname + '\'' +
                ", quitPortraitUri='" + quitPortraitUri + '\'' +
                ", quitReason=" + quitReason +
                ", quitTime='" + quitTime + '\'' +
                ", operatorId='" + operatorId + '\'' +
                ", operatorName='" + operatorName + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.quitUserId);
        dest.writeString(this.quitNickname);
        dest.writeString(this.quitPortraitUri);
        dest.writeInt(this.quitReason);
        dest.writeString(this.quitTime);
        dest.writeString(this.operatorId);
        dest.writeString(this.operatorName);
    }

    public GroupExitedMemberInfo() {
    }

    protected GroupExitedMemberInfo(Parcel in) {
        this.id = in.readInt();
        this.quitUserId = in.readString();
        this.quitNickname = in.readString();
        this.quitPortraitUri = in.readString();
        this.quitReason = in.readInt();
        this.quitTime = in.readString();
        this.operatorId = in.readString();
        this.operatorName = in.readString();
    }

    public static final Creator<GroupExitedMemberInfo> CREATOR = new Creator<GroupExitedMemberInfo>() {
        @Override
        public GroupExitedMemberInfo createFromParcel(Parcel source) {
            return new GroupExitedMemberInfo(source);
        }

        @Override
        public GroupExitedMemberInfo[] newArray(int size) {
            return new GroupExitedMemberInfo[size];
        }
    };
}
