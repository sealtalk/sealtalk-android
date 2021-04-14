package cn.rongcloud.im.model;

import cn.rongcloud.im.db.model.UserInfo;

public class UserCacheInfo extends UserInfo {
    private CountryInfo countryInfo;
    private String loginToken;
    private String password;

    public UserCacheInfo(){

    }

    public UserCacheInfo(String id) {
        setId(id);
    }

    public UserCacheInfo(String id, String loginToken, String phoneNumber, String password, String region, CountryInfo info) {
            setId(id);
            setPhoneNumber(phoneNumber);
            setLoginToken(loginToken);
            setCountryInfo(info);
            setRegion(region);
            setPassword(password);
    }

    public UserCacheInfo(String id, String token, String phone, String region, CountryInfo info) {
        setId(id);
        setPhoneNumber(phone);
        setLoginToken(token);
        setCountryInfo(info);
        setRegion(region);
    }

    public void setUserInfo(UserInfo info) {
        if (getId() != null && info != null && !getId().equals(info.getId())) {
            return;
        }
        setId(info.getId());
        setPortraitUri(info.getPortraitUri());
        setName(info.getName());
        setNameSpelling(info.getNameSpelling());
        setAlias(info.getAlias());
        setAliasSpelling(info.getAliasSpelling());
        setRegion(info.getRegion());
        setPhoneNumber(info.getPhoneNumber());
        setFriendStatus(info.getFriendStatus());
        setOrderSpelling(info.getOrderSpelling());
    }

    public void setUserCacheInfo(UserCacheInfo info) {
        setId(info.getId());
        setPortraitUri(info.getPortraitUri());
        setName(info.getName());
        setNameSpelling(info.getNameSpelling());
        setAlias(info.getAlias());
        setAliasSpelling(info.getAliasSpelling());
        setRegion(info.getRegion());
        setPhoneNumber(info.getPhoneNumber());
        setFriendStatus(info.getFriendStatus());
        setOrderSpelling(info.getOrderSpelling());
        setLoginToken(info.getLoginToken());
        setCountryInfo(info.getCountryInfo());
        setPassword(info.getPassword());
    }

    public CountryInfo getCountryInfo() {
        return countryInfo;
    }

    public void setCountryInfo(CountryInfo countryInfo) {
        this.countryInfo = countryInfo;
    }

    public String getLoginToken() {
        return loginToken;
    }

    public void setLoginToken(String loginToken) {
        this.loginToken = loginToken;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


}
