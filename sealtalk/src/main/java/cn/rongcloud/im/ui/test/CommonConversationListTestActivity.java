
package cn.rongcloud.im.ui.test;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.BaseActivity;
import cn.rongcloud.im.ui.CommonConversationTestActivity;
import cn.rongcloud.im.utils.ToastUtils;
import io.rong.common.rlog.RLog;
import io.rong.imkit.RongIM;
import io.rong.imkit.config.ConversationListBehaviorListener;
import io.rong.imkit.conversationlist.model.BaseUiConversation;
import io.rong.imlib.model.Conversation;

/**
 * Debug模式会话列表公共Activcity
 */
public class CommonConversationListTestActivity extends BaseActivity {

    public static final String CONVERSATION_TYPE = "ConversationType";
    public static final String TARGET_ID = "targetId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_conversation_list_test);

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
                    Intent intent = new Intent(context, CommonConversationTestActivity.class);
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
}