package cn.rongcloud.im.im.plugin;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.util.HashMap;

import cn.rongcloud.im.R;
import cn.rongcloud.im.SealApp;
import cn.rongcloud.im.common.Constant;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.ui.activity.ConversationActivity;
import cn.rongcloud.im.ui.dialog.SendPokeDialog;
import cn.rongcloud.im.utils.ToastUtils;
import io.rong.imkit.conversation.extension.RongExtension;
import io.rong.imkit.conversation.extension.component.plugin.IPluginModule;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;

public class PokePlugin implements IPluginModule {
    /**
     * 记录发送戳一下消息时间
     * Key：会话 id
     * Value：最近一次发送戳一下消息的时间戳，毫秒
     */
    private static final HashMap<String, Long> sendPokeTimeMap = new HashMap<>();

    @Override
    public Drawable obtainDrawable(Context context) {
        return context.getResources().getDrawable(R.drawable.rc_ext_plugin_poke_selector);
    }

    @Override
    public String obtainTitle(Context context) {
        return context.getString(R.string.im_plugin_poke_title);
    }

    @Override
    public void onClick(Fragment fragment, RongExtension rongExtension, int index) {
        final String targetId = rongExtension.getTargetId();
        Long lastSendTime = sendPokeTimeMap.get(targetId);
        long currentTimeMillis = System.currentTimeMillis();

        // 判断是否连续发送戳一下消息，当在允许间隔时间内时提示下次可发送的时间
        if (lastSendTime != null && currentTimeMillis - lastSendTime < Constant.POKE_MESSAGE_INTERVAL) {
            int nextEnabledTimeSecond = (int) (Constant.POKE_MESSAGE_INTERVAL - (currentTimeMillis - lastSendTime)) / 1000;
            String disablePokeMsg = SealApp.getApplication().getString(R.string.poke_allow_next_send_message_time_msg, nextEnabledTimeSecond);
            ToastUtils.showToast(disablePokeMsg);
            return;
        }

        // 判断当前会话是私人还是群组，决定对话框的显示形式
        Conversation.ConversationType conversationType = rongExtension.getConversationType();
        String targetName = "";
        boolean isToMulti = false;

        if (conversationType == Conversation.ConversationType.PRIVATE) {
            UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(targetId);
            targetName = userInfo.getName();
            isToMulti = false;
        } else if (conversationType == Conversation.ConversationType.GROUP) {
            // 当是群组会话时，判断是否为群主或管理员，仅群主和管理员可以发送戳一下消息
            FragmentActivity activity = fragment.getActivity();
            if(activity instanceof ConversationActivity){
                ConversationActivity conversationActivity = (ConversationActivity) activity;
                boolean groupOwner = conversationActivity.isGroupOwner();
                boolean groupManager = conversationActivity.isGroupManager();
                if(!groupOwner && !groupManager){
                    ToastUtils.showToast(R.string.poke_only_group_owner_and_manager_can_send);
                    return;
                }
            }

            Group groupInfo = RongUserInfoManager.getInstance().getGroupInfo(targetId);
            targetName = groupInfo.getName();
            isToMulti = true;
        }

        SendPokeDialog pokeDialog = new SendPokeDialog();
        pokeDialog.setTargetName(targetName);
        pokeDialog.setIsMultiSelect(isToMulti);
        pokeDialog.setTargetId(targetId);
        pokeDialog.setOnSendPokeClickedListener((isMultiSelect, userIds, pokeMessage) -> {
            if (isMultiSelect) {
                IMManager.getInstance().sendPokeMessageToGroup(targetId, pokeMessage, userIds, new IRongCallback.ISendMessageCallback() {
                    @Override
                    public void onAttached(Message message) {
                    }
                    @Override
                    public void onSuccess(Message message) {
                        // 记录当前发送的时间
                        sendPokeTimeMap.put(targetId, System.currentTimeMillis());
                    }
                    @Override
                    public void onError(Message message, RongIMClient.ErrorCode errorCode) {
                    }
                });
            } else {
                IMManager.getInstance().sendPokeMessageToPrivate(targetId, pokeMessage, new IRongCallback.ISendMediaMessageCallback() {
                    @Override
                    public void onProgress(Message message, int i) {
                    }
                    @Override
                    public void onCanceled(Message message) {
                    }
                    @Override
                    public void onAttached(Message message) {
                    }
                    @Override
                    public void onSuccess(Message message) {
                        // 记录当前发送的时间
                        sendPokeTimeMap.put(targetId, System.currentTimeMillis());
                    }
                    @Override
                    public void onError(Message message, RongIMClient.ErrorCode errorCode) {
                    }
                });
            }
        });
        // 显示对话框，收起扩展栏
        if (fragment != null && fragment.getChildFragmentManager() != null) {
            pokeDialog.show(fragment.getChildFragmentManager(), null);
            rongExtension.collapseExtension();
        }
    }

    @Override
    public void onActivityResult(int i, int i1, Intent intent) {

    }
}
