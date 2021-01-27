package cn.rongcloud.im.ui.test;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import cn.rongcloud.im.R;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.ui.activity.TitleBaseActivity;
import cn.rongcloud.im.ui.test.viewmodel.ChatRoomViewModel;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.chatroom.message.ChatRoomKVNotiMessage;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;

public class ChatRoomStatusActivity extends TitleBaseActivity implements View.OnClickListener {

    private ChatRoomStatusDeatilActivity.OnKVStatusEvent kvStatusEvent;
    public static boolean isFirstKVStatusDidChange;
    private Handler handler;
    private ChatRoomViewModel chatRoomViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room_status);
        setTitle("聊天室存储");
        handler = new Handler(Looper.getMainLooper());
        chatRoomViewModel = new ViewModelProvider(this).get(ChatRoomViewModel.class);
        isFirstKVStatusDidChange = true;
        initView();
        initListener();
    }

    private void initListener() {
        RongIMClient.getInstance().setKVStatusListener(new RongIMClient.KVStatusListener() {

            @Override
            public void onChatRoomKVSync(String roomId) {
                Log.e("ChatDetailActivity", "ChatRoomStatusActivity***onChatRoomKVStatusSync");
                ChatRoomStatusDeatilActivity.OnKVStatusEvent event = new ChatRoomStatusDeatilActivity.OnKVStatusEvent(roomId, ChatRoomStatusDeatilActivity.OnKVStatusEvent.KV_SYNC, new HashMap<>());
                ChatRoomStatusDeatilActivity.kvStatusEventList.add(event);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        chatRoomViewModel.executeChatRoomEvent(event);
                    }
                });
//                RongContext.getInstance().getEventBus().post(event);
            }

            @Override
            public void onChatRoomKVUpdate(String roomId, Map<String, String> chatRoomKvMap) {
                Log.e("ChatDetailActivity", "ChatRoomStatusActivity***onChatRoomKVStatusChange");
                ChatRoomStatusDeatilActivity.OnKVStatusEvent event = new ChatRoomStatusDeatilActivity.OnKVStatusEvent(roomId, ChatRoomStatusDeatilActivity.OnKVStatusEvent.KV_CHANGE, chatRoomKvMap);
                if (isFirstKVStatusDidChange) {
                    ChatRoomStatusDeatilActivity.kvStatusEventList.add(event);
                    isFirstKVStatusDidChange = false;
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        chatRoomViewModel.executeChatRoomEvent(event);
                    }
                });
//                RongContext.getInstance().getEventBus().post(event);
            }

            @Override
            public void onChatRoomKVRemove(String roomId, Map<String, String> chatRoomKvMap) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        chatRoomViewModel.executeChatRoomEvent(new ChatRoomStatusDeatilActivity.OnKVStatusEvent(roomId, ChatRoomStatusDeatilActivity.OnKVStatusEvent.KV_REMOVE, chatRoomKvMap));
                    }
                });
//                RongContext.getInstance().getEventBus().post(new ChatRoomStatusDeatilActivity.OnKVStatusEvent(roomId, ChatRoomStatusDeatilActivity.OnKVStatusEvent.KV_REMOVE, chatRoomKvMap));
            }
        });

        IMManager.getInstance().getMessageRouter().observe(this, new Observer<Message>() {
            @Override
            public void onChanged(Message message) {
                MessageContent content = message.getContent();
                if (content instanceof ChatRoomKVNotiMessage) {
                    ChatRoomStatusDeatilActivity.historyMessage.put(message.getUId(), message);
                    chatRoomViewModel.executeChatRoomEvent(new ChatRoomStatusDeatilActivity.OnReceiveMessageEvent(message));
                }
            }
        });
    }

//        private void initReceiveMessageListener() {
//        RongIM.setOnReceiveMessageListener(new RongIMClient.OnReceiveMessageListener() {
//            @Override
//            public boolean onReceived(Message message, int left) {
//
//                return false;
//            }
//        });
//    }

    private void initView() {
        Button btnChatRoom1 = findViewById(R.id.btn_chat_room1);
        Button btnChatRoom2 = findViewById(R.id.btn_chat_room2);
        btnChatRoom1.setOnClickListener(this);
        btnChatRoom2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_chat_room1:
                joinRoom("kvchatroom1");
                break;
            case R.id.btn_chat_room2:
                joinRoom("kvchatroom2");
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
        toDeatail(roomId);
    }

    private void toDeatail(String roomId) {
        Intent intent = new Intent(ChatRoomStatusActivity.this, ChatRoomStatusDeatilActivity.class);
        intent.putExtra("joinMessage", getStringDate() + " 加入成功 chatroomId:" + roomId);
        intent.putExtra("room_id", roomId);
        startActivity(intent);
    }

    public String getStringDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);
        return dateString;
    }
}
