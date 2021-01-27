package cn.rongcloud.im.task;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.rongcloud.im.db.DBManager;
import cn.rongcloud.im.db.dao.GroupDao;
import cn.rongcloud.im.db.dao.GroupMemberDao;
import cn.rongcloud.im.db.dao.UserDao;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.db.model.GroupExitedMemberInfo;
import cn.rongcloud.im.db.model.GroupMemberInfoDes;
import cn.rongcloud.im.db.model.GroupMemberInfoEntity;
import cn.rongcloud.im.db.model.GroupNoticeInfo;
import cn.rongcloud.im.db.model.UserInfo;
import cn.rongcloud.im.file.FileManager;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.model.AddMemberResult;
import cn.rongcloud.im.model.CopyGroupResult;
import cn.rongcloud.im.model.GroupMember;
import cn.rongcloud.im.model.GroupMemberInfoResult;
import cn.rongcloud.im.model.GroupNoticeInfoResult;
import cn.rongcloud.im.model.GroupNoticeResult;
import cn.rongcloud.im.model.GroupResult;
import cn.rongcloud.im.model.RegularClearStatusResult;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Result;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.model.UserSimpleInfo;
import cn.rongcloud.im.net.HttpClientManager;
import cn.rongcloud.im.net.RetrofitUtil;
import cn.rongcloud.im.net.service.GroupService;
import cn.rongcloud.im.ui.adapter.models.SearchGroupMember;
import cn.rongcloud.im.utils.NetworkBoundResource;
import cn.rongcloud.im.utils.NetworkOnlyResource;
import cn.rongcloud.im.utils.RongGenerate;
import cn.rongcloud.im.utils.SearchUtils;
import cn.rongcloud.im.utils.SingleSourceLiveData;
import io.rong.imkit.utils.CharacterParser;
import io.rong.imlib.model.Conversation;
import okhttp3.RequestBody;

public class GroupTask {
    private GroupService groupService;
    private Context context;
    private DBManager dbManager;
    private FileManager fileManager;

    public GroupTask(Context context) {
        this.context = context.getApplicationContext();
        groupService = HttpClientManager.getInstance(context).getClient().createService(GroupService.class);
        dbManager = DBManager.getInstance(context);
        fileManager = new FileManager(context);
    }

    /**
     * 创建群组
     *
     * @param groupName
     * @param memberList
     * @return
     */
    public LiveData<Resource<GroupResult>> createGroup(String groupName, List<String> memberList) {
        return new NetworkOnlyResource<GroupResult, Result<GroupResult>>() {
            @NonNull
            @Override
            protected LiveData<Result<GroupResult>> createCall() {
                HashMap<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("name", groupName);
                bodyMap.put("memberIds", memberList);
                return groupService.createGroup(RetrofitUtil.createJsonRequest(bodyMap));
            }
        }.asLiveData();
    }

    /**
     * 添加群成员
     *
     * @param groupId
     * @param memberList
     * @return
     */
    public LiveData<Resource<List<AddMemberResult>>> addGroupMember(String groupId, List<String> memberList) {
        return new NetworkOnlyResource<List<AddMemberResult>, Result<List<AddMemberResult>>>() {
            @NonNull
            @Override
            protected LiveData<Result<List<AddMemberResult>>> createCall() {
                HashMap<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("groupId", groupId);
                bodyMap.put("memberIds", memberList);
                return groupService.addGroupMember(RetrofitUtil.createJsonRequest(bodyMap));
            }
        }.asLiveData();
    }

    /**
     * 加入群组
     *
     * @param groupId
     * @return
     */
    public LiveData<Resource<Void>> joinGroup(String groupId) {
        return new NetworkOnlyResource<Void, Result>() {
            @NonNull
            @Override
            protected LiveData<Result> createCall() {
                HashMap<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("groupId", groupId);
                return groupService.joinGroup(RetrofitUtil.createJsonRequest(bodyMap));
            }
        }.asLiveData();
    }

    /**
     * 群主或群管理将群成员移出群组
     *
     * @param groupId
     * @param memberList
     * @return
     */
    public LiveData<Resource<Void>> kickGroupMember(String groupId, List<String> memberList) {
        return new NetworkOnlyResource<Void, Result>() {
            @Override
            protected void saveCallResult(@NonNull Void item) {
                GroupMemberDao groupMemberDao = dbManager.getGroupMemberDao();
                if (groupMemberDao != null) {
                    groupMemberDao.deleteGroupMember(groupId, memberList);
                }
            }

            @NonNull
            @Override
            protected LiveData<Result> createCall() {
                HashMap<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("groupId", groupId);
                bodyMap.put("memberIds", memberList);
                return groupService.kickMember(RetrofitUtil.createJsonRequest(bodyMap));
            }
        }.asLiveData();
    }

