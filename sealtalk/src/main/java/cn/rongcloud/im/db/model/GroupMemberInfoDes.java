package cn.rongcloud.im.db.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import java.util.ArrayList;

@Entity(tableName = "group_member_info_des", primaryKeys = {"groupId", "memberId"})
public class GroupMemberInfoDes implements Parcelable {

    @ColumnInfo(name = "groupId")
    @NonNull
    private String groupId;

    @ColumnInfo(name = "memberId")
    @NonNull
    private String memberId;

    @ColumnInfo(name = "groupNickname")
    private String groupNickname;

    @ColumnInfo(name = "region")
    private String region;

    @ColumnInfo(name = "phone")
    private String phone;

    @ColumnInfo(name = "WeChat")
    private String WeChat;

    @ColumnInfo(name = "Alipay")
    private String Alipay;

    @ColumnInfo(name = "memberDesc")
    private ArrayList<String> memberDesc;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getGroupNickname() {
        return groupNickname;
    }

    public void setGroupNickname(String groupNickname) {
        this.groupNickname = groupNickname;
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

    public String getWeChat() {
        return WeChat;
    }

    public void setWeChat(String weChat) {
        WeChat = weChat;
    }

    public String getAlipay() {
        return Alipay;
    }

    public void setAlipay(String alipay) {
        Alipay = alipay;
    }

    public ArrayList<String> getMemberDesc() {
        return memberDesc;
    }

    public void setMemberDesc(ArrayList<String> memberDesc) {
        this.memberDesc = memberDesc;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.groupId);
        dest.writeString(this.memberId);
        dest.writeString(this.groupNickname);
        dest.writeString(this.region);
        dest.writeString(this.phone);
        dest.writeString(this.WeChat);
        dest.writeString(this.Alipay);
        dest.writeStringList(this.memberDesc);
    }

    public GroupMemberInfoDes() {
    }

    protected GroupMemberInfoDes(Parcel in) {
        this.groupId = in.readString();
        this.memberId = in.readString();
        this.groupNickname = in.readString();
        this.region = in.readString();
        this.phone = in.readString();
        this.WeChat = in.readString();
        this.Alipay = in.readString();
        this.memberDesc = in.createStringArrayList();
    }

    public static final Creator<GroupMemberInfoDes> CREATOR = new Creator<GroupMemberInfoDes>() {
        @Override
        public GroupMemberInfoDes createFromParcel(Parcel source) {
            return new GroupMemberInfoDes(source);
        }

        @Override
        public GroupMemberInfoDes[] newArray(int size) {
            return new GroupMemberInfoDes[size];
        }
    };

    @Override
    public String toString() {
        return "GroupMemberInfoDes{" +
                "groupId='" + groupId + '\'' +
                ", memberId='" + memberId + '\'' +
                ", groupNickname='" + groupNickname + '\'' +
                ", region='" + region + '\'' +
                ", phone='" + phone + '\'' +
                ", WeChat='" + WeChat + '\'' +
                ", Alipay='" + Alipay + '\'' +
                ", memberDesc=" + memberDesc +
                '}';
    }
}
