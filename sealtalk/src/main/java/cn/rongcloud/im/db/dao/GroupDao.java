package cn.rongcloud.im.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.db.model.GroupExitedMemberInfo;
import cn.rongcloud.im.db.model.GroupMemberInfoDes;
import cn.rongcloud.im.db.model.GroupNoticeInfo;
import cn.rongcloud.im.model.RegularClearStatusResult;
import cn.rongcloud.im.ui.adapter.models.SearchGroupMember;

@Dao
public interface GroupDao {

    @Query("SELECT * from `group` where id=:groupId")
    LiveData<GroupEntity> getGroupInfo(String groupId);

    @Query("SELECT * from `group` where id=:groupId")
    GroupEntity getGroupInfoSync(String groupId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertGroup(GroupEntity group);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertGroup(List<GroupEntity> groupList);

    @Query("UPDATE `group` SET portrait_url=:portraitUri WHERE id=:groupId")
    int updateGroupPortrait(String groupId, String portraitUri);

    @Query("UPDATE `group` SET name=:name, name_spelling=:nameSpelling WHERE id=:groupId")
    int updateGroupName(String groupId, String name, String nameSpelling);

    @Query("DELETE FROM `group` WHERE id=:groupId")
    void deleteGroup(String groupId);

    @Query("SELECT `group`.id,`group`.name,`group`.portrait_url,`group`.bulletin," +
            "`group`.delete_at,`group`.max_member_count,`group`.member_count," +
            "`group`.owner_user_id,`group`.name_spelling," +
            "`group`.name_spelling_initial,`group`.order_spelling,`group`.type,`group`.bulletin_time," +
            "`group`.is_in_contact,`group`.regular_clear_state,`group`.is_mute_all,`group`.certification_status," +
            "group_member.group_id as member_id,user.name as nickname " +
            "from `group` " +
            "left join group_member " +
            "on `group`.id = group_member.group_id " +
            "left join user " +
            "on group_member.user_id = user.id  " +
            "where `group`.name like '%' || :matchSearch || '%' " +
            "OR `group`.name_spelling like '%$' || :matchSearch || '%' " +
            "OR `group`.name_spelling_initial like  '%' || :matchSearch || '%' " +
            "OR user.name like '%' || :matchSearch || '%' " +
            "OR user.name_spelling like '%$' || :matchSearch || '%' " +
            "OR user.name_spelling_initial  like '%' || :matchSearch || '%' " +
            "group by `group`.id " +
            "order by user.name_spelling ")
    LiveData<List<SearchGroupMember>> searchGroup(String matchSearch);

    @Query("SELECT * " +
            "from `group` " +
            "where name like '%' || :matchSearch || '%' " +
            "OR name_spelling like '%' || :matchSearch || '%' " +
            "OR name_spelling_initial like '%' || :matchSearch || '%' " +
            "order by name_spelling ")
    LiveData<List<GroupEntity>> searchGroupByName(String matchSearch);

    @Query("SELECT * FROM `group` WHERE id IN (:groupIds)")
    List<GroupEntity> getGroupInfoListSync(String[] groupIds);

    @Query("SELECT * FROM `group` WHERE id IN (:groupIds)")
    LiveData<List<GroupEntity>> getGroupInfoList(String[] groupIds);

    @Query("SELECT * FROM `group`")
    LiveData<List<GroupEntity>> getAllGroupInfoList();

    @Query("SELECT * FROM `group` WHERE is_in_contact=1")
    LiveData<List<GroupEntity>> getContactGroupInfoList();

    @Query("UPDATE `group` SET is_in_contact=:isInContact WHERE id=:groupId")
    int updateGroupContactState(String groupId, int isInContact);

    @Query("UPDATE `group` SET is_in_contact=0")
    int clearAllGroupContact();

    @Query("UPDATE `group` SET is_mute_all=:muteAllState WHERE id=:groupId")
    int updateMuteAllState(String groupId, int muteAllState);

    @Query("UPDATE `group` SET member_protection=:memberProtectionState WHERE id=:groupId")
    int updateMemberProtectionState(String groupId, int memberProtectionState);

    @Query("UPDATE `group` SET certification_status=:certiStatus WHERE id=:groupId")
    int updateCertiStatus(String groupId, int certiStatus);

    @Query("SELECT is_in_contact from `group` WHERE id=:groupId")
    int getGroupIsContactSync(String groupId);

    @Query("UPDATE `group` SET regular_clear_state=:regularClearState WHERE id=:groupId")
    int updateRegularClearState(String groupId, int regularClearState);

    @Query("SELECT regular_clear_state from `group` WHERE id=:groupId")
    LiveData<Integer> getRegularClear(String groupId);

    @Query("SELECT regular_clear_state from `group` WHERE id=:groupId")
    int getRegularClearSync(String groupId);

    @Query("UPDATE `group` SET bulletin=:notice, bulletin_time=:updateTime WHERE id=:groupId")
    int updateGroupNotice(String groupId, String notice, long updateTime);

    @Query("SELECT * from `group_notice`")
    LiveData<List<GroupNoticeInfo>> getGroupNoticeList();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertGroupNotice(List<GroupNoticeInfo> groupList);

    @Query("DELETE FROM group_notice WHERE id NOT in (:idList)")
    void deleteAllGroupNotice(List<String> idList);

    @Query("DELETE FROM group_notice")
    void deleteAllGroupNotice();

    @Query("DELETE FROM group_exited")
    void deleteAllGroupExited();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertGroupExited(List<GroupExitedMemberInfo> groupExitedList);

    @Query("SELECT * from `group_exited`")
    LiveData<List<GroupExitedMemberInfo>> getGroupExitedList();

    @Query("UPDATE `group_notice` SET status=:status WHERE id=:noticeId")
    int updateGroupNoticeStatus(String noticeId, int status);

    @Query("SELECT * from `group_member_info_des` WHERE groupId=:groupId And memberId=:memberId")
    LiveData<GroupMemberInfoDes> getGroupMemberInfoDes(String groupId, String memberId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertGroupMemberInfoDes(GroupMemberInfoDes infoDes);
}
