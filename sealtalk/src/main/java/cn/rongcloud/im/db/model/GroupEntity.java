package cn.rongcloud.im.db.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "group")
public class GroupEntity implements Parcelable {
    @PrimaryKey
    @NonNull
    private String id;
    @ColumnInfo(name = "portrait_url")
    private String portraitUri;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "name_spelling")
    private String nameSpelling;
    @ColumnInfo(name = "name_spelling_initial")
    private String nameSpellingInitial;
    @ColumnInfo(name = "order_spelling")
    private String orderSpelling;
    /**
     * 群人数
     */
    @ColumnInfo(name = "member_count")
    private int memberCount;
    /**
     * 群人数上限
     */
    @ColumnInfo(name = "max_member_count")
    private int maxMemberCount;
    /**
     * 群主 id
     */
    @ColumnInfo(name = "owner_user_id")
    private String creatorId;
    /**
     * 类型: 1,普通群;2,企业群
     */
    @ColumnInfo(name = "type")
    private int type;
    /**
     * 群公告
     */
    @ColumnInfo(name = "bulletin")
    private String bulletin;
    /**
     * 群公告更新时间
     */
    @ColumnInfo(name = "bulletin_time")
    private long bulletinTime;
    /**
     * 删除日期
     */
    @ColumnInfo(name = "delete_at")
    private Date deletedAt;
    /**
     * 是否在通讯录，0：不在；1：在
     */
    @ColumnInfo(name = "is_in_contact")
    private int isInContact;
    /**
     * 定时删除时间状态
     */
    @ColumnInfo(name = "regular_clear_state")
    private int regularClearState;
    /**
     * 全员禁言
     */
    @ColumnInfo(name = "is_mute_all")
    private int isMute;

    /**
     * 入群认证
     */
    @ColumnInfo(name = "certification_status")
    private int certiStatus;

    /**
     * 成员保护
     */
    @ColumnInfo(name = "member_protection")
    private int memberProtection;

    public int getMemberProtection() {
        return memberProtection;
    }

    public void setMemberProtection(int memberProtection) {
        this.memberProtection = memberProtection;
    }

    public int getIsMute() {
        return isMute;
    }

    public void setIsMute(int isMute) {
        this.isMute = isMute;
    }

    public int getRegularClearState() {
        return regularClearState;
    }

    public void setRegularClearState(int regularClearState) {
        this.regularClearState = regularClearState;
    }

    public int getCertiStatus() {
        return certiStatus;
    }

    public void setCertiStatus(int certiStatus) {
        this.certiStatus = certiStatus;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getPortraitUri() {
        return portraitUri;
    }

    public void setPortraitUri(String portraitUri) {
        this.portraitUri = portraitUri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameSpelling() {
        return nameSpelling;
    }

    public void setNameSpelling(String nameSpelling) {
        this.nameSpelling = nameSpelling;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public int getMaxMemberCount() {
        return maxMemberCount;
    }

    public void setMaxMemberCount(int maxMemberCount) {
        this.maxMemberCount = maxMemberCount;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getBulletin() {
        return bulletin;
    }

    public void setBulletin(String bulletin) {
        this.bulletin = bulletin;
    }

    public Date getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Date deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getNameSpellingInitial() {
        return nameSpellingInitial;
    }

    public void setNameSpellingInitial(String nameSpellingInitial) {
        this.nameSpellingInitial = nameSpellingInitial;
    }

    public String getOrderSpelling() {
        return orderSpelling;
    }

    public void setOrderSpelling(String orderSpelling) {
        this.orderSpelling = orderSpelling;
    }

    public long getBulletinTime() {
        return bulletinTime;
    }

    public void setBulletinTime(long bulletinTime) {
        this.bulletinTime = bulletinTime;
    }

    public int getIsInContact() {
        return isInContact;
    }

    public void setIsInContact(int isInContact) {
        this.isInContact = isInContact;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.portraitUri);
        dest.writeString(this.name);
        dest.writeString(this.nameSpelling);
        dest.writeString(this.nameSpellingInitial);
        dest.writeString(this.orderSpelling);
        dest.writeInt(this.memberCount);
        dest.writeInt(this.maxMemberCount);
        dest.writeString(this.creatorId);
        dest.writeInt(this.type);
        dest.writeString(this.bulletin);
        dest.writeLong(this.bulletinTime);
        dest.writeLong(this.deletedAt != null ? this.deletedAt.getTime() : -1);
        dest.writeInt(this.isInContact);
        dest.writeInt(this.regularClearState);
        dest.writeInt(this.isMute);
        dest.writeInt(this.certiStatus);
        dest.writeInt(this.memberProtection);
    }

    public GroupEntity() {
    }

    protected GroupEntity(Parcel in) {
        this.id = in.readString();
        this.portraitUri = in.readString();
        this.name = in.readString();
        this.nameSpelling = in.readString();
        this.nameSpellingInitial = in.readString();
        this.orderSpelling = in.readString();
        this.memberCount = in.readInt();
        this.maxMemberCount = in.readInt();
        this.creatorId = in.readString();
        this.type = in.readInt();
        this.bulletin = in.readString();
        this.bulletinTime = in.readLong();
        long tmpDeletedAt = in.readLong();
        this.deletedAt = tmpDeletedAt == -1 ? null : new Date(tmpDeletedAt);
        this.isInContact = in.readInt();
        this.regularClearState = in.readInt();
        this.isMute = in.readInt();
        this.certiStatus = in.readInt();
        this.memberProtection = in.readInt();
    }

    public static final Creator<GroupEntity> CREATOR = new Creator<GroupEntity>() {
        @Override
        public GroupEntity createFromParcel(Parcel source) {
            return new GroupEntity(source);
        }

        @Override
        public GroupEntity[] newArray(int size) {
            return new GroupEntity[size];
        }
    };

    @Override
    public String toString() {
        return "GroupEntity{" +
                "id='" + id + '\'' +
                ", portraitUri='" + portraitUri + '\'' +
                ", name='" + name + '\'' +
                ", nameSpelling='" + nameSpelling + '\'' +
                ", nameSpellingInitial='" + nameSpellingInitial + '\'' +
                ", orderSpelling='" + orderSpelling + '\'' +
                ", memberCount=" + memberCount +
                ", maxMemberCount=" + maxMemberCount +
                ", creatorId='" + creatorId + '\'' +
                ", type=" + type +
                ", bulletin='" + bulletin + '\'' +
                ", bulletinTime=" + bulletinTime +
                ", deletedAt=" + deletedAt +
                ", isInContact=" + isInContact +
                ", regularClearState=" + regularClearState +
                ", isMute=" + isMute +
                ", certiStatus=" + certiStatus +
                ", memberProtection=" + memberProtection +
                '}';
    }
}
