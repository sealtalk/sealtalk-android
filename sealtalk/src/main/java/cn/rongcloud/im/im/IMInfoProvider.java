package cn.rongcloud.im.im;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.ThreadManager;
import cn.rongcloud.im.db.DBManager;
import cn.rongcloud.im.db.dao.GroupDao;
import cn.rongcloud.im.db.dao.GroupMemberDao;
import cn.rongcloud.im.db.model.FriendDetailInfo;
import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.db.model.FriendStatus;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.db.model.GroupExitedMemberInfo;
import cn.rongcloud.im.db.model.GroupNoticeInfo;
import cn.rongcloud.im.db.model.UserInfo;
import cn.rongcloud.im.model.GetPokeResult;
import cn.rongcloud.im.model.GroupMember;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.task.FriendTask;
import cn.rongcloud.im.task.GroupTask;
import cn.rongcloud.im.task.UserTask;
import io.rong.contactcard.IContactCardInfoProvider;
import io.rong.imkit.RongIM;
import io.rong.imkit.feature.mention.RongMentionManager;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imkit.utils.CharacterParser;
import io.rong.imkit.utils.RongUtils;

public class IMInfoProvider {
    private final MediatorLiveData<Resource> triggerLiveData = new MediatorLiveData<>(); // 同步信息时用于触发事件使用的变量
    private volatile Observer<Resource> emptyObserver;// 空监听用于触发事件
    private volatile boolean groupMemberIsRequest;
    private GroupTask groupTask;
    private UserTask userTask;
    private FriendTask friendTask;
    private DBManager dbManager;

    public IMInfoProvider() {
    }

    public void init(Context context) {
        initTask(context);
        initInfoProvider(context);
        initData();
        dbManager = DBManager.getInstance(context);
    }

    /**
     * 初始化同步数据时使用的任务对象
     */
    private void initTask(Context context) {
        groupTask = new GroupTask(context.getApplicationContext());
        friendTask = new FriendTask(context.getApplicationContext());
        userTask = new UserTask(context.getApplicationContext());
        emptyObserver = resource -> {
            /*
             * 添加此监听只为触发 LiveData 的 onActive 行为，使其他事件可以执行
             * 此处不做更新操作，信息在存储到数据库时会同步更新
             */
        };
        triggerLiveData.observeForever(emptyObserver);
    }

    /**
     * 初始化信息提供者，包括用户信息，群组信息，群主成员信息
     */
    private void initInfoProvider(Context context) {
        // 获取用户信息
        RongUserInfoManager.getInstance().setUserInfoProvider(id -> {
            if(id.equals("__group_apply__")) {
                return new io.rong.imlib.model.UserInfo("__group_apply__", context.getResources().getString(R.string.seal_conversation_notification_group),
                        RongUtils.getUriFromDrawableRes(context, R.drawable.seal_group_notice_portrait));
            } else {
                updateUserInfo(id);
            }
            return null;
        }, true);

        // 获取群组信息
        RongUserInfoManager.getInstance().setGroupInfoProvider(id -> {
            updateGroupInfo(id);
            updateGroupMember(id);
            return null;
        }, true);

        // 获取群组单一成员信息
        RongUserInfoManager.getInstance().setGroupUserInfoProvider((gid, uid) -> {
            // 直接进行全部组内成员获取
            updateGroupMember(gid);
            return null;
        }, true);

        // 设置群组内成员
        //'@' 功能和VoIP功能在选人界面,需要知道群组内成员信息,开发者需要设置该提供者。
        RongIM.getInstance().setGroupMembersProvider((gid, callback) -> {
            updateIMGroupMember(gid, callback);
        });


        // RongCallkit 设置 成员信息
        //TOdo
//        RongCallKit.setGroupMemberProvider((groupId, result) -> {
//            updateCallGroupMember(groupId, result);
//            return null;
//        });

    }

    private void initData() {
        refreshReceivePokeMessageStatus();
    }