    /**
     * 退出群组
     *
     * @param groupId
     * @return
     */
    public LiveData<Resource<Void>> quitGroup(String groupId) {
        return new NetworkOnlyResource<Void, Result>() {
            @Override
            protected void saveCallResult(@NonNull Void item) {
                GroupDao groupDao = dbManager.getGroupDao();
                if (groupDao != null) {
                    groupDao.deleteGroup(groupId);
                }

                GroupMemberDao groupMemberDao = dbManager.getGroupMemberDao();
                if (groupMemberDao != null) {
                    groupMemberDao.deleteGroupMember(groupId);
                }

                IMManager.getInstance().clearConversationAndMessage(groupId, Conversation.ConversationType.GROUP);
            }

            @NonNull
            @Override
            protected LiveData<Result> createCall() {
                HashMap<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("groupId", groupId);
                return groupService.quitGroup(RetrofitUtil.createJsonRequest(bodyMap));
            }
        }.asLiveData();
    }

    /**
     * 解散群组
     *
     * @param groupId
     * @return
     */
    public LiveData<Resource<Void>> dismissGroup(String groupId) {
        return new NetworkOnlyResource<Void, Result>() {
            @Override
            protected void saveCallResult(@NonNull Void item) {
                GroupDao groupDao = dbManager.getGroupDao();
                if (groupDao != null) {
                    groupDao.deleteGroup(groupId);
                }

                GroupMemberDao groupMemberDao = dbManager.getGroupMemberDao();
                if (groupMemberDao != null) {
                    groupMemberDao.deleteGroupMember(groupId);
                }

                IMManager.getInstance().clearConversationAndMessage(groupId, Conversation.ConversationType.GROUP);
            }

            @NonNull
            @Override
            protected LiveData<Result> createCall() {
                HashMap<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("groupId", groupId);
                return groupService.dismissGroup(RetrofitUtil.createJsonRequest(bodyMap));
            }
        }.asLiveData();
    }

    /**
     * 转移群主
     *
     * @param groupId
     * @param userId
     * @return
     */
    public LiveData<Resource<Void>> transferGroup(String groupId, String userId) {
        return new NetworkOnlyResource<Void, Result>() {
            @NonNull
            @Override
            protected LiveData<Result> createCall() {
                HashMap<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("groupId", groupId);
                bodyMap.put("userId", userId);
                return groupService.transferGroup(RetrofitUtil.createJsonRequest(bodyMap));
            }

        }.asLiveData();
    }

    /**
     * 重命名群名称
     *
     * @param groupId
     * @param groupName
     * @return
     */
    public LiveData<Resource<Void>> renameGroup(String groupId, String groupName) {
        return new NetworkOnlyResource<Void, Result>() {
            @Override
            protected void saveCallResult(@NonNull Void item) {
                // 更新数据库中群组的名称
                GroupDao groupDao = dbManager.getGroupDao();
                if (groupDao != null) {
                    int updateResult;
                    updateResult = groupDao.updateGroupName(groupId, groupName, CharacterParser.getInstance().getSelling(groupName));

                    // 更新成时同时更新缓存
                    if (updateResult > 0) {
                        GroupEntity groupInfo = groupDao.getGroupInfoSync(groupId);
                        if (groupInfo != null) {
                            IMManager.getInstance().updateGroupInfoCache(groupId, groupName, Uri.parse(groupInfo.getPortraitUri()));
                        }
                    }
                }

            }

            @NonNull
            @Override
            protected LiveData<Result> createCall() {
                HashMap<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("groupId", groupId);
                bodyMap.put("name", groupName);
                return groupService.renameGroup(RetrofitUtil.createJsonRequest(bodyMap));
            }
        }.asLiveData();
    }

    /**
     * 获取定时清理状态信息
     *
     * @param groupId
     * @return
     */
    public LiveData<Resource<Integer>> getRegularClearState(String groupId) {
        return new NetworkBoundResource<Integer, Result<RegularClearStatusResult>>() {

            @Override
            protected void saveCallResult(@NonNull Result<RegularClearStatusResult> item) {
                if (item.code == 200 && item.result != null) {
                    updateGroupRegularClearStateInDB(groupId, item.result.clearStatus);
                }
            }

            @NonNull
            @Override
            protected LiveData<Integer> loadFromDb() {
                GroupDao groupDao = dbManager.getGroupDao();
                LiveData<Integer> regularClearState = null;
                if (groupDao != null) {
                    regularClearState = groupDao.getRegularClear(groupId);
                }
                return regularClearState;
            }

            @NonNull
            @Override
            protected LiveData<Result<RegularClearStatusResult>> createCall() {
                HashMap<String, Object> paramMap = new HashMap<>();
                paramMap.put("groupId", groupId);
                return groupService.getRegularClearState(RetrofitUtil.createJsonRequest(paramMap));
            }
        }.asLiveData();
    }

    /**
     * 设置定时清理群消息
     *
     * @param groupId
     * @param clearStatus
     * @return
     */
    public LiveData<Resource<Void>> setRegularClear(String groupId, int clearStatus) {
        return new NetworkOnlyResource<Void, Result>() {
            @NonNull
            @Override
            protected LiveData<Result> createCall() {
                HashMap<String, Object> paramMap = new HashMap<>();
                paramMap.put("groupId", groupId);
                paramMap.put("clearStatus", clearStatus);
                return groupService.setRegularClear(RetrofitUtil.createJsonRequest(paramMap));
            }

            @Override
            protected void saveCallResult(@NonNull Void item) {
                updateGroupRegularClearStateInDB(groupId, clearStatus);
            }
        }.asLiveData();
    }

