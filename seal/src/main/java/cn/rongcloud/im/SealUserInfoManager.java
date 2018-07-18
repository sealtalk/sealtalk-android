package cn.rongcloud.im;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import cn.rongcloud.im.db.BlackList;
import cn.rongcloud.im.db.BlackListDao;
import cn.rongcloud.im.db.DBManager;
import cn.rongcloud.im.db.Friend;
import cn.rongcloud.im.db.FriendDao;
import cn.rongcloud.im.db.GroupMember;
import cn.rongcloud.im.db.GroupMemberDao;
import cn.rongcloud.im.db.Groups;
import cn.rongcloud.im.db.GroupsDao;
import cn.rongcloud.im.db.UserInfoBean;
import cn.rongcloud.im.server.SealAction;
import cn.rongcloud.im.server.network.async.AsyncTaskManager;
import cn.rongcloud.im.server.network.async.OnDataListener;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.pinyin.CharacterParser;
import cn.rongcloud.im.server.response.GetBlackListResponse;
import cn.rongcloud.im.server.response.GetGroupInfoResponse;
import cn.rongcloud.im.server.response.GetGroupMemberResponse;
import cn.rongcloud.im.server.response.GetGroupResponse;
import cn.rongcloud.im.server.response.GetTokenResponse;
import cn.rongcloud.im.server.response.UserRelationshipResponse;
import cn.rongcloud.im.server.utils.NLog;
import cn.rongcloud.im.server.utils.RongGenerate;
import io.rong.common.RLog;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.UserInfo;

/**
 * Created by wangmingqiang on 16/9/10.
 * Company RongCloud
 * 数据库访问接口,目前接口有同步和异步之分
 * 第一次login时从app server获取数据,之后数据库读,数据的更新使用IMKit里的通知类消息
 * 因为存在app server获取数据失败的情况,代码里有很多这种异常情况的判断处理并重新从app server获取数据
 * 1.add...类接口为插入或更新数据库
 * 2.get...类接口为读取数据库
 * 3.delete...类接口为删除数据库数据
 * 4.sync...为同步接口,因为存在去掉sync相同名称的异步接口,有些同步类接口不带sync
 * 5.fetch...,pull...类接口都是从app server获取数据并存数据库,不同的只是返回值不一样,此类接口全部为private
 */
public class SealUserInfoManager implements OnDataListener {

    private final static String TAG = "SealUserInfoManager";
    private static final int GET_TOKEN = 800;

    /**
     * 用户信息全部未同步
     */
    private static final int NONE = 0;//00000
    /**
     * 好友信息同步成功
     */
    private static final int FRIEND = 1;//00001
    /**
     * 群组信息同步成功
     */
    private static final int GROUPS = 2;//00010
    /**
     * 群成员信息部分同步成功,n个群组n次访问网络,存在部分同步的情况
     */
    private static final int PARTGROUPMEMBERS = 4;//00100
    /**
     * 群成员信息同步成功
     */
    private static final int GROUPMEMBERS = 8;//01000
    /**
     * 黑名单信息同步成功
     */
    private static final int BLACKLIST = 16;//10000
    /**
     * 用户信息全部同步成功
     */
    private static final int ALL = 27;//11011

    private static SealUserInfoManager sInstance;
    private final Context mContext;
    private final AsyncTaskManager mAsyncTaskManager;
    private final SealAction action;
    private DBManager mDBManager;
    private Handler mWorkHandler;
    private HandlerThread mWorkThread;
    static Handler mHandler;
    private SharedPreferences sp;
    private List<Groups> mGroupsList;//同步群组成员信息时需要这个数据
    private int mGetAllUserInfoState;
    private boolean doingGetAllUserInfo = false;
    private FriendDao mFriendDao;
    private GroupsDao mGroupsDao;
    private GroupMemberDao mGroupMemberDao;
    private BlackListDao mBlackListDao;
    private LinkedHashMap<String, UserInfo> mUserInfoCache;

    public static SealUserInfoManager getInstance() {
        return sInstance;
    }

    public SealUserInfoManager(Context context) {
        mContext = context;
        mAsyncTaskManager = AsyncTaskManager.getInstance(mContext);
        sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        action = new SealAction(mContext);
        mHandler = new Handler(Looper.getMainLooper());
        mGroupsList = null;
    }

    public static void init(Context context) {
        RLog.d(TAG, "SealUserInfoManager init");
        sInstance = new SealUserInfoManager(context);
    }

    /**
     * 修改数据库的存贮路径为.../appkey/userID/,
     * 必须确保userID存在后才能初始化DBManager
     */
    public void openDB() {
        RLog.d(TAG, "SealUserInfoManager openDB");
        if (mDBManager == null || !mDBManager.isInitialized()) {
            mDBManager = DBManager.init(mContext);
            mWorkThread = new HandlerThread("sealUserInfoManager");
            mWorkThread.start();
            mWorkHandler = new Handler(mWorkThread.getLooper());
            mFriendDao = getFriendDao();
            mGroupsDao = getGroupsDao();
            mGroupMemberDao = getGroupMemberDao();
            mBlackListDao = getBlackListDao();
            mUserInfoCache = new LinkedHashMap<>();
            setUserInfoEngineListener();
        }
        mGetAllUserInfoState = sp.getInt("getAllUserInfoState", 0);
        RLog.d(TAG, "SealUserInfoManager mGetAllUserInfoState = " + mGetAllUserInfoState);
    }

    public void closeDB() {
        RLog.d(TAG, "SealUserInfoManager closeDB");
        if (mDBManager != null) {
            mDBManager.uninit();
            mDBManager = null;
            mFriendDao = null;
            mGroupsDao = null;
            mGroupMemberDao = null;
            mBlackListDao = null;
        }
        if (mWorkThread != null) {
            mWorkThread.quit();
            mWorkThread = null;
            mWorkHandler = null;
        }
        if (mUserInfoCache != null) {
            mUserInfoCache.clear();
            mUserInfoCache = null;
        }
        mGroupsList = null;
        UserInfoEngine.getInstance(mContext).setListener(null);
        GroupInfoEngine.getInstance(mContext).setmListener(null);
    }

