package cn.rongcloud.im.im;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.ErrorCode;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.common.LogTag;
import cn.rongcloud.im.common.ResultCallback;
import cn.rongcloud.im.db.DbManager;
import cn.rongcloud.im.im.message.SealContactNotificationMessage;
import cn.rongcloud.im.im.message.SealGroupNotificationMessage;
import cn.rongcloud.im.im.provider.ContactNotificationMessageProvider;
import cn.rongcloud.im.im.provider.SealGroupNotificationMessageItemProvider;
import cn.rongcloud.im.model.ChatRoomAction;
import cn.rongcloud.im.model.ContactNotificationMessageData;
import cn.rongcloud.im.model.LoginResult;
import cn.rongcloud.im.model.QuietHours;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.UserCacheInfo;
import cn.rongcloud.im.net.CallBackWrapper;
import cn.rongcloud.im.net.HttpClientManager;
import cn.rongcloud.im.net.service.UserService;
import cn.rongcloud.im.sp.UserCache;
import cn.rongcloud.im.sp.UserConfigCache;
import cn.rongcloud.im.ui.activity.ForwardActivity;
import cn.rongcloud.im.ui.activity.NewFriendListActivity;
import cn.rongcloud.im.ui.activity.SealPicturePagerActivity;
import cn.rongcloud.im.ui.activity.UserDetailActivity;
import cn.rongcloud.im.utils.log.SLog;
import io.rong.contactcard.ContactCardExtensionModule;
import io.rong.contactcard.IContactCardInfoProvider;
import io.rong.imkit.DefaultExtensionModule;
import io.rong.imkit.IExtensionModule;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongExtensionManager;
import io.rong.imkit.RongIM;
import io.rong.imkit.RongMessageItemLongClickActionManager;
import io.rong.imkit.manager.IUnReadMessageObserver;
import io.rong.imkit.mention.IMentionedInputListener;
import io.rong.imkit.mention.RongMentionManager;
import io.rong.imkit.model.GroupNotificationMessageData;
import io.rong.imkit.model.GroupUserInfo;
import io.rong.imkit.model.UIConversation;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.notification.MessageNotificationManager;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imkit.widget.provider.MessageItemLongClickAction;
import io.rong.imkit.widget.provider.RealTimeLocationMessageProvider;
import io.rong.imlib.CustomServiceConfig;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.cs.CustomServiceManager;
import io.rong.imlib.location.message.RealTimeLocationStartMessage;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Discussion;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.MentionedInfo;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.PublicServiceProfile;
import io.rong.imlib.model.UserInfo;
import io.rong.message.ContactNotificationMessage;
import io.rong.message.GroupNotificationMessage;
import io.rong.message.ImageMessage;
import io.rong.message.NotificationMessage;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;
import io.rong.push.RongPushClient;
import io.rong.push.pushconfig.PushConfig;
import io.rong.recognizer.RecognizeExtensionModule;
import io.rong.sight.SightExtensionModule;

public class IMManager {
    private static volatile IMManager instance;

    private MutableLiveData<ChatRoomAction> chatRoomActionLiveData = new MutableLiveData<>();
    private Context context;

    private UserConfigCache configCache;
    private UserCache userCache;

    private MutableLiveData<Boolean> autologinResult = new MutableLiveData<>();
    private MutableLiveData<Message> messageRouter = new MutableLiveData<>();
    private MutableLiveData<Boolean> kickedOffline = new MutableLiveData<>();

    private IMInfoProvider imInfoProvider;


    private IMManager() {
    }

    public static IMManager getInstance() {
        if (instance == null) {
            synchronized (IMManager.class) {
                if (instance == null) {
                    instance = new IMManager();
                }
            }
        }
        return instance;
    }

    /**
     * @param context
     */
    public void init(Context context) {
        this.context = context.getApplicationContext();

        // 初始化推送
        initPush();

        // 调用 RongIM 初始化
        initRongIM(context);

        // 初始化用户和群组信息内容提供者
        initInfoProvider(context);

        // 初始化自定义消息和消息模版
        initMessageAndTemplate();

        // 初始化扩展模块
        initExtensionModules(context);

        // 初始化已读回执类型
        initReadReceiptConversation();

        // 初始化会话界面相关内容
        initConversation();

        // 初始化会话列表界面相关内容
        initConversationList();

        // 初始化连接状态变化监听
        initConnectStateChangeListener();

        // 初始化消息监听
        initOnReceiveMessage(context);

        // 初始化聊天室监听
        initChatRoomActionListener();

        // 长按消息转发等功能
        initMessageItemLongClickAction(context);

        // 缓存连接
        cacheConnectIM();
    }