    private void updateGroupRegularClearStateInDB(String groupId, int state) {
        GroupDao groupDao = dbManager.getGroupDao();
        if (groupDao == null) return;

        groupDao.updateRegularClearState(groupId, state);
    }

    /**
     * 设置群公告
     *
     * @param groupId
     * @param bulletin
     * @return
     */
    public LiveData<Resource<Void>> setGroupNotice(String groupId, String bulletin) {
        return new NetworkOnlyResource<Void, Result>() {
            @NonNull
            @Override
            protected LiveData<Result> createCall() {
                HashMap<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("groupId", groupId);
                bodyMap.put("bulletin", bulletin);
                return groupService.setGroupBulletin(RetrofitUtil.createJsonRequest(bodyMap));
            }
        }.asLiveData();
    }

    /**
     * 获取群公告
     *
     * @param groupId
     * @return
     */
    public LiveData<Resource<GroupNoticeResult>> getGroupNotice(String groupId) {
        return new NetworkOnlyResource<GroupNoticeResult, Result<GroupNoticeResult>>() {
            @Override
            protected void saveCallResult(@NonNull GroupNoticeResult result) {
                GroupDao groupDao = dbManager.getGroupDao();
                if (groupDao != null) {
                    groupDao.updateGroupNotice(groupId, result.getContent(), result.getTimestamp());
                }
            }

            @NonNull
            @Override
            protected LiveData<Result<GroupNoticeResult>> createCall() {
                return groupService.getGroupBulletin(groupId);
            }
        }.asLiveData();
    }

    /**
     * 上传并设置群组头像
     *
     * @param groupId
     * @param portraitUrl
     * @return
     */
    public LiveData<Resource<Void>> uploadAndSetGroupPortrait(String groupId, Uri portraitUrl) {
        MediatorLiveData<Resource<Void>> result = new MediatorLiveData<>();
        // 先上传图片文件
        LiveData<Resource<String>> uploadResource = fileManager.uploadImage(portraitUrl);
        result.addSource(uploadResource, resource -> {
            if (resource.status != Status.LOADING) {
                result.removeSource(uploadResource);
            }

            if (resource.status == Status.ERROR) {
                result.setValue(Resource.error(resource.code, null));
                return;
            }

            if (resource.status == Status.SUCCESS) {
                String uploadUrl = resource.data;

                // 获取上传成功的地址后更新地址
                LiveData<Resource<Void>> setPortraitResource = setGroupPortrait(groupId, uploadUrl);
                result.addSource(setPortraitResource, portraitResultResource -> {
                    if (portraitResultResource.status != Status.LOADING) {
                        result.removeSource(setPortraitResource);
                    }

                    if (portraitResultResource.status == Status.ERROR) {
                        result.setValue(Resource.error(portraitResultResource.code, null));
                        return;
                    }

                    if (portraitResultResource.status == Status.SUCCESS) {
                        result.setValue(Resource.success(null));
                    }
                });
            }
        });

        return result;
    }

    /**
     * 设置群组头像
     *
     * @param groupId
     * @param portraitUrl 云存储空间的 url
     * @return
     */
    private LiveData<Resource<Void>> setGroupPortrait(String groupId, String portraitUrl) {
        return new NetworkOnlyResource<Void, Result>() {
            @Override
            protected void saveCallResult(@NonNull Void item) {
                // 更新数据库中群组的头像
                GroupDao groupDao = dbManager.getGroupDao();
                if (groupDao != null) {
                    int updateResult;
                    updateResult = groupDao.updateGroupPortrait(groupId, portraitUrl);

                    // 更新成时同时更新缓存
                    if (updateResult > 0) {
                        GroupEntity groupInfo = groupDao.getGroupInfoSync(groupId);
                        IMManager.getInstance().updateGroupInfoCache(groupId, groupInfo.getName(), Uri.parse(portraitUrl));
                    }
                }
            }

            @NonNull
            @Override
            protected LiveData<Result> createCall() {
                HashMap<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("groupId", groupId);
                bodyMap.put("portraitUri", portraitUrl);
                return groupService.setGroupPortraitUri(RetrofitUtil.createJsonRequest(bodyMap));
            }
        }.asLiveData();
    }

    /**
     * 设置群内昵称
     *
     * @param groupId
     * @param displayName
     * @return
     */
    public LiveData<Resource<Void>> setMemberDisplayName(String groupId, String displayName) {
        return new NetworkOnlyResource<Void, Result>() {
            @NonNull
            @Override
            protected LiveData<Result> createCall() {
                HashMap<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("groupId", groupId);
                bodyMap.put("displayName", displayName);
                return groupService.setMemberDisplayName(RetrofitUtil.createJsonRequest(bodyMap));
            }
        }.asLiveData();
    }