    /**
     * kit中提供用户信息的用户信息提供者
     * 1.读缓存
     * 2.读好友数据库
     * 3.读群组成员数据库
     * 4.网络获取
     */
    public void getUserInfo(final String userId) {
        if (TextUtils.isEmpty(userId)) {
            return;
        }
        if (mUserInfoCache != null) {
            UserInfo userInfo = mUserInfoCache.get(userId);
            if (userInfo != null) {
                RongIM.getInstance().refreshUserInfoCache(userInfo);
                NLog.d(TAG, "SealUserInfoManager getUserInfo from cache " + userId + " "
                        + userInfo.getName() + " " + userInfo.getPortraitUri());
                return;
            }
        }
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                UserInfo userInfo;
                Friend friend = getFriendByID(userId);
                if (friend != null) {
                    String name = friend.getName();
                    if (friend.isExitsDisplayName()) {
                        name = friend.getDisplayName();
                    }
                    userInfo = new UserInfo(friend.getUserId(), name, friend.getPortraitUri());
                    NLog.d(TAG, "SealUserInfoManager getUserInfo from Friend db " + userId + " "
                            + userInfo.getName() + " " + userInfo.getPortraitUri());
                    RongIM.getInstance().refreshUserInfoCache(userInfo);
                    return;
                }
                List<GroupMember> groupMemberList = getGroupMembersWithUserId(userId);
                if (groupMemberList != null && groupMemberList.size() > 0) {
                    GroupMember groupMember = groupMemberList.get(0);
                    userInfo = new UserInfo(groupMember.getUserId(), groupMember.getName(),
                            groupMember.getPortraitUri());
                    NLog.d(TAG, "SealUserInfoManager getUserInfo from GroupMember db " + userId + " "
                            + userInfo.getName() + " " + userInfo.getPortraitUri());
                    RongIM.getInstance().refreshUserInfoCache(userInfo);
                    return;
                }
                UserInfoEngine.getInstance(mContext).startEngine(userId);
            }
        });
    }

    public void getGroupInfo(final String groupsId) {
        if (TextUtils.isEmpty(groupsId)) {
            return;
        }
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                Group groupInfo;
                Groups group = getGroupsByID(groupsId);
                if (group != null) {
                    groupInfo = new Group(groupsId, group.getName(), Uri.parse(group.getPortraitUri()));
                    RongIM.getInstance().refreshGroupInfoCache(groupInfo);
                    NLog.d(TAG, "SealUserInfoManager getGroupInfo from db " + groupsId + " "
                            + groupInfo.getName() + " " + groupInfo.getPortraitUri());
                    return;
                }
                GroupInfoEngine.getInstance(mContext).startEngine(groupsId);
            }
        });
    }

    /**
     * 需要 rongcloud connect 成功后设置的 listener
     */
    public void setUserInfoEngineListener() {
        UserInfoEngine.getInstance(mContext).setListener(new UserInfoEngine.UserInfoListener() {
            @Override
            public void onResult(UserInfo info) {
                if (info != null && RongIM.getInstance() != null) {
                    if (TextUtils.isEmpty(info.getPortraitUri() == null ? null : info.getPortraitUri().toString())) {
                        info.setPortraitUri(Uri.parse(RongGenerate.generateDefaultAvatar(info.getName(), info.getUserId())));
                    }
                    NLog.d(TAG, "SealUserInfoManager getUserInfo from network " + info.getUserId() + " " + info.getName() + " " + info.getPortraitUri());
                    RongIM.getInstance().refreshUserInfoCache(info);
                }
            }
        });
        GroupInfoEngine.getInstance(mContext).setmListener(new GroupInfoEngine.GroupInfoListeners() {
            @Override
            public void onResult(Group info) {
                if (info != null && RongIM.getInstance() != null) {
                    NLog.d(TAG, "SealUserInfoManager getGroupInfo from network " + info.getId() + " " + info.getName() + " " + info.getPortraitUri());
                    if (TextUtils.isEmpty(info.getPortraitUri() == null ? null : info.getPortraitUri().toString())) {
                        info.setPortraitUri(Uri.parse(RongGenerate.generateDefaultAvatar(info.getName(), info.getId())));
                    }
                    RongIM.getInstance().refreshGroupInfoCache(info);
                }
            }
        });
    }

    /**
     * 第一次登录时同步好友,群组,群组成员,黑名单数据
     */
    public void getAllUserInfo() {
        if (!isNetworkConnected())
            return;
        if (hasGetAllUserInfo())
            return;
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    doingGetAllUserInfo = true;
                    //在获取用户信息时无论哪一个步骤出错,都不继续往下执行,因为网络出错,很可能再次的网络访问还是有问题
                    if (needGetAllUserInfo()) {
                        if (!fetchFriends()) {
                            setGetAllUserInfoDone();
                            return;
                        }
                        //必须取得群组信息成功时才有必要获取群组成员信息
                        if (fetchGroups()) {
                            if (!fetchGroupMembers()) {
                                setGetAllUserInfoDone();
                                return;
                            }
                        } else {
                            setGetAllUserInfoDone();
                            return;
                        }
                        fetchBlackList();
                    } else {
                        if (!hasGetFriends()) {
                            if (!fetchFriends()) {
                                setGetAllUserInfoDone();
                                return;
                            }
                        }
                        if (!hasGetGroups()) {
                            if (!fetchGroups()) {
                                setGetAllUserInfoDone();
                                return;
                            }
                            if (!hasGetAllGroupMembers()) {
                                if (!fetchGroupMembers()) {
                                    setGetAllUserInfoDone();
                                    return;
                                }
                            }
                        }
                        //部分群组信息同步的情况,此时需要特殊处理,但是目前暂未处理
                        //// TODO: 16/9/20
                        if (!hasGetAllGroupMembers()) {
                            if (hasGetPartGroupMembers()) {
                                syncDeleteGroupMembers();
                            }
                            if (mGroupsList == null) {
                                mGroupsList = getGroups();
                            }
                            fetchGroupMembers();
                        }
                        if (!hasGetBlackList()) {
                            fetchBlackList();
                        }
                    }
                } catch (HttpException e) {
                    e.printStackTrace();
                    RLog.d(TAG, "fetchUserInfo occurs HttpException e=" + e.toString() + "mGetAllUserInfoState=" + mGetAllUserInfoState);
                } catch (Exception e) {
                    e.printStackTrace();
                    RLog.d(TAG, "fetchUserInfo occurs Exception e=" + e.toString() + "mGetAllUserInfoState=" + mGetAllUserInfoState);
                }
                setGetAllUserInfoDone();
            }
        });
    }

    private void setGetAllUserInfoDone() {
        RLog.d(TAG, "SealUserInfoManager setGetAllUserInfoDone = " + mGetAllUserInfoState);
        doingGetAllUserInfo = false;
        sp.edit().putInt("getAllUserInfoState", mGetAllUserInfoState).commit();
    }

    private boolean fetchFriends() throws HttpException {
        UserRelationshipResponse userRelationshipResponse;
        try {
            userRelationshipResponse = action.getAllUserRelationship();
        } catch (JSONException e) {
            NLog.d(TAG, "fetchFriends occurs JSONException e=" + e.toString());
            return true;
        }
        if (userRelationshipResponse != null && userRelationshipResponse.getCode() == 200) {
            List<UserRelationshipResponse.ResultEntity> list = userRelationshipResponse.getResult();
            if (list != null && list.size() > 0) {
                syncDeleteFriends();
                addFriends(list);
            }
            mGetAllUserInfoState |= FRIEND;
            return true;
        }
        return false;
    }

    private List<Friend> pullFriends() throws HttpException {
        List<Friend> friendsList = null;
        UserRelationshipResponse userRelationshipResponse;
        try {
            userRelationshipResponse = action.getAllUserRelationship();
        } catch (JSONException e) {
            NLog.d(TAG, "pullFriends occurs JSONException e=" + e.toString());
            return null;
        }
        if (userRelationshipResponse != null && userRelationshipResponse.getCode() == 200) {
            List<UserRelationshipResponse.ResultEntity> list = userRelationshipResponse.getResult();
            if (list != null && list.size() > 0) {
                syncDeleteFriends();
                friendsList = addFriends(list);
            }
            mGetAllUserInfoState |= FRIEND;
        }
        return friendsList;
    }

    private boolean fetchGroups() throws HttpException {
        GetGroupResponse groupResponse;
        try {
            groupResponse = action.getGroups();
        } catch (JSONException e) {
            NLog.d(TAG, "fetchGroups occurs JSONException e=" + e.toString());
            return true;
        }
        if (groupResponse != null && groupResponse.getCode() == 200) {
            List<GetGroupResponse.ResultEntity> groupsList = groupResponse.getResult();
            if (groupsList != null && groupsList.size() > 0) {
                syncDeleteGroups();
                addGroups(groupsList);
            }
            mGetAllUserInfoState |= GROUPS;
            return true;
        }
        return false;
    }

    /**
     * 从server获取群组信息,群组create时使用
     * 注意这个接口同其它getGroups接口的区别,此方法只是写数据库不返回数据
     *
     * @param groupID 群组ID
     */
    public void getGroups(final String groupID) {
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    //如果当前正在执行从server获取数据操作,不重复执行,但是是否会存在问题,我不是太确定,实际测试时看一下效果
                    if (doingGetAllUserInfo)
                        return;
                    if (!hasGetGroups()) {
                        fetchGroups();
                    } else {
                        GetGroupInfoResponse groupInfoResponse = action.getGroupInfo(groupID);
                        if (groupInfoResponse != null && groupInfoResponse.getCode() == 200) {
                            GetGroupInfoResponse.ResultEntity groupInfo = groupInfoResponse.getResult();
                            if (groupInfo != null) {
                                String role = groupInfo.getCreatorId().equals(RongIM.getInstance().getCurrentUserId()) ? "0" : "1";
                                syncAddGroup(new Groups(groupID,
                                        groupInfo.getName(),
                                        groupInfo.getPortraitUri(),
                                        role));
                            }
                        } else {
                            mGetAllUserInfoState &= ~GROUPS;
                        }
                    }
                } catch (HttpException e) {
                    e.printStackTrace();
                    mGetAllUserInfoState &= ~GROUPS;
                    NLog.d(TAG, "fetchGroups occurs HttpException e=" + e.toString() + "groupID=" + groupID);
                } catch (JSONException e) {
                    e.printStackTrace();
                    NLog.d(TAG, "fetchGroups occurs JSONException e=" + e.toString() + "groupID=" + groupID);
                }
                sp.edit().putInt("getAllUserInfoState", mGetAllUserInfoState).commit();
            }
        });
    }

    private List<Groups> pullGroups() throws HttpException {
        List<Groups> groupsList = null;
        GetGroupResponse groupResponse;
        try {
            groupResponse = action.getGroups();
        } catch (JSONException e) {
            NLog.d(TAG, "pullGroups occurs JSONException e=" + e.toString());
            return null;
        }
        if (groupResponse != null && groupResponse.getCode() == 200) {
            List<GetGroupResponse.ResultEntity> list = groupResponse.getResult();
            if (list != null && list.size() > 0) {
                syncDeleteGroups();
                groupsList = addGroups(list);
            }
            mGetAllUserInfoState |= GROUPS;
            return groupsList;
        }
        return null;
    }

    private Groups pullGroups(String groupID) throws HttpException {
        Groups group = null;
        List<Groups> groupsList;
        GetGroupResponse groupResponse;
        try {
            groupResponse = action.getGroups();
        } catch (JSONException e) {
            NLog.d(TAG, "pullGroups(String groupID) occurs JSONException e=" + e.toString());
            return null;
        }
        if (groupResponse != null && groupResponse.getCode() == 200) {
            List<GetGroupResponse.ResultEntity> list = groupResponse.getResult();
            if (list != null && list.size() > 0) {
                syncDeleteGroups();
                groupsList = addGroups(list);
                for (Groups groups : groupsList) {
                    if (groupID.equals(groups.getGroupsId()))
                        group = groups;
                }
            }
            mGetAllUserInfoState |= GROUPS;
            return group;
        }
        return null;
    }

    private boolean fetchGroupMembers() throws HttpException {
        int fetchGroupCount = 0;
        if (mGroupsList != null && mGroupsList.size() > 0) {
            syncDeleteGroupMembers();
            for (Groups group : mGroupsList) {
                GetGroupMemberResponse groupMemberResponse;
                try {
                    groupMemberResponse = action.getGroupMember(group.getGroupsId());
                } catch (JSONException e) {
                    NLog.d(TAG, "fetchGroupMembers occurs JSONException e=" + e.toString() + ", groupID=" + group.getGroupsId());
                    fetchGroupCount++;
                    continue;
                }
                if (groupMemberResponse != null && groupMemberResponse.getCode() == 200) {
                    fetchGroupCount++;
                    List<GetGroupMemberResponse.ResultEntity> list = groupMemberResponse.getResult();
                    if (list != null && list.size() > 0) {
                        if (mGroupMemberDao != null) {
                            addGroupMembers(list, group.getGroupsId());
                        } else if (mDBManager == null) {
                            //如果这两个都为null,说明是被踢,已经关闭数据库,没要必要继续执行
                            return false;
                        }
                    }
                } else {
                    if (fetchGroupCount > 0) {
                        setGetAllUserInfoWithPartGroupMembersState();
                    }
                    return false;
                }
            }
            if (mGroupsList != null && fetchGroupCount == mGroupsList.size()) {
                setGetAllUserInfoWtihAllGroupMembersState();
                return true;
            }
        } else {
            setGetAllUserInfoWtihAllGroupMembersState();
            return true;
        }
        return false;
    }

    /**
     * 从server获取群组成员信息,群组create时使用
     * 注意这个接口同其它getGroupMember接口的区别,此方法只是写数据库不返回数据
     *
     * @param groupID 群组ID
     */
    public void getGroupMember(final String groupID) {
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                if (doingGetAllUserInfo)
                    return;
                if (hasGetAllGroupMembers()) {
                    GetGroupMemberResponse groupMemberResponse;
                    try {
                        groupMemberResponse = action.getGroupMember(groupID);
                    } catch (HttpException e) {
                        e.printStackTrace();
                        setGetAllUserInfoWithPartGroupMembersState();
                        sp.edit().putInt("getAllUserInfoState", mGetAllUserInfoState).commit();
                        NLog.d(TAG, "getGroupMember occurs HttpException e=" + e.toString() + "groupID=" + groupID);
                        return;
                    } catch (JSONException e) {
                        e.printStackTrace();
                        NLog.d(TAG, "getGroupMember occurs JSONException e=" + e.toString() + "groupID=" + groupID);
                        return;
                    }
                    if (groupMemberResponse != null && groupMemberResponse.getCode() == 200) {
                        List<GetGroupMemberResponse.ResultEntity> list = groupMemberResponse.getResult();
                        if (list != null && list.size() > 0) {
                            syncDeleteGroupMembers(groupID);
                            addGroupMembers(list, groupID);
                        }
                    } else {
                        setGetAllUserInfoWithPartGroupMembersState();
                        sp.edit().putInt("getAllUserInfoState", mGetAllUserInfoState).commit();
                    }
                } else {
                    if (hasGetPartGroupMembers()) {
                        syncDeleteGroupMembers();
                    }
                    if (mGroupsList == null) {
                        mGroupsList = getGroups();
                    }
                    try {
                        fetchGroupMembers();
                        sp.edit().putInt("getAllUserInfoState", mGetAllUserInfoState).commit();
                    } catch (HttpException e) {
                        setGetAllUserInfoWithPartGroupMembersState();
                        sp.edit().putInt("getAllUserInfoState", mGetAllUserInfoState).commit();
                        NLog.d(TAG, "getGroupMember occurs HttpException e=" + e.toString() + "groupID=" + groupID);
                        return;
                    }
                }
            }
        });
    }

    /**
     * 返回群组成员信息
     *
     * @param groupsID 群组ID
     * @return 群组成员信息
     */
    private List<GroupMember> pullGroupMembers(String groupsID) throws HttpException {

        int fetchGroupCount = 0;
        List<GroupMember> groupMembersList = null;
        if (mGroupsList == null && hasGetGroups()) {
            mGroupsList = getGroups();
        }
        if (mGroupsList != null && mGroupsList.size() > 0) {
            syncDeleteGroupMembers();
            for (Groups group : mGroupsList) {
                GetGroupMemberResponse groupMemberResponse;
                try {
                    groupMemberResponse = action.getGroupMember(group.getGroupsId());
                } catch (JSONException e) {
                    NLog.d(TAG, "pullGroupMembers(String groupsID) occurs JSONException e=" + e.toString() + ", groupID=" + groupsID);
                    fetchGroupCount++;
                    continue;
                }
                if (groupMemberResponse != null && groupMemberResponse.getCode() == 200) {
                    fetchGroupCount++;
                    List<GetGroupMemberResponse.ResultEntity> list = groupMemberResponse.getResult();
                    if (list != null && list.size() > 0) {
                        List<GroupMember> memberList = addGroupMembers(list, group.getGroupsId());
                        if (groupsID.equals(group.getGroupsId())) {
                            groupMembersList = memberList;
                        }
                    }
                } else {
                    if (fetchGroupCount > 0) {
                        setGetAllUserInfoWithPartGroupMembersState();
                    }
                    return groupMembersList;
                }
            }
            if (fetchGroupCount == mGroupsList.size()) {
                setGetAllUserInfoWtihAllGroupMembersState();
            }
        } else {
            setGetAllUserInfoWtihAllGroupMembersState();
        }
        return groupMembersList;
    }

    private boolean fetchBlackList() throws HttpException {
        GetBlackListResponse blackListResponse;
        try {
            blackListResponse = action.getBlackList();
        } catch (JSONException e) {
            NLog.d(TAG, "fetchBlackList occurs JSONException e=" + e.toString());
            return true;
        }
        if (blackListResponse != null && blackListResponse.getCode() == 200) {
            List<GetBlackListResponse.ResultEntity> blackList = blackListResponse.getResult();
            if (blackList != null && blackList.size() > 0) {
                syncDeleteBlackList();
                addBlackList(blackList);
            }
            mGetAllUserInfoState |= BLACKLIST;
            return true;
        }
        return false;
    }

    private List<BlackList> pullBlackList() throws HttpException {
        List<BlackList> blackList = null;
        GetBlackListResponse blackListResponse;
        try {
            blackListResponse = action.getBlackList();
        } catch (JSONException e) {
            NLog.d(TAG, "pullBlackList occurs JSONException e=" + e.toString());
            return null;
        }
        if (blackListResponse != null && blackListResponse.getCode() == 200) {
            List<GetBlackListResponse.ResultEntity> responseBlackList = blackListResponse.getResult();
            if (responseBlackList != null && responseBlackList.size() > 0) {
                syncDeleteBlackList();
                blackList = addBlackList(responseBlackList);
            }
            mGetAllUserInfoState |= BLACKLIST;
        }
        return blackList;
    }

    private FriendDao getFriendDao() {
        if (mDBManager != null && mDBManager.getDaoSession() != null) {
            return mDBManager.getDaoSession().getFriendDao();
        } else {
            return null;
        }
    }

    private GroupsDao getGroupsDao() {
        if (mDBManager != null && mDBManager.getDaoSession() != null) {
            return mDBManager.getDaoSession().getGroupsDao();
        } else {
            return null;
        }
    }

    private GroupMemberDao getGroupMemberDao() {
        if (mDBManager != null && mDBManager.getDaoSession() != null) {
            return mDBManager.getDaoSession().getGroupMemberDao();
        } else {
            return null;
        }
    }

    private BlackListDao getBlackListDao() {
        if (mDBManager != null && mDBManager.getDaoSession() != null) {
            return mDBManager.getDaoSession().getBlackListDao();
        } else {
            return null;
        }
    }

    public void addFriend(final Friend friend) {
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mFriendDao != null) {
                    if (friend != null) {
                        if (friend.getPortraitUri() == null || TextUtils.isEmpty(friend.getPortraitUri().toString())) {
                            friend.setPortraitUri(Uri.parse(getPortrait(friend)));
                        }
                        mFriendDao.insertOrReplace(friend);
                    }
                }
            }
        });
    }

    public void addGroup(final Groups groups) {
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                syncAddGroup(groups);
            }
        });
    }

    private void syncAddGroup(final Groups groups) {
        if (mGroupsDao != null) {
            if (groups != null) {
                String portrait = groups.getPortraitUri();
                if (TextUtils.isEmpty(portrait)) {
                    portrait = RongGenerate.generateDefaultAvatar(groups.getName(), groups.getGroupsId());
                    groups.setPortraitUri(portrait);
                }
                mGroupsDao.insertOrReplace(groups);
            }
        }
    }

    public void addGroupMember(final GroupMember groupMember) {
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mGroupMemberDao != null) {
                    if (groupMember != null) {
                        Uri portrait = groupMember.getPortraitUri();
                        if (portrait == null) {
                            portrait = getPortrait(groupMember);
                            groupMember.setPortraitUri(portrait);
                        }
                        mGroupMemberDao.insertOrReplace(groupMember);
                    }
                }
            }
        });
    }

    /**
     * 同步接口,从server获取的好友信息插入数据库,目前只有同步接口,如果需要可以加异步接口
     *
     * @param list server获取的好友信息
     * @return List<Friend> 好友列表
     */
    private List<Friend> addFriends(final List<UserRelationshipResponse.ResultEntity> list) {
        if (list != null && list.size() > 0) {
            List<Friend> friendsList = new ArrayList<>();
            for (UserRelationshipResponse.ResultEntity resultEntity : list) {
                if (resultEntity.getStatus() == 20) {
                    Friend friend = new Friend(
                            resultEntity.getUser().getId(),
                            resultEntity.getUser().getNickname(),
                            Uri.parse(resultEntity.getUser().getPortraitUri()),
                            resultEntity.getDisplayName(),
                            null, null, null, null,
                            CharacterParser.getInstance().getSpelling(resultEntity.getUser().getNickname()),
                            CharacterParser.getInstance().getSpelling(resultEntity.getDisplayName()));
                    if (friend.getPortraitUri() == null || TextUtils.isEmpty(friend.getPortraitUri().toString())) {
                        String portrait = getPortrait(friend);
                        if (portrait != null) {
                            friend.setPortraitUri(Uri.parse(getPortrait(friend)));
                        }
                    }
                    friendsList.add(friend);
                }
            }
            if (friendsList.size() > 0) {
                if (mFriendDao != null) {
                    mFriendDao.insertOrReplaceInTx(friendsList);
                }
            }
            return friendsList;
        } else {
            return null;
        }
    }

    /**
     * 同步接口,从server获取的群组信息插入数据库,目前只有同步接口,如果需要可以加异步接口
     *
     * @param list server获取的群组信息
     * @return List<Groups> 群组列表
     */
    private List<Groups> addGroups(final List<GetGroupResponse.ResultEntity> list) {
        if (list != null && list.size() > 0) {
            mGroupsList = new ArrayList<>();
            for (GetGroupResponse.ResultEntity groups : list) {
                String portrait = groups.getGroup().getPortraitUri();
                if (TextUtils.isEmpty(portrait)) {
                    portrait = RongGenerate.generateDefaultAvatar(groups.getGroup().getName(), groups.getGroup().getId());
                }
                mGroupsList.add(new Groups(groups.getGroup().getId(),
                        groups.getGroup().getName(),
                        portrait,
                        String.valueOf(groups.getRole())
                ));
            }
            if (mGroupsList.size() > 0) {
                if (mGroupsDao != null) {
                    mGroupsDao.insertOrReplaceInTx(mGroupsList);
                }
            }
            return mGroupsList;
        } else {
            return null;
        }
    }


    /**
     * 删除一个群组里的所有群成员
     *
     * @param groupID 群组ID
     */
    private void syncDeleteGroupMembers(String groupID) {
        if (mGroupMemberDao != null) {
            mGroupMemberDao.queryBuilder()
                    .where(GroupMemberDao.Properties.GroupId.eq(groupID))
                    .buildDelete().executeDeleteWithoutDetachingEntities();
        }
    }

    /**
     * 同步接口,从server获取的群成员信息插入数据库,目前只有同步接口,如果需要可以加异步接口
     *
     * @param list    server获取的群组信息
     * @param groupID 群组ID
     * @return List<GroupMember> 群组成员列表
     */
    private List<GroupMember> addGroupMembers(final List<GetGroupMemberResponse.ResultEntity> list, final String groupID) {
        if (list != null && list.size() > 0) {
            List<GroupMember> groupsMembersList = setCreatedToTop(list, groupID);
            if (groupsMembersList != null && groupsMembersList.size() > 0) {
                for (GroupMember groupMember : groupsMembersList) {
                    if (groupMember != null && (groupMember.getPortraitUri() == null || TextUtils.isEmpty(groupMember.getPortraitUri().toString()))) {
                        Uri portrait = getPortrait(groupMember);
                        groupMember.setPortraitUri(portrait);
                    }
                }
                if (mGroupMemberDao != null) {
                    mGroupMemberDao.insertOrReplaceInTx(groupsMembersList);
                }
            }
            return groupsMembersList;
        } else {
            return null;
        }
    }

    public void addBlackList(final BlackList blackList) {
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mBlackListDao != null) {
                    mBlackListDao.insertOrReplace(blackList);
                }
            }
        });
    }

    /**
     * 同步接口,从server获取的黑名单信息插入数据库,目前只有同步接口,如果需要可以加异步接口
     *
     * @param responseList server获取的黑名单信息
     * @return List<BlackList> 黑名单成员列表
     */
    public List<BlackList> addBlackList(List<GetBlackListResponse.ResultEntity> responseList) {
        if (responseList != null && responseList.size() > 0) {
            List<BlackList> blackList = new ArrayList<>();
            for (GetBlackListResponse.ResultEntity black : responseList) {
                blackList.add(new BlackList(black.getUser().getId(),
                        null,
                        null
                ));
            }
            if (blackList.size() > 0) {
                if (mBlackListDao != null) {
                    mBlackListDao.insertOrReplaceInTx(blackList);
                }
            }
            return blackList;
        } else {
            return null;
        }
    }

    /**
     * 同步接口,获取全部好友信息
     *
     * @return List<Friend> 好友列表
     */
    public List<Friend> getFriends() {
        if (mFriendDao != null) {
            return mFriendDao.loadAll();
        } else {
            return null;
        }
    }

    /**
     * 异步接口,获取全部好友信息
     *
     * @param callback 获取好友信息的回调
     */
    public void getFriends(final ResultCallback<List<Friend>> callback) {
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                List<Friend> friendsList;
                if (!doingGetAllUserInfo && !hasGetFriends()) {
                    if (!isNetworkConnected()) {
                        onCallBackFail(callback);
                        return;
                    }
                    try {
                        friendsList = pullFriends();
                        sp.edit().putInt("getAllUserInfoState", mGetAllUserInfoState).commit();
                    } catch (HttpException e) {
                        onCallBackFail(callback);
                        NLog.d(TAG, "getFriends occurs HttpException e=" + e.toString() + "mGetAllUserInfoState=" + mGetAllUserInfoState);
                        return;
                    }
                } else {
                    friendsList = getFriends();
                }
                if (callback != null) {
                    callback.onCallback(friendsList);
                }
            }
        });
    }

    private List<Friend> syncGetFriends() {
        List<Friend> friendsList = null;
        if (!doingGetAllUserInfo && !hasGetFriends()) {
            if (!isNetworkConnected()) {
                return null;
            }
            try {
                friendsList = pullFriends();
                sp.edit().putInt("getAllUserInfoState", mGetAllUserInfoState).commit();
            } catch (HttpException e) {
                NLog.d(TAG, "getFriends occurs HttpException e=" + e.toString() + "mGetAllUserInfoState=" + mGetAllUserInfoState);
            }
        } else {
            friendsList = getFriends();
        }
        return friendsList;
    }

    /**
     * 同步获取群组列表
     *
     * @return List<Groups> 群组列表
     */
    public List<Groups> getGroups() {
        if (mGroupsDao != null) {
            return mGroupsDao.loadAll();
        } else {
            return null;
        }
    }

    /**
     * 获取群组列表
     *
     * @param callback 群组列表的回调
     */
    public void getGroups(final ResultCallback<List<Groups>> callback) {
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                List<Groups> groupsList;
                if (!doingGetAllUserInfo && !hasGetGroups()) {
                    if (!isNetworkConnected()) {
                        onCallBackFail(callback);
                    }
                    try {
                        groupsList = pullGroups();
                        sp.edit().putInt("getAllUserInfoState", mGetAllUserInfoState).commit();
                    } catch (HttpException e) {
                        onCallBackFail(callback);
                        NLog.d(TAG, "getGroups occurs HttpException e=" + e.toString() + "mGetAllUserInfoState=" + mGetAllUserInfoState);
                        return;
                    }
                } else {
                    groupsList = getGroups();
                }
                if (callback != null) {
                    callback.onCallback(groupsList);
                }
            }
        });
    }

    /**
     * 同步获取群组成员信息
     *
     * @param groupID 群组ID
     * @return List<GroupMember> 群组成员列表
     */
    public List<GroupMember> getGroupMembers(String groupID) {
        if (TextUtils.isEmpty(groupID)) {
            return null;
        } else {
            if (mGroupMemberDao != null) {
                return mGroupMemberDao.queryBuilder().
                        where(GroupMemberDao.Properties.GroupId.eq(groupID)).list();
            } else {
                return null;
            }
        }
    }

    /**
     * 同步获取群组成员信息
     *
     * @param userId 用户Id
     * @return List<GroupMember> 群组成员列表
     */
    public List<GroupMember> getGroupMembersWithUserId(String userId) {
        if (TextUtils.isEmpty(userId)) {
            return null;
        } else {
            if (mGroupMemberDao != null) {
                return mGroupMemberDao.queryBuilder().
                        where(GroupMemberDao.Properties.UserId.eq(userId)).list();
            } else {
                return null;
            }
        }
    }

    /**
     * 异步获取群组成员信息
     *
     * @param groupID  群组ID
     * @param callback 获取群组成员信息的回调
     */
    public void getGroupMembers(final String groupID, final ResultCallback<List<GroupMember>> callback) {
        if (TextUtils.isEmpty(groupID)) {
            if (callback != null) {
                callback.onError(null);
            }
        } else {
            mWorkHandler.post(new Runnable() {
                @Override
                public void run() {
                    List<GroupMember> groupMembersList;
                    if (!doingGetAllUserInfo && (!hasGetAllGroupMembers() || hasGetPartGroupMembers())) {
                        if (!isNetworkConnected()) {
                            onCallBackFail(callback);
                            return;
                        }
                        try {
                            groupMembersList = pullGroupMembers(groupID);
                            sp.edit().putInt("getAllUserInfoState", mGetAllUserInfoState).commit();
                        } catch (HttpException e) {
                            onCallBackFail(callback);
                            NLog.d(TAG, "getGroupMembers occurs HttpException e=" + e.toString() + "mGetAllUserInfoState=" + mGetAllUserInfoState);
                            return;
                        }
                    } else {
                        groupMembersList = getGroupMembers(groupID);
                    }
                    if (callback != null) {
                        callback.onCallback(groupMembersList);
                    }
                }
            });
        }
    }

    /**
     * 同步获取黑名单列表信息
     *
     * @return List<BlackList> 黑名单列表
     */
    private List<BlackList> getBlackList() {
        if (mBlackListDao != null) {
            return mBlackListDao.loadAll();
        } else {
            return null;
        }
    }

    /**
     * 异步获取黑名单信息
     *
     * @param callback 获取黑名单信息的回调
     */
    public void getBlackList(final ResultCallback<List<UserInfo>> callback) {
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                List<UserInfo> userInfoList = new ArrayList<>();
                List<Friend> friendsList = syncGetFriends();
                List<BlackList> blackList;
                if (!doingGetAllUserInfo && !hasGetBlackList()) {
                    if (!isNetworkConnected()) {
                        onCallBackFail(callback);
                        return;
                    }
                    try {
                        blackList = pullBlackList();
                        sp.edit().putInt("getAllUserInfoState", mGetAllUserInfoState).commit();
                    } catch (HttpException e) {
                        onCallBackFail(callback);
                        NLog.d(TAG, "getBlackList occurs HttpException e=" + e.toString() + "mGetAllUserInfoState=" + mGetAllUserInfoState);
                        return;
                    }
                } else {
                    blackList = getBlackList();
                }
                if (blackList != null && friendsList != null
                        && blackList.size() > 0
                        && friendsList.size() > 0) {
                    for (BlackList black : blackList) {
                        for (Friend friend : friendsList)
                            if (black.getUserId().equals(friend.getUserId())) {
                                userInfoList.add(new UserInfo(friend.getUserId(), friend.getName(), friend.getPortraitUri()));
                            }
                    }
                }
                if (callback != null) {
                    callback.onCallback(userInfoList);
                }
            }
        });
    }

    /**
     * 更新群组名称
     *
     * @param groupID   群组ID
     * @param groupName 新的群组名称
     */
    public void updateGroupsName(final String groupID, final String groupName) {
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                Groups group;
                if (mGroupsDao != null) {
                    group = mGroupsDao.load(groupID);
                } else {
                    return;
                }
                if (group != null) {
                    group.setName(groupName);
                    if (mGroupsDao != null) {
                        mGroupsDao.insertOrReplace(group);
                    }
                }
            }
        });
    }

    /**
     * 清除所有用户数据
     * 注意这是个同步函数,目前用到的也是同步场景
     */
    public void deleteAllUserInfo() {
        if (mFriendDao != null) {
            mFriendDao.deleteAll();
        }
        if (mGroupsDao != null) {
            mGroupsDao.deleteAll();
        }
        if (mGroupMemberDao != null) {
            mGroupMemberDao.deleteAll();
        }
        if (mBlackListDao != null) {
            mBlackListDao.deleteAll();
        }
    }

    private void syncDeleteFriends() {
        if (mFriendDao != null) {
            mFriendDao.deleteAll();
        }
    }

    public void deleteFriends() {
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mFriendDao != null) {
                    mFriendDao.deleteAll();
                }
            }
        });
    }

    private void syncDeleteGroups() {
        if (mGroupsDao != null) {
            mGroupsDao.deleteAll();
        }
    }

    public void deleteGroups() {
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mGroupsDao != null) {
                    mGroupsDao.deleteAll();
                }
            }
        });
    }

    public void deleteGroups(final Groups group) {
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mGroupsDao != null) {
                    mGroupsDao.delete(group);
                }
            }
        });
    }

    public void deleteGroupMembers() {
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mGroupMemberDao != null) {
                    mGroupMemberDao.deleteAll();
                }
            }
        });
    }

    /**
     * 删除群组成员,群成员被踢时使用
     *
     * @param groupID       群组ID
     * @param kickedUserIDs 被踢群成员userID
     * @return void
     */
    public void deleteGroupMembers(final String groupID, final List<String> kickedUserIDs) {
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                if (kickedUserIDs != null && kickedUserIDs.size() > 0) {
                    for (String userIDs : kickedUserIDs) {
                        if (mGroupMemberDao != null) {
                            mGroupMemberDao.queryBuilder()
                                    .where(GroupMemberDao.Properties.GroupId.eq(groupID),
                                            GroupMemberDao.Properties.UserId.eq(userIDs))
                                    .buildDelete().executeDeleteWithoutDetachingEntities();
                        }
                    }
                }
            }
        });
    }

    /**
     * 删除群组成员,退出群组,被踢,群组解散时会使用
     *
     * @param groupID 群组ID
     * @return void
     */
    public void deleteGroupMembers(final String groupID) {
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mGroupMemberDao != null) {
                    mGroupMemberDao.queryBuilder().where(GroupMemberDao.Properties.GroupId.eq(groupID))
                            .buildDelete().executeDeleteWithoutDetachingEntities();
                }
            }
        });
    }


    private void syncDeleteGroupMembers() {
        if (mGroupMemberDao != null) {
            mGroupMemberDao.deleteAll();
        }
    }

    public void deleteBlackList() {
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mBlackListDao != null) {
                    mBlackListDao.deleteAll();
                }
            }
        });
    }

    public void deleteBlackList(final String userID) {
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mBlackListDao != null) {
                    mBlackListDao.deleteByKey(userID);
                }
            }
        });
    }

    private void syncDeleteBlackList() {
        if (mBlackListDao != null) {
            mBlackListDao.deleteAll();
        }
    }

    public boolean isFriendsRelationship(String userID) {
        if (TextUtils.isEmpty(userID))
            return false;
        else
            return getFriendByID(userID) != null;
    }

    /**
     * 同步接口,获取1个好友信息
     *
     * @param userID 好友ID
     * @return Friend 好友信息
     */
    public Friend getFriendByID(String userID) {
        if (TextUtils.isEmpty(userID)) {
            return null;
        } else {
            if (mFriendDao != null) {
                return mFriendDao.queryBuilder().where(FriendDao.Properties.UserId.eq(userID)).unique();
            } else {
                return null;
            }
        }
    }

    /**
     * 异步接口,获取1个好友信息
     *
     * @param userID   好友ID
     * @param callback 获取好友信息回调
     */
    public void getFriendByID(final String userID, final ResultCallback<Friend> callback) {
        if (TextUtils.isEmpty(userID)) {
            if (callback != null)
                callback.onError(null);
        } else {
            mWorkHandler.post(new Runnable() {
                @Override
                public void run() {
                    Friend friend = null;
                    if (mFriendDao != null) {
                        friend = mFriendDao.queryBuilder().where(FriendDao.Properties.UserId.eq(userID)).unique();
                    }
                    if (callback != null) {
                        if (friend != null) {
                            callback.onCallback(friend);
                        } else {
                            callback.onFail("Appointed UserInfo does not existed.");
                        }
                    }
                }
            });
        }
    }

    /**
     * 同步接口,获取1个群组信息
     *
     * @param groupID 群组ID
     * @return Groups 群组信息
     */
    public Groups getGroupsByID(String groupID) {
        if (TextUtils.isEmpty(groupID)) {
            return null;
        } else {
            if (mGroupsDao != null) {
                return mGroupsDao.load(groupID);
            } else {
                return null;
            }
        }
    }

    /**
     * 异步接口,获取1个群组信息
     *
     * @param groupID  群组ID
     * @param callback 获取群组信息回调
     */
    public void getGroupsByID(final String groupID, final ResultCallback<Groups> callback) {
        if (TextUtils.isEmpty(groupID)) {
            if (callback != null) {
                callback.onError(null);
            }
        } else {
            mWorkHandler.post(new Runnable() {
                @Override
                public void run() {
                    Groups group;
                    if (!doingGetAllUserInfo && !hasGetGroups()) {
                        if (isNetworkConnected()) {
                            try {
                                group = pullGroups(groupID);
                                sp.edit().putInt("getAllUserInfoState", mGetAllUserInfoState).commit();
                            } catch (HttpException e) {
                                onCallBackFail(callback);
                                NLog.d(TAG, "getGroupsByID occurs HttpException e=" + e.toString() + "mGetAllUserInfoState=" + mGetAllUserInfoState);
                                return;
                            }
                        } else {
                            onCallBackFail(callback);
                            return;
                        }
                    } else {
                        group = getGroupsByID(groupID);
                    }
                    if (callback != null) {
                        callback.onCallback(group);
                    }
                }
            });
        }
    }

    /**
     * 发送请求（需要检查网络）
     *
     * @param requestCode 请求码
     */
    private void request(int requestCode) {
        if (mAsyncTaskManager != null) {
            mAsyncTaskManager.request(requestCode, this);
        }
    }

    /**
     * 发送请求
     *
     * @param requestCode    请求码
     * @param isCheckNetwork 是否需检查网络，true检查，false不检查
     */
    public void request(int requestCode, boolean isCheckNetwork) {
        if (mAsyncTaskManager != null) {
            mAsyncTaskManager.request(requestCode, isCheckNetwork, this);
        }
    }

    /**
     * 取消所有请求
     */
    public void cancelRequest() {
        if (mAsyncTaskManager != null) {
            mAsyncTaskManager.cancelRequest();
        }
    }

    @Override
    public Object doInBackground(int requestCode, String parameter) throws HttpException {
        switch (requestCode) {
            case GET_TOKEN:
                return action.getToken();
        }
        return null;
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            if (requestCode == GET_TOKEN) {
                GetTokenResponse tokenResponse = (GetTokenResponse) result;
                if (tokenResponse.getCode() == 200) {
                    String token = tokenResponse.getResult().getToken();
                    SharedPreferences sp = mContext.getSharedPreferences("config", Context.MODE_PRIVATE);
                    sp.edit().putString("loginToken", token).commit();
                    if (!TextUtils.isEmpty(token)) {
                        RongIM.connect(token, new RongIMClient.ConnectCallback() {
                            @Override
                            public void onTokenIncorrect() {
                                Log.e(TAG, "reToken still Incorrect");
                            }

                            @Override
                            public void onSuccess(String s) {
                                SharedPreferences sp = mContext.getSharedPreferences("config", Context.MODE_PRIVATE);
                                sp.edit().putString(SealConst.SEALTALK_LOGIN_ID, s).commit();
                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode e) {
                                Log.e(TAG, "reToken occur error ErrorCode =" + e);
                            }
                        });
                    }
                }
            }
        }
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
    }

    public void reGetToken() {
        request(GET_TOKEN);
    }

    /**
     * 泛型类，主要用于 API 中功能的回调处理。
     *
     * @param <T> 声明一个泛型 T。
     */
    public static abstract class ResultCallback<T> {

        public static class Result<T> {
            public T t;
        }

        public ResultCallback() {

        }

        /**
         * 成功时回调。
         *
         * @param t 已声明的类型。
         */
        public abstract void onSuccess(T t);

        /**
         * 错误时回调。
         *
         * @param errString 错误提示
         */
        public abstract void onError(String errString);


        public void onFail(final String errString) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onError(errString);
                }
            });
        }

        public void onCallback(final T t) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onSuccess(t);
                }
            });
        }
    }

    private List<GroupMember> setCreatedToTop(List<GetGroupMemberResponse.ResultEntity> groupMember, String groupID) {
        List<GroupMember> newList = new ArrayList<>();
        GroupMember created = null;
        for (GetGroupMemberResponse.ResultEntity group : groupMember) {
            String groupName = null;
            String groupPortraitUri = null;
            Groups groups = getGroupsByID(groupID);
            if (groups != null) {
                groupName = groups.getName();
                groupPortraitUri = groups.getPortraitUri();
            }
            GroupMember newMember = new GroupMember(groupID,
                    group.getUser().getId(),
                    group.getUser().getNickname(),
                    Uri.parse(group.getUser().getPortraitUri()),
                    group.getDisplayName(),
                    CharacterParser.getInstance().getSpelling(group.getUser().getNickname()),
                    CharacterParser.getInstance().getSpelling(group.getDisplayName()),
                    groupName,
                    CharacterParser.getInstance().getSpelling(groupName),
                    groupPortraitUri);
            if (group.getRole() == 0) {
                created = newMember;
            } else {
                newList.add(newMember);
            }
        }
        if (created != null) {
            newList.add(created);
        }
        Collections.reverse(newList);
        return newList;
    }

    private boolean needGetAllUserInfo() {
        return mGetAllUserInfoState == NONE;
    }

    private boolean hasGetAllUserInfo() {
        return mGetAllUserInfoState == ALL;
    }

    private boolean hasGetFriends() {
        return (mGetAllUserInfoState & FRIEND) != 0;
    }

    private boolean hasGetGroups() {
        return (mGetAllUserInfoState & GROUPS) != 0;
    }

    private boolean hasGetAllGroupMembers() {
        return ((mGetAllUserInfoState & GROUPMEMBERS) != 0)
                && ((mGetAllUserInfoState & PARTGROUPMEMBERS) == 0);
    }

    private boolean hasGetPartGroupMembers() {
        return ((mGetAllUserInfoState & GROUPMEMBERS) == 0)
                && ((mGetAllUserInfoState & PARTGROUPMEMBERS) != 0);
    }

    private boolean hasGetBlackList() {
        return (mGetAllUserInfoState & BLACKLIST) != 0;
    }

    private void setGetAllUserInfoWithPartGroupMembersState() {
        mGetAllUserInfoState &= ~GROUPMEMBERS;
        mGetAllUserInfoState |= PARTGROUPMEMBERS;
    }

    private void setGetAllUserInfoWtihAllGroupMembersState() {
        mGetAllUserInfoState &= ~PARTGROUPMEMBERS;
        mGetAllUserInfoState |= GROUPMEMBERS;
    }

    private void onCallBackFail(ResultCallback<?> callback) {
        if (callback != null) {
            callback.onFail(null);
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }

    /**
     * app中获取用户头像的接口,此前这部分调用分散在app显示头像的每处代码中,整理写一个方法使app代码更整洁
     * 这个方法不涉及读数据库,头像空时直接生成默认头像
     */
    public String getPortraitUri(UserInfo userInfo) {
        if (userInfo != null) {
            if (userInfo.getPortraitUri() != null) {
                if (TextUtils.isEmpty(userInfo.getPortraitUri().toString())) {
                    if (userInfo.getName() != null) {
                        return RongGenerate.generateDefaultAvatar(userInfo);
                    } else {
                        return null;
                    }
                } else {
                    return userInfo.getPortraitUri().toString();
                }
            } else {
                if (userInfo.getName() != null) {
                    return RongGenerate.generateDefaultAvatar(userInfo);
                } else {
                    return null;
                }
            }

        }
        return null;
    }

    public String getPortraitUri(UserInfoBean bean) {
        if (bean != null) {
            if (bean.getPortraitUri() != null) {
                if (TextUtils.isEmpty(bean.getPortraitUri().toString())) {
                    if (bean.getName() != null) {
                        return RongGenerate.generateDefaultAvatar(bean.getName(), bean.getUserId());
                    } else {
                        return null;
                    }
                } else {
                    return bean.getPortraitUri().toString();
                }
            } else {
                if (bean.getName() != null) {
                    return RongGenerate.generateDefaultAvatar(bean.getName(), bean.getUserId());
                } else {
                    return null;
                }
            }

        }
        return null;
    }

    public String getPortraitUri(GetGroupInfoResponse groupInfoResponse) {
        if (groupInfoResponse.getResult() != null) {
            Groups groups = new Groups(groupInfoResponse.getResult().getId(),
                    groupInfoResponse.getResult().getName(),
                    groupInfoResponse.getResult().getPortraitUri());
            return getPortraitUri(groups);
        }
        return null;
    }

    /**
     * 获取用户头像,头像为空时会生成默认的头像,此默认头像可能已经存在数据库中,不重新生成
     * 先从缓存读,再从数据库读
     */
    private String getPortrait(Friend friend) {
        if (friend != null) {
            if (friend.getPortraitUri() == null || TextUtils.isEmpty(friend.getPortraitUri().toString())) {
                if (TextUtils.isEmpty(friend.getUserId())) {
                    return null;
                } else {
                    UserInfo userInfo = mUserInfoCache.get(friend.getUserId());
                    if (userInfo != null) {
                        if (userInfo.getPortraitUri() != null && !TextUtils.isEmpty(userInfo.getPortraitUri().toString())) {
                            return userInfo.getPortraitUri().toString();
                        } else {
                            mUserInfoCache.remove(friend.getUserId());
                        }
                    }
                    List<GroupMember> groupMemberList = getGroupMembersWithUserId(friend.getUserId());
                    if (groupMemberList != null && groupMemberList.size() > 0) {
                        GroupMember groupMember = groupMemberList.get(0);
                        if (groupMember.getPortraitUri() != null && !TextUtils.isEmpty(groupMember.getPortraitUri().toString()))
                            return groupMember.getPortraitUri().toString();
                    }
                    String portrait = RongGenerate.generateDefaultAvatar(friend.getName(), friend.getUserId());
                    //缓存信息kit会使用,备注名存在时需要缓存displayName
                    String name = friend.getName();
                    if (friend.isExitsDisplayName()) {
                        name = friend.getDisplayName();
                    }
                    userInfo = new UserInfo(friend.getUserId(), name, Uri.parse(portrait));
                    mUserInfoCache.put(friend.getUserId(), userInfo);
                    return portrait;
                }
            } else {
                return friend.getPortraitUri().toString();
            }
        }
        return null;
    }

    private Uri getPortrait(GroupMember groupMember) {
        if (groupMember != null) {
            if (groupMember.getPortraitUri() == null || TextUtils.isEmpty(groupMember.getPortraitUri().toString())) {
                if (TextUtils.isEmpty(groupMember.getUserId())) {
                    return null;
                } else {
                    UserInfo userInfo = mUserInfoCache.get(groupMember.getUserId());
                    if (userInfo != null) {
                        if (userInfo.getPortraitUri() != null && !TextUtils.isEmpty(userInfo.getPortraitUri().toString())) {
                            return userInfo.getPortraitUri();
                        } else {
                            mUserInfoCache.remove(groupMember.getUserId());
                        }
                    }
                    Friend friend = getFriendByID(groupMember.getUserId());
                    if (friend != null) {
                        if (friend.getPortraitUri() != null && !TextUtils.isEmpty(friend.getPortraitUri().toString())) {
                            return friend.getPortraitUri();
                        }
                    }
                    List<GroupMember> groupMemberList = getGroupMembersWithUserId(groupMember.getUserId());
                    if (groupMemberList != null && groupMemberList.size() > 0) {
                        GroupMember member = groupMemberList.get(0);
                        if (member.getPortraitUri() != null && !TextUtils.isEmpty(member.getPortraitUri().toString())) {
                            return member.getPortraitUri();
                        }
                    }
                    String portrait = RongGenerate.generateDefaultAvatar(groupMember.getName(), groupMember.getUserId());
                    if (!TextUtils.isEmpty(portrait)) {
                        userInfo = new UserInfo(groupMember.getUserId(), groupMember.getName(), Uri.parse(portrait));
                        mUserInfoCache.put(groupMember.getUserId(), userInfo);
                        return Uri.parse(portrait);
                    } else {
                        return null;
                    }
                }
            } else {
                return groupMember.getPortraitUri();
            }
        }
        return null;
    }
}
