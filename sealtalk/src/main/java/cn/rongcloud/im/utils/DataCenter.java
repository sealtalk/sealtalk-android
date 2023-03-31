package cn.rongcloud.im.utils;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** @author gusd @Date 2022/03/29 */
public interface DataCenter {

    Map<String, DataCenter> DATA_CENTER_MAP = new LinkedHashMap<>();

    public static DataCenter getDataCenter(String code) {
        DataCenter center = DATA_CENTER_MAP.get(code);
        if (center == null) {
            for (DataCenter value : DATA_CENTER_MAP.values()) {
                if (value.isDefault()) {
                    return value;
                }
            }
        }
        return center;
    }

    public static void addDataCenter(@NonNull DataCenter dataCenter) {
        DATA_CENTER_MAP.put(dataCenter.getCode(), dataCenter);
    }

    public static List<DataCenter> getDataCenterList() {
        return new ArrayList<>(DATA_CENTER_MAP.values());
    }

    public String getNaviUrl();

    @StringRes
    public int getNameId();

    public String getCode();

    public String getAppKey();

    public String getAppServer();

    public default boolean isDefault() {
        return false;
    }
}
