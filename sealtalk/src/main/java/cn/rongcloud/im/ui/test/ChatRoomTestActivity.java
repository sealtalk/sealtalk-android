package cn.rongcloud.im.ui.test;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.activity.TitleBaseActivity;
import cn.rongcloud.im.ui.view.SettingItemView;

public class ChatRoomTestActivity extends TitleBaseActivity implements View.OnClickListener {

    private SettingItemView sivChatRoomKv;
    private SettingItemView sivChatRoomListenerTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room_test);
        initView();
    }

    private void initView() {
        sivChatRoomKv = (SettingItemView) findViewById(R.id.siv_chat_room_kv);
        sivChatRoomListenerTest = (SettingItemView) findViewById(R.id.siv_chat_room_listener_test);

        sivChatRoomKv.setOnClickListener(this);
        sivChatRoomListenerTest.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.siv_chat_room_kv:
                Intent intent = new Intent(this, ChatRoomStatusActivity.class);
                startActivity(intent);
                break;
            case R.id.siv_chat_room_listener_test:
                Intent intent1 = new Intent(this, ChatRoomListenerTestActivity.class);
                startActivity(intent1);
                break;
            default:
                break;
        }
    }
}