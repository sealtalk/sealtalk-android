package cn.rongcloud.im.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import cn.rongcloud.im.db.dao.FriendDao;
import cn.rongcloud.im.db.dao.GroupDao;
import cn.rongcloud.im.db.dao.GroupMemberDao;
import cn.rongcloud.im.db.dao.UserDao;
import cn.rongcloud.im.db.model.BlackListEntity;
import cn.rongcloud.im.db.model.FriendInfo;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.db.model.GroupMemberInfoEntity;
import cn.rongcloud.im.db.model.UserInfo;

@Database(entities = {UserInfo.class, FriendInfo.class, GroupEntity.class, GroupMemberInfoEntity.class, BlackListEntity.class}, version = 1, exportSchema = false)
@TypeConverters(cn.rongcloud.im.db.TypeConverters.class)
public abstract class SealTalkDatabase extends RoomDatabase {
    public abstract UserDao getUserDao();

    public abstract FriendDao getFriendDao();

    public abstract GroupDao getGroupDao();

    public abstract GroupMemberDao getGroupMemberDao();
}