    /**
     * 更新用户信息
     *
     * @param userId
     */
    public void updateUserInfo(String userId) {
        ThreadManager.getInstance().runOnUIThread(() -> {
            LiveData<Resource<UserInfo>> userSource = userTask.getUserInfo(userId);
            triggerLiveData.addSource(userSource, resource -> {
                if (resource.status == Status.SUCCESS || resource.status == Status.ERROR) {
                    // 确认成功或失败后，移除数据源
                    // 在请求成功后，会在插入数据时同步更新缓存
                    triggerLiveData.removeSource(userSource);
                }
            });
        });
    }

    /**
     * 更新群组信息
     *
     * @param groupId
     */
    public void updateGroupInfo(String groupId) {
        ThreadManager.getInstance().runOnUIThread(() -> {
            LiveData<Resource<GroupEntity>> groupSource = groupTask.getGroupInfo(groupId);
            triggerLiveData.addSource(groupSource, resource -> {
                if (resource.status == Status.SUCCESS || resource.status == Status.ERROR) {
                    // 确认成功或失败后，移除数据源
                    // 在请求成功后，会在插入数据时同步更新缓存
                    triggerLiveData.removeSource(groupSource);
                }
            });
        });
    }

    /**
     * 更新群组成员
     *
     * @param groupId
     */
    public void updateGroupMember(String groupId) {
        ThreadManager.getInstance().runOnUIThread(() -> {
            // 考虑到在群内频繁调用此方法,当有请求时不进行请求
            if (groupMemberIsRequest) return;

            groupMemberIsRequest = true;
            LiveData<Resource<List<GroupMember>>> groupMemberSource = groupTask.getGroupMemberInfoList(groupId);
            triggerLiveData.addSource(groupMemberSource, resource -> {
                if (resource.status == Status.SUCCESS || resource.status == Status.ERROR) {
                    // 确认成功或失败后，移除数据源
                    // 在请求成功后，会在插入数据时同步更新缓存
                    triggerLiveData.removeSource(groupMemberSource);
                    groupMemberIsRequest = false;
                }
            });
        });
    }

    /**
     * 请求更新 IM 中群组成员
     *
     * @param groupId
     * @param callback
     */
    private void updateIMGroupMember(String groupId, RongMentionManager.IGroupMemberCallback callback) {
        ThreadManager.getInstance().runOnUIThread(() -> {
            // 考虑到在群内频繁调用此方法,当有请求时进行请求
            if (groupMemberIsRequest) return;

            groupMemberIsRequest = true;
            LiveData<Resource<List<GroupMember>>> groupMemberSource = groupTask.getGroupMemberInfoList(groupId);
            triggerLiveData.addSource(groupMemberSource, resource -> {
                if (resource.status == Status.SUCCESS || resource.status == Status.ERROR) {
                    // 确认成功或失败后，移除数据源
                    // 在请求成功后，会在插入数据时同步更新缓存
                    triggerLiveData.removeSource(groupMemberSource);
                    groupMemberIsRequest = false;

                }

                if (resource.status == Status.SUCCESS && resource.data != null) {
                    List<GroupMember> data = resource.data;
                    List<io.rong.imlib.model.UserInfo> userInfoList = new ArrayList<>();
                    for (GroupMember member : data) {
                        String name = member.getGroupNickName();
                        if (TextUtils.isEmpty(name)) {
                            name = member.getName();
                        }

                        io.rong.imlib.model.UserInfo info = new io.rong.imlib.model.UserInfo(member.getUserId(), name, Uri.parse(member.getPortraitUri()));
                        userInfoList.add(info);
                    }
                    callback.onGetGroupMembersResult(userInfoList);
                }
            });
        });
    }

