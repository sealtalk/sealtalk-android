package cn.rongcloud.im.task;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.rongcloud.im.common.ThreadManager;
import cn.rongcloud.im.contact.PhoneContactManager;
import cn.rongcloud.im.db.DbManager;
import cn.rongcloud.im.db.dao.FriendDao;
import cn.rongcloud.im.db.dao.GroupMemberDao;
import cn.rongcloud.im.db.dao.UserDao;
import cn.rongcloud.im.db.model.FriendInfo;
import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.db.model.FriendStatus;
import cn.rongcloud.im.db.model.PhoneContactInfoEntity;
import cn.rongcloud.im.db.model.UserInfo;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.model.AddFriendResult;
import cn.rongcloud.im.model.GetContactInfoResult;
import cn.rongcloud.im.model.PhoneContactInfo;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Result;
import cn.rongcloud.im.model.SearchFriendInfo;
import cn.rongcloud.im.model.SimplePhoneContactInfo;
import cn.rongcloud.im.net.HttpClientManager;
import cn.rongcloud.im.net.RetrofitUtil;
import cn.rongcloud.im.net.service.FriendService;
import cn.rongcloud.im.utils.CharacterParser;
import cn.rongcloud.im.utils.NetworkBoundResource;
import cn.rongcloud.im.utils.NetworkOnlyResource;
import cn.rongcloud.im.utils.RongGenerate;
import cn.rongcloud.im.utils.SearchUtils;
import cn.rongcloud.im.utils.log.SLog;
import io.rong.imlib.model.Conversation;
import okhttp3.RequestBody;

public class FriendTask {
    private static final String TAG = "FriendTask";
    private Context context;
    private FriendService friendService;
    private DbManager dbManager;

    public FriendTask(Context context) {
        this.context = context.getApplicationContext();
        friendService = HttpClientManager.getInstance(this.context).getClient().createService(FriendService.class);
        dbManager = DbManager.getInstance(this.context);
    }

    /**
     * 获取所有好友信息
     *
     * @return
     */
    public LiveData<Resource<List<FriendShipInfo>>> getAllFriends() {
        SLog.i(TAG, "getAllFriends()");

        return new NetworkBoundResource<List<FriendShipInfo>, Result<List<FriendShipInfo>>>() {
            @Override
            protected void saveCallResult(@NonNull Result<List<FriendShipInfo>> item) {
                List<FriendShipInfo> list = item.getResult();
                SLog.i(TAG, "saveCallResult() list.size() :" + list.size());
                UserInfo userInfo = null;
                FriendInfo friendInfo = null;
                List<UserInfo> userInfoList = new ArrayList<>();
                List<FriendInfo> friendInfoList = new ArrayList<>();
                for (FriendShipInfo friendShipInfo : list) {
                    userInfo = new UserInfo();
                    friendInfo = new FriendInfo();
                    userInfo.setId(friendShipInfo.getUser().getId());
                    userInfo.setName(friendShipInfo.getUser().getNickname());

                    String portraitUri = friendShipInfo.getUser().getPortraitUri();
                    // 若头像为空则生成默认头像
                    if (TextUtils.isEmpty(portraitUri)) {
                        portraitUri = RongGenerate.generateDefaultAvatar(context, friendShipInfo.getUser().getId(), friendShipInfo.getUser().getNickname());
                    }
                    userInfo.setPortraitUri(portraitUri);
                    userInfo.setAlias(friendShipInfo.getDisplayName());
                    userInfo.setFriendStatus(friendShipInfo.getStatus());
                    userInfo.setPhoneNumber(friendShipInfo.getUser().getPhone());
                    userInfo.setRegion(friendShipInfo.getUser().getRegion());
                    userInfo.setAliasSpelling(SearchUtils.fullSearchableString(friendShipInfo.getDisplayName()));
                    userInfo.setAliasSpellingInitial(SearchUtils.initialSearchableString(friendShipInfo.getDisplayName()));

                    userInfo.setNameSpelling(SearchUtils.fullSearchableString(friendShipInfo.getUser().getNickname()));
                    userInfo.setNameSpellingInitial(SearchUtils.initialSearchableString(friendShipInfo.getUser().getNickname()));

                    if (!TextUtils.isEmpty(friendShipInfo.getDisplayName())) {
                        userInfo.setOrderSpelling(CharacterParser.getInstance().getSpelling(friendShipInfo.getDisplayName()));
                    } else {
                        userInfo.setOrderSpelling(CharacterParser.getInstance().getSpelling(friendShipInfo.getUser().getNickname()));
                    }

                    friendInfo.setId(friendShipInfo.getUser().getId());
                    friendInfo.setMessage(friendShipInfo.getMessage());
                    friendInfo.setUpdatedAt(friendShipInfo.getUpdatedAt());
                    userInfoList.add(userInfo);
                    friendInfoList.add(friendInfo);

                    // 更新 IMKit 显示缓存
                    String name = userInfo.getAlias();
                    if (TextUtils.isEmpty(name)) {
                        name = userInfo.getName();
                    }
                    IMManager.getInstance().updateUserInfoCache(userInfo.getId(), name, Uri.parse(userInfo.getPortraitUri()));
                }

                UserDao userDao = dbManager.getUserDao();
                if (userDao != null) {
                    userDao.insertUserList(userInfoList);
                }

                FriendDao friendDao = dbManager.getFriendDao();
                if (friendDao != null) {
                    friendDao.insertFriendShipList(friendInfoList);
                }
            }

            @NonNull
            @Override
            protected LiveData<List<FriendShipInfo>> loadFromDb() {
                SLog.i(TAG, "getAllFriends() loadFromDb()");
                FriendDao friendDao = dbManager.getFriendDao();
                if (friendDao != null) {
                    return friendDao.getAllFriendListDB();
                } else {
                    return new MutableLiveData<>(null);
                }
            }

            @NonNull
            @Override
            protected LiveData<Result<List<FriendShipInfo>>> createCall() {
                SLog.i(TAG, "getAllFriends() createCall()");
                return friendService.getAllFriendList();
            }

            @Override
            protected boolean shouldFetch(@Nullable List<FriendShipInfo> data) {
                return true;
            }
        }.asLiveData();
    }