    /**
     * 缓存登录
     */
    private void cacheConnectIM() {
        if (RongIM.getInstance().getCurrentConnectionStatus() == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED) {
            autologinResult.setValue(true);
            return ;
        }
        // 用户设置缓存 sp
        configCache = new UserConfigCache(context.getApplicationContext());
        userCache = new UserCache(context.getApplicationContext());

        UserCacheInfo userCache = this.userCache.getUserCache();
        if (userCache == null) {
            autologinResult.setValue(false);
            return;
        }

        String loginToken = this.userCache.getUserCache().getLoginToken();
        if (TextUtils.isEmpty(loginToken)) {
            autologinResult.setValue(false);
            return;
        }

        connectIM(loginToken, true, new ResultCallback<String>() {
            @Override
            public void onSuccess(String s) {
                autologinResult.setValue(true);
            }

            @Override
            public void onFail(int errorCode) {
                autologinResult.setValue(false);
            }
        });
    }

    public void evaluateCustomService(String targetId, int stars, CustomServiceConfig.CSEvaSolveStatus resolveStatus, String lables, String suggestion, String dialogId) {
        RongIMClient.getInstance().evaluateCustomService(targetId, stars, resolveStatus, lables,
                suggestion, dialogId, null);
    }


    /**
     * 设置人工评价监听
     * 当人工评价有标签等配置时，在回调中返回配置
     *
     * @param listener
     */
    public void setCustomServiceHumanEvaluateListener(CustomServiceManager.OnHumanEvaluateListener listener) {
        RongIMClient.getInstance().setCustomServiceHumanEvaluateListener(listener);
    }

    /**
     * 设置正在输入消息状态
     *
     * @param listener
     */
    public void setTypingStatusListener(RongIMClient.TypingStatusListener listener) {
        RongIMClient.setTypingStatusListener(listener);
    }


    /**
     * 获取讨论组信息
     *
     * @param targetId
     * @param callback
     */
    public void getDiscussion(String targetId, RongIMClient.ResultCallback<Discussion> callback) {
        RongIM.getInstance().getDiscussion(targetId, callback);
    }

    /**
     * 获取从公众号信息
     *
     * @param type
     * @param targetId
     * @param callback
     */
    public void getPublicServiceProfile(Conversation.PublicServiceType type, String targetId, RongIMClient.ResultCallback<PublicServiceProfile> callback) {
        RongIM.getInstance().getPublicServiceProfile(type, targetId, callback);
    }

