package cn.rongcloud.im.db.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "friend_description")
public class FriendDescription implements Parcelable {

    @PrimaryKey
    @NonNull
    private String id;

    @ColumnInfo(name = "displayName")
    private String displayName;

    @ColumnInfo(name = "region")
    private String region;

    @ColumnInfo(name = "phone")
    private String phone;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "imageUri")
    private String imageUri;

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }



    @Override
    public String toString() {
        return "FriendDescription{" +
                "id=" + id +
                ", displayName='" + displayName + '\'' +
                ", region='" + region + '\'' +
                ", phone='" + phone + '\'' +
                ", description='" + description + '\'' +
                ", imageUri='" + imageUri + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.displayName);
        dest.writeString(this.region);
        dest.writeString(this.phone);
        dest.writeString(this.description);
        dest.writeString(this.imageUri);
    }

    public FriendDescription() {
    }

    protected FriendDescription(Parcel in) {
        this.id = in.readString();
        this.displayName = in.readString();
        this.region = in.readString();
        this.phone = in.readString();
        this.description = in.readString();
        this.imageUri = in.readString();
    }

    public static final Creator<FriendDescription> CREATOR = new Creator<FriendDescription>() {
        @Override
        public FriendDescription createFromParcel(Parcel source) {
            return new FriendDescription(source);
        }

        @Override
        public FriendDescription[] newArray(int size) {
            return new FriendDescription[size];
        }
    };
}