    /**
     * 获取群组信息
     *
     * @param groupId
     * @return
     */
    public LiveData<Resource<GroupEntity>> getGroupInfo(final String groupId) {
        return new NetworkBoundResource<GroupEntity, Result<GroupEntity>>() {
            @Override
            protected void saveCallResult(@NonNull Result<GroupEntity> item) {
                if (item.getResult() == null) return;

                GroupEntity groupEntity = item.getResult();
                GroupDao groupDao = dbManager.getGroupDao();
                if (groupDao != null) {
                    // 判断是否在通讯录中
                    int groupIsContact = groupDao.getGroupIsContactSync(groupId);
                    int regularClearState = groupDao.getRegularClearSync(groupId);

                    String portraitUri = groupEntity.getPortraitUri();
                    if (TextUtils.isEmpty(portraitUri)) {
                        portraitUri = RongGenerate.generateDefaultAvatar(context, groupEntity.getId(), groupEntity.getName());
                        groupEntity.setPortraitUri(portraitUri);
                    }
                    groupEntity.setNameSpelling(SearchUtils.fullSearchableString(groupEntity.getName()));
                    groupEntity.setNameSpellingInitial(SearchUtils.initialSearchableString(groupEntity.getName()));
                    groupEntity.setOrderSpelling(CharacterParser.getInstance().getSelling(groupEntity.getName()));
                    groupEntity.setIsInContact(groupIsContact);
                    groupEntity.setRegularClearState(regularClearState);
                    groupDao.insertGroup(groupEntity);
                }

                // 更新 IMKit 缓存群组数据
                IMManager.getInstance().updateGroupInfoCache(groupEntity.getId(), groupEntity.getName(), Uri.parse(groupEntity.getPortraitUri()));
            }

            @NonNull
            @Override
            protected LiveData<GroupEntity> loadFromDb() {
                GroupDao groupDao = dbManager.getGroupDao();
                LiveData<GroupEntity> groupInfo = null;
                if (groupDao != null) {
                    groupInfo = groupDao.getGroupInfo(groupId);
                } else {
                    groupInfo = new MutableLiveData<>(null);
                }
                return groupInfo;
            }

            @NonNull
            @Override
            protected LiveData<Result<GroupEntity>> createCall() {
                return groupService.getGroupInfo(groupId);
            }
        }.asLiveData();
    }

    /**
     * 获取群组信息 ( 同步方法 )
     *
     * @param groupId
     * @return
     */
    public GroupEntity getGroupInfoSync(final String groupId) {
        return dbManager.getGroupDao().getGroupInfoSync(groupId);
    }

    /**
     * 获取群组 list 信息 ( 同步方法 )
     *
     * @param groupIds
     * @return
     */
    public List<GroupEntity> getGroupInfoListSync(String[] groupIds) {
        return dbManager.getGroupDao().getGroupInfoListSync(groupIds);
    }

    /**
     * 获取群组 list 信息 ( 异步 )
     *
     * @param groupIds
     * @return
     */
    public LiveData<List<GroupEntity>> getGroupInfoList(String[] groupIds) {
        GroupDao groupDao = dbManager.getGroupDao();
        LiveData<List<GroupEntity>> groupInfoList = null;
        if (groupDao != null) {
            groupInfoList = groupDao.getGroupInfoList(groupIds);
        } else {
            groupInfoList = new MutableLiveData<>(null);
        }
        return groupInfoList;
    }

    public LiveData<GroupEntity> getGroupInfoInDB(String groupIds) {
        GroupDao groupDao = dbManager.getGroupDao();
        LiveData<GroupEntity> groupInfo = null;
        if (groupDao != null) {
            groupInfo = groupDao.getGroupInfo(groupIds);
        } else {
            groupInfo = new MutableLiveData<>(null);
        }
        return groupInfo;
    }

