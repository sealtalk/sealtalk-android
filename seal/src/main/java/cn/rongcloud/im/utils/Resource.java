package cn.rongcloud.im.utils;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import io.rong.common.ParcelUtils;
/**
 * Created by AMing on 16/6/13.
 * Company RongCloud
 */
public class Resource implements Parcelable {
    protected Uri uri;
    public static final Creator<Resource> CREATOR = new Creator() {
        public Resource createFromParcel(Parcel in) {
            return new Resource(in);
        }

        public Resource[] newArray(int size) {
            return new Resource[size];
        }
    };

    public Resource() {
    }

    public Resource(Uri uri) {
        this.uri = uri;
    }

    public Resource(Parcel in) {
        this( ParcelUtils.readFromParcel(in, Uri.class));
    }

    public Resource(Resource resource) {
        this.uri = resource.getUri();
    }

    public Resource(String uriPath) {
        this(Uri.parse(uriPath));
    }

    public Uri getUri() {
        return this.uri;
    }

    public int hashCode() {
        return this.uri.hashCode();
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Resource o) {
        return (o != null && (o.getUri() != null || this.getUri() == null)) && o.getUri().equals(this.getUri());
    }

    public void writeToParcel(Parcel dest, int flags) {
        ParcelUtils.writeToParcel(dest, this.uri);
    }
}
