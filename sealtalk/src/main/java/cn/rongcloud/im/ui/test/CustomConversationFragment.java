package cn.rongcloud.im.ui.test;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import cn.rongcloud.im.R;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.utils.ToastUtils;
import io.rong.common.RLog;
import io.rong.imkit.IMCenter;
import io.rong.imkit.MessageItemLongClickAction;
import io.rong.imkit.MessageItemLongClickActionManager;
import io.rong.imkit.config.RongConfigCenter;
import io.rong.imkit.conversation.ConversationFragment;
import io.rong.imkit.conversation.messgelist.viewmodel.MessageViewModel;
import io.rong.imkit.event.actionevent.DeleteEvent;
import io.rong.imkit.event.actionevent.RecallEvent;
import io.rong.imkit.model.UiMessage;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imlib.ChannelClient;
import io.rong.imlib.IRongCoreCallback;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.IRongCoreListener;
import io.rong.imlib.ReadReceiptV2Manager;
import io.rong.imlib.RongCoreClient;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.BlockedMessageInfo;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.GroupReadReceiptInfoV2;
import io.rong.imlib.model.HistoryMessageOption;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageBlockType;
import io.rong.message.RecallNotificationMessage;
import io.rong.message.TextMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** 自定义ConversationListFragment 供Debug模式下测试使用 */
public class CustomConversationFragment extends ConversationFragment
        implements IRongCoreListener.UltraGroupMessageChangeListener {

    static final String TAG = CustomConversationFragment.class.getCanonicalName();
    private Conversation.ConversationType currentConversationType;
    private String targetId;
    private MessageViewModel messageViewModel;
    private MessageItemLongClickAction modifyAction;
    private MessageItemLongClickAction updateExpansionAction;
    private MessageItemLongClickAction removeExpansionAction;
    private MessageItemLongClickAction recallAction;
    private MessageItemLongClickAction recallAndDeleteAction;
    private MessageItemLongClickAction pullRemoteAction;

    private Handler mainHandler = new Handler(Looper.getMainLooper());

    public CustomConversationFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() == null || getActivity().getIntent() == null) {
            return;
        }
        String type = getActivity().getIntent().getStringExtra(RouteUtils.CONVERSATION_TYPE);
        targetId = getActivity().getIntent().getStringExtra(RouteUtils.TARGET_ID);
        currentConversationType =
                Conversation.ConversationType.valueOf(type.toUpperCase(Locale.US));
        messageViewModel = new ViewModelProvider(this).get(MessageViewModel.class);
        messageViewModel
                .getUiMessageLiveData()
                .observeForever(
                        new Observer<List<UiMessage>>() {
                            @Override
                            public void onChanged(List<UiMessage> uiMessages) {
                                List<Message> messageList = new ArrayList<>();
                                for (UiMessage uiMessage : uiMessages) {
                                    messageList.add(uiMessage.getMessage());
                                }
                                sendResponse(messageList);
                            }
                        });

        init(messageViewModel);

        IMManager.getInstance().addUltraGroupMessageChangeListener(this);
        initLongClick();
        RongIMClient.getInstance()
                .setMessageBlockListener(
                        new IRongCoreListener.MessageBlockListener() {
                            @Override
                            public void onMessageBlock(BlockedMessageInfo info) {
                                if (getActivity() == null || getActivity().isFinishing()) {
                                    return;
                                }
                                Map<Integer, String> map = new HashMap<>();
                                map.put(MessageBlockType.UNKNOWN.value, "未知类型");
                                map.put(MessageBlockType.BLOCK_GLOBAL.value, " 全局敏感词");
                                map.put(MessageBlockType.BLOCK_CUSTOM.value, "自定义敏感词拦截");
                                map.put(MessageBlockType.BLOCK_THIRD_PATY.value, "第三方审核拦截");
                                StringBuilder builder = new StringBuilder();
                                builder.append("会话类型=" + info.getConversationType().getName())
                                        .append("\n")
                                        .append("会话ID=" + info.getTargetId())
                                        .append("\n")
                                        .append("channelID=" + info.getChannelId())
                                        .append("\n")
                                        .append("被拦截的消息ID=" + info.getBlockMsgUId())
                                        .append("\n")
                                        .append(
                                                "被拦截原因的类型="
                                                        + info.getType().value
                                                        + " ("
                                                        + map.get(info.getType().value)
                                                        + ")")
                                        .append("消息触发源类型=" + info.getSourceType())
                                        .append("\n")
                                        .append("消息触发源内容=" + info.getSourceContent())
                                        .append("\n");

                                new AlertDialog.Builder(
                                                getActivity(),
                                                AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                        .setMessage(builder.toString())
                                        .setCancelable(true)
                                        .show();
                            }
                        });
    }

    private void init(MessageViewModel messageViewModel) {
        ReadReceiptV2Manager.setGroupReadReceiptV2Listener(
                new ReadReceiptV2Manager.GroupReadReceiptV2Listener() {
                    @Override
                    public void onReadReceiptReceived(Message message) {}

                    @Override
                    public void onMessageReceiptResponse(
                            Conversation.ConversationType type,
                            String targetId,
                            String channelId,
                            String messageUId,
                            int readCount,
                            int totalCount) {
                        refreshMessage(messageUId, messageViewModel.getUiMessages());
                    }
                });
    }

    private void refreshMessage(String messageUId, List<UiMessage> uiMessagesList) {
        if (uiMessagesList == null || uiMessagesList.isEmpty()) {
            return;
        }
        for (UiMessage uiMessage : uiMessagesList) {
            if (TextUtils.equals(uiMessage.getUId(), messageUId)) {
                RongIMClient.getInstance()
                        .getMessageByUid(
                                messageUId,
                                new RongIMClient.ResultCallback<Message>() {
                                    @Override
                                    public void onSuccess(Message message) {
                                        uiMessage.setMessage(message);
                                        messageViewModel.refreshSingleMessage(uiMessage);
                                    }

                                    @Override
                                    public void onError(RongIMClient.ErrorCode e) {}
                                });
                break;
            }
        }
    }

    private void initLongClick() {
        if (!RongConfigCenter.conversationConfig().isShowMoreClickAction()) {
            return;
        }
        if (modifyAction == null) {
            modifyAction =
                    new MessageItemLongClickAction.Builder()
                            .titleResId(R.string.rc_dialog_item_message_modify)
                            .actionListener(
                                    (context, message) -> {
                                        modifyUltraGroupMessage(message);
                                        return true;
                                    })
                            .showFilter(
                                    message ->
                                            message.getConversationType()
                                                    .equals(
                                                            Conversation.ConversationType
                                                                    .ULTRA_GROUP))
                            .build();
            MessageItemLongClickActionManager.getInstance()
                    .addMessageItemLongClickAction(modifyAction);
        }

        if (updateExpansionAction == null) {
            updateExpansionAction =
                    new MessageItemLongClickAction.Builder()
                            .titleResId(R.string.rc_dialog_item_message_update)
                            .actionListener(
                                    (context, message) -> {
                                        if (message.getConversationType()
                                                        .equals(
                                                                Conversation.ConversationType
                                                                        .PRIVATE)
                                                || message.getConversationType()
                                                        .equals(
                                                                Conversation.ConversationType
                                                                        .GROUP)) {
                                            Map<String, String> map = new HashMap<>();
                                            map.put("user", "Jack");
                                            RongCoreClient.getInstance()
                                                    .updateMessageExpansion(
                                                            map,
                                                            message.getUId(),
                                                            new IRongCoreCallback
                                                                    .OperationCallback() {
                                                                @Override
                                                                public void onSuccess() {
                                                                    try {
                                                                        Thread.sleep(1000);
                                                                    } catch (
                                                                            InterruptedException
                                                                                    e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                    RongCoreClient.getInstance()
                                                                            .getMessageByUid(
                                                                                    message
                                                                                            .getUId(),
                                                                                    new IRongCoreCallback
                                                                                                    .ResultCallback<
                                                                                            Message>() {
                                                                                        @Override
                                                                                        public void
                                                                                                onSuccess(
                                                                                                        Message
                                                                                                                msg) {
                                                                                            mainHandler
                                                                                                    .post(
                                                                                                            () -> {
                                                                                                                ToastUtils
                                                                                                                        .showToast(
                                                                                                                                "更新消息扩展成功，消息扩展信息："
                                                                                                                                        + msg
                                                                                                                                                .getExpansion());
                                                                                                            });
                                                                                        }

                                                                                        @Override
                                                                                        public void
                                                                                                onError(
                                                                                                        IRongCoreEnum
                                                                                                                        .CoreErrorCode
                                                                                                                e) {}
                                                                                    });
                                                                }

                                                                @Override
                                                                public void onError(
                                                                        IRongCoreEnum.CoreErrorCode
                                                                                coreErrorCode) {
                                                                    mainHandler.post(
                                                                            () -> {
                                                                                // 此时已经回到主线程
                                                                                ToastUtils
                                                                                        .showToast(
                                                                                                "更新消息扩展失败-"
                                                                                                        + coreErrorCode);
                                                                            });
                                                                }
                                                            });
                                            return true;
                                        }
                                        updateUltraGroupMessageExpansion(message);
                                        return true;
                                    })
                            .showFilter(
                                    message ->
                                            message.getConversationType()
                                                            .equals(
                                                                    Conversation.ConversationType
                                                                            .ULTRA_GROUP)
                                                    || message.getConversationType()
                                                            .equals(
                                                                    Conversation.ConversationType
                                                                            .PRIVATE)
                                                    || message.getConversationType()
                                                            .equals(
                                                                    Conversation.ConversationType
                                                                            .GROUP))
                            .build();
            MessageItemLongClickActionManager.getInstance()
                    .addMessageItemLongClickAction(updateExpansionAction);
        }

        if (removeExpansionAction == null) {
            removeExpansionAction =
                    new MessageItemLongClickAction.Builder()
                            .titleResId(R.string.rc_dialog_item_ultra_message_delete)
                            .actionListener(
                                    (context, message) -> {
                                        ArrayList<String> list = new ArrayList<>();
                                        list.add("100");
                                        ChannelClient.getInstance()
                                                .removeUltraGroupMessageExpansion(
                                                        message.getUId(),
                                                        list,
                                                        new IRongCoreCallback.OperationCallback() {
                                                            @Override
                                                            public void onSuccess() {
                                                                try {
                                                                    Thread.sleep(1000);
                                                                } catch (InterruptedException e) {
                                                                    e.printStackTrace();
                                                                }
                                                                RongCoreClient.getInstance()
                                                                        .getMessageByUid(
                                                                                message.getUId(),
                                                                                new IRongCoreCallback
                                                                                                .ResultCallback<
                                                                                        Message>() {
                                                                                    @Override
                                                                                    public void
                                                                                            onSuccess(
                                                                                                    Message
                                                                                                            msg) {
                                                                                        mainHandler
                                                                                                .post(
                                                                                                        () -> {
                                                                                                            ToastUtils
                                                                                                                    .showToast(
                                                                                                                            "删除消息扩展成功");
                                                                                                            ToastUtils
                                                                                                                    .showToast(
                                                                                                                            "消息扩展信息："
                                                                                                                                    + msg
                                                                                                                                            .getExpansion());
                                                                                                        });
                                                                                    }

                                                                                    @Override
                                                                                    public void
                                                                                            onError(
                                                                                                    IRongCoreEnum
                                                                                                                    .CoreErrorCode
                                                                                                            e) {}
                                                                                });
                                                            }

                                                            @Override
                                                            public void onError(
                                                                    IRongCoreEnum.CoreErrorCode
                                                                            coreErrorCode) {
                                                                mainHandler.post(
                                                                        () -> {
                                                                            // 此时已经回到主线程
                                                                            ToastUtils.showToast(
                                                                                    "删除消息扩展失败-"
                                                                                            + coreErrorCode);
                                                                        });
                                                            }
                                                        });
                                        return true;
                                    })
                            .showFilter(
                                    message ->
                                            message.getConversationType()
                                                    .equals(
                                                            Conversation.ConversationType
                                                                    .ULTRA_GROUP))
                            .build();
            MessageItemLongClickActionManager.getInstance()
                    .addMessageItemLongClickAction(removeExpansionAction);
        }

        if (recallAction == null) {
            recallAction =
                    new MessageItemLongClickAction.Builder()
                            .titleResId(R.string.rc_dialog_item_ultra_message_recall)
                            .actionListener(
                                    (context, message) -> {
                                        ChannelClient.getInstance()
                                                .recallUltraGroupMessage(
                                                        message.getMessage(),
                                                        new IRongCoreCallback.ResultCallback<
                                                                RecallNotificationMessage>() {
                                                            @Override
                                                            public void onSuccess(
                                                                    RecallNotificationMessage
                                                                            recallNotificationMessage) {
                                                                mainHandler.post(
                                                                        () -> {
                                                                            // 此时已经回到主线程
                                                                            ToastUtils.showToast(
                                                                                    "撤回超级群消息成功-");
                                                                            invokeRecallEvent(
                                                                                    message
                                                                                            .getMessage(),
                                                                                    recallNotificationMessage);
                                                                        });
                                                            }

                                                            @Override
                                                            public void onError(
                                                                    IRongCoreEnum.CoreErrorCode e) {
                                                                mainHandler.post(
                                                                        () -> {
                                                                            // 此时已经回到主线程
                                                                            ToastUtils.showToast(
                                                                                    "撤回超级群消息失败-"
                                                                                            + e
                                                                                                    .getValue());
                                                                        });
                                                            }
                                                        });
                                        return true;
                                    })
                            .showFilter(
                                    message ->
                                            message.getConversationType()
                                                    .equals(
                                                            Conversation.ConversationType
                                                                    .ULTRA_GROUP))
                            .build();
            MessageItemLongClickActionManager.getInstance()
                    .addMessageItemLongClickAction(recallAction);
        }

        if (recallAndDeleteAction == null) {
            recallAndDeleteAction =
                    new MessageItemLongClickAction.Builder()
                            .titleResId(R.string.rc_dialog_item_ultra_message_recall_and_del)
                            .actionListener(
                                    (context, message) -> {
                                        ChannelClient.getInstance()
                                                .recallUltraGroupMessage(
                                                        message.getMessage(),
                                                        true,
                                                        new IRongCoreCallback.ResultCallback<
                                                                RecallNotificationMessage>() {
                                                            @Override
                                                            public void onSuccess(
                                                                    RecallNotificationMessage
                                                                            recallNotificationMessage) {
                                                                mainHandler.post(
                                                                        () -> {
                                                                            // 此时已经回到主线程
                                                                            ToastUtils.showToast(
                                                                                    "撤回超级群消息并删除，成功-");
                                                                        });
                                                            }

                                                            @Override
                                                            public void onError(
                                                                    IRongCoreEnum.CoreErrorCode e) {
                                                                mainHandler.post(
                                                                        () -> {
                                                                            // 此时已经回到主线程
                                                                            ToastUtils.showToast(
                                                                                    "撤回超级群消息并删除，失败-"
                                                                                            + e
                                                                                                    .getValue());
                                                                        });
                                                            }
                                                        });
                                        return true;
                                    })
                            .showFilter(
                                    message ->
                                            message.getConversationType()
                                                    .equals(
                                                            Conversation.ConversationType
                                                                    .ULTRA_GROUP))
                            .build();
            MessageItemLongClickActionManager.getInstance()
                    .addMessageItemLongClickAction(recallAndDeleteAction);
        }
        if (pullRemoteAction == null) {
            pullRemoteAction =
                    new MessageItemLongClickAction.Builder()
                            .titleResId(R.string.rc_dialog_item_pull_remote_messages)
                            .actionListener(
                                    (context, message) -> {
                                        HistoryMessageOption historyMessageOption =
                                                new HistoryMessageOption();
                                        historyMessageOption.setOrder(
                                                HistoryMessageOption.PullOrder.DESCEND);
                                        historyMessageOption.setCount(5);
                                        historyMessageOption.setDataTime(0);
                                        ChannelClient.getInstance()
                                                .getMessages(
                                                        message.getConversationType(),
                                                        message.getTargetId(),
                                                        message.getMessage().getChannelId(),
                                                        historyMessageOption,
                                                        (messageList, errorCode) -> {
                                                            for (Message msg : messageList) {
                                                                RLog.i(
                                                                        TAG,
                                                                        "getMessages uid = "
                                                                                + msg.getUId());
                                                            }
                                                            ChannelClient.getInstance()
                                                                    .getBatchRemoteUltraGroupMessages(
                                                                            messageList,
                                                                            new IRongCoreCallback
                                                                                    .IGetBatchRemoteUltraGroupMessageCallback() {
                                                                                @Override
                                                                                public void
                                                                                        onSuccess(
                                                                                                List<
                                                                                                                Message>
                                                                                                        matchedMsgList,
                                                                                                List<
                                                                                                                Message>
                                                                                                        notMatchedMsgList) {
                                                                                    mainHandler
                                                                                            .post(
                                                                                                    () -> {
                                                                                                        // 此时已经回到主线程
                                                                                                        StringBuilder
                                                                                                                stringBuilder =
                                                                                                                        new StringBuilder();
                                                                                                        stringBuilder
                                                                                                                .append(
                                                                                                                        "match : ");
                                                                                                        for (Message
                                                                                                                message1 :
                                                                                                                        matchedMsgList) {
                                                                                                            stringBuilder
                                                                                                                    .append(
                                                                                                                            message1
                                                                                                                                    .getUId())
                                                                                                                    .append(
                                                                                                                            ",");
                                                                                                        }
                                                                                                        stringBuilder
                                                                                                                .append(
                                                                                                                        ", notMatch : ");
                                                                                                        for (Message
                                                                                                                message1 :
                                                                                                                        notMatchedMsgList) {
                                                                                                            stringBuilder
                                                                                                                    .append(
                                                                                                                            message1
                                                                                                                                    .getUId())
                                                                                                                    .append(
                                                                                                                            ",");
                                                                                                        }
                                                                                                        ToastUtils
                                                                                                                .showToast(
                                                                                                                        "getBatchRemoteUltraGroupMessages-"
                                                                                                                                + stringBuilder
                                                                                                                                        .toString());
                                                                                                    });
                                                                                }

                                                                                @Override
                                                                                public void onError(
                                                                                        IRongCoreEnum
                                                                                                        .CoreErrorCode
                                                                                                errorCode) {

                                                                                    mainHandler
                                                                                            .post(
                                                                                                    () -> {
                                                                                                        // 此时已经回到主线程
                                                                                                        ToastUtils
                                                                                                                .showToast(
                                                                                                                        "getBatchRemoteUltraGroupMessages error-"
                                                                                                                                + errorCode);
                                                                                                    });
                                                                                }
                                                                            });
                                                        });
                                        return true;
                                    })
                            .showFilter(
                                    message ->
                                            message.getConversationType()
                                                    .equals(
                                                            Conversation.ConversationType
                                                                    .ULTRA_GROUP))
                            .build();
            MessageItemLongClickActionManager.getInstance()
                    .addMessageItemLongClickAction(pullRemoteAction);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ReadReceiptV2Manager.setGroupReadReceiptV2Listener(null);
        IMManager.getInstance().removeUltraGroupMessageChangeListener(this);
    }

    private void sendResponse(List<Message> messageList) {
        if (!RongConfigCenter.conversationConfig()
                .isShowReadReceiptRequest(currentConversationType)) {
            return;
        }
        List<io.rong.imlib.model.Message> responseMessageList = new ArrayList<>();
        for (io.rong.imlib.model.Message message : messageList) {
            GroupReadReceiptInfoV2 readReceiptInfo = message.getGroupReadReceiptInfoV2();
            if (readReceiptInfo == null) {
                continue;
            }
            if (message.getMessageDirection() == Message.MessageDirection.SEND) {
                continue;
            }
            if (!readReceiptInfo.hasRespond()) {
                responseMessageList.add(message);
            }
        }
        if (responseMessageList.size() > 0) {
            ReadReceiptV2Manager.getInstance()
                    .sendReadReceiptResponse(
                            currentConversationType, targetId, null, responseMessageList, null);
        }
    }

    private void modifyUltraGroupMessage(UiMessage message) {
        TextMessage textMessage = new TextMessage("这是条修改消息");
        ChannelClient.getInstance()
                .modifyUltraGroupMessage(
                        message.getUId(),
                        textMessage,
                        new IRongCoreCallback.OperationCallback() {
                            @Override
                            public void onSuccess() {
                                RLog.i(TAG, "修改消息成功");
                                message.setContentSpannable(null);
                                message.getMessage().setContent(textMessage);
                                IMCenter.getInstance().refreshMessage(message.getMessage());
                                mainHandler.post(
                                        () -> {
                                            // 此时已经回到主线程
                                            ToastUtils.showToast("修改消息成功");
                                        });
                            }

                            @Override
                            public void onError(IRongCoreEnum.CoreErrorCode coreErrorCode) {
                                RLog.i(TAG, "消息消息失败-" + coreErrorCode);
                                mainHandler.post(
                                        () -> {
                                            // 此时已经回到主线程
                                            ToastUtils.showToast("修改消息失败-" + coreErrorCode);
                                        });
                            }
                        });
    }

    private void updateUltraGroupMessageExpansion(UiMessage message) {
        Map<String, String> map = new HashMap<>();
        map.put("user", "Jack");
        ChannelClient.getInstance()
                .updateUltraGroupMessageExpansion(
                        map,
                        message.getUId(),
                        new IRongCoreCallback.OperationCallback() {
                            @Override
                            public void onSuccess() {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                RongCoreClient.getInstance()
                                        .getMessageByUid(
                                                message.getUId(),
                                                new IRongCoreCallback.ResultCallback<Message>() {
                                                    @Override
                                                    public void onSuccess(Message msg) {
                                                        mainHandler.post(
                                                                () ->
                                                                        ToastUtils.showToast(
                                                                                "更新消息扩展成功, 扩展信息："
                                                                                        + msg
                                                                                                .getExpansion()));
                                                    }

                                                    @Override
                                                    public void onError(
                                                            IRongCoreEnum.CoreErrorCode e) {}
                                                });
                            }

                            @Override
                            public void onError(IRongCoreEnum.CoreErrorCode e) {
                                mainHandler.post(() -> ToastUtils.showToast("更新消息扩展失败-" + e));
                            }
                        });
    }

    @Override
    public void onUltraGroupMessageExpansionUpdated(List<Message> messages) {}

    @Override
    public void onUltraGroupMessageModified(List<Message> messages) {}

    @Override
    public void onUltraGroupMessageRecalled(List<Message> messages) {
        mainHandler.post(
                new Runnable() {
                    @Override
                    public void run() {
                        if (messages == null || messages.isEmpty()) {
                            return;
                        }
                        for (Message msg : messages) {
                            if (!(msg.getContent() instanceof RecallNotificationMessage)
                                    || (((RecallNotificationMessage) msg.getContent())
                                            .isDelete())) {
                                DeleteEvent deleteEvent =
                                        new DeleteEvent(
                                                currentConversationType,
                                                targetId,
                                                new int[] {msg.getMessageId()});
                                messageViewModel.onDeleteMessage(deleteEvent);
                            } else {
                                invokeRecallEvent(
                                        msg, (RecallNotificationMessage) msg.getContent());
                            }
                        }
                    }
                });
    }

    private void invokeRecallEvent(Message msg, RecallNotificationMessage r) {
        RecallEvent recallEvent =
                new RecallEvent(currentConversationType, targetId, msg.getMessageId(), r);
        messageViewModel.onRecallEvent(recallEvent);
    }
}