    /**
     * 获取好友信息
     *
     * @param userId
     * @return
     */
    public LiveData<Resource<FriendShipInfo>> getFriendInfo(String userId) {
        return new NetworkBoundResource<FriendShipInfo, Result<FriendShipInfo>>() {
            @Override
            protected void saveCallResult(@NonNull Result<FriendShipInfo> item) {
                UserDao userDao = dbManager.getUserDao();
                FriendDao friendDao = dbManager.getFriendDao();
                if (userDao == null || friendDao == null) return;

                FriendShipInfo friendShipInfo = item.getResult();
                if (friendShipInfo == null) return;

                UserInfo userInfo = new UserInfo();
                FriendInfo friendInfo = new FriendInfo();
                userInfo.setId(friendShipInfo.getUser().getId());
                userInfo.setName(friendShipInfo.getUser().getNickname());
                String portraitUri = friendShipInfo.getUser().getPortraitUri();
                // 若头像为空则生成默认头像
                if (TextUtils.isEmpty(portraitUri)) {
                    portraitUri = RongGenerate.generateDefaultAvatar(context, friendShipInfo.getUser().getId(), friendShipInfo.getUser().getNickname());
                }
                userInfo.setPortraitUri(portraitUri);
                userInfo.setAlias(friendShipInfo.getDisplayName());
                userInfo.setFriendStatus(FriendStatus.IS_FRIEND.getStatusCode());
                userInfo.setPhoneNumber(friendShipInfo.getUser().getPhone());
                userInfo.setRegion(friendShipInfo.getUser().getRegion());
                userInfo.setAliasSpelling(SearchUtils.fullSearchableString(friendShipInfo.getDisplayName()));
                userInfo.setAliasSpellingInitial(SearchUtils.initialSearchableString(friendShipInfo.getDisplayName()));
                userInfo.setNameSpelling(SearchUtils.fullSearchableString(friendShipInfo.getUser().getNickname()));
                userInfo.setNameSpellingInitial(SearchUtils.initialSearchableString(friendShipInfo.getUser().getNickname()));
                if (!TextUtils.isEmpty(friendShipInfo.getDisplayName())) {
                    userInfo.setOrderSpelling(CharacterParser.getInstance().getSpelling(friendShipInfo.getDisplayName()));
                } else {
                    userInfo.setOrderSpelling(CharacterParser.getInstance().getSpelling(friendShipInfo.getUser().getNickname()));
                }

                friendInfo.setId(friendShipInfo.getUser().getId());
                friendInfo.setMessage(friendShipInfo.getMessage());
                friendInfo.setUpdatedAt(friendShipInfo.getUpdatedAt() == null ? friendShipInfo.getUser().getUpdatedAt() : friendShipInfo.getUpdatedAt());

                userDao.insertUser(userInfo);
                friendDao.insertFriendShip(friendInfo);

                // 更新 IMKit 显示缓存
                String name = userInfo.getAlias();
                if (TextUtils.isEmpty(name)) {
                    name = userInfo.getName();
                }
                IMManager.getInstance().updateUserInfoCache(userInfo.getId(), name, Uri.parse(userInfo.getPortraitUri()));
            }

            @NonNull
            @Override
            protected LiveData<FriendShipInfo> loadFromDb() {
                FriendDao friendDao = dbManager.getFriendDao();
                LiveData<FriendShipInfo> friendInfo;
                if (friendDao == null) {
                    friendInfo = new MutableLiveData<>(null);
                } else {
                    friendInfo = friendDao.getFriendInfo(userId);
                }
                return friendInfo;
            }

            @NonNull
            @Override
            protected LiveData<Result<FriendShipInfo>> createCall() {
                return friendService.getFriendInfo(userId);
            }
        }.asLiveData();
    }