    /**
     * 获取群成员列表,通过成员名称筛选
     *
     * @param groupId
     * @param filterByName 通过姓名模糊匹配
     * @return
     */
    public LiveData<Resource<List<GroupMember>>> getGroupMemberInfoList(final String groupId, String filterByName) {
        return new NetworkBoundResource<List<GroupMember>, Result<List<GroupMemberInfoResult>>>() {
            @Override
            protected void saveCallResult(@NonNull Result<List<GroupMemberInfoResult>> item) {
                if (item.getResult() == null) return;

                GroupMemberDao groupMemberDao = dbManager.getGroupMemberDao();
                UserDao userDao = dbManager.getUserDao();

                // 获取新数据前清除掉原成员信息
                if (groupMemberDao != null) {
                    groupMemberDao.deleteGroupMember(groupId);
                }

                List<GroupMemberInfoResult> result = item.getResult();
                List<GroupMemberInfoEntity> groupEntityList = new ArrayList<>();
                List<UserInfo> newUserList = new ArrayList<>();
                for (GroupMemberInfoResult info : result) {
                    UserSimpleInfo user = info.getUser();
                    GroupMemberInfoEntity groupEntity = new GroupMemberInfoEntity();
                    groupEntity.setGroupId(groupId);

                    // 默认优先显示群备注名。当没有群备注时，则看此用户为当前用户的好友，如果是好友则显示备注名称。其次再试显示用户名
                    String displayName = TextUtils.isEmpty(info.getDisplayName()) ? "" : info.getDisplayName();
                    String nameInKitCache = displayName;

                    if (TextUtils.isEmpty(nameInKitCache)) {
                        nameInKitCache = user.getName();
                    }

                    groupEntity.setNickName(displayName);
                    groupEntity.setNickNameSpelling(SearchUtils.fullSearchableString(displayName));
                    groupEntity.setUserId(user.getId());
                    groupEntity.setRole(info.getRole());
                    groupEntity.setCreateTime(info.getCreatedTime());
                    groupEntity.setUpdateTime(info.getUpdatedTime());
                    groupEntity.setJoinTime(info.getTimestamp());
                    groupEntityList.add(groupEntity);

                    // 更新 IMKit 缓存群组成员数据
                    IMManager.getInstance().updateGroupMemberInfoCache(groupId, user.getId(), nameInKitCache);

                    if (userDao != null) {
                        // 更新已存在的用户信息
                        String portraitUri = user.getPortraitUri();

                        // 当没有头像时生成默认头像
                        if (TextUtils.isEmpty(portraitUri)) {
                            portraitUri = RongGenerate.generateDefaultAvatar(context, user.getId(), user.getName());
                            user.setPortraitUri(portraitUri);
                        }
                        int updateResult = userDao.updateNameAndPortrait(user.getId(), user.getName(), io.rong.imkit.utils.CharacterParser.getInstance().getSelling(user.getName()), user.getPortraitUri());

                        // 当没有更新成功时，添加到新用户列表中
                        if (updateResult == 0) {
                            UserInfo userInfo = new UserInfo();
                            userInfo.setId(user.getId());
                            userInfo.setName(user.getName());
                            userInfo.setNameSpelling(SearchUtils.fullSearchableString(user.getName()));
                            userInfo.setPortraitUri(user.getPortraitUri());
                            newUserList.add(userInfo);
                        }
                    }
                }

                // 更新群组成员
                if (groupMemberDao != null) {
                    groupMemberDao.insertGroupMemberList(groupEntityList);
                }

                if (userDao != null) {
                    // 插入新的用户信息
                    userDao.insertUserListIgnoreExist(newUserList);
                }

            }

            @Override
            protected boolean shouldFetch(@Nullable List<GroupMember> data) {
                boolean shouldFetch = true;
                // 当数据库中有群成员数据时，当用姓名进行筛选时不进行网络请求
                if (data != null && data.size() > 0 && !TextUtils.isEmpty(filterByName)) {
                    shouldFetch = false;
                }
                return shouldFetch;
            }

            @NonNull
            @Override
            protected LiveData<List<GroupMember>> loadFromDb() {
                GroupMemberDao groupMemberDao = dbManager.getGroupMemberDao();
                if (groupMemberDao != null) {
                    if (TextUtils.isEmpty(filterByName)) {
                        return groupMemberDao.getGroupMemberList(groupId);
                    } else {
                        return groupMemberDao.getGroupMemberList(groupId, filterByName);
                    }
                }
                return new MutableLiveData<>(null);
            }

            @NonNull
            @Override
            protected LiveData<Result<List<GroupMemberInfoResult>>> createCall() {
                return groupService.getGroupMemberList(groupId);
            }
        }.asLiveData();
    }

    /**
     * 获取群成员列表
     *
     * @param groupId
     * @return
     */
    public LiveData<Resource<List<GroupMember>>> getGroupMemberInfoList(final String groupId) {
        return getGroupMemberInfoList(groupId, null);
    }

    public LiveData<List<GroupMember>> getGroupMemberInfoListInDB(final String groupId) {
        GroupMemberDao groupMemberDao = dbManager.getGroupMemberDao();
        if (groupMemberDao != null) {
            return groupMemberDao.getGroupMemberList(groupId);
        }
        return null;
    }

    public LiveData<List<GroupMember>> searchGroupMemberInDB(final String groupId, String searchKey) {
        GroupMemberDao groupMemberDao = dbManager.getGroupMemberDao();
        if (groupMemberDao != null) {
            return groupMemberDao.searchGroupMember(groupId, searchKey);
        }
        return null;
    }

    public LiveData<List<SearchGroupMember>> searchGroup(String match) {
        return dbManager.getGroupDao().searchGroup(match);
    }

    public LiveData<List<GroupEntity>> searchGroupByName(String match) {
        return dbManager.getGroupDao().searchGroupByName(match);
    }

    /**
     * 删除管理员
     *
     * @param groupId
     * @param memberIds
     * @return
     */
    public LiveData<Resource<Void>> removeManager(String groupId, String[] memberIds) {
        return new NetworkOnlyResource<Void, Result>() {
            @NonNull
            @Override
            protected LiveData createCall() {
                HashMap<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("groupId", groupId);
                bodyMap.put("memberIds", memberIds);
                RequestBody body = RetrofitUtil.createJsonRequest(bodyMap);
                return groupService.removeManager(body);
            }
        }.asLiveData();
    }

    /**
     * 设置禁言
     *
     * @param groupId
     * @param muteStatus 1开启禁言，0关闭禁言
     * @param userId     可发言用户id，不设置除了群主和管理员，全员禁言
     * @return
     */
    public LiveData<Resource<Void>> setMuteAll(String groupId, int muteStatus, String userId) {
        return new NetworkOnlyResource<Void, Result>() {
            @NonNull
            @Override
            protected LiveData<Result> createCall() {
                HashMap<String, Object> paramMap = new HashMap<>();
                paramMap.put("groupId", groupId);
                paramMap.put("muteStatus", muteStatus);
                if (!TextUtils.isEmpty(userId)) {
                    paramMap.put("userId", userId);
                }
                return groupService.muteAll(RetrofitUtil.createJsonRequest(paramMap));
            }

            @Override
            protected void saveCallResult(@NonNull Void item) {
                GroupDao groupDao = dbManager.getGroupDao();
                if (groupDao != null) {
                    groupDao.updateMuteAllState(groupId, muteStatus);
                }
            }
        }.asLiveData();
    }

