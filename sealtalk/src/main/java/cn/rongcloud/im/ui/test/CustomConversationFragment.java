package cn.rongcloud.im.ui.test;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.rong.common.rlog.RLog;
import io.rong.imkit.config.RongConfigCenter;
import io.rong.imkit.conversation.ConversationFragment;
import io.rong.imkit.conversation.messgelist.viewmodel.MessageViewModel;
import io.rong.imkit.model.UiMessage;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imlib.ReadReceiptV2Manager;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.GroupReadReceiptInfoV2;
import io.rong.imlib.model.Message;
import io.rong.message.TextMessage;

/**
 * 自定义ConversationListFragment
 * 供Debug模式下测试使用
 */
public class CustomConversationFragment extends ConversationFragment {

    final static String TAG = CustomConversationFragment.class.getCanonicalName();
    private Conversation.ConversationType currentConversationType;
    private String targetId;

    public CustomConversationFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        RLog.i(TAG, "CustomConversationFragment onChange");
        String type = getActivity().getIntent().getStringExtra(RouteUtils.CONVERSATION_TYPE);
        targetId = getActivity().getIntent().getStringExtra(RouteUtils.TARGET_ID);
        currentConversationType = Conversation.ConversationType.valueOf(type.toUpperCase(Locale.US));
        MessageViewModel messageViewModel = new ViewModelProvider(this).get(MessageViewModel.class);
        messageViewModel.getUiMessageLiveData().observeForever(new Observer<List<UiMessage>>() {
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
    }

    private void init(MessageViewModel messageViewModel) {
        ReadReceiptV2Manager.setGroupReadReceiptV2Listener(new ReadReceiptV2Manager.GroupReadReceiptV2Listener() {
            @Override
            public void onReadReceiptReceived(Message message) {

            }

            @Override
            public void onMessageReceiptResponse(Conversation.ConversationType type, String targetId, String channelId, String messageUId, int readCount, int totalCount) {
                List<UiMessage> uiMessagesList = messageViewModel.getUiMessages();
                if (uiMessagesList == null || uiMessagesList.size() == 0) {
                    return;
                }
                for (int i = 0; i < uiMessagesList.size(); i++) {
                    UiMessage uiMessage = uiMessagesList.get(i);
                    if (TextUtils.equals(uiMessage.getUId(), messageUId)) {
                        RongIMClient.getInstance().getMessageByUid(messageUId, new RongIMClient.ResultCallback<Message>() {
                            @Override
                            public void onSuccess(Message message) {
                                uiMessage.setMessage(message);
                                messageViewModel.refreshSingleMessage(uiMessage);
                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode e) {

                            }
                        });
                        break;
                    }
                }
            }
        });
    }

    private void sendResponse(List<Message> messageList) {
        if (!RongConfigCenter.conversationConfig().isShowReadReceiptRequest(currentConversationType)) {
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
            ReadReceiptV2Manager.getInstance().sendReadReceiptResponse(currentConversationType, targetId, null, responseMessageList, null);
        }
    }

}