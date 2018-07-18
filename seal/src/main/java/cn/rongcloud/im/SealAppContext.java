package cn.rongcloud.im;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.view.View;

import com.alibaba.fastjson.JSONException;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.db.Friend;
import cn.rongcloud.im.db.GroupMember;
import cn.rongcloud.im.db.Groups;
import cn.rongcloud.im.message.module.SealExtensionModule;
import cn.rongcloud.im.server.broadcast.BroadcastManager;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.pinyin.CharacterParser;
import cn.rongcloud.im.server.response.ContactNotificationMessageData;
import cn.rongcloud.im.server.utils.NLog;
import cn.rongcloud.im.server.utils.json.JsonMananger;
import cn.rongcloud.im.ui.activity.LoginActivity;
import cn.rongcloud.im.ui.activity.MainActivity;
import cn.rongcloud.im.ui.activity.NewFriendListActivity;
import cn.rongcloud.im.ui.activity.UserDetailActivity;
import io.rong.calllib.RongCallClient;
import io.rong.calllib.RongCallSession;
import io.rong.imkit.DefaultExtensionModule;
import io.rong.imkit.IExtensionModule;
import io.rong.imkit.RongExtensionManager;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.GroupNotificationMessageData;
import io.rong.imkit.model.GroupUserInfo;
import io.rong.imkit.model.UIConversation;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UserInfo;
import io.rong.message.ContactNotificationMessage;
import io.rong.message.GroupNotificationMessage;
import io.rong.message.ImageMessage;

/**
 * 融云相关监听 事件集合类
 * Created by AMing on 16/1/7.
 * Company RongCloud
 */