    /**
     * 设置群成员保护
     *
     * @param groupId
     * @param memberProtection 成员保护模式: 0 关闭、1 开启
     * @return
     */
    public LiveData<Resource<Void>> setMemberProtection(String groupId, int memberProtection) {
        return new NetworkOnlyResource<Void, Result>() {
            @NonNull
            @Override
            protected LiveData<Result> createCall() {
                HashMap<String, Object> paramMap = new HashMap<>();
                paramMap.put("groupId", groupId);
                paramMap.put("memberProtection", memberProtection);
                return groupService.setGroupProtection(RetrofitUtil.createJsonRequest(paramMap));
            }

            @Override
            protected void saveCallResult(@NonNull Void item) {
                GroupDao groupDao = dbManager.getGroupDao();
                if (groupDao != null) {
                    groupDao.updateMemberProtectionState(groupId, memberProtection);
                }
            }
        }.asLiveData();
    }

    /**
     * 入群认证
     *
     * @param groupId
     * @param certiStatus 认证状态： 0 开启(需要认证)、1 关闭（不需要认证）
     * @return
     */
    public LiveData<Resource<Void>> setCertification(String groupId, int certiStatus) {
        return new NetworkOnlyResource<Void, Result<Void>>() {

            @NonNull
            @Override
            protected LiveData<Result<Void>> createCall() {
                HashMap<String, Object> paramMap = new HashMap<>();
                paramMap.put("groupId", groupId);
                paramMap.put("certiStatus", certiStatus);
                return groupService.setCertification(RetrofitUtil.createJsonRequest(paramMap));
            }

            @Override
            protected void saveCallResult(@NonNull Void item) {
                GroupDao groupDao = dbManager.getGroupDao();
                if (groupDao != null) {
                    groupDao.updateCertiStatus(groupId, certiStatus);
                }
                super.saveCallResult(item);
            }
        }.asLiveData();
    }

    /**
     * 添加管理员
     *
     * @param groupId
     * @param membersIds
     * @return
     */
    public LiveData<Resource<Void>> addManager(String groupId, String[] membersIds) {
        return new NetworkOnlyResource<Void, Result>() {
            @NonNull
            @Override
            protected LiveData createCall() {
                HashMap<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("groupId", groupId);
                bodyMap.put("memberIds", membersIds);
                RequestBody body = RetrofitUtil.createJsonRequest(bodyMap);
                return groupService.addManager(body);
            }
        }.asLiveData();
    }

    /**
     * 获取所有群信息
     *
     * @return
     */
    public LiveData<List<GroupEntity>> getAllGroupInfoList() {
        return dbManager.getGroupDao().getAllGroupInfoList();
    }

    public LiveData<GroupEntity> getGroupInfoFromDB(String groupId) {
        return dbManager.getGroupDao().getGroupInfo(groupId);
    }

    /**
     * 群组保存到通讯录
     *
     * @return
     */
    public LiveData<Resource<Void>> saveGroupToContact(String groupId) {
        return new NetworkOnlyResource<Void, Result>() {
            @Override
            protected void saveCallResult(@NonNull Void item) {
                updateGroupContactStateInDB(groupId, true);
            }

            @NonNull
            @Override
            protected LiveData<Result> createCall() {
                HashMap<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("groupId", groupId);
                return groupService.saveToContact(RetrofitUtil.createJsonRequest(bodyMap));
            }
        }.asLiveData();
    }

    /**
     * 群组从通讯录中移除
     *
     * @return
     */
    public LiveData<Resource<Void>> removeGroupFromContact(String groupId) {
        return new NetworkOnlyResource<Void, Result>() {
            @Override
            protected void saveCallResult(@NonNull Void item) {
                updateGroupContactStateInDB(groupId, false);
            }

            @NonNull
            @Override
            protected LiveData<Result> createCall() {
                HashMap<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("groupId", groupId);
                return groupService.removeFromContact(RetrofitUtil.createJsonRequest(bodyMap));
            }
        }.asLiveData();
    }

    /**
     * 更新群组在通讯录状态
     *
     * @param isToContact
     */
    private void updateGroupContactStateInDB(String groupId, boolean isToContact) {
        GroupDao groupDao = dbManager.getGroupDao();
        if (groupDao == null) return;

        groupDao.updateGroupContactState(groupId, isToContact ? 1 : 0);
    }

    /**
     * 获取群通知消息详情
     *
     * @return
     */

