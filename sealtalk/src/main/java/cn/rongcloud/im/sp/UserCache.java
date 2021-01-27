package cn.rongcloud.im.sp;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;

import cn.rongcloud.im.model.UserCacheInfo;

/**
 * 缓存登录后的用户信息。 即最有一个登录用户。
 * 当用户退出时可不清理。 可用于登录时自动填充用户账号和密码。
 * 在应用登录运行过程中， 可通过缓存获取当前运行的用户信息。
 * 此类不支持多进程使用
 */
public class UserCache {
    private static final String SP_NAME = "User_cache";
    private static final String SP_CACHE_USER = "last_login_user";
    private final SharedPreferences sp;

    public UserCache(Context context) {
        sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 缓存去登录之后 User 的信息。
     * @param userCacehInfo
     */
    public void saveUserCache(UserCacheInfo userCacehInfo) {
        if (userCacehInfo == null || TextUtils.isEmpty(userCacehInfo.getId())) {
            return ;
        }
        UserCacheInfo tmpCacheInfo = getUserCache();
        if (tmpCacheInfo != null && !TextUtils.isEmpty(tmpCacheInfo.getId()) && !userCacehInfo.getId().equals(tmpCacheInfo.getId())) {
            // 另一个不同的用户
            Gson gson = new Gson();
            String userJson = gson.toJson(userCacehInfo);
            sp.edit().putString(SP_CACHE_USER, userJson).commit();
            return;
        }
        // 同一个用户或者第一个用户
        if (tmpCacheInfo != null) {
            tmpCacheInfo.setUserCacheInfo(userCacehInfo);
        } else {
            tmpCacheInfo = userCacehInfo;
        }

        Gson gson = new Gson();
        String userJson = gson.toJson(tmpCacheInfo);
        sp.edit().putString(SP_CACHE_USER, userJson).commit();
    }



    /**
     * 获取用户缓存信息
     * @return
     */
    public UserCacheInfo getUserCache() {
        try {
            String userJson = sp.getString(SP_CACHE_USER, "");
            if (TextUtils.isEmpty(userJson)) {
                return  null;
            }

            Gson gson = new Gson();
            UserCacheInfo userCacheInfo = gson.fromJson(userJson, UserCacheInfo.class);
            return userCacheInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取当前的用户ID
     * @return
     */
    public String getCurrentUserId() {
        UserCacheInfo userCache = getUserCache();
        if (userCache == null) {
            return null;
        }
        return userCache.getId();
    }

    /**
     * 退出登录所要清理的缓存
     */
    public void logoutClear() {
        UserCacheInfo userCache = getUserCache();
        if (userCache == null) {
            return ;
        }
        userCache.setLoginToken("");
        userCache.setId("");
        Gson gson = new Gson();
        String userJson = gson.toJson(userCache);
        sp.edit().putString(SP_CACHE_USER, userJson).commit();
    }
}
