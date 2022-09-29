package cn.rongcloud.im.utils;

import androidx.annotation.StringRes;
import cn.rongcloud.im.R;

/** @author gusd @Date 2022/07/26 */
public enum DataCenterImpl implements DataCenter {

    //    BEIJING(
    //            R.string.data_center_beijing,
    //            "beijing",
    //            "nav.cn.ronghub.com",
    //            "n19jmcy59f1q9",
    //            "http://api-sealtalk.rongcloud.cn"),
    SINGAPORE(
            R.string.data_center_singapore,
            "singapore",
            "navsg01.cn.ronghub.com",
            "8w7jv4qb8340y",
            "http://sealtalk-server-awssg.ronghub.com"),
    NORTH_AMERICA(
            R.string.data_center_north_america,
            "north_america",
            "nav-us.ronghub.com",
            "4z3hlwrv4hqwt",
            "http://sealtalk-server-us.ronghub.com");

    DataCenterImpl(
            @StringRes int resId, String code, String naviUrl, String appKey, String appServer) {
        this.code = code;
        this.resId = resId;
        this.naviUrl = naviUrl;
        this.appKey = appKey;
        this.appServer = appServer;
    }

    public String getNaviUrl() {
        return naviUrl;
    }

    @StringRes
    public int getNameId() {
        return resId;
    }

    public String getCode() {
        return code;
    }

    public String getAppKey() {
        return appKey;
    }

    public String getAppServer() {
        return appServer;
    }

    public static DataCenter valueByCode(String code) {
        for (DataCenterImpl value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    private final String code;
    @StringRes private final int resId;
    private final String naviUrl;
    private final String appKey;
    private final String appServer;
}
