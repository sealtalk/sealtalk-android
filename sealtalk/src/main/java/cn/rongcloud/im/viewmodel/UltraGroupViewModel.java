package cn.rongcloud.im.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import cn.rongcloud.im.R;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.model.UltraChannelInfo;
import cn.rongcloud.im.model.UltraGroupInfo;
import cn.rongcloud.im.model.UltraGroupMemberListResult;
import cn.rongcloud.im.task.UltraGroupTask;
import cn.rongcloud.im.ultraGroup.UltraGroupManager;
import cn.rongcloud.im.utils.SingleSourceLiveData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.rong.common.RLog;
import io.rong.imkit.ConversationEventListener;
import io.rong.imkit.IMCenter;
import io.rong.imkit.config.DataProcessor;
import io.rong.imkit.config.RongConfigCenter;
import io.rong.imkit.conversationlist.model.BaseUiConversation;
import io.rong.imkit.conversationlist.model.GatheredConversation;
import io.rong.imkit.conversationlist.model.GroupConversation;
import io.rong.imkit.conversationlist.model.PublicServiceConversation;
import io.rong.imkit.conversationlist.model.SingleConversation;
import io.rong.imkit.event.Event;
import io.rong.imkit.event.actionevent.ClearEvent;
import io.rong.imkit.event.actionevent.DeleteEvent;
import io.rong.imkit.event.actionevent.DownloadEvent;
import io.rong.imkit.event.actionevent.InsertEvent;
import io.rong.imkit.event.actionevent.MessageEventListener;
import io.rong.imkit.event.actionevent.RecallEvent;
import io.rong.imkit.event.actionevent.RefreshEvent;
import io.rong.imkit.event.actionevent.SendEvent;
import io.rong.imkit.event.actionevent.SendMediaEvent;
import io.rong.imkit.feature.resend.ResendManager;
import io.rong.imkit.model.NoticeContent;
import io.rong.imkit.notification.MessageNotificationHelper;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imkit.userinfo.model.GroupUserInfo;
import io.rong.imkit.widget.refresh.constant.RefreshState;
import io.rong.imlib.ChannelClient;
import io.rong.imlib.IRongCoreCallback;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.IRongCoreListener;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.ConversationStatus;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;
import io.rong.message.GroupNotificationMessage;
import io.rong.message.InformationNotificationMessage;
import io.rong.message.ReadReceiptMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class UltraGroupViewModel extends AndroidViewModel
        implements RongUserInfoManager.UserDataObserver {
    private final String TAG = UltraGroupViewModel.class.getSimpleName();
    private final int REFRESH_INTERVAL = 500;
    private final SharedPreferences sharedPreferences;
    protected Conversation.ConversationType[] mSupportedTypes;
    protected int mSizePerPage;
    protected long mLastSyncTime;
    protected Application mApplication;
    protected CopyOnWriteArrayList<BaseUiConversation> mUiConversationList =
            new CopyOnWriteArrayList<>();
    protected MediatorLiveData<List<BaseUiConversation>> mConversationListLiveData;
    protected DataProcessor<Conversation> mDataFilter;
    protected Handler mHandler;
    private final MutableLiveData<RongIMClient.ConnectionStatusListener.ConnectionStatus>
            mConnectionStatusLiveData = new MutableLiveData<>();
    private final MutableLiveData<NoticeContent> mNoticeContentLiveData = new MutableLiveData<>();
    private final MutableLiveData<Event.RefreshEvent> mRefreshEventLiveData =
            new MutableLiveData<>();
    private boolean isTaskScheduled;
    private String channelId = "";
    private String defaultGroupId;
    private final UltraGroupTask ultraGroupTask;
    private final SingleSourceLiveData<Resource<List<UltraGroupInfo>>> ultraGroupMemberListResult =
            new SingleSourceLiveData<>();
    private final SingleSourceLiveData<Resource<List<UltraChannelInfo>>>
            ultraGroupChannelListResult = new SingleSourceLiveData<>();
    private final SingleSourceLiveData<Resource<List<UltraGroupMemberListResult>>>
            ultraGroupMemberInfoListResult = new SingleSourceLiveData<>();
    private final MediatorLiveData<Resource<String>> createGroupResult = new MediatorLiveData<>();
    private final MediatorLiveData<Resource<String>> uploadPortrait = new MediatorLiveData<>();
    private final MediatorLiveData<List<String>> groupMemberChange = new MediatorLiveData<>();
    private final SingleSourceLiveData<Resource<List<String>>> ultraGroupMemberAddResult =
            new SingleSourceLiveData<>();
    private final SingleSourceLiveData<Resource<String>> channelCreateResult =
            new SingleSourceLiveData<>();
    private final SingleSourceLiveData<Resource<Void>> exitGroupResult =
            new SingleSourceLiveData<>();
    private final SingleSourceLiveData<Resource<Void>> dismissGroupResult =
            new SingleSourceLiveData<>();
    private final ConversationEventListener mConversationEventListener =
            new ConversationEventListener() {
                @Override
                public void onSaveDraft(
                        Conversation.ConversationType type, String targetId, String content) {
                    getConversation(type, targetId);
                }

                @Override
                public void onClearedMessage(Conversation.ConversationType type, String targetId) {
                    getConversation(type, targetId);
                }

                @Override
                public void onClearedUnreadStatus(
                        Conversation.ConversationType type, String targetId) {
                    getConversation(type, targetId);
                }

                @Override
                public void onConversationRemoved(
                        Conversation.ConversationType type, String targetId) {
                    BaseUiConversation oldItem =
                            findConversationFromList(type, targetId, mDataFilter.isGathered(type));
                    if (oldItem != null) {
                        mUiConversationList.remove(oldItem);
                        mConversationListLiveData.postValue(mUiConversationList);
                    }
                }

                @Override
                public void onOperationFailed(RongIMClient.ErrorCode code) {}

                @Override
                public void onClearConversations(
                        Conversation.ConversationType... conversationTypes) {
                    RLog.d(TAG, "onClearConversations");
                    List<Conversation.ConversationType> clearedTypes =
                            Arrays.asList(conversationTypes);
                    for (BaseUiConversation item : mUiConversationList) {
                        if (clearedTypes.contains(item.mCore.getConversationType())) {
                            mUiConversationList.remove(item);
                        }
                    }
                    mConversationListLiveData.postValue(mUiConversationList);
                }

                @Override
                public void onChannelChange(
                        String groupId,
                        String channelId,
                        IRongCoreEnum.UltraGroupChannelType type) {
                    for (BaseUiConversation baseUiConversation : mUiConversationList) {
                        if (baseUiConversation.mCore.getChannelId().equals(channelId)) {
                            baseUiConversation.mCore.setChannelType(type);
                        }
                    }
                    mConversationListLiveData.postValue(mUiConversationList);
                }

                @Override
                public void onChannelDelete(String groupId, String channelId) {
                    List<BaseUiConversation> removeList = new ArrayList<>();
                    for (BaseUiConversation baseUiConversation : mUiConversationList) {
                        if (baseUiConversation.mCore.getChannelId().equals(channelId)) {
                            removeList.add(baseUiConversation);
                        }
                    }
                    mUiConversationList.removeAll(removeList);
                    mConversationListLiveData.postValue(mUiConversationList);
                }
            };
    private final MessageEventListener mMessageEventListener =
            new MessageEventListener() {
                @Override
                public void onSendMessage(SendEvent event) {
                    if (event != null && event.getMessage() != null) {
                        getConversation(
                                event.getMessage().getConversationType(),
                                event.getMessage().getTargetId());
                    }
                }

                @Override
                public void onSendMediaMessage(SendMediaEvent event) {
                    if (event != null
                            && event.getEvent() != SendMediaEvent.PROGRESS
                            && event.getMessage() != null) {
                        getConversation(
                                event.getMessage().getConversationType(),
                                event.getMessage().getTargetId());
                    }
                }

                @Override
                public void onDownloadMessage(DownloadEvent event) {
                    if (event == null) {
                        return;
                    }
                    Conversation.ConversationType type = event.getMessage().getConversationType();
                    String targetId = event.getMessage().getTargetId();
                    BaseUiConversation oldItem =
                            findConversationFromList(type, targetId, mDataFilter.isGathered(type));
                    if (oldItem != null
                            && oldItem.mCore.getLatestMessageId()
                                    == event.getMessage().getMessageId()
                            && event.getEvent() != DownloadEvent.PROGRESS) {
                        getConversation(type, targetId);
                    }
                }

                @Override
                public void onDeleteMessage(DeleteEvent event) {
                    if (event != null) {
                        getConversation(event.getConversationType(), event.getTargetId());
                    }
                }

                @Override
                public void onRecallEvent(RecallEvent event) {
                    if (event != null) {
                        getConversation(event.getConversationType(), event.getTargetId());
                    }
                }

                @Override
                public void onRefreshEvent(RefreshEvent event) {}

                @Override
                public void onInsertMessage(InsertEvent event) {
                    if (event == null) {
                        return;
                    }
                    Conversation.ConversationType type = event.getMessage().getConversationType();
                    String targetId = event.getMessage().getTargetId();
                    String channelId = event.getMessage().getChannelId();
                    getConversation(type, targetId);
                }

                @Override
                public void onClearMessages(ClearEvent event) {
                    getConversation(event.getConversationType(), event.getTargetId());
                }
            };
    private final RongIMClient.OnReceiveMessageWrapperListener mOnReceiveMessageListener =
            new RongIMClient.OnReceiveMessageWrapperListener() {
                @Override
                public boolean onReceived(
                        Message message, int left, boolean hasPackage, boolean offline) {
                    if (!message.getConversationType()
                            .equals(Conversation.ConversationType.ULTRA_GROUP)) {
                        return false;
                    }
                    if (message.getContent() instanceof GroupNotificationMessage
                            && message.getObjectName().equals("ST:UltraGrpNtf")) {
                        sharedPreferences.edit().clear().commit();
                        UltraGroupManager.getInstance().notifyGroupChange();
                        return false;
                    }

                    Gson gson = new Gson();
                    String userJson = sharedPreferences.getString("member_list", "");
                    List<UltraGroupInfo> stringList =
                            gson.fromJson(
                                    userJson, new TypeToken<List<UltraGroupInfo>>() {}.getType());
                    boolean hasContain = false;
                    if (message.getContent() instanceof InformationNotificationMessage) {
                        if (stringList != null) {
                            for (UltraGroupInfo ultraGroupInfo : stringList) {
                                if (ultraGroupInfo.groupId.equals(message.getTargetId())) {
                                    hasContain = true;
                                    break;
                                }
                            }
                        }

                        if (!hasContain) {
                            sharedPreferences.edit().clear().commit();
                            UltraGroupManager.getInstance().notifyGroupChange();
                        }
                    }
                    getConversationList(message.getTargetId(), false, false);
                    return false;
                }
            };
    private final RongIMClient.ReadReceiptListener mReadReceiptListener =
            new RongIMClient.ReadReceiptListener() {
                @Override
                public void onReadReceiptReceived(Message message) {
                    if (message != null && message.getContent() instanceof ReadReceiptMessage) {
                        Conversation.ConversationType type = message.getConversationType();
                        BaseUiConversation oldItem =
                                findConversationFromList(
                                        type, message.getTargetId(), mDataFilter.isGathered(type));
                        if (oldItem != null
                                && type.equals(Conversation.ConversationType.PRIVATE)
                                && oldItem.mCore.getSentTime()
                                        == ((ReadReceiptMessage) message.getContent())
                                                .getLastMessageSendTime()) {
                            oldItem.mCore.setSentStatus(Message.SentStatus.READ);
                            mConversationListLiveData.postValue(mUiConversationList);
                        }
                    }
                }

                @Override
                public void onMessageReceiptRequest(
                        Conversation.ConversationType type, String targetId, String messageUId) {}

                @Override
                public void onMessageReceiptResponse(
                        Conversation.ConversationType type,
                        String targetId,
                        String messageUId,
                        HashMap<String, Long> respondUserIdList) {}
            };
    private final RongIMClient.OnRecallMessageListener mOnRecallMessageListener =
            (message, recallNotificationMessage) -> {
                if (message != null) {
                    getConversation(message.getConversationType(), message.getTargetId());
                }
                return false;
            };
    private final RongIMClient.SyncConversationReadStatusListener
            mSyncConversationReadStatusListener =
                    new RongIMClient.SyncConversationReadStatusListener() {
                        @Override
                        public void onSyncConversationReadStatus(
                                Conversation.ConversationType type, String targetId) {
                            BaseUiConversation oldItem =
                                    findConversationFromList(
                                            type, targetId, mDataFilter.isGathered(type));
                            if (oldItem != null) {
                                oldItem.mCore.setUnreadMessageCount(0);
                                mConversationListLiveData.postValue(mUiConversationList);
                            }
                        }
                    };
    private final RongIMClient.ConnectionStatusListener mConnectionStatusListener =
            new RongIMClient.ConnectionStatusListener() {
                @Override
                public void onChanged(ConnectionStatus status) {
                    mConnectionStatusLiveData.postValue(status);
                    if (status.equals(ConnectionStatus.CONNECTED)) {
                        if (defaultGroupId != null) {
                            getConversationList(defaultGroupId, false, false);
                        }
                    }
                    // 更新连接状态通知信息
                    updateNoticeContent(status);
                }
            };
    private final RongIMClient.ConversationStatusListener mConversationStatusListener =
            conversationStatus -> onConversationStatusChange(conversationStatus);

    private final IRongCoreListener.UltraGroupMessageChangeListener
            ultraGroupMessageChangeListener =
                    new IRongCoreListener.UltraGroupMessageChangeListener() {
                        @Override
                        public void onUltraGroupMessageExpansionUpdated(List<Message> messages) {}

                        @Override
                        public void onUltraGroupMessageModified(List<Message> messages) {}

                        @Override
                        public void onUltraGroupMessageRecalled(List<Message> messages) {
                            new Handler(Looper.getMainLooper())
                                    .post(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (messages != null && !messages.isEmpty()) {
                                                        for (Message msg : messages) {
                                                            getConversation(
                                                                    msg.getConversationType(),
                                                                    msg.getTargetId(),
                                                                    msg.getChannelId());
                                                        }
                                                    }
                                                }
                                            });
                        }
                    };

    public UltraGroupViewModel(Application application) {
        super(application);
        mApplication = application;
        ultraGroupTask = new UltraGroupTask(application);
        mHandler = new Handler(Looper.getMainLooper());
        mSupportedTypes =
                RongConfigCenter.conversationListConfig().getDataProcessor().supportedTypes();
        mSizePerPage = RongConfigCenter.conversationListConfig().getConversationCountPerPage();
        mDataFilter = RongConfigCenter.conversationListConfig().getDataProcessor();
        sharedPreferences = application.getSharedPreferences("ultra", Context.MODE_PRIVATE);
        mConversationListLiveData = new MediatorLiveData<>();
        RongUserInfoManager.getInstance().addUserDataObserver(this);
        IMCenter.getInstance().addOnReceiveMessageListener(mOnReceiveMessageListener);
        IMCenter.getInstance().addConnectionStatusListener(mConnectionStatusListener);
        IMCenter.getInstance().addConversationStatusListener(mConversationStatusListener);
        IMCenter.getInstance().addReadReceiptListener(mReadReceiptListener);
        IMCenter.getInstance()
                .addSyncConversationReadStatusListener(mSyncConversationReadStatusListener);
        IMCenter.getInstance().addOnRecallMessageListener(mOnRecallMessageListener);
        IMCenter.getInstance().addConversationEventListener(mConversationEventListener);
        IMCenter.getInstance().addMessageEventListener(mMessageEventListener);
        IMManager.getInstance().addUltraGroupMessageChangeListener(ultraGroupMessageChangeListener);
    }

    /**
     * 从本地数据库获取会话列表。 此处借鉴前端的函数节流思想，在 {@link #REFRESH_INTERVAL} 时间内，丢弃掉其它触发，只做一次执行。 以便提高接受大量消息时的刷新性能。
     *
     * @param loadMore 是否根据上次同步的时间戳拉取更多会话。 false: 从数据库拉取最新 N 条会话。true: 根据 UI 上最后一条会话的时间戳，继续拉取之前的 N
     *     条会话。
     * @param isEventManual 是否是用还手动触发的刷新获取，手动触发的需要主动关闭下
     */
    public void getConversationList(
            final String groupId, final boolean loadMore, final boolean isEventManual) {
        if (isTaskScheduled) {
            return;
        }
        isTaskScheduled = true;
        mHandler.postDelayed(
                () -> {
                    isTaskScheduled = false;
                    ChannelClient.getInstance()
                            .getConversationListForAllChannel(
                                    Conversation.ConversationType.ULTRA_GROUP,
                                    groupId,
                                    new IRongCoreCallback.ResultCallback<List<Conversation>>() {
                                        @Override
                                        public void onSuccess(List<Conversation> conversations) {
                                            mUiConversationList.clear();
                                            if (isEventManual) {
                                                if (loadMore) {
                                                    mRefreshEventLiveData.postValue(
                                                            new Event.RefreshEvent(
                                                                    RefreshState.LoadFinish));
                                                } else {
                                                    mRefreshEventLiveData.postValue(
                                                            new Event.RefreshEvent(
                                                                    RefreshState.RefreshFinish));
                                                }
                                            }
                                            if (conversations == null
                                                    || conversations.size() == 0) {
                                                mConversationListLiveData.postValue(
                                                        mUiConversationList);
                                                return;
                                            }
                                            mLastSyncTime =
                                                    conversations
                                                            .get(conversations.size() - 1)
                                                            .getSentTime();
                                            CopyOnWriteArrayList<Conversation> copyList =
                                                    new CopyOnWriteArrayList<>(conversations);
                                            List<Conversation> filterResult =
                                                    mDataFilter.filtered(copyList);
                                            if (filterResult != null && filterResult.size() > 0) {
                                                for (Conversation conversation : filterResult) {
                                                    boolean isGathered =
                                                            mDataFilter.isGathered(
                                                                    conversation
                                                                            .getConversationType());
                                                    BaseUiConversation oldItem =
                                                            findConversationFromList(
                                                                    conversation
                                                                            .getConversationType(),
                                                                    conversation.getTargetId(),
                                                                    conversation.getChannelId(),
                                                                    isGathered);

                                                    if (oldItem != null) {
                                                        oldItem.onConversationUpdate(conversation);
                                                    } else {
                                                        if (isGathered) {
                                                            mUiConversationList.add(
                                                                    new GatheredConversation(
                                                                            mApplication
                                                                                    .getApplicationContext(),
                                                                            conversation));
                                                        } else if (conversation
                                                                        .getConversationType()
                                                                        .equals(
                                                                                Conversation
                                                                                        .ConversationType
                                                                                        .GROUP)
                                                                || conversation
                                                                        .getConversationType()
                                                                        .equals(
                                                                                Conversation
                                                                                        .ConversationType
                                                                                        .ULTRA_GROUP)) {
                                                            mUiConversationList.add(
                                                                    new GroupConversation(
                                                                            mApplication
                                                                                    .getApplicationContext(),
                                                                            conversation));
                                                        } else if (conversation
                                                                        .getConversationType()
                                                                        .equals(
                                                                                Conversation
                                                                                        .ConversationType
                                                                                        .PUBLIC_SERVICE)
                                                                || conversation
                                                                        .getConversationType()
                                                                        .equals(
                                                                                Conversation
                                                                                        .ConversationType
                                                                                        .APP_PUBLIC_SERVICE)) {
                                                            mUiConversationList.add(
                                                                    new PublicServiceConversation(
                                                                            mApplication
                                                                                    .getApplicationContext(),
                                                                            conversation));
                                                        } else {
                                                            mUiConversationList.add(
                                                                    new SingleConversation(
                                                                            mApplication
                                                                                    .getApplicationContext(),
                                                                            conversation));
                                                        }
                                                    }
                                                }
                                                sort();
                                                RLog.d(TAG, "conversation list onChanged 4444.");
                                                mConversationListLiveData.postValue(
                                                        mUiConversationList);
                                            }
                                        }

                                        @Override
                                        public void onError(IRongCoreEnum.CoreErrorCode e) {
                                            if (loadMore) {
                                                mRefreshEventLiveData.postValue(
                                                        new Event.RefreshEvent(
                                                                RefreshState.LoadFinish));
                                            } else {
                                                mRefreshEventLiveData.postValue(
                                                        new Event.RefreshEvent(
                                                                RefreshState.RefreshFinish));
                                            }
                                        }
                                    });
                },
                REFRESH_INTERVAL);
    }

    protected BaseUiConversation findConversationFromList(
            Conversation.ConversationType conversationType,
            String targetId,
            String channelId,
            boolean isGathered) {
        for (BaseUiConversation uiConversation : mUiConversationList) {
            if (isGathered
                    && uiConversation instanceof GatheredConversation
                    && Objects.equals(
                            conversationType, uiConversation.mCore.getConversationType())) {
                return uiConversation;
            } else if (!isGathered) {
                if (uiConversation.mCore.getConversationType().equals(conversationType)
                        && Objects.equals(uiConversation.mCore.getTargetId(), targetId)
                        && Objects.equals(uiConversation.mCore.getChannelId(), channelId)) {
                    return uiConversation;
                }
            }
        }
        return null;
    }

    protected BaseUiConversation findConversationFromList(
            Conversation.ConversationType conversationType, String targetId, boolean isGathered) {
        for (BaseUiConversation uiConversation : mUiConversationList) {
            if (isGathered
                    && uiConversation instanceof GatheredConversation
                    && Objects.equals(
                            conversationType, uiConversation.mCore.getConversationType())) {
                return uiConversation;
            } else if (!isGathered) {
                if (uiConversation.mCore.getConversationType().equals(conversationType)
                        && Objects.equals(uiConversation.mCore.getTargetId(), targetId)) {
                    return uiConversation;
                }
            }
        }
        return null;
    }

    // conversationList排序规律：
    // 1. 首先是top会话，按时间顺序排列。
    // 2. 然后非top会话也是按时间排列。
    protected void sort() {
        List temp = Arrays.asList(mUiConversationList.toArray());
        Collections.sort(
                temp,
                (Comparator<BaseUiConversation>)
                        (o1, o2) -> {
                            if (o1.mCore.isTop() && o2.mCore.isTop()
                                    || !o1.mCore.isTop() && !o2.mCore.isTop()) {
                                return Long.compare(o2.mCore.getSentTime(), o1.mCore.getSentTime());
                            } else if (o1.mCore.isTop() && !o2.mCore.isTop()) {
                                return -1;
                            } else if (!o1.mCore.isTop() && o2.mCore.isTop()) {
                                return 1;
                            }
                            return 0;
                        });
        mUiConversationList.clear();
        mUiConversationList.addAll(temp);
    }

    /**
     * 会话状态（置顶或免打扰）发生变化时的回调。
     *
     * @param statuses 发生变更的会话状态。
     */
    private void onConversationStatusChange(ConversationStatus[] statuses) {
        for (ConversationStatus status : statuses) {
            Conversation.ConversationType type = status.getConversationType();
            BaseUiConversation oldItem =
                    findConversationFromList(
                            type,
                            status.getTargetId(),
                            status.getChannelId(),
                            mDataFilter.isGathered(type));
            if (oldItem != null) {
                if (status.getStatus().get(ConversationStatus.TOP_KEY) != null) {
                    oldItem.mCore.setTop(status.isTop());
                }
                if (status.getStatus().get(ConversationStatus.NOTIFICATION_KEY) != null) {
                    oldItem.mCore.setNotificationStatus(status.getNotifyStatus());
                    oldItem.mCore.setPushNotificationLevel(
                            status.getNotificationLevel().getValue());
                }
                MessageNotificationHelper.updateLevelMap(oldItem.mCore);
                sort();
                mConversationListLiveData.postValue(mUiConversationList);
            } else {
                getConversation(type, status.getTargetId());
            }
        }
    }

    private void getConversation(Conversation.ConversationType type, String targetId) {
        getConversation(type, targetId, channelId);
    }

    private void getConversation(
            Conversation.ConversationType type, String targetId, String channelID) {
        ChannelClient.getInstance()
                .getConversation(
                        type,
                        targetId,
                        channelID,
                        new IRongCoreCallback.ResultCallback<Conversation>() {
                            @Override
                            public void onSuccess(Conversation conversation) {
                                if (conversation == null) {
                                    return;
                                }
                                if (Objects.equals(
                                                conversation.getSentStatus(),
                                                Message.SentStatus.FAILED)
                                        && ResendManager.getInstance()
                                                .needResend(conversation.getLatestMessageId())) {
                                    conversation.setSentStatus(Message.SentStatus.SENDING);
                                }
                                MessageNotificationHelper.updateLevelMap(conversation);
                                updateByConversation(conversation);
                            }

                            @Override
                            public void onError(IRongCoreEnum.CoreErrorCode e) {
                                // Todo 数据获取失败，下拉刷新
                            }
                        });
    }

    protected void updateByConversation(Conversation conversation) {
        if (conversation == null) {
            return;
        }
        List<Conversation> list = new CopyOnWriteArrayList<>();
        list.add(conversation);
        List<Conversation> filterList = mDataFilter.filtered(list);
        if (filterList != null
                && filterList.size() > 0
                && isSupported(conversation.getConversationType())) {
            BaseUiConversation oldItem =
                    findConversationFromList(
                            conversation.getConversationType(),
                            conversation.getTargetId(),
                            conversation.getChannelId(),
                            mDataFilter.isGathered(conversation.getConversationType()));
            if (oldItem != null) {
                oldItem.onConversationUpdate(conversation);
            } else {
                if (mDataFilter.isGathered(conversation.getConversationType())) {
                    mUiConversationList.add(
                            new GatheredConversation(
                                    mApplication.getApplicationContext(), conversation));
                } else if (conversation
                                .getConversationType()
                                .equals(Conversation.ConversationType.GROUP)
                        || conversation
                                .getConversationType()
                                .equals(Conversation.ConversationType.ULTRA_GROUP)) {
                    mUiConversationList.add(
                            new GroupConversation(
                                    mApplication.getApplicationContext(), conversation));
                } else if (conversation
                                .getConversationType()
                                .equals(Conversation.ConversationType.PUBLIC_SERVICE)
                        || conversation
                                .getConversationType()
                                .equals(Conversation.ConversationType.APP_PUBLIC_SERVICE)) {
                    mUiConversationList.add(
                            new PublicServiceConversation(
                                    mApplication.getApplicationContext(), conversation));
                } else {
                    mUiConversationList.add(
                            new SingleConversation(
                                    mApplication.getApplicationContext(), conversation));
                }
            }
            sort();
            mConversationListLiveData.postValue(mUiConversationList);
        }
    }

    protected boolean isSupported(Conversation.ConversationType type) {
        if (mSupportedTypes == null) {
            return false;
        }
        for (Conversation.ConversationType conversationType : mSupportedTypes) {
            if (conversationType.equals(type)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onCleared() {
        IMCenter.getInstance().removeConnectionStatusListener(mConnectionStatusListener);
        IMCenter.getInstance().removeOnReceiveMessageListener(mOnReceiveMessageListener);
        IMCenter.getInstance().removeConversationStatusListener(mConversationStatusListener);
        IMCenter.getInstance().removeMessageEventListener(mMessageEventListener);
        IMCenter.getInstance().removeReadReceiptListener(mReadReceiptListener);
        IMCenter.getInstance().removeOnRecallMessageListener(mOnRecallMessageListener);
        IMCenter.getInstance().removeConversationEventListener(mConversationEventListener);
        IMCenter.getInstance()
                .removeSyncConversationReadStatusListeners(mSyncConversationReadStatusListener);
        IMManager.getInstance()
                .removeUltraGroupMessageChangeListener(ultraGroupMessageChangeListener);
    }

    public MediatorLiveData<List<BaseUiConversation>> getConversationListLiveData() {
        return mConversationListLiveData;
    }

    /** 获取连接状态通知内容 */
    public LiveData<NoticeContent> getNoticeContentLiveData() {
        return mNoticeContentLiveData;
    }

    /**
     * 获取刷新事件 LiveData
     *
     * @return 刷新事件
     */
    public LiveData<Event.RefreshEvent> getRefreshEventLiveData() {
        return mRefreshEventLiveData;
    }

    /** 更新连接状态通知 */
    private void updateNoticeContent(
            RongIMClient.ConnectionStatusListener.ConnectionStatus status) {
        NoticeContent noticeContent = new NoticeContent();
        String content = null;
        boolean isShowContent = true;
        int resId = 0;

        Resources resources = mApplication.getResources();
        if (!RongConfigCenter.conversationListConfig().isEnableConnectStateNotice()) {
            RLog.e(TAG, "rc_is_show_warning_notification is disabled.");
            return;
        }
        if (status.equals(
                RongIMClient.ConnectionStatusListener.ConnectionStatus.NETWORK_UNAVAILABLE)) {
            content = resources.getString(R.string.rc_conversation_list_notice_network_unavailable);
            resId = R.drawable.rc_ic_error_notice;
        } else if (status.equals(
                RongIMClient.ConnectionStatusListener.ConnectionStatus
                        .KICKED_OFFLINE_BY_OTHER_CLIENT)) {
            content = resources.getString(R.string.rc_conversation_list_notice_kicked);
            resId = R.drawable.rc_ic_error_notice;
        } else if (status.equals(
                RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED)) {
            isShowContent = false;
        } else if (status.equals(
                RongIMClient.ConnectionStatusListener.ConnectionStatus.UNCONNECTED)) {
            content = resources.getString(R.string.rc_conversation_list_notice_disconnect);
            resId = R.drawable.rc_ic_error_notice;
        } else if (status.equals(RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTING)
                || status.equals(RongIMClient.ConnectionStatusListener.ConnectionStatus.SUSPEND)) {
            content = resources.getString(R.string.rc_conversation_list_notice_connecting);
            resId = R.drawable.rc_conversationlist_notice_connecting_animated;
        }

        noticeContent.setContent(content);
        noticeContent.setShowNotice(isShowContent);
        noticeContent.setIconResId(resId);

        mNoticeContentLiveData.postValue(noticeContent);
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public void setDefaultGroupId(String defaultGroupId) {
        this.defaultGroupId = defaultGroupId;
    }

    public void getUltraGroupMemberList() {
        ultraGroupMemberListResult.setSource(ultraGroupTask.getUltraGroupMemberList());
    }

    public void getUltraGroupChannelList(String groupId) {
        ultraGroupChannelListResult.setSource(ultraGroupTask.getUltraGroupChannelList(groupId));
    }

    public void createUltraGroup(String groupName, Uri portraitUri, String summary) {
        LiveData<Resource<String>> createGroupResource =
                ultraGroupTask.createUltraGroup(groupName, portraitUri, summary);

        createGroupResult.addSource(
                createGroupResource,
                groupResultResource -> {
                    if (groupResultResource.status != Status.LOADING) {
                        createGroupResult.removeSource(createGroupResource);
                    }
                    // 判断是否创建群组成功
                    if (groupResultResource.status == Status.SUCCESS) {
                        createGroupResult.setValue(Resource.success(groupResultResource.data));
                    } else {
                        createGroupResult.setValue(Resource.error(groupResultResource.code, null));
                    }
                });
    }

    /** 完成上传 */
    public void nextToUploadPortraitResult(Uri groupPortrait) {
        // 进行上传群组头像
        LiveData<Resource<String>> uploadResource =
                ultraGroupTask.uploadAndSetGroupPortrait(groupPortrait);
        uploadPortrait.addSource(
                uploadResource,
                resource -> {
                    if (resource.status != Status.LOADING) {
                        uploadPortrait.removeSource(uploadResource);
                    }
                    // 判断是否上传头像成功
                    if (resource.status == Status.SUCCESS) {
                        uploadPortrait.setValue(Resource.success(resource.data));
                    } else {
                        uploadPortrait.setValue(Resource.error(resource.code, null));
                    }
                });
    }

    public SingleSourceLiveData<Resource<List<UltraGroupInfo>>> getUltraGroupMemberListResult() {
        return ultraGroupMemberListResult;
    }

    public MediatorLiveData<Resource<String>> getCreateGroupResult() {
        return createGroupResult;
    }

    public MediatorLiveData<Resource<String>> getUploadPortrait() {
        return uploadPortrait;
    }

    public void addUltraGroupMember(String groupId, List<String> memberList) {
        ultraGroupMemberAddResult.setSource(
                ultraGroupTask.addUltraGroupMember(groupId, memberList));
    }

    public void createChannel(
            String groupId, String channelName, IRongCoreEnum.UltraGroupChannelType type) {
        channelCreateResult.setSource(
                ultraGroupTask.ultraGroupChannelCreate(groupId, channelName, type));
    }

    public SingleSourceLiveData<Resource<List<String>>> getUltraGroupMemberAddResult() {
        return ultraGroupMemberAddResult;
    }

    public SingleSourceLiveData<Resource<String>> getChannelCreateResult() {
        return channelCreateResult;
    }

    public void getUltraGroupMemberInfoList(String groupId, int pageNum, int limit) {
        ultraGroupMemberInfoListResult.setSource(
                ultraGroupTask.getUltraGroupMemberInfoList(groupId, pageNum, limit));
    }

    public SingleSourceLiveData<Resource<List<UltraGroupMemberListResult>>>
            getUltraGroupMemberInfoListResult() {
        return ultraGroupMemberInfoListResult;
    }

    public void exitUltraGroup(String groupId) {
        exitGroupResult.setSource(ultraGroupTask.quitUltraGroup(groupId));
    }

    public SingleSourceLiveData<Resource<Void>> getExitGroupResult() {
        return exitGroupResult;
    }

    public void dismissUltraGroup(String groupId) {
        dismissGroupResult.setSource(ultraGroupTask.dismissUltraGroup(groupId));
    }

    public SingleSourceLiveData<Resource<Void>> getDismissGroupResult() {
        return dismissGroupResult;
    }

    public SingleSourceLiveData<Resource<List<UltraChannelInfo>>> getUltraGroupChannelListResult() {
        return ultraGroupChannelListResult;
    }

    /**
     * 从本地数据库获取会话列表。 此处借鉴前端的函数节流思想，在 {@link #REFRESH_INTERVAL} 时间内，丢弃掉其它触发，只做一次执行。 以便提高接受大量消息时的刷新性能。
     *
     * @param loadMore 是否根据上次同步的时间戳拉取更多会话。 false: 从数据库拉取最新 N 条会话。true: 根据 UI 上最后一条会话的时间戳，继续拉取之前的 N
     *     条会话。
     * @param isEventManual 是否是用还手动触发的刷新获取，手动触发的需要主动关闭下
     */
    public void getConversationList(final boolean loadMore, final boolean isEventManual) {
        if (isTaskScheduled) {
            return;
        }
        isTaskScheduled = true;
        mHandler.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        long timestamp = 0;
                        isTaskScheduled = false;
                        if (loadMore) {
                            timestamp = mLastSyncTime;
                        }
                        RongIMClient.getInstance()
                                .getConversationListByPage(
                                        new RongIMClient.ResultCallback<List<Conversation>>() {
                                            @Override
                                            public void onSuccess(
                                                    List<Conversation> conversations) {
                                                if (isEventManual) {
                                                    if (loadMore) {
                                                        mRefreshEventLiveData.postValue(
                                                                new Event.RefreshEvent(
                                                                        RefreshState.LoadFinish));
                                                    } else {
                                                        mRefreshEventLiveData.postValue(
                                                                new Event.RefreshEvent(
                                                                        RefreshState
                                                                                .RefreshFinish));
                                                    }
                                                }
                                                if (conversations == null
                                                        || conversations.size() == 0) {
                                                    return;
                                                }
                                                RLog.d(
                                                        TAG,
                                                        "getConversationListByPage. size:"
                                                                + conversations.size());
                                                mLastSyncTime =
                                                        conversations
                                                                .get(conversations.size() - 1)
                                                                .getSentTime();
                                                CopyOnWriteArrayList<Conversation> copyList =
                                                        new CopyOnWriteArrayList<>(conversations);
                                                List<Conversation> filterResult =
                                                        mDataFilter.filtered(copyList);
                                                if (filterResult != null
                                                        && filterResult.size() > 0) {
                                                    for (Conversation conversation : filterResult) {
                                                        boolean isGathered =
                                                                mDataFilter.isGathered(
                                                                        conversation
                                                                                .getConversationType());
                                                        BaseUiConversation oldItem =
                                                                findConversationFromList(
                                                                        conversation
                                                                                .getConversationType(),
                                                                        conversation.getTargetId(),
                                                                        isGathered);

                                                        if (oldItem != null) {
                                                            oldItem.onConversationUpdate(
                                                                    conversation);
                                                        } else {
                                                            if (isGathered) {
                                                                mUiConversationList.add(
                                                                        new GatheredConversation(
                                                                                mApplication
                                                                                        .getApplicationContext(),
                                                                                conversation));
                                                            } else if (conversation
                                                                            .getConversationType()
                                                                            .equals(
                                                                                    Conversation
                                                                                            .ConversationType
                                                                                            .GROUP)
                                                                    || conversation
                                                                            .getConversationType()
                                                                            .equals(
                                                                                    Conversation
                                                                                            .ConversationType
                                                                                            .ULTRA_GROUP)) {
                                                                mUiConversationList.add(
                                                                        new GroupConversation(
                                                                                mApplication
                                                                                        .getApplicationContext(),
                                                                                conversation));
                                                            } else if (conversation
                                                                            .getConversationType()
                                                                            .equals(
                                                                                    Conversation
                                                                                            .ConversationType
                                                                                            .PUBLIC_SERVICE)
                                                                    || conversation
                                                                            .getConversationType()
                                                                            .equals(
                                                                                    Conversation
                                                                                            .ConversationType
                                                                                            .APP_PUBLIC_SERVICE)) {
                                                                mUiConversationList.add(
                                                                        new PublicServiceConversation(
                                                                                mApplication
                                                                                        .getApplicationContext(),
                                                                                conversation));
                                                            } else {
                                                                mUiConversationList.add(
                                                                        new SingleConversation(
                                                                                mApplication
                                                                                        .getApplicationContext(),
                                                                                conversation));
                                                            }
                                                        }
                                                    }
                                                    sort();
                                                    RLog.d(
                                                            TAG,
                                                            "conversation list onChanged. 77777");
                                                    mConversationListLiveData.postValue(
                                                            mUiConversationList);
                                                }
                                            }

                                            @Override
                                            public void onError(RongIMClient.ErrorCode e) {
                                                if (loadMore) {
                                                    mRefreshEventLiveData.postValue(
                                                            new Event.RefreshEvent(
                                                                    RefreshState.LoadFinish));
                                                } else {
                                                    mRefreshEventLiveData.postValue(
                                                            new Event.RefreshEvent(
                                                                    RefreshState.RefreshFinish));
                                                }
                                            }
                                        },
                                        timestamp,
                                        mSizePerPage,
                                        mSupportedTypes);
                    }
                },
                REFRESH_INTERVAL);
    }

    @Override
    public void onUserUpdate(UserInfo info) {
        if (info == null) {
            return;
        }
        for (BaseUiConversation uiConversation : mUiConversationList) {
            uiConversation.onUserInfoUpdate(info);
        }
        refreshConversationList();
    }

    @Override
    public void onGroupUpdate(Group group) {
        for (BaseUiConversation uiConversation : mUiConversationList) {
            uiConversation.onGroupInfoUpdate(group);
        }
        refreshConversationList();
    }

    @Override
    public void onGroupUserInfoUpdate(GroupUserInfo groupUserInfo) {
        for (BaseUiConversation uiConversation : mUiConversationList) {
            uiConversation.onGroupMemberUpdate(groupUserInfo);
        }
        refreshConversationList();
    }

    private void refreshConversationList() {
        if (Thread.currentThread().equals(Looper.getMainLooper().getThread())) {
            mConversationListLiveData.setValue(mUiConversationList);
        } else {
            mConversationListLiveData.postValue(mUiConversationList);
        }
    }

    public LiveData<Boolean> changeChannelType(String groupId, String channelId, int type) {
        MediatorLiveData<Boolean> liveData = new MediatorLiveData<Boolean>();
        liveData.addSource(
                ultraGroupTask.changeChannelType(groupId, channelId, type),
                new Observer<Resource<Boolean>>() {
                    @Override
                    public void onChanged(Resource<Boolean> booleanResource) {
                        if (booleanResource.status == Status.SUCCESS) {
                            UltraChannelInfo ultraChannelInfo =
                                    UltraGroupManager.getInstance()
                                            .getUltraChannelInfo(groupId, channelId);
                            if (ultraChannelInfo != null) {
                                ultraChannelInfo.setType(type);
                                UltraGroupManager.getInstance()
                                        .refreshUltraChannelInfo(
                                                mApplication.getBaseContext(),
                                                groupId,
                                                ultraChannelInfo);
                            }
                            liveData.setValue(true);
                        } else if (booleanResource.status == Status.ERROR) {
                            liveData.setValue(false);
                        }
                    }
                });
        return liveData;
    }

    public LiveData<Boolean> delChannelUsers(
            String groupId, String channelId, List<String> memberIds) {
        MediatorLiveData<Boolean> liveData = new MediatorLiveData<>();
        liveData.addSource(
                ultraGroupTask.delChannelUsers(groupId, channelId, memberIds),
                new Observer<Resource<Boolean>>() {
                    @Override
                    public void onChanged(Resource<Boolean> booleanResource) {
                        if (booleanResource.status == Status.SUCCESS) {
                            queryChannelMembers(groupId, channelId);
                            liveData.setValue(true);
                        } else if (booleanResource.status == Status.ERROR) {
                            liveData.setValue(false);
                        }
                    }
                });
        return liveData;
    }

    public LiveData<Boolean> delChannel(String groupId, String channelId) {
        MediatorLiveData<Boolean> liveData = new MediatorLiveData<>();
        liveData.addSource(
                ultraGroupTask.delChannel(groupId, channelId),
                new Observer<Resource<Boolean>>() {
                    @Override
                    public void onChanged(Resource<Boolean> booleanResource) {
                        if (booleanResource.status == Status.SUCCESS) {
                            liveData.setValue(true);
                        } else if (booleanResource.status == Status.ERROR) {
                            liveData.setValue(false);
                        }
                    }
                });
        return liveData;
    }

    public LiveData<Boolean> addChannelUsers(
            String groupId, String channelId, List<String> memberIds) {
        MediatorLiveData<Boolean> liveData = new MediatorLiveData<>();
        liveData.addSource(
                ultraGroupTask.addChannelUsers(groupId, channelId, memberIds),
                new Observer<Resource<Boolean>>() {
                    @Override
                    public void onChanged(Resource<Boolean> booleanResource) {
                        if (booleanResource.status == Status.SUCCESS) {
                            queryChannelMembers(groupId, channelId);
                            liveData.setValue(true);
                        } else if (booleanResource.status == Status.ERROR) {
                            liveData.setValue(false);
                        }
                    }
                });
        return liveData;
    }

    public LiveData<List<String>> obChannelMembersChange() {
        return groupMemberChange;
    }

    public void queryChannelMembers(String groupId, String channelId) {
        final LiveData<Resource<List<String>>> channelUsers =
                ultraGroupTask.getChannelUsers(groupId, channelId, 0, 100);
        groupMemberChange.addSource(
                channelUsers,
                new Observer<Resource<List<String>>>() {
                    @Override
                    public void onChanged(Resource<List<String>> listResource) {

                        if (listResource.status == Status.SUCCESS) {
                            if (listResource.data != null) {
                                groupMemberChange.setValue(listResource.data);
                            } else {
                                groupMemberChange.setValue(Collections.emptyList());
                            }
                            groupMemberChange.removeSource(channelUsers);
                        } else if (listResource.status == Status.ERROR) {
                            groupMemberChange.setValue(Collections.emptyList());
                            groupMemberChange.removeSource(channelUsers);
                        }
                    }
                });
    }
}
