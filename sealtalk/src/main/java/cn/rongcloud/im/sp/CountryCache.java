package cn.rongcloud.im.sp;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import cn.rongcloud.im.model.CountryInfo;

public class CountryCache {
    private static final String SP_NAME = "country_list_cache";
    private static final String SP_COUNTRY_LIST = "country_list";
    private final SharedPreferences sp;

    public CountryCache(Context context) {
        sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 存储国家地区消息
     * @param countryInfoList
     */
    public void saveCountryList(List<CountryInfo> countryInfoList) {
        if (countryInfoList == null) {
            return;
        }
        Gson gson = new Gson();
        String json = gson.toJson(countryInfoList);
        sp.edit().putString(SP_COUNTRY_LIST, json).commit();
    }

    /**
     * 获取国家地区信息缓存
     * @return
     */
    public List<CountryInfo> getCountryListCache() {
        try {
            String json = sp.getString(SP_COUNTRY_LIST, "");
            if (TextUtils.isEmpty(json)) {
                return null;
            }
            Gson gson = new Gson();
            List<CountryInfo> countryInfoList = gson.fromJson(json, new TypeToken<List<CountryInfo>>() {
            }.getType());
            return countryInfoList;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取国家信息
     * @param region
     * @return
     */
    public CountryInfo getCountryInfoByRegion(String region) {
        List<CountryInfo> countryListCache = getCountryListCache();
        if (countryListCache == null || countryListCache.size() <= 0) {
            return null;
        }

        for (CountryInfo info: countryListCache ) {
            if (region.equals(info.getZipCode())) {
                return info;
            }
        }
        return null;
    }
}