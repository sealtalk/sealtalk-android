package cn.rongcloud.im.ui.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;

import java.util.Locale;

import cn.rongcloud.im.R;
import cn.rongcloud.im.server.utils.NToast;
import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.ConversationFragment;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;

/**
 * Created by AMing on 16/5/27.
 * Company RongCloud
 */
public class DoubleChatRoomActivity extends FragmentActivity {
    private String chatroomId1;
    private String chatroomId2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doublechatroom);
        chatroomId1 = getIntent().getStringExtra("chatroomId1");
        chatroomId2 = getIntent().getStringExtra("chatroomId2");

    }


    private void enterFragment1(Conversation.ConversationType mConversationType, String mTargetId) {

        ConversationFragment fragment = new ConversationFragment();

        Uri uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon()
                  .appendPath("conversation").appendPath(mConversationType.getName().toLowerCase(Locale.US))
                  .appendQueryParameter("targetId", mTargetId).build();

        fragment.setUri(uri);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.temp1, fragment);
        transaction.commit();
    }

    private void enterFragment2(Conversation.ConversationType mConversationType, String mTargetId) {

        ConversationFragment fragment = new ConversationFragment();

        Uri uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon()
                  .appendPath("conversation").appendPath(mConversationType.getName().toLowerCase(Locale.US))
                  .appendQueryParameter("targetId", mTargetId).build();

        fragment.setUri(uri);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.temp2, fragment);
        transaction.commit();
    }

    public void add1(View view) {
        if (RongIM.getInstance().getCurrentConnectionStatus().equals(RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED) && !TextUtils.isEmpty(chatroomId1)) {
            enterFragment1(Conversation.ConversationType.CHATROOM, chatroomId1);
        }
    }
    public void add2(View view) {
        if (RongIM.getInstance().getCurrentConnectionStatus().equals(RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED) && !TextUtils.isEmpty(chatroomId2)) {
            enterFragment2(Conversation.ConversationType.CHATROOM, chatroomId2);
        }
    }
    public void quit1(View view) {
        if (RongIM.getInstance().getCurrentConnectionStatus().equals(RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED) && !TextUtils.isEmpty(chatroomId1)) {
            RongIM.getInstance().quitChatRoom(chatroomId1, new RongIMClient.OperationCallback() {
                @Override
                public void onSuccess() {
                    NToast.shortToast(DoubleChatRoomActivity.this, "quit success 1");
                }

                @Override
                public void onError(RongIMClient.ErrorCode errorCode) {

                }
            });
        }
    }
    public void quit2(View view) {
        if (RongIM.getInstance().getCurrentConnectionStatus().equals(RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED) && !TextUtils.isEmpty(chatroomId2)) {
            RongIM.getInstance().quitChatRoom(chatroomId2, new RongIMClient.OperationCallback() {
                @Override
                public void onSuccess() {
                    NToast.shortToast(DoubleChatRoomActivity.this, "quit success 2");
                }

                @Override
                public void onError(RongIMClient.ErrorCode errorCode) {

                }
            });
        }
    }
}