    /**
     * 请求音视频中更新群组成员
     *
     * @param groupId
     * @param result
     */
    //todo
//    private void updateCallGroupMember(String groupId, RongCallKit.OnGroupMembersResult result) {
//        ThreadManager.getInstance().runOnUIThread(() -> {
//            // 考虑到在群内频繁调用此方法,当有请求时进行请求
//            if (groupMemberIsRequest) return;
//
//            groupMemberIsRequest = true;
//            LiveData<Resource<List<GroupMember>>> groupMemberSource = groupTask.getGroupMemberInfoList(groupId);
//            triggerLiveData.addSource(groupMemberSource, resource -> {
//                if (resource.status == Status.SUCCESS || resource.status == Status.ERROR) {
//                    // 确认成功或失败后，移除数据源
//                    // 在请求成功后，会在插入数据时同步更新缓存
//                    triggerLiveData.removeSource(groupMemberSource);
//                    groupMemberIsRequest = false;
//
//                }
//
//                if (resource.status == Status.SUCCESS && resource.data != null && result != null) {
//                    List<GroupMember> data = resource.data;
//                    ArrayList<String> userInfoIdList = new ArrayList<>();
//                    for (GroupMember member : data) {
//                        userInfoIdList.add(member.getUserId());
//                    }
//                    result.onGotMemberList(userInfoIdList);
//                }
//            });
//        });
//    }

    /**
     * 请求更新好友信息
     *
     * @param friendId
     */
    public void updateFriendInfo(String friendId) {
        ThreadManager.getInstance().runOnUIThread(() -> {
            LiveData<Resource<FriendShipInfo>> friendInfo = friendTask.getFriendInfo(friendId);
            triggerLiveData.addSource(friendInfo, resource -> {
                if (resource.status == Status.SUCCESS || resource.status == Status.ERROR) {
                    // 确认成功或失败后，移除数据源
                    // 在请求成功后，会在插入数据时同步更新缓存
                    triggerLiveData.removeSource(friendInfo);
                }
            });
        });
    }

    /**
     * 获取联系人列表
     *
     * @param contactInfoCallback
     */
    public void getAllContactUserInfo(IContactCardInfoProvider.IContactCardInfoCallback contactInfoCallback) {
        ThreadManager.getInstance().runOnUIThread(() -> {
            LiveData<Resource<List<FriendShipInfo>>> allFriends = friendTask.getAllFriends();
            triggerLiveData.addSource(allFriends, resource -> {
                if (resource.status == Status.SUCCESS || resource.status == Status.ERROR) {
                    // 确认成功或失败后，移除数据源
                    triggerLiveData.removeSource(allFriends);
                    List<FriendShipInfo> friendShipInfoList = resource.data;
                    List<io.rong.imlib.model.UserInfo> userInfoList = new ArrayList<>();
                    if (friendShipInfoList != null) {
                        for (FriendShipInfo info : friendShipInfoList) {
                            if (info.getStatus() != FriendStatus.IS_FRIEND.getStatusCode()) {
                                continue;
                            }
                            FriendDetailInfo friendUser = info.getUser();
                            if (friendUser != null) {
                                io.rong.imlib.model.UserInfo user = new io.rong.imlib.model.UserInfo(friendUser.getId(), friendUser.getNickname(), Uri.parse(friendUser.getPortraitUri()));
                                if (!TextUtils.isEmpty(info.getDisplayName())) {
                                    JsonObject jsonObject = new JsonObject();
                                    jsonObject.addProperty("displayName", info.getDisplayName());
                                    user.setExtra(jsonObject.toString());
                                }
                                userInfoList.add(user);
                            }
                        }
                    }
                    contactInfoCallback.getContactCardInfoCallback(userInfoList);
                }
            });
        });
    }

    /**
     * 获取单一用户联系人
     *
     * @param userId
     * @param contactInfoCallback
     */
    public void getContactUserInfo(String userId, IContactCardInfoProvider.IContactCardInfoCallback contactInfoCallback) {
        ThreadManager.getInstance().runOnUIThread(() -> {
            LiveData<Resource<FriendShipInfo>> friendInfo = friendTask.getFriendInfo(userId);
            triggerLiveData.addSource(friendInfo, resource -> {
                if (resource.status == Status.SUCCESS || resource.status == Status.ERROR) {
                    // 确认成功或失败后，移除数据源
                    triggerLiveData.removeSource(friendInfo);
                    FriendShipInfo data = resource.data;
                    List<io.rong.imlib.model.UserInfo> userInfoList = new ArrayList<>();
                    if (data != null) {
                        FriendDetailInfo friendUser = data.getUser();
                        if (friendUser != null) {
                            io.rong.imlib.model.UserInfo user = new io.rong.imlib.model.UserInfo(friendUser.getId(), friendUser.getNickname(), Uri.parse(friendUser.getPortraitUri()));
                            userInfoList.add(user);
                        }
                    }
                    contactInfoCallback.getContactCardInfoCallback(userInfoList);
                }
            });
        });
    }

