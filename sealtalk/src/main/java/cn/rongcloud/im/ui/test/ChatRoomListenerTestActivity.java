package cn.rongcloud.im.ui.test;

import android.app.AlertDialog;
import android.os.Bundle;
import cn.rongcloud.im.common.BlockListener;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.task.AppTask;
import io.rong.imkit.conversation.RongConversationActivity;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.chatroom.base.RongChatRoomClient;
import io.rong.imlib.model.ChatRoomMemberAction;
import java.util.List;

public class ChatRoomListenerTestActivity extends RongConversationActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppTask appTask = IMManager.getInstance().getAppTask();
        // 是否处于Debug 测试模式
        if (appTask != null && appTask.isDebugMode()) {
            RongIMClient.getInstance().setMessageBlockListener(new BlockListener(this));

            RongChatRoomClient.setChatRoomMemberListener(
                    new RongChatRoomClient.ChatRoomMemberActionListener() {
                        @Override
                        public void onMemberChange(
                                List<ChatRoomMemberAction> chatRoomMemberActions, String roomId) {
                            if (ChatRoomListenerTestActivity.this.isFinishing()) {
                                return;
                            }
                            if (chatRoomMemberActions == null || chatRoomMemberActions.isEmpty()) {
                                return;
                            }

                            StringBuilder builder = new StringBuilder();
                            for (int i = 0; i < chatRoomMemberActions.size(); i++) {
                                ChatRoomMemberAction member = chatRoomMemberActions.get(i);
                                if (member.getChatRoomMemberAction()
                                        == ChatRoomMemberAction.ChatRoomMemberActionType
                                                .CHAT_ROOM_MEMBER_JOIN) {
                                    builder.append(
                                            "用户:"
                                                    + chatRoomMemberActions.get(i).getUserId()
                                                    + "加入聊天室:"
                                                    + roomId);
                                } else if (member.getChatRoomMemberAction()
                                        == ChatRoomMemberAction.ChatRoomMemberActionType
                                                .CHAT_ROOM_MEMBER_QUIT) {
                                    builder.append(
                                            "用户:"
                                                    + chatRoomMemberActions.get(i).getUserId()
                                                    + "退出聊天室:"
                                                    + roomId);
                                } else {
                                    builder.append(
                                            "用户:"
                                                    + chatRoomMemberActions.get(i).getUserId()
                                                    + "加入或退出聊天室:"
                                                    + roomId
                                                    + " 未知UNKOWN!");
                                }
                                builder.append("\n");
                            }
                            new AlertDialog.Builder(
                                            ChatRoomListenerTestActivity.this,
                                            AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
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
