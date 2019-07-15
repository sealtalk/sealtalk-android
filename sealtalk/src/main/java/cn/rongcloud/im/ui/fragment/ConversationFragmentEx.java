package cn.rongcloud.im.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.im.provider.ForwardClickActions;
import cn.rongcloud.im.ui.activity.GroupReadReceiptDetailActivity;
import cn.rongcloud.im.ui.dialog.EvaluateBottomDialog;
import io.rong.common.RLog;
import io.rong.imkit.RongExtension;
import io.rong.imkit.RongIM;
import io.rong.imkit.actions.IClickActions;
import io.rong.imkit.fragment.ConversationFragment;
import io.rong.imkit.mention.RongMentionManager;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.MentionedInfo;
import io.rong.message.TextMessage;

/**
 * 会话 Fragment 继承自ConversationFragment
 * onResendItemClick: 重发按钮点击事件. 如果返回 false,走默认流程,如果返回 true,走自定义流程
 * onReadReceiptStateClick: 已读回执详情的点击事件.
 * 如果不需要重写 onResendItemClick 和 onReadReceiptStateClick ,可以不必定义此类,直接集成 ConversationFragment 就可以了
 */
public class ConversationFragmentEx extends ConversationFragment {
    private OnShowAnnounceListener onShowAnnounceListener;
    private RongExtension rongExtension;
    private ListView listView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        rongExtension = (RongExtension) v.findViewById(io.rong.imkit.R.id.rc_extension);
        View messageListView = findViewById(v, io.rong.imkit.R.id.rc_layout_msg_list);
        listView = findViewById(messageListView, io.rong.imkit.R.id.rc_list);
        return v;
    }


    /**
     * 回执消息的点击监听，
     *
     * @param message
     */
    @Override
    public void onReadReceiptStateClick(io.rong.imlib.model.Message message) {
        if (message.getConversationType() == Conversation.ConversationType.GROUP) { //目前只适配了群组会话
            // 群组显示未读消息的人的信息
            Intent intent = new Intent(getActivity(), GroupReadReceiptDetailActivity.class);
            intent.putExtra(IntentExtra.PARCEL_MESSAGE, message);
            getActivity().startActivity(intent);
        }
    }

    // 警告 dialog
    @Override
    public void onWarningDialog(String msg) {
        String typeStr = getUri().getLastPathSegment();
        if (!typeStr.equals("chatroom")) {
            super.onWarningDialog(msg);
        }
    }

    @Override
    public void onShowAnnounceView(String announceMsg, String announceUrl) {
        // 此处为接收到通知消息， 然后回调到 activity 显示。
        if (onShowAnnounceListener != null) {
            onShowAnnounceListener.onShowAnnounceView(announceMsg, announceUrl);
        }
    }


    @Override
    public void onShowStarAndTabletDialog(String dialogId) {
        // 评星的dialog 或者自定义评价 dialog 可在此自定义显示
        showEvaluateDialog(dialogId);
    }

    @Override
    public List<IClickActions> getMoreClickActions() {
        List<IClickActions> actions = new ArrayList();
        actions.addAll(super.getMoreClickActions());
        actions.add(0, new ForwardClickActions());
        return actions;
    }

    /**
     * 输入区Plugin 按钮点击监听。
     *
     * @param v
     * @param extensionBoard
     */
    @Override
    public void onPluginToggleClick(View v, ViewGroup extensionBoard) {
        // 当点击输入去 Plugin （+）的切换按钮后， 则是消息列表显示最后一条。
        setMessageListLast();
    }

    /**
     * 输入区表情切换按钮的监听
     *
     * @param v
     * @param extensionBoard
     */
    @Override
    public void onEmoticonToggleClick(View v, ViewGroup extensionBoard) {
        // 当点击输入去表情的切换按钮后， 则是消息列表显示最后一条。
        setMessageListLast();
    }

    @Override
    public void onSendToggleClick(View v, String text) {
        if (TextUtils.isEmpty(text) || TextUtils.isEmpty(text.trim())) {
            RLog.e(TAG, "text content must not be null");
            return;
        }

        TextMessage textMessage = TextMessage.obtain(text);
        MentionedInfo mentionedInfo = RongMentionManager.getInstance().onSendButtonClick();
        if (mentionedInfo != null) {
            if (mentionedInfo.getMentionedUserIdList().contains("-1")) {
                mentionedInfo.setType(MentionedInfo.MentionedType.ALL);
            } else {
                mentionedInfo.setType(MentionedInfo.MentionedType.PART);
            }
            textMessage.setMentionedInfo(mentionedInfo);
        }
        io.rong.imlib.model.Message message = io.rong.imlib.model.Message.obtain(getTargetId(), getConversationType(), textMessage);
        RongIM.getInstance().sendMessage(message, null, null, (IRongCallback.ISendMessageCallback) null);
    }

    @Override
    public boolean showMoreClickItem() {
        return true;
    }


    /**
     * 会话界面设置最后一条
     */
    private void setMessageListLast() {
        if (!rongExtension.isExtensionExpanded()) {
            listView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    listView.requestFocusFromTouch();
                    listView.setSelection(listView.getCount());
                }
            }, 100);
        }
    }


    /**
     * 显示客服评价的dialog。
     *
     * @param dialogId
     */
    private void showEvaluateDialog(final String dialogId) {
        EvaluateBottomDialog.Builder builder = new EvaluateBottomDialog.Builder();
        builder.setTargetId(getTargetId());
        builder.setDialogId(dialogId);
        EvaluateBottomDialog dialog = builder.build();
        dialog.setOnEvaluateListener(new EvaluateBottomDialog.OnEvaluateListener() {
            @Override
            public void onCancel() {
                FragmentActivity activity = getActivity();
                if(activity != null){
                    activity.finish();
                }
            }

            @Override
            public void onSubmitted() {
                FragmentActivity activity = getActivity();
                if(activity != null){
                    activity.finish();
                }
            }
        });
        dialog.show(getActivity().getSupportFragmentManager(), dialogId);
    }

    /**
     * 设置通知信息回调。
     *
     * @param listener
     */
    public void setOnShowAnnounceBarListener(OnShowAnnounceListener listener) {
        onShowAnnounceListener = listener;
    }

    /**
     * 显示通告栏的监听器
     */
    public interface OnShowAnnounceListener {

        /**
         * 展示通告栏的回调
         *
         * @param announceMsg 通告栏展示内容
         * @param annouceUrl  通告栏点击链接地址，若此参数为空，则表示不需要点击链接，否则点击进入链接页面
         * @return
         */
        void onShowAnnounceView(String announceMsg, String annouceUrl);
    }
}
