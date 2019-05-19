package cn.rongcloud.im.utils;

import android.content.Context;
import android.content.SharedPreferences;

import io.rong.pushperm.log.LLog;

public class PushSetAdvisorHistory {


    private static final String SP_PERM_SET = "sp_perm_set";
    private static final int SP_COUNT = 6;
    private static final int SP_TIME = 1000 * 60 * 60 * 24;

    public static boolean isCanSet(Context context, String perm) {
        SharedPreferences sp = context.getSharedPreferences(SP_PERM_SET, Context.MODE_PRIVATE);
          int count = sp.getInt(perm, 0);
          long perTime = sp.getLong(perm + "-time", 0);
          if (count <= 0 || perTime <=  0)  {
              LLog.d("sp_hsy", "count  = " + count);
              LLog.d("sp_hsy", "perTime = " + perTime);

              return true;
          }

          long tTime = SP_TIME / SP_COUNT;

          if (System.currentTimeMillis() - perTime > tTime) {
              LLog.d("sp_hsy", "tTime  = " + tTime);
              LLog.d("sp_hsy", "System.currentTimeMillis() - perTime  = " + (System.currentTimeMillis() - perTime));
              return true;
          }
          return false;
    }

    public static void saveSetHistory(Context context, String perm) {
        SharedPreferences sp = context.getSharedPreferences(SP_PERM_SET, Context.MODE_PRIVATE);
        int count = sp.getInt(perm, 0);
        count ++;
        LLog.d("sp_hsy", "save, count = " + count);
        LLog.d("sp_hsy", "save, perTime = " + System.currentTimeMillis());
        sp.edit().putInt(perm, count).putLong(perm + "-time", System.currentTimeMillis())
                .commit();
    }
}