    public FriendShipInfo getFriendShipInfoFromDBSync(String userId) {
        return dbManager.getFriendDao().getFriendInfoSync(userId);
    }

    public List<FriendShipInfo> getFriendShipInfoListFromDBSync(String[] userIds) {
        return dbManager.getFriendDao().getFriendInfoListSync(userIds);
    }

    public LiveData<List<FriendShipInfo>> getFriendShipInfoListFromDB(String[] userIds) {
        return dbManager.getFriendDao().getFriendInfoList(userIds);
    }

    /**
     * 接受好友请求
     *
     * @param friendId
     * @return
     */
    public LiveData<Resource<Boolean>> agree(String friendId) {
        return new NetworkOnlyResource<Boolean, Result<Boolean>>() {

            @NonNull
            @Override
            protected LiveData<Result<Boolean>> createCall() {
                HashMap<String, Object> paramsMap = new HashMap<>();
                paramsMap.put("friendId", friendId);
                RequestBody body = RetrofitUtil.createJsonRequest(paramsMap);
                return friendService.agreeFriend(body);
            }
        }.asLiveData();
    }

    /**
     * 忽略好友请求
     *
     * @param friendId
     * @return
     */
    public LiveData<Resource<Void>> ingore(String friendId) {
        return new NetworkOnlyResource<Void, Result<Void>>() {

            @NonNull
            @Override
            protected LiveData<Result<Void>> createCall() {
                HashMap<String, Object> paramsMap = new HashMap<>();
                paramsMap.put("friendId", friendId);
                RequestBody body = RetrofitUtil.createJsonRequest(paramsMap);
                return friendService.ingoreFriend(body);
            }
        }.asLiveData();
    }

    public LiveData<List<FriendShipInfo>> searchFriendsFromDB(String match) {
        return dbManager.getFriendDao().searchFriendShip(match);
    }

    /**
     * 设置好友备注名
     *
     * @param friendId
     * @param alias
     * @return
     */
    public LiveData<Resource<Void>> setFriendAliasName(String friendId, String alias) {
        return new NetworkOnlyResource<Void, Result>() {
            @Override
            protected void saveCallResult(@NonNull Void item) {
                UserDao userDao = dbManager.getUserDao();
                if (userDao != null) {
                    String aliasSpelling = CharacterParser.getInstance().getSpelling(alias);
                    userDao.updateAlias(friendId, alias, aliasSpelling);

                    UserInfo userInfo = userDao.getUserByIdSync(friendId);
                    // 更新 IMKit 显示缓存
                    String name = userInfo.getAlias();
                    if (TextUtils.isEmpty(name)) {
                        name = userInfo.getName();
                    }
                    IMManager.getInstance().updateUserInfoCache(userInfo.getId(), name, Uri.parse(userInfo.getPortraitUri()));
                    // 需要获取此用户所在自己的哪些群组， 然后遍历修改其群组的个人信息。
                    // 用于当有备注的好友在群组时， 显示备注名称
                    GroupMemberDao groupMemberDao = dbManager.getGroupMemberDao();
                    List<String> groupIds = groupMemberDao.getGroupIdListByUserId(friendId);
                    if (groupIds != null && groupIds.size() > 0) {
                        for (String groupId : groupIds) {
                            IMManager.getInstance().updateGroupMemberInfoCache(groupId, friendId, name);
                        }
                    }
                }
            }

            @NonNull
            @Override
            protected LiveData<Result> createCall() {
                HashMap<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("friendId", friendId);
                bodyMap.put("displayName", alias);
                return friendService.setFriendAlias(RetrofitUtil.createJsonRequest(bodyMap));
            }
        }.asLiveData();
    }


