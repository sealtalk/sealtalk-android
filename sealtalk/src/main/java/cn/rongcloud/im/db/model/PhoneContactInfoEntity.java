package cn.rongcloud.im.db.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "phone_contact")
public class PhoneContactInfoEntity {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "phone_number")
    private String phoneNumber;

    @ColumnInfo(name = "is_friend")
    private int relationship;   // 0 非好友 1 好友

    @ColumnInfo(name = "user_id")
    private String userId;

    @ColumnInfo(name = "contact_name")
    private String contactName;

    @NonNull
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(@NonNull String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getRelationship() {
        return relationship;
    }

    public void setRelationship(int relationship) {
        this.relationship = relationship;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }
}
