package cn.rongcloud.im.model;

import android.os.Parcel;
import android.os.Parcelable;

public class UltraUserInfo implements Parcelable {
    private String portraitId;
    private int conversationType;
    private String groupId;
    private String creatorId;
    private String groupName;

    protected UltraUserInfo(Parcel in) {
        setPortraitId(in.readString());
        setConversationType(in.readInt());
        setGroupId(in.readString());
        setCreatorId(in.readString());
        setGroupName(in.readString());
    }

    public static final Creator<UltraUserInfo> CREATOR =
            new Creator<UltraUserInfo>() {
                @Override
                public UltraUserInfo createFromParcel(Parcel in) {
                    return new UltraUserInfo(in);
                }

                @Override
                public UltraUserInfo[] newArray(int size) {
                    return new UltraUserInfo[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.portraitId);
        dest.writeInt(this.conversationType);
        dest.writeString(this.groupId);
        dest.writeString(this.creatorId);
        dest.writeString(this.groupName);
    }

    public String getPortraitId() {
        return portraitId;
    }

    public void setPortraitId(String portraitId) {
        this.portraitId = portraitId;
    }

    public int getConversationType() {
        return conversationType;
    }

    public void setConversationType(int conversationType) {
        this.conversationType = conversationType;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
