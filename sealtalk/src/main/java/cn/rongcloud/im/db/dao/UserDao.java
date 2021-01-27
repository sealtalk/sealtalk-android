package cn.rongcloud.im.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import cn.rongcloud.im.db.model.UserInfo;

@Dao
public interface UserDao {
    @Query("SELECT * FROM user WHERE id=:id")
    LiveData<UserInfo> getUserById(String id);

    @Query("SELECT * FROM user WHERE id=:id")
    UserInfo getUserByIdSync(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(UserInfo userInfo);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUserList(List<UserInfo> userInfo);

    @Query("UPDATE user SET name=:name, name_spelling=:nameSpelling, portrait_uri=:portraitUrl WHERE id=:id")
    int updateNameAndPortrait(String id, String name, String nameSpelling, String portraitUrl);

    @Query("UPDATE user SET st_account=:sAccount WHERE id=:id")
    int updateSAccount(String id, String sAccount);

    @Query("UPDATE user SET gender=:gender WHERE id=:id")
    int updateGender(String id, String gender);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertUserListIgnoreExist(List<UserInfo> userInfoList);

    @Query("UPDATE user SET name=:name,name_spelling=:nameSpelling WHERE id=:id")
    int updateName(String id, String name, String nameSpelling);

    @Query("UPDATE user SET alias=:alias,alias_spelling=:aliasSpelling,order_spelling=:aliasSpelling WHERE id=:id")
    int updateAlias(String id, String alias, String aliasSpelling);

    @Query("UPDATE user SET portrait_uri=:portraitUrl WHERE id=:id")
    int updatePortrait(String id, String portraitUrl);

    @Query("UPDATE user SET friend_status=:friendStatus WHERE id=:id")
    int updateFriendStatus(String id, int friendStatus);

    @Query("UPDATE user SET alias=:alias WHERE id=:id")
    int updateAlias(String id, String alias);

    @Query("UPDATE user SET friend_status=:friendStatus WHERE id in (:idList)")
    int updateFriendsStatus(List<String> idList, int friendStatus);

}
