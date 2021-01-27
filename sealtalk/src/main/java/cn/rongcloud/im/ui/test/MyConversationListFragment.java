package cn.rongcloud.im.ui.test;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import io.rong.common.rlog.RLog;
import io.rong.imkit.RongIM;
import io.rong.imkit.config.ConversationListBehaviorListener;
import io.rong.imkit.conversationlist.ConversationListFragment;
import io.rong.imkit.conversationlist.model.BaseUiConversation;
import io.rong.imlib.model.Conversation;

public class MyConversationListFragment extends ConversationListFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RongIM.setConversationListBehaviorListener(new ConversationListBehaviorListener() {
            @Override
            public boolean onConversationPortraitClick(Context context, Conversation.ConversationType conversationType, String targetId) {
                return false;
            }

            @Override
            public boolean onConversationPortraitLongClick(Context context, Conversation.ConversationType conversationType, String targetId) {
                return false;
            }

            @Override
            public boolean onConversationLongClick(Context context, View view, BaseUiConversation conversation) {
                return false;
            }

            @Override
            public boolean onConversationClick(Context context, View view, BaseUiConversation conversation) {
                try {
                    Intent intent = new Intent(context, MessageExpansionDetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("uerid", conversation.mCore.getTargetId());
                    bundle.putInt("conversationType", conversation.mCore.getConversationType().getValue());
                    intent.putExtras(bundle);
                    startActivity(intent);
                    return true;
                } catch (Exception e) {
                    RLog.e("MyConversationListFragment", e.toString());
                    return false;
                }
            }
        });
    }
}