    public LiveData<Resource<List<GroupNoticeInfo>>> getGroupNoticeInfo() {
        return new NetworkBoundResource<List<GroupNoticeInfo>, Result<List<GroupNoticeInfoResult>>>() {

            @Override
            protected void saveCallResult(@NonNull Result<List<GroupNoticeInfoResult>> item) {
                if (item.getResult() == null) return;

                GroupDao groupDao = dbManager.getGroupDao();

                List<GroupNoticeInfoResult> resultList = item.getResult();
                List<GroupNoticeInfo> infoList = new ArrayList<>();
                if (resultList != null && resultList.size() > 0) {
                    List<String> idList = new ArrayList<>();
                    for (GroupNoticeInfoResult infoResult : resultList) {
                        GroupNoticeInfo noticeInfo = new GroupNoticeInfo();
                        noticeInfo.setId(infoResult.id);
                        idList.add(infoResult.id);
                        noticeInfo.setCreatedAt(infoResult.createdAt);
                        noticeInfo.setCreatedTime(infoResult.timestamp);
                        noticeInfo.setType(infoResult.type);
                        noticeInfo.setStatus(infoResult.status);
                        if (infoResult.receiver != null) {
                            noticeInfo.setReceiverId(infoResult.receiver.id);
                            noticeInfo.setReceiverNickName(infoResult.receiver.nickname);
                        }
                        if (infoResult.requester != null) {
                            noticeInfo.setRequesterId(infoResult.requester.id);
                            noticeInfo.setRequesterNickName(infoResult.requester.nickname);
                        }
                        if (infoResult.group != null) {
                            noticeInfo.setGroupId(infoResult.group.id);
                            noticeInfo.setGroupNickName(infoResult.group.name);
                        }
                        infoList.add(noticeInfo);
                    }
                    //防止直接 delteAll 导致的返回数据 success 状态导致返回错误的空数据结果
                    groupDao.deleteAllGroupNotice(idList);
                    groupDao.insertGroupNotice(infoList);
                } else if (resultList != null) {
                    // 返回无通知数据时清空数据库的数据
                    groupDao.deleteAllGroupNotice();
                }

            }

            @NonNull
            @Override
            protected LiveData<List<GroupNoticeInfo>> loadFromDb() {
                GroupDao groupDao = dbManager.getGroupDao();
                if (groupDao != null) {
                    LiveData<List<GroupNoticeInfo>> liveInfoList = groupDao.getGroupNoticeList();
                    return liveInfoList;
                }
                return new MutableLiveData<>(null);
            }

            @NonNull
            @Override
            protected LiveData<Result<List<GroupNoticeInfoResult>>> createCall() {
                return groupService.getGroupNoticeInfo();
            }

        }.asLiveData();
    }

    /**
     * 设置消息状态
     *
     * @param groupId
     * @param receiverId
     * @param status     0 忽略、 1 同意
     * @return
     */
    public LiveData<Resource<Void>> setNoticeStatus(String groupId, String receiverId, String status, String noticeId) {
        return new NetworkOnlyResource<Void, Result<Void>>() {

            @NonNull
            @Override
            protected LiveData<Result<Void>> createCall() {
                HashMap<String, Object> paramMap = new HashMap<>();
                paramMap.put("groupId", groupId);
                paramMap.put("receiverId", receiverId);
                paramMap.put("status", Integer.valueOf(status));
                return groupService.setGroupNoticeStatus(RetrofitUtil.createJsonRequest(paramMap));
            }

            @Override
            protected void saveCallResult(@NonNull Void item) {
                GroupDao groupDao = dbManager.getGroupDao();
                // 更新通知状态
                if (groupDao != null) {
                    groupDao.updateGroupNoticeStatus(noticeId, Integer.valueOf(status));
                }
                super.saveCallResult(item);
            }
        }.asLiveData();
    }

    /**
     * 清空所有消息
     *
     * @return
     */
    public LiveData<Resource<Void>> clearGroupNotice() {
        return new NetworkOnlyResource<Void, Result<Void>>() {

            @NonNull
            @Override
            protected LiveData<Result<Void>> createCall() {
                return groupService.clearGroupNotice();
            }

            @Override
            protected void saveCallResult(@NonNull Void item) {
                GroupDao groupDao = dbManager.getGroupDao();
                if (groupDao != null) {
                    groupDao.deleteAllGroupNotice();
                }
                super.saveCallResult(item);
            }
        }.asLiveData();
    }

    /**
     * 复制群组
     *
     * @param groupId
     * @param name
     * @param portraitUri
     * @return
     */
    public LiveData<Resource<CopyGroupResult>> copyGroup(String groupId, String name, String portraitUri) {
        return new NetworkOnlyResource<CopyGroupResult, Result<CopyGroupResult>>() {
            @NonNull
            @Override
            protected LiveData<Result<CopyGroupResult>> createCall() {
                HashMap<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("groupId", groupId);
                bodyMap.put("name", name);
                if (!TextUtils.isEmpty(portraitUri)) {
                    bodyMap.put("portraitUri", portraitUri);
                }
                return groupService.copyGroup(RetrofitUtil.createJsonRequest(bodyMap));
            }
        }.asLiveData();
    }

    /**
     * 获取群通知消息详情
     *
     * @return
     */

