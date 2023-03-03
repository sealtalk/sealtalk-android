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

/** 超级群列表选择器。 点击超级群会话后，设置到static conversation，外部获取后需要置空 */
public class UltraGroupConversationListPickerActivity extends FragmentActivity {

    public static BaseUiConversation conversation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_expansion_picker_activity);

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
                        UltraGroupConversationListPickerActivity.conversation = conversation;
                        finish();
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
