package cn.rongcloud.im.ui.test;

import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.test.provider.GroupReadReceiptTextMessageItemProvider;
import cn.rongcloud.im.utils.ToastUtils;
import io.rong.common.rlog.RLog;
import io.rong.imkit.RongIM;
import io.rong.imkit.config.ConversationListBehaviorListener;
import io.rong.imkit.config.RongConfigCenter;
import io.rong.imkit.conversation.messgelist.provider.TextMessageItemProvider;
import io.rong.imkit.conversationlist.model.BaseUiConversation;
import io.rong.imlib.model.Conversation;

public class GRRConversationListTestActivity extends FragmentActivity {

    /**
     * 1标识群消息发送方;2标识群消息接收方
     */
    public static final String CONVERSATION_TYPE = "ConversationType";
    public static final String TARGET_ID = "targetId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list_for_group_read_receipt);

        init();

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
                if (conversation.mCore.getConversationType() != Conversation.ConversationType.GROUP) {
                    ToastUtils.showToast("只支持群组");
                    return true;
                }
                try {
                    Intent intent = new Intent(context, GRRSenderTestActivity.class);
                    intent.putExtra(CONVERSATION_TYPE, conversation.mCore.getConversationType().getName().toLowerCase());
                    intent.putExtra(TARGET_ID, conversation.mCore.getTargetId());
                    startActivity(intent);
                    return true;
                } catch (Exception e) {
                    RLog.e("ConversationListForGroupReadReceiptActivity", e.toString());
                    return false;
                }
            }
        });
    }

    private void init() {
        RongConfigCenter.conversationConfig().replaceMessageProvider(TextMessageItemProvider.class, new GroupReadReceiptTextMessageItemProvider());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RongIM.setConversationListBehaviorListener(null);
        RongConfigCenter.conversationConfig().replaceMessageProvider(GroupReadReceiptTextMessageItemProvider.class, new TextMessageItemProvider());
    }


}