    /**
     * 申请添加好友
     *
     * @param friendId
     * @param inviteMsg
     * @return
     */
    public LiveData<Resource<AddFriendResult>> inviteFriend(String friendId, String inviteMsg) {
        return new NetworkOnlyResource<AddFriendResult, Result<AddFriendResult>>() {
            @NonNull
            @Override
            protected LiveData<Result<AddFriendResult>> createCall() {
                HashMap<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("friendId", friendId);
                bodyMap.put("message", inviteMsg);
                return friendService.inviteFriend(RetrofitUtil.createJsonRequest(bodyMap));
            }
        }.asLiveData();
    }

    /**
     * 删除好友
     *
     * @param friendId
     * @return
     */
    public LiveData<Resource<Void>> deleteFriend(String friendId) {
        return new NetworkOnlyResource<Void, Result>() {
            @Override
            protected void saveCallResult(@NonNull Void item) {
                FriendDao friendDao = dbManager.getFriendDao();
                if (friendDao != null) {
                    friendDao.deleteFriend(friendId);
                    friendDao.removeFromBlackList(friendId);
                }
                UserDao userDao = dbManager.getUserDao();
                if (userDao != null) {
                    userDao.updateFriendStatus(friendId, FriendStatus.DELETE_FRIEND.getStatusCode());
                }
                IMManager.getInstance().clearConversationAndMessage(friendId, Conversation.ConversationType.PRIVATE);
            }

            @NonNull
            @Override
            protected LiveData<Result> createCall() {
                HashMap<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("friendId", friendId);
                return friendService.deleteFriend(RetrofitUtil.createJsonRequest(bodyMap));
            }
        }.asLiveData();
    }

    public LiveData<List<FriendShipInfo>> getAllFriendsExcludeGroup(String excludeGroupId) {
        return dbManager.getFriendDao().getAllFriendsExcluedGroup(excludeGroupId);
    }

    public LiveData<List<FriendShipInfo>> getAllFriendsIncludeGroup(String includeGroupId) {
        return dbManager.getFriendDao().getFriendsIncludeGroup(includeGroupId);
    }

    public LiveData<Resource<SearchFriendInfo>> searchFriendFromServer(String stAccount, String region, String phone) {
        return new NetworkOnlyResource<SearchFriendInfo, Result<SearchFriendInfo>>() {

            @NonNull
            @Override
            protected LiveData<Result<SearchFriendInfo>> createCall() {
                HashMap<String, String> queryMap = new HashMap<>();
                if(!TextUtils.isEmpty(stAccount)){
                    queryMap.put("st_account",stAccount);
                } else{
                    queryMap.put("region", region);
                    queryMap.put("phone", phone);
                }
                return friendService.searchFriend(queryMap);
            }
        }.asLiveData();
    }

    public LiveData<FriendShipInfo> getFriendShipInfoFromDB(String userId) {
        return dbManager.getFriendDao().getFriendInfo(userId);
    }

