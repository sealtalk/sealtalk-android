package cn.rongcloud.im.db;

import android.content.Context;

import androidx.room.Room;

import cn.rongcloud.im.common.LogTag;
import cn.rongcloud.im.db.dao.FriendDao;
import cn.rongcloud.im.db.dao.GroupDao;
import cn.rongcloud.im.db.dao.GroupMemberDao;
import cn.rongcloud.im.db.dao.UserDao;
import cn.rongcloud.im.utils.log.SLog;
import io.rong.imlib.MD5;

/**
 * 数据库管理类
 */
public class DBManager {
    private final String DB_NAME_FORMAT = "user_%s";
    private volatile static DBManager instance;
    private Context context;
    private SealTalkDatabase database;
    private String currentUserId;

    private DBManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public static DBManager getInstance(Context context) {
        if (instance == null) {
            synchronized (DBManager.class) {
                if (instance == null) {
                    instance = new DBManager(context);
                }
            }
        }

        return instance;
    }

    /**
     * 打开指定用户数据库
     *
     * @param userId
     */
    public void openDb(String userId) {
        if (currentUserId != null) {
            if (!currentUserId.equals(userId)) {
                closeDb();
            } else {
                SLog.d(LogTag.DB, "user:" + userId + ", has opened db.");
                return;
            }
        } else {
            // do nothing
        }
        currentUserId = userId;
        String userIdMD5 = MD5.encrypt(userId);
        database = Room.databaseBuilder(context, SealTalkDatabase.class, String.format(DB_NAME_FORMAT, userIdMD5))
                .fallbackToDestructiveMigration()
                .build();
        SLog.d(LogTag.DB, "openDb,userId:" + currentUserId);
    }

    public void closeDb() {
        if (database != null) {
            SLog.d(LogTag.DB, "closeDb,userId:" + currentUserId);
            database.close();
        }
        currentUserId = "";
    }

    public UserDao getUserDao() {
        if (database == null) {
            SLog.e(LogTag.DB, "Get Dao need openDb first.");
            return null;
        }
        return database.getUserDao();
    }

    public FriendDao getFriendDao() {
        if (database == null) {
            SLog.e(LogTag.DB, "Get Dao need openDb first.");
            return null;
        }
        return database.getFriendDao();
    }

    public GroupDao getGroupDao() {
        if (database == null) {
            SLog.e(LogTag.DB, "Get Dao need openDb first.");
            return null;
        }
        return database.getGroupDao();
    }

    public GroupMemberDao getGroupMemberDao() {
        if (database == null) {
            SLog.e(LogTag.DB, "Get Dao need openDb first.");
            return null;
        }
        return database.getGroupMemberDao();
    }
}
