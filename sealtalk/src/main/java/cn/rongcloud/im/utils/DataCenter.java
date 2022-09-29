package cn.rongcloud.im.utils;

import androidx.annotation.StringRes;

/** @author gusd @Date 2022/03/29 */
public interface DataCenter {

    public String getNaviUrl();

    @StringRes
    public int getNameId();

    public String getCode();

    public String getAppKey();

    public String getAppServer();
}
