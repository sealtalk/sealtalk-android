package cn.rongcloud.im.db;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import java.io.File;

import cn.rongcloud.im.SealConst;
import io.rong.common.RLog;
import io.rong.imkit.userInfoCache.RongDatabaseContext;

import static android.content.Context.MODE_PRIVATE;


/**
 * [数据库管理类，数据采用GreenDao来实现，所有实现通过模板自动生成；通过获取daoSession来获取所有的dao，从而实现操作对象]
 *
 * @author devin.hu
 * @version 1.0
 * @date 2013-9-17
 *
 **/
public class DBManager {

    private final static String TAG = "DBManager";
    private final static String DB_NAME = "SealUserInfo";
    private static DBManager instance;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private static Context mContext;
    private static boolean isInitialized;

    /**
     * [获取DBManager实例，单例模式实现]
     *
     * @return DBManager
     */
    public static DBManager getInstance() {
        return instance;
    }

    /**
     * [初始化DBManager实例，单例模式实现]
     *
     * @param context
     * @return DBManager
     */
    public static DBManager init(Context context) {
        if (isInitialized) {
            RLog.d(TAG, "DBManager has init");
            return instance;
        }
        RLog.d(TAG, "DBManager init");
        mContext = context;
        instance = new DBManager(context);
        isInitialized = true;
        return instance;
    }

    public boolean isInitialized() {
        return  isInitialized;
    }

    /**
     * 构造方法
     * @param context
     */
    private DBManager(Context context) {
        DaoMaster.OpenHelper helper = new
        DaoMaster.DevOpenHelper(new RongDatabaseContext(context, getDbPath()), DB_NAME, null);
        daoMaster = new DaoMaster(helper.getWritableDatabase());
        daoSession = daoMaster.newSession();
        //数据库存贮路径修改,直接删除旧的数据库
        mContext.deleteDatabase(mContext.getPackageName());
    }

    public DaoMaster getDaoMaster() {
        return daoMaster;
    }

    public void setDaoMaster(DaoMaster daoMaster) {
        this.daoMaster = daoMaster;
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }

    public void setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
    }

    private static String getAppKey() {
        String appKey = null;
        try {
            ApplicationInfo applicationInfo = mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(), PackageManager.GET_META_DATA);
            if (applicationInfo != null) {
                appKey = applicationInfo.metaData.getString("RONG_CLOUD_APP_KEY");
            }
            if (TextUtils.isEmpty(appKey)) {
                throw new IllegalArgumentException("can't find RONG_CLOUD_APP_KEY in AndroidManifest.xml.");
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            throw new ExceptionInInitializerError("can't find packageName!");
        }
        return appKey;
    }

    private static String getDbPath () {
        String currentUserId = mContext.getSharedPreferences("config", MODE_PRIVATE).getString(SealConst.SEALTALK_LOGIN_ID, null);
        String dbPath = mContext.getFilesDir().getAbsolutePath();
        dbPath = dbPath + File.separator + getAppKey() + File.separator + currentUserId;
        RLog.d(TAG, "DBManager dbPath = " + dbPath);
        return  dbPath;
    }

    public void uninit() {
        RLog.d(TAG, "DBManager uninit");
        if (daoSession != null && daoSession.getDatabase() != null) {
            daoSession.getDatabase().close();
        }
        daoSession = null;
        daoMaster = null;
        isInitialized = false;
    }
}