    /**
     * 刷新群通知信息
     */
    public void refreshGroupNotideInfo() {
        ThreadManager.getInstance().runOnUIThread(() -> {
            LiveData<Resource<List<GroupNoticeInfo>>> groupNoticeInfo = groupTask.getGroupNoticeInfo();
            triggerLiveData.addSource(groupNoticeInfo, resource -> {
                if (resource.status == Status.SUCCESS || resource.status == Status.ERROR) {
                    // 确认成功或失败后，移除数据源
                    triggerLiveData.removeSource(groupNoticeInfo);
                }
            });
        });
    }

    /**
     * 刷新退群列表
     */
    public void refreshGroupExitedInfo(String groupId) {
        ThreadManager.getInstance().runOnUIThread(() -> {
            LiveData<Resource<List<GroupExitedMemberInfo>>> groupExitedInfo = groupTask.getGroupExitedMemberInfo(groupId);
            triggerLiveData.addSource(groupExitedInfo, resource -> {
                if (resource.status == Status.SUCCESS || resource.status == Status.ERROR) {
                    // 确认成功或失败后，移除数据源
                    triggerLiveData.removeSource(groupExitedInfo);
                }
            });
        });
    }

    /**
     * 更新数据库中群组名称
     *
     * @param groupId
     * @param groupName
     */
    public void updateGroupNameInDb(String groupId, String groupName) {
        ThreadManager.getInstance().runOnWorkThread(() -> {
            GroupDao groupDao = dbManager.getGroupDao();
            if (groupDao != null) {
                int updateResult = groupDao.updateGroupName(groupId, groupName, CharacterParser.getInstance().getSelling(groupName));

                // 更新成时同时更新缓存
                if (updateResult > 0) {
                    GroupEntity groupInfo = groupDao.getGroupInfoSync(groupId);
                    if (groupInfo != null) {
                        IMManager.getInstance().updateGroupInfoCache(groupId, groupName, Uri.parse(groupInfo.getPortraitUri()));
                    }
                }
            }
        });
    }

    /**
     * 删除数据库中群组及对应的群组成员
     *
     * @param groupId
     */
    public void deleteGroupInfoInDb(String groupId) {
        ThreadManager.getInstance().runOnWorkThread(() -> {
            GroupDao groupDao = dbManager.getGroupDao();
            if (groupDao != null) {
                groupDao.deleteGroup(groupId);
            }
            GroupMemberDao groupMemberDao = dbManager.getGroupMemberDao();
            if (groupMemberDao != null) {
                groupMemberDao.deleteGroupMember(groupId);
            }
        });
    }

    /**
     * 获取群组成员信息，当没有群昵称时使用原用户名，而不是备注名
     *
     * @param groupId
     * @param targetId
     * @return
     */
    public io.rong.imlib.model.UserInfo getGroupMemberUserInfo(String groupId, String targetId) {
        GroupMember groupMember = dbManager.getGroupMemberDao().getGroupMemberInfoSync(groupId, targetId);
        io.rong.imlib.model.UserInfo userInfo = null;
        if (groupMember != null) {
            String groupMemberName = TextUtils.isEmpty(groupMember.getGroupNickName()) ? groupMember.getName() : groupMember.getGroupNickName();
            userInfo = new io.rong.imlib.model.UserInfo(groupMember.getUserId(), groupMemberName,
                    Uri.parse(groupMember.getPortraitUri()));
        }
        return userInfo;
    }

    /**
     * 获取接收戳一下消息状态
     */
    public void refreshReceivePokeMessageStatus() {
        ThreadManager.getInstance().runOnUIThread(() -> {
            LiveData<Resource<GetPokeResult>> receivePokeMessageState = userTask.getReceivePokeMessageState();
            triggerLiveData.addSource(receivePokeMessageState, resource -> {
                if (resource.status != Status.LOADING) {
                    triggerLiveData.removeSource(receivePokeMessageState);
                }
            });
        });
    }

}