    /**
     * 获取会话通知消息免打扰状态
     *
     * @param conversationType
     * @param targetId
     * @return Resource 中 data 为 true 表示进行消息通知，false 表示消息免打扰
     */
    public LiveData<Resource<Boolean>> getConversationNotificationStatus(Conversation.ConversationType conversationType, String targetId) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null));

        RongIM.getInstance().getConversationNotificationStatus(conversationType, targetId, new RongIMClient.ResultCallback<Conversation.ConversationNotificationStatus>() {
            @Override
            public void onSuccess(Conversation.ConversationNotificationStatus status) {
                if (status != null) {
                    result.postValue(Resource.success(status == Conversation.ConversationNotificationStatus.NOTIFY));
                } else {
                    result.postValue(Resource.success(true));
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                SLog.e(LogTag.IM, "get conversation notification status error, msg:" + errorCode.getMessage() + ", code:" + errorCode.getValue());
                result.postValue(Resource.error(ErrorCode.IM_ERROR.getCode(), null));
            }
        });

        return result;
    }

    /**
     * 获取会话是否置顶
     *
     * @param conversationType
     * @param targetId
     * @return
     */
    public LiveData<Resource<Boolean>> getConversationIsOnTop(Conversation.ConversationType conversationType, String targetId) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null));

        RongIM.getInstance().getConversation(conversationType, targetId, new RongIMClient.ResultCallback<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                if (conversation != null) {
                    result.postValue(Resource.success(conversation.isTop()));
                } else {
                    result.postValue(Resource.success(false));
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                SLog.e(LogTag.IM, "get conversation error, msg:" + errorCode.getMessage() + ", code:" + errorCode.getValue());
                result.postValue(Resource.error(ErrorCode.IM_ERROR.getCode(), null));
            }
        });

        return result;
    }

    /**
     * 设置会话免打扰状态
     *
     * @param conversationType
     * @param targetId
     * @param isNotify
     * @return
     */
    public LiveData<Resource<Boolean>> setConversationNotificationStatus(Conversation.ConversationType conversationType, String targetId, boolean isNotify) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null));

        RongIM.getInstance().setConversationNotificationStatus(conversationType, targetId,
                isNotify ? Conversation.ConversationNotificationStatus.NOTIFY
                        : Conversation.ConversationNotificationStatus.DO_NOT_DISTURB
                , new RongIMClient.ResultCallback<Conversation.ConversationNotificationStatus>() {
                    @Override
                    public void onSuccess(Conversation.ConversationNotificationStatus status) {
                        result.postValue(Resource.success(isNotify));
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode errorCode) {
                        SLog.e(LogTag.IM, "get conversation notification status error, msg:" + errorCode.getMessage() + ", code:" + errorCode.getValue());
                        result.postValue(Resource.error(ErrorCode.IM_ERROR.getCode(), !isNotify));
                    }
                });

        return result;
    }

    /**
     * 设置会话置顶
     *
     * @param conversationType
     * @param targetId
     * @param isTop
     * @return
     */
    public LiveData<Resource<Boolean>> setConversationToTop(Conversation.ConversationType conversationType, String targetId, boolean isTop) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null));

        RongIM.getInstance().setConversationToTop(conversationType, targetId, isTop, new RongIMClient.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                result.setValue(Resource.success(isTop));
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                SLog.e(LogTag.IM, "get conversation to top error, msg:" + errorCode.getMessage() + ", code:" + errorCode.getValue());
                result.setValue(Resource.error(ErrorCode.IM_ERROR.getCode(), !isTop));
            }
        });

        return result;
    }

    /**
     * 清除会话历史聊天信息
     *
     * @param conversationType
     * @param targetId
     * @return
     */
    public LiveData<Resource<Boolean>> cleanHistoryMessage(Conversation.ConversationType conversationType, String targetId) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null));

        RongIM.getInstance().clearMessages(conversationType, targetId, new RongIMClient.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                result.postValue(Resource.success(true));
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                SLog.e(LogTag.IM, "clean history message, msg:" + errorCode.getMessage() + ", code:" + errorCode.getValue());
                result.postValue(Resource.error(ErrorCode.IM_ERROR.getCode(), false));
            }
        });

        // 清除远端消息
        RongIMClient.getInstance().cleanRemoteHistoryMessages(
                conversationType,
                targetId, System.currentTimeMillis(),
                null);

        return result;
    }

    /**
     * 初始化会话相关
     */
    private void initConversation() {
        // 启用会话界面新消息提示
        RongIM.getInstance().enableNewComingMessageIcon(true);
        // 启用会话界面未读信息提示
        RongIM.getInstance().enableUnreadMessageIcon(true);

        // 添加会话界面点击事件
        RongIM.setConversationClickListener(new RongIM.ConversationClickListener() {
            @Override
            public boolean onUserPortraitClick(Context context, Conversation.ConversationType conversationType, UserInfo userInfo, String s) {
                Intent intent = new Intent(context, UserDetailActivity.class);
                intent.putExtra(IntentExtra.STR_TARGET_ID, userInfo.getUserId());
                if (conversationType == Conversation.ConversationType.GROUP) {
                    Group groupInfo = RongUserInfoManager.getInstance().getGroupInfo(s);
                    if (groupInfo != null) {
                        intent.putExtra(IntentExtra.STR_GROUP_NAME, groupInfo.getName());
                    }
                }
                context.startActivity(intent);
                return true;
            }

            @Override
            public boolean onUserPortraitLongClick(Context context, Conversation.ConversationType conversationType, UserInfo userInfo, String s) {
                return false;
            }

            @Override
            public boolean onMessageClick(Context context, View view, Message message) {
                if (message.getContent() instanceof ImageMessage) {
                    Intent intent = new Intent(view.getContext(), SealPicturePagerActivity.class);
                    intent.setPackage(view.getContext().getPackageName());
                    intent.putExtra("message", message);
                    view.getContext().startActivity(intent);
                    return true;
                }
                return false;
            }

            @Override
            public boolean onMessageLinkClick(Context context, String s, Message message) {
                return false;
            }

            @Override
            public boolean onMessageLongClick(Context context, View view, Message message) {
                return false;
            }
        });

    }

    /**
     * 初始化会话列表相关事件
     */
    private void initConversationList() {
        // 设置会话列表行为监听
        RongIM.setConversationListBehaviorListener(new RongIM.ConversationListBehaviorListener() {
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
                /*
                 * 当点击会话列表中通知添加好友消息时，判断是否已成为好友
                 * 已成为好友时，跳转到私聊界面
                 * 非好友时跳转到新的朋友界面查看添加好友状态
                 */
                MessageContent messageContent = uiConversation.getMessageContent();
                if (messageContent instanceof ContactNotificationMessage) {
                    ContactNotificationMessage contactNotificationMessage = (ContactNotificationMessage) messageContent;
                    if (contactNotificationMessage.getOperation().equals("AcceptResponse")) {
                        // 被加方同意请求后
                        if (contactNotificationMessage.getExtra() != null) {
                            ContactNotificationMessageData bean = null;
                            try {
                                Gson gson = new Gson();
                                bean = gson.fromJson(contactNotificationMessage.getExtra(), ContactNotificationMessageData.class);
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
        });
    }

    /**
     * 更新 IMKit 显示用用户信息
     *
     * @param userId
     * @param userName
     * @param portraitUri
     */
    public void updateUserInfoCache(String userId, String userName, Uri portraitUri) {
        UserInfo userInfo = new UserInfo(userId, userName, portraitUri);
        RongIM.getInstance().refreshUserInfoCache(userInfo);
    }

    /**
     * 更新 IMKit 显示用群组信息
     *
     * @param groupId
     * @param groupName
     * @param portraitUri
     */
    public void updateGroupInfoCache(String groupId, String groupName, Uri portraitUri) {
        Group group = new Group(groupId, groupName, portraitUri);
        RongIM.getInstance().refreshGroupInfoCache(group);
    }

    /**
     * 更新 IMKit 显示用群组成员信息
     *
     * @param groupId
     * @param userId
     * @param nickName
     */
    public void updateGroupMemberInfoCache(String groupId, String userId, String nickName) {
        GroupUserInfo groupMemberInfo = new GroupUserInfo(groupId, userId, nickName);
        RongIM.getInstance().refreshGroupUserInfoCache(groupMemberInfo);
    }

    /**
     * 获取当前用户 id
     *
     * @return
     */
    public String getCurrentId() {
        return RongIM.getInstance().getCurrentUserId();
    }

    /**
     * 群发消息
     *
     * @param targetId
     * @param conversationType
     * @param message
     * @return
     */
    public LiveData<Resource<Message>> sendToAll(String targetId, Conversation.ConversationType conversationType, String message) {
        MutableLiveData<Resource<Message>> result = new MutableLiveData<>();

        TextMessage textMessage = TextMessage.obtain(RongContext.getInstance().getString(R.string.profile_group_notice_prefix) + message);
        MentionedInfo mentionedInfo = new MentionedInfo(MentionedInfo.MentionedType.ALL, null, null);
        textMessage.setMentionedInfo(mentionedInfo);

        RongIM.getInstance().sendMessage(Message.obtain(targetId, conversationType, textMessage), null, null, new IRongCallback.ISendMessageCallback() {
            @Override
            public void onAttached(Message message) {

            }

            @Override
            public void onSuccess(Message message) {
                result.postValue(Resource.success(message));
            }

            @Override
            public void onError(Message message, RongIMClient.ErrorCode errorCode) {
                SLog.e(LogTag.IM, "send to All error,msg:" + errorCode.getMessage() + ",code:" + errorCode.getValue());
                result.postValue(Resource.error(ErrorCode.IM_ERROR.getCode(), message));
            }
        });

        return result;
    }

    /**
     * 清除会话及消息
     *
     * @param targetId
     * @param conversationType
     */
    public void clearConversationAndMessage(String targetId, Conversation.ConversationType conversationType) {
        RongIM.getInstance().getConversation(conversationType, targetId, new RongIMClient.ResultCallback<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                RongIM.getInstance().clearMessages(conversationType, targetId, new RongIMClient.ResultCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        RongIM.getInstance().removeConversation(conversationType, targetId, null);
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
    }

    /**
     * 初始化推送
     */
    private void initPush() {
        /*
         * 配置 融云 IM 消息推送
         * 根据需求配置各个平台的推送
         * 配置推送需要在初始化 融云 SDK 之前
         */
        PushConfig config = new PushConfig
                .Builder()
                .enableHWPush(true)
                .enableMiPush("2882303761517473625", "5451747338625")
                .enableMeiZuPush("112988", "2fa951a802ac4bd5843d694517307896")
                .enableVivoPush(true)
                .enableFCM(true)
                .build();
        RongPushClient.setPushConfig(config);
    }

    /**
     * 注册消息及消息模版
     */
    private void initMessageAndTemplate() {
        SLog.d("ss_register_message", "initMessageAndTemplate");
        RongIM.registerMessageType(SealGroupNotificationMessage.class);
        RongIM.registerMessageType(SealContactNotificationMessage.class);
        RongIM.registerMessageTemplate(new ContactNotificationMessageProvider());
        RongIM.registerMessageTemplate(new SealGroupNotificationMessageItemProvider());
        RongIM.registerMessageTemplate(new RealTimeLocationMessageProvider());
    }

    /**
     * 初始化扩展模块
     *
     * @param context
     */
    private void initExtensionModules(Context context) {
        /**
         * 因为 SealExtensionModule 继承与融云默认 DefaultExtensionModule，
         * 需要先移除掉默认的扩展后再进行注册
         * 继承并覆盖默认的扩展模块可在自己需要的时机控制各默认模块的展示与隐藏
         */
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
            }
        }

        RongExtensionManager.getInstance().registerExtensionModule(new SealExtensionModule(context));

        // 个人名片
        RongExtensionManager.getInstance().registerExtensionModule(createContactCardExtensionModule());
        // 语音输入
        RongExtensionManager.getInstance().registerExtensionModule(new RecognizeExtensionModule());
        // 小视频
        RongExtensionManager.getInstance().registerExtensionModule(new SightExtensionModule());
    }

    /**
     * 调用初始化 RongIM
     *
     * @param context
     */
    private void initRongIM(Context context) {
        /*
         * 如果是连接到私有云需要在此配置服务器地址
         * 如果是公有云则不需要调用此方法
         */
        //RongIM.setServerInfo("nav.cn.ronghub.com", "up.qbox.me");

        /*
         * 初始化 SDK，在整个应用程序全局，只需要调用一次。建议在 Application 继承类中调用。
         */

        /* 若直接调用init方法请在 IMLib 模块中的 AndroidManifest.xml 中, 找到 <meta-data> 中 android:name 为 RONG_CLOUD_APP_KEY的标签，
         * 将 android:value 替换为融云 IM 申请的APP KEY
         */
        //RongIM.init(this);

        // 可在初始 SDK 时直接带入融云 IM 申请的APP KEY
        RongIM.init(context, "n19jmcy59f1q9", true);

//        RongIM.init(context, "kj7swf8o7dot2", true);

    }

    /**
     * 初始化信息提供者，包括用户信息，群组信息，群主成员信息
     */
    private void initInfoProvider(Context context) {
        imInfoProvider = new IMInfoProvider();
        imInfoProvider.init(context);
    }

    /**
     * 创建个人名片模块
     *
     * @return
     */
    private ContactCardExtensionModule createContactCardExtensionModule() {
        return new ContactCardExtensionModule(new IContactCardInfoProvider() {
            /**
             * 获取所有通讯录用户
             *
             * @param contactInfoCallback
             */
            @Override
            public void getContactAllInfoProvider(IContactCardInfoCallback contactInfoCallback) {
                imInfoProvider.getAllContactUserInfo(contactInfoCallback);
            }

            /**
             * 获取单一用户
             *
             * @param userId
             * @param name
             * @param portrait
             * @param contactInfoCallback
             */
            @Override
            public void getContactAppointedInfoProvider(String userId, String name, String portrait, IContactCardInfoCallback contactInfoCallback) {
                imInfoProvider.getContactUserInfo(userId, contactInfoCallback);
            }
        }, (view, content) -> {
            Context activityContext = view.getContext();
            // 点击名片进入到个人详细界面
            Intent intent = new Intent(activityContext, UserDetailActivity.class);
            intent.putExtra(IntentExtra.STR_TARGET_ID, content.getId());
            activityContext.startActivity(intent);
        });
    }

    /**
     * 初始化已读回执类型
     */
    private void initReadReceiptConversation() {
        // 将私聊，群组加入消息已读回执
        Conversation.ConversationType[] types = new Conversation.ConversationType[]{
                Conversation.ConversationType.PRIVATE,
                Conversation.ConversationType.GROUP,
                Conversation.ConversationType.ENCRYPTED
        };
        RongIM.getInstance().setReadReceiptConversationTypeList(types);
    }

    /**
     * 初始化连接状态监听
     */
    private void initConnectStateChangeListener() {
        RongIM.setConnectionStatusListener(new RongIMClient.ConnectionStatusListener() {
            @Override
            public void onChanged(ConnectionStatus connectionStatus) {
                SLog.d(LogTag.IM, "ConnectionStatus onChanged = " + connectionStatus.getMessage());
                if (connectionStatus.equals(ConnectionStatus.KICKED_OFFLINE_BY_OTHER_CLIENT)) {
                    //被其他提出时，需要返回登录界面
                    kickedOffline.postValue(true);
                } else if (connectionStatus == ConnectionStatus.TOKEN_INCORRECT) {
                    //TODO token 错误时，重新登录
                }
            }
        });
    }

    /**
     * 初始化消息监听
     */
    private void initOnReceiveMessage(Context context) {
        RongIM.setOnReceiveMessageListener(new RongIMClient.OnReceiveMessageListener() {
            @Override
            public boolean onReceived(Message message, int i) {
                messageRouter.postValue(message);
                MessageContent messageContent = message.getContent();
                if (messageContent instanceof ContactNotificationMessage) { // 添加好友状态信息
                    ContactNotificationMessage contactNotificationMessage = (ContactNotificationMessage) messageContent;
                    if (contactNotificationMessage.getOperation().equals("Request")) {

                    } else if (contactNotificationMessage.getOperation().equals("AcceptResponse")) {

                        // 根据好友 id 进行获取好友信息并刷新
                        String sourceUserId = contactNotificationMessage.getSourceUserId();
                        imInfoProvider.updateFriendInfo(sourceUserId);
                    }
                } else if (messageContent instanceof GroupNotificationMessage) {    // 群组通知消息
                    GroupNotificationMessage groupNotificationMessage = (GroupNotificationMessage) messageContent;
                    SLog.d(LogTag.IM, "onReceived GroupNotificationMessage:" + groupNotificationMessage.getMessage());

                    String groupID = message.getTargetId();
                    GroupNotificationMessageData data = null;
                    try {
                        String currentID = RongIM.getInstance().getCurrentUserId();
                        try {
                            Gson gson = new Gson();
                            data = gson.fromJson(groupNotificationMessage.getData(), GroupNotificationMessageData.class);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (groupNotificationMessage.getOperation().equals("Create")) {
                            // 创建群组,获取群组信息
                            imInfoProvider.updateGroupInfo(groupID);
                            imInfoProvider.updateGroupMember(groupID);
                        } else if (groupNotificationMessage.getOperation().equals("Dismiss")) {
                            // 删除数据库中群组
                            imInfoProvider.deleteGroupInfoInDb(groupID);
                            // 删除群组会话和消息
                            clearConversationAndMessage(groupID, Conversation.ConversationType.GROUP);

                        } else if (groupNotificationMessage.getOperation().equals("Kicked")) {
                            //群组踢人
                            boolean isKicked = false;
                            if (data != null) {
                                List<String> memberIdList = data.getTargetUserIds();
                                if (memberIdList != null) {
                                    for (String userId : memberIdList) {
                                        if (currentID.equals(userId)) {
                                            // 被踢出群组，删除群组会话和消息
                                            clearConversationAndMessage(groupID, Conversation.ConversationType.GROUP);
                                            imInfoProvider.deleteGroupInfoInDb(groupID);
                                            isKicked = true;
                                            break;
                                        }
                                    }
                                }
                            }
                            // 如果未被提出，则更新群组信息和群成员
                            if(!isKicked){
                                imInfoProvider.updateGroupInfo(groupID);
                                imInfoProvider.updateGroupMember(groupID);
                            }
                        } else if (groupNotificationMessage.getOperation().equals("Add")) {
                            // 群组添加人员
                            imInfoProvider.updateGroupInfo(groupID);
                            imInfoProvider.updateGroupMember(groupID);
                        } else if (groupNotificationMessage.getOperation().equals("Quit")) {
                            // 退出群组，当非自己退出室刷新群组信息
                            if(!currentID.equals(groupNotificationMessage.getOperatorUserId())){
                                imInfoProvider.updateGroupInfo(groupID);
                                imInfoProvider.updateGroupMember(groupID);
                            }
                        } else if (groupNotificationMessage.getOperation().equals("Rename")) {
                            // 群组重命名,更新群信息
                            if (data != null) {
                                // 更新数据库中群组名称
                                String targetGroupName = data.getTargetGroupName();
                                imInfoProvider.updateGroupNameInDb(groupID, targetGroupName);
                            }
                        } else if (groupNotificationMessage.getOperation().equals("Transfer")) {
                            // 转移群主，获取群组信息
                            imInfoProvider.updateGroupInfo(groupID);
                            imInfoProvider.updateGroupMember(groupID);
                        } else if (groupNotificationMessage.getOperation().equals("SetManager")) {
                            // 设置管理员，获取群组信息
                            imInfoProvider.updateGroupInfo(groupID);
                            imInfoProvider.updateGroupMember(groupID);
                        } else if (groupNotificationMessage.getOperation().equals("RemoveManager")) {
                            // 移除管理员，获取群组信息
                            imInfoProvider.updateGroupInfo(groupID);
                            imInfoProvider.updateGroupMember(groupID);
                        }
                    } catch (Exception e) {
                        SLog.d(LogTag.IM, "onReceived process GroupNotificationMessage catch exception:" + e.getMessage());
                        e.printStackTrace();
                    }
                    return true;
                }
                return false;
            }
        });
    }


    private void initMessageItemLongClickAction(Context context) {
        MessageItemLongClickAction action = new MessageItemLongClickAction.Builder()
                .titleResId(R.string.seal_forward_message)
                .showFilter(new MessageItemLongClickAction.Filter() {
                    @Override
                    public boolean filter(UIMessage message) {
                        MessageContent messageContent = message.getContent();
                        return !(messageContent instanceof NotificationMessage)
                                && !(messageContent instanceof VoiceMessage)
                                && !(messageContent instanceof RealTimeLocationStartMessage)
                                && message.getSentStatus() != Message.SentStatus.FAILED
                                && message.getSentStatus() != Message.SentStatus.CANCELED
                                && !message.getConversationType().equals(Conversation.ConversationType.ENCRYPTED);
                    }
                })
                .actionListener(new MessageItemLongClickAction.MessageItemLongClickListener() {
                    @Override
                    public boolean onMessageItemLongClick(Context context, UIMessage message) {
                        Message forwardMesage = message.getMessage();
                        Intent intent = new Intent(context, ForwardActivity.class);
                        ArrayList<Message> messageList = new ArrayList<>();
                        messageList.add(forwardMesage);
                        intent.putParcelableArrayListExtra(IntentExtra.FORWARD_MESSAGE_LIST, messageList);
                        context.startActivity(intent);
                        return true;
                    }
                })
                .build();

        RongMessageItemLongClickActionManager.getInstance().addMessageItemLongClickAction(action, -1);
    }

    /**
     * 设置通知消息免打扰
     *
     * @param startTime
     * @param spanMinutes
     * @return
     */
    public MutableLiveData<Resource<QuietHours>> setNotificationQuietHours(String startTime, int spanMinutes, boolean isCache) {
        MutableLiveData<Resource<QuietHours>> result = new MutableLiveData<>();
        RongIMClient.getInstance().setNotificationQuietHours(startTime, spanMinutes, new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
                MessageNotificationManager.getInstance().setNotificationQuietHours(startTime, spanMinutes);
                // 设置用户消息免打扰缓存状态
                if (isCache) {
                    configCache.setNotifiDonotDistrabStatus(getCurrentId(), true);
                    configCache.setNotifiQuietHours(getCurrentId(), startTime, spanMinutes);
                    result.setValue(Resource.success(configCache.getNotifiQUietHours(getCurrentId())));
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                result.postValue(Resource.error(ErrorCode.IM_ERROR.getCode(), null));
            }
        });
        return result;
    }

    /**
     * 清理通知免打扰
     *
     * @return
     */
    public MutableLiveData<Resource<Boolean>> removeNotificationQuietHours() {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        RongIM.getInstance().removeNotificationQuietHours(new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
                // 设置用户消息免打扰缓存状态
                configCache.setNotifiDonotDistrabStatus(getCurrentId(), false);
                result.postValue(Resource.success(true));
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                result.postValue(Resource.error(ErrorCode.IM_ERROR.getCode(), false));
            }
        });

        return result;
    }

    /**
     * 获取通知时间
     *
     * @return
     */
    public MutableLiveData<Resource<QuietHours>> getNotificationQuietHours() {
        MutableLiveData<Resource<QuietHours>> result = new MutableLiveData<>();
        RongIM.getInstance().getNotificationQuietHours(new RongIMClient.GetNotificationQuietHoursCallback() {
            @Override
            public void onSuccess(String s, int i) {
                QuietHours quietHours = new QuietHours();
                quietHours.startTime = s;
                quietHours.spanMinutes = i;
                result.postValue(Resource.success(quietHours));
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                result.postValue(Resource.error(ErrorCode.IM_ERROR.getCode(), null));
            }
        });
        return result;
    }

    /**
     * 新消息通知设置
     *
     * @param status
     */
    public void setRemindStatus(boolean status) {
        if (status) {
            boolean donotDistrabStatus = configCache.getNotifiDonotDistrabStatus(getCurrentId());
            if (!donotDistrabStatus) {
                removeNotificationQuietHours();
            }
        } else {
            setNotificationQuietHours("00:00:00", 1439, false);
        }
        configCache.setNewMessageRemind(getCurrentId(), status);
    }


    /**
     * 获取消息通知设置
     *
     * @return
     */
    public boolean getRemindStatus() {
        return configCache.getNewMessageRemind(getCurrentId());
    }

    /**
     * 获取通知设置消息
     *
     * @return
     */
    public QuietHours getNotifiQuietHours() {
        return configCache.getNotifiQUietHours(getCurrentId());
    }

    /**
     * 初始化聊天室监听
     */
    private void initChatRoomActionListener() {
        RongIMClient.setChatRoomActionListener(new RongIMClient.ChatRoomActionListener() {
            @Override
            public void onJoining(String roomId) {
                chatRoomActionLiveData.postValue(ChatRoomAction.joining(roomId));
            }

            @Override
            public void onJoined(String roomId) {
                chatRoomActionLiveData.postValue(ChatRoomAction.joined(roomId));
            }

            @Override
            public void onQuited(String roomId) {
                chatRoomActionLiveData.postValue(ChatRoomAction.quited(roomId));
            }

            @Override
            public void onError(String roomId, RongIMClient.ErrorCode errorCode) {
                chatRoomActionLiveData.postValue(ChatRoomAction.error(roomId));
            }
        });
    }

    /**
     * 获取聊天室行为状态
     *
     * @return
     */
    public LiveData<ChatRoomAction> getChatRoomAction() {
        return chatRoomActionLiveData;
    }

    /**
     * 监听未读消息状态
     */
    public void addUnReadMessageCountChangedObserver(IUnReadMessageObserver observer, Conversation.ConversationType[] conversationTypes) {
        RongIM.getInstance().addUnReadMessageCountChangedObserver(observer, conversationTypes);
    }


    /**
     * 移除未读消息监听
     *
     * @param observer
     */
    public void removeUnReadMessageCountChangedObserver(IUnReadMessageObserver observer) {
        RongIM.getInstance().removeUnReadMessageCountChangedObserver(observer);
    }

    /**
     * 清理未读消息状态
     *
     * @param conversationTypes 指定清理的会话类型
     */
    public void clearMessageUnreadStatus(Conversation.ConversationType[] conversationTypes) {
        RongIM.getInstance().getConversationList(new RongIMClient.ResultCallback<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                if (conversations != null && conversations.size() > 0) {
                    for (Conversation c : conversations) {
                        RongIM.getInstance().clearMessagesUnreadStatus(c.getConversationType(), c.getTargetId(), null);
                    }
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode e) {

            }
        }, conversationTypes);
    }

    /**
     * 自动重连结果
     *
     * @return
     */
    public LiveData<Boolean> getAutoLoginResult() {
        return autologinResult;
    }

    /**
     * 群 @ 监听
     *
     * @return
     */
    public LiveData<String> mentionedInput() {
        MutableLiveData<String> result = new MutableLiveData<>();
        RongMentionManager.getInstance().setMentionedInputListener(new IMentionedInputListener() {
            @Override
            public boolean onMentionedInput(Conversation.ConversationType conversationType, String targetId) {
                result.postValue(targetId);
                return true;
            }
        });
        return result;
    }

    /**
     * 获取群成员列表
     *
     * @param targetId
     */
    public LiveData<List<UserInfo>> getGroupMembers(String targetId) {
        MutableLiveData<List<UserInfo>> result = new MutableLiveData<>();
        RongIM.IGroupMembersProvider groupMembersProvider = RongMentionManager.getInstance().getGroupMembersProvider();
        if (groupMembersProvider == null) {
            result.postValue(null);
        } else {
            groupMembersProvider.getGroupMembers(targetId, new RongIM.IGroupMemberCallback() {
                @Override
                public void onGetGroupMembersResult(final List<UserInfo> members) {
                    result.postValue(members);
                }
            });
        }

        return result;
    }

    /**
     * 获取讨论组中的人员
     *
     * @param targetId
     */
    public LiveData<List<UserInfo>> getDiscussionMembers(String targetId) {
        MutableLiveData<List<UserInfo>> result = new MutableLiveData<>();
        RongIMClient.getInstance().getDiscussion(targetId, new RongIMClient.ResultCallback<Discussion>() {
            @Override
            public void onSuccess(Discussion discussion) {
                List<String> memeberIds = discussion.getMemberIdList();
                if (memeberIds == null || memeberIds.size() <= 0) {
                    result.postValue(null);
                    return;
                }
                List<UserInfo> userInfos = new ArrayList<>();
                for (String id : memeberIds) {
                    UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(id);
                    if (userInfo != null) {
                        userInfos.add(userInfo);
                    }
                }
                result.postValue(userInfos);

            }

            @Override
            public void onError(RongIMClient.ErrorCode e) {
                result.postValue(null);
            }
        });

        return result;
    }

    public MutableLiveData<Message> getMessageRouter() {
        return messageRouter;
    }


    /**
     * 退出
     */
    public void logout() {
        RongIM.getInstance().logout();
    }

    /**
     * 连接 IM 服务
     *
     * @param token
     * @param getTokenOnIncorrect
     * @param callback
     */
    public void connectIM(String token, boolean getTokenOnIncorrect, ResultCallback<String> callback) {
        /*
         *  考虑到会有后台调用此方法，所以不采用 LiveData 做返回值
         */
        RongIM.connect(token, new RongIMClient.ConnectCallback() {
            @Override
            public void onTokenIncorrect() {
                if (getTokenOnIncorrect) {
                    getToken(new ResultCallback<LoginResult>() {
                        @Override
                        public void onSuccess(LoginResult loginResult) {
                            connectIM(loginResult.token, false, callback);
                        }

                        @Override
                        public void onFail(int errorCode) {
                            callback.onFail(errorCode);
                        }
                    });
                } else {
                    if (callback != null) {
                        callback.onFail(ErrorCode.IM_ERROR.getCode());
                    } else {
                        // do nothing
                    }
                }
            }

            @Override
            public void onSuccess(String s) {
                // 连接 IM 成功后，初始化数据库
                DbManager.getInstance(context).openDb(s);
                callback.onSuccess(s);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                SLog.e(LogTag.IM, "connect error - code:" + errorCode.getValue() + ", msg:" + errorCode.getMessage());
                if (callback != null) {
                    callback.onFail(errorCode.getValue());
                } else {
                    // do nothing
                }
            }
        });
    }

    /**
     * 获取用户 IM token
     * 此接口需要在登录成功后可调用，用于在 IM 提示 token 失效时刷新 token 使用
     *
     * @param callback
     */
    private void getToken(ResultCallback<LoginResult> callback) {
        UserService userService = HttpClientManager.getInstance(context).getClient().createService(UserService.class);
        /*
         *  考虑到会有后台调用此方法，所以不采用 LiveData 做返回值
         */
        userService.getToken().enqueue(new CallBackWrapper<>(callback));
    }

    /**
     * 获取连接状态
     * @return
     */
    public RongIMClient.ConnectionStatusListener.ConnectionStatus getConnectStatus() {
        return RongIM.getInstance().getCurrentConnectionStatus();
    }

    /**
     * 被踢监听
     * @return
     */
    public LiveData<Boolean> getKickedOffline() {
        return kickedOffline;
    }
}


