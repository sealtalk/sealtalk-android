package cn.rongcloud.im.ui.test;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.lifecycle.Observer;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.LogTag;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.model.ChatRoomAction;
import cn.rongcloud.im.ui.activity.TitleBaseActivity;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.utils.log.SLog;
import io.rong.imkit.RongIM;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.chatroom.base.RongChatRoomClient;

public class ChatRoomListenerTestActivity extends TitleBaseActivity implements View.OnClickListener {

    public static final String CHATROOMLISTENER_1 = "chatroomlistener1";
    public static final String CHATROOMLISTENER_2 = "chatroomlistener2";
    private Button btnChatRoomTest1;
    private Button btnChatRoomTest2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room_listener_test);
        initView();
    }

    private void initView() {
        btnChatRoomTest1 = (Button) findViewById(R.id.btn_chat_room_test_1);
        btnChatRoomTest2 = (Button) findViewById(R.id.btn_chat_room_test_2);

        btnChatRoomTest1.setOnClickListener(this);
        btnChatRoomTest2.setOnClickListener(this);

        IMManager.getInstance().getChatRoomAction().observe(this, new Observer<ChatRoomAction>() {
            @Override
            public void onChanged(ChatRoomAction chatRoomAction) {
                if (chatRoomAction.status == ChatRoomAction.Status.ERROR) {
                    ToastUtils.showToast(R.string.discovery_chat_room_join_failure);
                } else {
                    SLog.d(LogTag.IM, "ChatRoom action, status: " + chatRoomAction.status.name() + " - ChatRoom id:" + chatRoomAction.roomId);
                }
                switch (chatRoomAction.status) {
                    case ERROR:
                        ToastUtils.showToast("加入聊天室出错,roomId=" + chatRoomAction.roomId);
                        break;
                    case JOINING:
                        break;
                    case JOINED:
                        ToastUtils.showToast("加入聊天室成功,roomId=" + chatRoomAction.roomId);
                        break;
                    case RESET:
                        ToastUtils.showToast("聊天室被重置,roomId=" + chatRoomAction.roomId);
                        break;
                    case QUITED:
                        ToastUtils.showToast("退出聊天室,roomId=" + chatRoomAction.roomId);
                        break;
                    case DESTROY:
                        ToastUtils.showToast("聊天室销毁,roomId=" + chatRoomAction.roomId);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_chat_room_test_1:
                joinRoom(CHATROOMLISTENER_1);
                break;
            case R.id.btn_chat_room_test_2:
                joinRoom(CHATROOMLISTENER_2);
                break;
            default:
                break;
        }
    }

    private void joinRoom(String roomId) {
        RongIM.getInstance().joinChatRoom(roomId, 20, new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}