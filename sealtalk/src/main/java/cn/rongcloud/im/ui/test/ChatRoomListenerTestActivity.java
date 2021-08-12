package cn.rongcloud.im.ui.test;

import android.app.AlertDialog;
import android.os.Bundle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.task.AppTask;
import io.rong.imkit.conversation.RongConversationActivity;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imlib.IRongCoreListener;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.chatroom.base.RongChatRoomClient;
import io.rong.imlib.model.BlockedMessageInfo;
import io.rong.imlib.model.ChatRoomMemberAction;
import io.rong.imlib.model.MessageBlockType;

public class ChatRoomListenerTestActivity extends RongConversationActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppTask appTask = IMManager.getInstance().getAppTask();
        //是否处于Debug 测试模式
        if (appTask != null && appTask.isDebugMode()) {
            Map<Integer, String> map = new HashMap<>();
            map.put(MessageBlockType.UNKNOWN.value, "未知类型");
            map.put(MessageBlockType.BLOCK_GLOBAL.value, " 全局敏感词");
            map.put(MessageBlockType.BLOCK_CUSTOM.value, "自定义敏感词拦截");
            map.put(MessageBlockType.BLOCK_THIRD_PATY.value, "第三方审核拦截");
            RongIMClient.getInstance().setMessageBlockListener(new IRongCoreListener.MessageBlockListener() {
                @Override
                public void onMessageBlock(BlockedMessageInfo info) {
                    if (ChatRoomListenerTestActivity.this.isFinishing()) {
                        return;
                    }
                    StringBuilder builder = new StringBuilder();
                    builder.append("会话类型=" + info.getConversationType().getName())
                            .append("\n")
                            .append("会话ID=" + info.getTargetId())
                            .append("\n")
                            .append("被拦截的消息ID=" + info.getBlockMsgUId())
                            .append("\n")
                            .append("被拦截原因的类型=" + info.getType().value + " (" + map.get(info.getType().value) + ")")
                            .append("\n");

                    new AlertDialog.Builder(ChatRoomListenerTestActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                            .setMessage(builder.toString())
                            .setCancelable(true)
                            .show();
                }
            });

            RongChatRoomClient.setChatRoomMemberListener(new RongChatRoomClient.ChatRoomMemberActionListener() {
                @Override
                public void onMemberChange(List<ChatRoomMemberAction> chatRoomMemberActions, String roomId) {
                    if (ChatRoomListenerTestActivity.this.isFinishing()) {
                        return;
                    }
                    if (chatRoomMemberActions == null || chatRoomMemberActions.isEmpty()) {
                        return;
                    }

                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < chatRoomMemberActions.size(); i++) {
                        ChatRoomMemberAction member = chatRoomMemberActions.get(i);
                        if (member.getChatRoomMemberAction() == ChatRoomMemberAction.ChatRoomMemberActionType.CHAT_ROOM_MEMBER_JOIN) {
                            builder.append("用户:" + chatRoomMemberActions.get(i).getUserId() + "加入聊天室:" + roomId);
                        } else if (member.getChatRoomMemberAction() == ChatRoomMemberAction.ChatRoomMemberActionType.CHAT_ROOM_MEMBER_QUIT) {
                            builder.append("用户:" + chatRoomMemberActions.get(i).getUserId() + "退出聊天室:" + roomId);
                        } else {
                            builder.append("用户:" + chatRoomMemberActions.get(i).getUserId() + "加入或退出聊天室:" + roomId + " 未知UNKOWN!");
                        }
                        builder.append("\n");
                    }
                    new AlertDialog.Builder(ChatRoomListenerTestActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                            .setMessage(builder.toString())
                            .setCancelable(true)
                            .show();
                }
            });
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (IMManager.getInstance().getAppTask().isDebugMode()) {
            RouteUtils.registerActivity(RouteUtils.RongActivityType.ConversationActivity, null);
        }
    }
}