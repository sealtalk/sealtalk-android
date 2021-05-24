package cn.rongcloud.im.ui.test;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import cn.rongcloud.im.R;
import io.rong.common.RLog;
import io.rong.imkit.RongIM;
import io.rong.imkit.config.ConversationListBehaviorListener;
import io.rong.imkit.conversationlist.model.BaseUiConversation;
import io.rong.imlib.model.Conversation;

public class ShortageConversationListActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_expansion_activity);
        FragmentManager fragmentManage = getSupportFragmentManager();
        MyConversationListFragment fragement = (MyConversationListFragment) fragmentManage.findFragmentById(R.id.conversationlist);
        Uri uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon()
                .appendPath("conversationlist")
                .appendQueryParameter(Conversation.ConversationType.PRIVATE.getName(), "false")
                .appendQueryParameter(Conversation.ConversationType.GROUP.getName(), "false")
                .appendQueryParameter(Conversation.ConversationType.PUBLIC_SERVICE.getName(), "false")
                .appendQueryParameter(Conversation.ConversationType.APP_PUBLIC_SERVICE.getName(), "false")
                .appendQueryParameter(Conversation.ConversationType.SYSTEM.getName(), "true")
                .build();
        // fragement.setUri(uri);

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
                    Intent intent = new Intent(context, ShortageDetailActivity.class);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RongIM.setConversationListBehaviorListener(null);
    }
}
