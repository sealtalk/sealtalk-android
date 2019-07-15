package cn.rongcloud.im.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 国家信息类
 */
public class CountryInfo implements Parcelable {

    private String countryName;      //国家名
    private String zipCode;          //区号
    private String firstChar;        //首字母
    private String countryNameCN;
    private String countryNameEN;

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getFirstChar() {
        return firstChar;
    }

    public void setFirstChar(String firstChar) {
        this.firstChar = firstChar;
    }

    public String getCountryNameCN() {
        return countryNameCN;
    }

    public void setCountryNameCN(String countryNameCN) {
        this.countryNameCN = countryNameCN;
    }

    public String getCountryNameEN() {
        return countryNameEN;
    }

    public void setCountryNameEN(String countryNameEN) {
        this.countryNameEN = countryNameEN;
    }


    @Override
    public String toString() {
        return "CountryInfo{" +
                "countryName='" + countryName + '\'' +
                ", zipCode='" + zipCode + '\'' +
                ", firstChar='" + firstChar + '\'' +
                ", countryNameCN='" + countryNameCN + '\'' +
                ", countryNameEN='" + countryNameEN + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.countryName);
        dest.writeString(this.zipCode);
        dest.writeString(this.firstChar);
        dest.writeString(this.countryNameCN);
        dest.writeString(this.countryNameEN);
    }

    public CountryInfo() {
    }

    protected CountryInfo(Parcel in) {
        this.countryName = in.readString();
        this.zipCode = in.readString();
        this.firstChar = in.readString();
        this.countryNameCN = in.readString();
        this.countryNameEN = in.readString();
    }

    public static final Parcelable.Creator<CountryInfo> CREATOR = new Parcelable.Creator<CountryInfo>() {
        @Override
        public CountryInfo createFromParcel(Parcel source) {
            return new CountryInfo(source);
        }

        @Override
        public CountryInfo[] newArray(int size) {
            return new CountryInfo[size];
        }
    };
}
