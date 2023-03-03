package cn.rongcloud.im.ui.test;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.activity.ConversationActivity;
import cn.rongcloud.im.ui.activity.UltraConversationActivity;
import cn.rongcloud.im.utils.log.SLog;
import io.rong.common.RLog;
import io.rong.imkit.IMCenter;
import io.rong.imkit.config.ConversationListBehaviorListener;
import io.rong.imkit.config.RongConfigCenter;
import io.rong.imkit.conversationlist.ConversationListFragment;
import io.rong.imkit.conversationlist.model.BaseUiConversation;
import io.rong.imkit.conversationlist.model.GatheredConversation;
import io.rong.imkit.conversationlist.viewmodel.UltraGroupConversationListViewModel;
import io.rong.imkit.event.Event;
import io.rong.imkit.model.NoticeContent;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imkit.widget.adapter.ViewHolder;
import io.rong.imkit.widget.dialog.OptionsPopupDialog;
import io.rong.imkit.widget.refresh.constant.RefreshState;
import io.rong.imlib.ChannelClient;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.IRongCoreCallback;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.TextMessage;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MyConversationListFragment extends ConversationListFragment {

    private static final String TAG = "MyConversationListFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RouteUtils.registerActivity(
                RouteUtils.RongActivityType.ConversationActivity, UltraConversationActivity.class);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RouteUtils.registerActivity(
                RouteUtils.RongActivityType.ConversationActivity, ConversationActivity.class);
    }

    @Override
    protected void subscribeUi() {
        // 会话列表数据监听
        mConversationListViewModel =
                new ViewModelProvider(this).get(UltraGroupConversationListViewModel.class);
        mConversationListViewModel.getConversationList(false, false, 0);
        mConversationListViewModel
                .getConversationListLiveData()
                .observe(
                        getViewLifecycleOwner(),
                        new Observer<List<BaseUiConversation>>() {
                            @Override
                            public void onChanged(List<BaseUiConversation> uiConversations) {
                                RLog.d(TAG, "conversation list onChanged.");
                                mAdapter.setDataCollection(uiConversations);
                            }
                        });
        // 连接状态监听
        mConversationListViewModel
                .getNoticeContentLiveData()
                .observe(
                        getViewLifecycleOwner(),
                        new Observer<NoticeContent>() {
                            @Override
                            public void onChanged(NoticeContent noticeContent) {
                                // 当连接通知没有显示时，延迟进行显示，防止连接闪断造成画面闪跳。
                                if (mNoticeContainerView.getVisibility() == View.GONE) {
                                    mHandler.postDelayed(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    // 刷新时使用最新的通知内容
                                                    updateNoticeContent(
                                                            mConversationListViewModel
                                                                    .getNoticeContentLiveData()
                                                                    .getValue());
                                                }
                                            },
                                            NOTICE_SHOW_DELAY_MILLIS);
                                } else {
                                    updateNoticeContent(noticeContent);
                                }
                            }
                        });
        // 刷新事件监听
        mConversationListViewModel
                .getRefreshEventLiveData()
                .observe(
                        getViewLifecycleOwner(),
                        new Observer<Event.RefreshEvent>() {
                            @Override
                            public void onChanged(Event.RefreshEvent refreshEvent) {
                                if (refreshEvent.state.equals(RefreshState.LoadFinish)) {
                                    mRefreshLayout.finishLoadMore();
                                } else if (refreshEvent.state.equals(RefreshState.RefreshFinish)) {
                                    mRefreshLayout.finishRefresh();
                                }
                            }
                        });
    }

    @Override
    public void onItemClick(View view, ViewHolder holder, int position) {
        if (position < 0) {
            return;
        }
        BaseUiConversation baseUiConversation = mAdapter.getItem(position);
        if (baseUiConversation != null && baseUiConversation.mCore != null) {
            if (baseUiConversation instanceof GatheredConversation) {
                RouteUtils.routeToSubConversationListActivity(
                        view.getContext(),
                        ((GatheredConversation) baseUiConversation).mGatheredType,
                        baseUiConversation.mCore.getConversationTitle());
            } else {
                RouteUtils.routeToConversationActivity(
                        view.getContext(), baseUiConversation.getConversationIdentifier());
            }
        } else {
            RLog.e(TAG, "invalid conversation.");
        }

        ((UltraGroupConversationListViewModel) mConversationListViewModel)
                .setChannelId(baseUiConversation.mCore.getChannelId());
    }

    @Override
    public boolean onItemLongClick(View view, ViewHolder holder, int position) {
        if (position < 0) {
            return false;
        }
        final BaseUiConversation baseUiConversation = mAdapter.getItem(position);
        ConversationListBehaviorListener listBehaviorListener =
                RongConfigCenter.conversationListConfig().getListener();
        if (listBehaviorListener != null
                && listBehaviorListener.onConversationLongClick(
                        view.getContext(), view, baseUiConversation)) {
            RLog.d(TAG, "ConversationList item click event has been intercepted by App.");
            return true;
        }
        final ArrayList<String> items = new ArrayList<>();
        final String removeItem =
                view.getContext()
                        .getResources()
                        .getString(R.string.rc_conversation_list_dialog_remove);
        final String setTopItem =
                view.getContext()
                        .getResources()
                        .getString(R.string.rc_conversation_list_dialog_set_top);
        final String cancelTopItem =
                view.getContext()
                        .getResources()
                        .getString(R.string.rc_conversation_list_dialog_cancel_top);

        items.add("channelId : " + baseUiConversation.mCore.getChannelId());
        if (!(baseUiConversation instanceof GatheredConversation)) {
            if (baseUiConversation.mCore.isTop()) {
                items.add(cancelTopItem);
            } else {
                items.add(setTopItem);
            }
        }
        items.add(removeItem);
        items.add("获取超级群所有频道@消息数");
        items.add("获取超级群当前频道@消息数");
        items.add("获取未读@消息列表");
        items.add("发送扩展消息");
        items.add("获取超级群当前频道首条未读消息sendTime");
        int size = items.size();
        OptionsPopupDialog.newInstance(view.getContext(), items.toArray(new String[size]))
                .setOptionsPopupDialogListener(
                        which -> {
                            if (items.get(which).equals(setTopItem)
                                    || items.get(which).equals(cancelTopItem)) {
                                IMCenter.getInstance()
                                        .setConversationToTop(
                                                baseUiConversation.getConversationIdentifier(),
                                                !baseUiConversation.mCore.isTop(),
                                                false,
                                                new RongIMClient.ResultCallback<Boolean>() {
                                                    @Override
                                                    public void onSuccess(Boolean value) {
                                                        Toast.makeText(
                                                                        view.getContext(),
                                                                        items.get(which),
                                                                        Toast.LENGTH_SHORT)
                                                                .show();
                                                    }

                                                    @Override
                                                    public void onError(
                                                            RongIMClient.ErrorCode errorCode) {}
                                                });
                            } else if (items.get(which).equals(removeItem)) {
                                IMCenter.getInstance()
                                        .removeConversation(
                                                baseUiConversation.mCore.getConversationType(),
                                                baseUiConversation.mCore.getTargetId(),
                                                null);
                            } else if (items.get(which).equals("获取超级群所有频道@消息数")) {
                                ChannelClient.getInstance()
                                        .getUltraGroupUnreadMentionedCount(
                                                baseUiConversation.mCore.getTargetId(),
                                                new IRongCoreCallback.ResultCallback<Integer>() {
                                                    @Override
                                                    public void onSuccess(Integer integer) {
                                                        Toast.makeText(
                                                                        view.getContext(),
                                                                        "超级群 @消息数 ：" + integer,
                                                                        Toast.LENGTH_LONG)
                                                                .show();
                                                    }

                                                    @Override
                                                    public void onError(
                                                            IRongCoreEnum.CoreErrorCode e) {
                                                        Toast.makeText(
                                                                        view.getContext(),
                                                                        "获取超级群 @消息数失败",
                                                                        Toast.LENGTH_LONG)
                                                                .show();
                                                    }
                                                });
                            } else if (items.get(which).equals("获取超级群当前频道@消息数")) {
                                Toast.makeText(
                                                view.getContext(),
                                                "超级群当前频道 @消息数 ："
                                                        + baseUiConversation.mCore
                                                                .getMentionedCount(),
                                                Toast.LENGTH_LONG)
                                        .show();
                            } else if (items.get(which).equals("获取未读@消息列表")) {
                                ChannelClient.getInstance()
                                        .getUnreadMentionedMessages(
                                                baseUiConversation.mCore.getConversationType(),
                                                baseUiConversation.mCore.getTargetId(),
                                                baseUiConversation.mCore.getChannelId(),
                                                100,
                                                true,
                                                new IRongCoreCallback.ResultCallback<
                                                        List<Message>>() {
                                                    @Override
                                                    public void onSuccess(List<Message> list) {
                                                        if (list == null || list.isEmpty()) {
                                                            return;
                                                        }
                                                        StringBuilder stringBuilder =
                                                                new StringBuilder();
                                                        stringBuilder
                                                                .append("总共 ")
                                                                .append(list.size())
                                                                .append("未读@消息数，分别为 id：");
                                                        for (Message message : list) {
                                                            stringBuilder
                                                                    .append(message.getMessageId())
                                                                    .append(",");
                                                        }
                                                        Toast.makeText(
                                                                        view.getContext(),
                                                                        stringBuilder.toString(),
                                                                        Toast.LENGTH_LONG)
                                                                .show();
                                                    }

                                                    @Override
                                                    public void onError(
                                                            IRongCoreEnum.CoreErrorCode e) {
                                                        Toast.makeText(
                                                                        view.getContext(),
                                                                        "获取超级群 @消息列表失败 : "
                                                                                + e.getMessage(),
                                                                        Toast.LENGTH_LONG)
                                                                .show();
                                                    }
                                                });
                            } else if (items.get(which).equals("发送扩展消息")) {
                                TextMessage textMessage =
                                        new TextMessage("这是一条扩展消息 " + System.currentTimeMillis());
                                Message message =
                                        Message.obtain(
                                                baseUiConversation.getConversationIdentifier(),
                                                textMessage);
                                message.setCanIncludeExpansion(true);
                                HashMap<String, String> map = new HashMap<>();
                                map.put("89", "100");
                                message.setExpansion(map);
                                IMCenter.getInstance()
                                        .sendMessage(
                                                message,
                                                null,
                                                null,
                                                new IRongCallback.ISendMessageCallback() {
                                                    @Override
                                                    public void onAttached(Message message) {}

                                                    @Override
                                                    public void onSuccess(Message message) {
                                                        RLog.i(TAG, "success");
                                                    }

                                                    @Override
                                                    public void onError(
                                                            Message message,
                                                            RongIMClient.ErrorCode errorCode) {
                                                        RLog.i(TAG, "ErrorCode = " + errorCode);
                                                    }
                                                });
                            } else if (items.get(which).equals("获取超级群当前频道首条未读消息sendTime")) {
                                ChannelClient.getInstance()
                                        .getConversation(
                                                Conversation.ConversationType.ULTRA_GROUP,
                                                baseUiConversation.mCore.getTargetId(),
                                                baseUiConversation.mCore.getChannelId(),
                                                new IRongCoreCallback.ResultCallback<
                                                        Conversation>() {
                                                    @Override
                                                    public void onSuccess(
                                                            Conversation conversation) {
                                                        String content =
                                                                "会话名:"
                                                                        + baseUiConversation.mCore
                                                                                .getConversationTitle()
                                                                        + ",time:"
                                                                        + baseUiConversation.mCore
                                                                                .getFirstUnreadMsgSendTime();
                                                        RLog.i(
                                                                TAG,
                                                                "first getConversation success,"
                                                                        + content);

                                                        List<Message> list = new ArrayList<>();
                                                        Message msg =
                                                                Message.obtain(
                                                                        conversation.getTargetId(),
                                                                        conversation
                                                                                .getConversationType(),
                                                                        conversation.getChannelId(),
                                                                        null);
                                                        msg.setSentTime(
                                                                conversation
                                                                        .getFirstUnreadMsgSendTime());
                                                        list.add(msg);
                                                        ChannelClient.getInstance()
                                                                .getBatchRemoteUltraGroupMessages(
                                                                        list,
                                                                        new IRongCoreCallback
                                                                                .IGetBatchRemoteUltraGroupMessageCallback() {
                                                                            @Override
                                                                            public void onSuccess(
                                                                                    List<Message>
                                                                                            matchedMsgList,
                                                                                    List<Message>
                                                                                            notMatchedMsgList) {
                                                                                if (matchedMsgList
                                                                                                != null
                                                                                        && !matchedMsgList
                                                                                                .isEmpty()) {
                                                                                    Message
                                                                                            message =
                                                                                                    matchedMsgList
                                                                                                            .get(
                                                                                                                    0);
                                                                                    RLog.i(
                                                                                            TAG,
                                                                                            "first unread msg success, getMsg onSuccess:"
                                                                                                    + (message
                                                                                                                    != null
                                                                                                            ? message
                                                                                                                    .getUId()
                                                                                                            : ""));
                                                                                    String
                                                                                            msgContent =
                                                                                                    message
                                                                                                                    == null
                                                                                                            ? ""
                                                                                                            : ((message
                                                                                                                                    .getContent()
                                                                                                                            instanceof
                                                                                                                            TextMessage)
                                                                                                                    ? ((TextMessage)
                                                                                                                                    message
                                                                                                                                            .getContent())
                                                                                                                            .getContent()
                                                                                                                    : message.getContent()
                                                                                                                            .getClass()
                                                                                                                            .getSimpleName());
                                                                                    String content =
                                                                                            "会话名:"
                                                                                                    + baseUiConversation
                                                                                                            .mCore
                                                                                                            .getConversationTitle()
                                                                                                    + ",time:"
                                                                                                    + baseUiConversation
                                                                                                            .mCore
                                                                                                            .getFirstUnreadMsgSendTime()
                                                                                                    + ",timeStr:"
                                                                                                    + date2TimeStampMillis(
                                                                                                            baseUiConversation
                                                                                                                    .mCore
                                                                                                                    .getFirstUnreadMsgSendTime())
                                                                                                    + ",content:"
                                                                                                    + msgContent;
                                                                                    mHandler.post(
                                                                                            () ->
                                                                                                    Toast
                                                                                                            .makeText(
                                                                                                                    view
                                                                                                                            .getContext(),
                                                                                                                    content,
                                                                                                                    Toast
                                                                                                                            .LENGTH_LONG)
                                                                                                            .show());
                                                                                } else {
                                                                                    RLog.i(
                                                                                            TAG,
                                                                                            "first unread msg success, getMsg error:");
                                                                                    String content =
                                                                                            "会话名:"
                                                                                                    + baseUiConversation
                                                                                                            .mCore
                                                                                                            .getConversationTitle()
                                                                                                    + ",time:"
                                                                                                    + baseUiConversation
                                                                                                            .mCore
                                                                                                            .getFirstUnreadMsgSendTime()
                                                                                                    + ",timeStr:"
                                                                                                    + date2TimeStampMillis(
                                                                                                            baseUiConversation
                                                                                                                    .mCore
                                                                                                                    .getFirstUnreadMsgSendTime());
                                                                                    mHandler.post(
                                                                                            () ->
                                                                                                    Toast
                                                                                                            .makeText(
                                                                                                                    view
                                                                                                                            .getContext(),
                                                                                                                    content,
                                                                                                                    Toast
                                                                                                                            .LENGTH_LONG)
                                                                                                            .show());
                                                                                }
                                                                            }

                                                                            @Override
                                                                            public void onError(
                                                                                    IRongCoreEnum
                                                                                                    .CoreErrorCode
                                                                                            errorCode) {
                                                                                SLog.i(
                                                                                        TAG,
                                                                                        "getBatchRemoteUltraGroupMessages onError: "
                                                                                                + errorCode);
                                                                                String content =
                                                                                        "会话名:"
                                                                                                + baseUiConversation
                                                                                                        .mCore
                                                                                                        .getConversationTitle()
                                                                                                + ",time:"
                                                                                                + baseUiConversation
                                                                                                        .mCore
                                                                                                        .getFirstUnreadMsgSendTime()
                                                                                                + ",timeStr:"
                                                                                                + date2TimeStampMillis(
                                                                                                        baseUiConversation
                                                                                                                .mCore
                                                                                                                .getFirstUnreadMsgSendTime());
                                                                                mHandler.post(
                                                                                        () ->
                                                                                                Toast
                                                                                                        .makeText(
                                                                                                                view
                                                                                                                        .getContext(),
                                                                                                                content,
                                                                                                                Toast
                                                                                                                        .LENGTH_LONG)
                                                                                                        .show());
                                                                            }
                                                                        });
                                                    }

                                                    @Override
                                                    public void onError(
                                                            IRongCoreEnum.CoreErrorCode errorCode) {
                                                        RLog.i(
                                                                TAG,
                                                                "first unread msg ErrorCode = "
                                                                        + errorCode);
                                                    }
                                                });
                            }
                        })
                .show();
        return true;
    }

    public String date2TimeStampMillis(long date) {
        if (date <= 0) return "0";
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(date));
    }
}