    /**
     * 获取手机通讯录中所有的用户信息，仅展示已注册了 SealTalk 的用户
     *
     * @return
     */
    public LiveData<Resource<List<PhoneContactInfo>>> getPhoneContactInfo() {
        MediatorLiveData<Resource<List<PhoneContactInfo>>> result = new MediatorLiveData<>();
        MutableLiveData<List<String>> phoneNumList = new MutableLiveData<>();
        ThreadManager.getInstance().runOnWorkThread(new Runnable() {
            @Override
            public void run() {
                phoneNumList.postValue(PhoneContactManager.getInstance().getAllContactPhoneNumber());
            }
        });

        result.addSource(phoneNumList, new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> phoneNumberList) {
                result.removeSource(phoneNumList);
                LiveData<Resource<List<PhoneContactInfo>>> phoneContactInfo = getPhoneContactInfo(phoneNumberList);
                result.addSource(phoneContactInfo, new Observer<Resource<List<PhoneContactInfo>>>() {
                    @Override
                    public void onChanged(Resource<List<PhoneContactInfo>> listResource) {
                        result.setValue(listResource);
                    }
                });
            }
        });

        return result;
    }

    /**
     * 获取手机通讯录中指定的用户信息，仅展示已注册了 SealTalk 的用户
     *
     * @param phoneNumberList
     * @return
     */
    public LiveData<Resource<List<PhoneContactInfo>>> getPhoneContactInfo(List<String> phoneNumberList) {
        return new NetworkBoundResource<List<PhoneContactInfo>, Result<List<GetContactInfoResult>>>() {
            @Override
            protected void saveCallResult(@NonNull Result<List<GetContactInfoResult>> item) {
                List<GetContactInfoResult> contactInfoList = item.getResult();
                if (contactInfoList == null || contactInfoList.size() == 0) return;

                UserDao userDao = dbManager.getUserDao();
                FriendDao friendDao = dbManager.getFriendDao();
                if (userDao == null || friendDao == null) return;

                List<PhoneContactInfoEntity> phoneContactInfoEntityList = new ArrayList<>();

                // 获取本地通讯录，做姓名匹配
                List<SimplePhoneContactInfo> allContactInfo = PhoneContactManager.getInstance().getAllContactInfo();
                HashMap<String, String> phoneNameMap = new HashMap();
                for (SimplePhoneContactInfo simplePhoneContactInfo : allContactInfo) {
                    phoneNameMap.put(simplePhoneContactInfo.getPhone(), simplePhoneContactInfo.getName());
                }

                for (GetContactInfoResult contactInfo : contactInfoList) {
                    int registered = contactInfo.getRegistered();
                    if (registered == 0) {
                        // 未注册不进行处理
                        continue;
                    }

                    String id = contactInfo.getId();
                    UserInfo userInfo = userDao.getUserByIdSync(id);
                    if (userInfo == null) {
                        userInfo = new UserInfo();
                        userInfo.setId(contactInfo.getId());
                    }
                    userInfo.setStAccount(contactInfo.getStAccount());
                    userInfo.setName(contactInfo.getNickname());
                    String portraitUri = contactInfo.getPortraitUri();
                    // 若头像为空则生成默认头像
                    if (TextUtils.isEmpty(portraitUri)) {
                        portraitUri = RongGenerate.generateDefaultAvatar(context, contactInfo.getId(), contactInfo.getNickname());
                    }
                    userInfo.setPortraitUri(portraitUri);
                    userInfo.setPhoneNumber(contactInfo.getPhone());
                    userInfo.setNameSpelling(SearchUtils.fullSearchableString(contactInfo.getNickname()));
                    userInfo.setNameSpellingInitial(SearchUtils.initialSearchableString(contactInfo.getNickname()));
                    if (TextUtils.isEmpty(userInfo.getAlias())) {
                        userInfo.setOrderSpelling(CharacterParser.getInstance().getSpelling(contactInfo.getNickname()));
                    }
                    userInfo.setStAccount(contactInfo.getStAccount());

                    userDao.insertUser(userInfo);
                    // 更新 IMKit 显示缓存
                    String name = userInfo.getAlias();
                    if (TextUtils.isEmpty(name)) {
                        name = userInfo.getName();
                    }
                    IMManager.getInstance().updateUserInfoCache(userInfo.getId(), name, Uri.parse(userInfo.getPortraitUri()));

                    // 添加通讯录信息
                    PhoneContactInfoEntity phoneContactInfoEntity = new PhoneContactInfoEntity();
                    phoneContactInfoEntity.setPhoneNumber(contactInfo.getPhone());
                    phoneContactInfoEntity.setContactName(phoneNameMap.get(contactInfo.getPhone()));
                    phoneContactInfoEntity.setUserId(contactInfo.getId());
                    phoneContactInfoEntity.setRelationship(contactInfo.getRelationship());
                    phoneContactInfoEntityList.add(phoneContactInfoEntity);
                }

                friendDao.insertPhoneContactInfo(phoneContactInfoEntityList);
            }

            @NonNull
            @Override
            protected LiveData<List<PhoneContactInfo>> loadFromDb() {
                FriendDao friendDao = dbManager.getFriendDao();
                if (friendDao != null) {
                    return friendDao.getPhoneContactInfo();
                } else {
                    return new MutableLiveData<>(null);
                }
            }

            @NonNull
            @Override
            protected LiveData<Result<List<GetContactInfoResult>>> createCall() {
                HashMap<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("contactList", phoneNumberList);
                return friendService.getContactsInfo(RetrofitUtil.createJsonRequest(bodyMap));
            }
        }.asLiveData();
    }

    /**
     * 所搜数据库中手机通讯录已注册用户的信息，通过通讯录中名称和 SealTalk 号模糊搜索
     *
     * @param keyword
     * @return
     */
    public LiveData<List<PhoneContactInfo>> searchPhoneContactInfo(String keyword) {
        LiveData<List<PhoneContactInfo>> searchPhoneContactInfo;
        FriendDao friendDao = dbManager.getFriendDao();
        if (friendDao != null) {
            searchPhoneContactInfo = friendDao.searchPhoneContactInfo(keyword);
        } else {
            searchPhoneContactInfo = new MutableLiveData<>(null);
        }

        return searchPhoneContactInfo;
    }

}