public class SealAppContext implements RongIM.ConversationListBehaviorListener,
        RongIMClient.OnReceiveMessageListener,
        RongIM.UserInfoProvider,
        RongIM.GroupInfoProvider,
        RongIM.GroupUserInfoProvider,
        RongIM.LocationProvider,
        RongIMClient.ConnectionStatusListener,
        RongIM.ConversationBehaviorListener,
        RongIM.IGroupMembersProvider {

    private static final int CLICK_CONVERSATION_USER_PORTRAIT = 1;


    private final static String TAG = "SealAppContext";
    public static final String UPDATE_FRIEND = "update_friend";
    public static final String UPDATE_RED_DOT = "update_red_dot";
    public static final String UPDATE_GROUP_NAME = "update_group_name";
    public static final String UPDATE_GROUP_MEMBER = "update_group_member";
    public static final String GROUP_DISMISS = "group_dismiss";

    private Context mContext;

    private static SealAppContext mRongCloudInstance;

    private RongIM.LocationProvider.LocationCallback mLastLocationCallback;

    private static ArrayList<Activity> mActivities;

    public SealAppContext(Context mContext) {
        this.mContext = mContext;
        initListener();
        mActivities = new ArrayList<>();
        SealUserInfoManager.init(mContext);
    }

    /**
     * 初始化 RongCloud.
     *
     * @param context 上下文。
     */
    public static void init(Context context) {

        if (mRongCloudInstance == null) {
            synchronized (SealAppContext.class) {

                if (mRongCloudInstance == null) {
                    mRongCloudInstance = new SealAppContext(context);
                }
            }
        }

    }

    /**
     * 获取RongCloud 实例。
     *
     * @return RongCloud。
     */
    public static SealAppContext getInstance() {
        return mRongCloudInstance;
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * init 后就能设置的监听
     */
    private void initListener() {
        RongIM.setConversationBehaviorListener(this);//设置会话界面操作的监听器。
        RongIM.setConversationListBehaviorListener(this);
        RongIM.setConnectionStatusListener(this);
        RongIM.setUserInfoProvider(this, true);
        RongIM.setGroupInfoProvider(this, true);
        RongIM.setLocationProvider(this);//设置地理位置提供者,不用位置的同学可以注掉此行代码
        setInputProvider();
        //setUserInfoEngineListener();//移到SealUserInfoManager
        setReadReceiptConversationType();
        RongIM.getInstance().enableNewComingMessageIcon(true);
        RongIM.getInstance().enableUnreadMessageIcon(true);
        RongIM.getInstance().setGroupMembersProvider(this);
        //RongIM.setGroupUserInfoProvider(this, true);//seal app暂时未使用这种方式,目前使用UserInfoProvider
        BroadcastManager.getInstance(mContext).addAction(SealConst.EXIT, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                quit(false);
            }
        });
    }

    private void setReadReceiptConversationType() {
        Conversation.ConversationType[] types = new Conversation.ConversationType[]{
                Conversation.ConversationType.PRIVATE,
                Conversation.ConversationType.GROUP,
                Conversation.ConversationType.DISCUSSION
        };
        RongIM.getInstance().setReadReceiptConversationTypeList(types);
    }

    private void setInputProvider() {
        RongIM.setOnReceiveMessageListener(this);

        List<IExtensionModule> moduleList = RongExtensionManager.getInstance().getExtensionModules();
        IExtensionModule defaultModule = null;
        if (moduleList != null) {
            for (IExtensionModule module : moduleList) {
                if (module instanceof DefaultExtensionModule) {
                    defaultModule = module;
                    break;
                }
            }
            if (defaultModule != null) {
                RongExtensionManager.getInstance().unregisterExtensionModule(defaultModule);
                RongExtensionManager.getInstance().registerExtensionModule(new SealExtensionModule());
            }
        }
    }

    @Override
    public boolean onConversationPortraitClick(Context context, Conversation.ConversationType conversationType, String s) {
        return false;
    }

    @Override
    public boolean onConversationPortraitLongClick(Context context, Conversation.ConversationType conversationType, String s) {
        return false;
    }

    @Override
    public boolean onConversationLongClick(Context context, View view, UIConversation uiConversation) {
        return false;
    }

    @Override
    public boolean onConversationClick(Context context, View view, UIConversation uiConversation) {
        MessageContent messageContent = uiConversation.getMessageContent();
        if (messageContent instanceof ContactNotificationMessage) {
            ContactNotificationMessage contactNotificationMessage = (ContactNotificationMessage) messageContent;
            if (contactNotificationMessage.getOperation().equals("AcceptResponse")) {
                // 被加方同意请求后
                if (contactNotificationMessage.getExtra() != null) {
                    ContactNotificationMessageData bean = null;
                    try {
                        bean = JsonMananger.jsonToBean(contactNotificationMessage.getExtra(), ContactNotificationMessageData.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    RongIM.getInstance().startPrivateChat(context, uiConversation.getConversationSenderId(), bean.getSourceUserNickname());

                }
            } else {
                context.startActivity(new Intent(context, NewFriendListActivity.class));
            }
            return true;
        }
        return false;
    }

    /**
     * @param message 收到的消息实体。
     * @param i
     * @return
     */
    @Override
    public boolean onReceived(Message message, int i) {
        MessageContent messageContent = message.getContent();
        if (messageContent instanceof ContactNotificationMessage) {
            ContactNotificationMessage contactNotificationMessage = (ContactNotificationMessage) messageContent;
            if (contactNotificationMessage.getOperation().equals("Request")) {
                //对方发来好友邀请
                BroadcastManager.getInstance(mContext).sendBroadcast(UPDATE_RED_DOT);
            } else if (contactNotificationMessage.getOperation().equals("AcceptResponse")) {
                //对方同意我的好友请求
                ContactNotificationMessageData contactNotificationMessageData;
                try {
                    contactNotificationMessageData = JsonMananger.jsonToBean(contactNotificationMessage.getExtra(), ContactNotificationMessageData.class);
                } catch (HttpException e) {
                    e.printStackTrace();
                    return false;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
                if (contactNotificationMessageData != null) {
                    if (SealUserInfoManager.getInstance().isFriendsRelationship(contactNotificationMessage.getSourceUserId())) {
                        return false;
                    }
                    SealUserInfoManager.getInstance().addFriend(
                            new Friend(contactNotificationMessage.getSourceUserId(),
                                    contactNotificationMessageData.getSourceUserNickname(),
                                    null, null, null, null,
                                    null, null,
                                    CharacterParser.getInstance().getSpelling(contactNotificationMessageData.getSourceUserNickname()),
                                    null));
                }
                BroadcastManager.getInstance(mContext).sendBroadcast(UPDATE_FRIEND);
                BroadcastManager.getInstance(mContext).sendBroadcast(UPDATE_RED_DOT);
            }
            /*// 发广播通知更新好友列表
            BroadcastManager.getInstance(mContext).sendBroadcast(UPDATE_RED_DOT);
            }*/
        } else if (messageContent instanceof GroupNotificationMessage) {
            GroupNotificationMessage groupNotificationMessage = (GroupNotificationMessage) messageContent;
            NLog.e("onReceived:" + groupNotificationMessage.getMessage());
            String groupID = message.getTargetId();
            GroupNotificationMessageData data = null;
            try {
                String currentID = RongIM.getInstance().getCurrentUserId();
                try {
                    data = jsonToBean(groupNotificationMessage.getData());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (groupNotificationMessage.getOperation().equals("Create")) {
                    //创建群组
                    SealUserInfoManager.getInstance().getGroups(groupID);
                    SealUserInfoManager.getInstance().getGroupMember(groupID);
                } else if (groupNotificationMessage.getOperation().equals("Dismiss")) {
                    //解散群组
                    hangUpWhenQuitGroup();      //挂断电话
                    handleGroupDismiss(groupID);
                } else if (groupNotificationMessage.getOperation().equals("Kicked")) {
                    //群组踢人
                    if (data != null) {
                        List<String> memberIdList = data.getTargetUserIds();
                        if (memberIdList != null) {
                            for (String userId : memberIdList) {
                                if (currentID.equals(userId)) {
                                    hangUpWhenQuitGroup();
                                    RongIM.getInstance().removeConversation(Conversation.ConversationType.GROUP, message.getTargetId(), new RongIMClient.ResultCallback<Boolean>() {
                                        @Override
                                        public void onSuccess(Boolean aBoolean) {
                                            Log.e("SealAppContext", "Conversation remove successfully.");
                                        }

                                        @Override
                                        public void onError(RongIMClient.ErrorCode e) {

                                        }
                                    });
                                }
                            }
                        }

                        List<String> kickedUserIDs = data.getTargetUserIds();
                        SealUserInfoManager.getInstance().deleteGroupMembers(groupID, kickedUserIDs);
                        BroadcastManager.getInstance(mContext).sendBroadcast(UPDATE_GROUP_MEMBER, groupID);
                    }
                } else if (groupNotificationMessage.getOperation().equals("Add")) {
                    //群组添加人员
                    SealUserInfoManager.getInstance().getGroups(groupID);
                    SealUserInfoManager.getInstance().getGroupMember(groupID);
                    BroadcastManager.getInstance(mContext).sendBroadcast(UPDATE_GROUP_MEMBER, groupID);
                } else if (groupNotificationMessage.getOperation().equals("Quit")) {
                    //退出群组
                    if (data != null) {
                        List<String> quitUserIDs = data.getTargetUserIds();
                        if (quitUserIDs.contains(currentID)) {
                            hangUpWhenQuitGroup();
                        }
                        SealUserInfoManager.getInstance().deleteGroupMembers(groupID, quitUserIDs);
                        BroadcastManager.getInstance(mContext).sendBroadcast(UPDATE_GROUP_MEMBER, groupID);
                    }
                } else if (groupNotificationMessage.getOperation().equals("Rename")) {
                    //群组重命名
                    if (data != null) {
                        String targetGroupName = data.getTargetGroupName();
                        SealUserInfoManager.getInstance().updateGroupsName(groupID, targetGroupName);
                        List<String> groupNameList = new ArrayList<>();
                        groupNameList.add(groupID);
                        groupNameList.add(data.getTargetGroupName());
                        groupNameList.add(data.getOperatorNickname());
                        BroadcastManager.getInstance(mContext).sendBroadcast(UPDATE_GROUP_NAME, groupNameList);
                        Groups oldGroup = SealUserInfoManager.getInstance().getGroupsByID(groupID);
                        if (oldGroup != null) {
                            Group group = new Group(groupID, data.getTargetGroupName(), Uri.parse(oldGroup.getPortraitUri()));
                            RongIM.getInstance().refreshGroupInfoCache(group);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        } else if (messageContent instanceof ImageMessage) {
            //ImageMessage imageMessage = (ImageMessage) messageContent;
        }
        return false;
    }

    private void handleGroupDismiss(final String groupID) {
        RongIM.getInstance().getConversation(Conversation.ConversationType.GROUP, groupID, new RongIMClient.ResultCallback<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                RongIM.getInstance().clearMessages(Conversation.ConversationType.GROUP, groupID, new RongIMClient.ResultCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        RongIM.getInstance().removeConversation(Conversation.ConversationType.GROUP, groupID, null);
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode e) {

                    }
                });
            }

            @Override
            public void onError(RongIMClient.ErrorCode e) {

            }
        });
        SealUserInfoManager.getInstance().deleteGroups(new Groups(groupID));
        SealUserInfoManager.getInstance().deleteGroupMembers(groupID);
        BroadcastManager.getInstance(mContext).sendBroadcast(SealConst.GROUP_LIST_UPDATE);
        BroadcastManager.getInstance(mContext).sendBroadcast(GROUP_DISMISS, groupID);
    }

    /**
     * 用户信息提供者的逻辑移到SealUserInfoManager
     * 先从数据库读,没有数据时从网络获取
     */
    @Override
    public UserInfo getUserInfo(String s) {
        //UserInfoEngine.getInstance(mContext).startEngine(s);
        SealUserInfoManager.getInstance().getUserInfo(s);
        return null;
    }

    @Override
    public Group getGroupInfo(String s) {
        //return GroupInfoEngine.getInstance(mContext).startEngine(s);
        SealUserInfoManager.getInstance().getGroupInfo(s);
        return null;
    }

    @Override
    public GroupUserInfo getGroupUserInfo(String groupId, String userId) {
        //return GroupUserInfoEngine.getInstance(mContext).startEngine(groupId, userId);
        return null;
    }


    @Override
    public void onStartLocation(Context context, LocationCallback locationCallback) {
        /**
         * demo 代码  开发者需替换成自己的代码。
         */
    }

    @Override
    public boolean onUserPortraitClick(Context context, Conversation.ConversationType conversationType, UserInfo userInfo) {
        if (conversationType == Conversation.ConversationType.CUSTOMER_SERVICE || conversationType == Conversation.ConversationType.PUBLIC_SERVICE || conversationType == Conversation.ConversationType.APP_PUBLIC_SERVICE) {
            return false;
        }
        //开发测试时,发送系统消息的userInfo只有id不为空
        if (userInfo != null && userInfo.getName() != null && userInfo.getPortraitUri() != null) {
            Intent intent = new Intent(context, UserDetailActivity.class);
            intent.putExtra("conversationType", conversationType.getValue());
            Friend friend = CharacterParser.getInstance().generateFriendFromUserInfo(userInfo);
            intent.putExtra("friend", friend);
            intent.putExtra("type", CLICK_CONVERSATION_USER_PORTRAIT);
            context.startActivity(intent);
        }
        return true;
    }

    @Override
    public boolean onUserPortraitLongClick(Context context, Conversation.ConversationType conversationType, UserInfo userInfo) {
        return false;
    }

    @Override
    public boolean onMessageClick(final Context context, final View view, final Message message) {
        //real-time location message end
        /**
         * demo 代码  开发者需替换成自己的代码。
         */
        if (message.getContent() instanceof ImageMessage) {
            /*Intent intent = new Intent(context, PhotoActivity.class);
            intent.putExtra("message", message);
            context.startActivity(intent);*/
        }

        return false;
    }


    private void startRealTimeLocation(Context context, Conversation.ConversationType conversationType, String targetId) {

    }

    private void joinRealTimeLocation(Context context, Conversation.ConversationType conversationType, String targetId) {

    }

    @Override
    public boolean onMessageLinkClick(Context context, String s) {
        return false;
    }

    @Override
    public boolean onMessageLongClick(Context context, View view, Message message) {
        return false;
    }


    public RongIM.LocationProvider.LocationCallback getLastLocationCallback() {
        return mLastLocationCallback;
    }

    public void setLastLocationCallback(RongIM.LocationProvider.LocationCallback lastLocationCallback) {
        this.mLastLocationCallback = lastLocationCallback;
    }

    @Override
    public void onChanged(ConnectionStatus connectionStatus) {
        NLog.d(TAG, "ConnectionStatus onChanged = " + connectionStatus.getMessage());
        if (connectionStatus.equals(ConnectionStatus.KICKED_OFFLINE_BY_OTHER_CLIENT)) {
            quit(true);
        }
    }

    public void pushActivity(Activity activity) {
        mActivities.add(activity);
    }

    public void popActivity(Activity activity) {
        if (mActivities.contains(activity)) {
            activity.finish();
            mActivities.remove(activity);
        }
    }

    public void popAllActivity() {
        try {
            if (MainActivity.mViewPager != null) {
                MainActivity.mViewPager.setCurrentItem(0);
            }
            for (Activity activity : mActivities) {
                if (activity != null) {
                    activity.finish();
                }
            }
            mActivities.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RongIMClient.ConnectCallback getConnectCallback() {
        RongIMClient.ConnectCallback connectCallback = new RongIMClient.ConnectCallback() {
            @Override
            public void onTokenIncorrect() {
                NLog.d(TAG, "ConnectCallback connect onTokenIncorrect");
                SealUserInfoManager.getInstance().reGetToken();
            }

            @Override
            public void onSuccess(String s) {
                NLog.d(TAG, "ConnectCallback connect onSuccess");
                SharedPreferences sp = mContext.getSharedPreferences("config", Context.MODE_PRIVATE);
                sp.edit().putString(SealConst.SEALTALK_LOGIN_ID, s).commit();
            }

            @Override
            public void onError(final RongIMClient.ErrorCode e) {
                NLog.d(TAG, "ConnectCallback connect onError-ErrorCode=" + e);
            }
        };
        return connectCallback;
    }

    private GroupNotificationMessageData jsonToBean(String data) {
        GroupNotificationMessageData dataEntity = new GroupNotificationMessageData();
        try {
            JSONObject jsonObject = new JSONObject(data);
            if (jsonObject.has("operatorNickname")) {
                dataEntity.setOperatorNickname(jsonObject.getString("operatorNickname"));
            }
            if (jsonObject.has("targetGroupName")) {
                dataEntity.setTargetGroupName(jsonObject.getString("targetGroupName"));
            }
            if (jsonObject.has("timestamp")) {
                dataEntity.setTimestamp(jsonObject.getLong("timestamp"));
            }
            if (jsonObject.has("targetUserIds")) {
                JSONArray jsonArray = jsonObject.getJSONArray("targetUserIds");
                for (int i = 0; i < jsonArray.length(); i++) {
                    dataEntity.getTargetUserIds().add(jsonArray.getString(i));
                }
            }
            if (jsonObject.has("targetUserDisplayNames")) {
                JSONArray jsonArray = jsonObject.getJSONArray("targetUserDisplayNames");
                for (int i = 0; i < jsonArray.length(); i++) {
                    dataEntity.getTargetUserDisplayNames().add(jsonArray.getString(i));
                }
            }
            if (jsonObject.has("oldCreatorId")) {
                dataEntity.setOldCreatorId(jsonObject.getString("oldCreatorId"));
            }
            if (jsonObject.has("oldCreatorName")) {
                dataEntity.setOldCreatorName(jsonObject.getString("oldCreatorName"));
            }
            if (jsonObject.has("newCreatorId")) {
                dataEntity.setNewCreatorId(jsonObject.getString("newCreatorId"));
            }
            if (jsonObject.has("newCreatorName")) {
                dataEntity.setNewCreatorName(jsonObject.getString("newCreatorName"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataEntity;
    }

    private void quit(boolean isKicked) {
        Log.d(TAG, "quit isKicked " + isKicked);
        SharedPreferences.Editor editor = mContext.getSharedPreferences("config", Context.MODE_PRIVATE).edit();
        if (!isKicked) {
            editor.putBoolean("exit", true);
        }
        editor.putString("loginToken", "");
        editor.putString(SealConst.SEALTALK_LOGIN_ID, "");
        editor.putInt("getAllUserInfoState", 0);
        editor.commit();
        /*//这些数据清除操作之前一直是在login界面,因为app的数据库改为按照userID存储,退出登录时先直接删除
        //这种方式是很不友好的方式,未来需要修改同app server的数据同步方式
        //SealUserInfoManager.getInstance().deleteAllUserInfo();*/
        SealUserInfoManager.getInstance().closeDB();
        RongIM.getInstance().logout();
        Intent loginActivityIntent = new Intent();
        loginActivityIntent.setClass(mContext, LoginActivity.class);
        loginActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        if (isKicked) {
            loginActivityIntent.putExtra("kickedByOtherClient", true);
        }
        mContext.startActivity(loginActivityIntent);
    }

    @Override
    public void getGroupMembers(String groupId, final RongIM.IGroupMemberCallback callback) {
        SealUserInfoManager.getInstance().getGroupMembers(groupId, new SealUserInfoManager.ResultCallback<List<GroupMember>>() {
            @Override
            public void onSuccess(List<GroupMember> groupMembers) {
                List<UserInfo> userInfos = new ArrayList<>();
                if (groupMembers != null) {
                    for (GroupMember groupMember : groupMembers) {
                        if (groupMember != null) {
                            UserInfo userInfo = new UserInfo(groupMember.getUserId(), groupMember.getName(), groupMember.getPortraitUri());
                            userInfos.add(userInfo);
                        }
                    }
                }
                callback.onGetGroupMembersResult(userInfos);
            }

            @Override
            public void onError(String errString) {
                callback.onGetGroupMembersResult(null);
            }
        });
    }

    private void hangUpWhenQuitGroup() {
        RongCallSession session = RongCallClient.getInstance().getCallSession();
        if (session != null) {
            RongCallClient.getInstance().hangUpCall(session.getCallId());
        }
    }
}