    public LiveData<Resource<List<GroupExitedMemberInfo>>> getGroupExitedMemberInfo(String groupId) {
        return new NetworkBoundResource<List<GroupExitedMemberInfo>, Result<List<GroupExitedMemberInfo>>>() {

            @Override
            protected void saveCallResult(@NonNull Result<List<GroupExitedMemberInfo>> item) {
                if (item.getResult() == null) return;

                GroupDao groupDao = dbManager.getGroupDao();

                List<GroupExitedMemberInfo> resultList = item.getResult();
                if (groupDao != null) {
                    groupDao.deleteAllGroupExited();
                }
                if (resultList != null && resultList.size() > 0) {
                    groupDao.insertGroupExited(resultList);
                }
            }


            @NonNull
            @Override
            protected LiveData<List<GroupExitedMemberInfo>> loadFromDb() {
                GroupDao groupDao = dbManager.getGroupDao();
                if (groupDao != null) {
                    LiveData<List<GroupExitedMemberInfo>> liveInfoList = groupDao.getGroupExitedList();
                    return liveInfoList;
                }
                return new MutableLiveData<>(null);
            }

            @NonNull
            @Override
            protected LiveData<Result<List<GroupExitedMemberInfo>>> createCall() {
                HashMap<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("groupId", groupId);
                return groupService.getGroupExitedMemberInfo(RetrofitUtil.createJsonRequest(bodyMap));
            }

        }.asLiveData();
    }

    /**
     * 获取群成员用户信息
     *
     * @param groupId
     * @param memberId
     * @return
     */

    public LiveData<Resource<GroupMemberInfoDes>> getGroupMemberInfoDes(String groupId, String memberId) {
        return new NetworkBoundResource<GroupMemberInfoDes, Result<GroupMemberInfoDes>>() {

            @Override
            protected void saveCallResult(@NonNull Result<GroupMemberInfoDes> item) {
                if (item.getResult() == null) return;

                GroupDao groupDao = dbManager.getGroupDao();

                GroupMemberInfoDes info = item.getResult();
                info.setGroupId(groupId);
                info.setMemberId(memberId);
                if (groupDao != null && info != null) {
                    groupDao.insertGroupMemberInfoDes(info);
                }
            }


            @NonNull
            @Override
            protected LiveData<GroupMemberInfoDes> loadFromDb() {
                GroupDao groupDao = dbManager.getGroupDao();
                if (groupDao != null) {
                    LiveData<GroupMemberInfoDes> info = groupDao.getGroupMemberInfoDes(groupId, memberId);
                    return info;
                }
                return new MutableLiveData<>(null);
            }

            @NonNull
            @Override
            protected LiveData<Result<GroupMemberInfoDes>> createCall() {
                HashMap<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("groupId", groupId);
                bodyMap.put("memberId", memberId);
                return groupService.getGroupInfoDes(RetrofitUtil.createJsonRequest(bodyMap));
            }

        }.asLiveData();
    }

    /**
     * 设置群成员用户信息
     *
     * @param groupId       群 Id	String	必填
     * @param memberId      群用户 Id	String	必填
     * @param groupNickname 群成员昵称 String	非必填
     * @param region        区号	String	非必填
     * @param phone         电话	String	非必填
     * @param WeChat        微信号	String	非必填
     * @param Alipay        支付宝号	String	非必填
     * @param memberDesc    描述 array 非必填
     * @return
     */
    public LiveData<Resource<Void>> setGroupMemberInfoDes(String groupId, String memberId, String groupNickname
            , String region, String phone, String WeChat, String Alipay, ArrayList<String> memberDesc) {
        return new NetworkOnlyResource<Void, Result<Void>>() {
            @NonNull
            @Override
            protected LiveData<Result<Void>> createCall() {
                HashMap<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("groupId", groupId);
                bodyMap.put("memberId", memberId);
                if (groupNickname != null) {
                    bodyMap.put("groupNickname", groupNickname);
                }
                if (region != null) {
                    bodyMap.put("region", region);
                }
                if (phone != null) {
                    bodyMap.put("phone", phone);
                }
                if (WeChat != null) {
                    bodyMap.put("WeChat", WeChat);
                }
                if (Alipay != null) {
                    bodyMap.put("Alipay", Alipay);
                }
                if (memberDesc != null) {
                    bodyMap.put("memberDesc", memberDesc);
                }
                return groupService.setGroupInfoDes(RetrofitUtil.createJsonRequest(bodyMap));
            }

            @Override
            protected void saveCallResult(@NonNull Void item) {
                super.saveCallResult(item);
                GroupMemberInfoDes groupMemberInfoDes = new GroupMemberInfoDes();
                groupMemberInfoDes.setGroupId(groupId);
                groupMemberInfoDes.setMemberId(memberId);
                if (groupNickname != null) {
                    groupMemberInfoDes.setGroupNickname(groupNickname);
                    //设置成功后更新 groupMember 数据库 NickName
                    GroupMemberDao groupMemberDao = dbManager.getGroupMemberDao();
                    groupMemberDao.updateMemberNickName(groupNickname, groupId, memberId);
                }
                if (region != null) {
                    groupMemberInfoDes.setRegion(region);
                }
                if (phone != null) {
                    groupMemberInfoDes.setPhone(phone);
                }
                if (WeChat != null) {
                    groupMemberInfoDes.setWeChat(WeChat);
                }
                if (Alipay != null) {
                    groupMemberInfoDes.setAlipay(Alipay);
                }
                if (memberDesc != null) {
                    groupMemberInfoDes.setMemberDesc(memberDesc);
                }
                GroupDao groupDao = dbManager.getGroupDao();
                groupDao.insertGroupMemberInfoDes(groupMemberInfoDes);
            }
        }.asLiveData();
    }
}
