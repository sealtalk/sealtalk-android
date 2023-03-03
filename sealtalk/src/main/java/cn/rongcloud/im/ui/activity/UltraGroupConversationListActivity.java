package cn.rongcloud.im.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import androidx.fragment.app.FragmentActivity;
import cn.rongcloud.im.R;
import io.rong.imkit.RongIM;
import io.rong.imkit.config.ConversationListBehaviorListener;
import io.rong.imkit.conversationlist.model.BaseUiConversation;
import io.rong.imlib.model.Conversation;

public class UltraGroupConversationListActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_expansion_activity);
        findViewById(R.id.btn_search)
                .setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                SealSearchUltraGroupActivity.start(
                                        UltraGroupConversationListActivity.this,
                                        SealSearchUltraGroupActivity.TYPE_ALL_TARGET,
                                        null);
                            }
                        });

        RongIM.setConversationListBehaviorListener(
                new ConversationListBehaviorListener() {
                    @Override
                    public boolean onConversationPortraitClick(
                            Context context,
                            Conversation.ConversationType conversationType,
                            String targetId) {
                        return false;
                    }

                    @Override
                    public boolean onConversationPortraitLongClick(
                            Context context,
                            Conversation.ConversationType conversationType,
                            String targetId) {
                        return false;
                    }

                    @Override
                    public boolean onConversationLongClick(
                            Context context, View view, BaseUiConversation conversation) {
                        return false;
                    }

                    @Override
                    public boolean onConversationClick(
                            Context context, View view, BaseUiConversation conversation) {

                        RongIM.getInstance()
                                .startConversation(
                                        context,
                                        conversation.getConversationIdentifier(),
                                        "Ultra group");
                        return true;
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RongIM.setConversationListBehaviorListener(null);
    }
}
