package cn.rongcloud.im.model;

/**
 * Created by duanliuyi on 2017/5/12.
 */

public class CountryZipModel {

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
}
