package cn.rongcloud.im.sp;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import io.rong.imlib.RongIMClient;

public class UltraGroupCache {

    private static final String SP_NAME = "ultra";

    private static SharedPreferences get(Context context) {
        return context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    public static void clear(Context context) {
        get(context).edit().clear().commit();
    }

    public static String getName(Context context) {
        return get(context).getString("name", "");
    }

    public static String getCreatorId(Context context) {
        return get(context).getString("creatorId", "");
    }

    /** 是否是群主 */
    public static boolean isGroupOwner(Context context) {
        String creatorId = getCreatorId(context);
        return !TextUtils.isEmpty(creatorId)
                && creatorId.equals(RongIMClient.getInstance().getCurrentUserId());
    }
}
