package cn.rongcloud.im.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.lang.reflect.Method;

import cn.rongcloud.im.R;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.ui.test.CustomConversationFragment;
import io.rong.imkit.config.ConversationClickListener;
import io.rong.imkit.config.RongConfigCenter;
import io.rong.imkit.widget.dialog.PromptPopupDialog;
import io.rong.imlib.IRongCoreListener;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.BlockedMessageInfo;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;
import io.rong.message.ReferenceMessage;

public class CommonConversationTestActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_conversation_test);

        // 添加会话界面
        CustomConversationFragment conversationFragment = new CustomConversationFragment();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.common_conversation_container, conversationFragment);
        transaction.commit();

        RongConfigCenter.conversationConfig().setConversationClickListener(new ConversationClickListener() {
            @Override
            public boolean onUserPortraitClick(Context context, Conversation.ConversationType conversationType, UserInfo user, String targetId) {
                return false;
            }

            @Override
            public boolean onUserPortraitLongClick(Context context, Conversation.ConversationType conversationType, UserInfo user, String targetId) {
                return false;
            }

            @Override
            public boolean onMessageClick(Context context, View view, Message message) {
                if (message.getObjectName().equals("RC:ReferenceMsg")) {
                    ReferenceMessage referenceMessage = (ReferenceMessage) message.getContent();
                    PromptPopupDialog dialog = PromptPopupDialog.newInstance(view.getContext(), "被引用消息ID属性", "referMsgUid=" + referenceMessage.getReferMsgUid());
                    dialog.show();
                    return true;
                }
                return false;
            }

            @Override
            public boolean onMessageLongClick(Context context, View view, Message message) {
                return false;
            }

            @Override
            public boolean onMessageLinkClick(Context context, String link, Message message) {
                return false;
            }

            @Override
            public boolean onReadReceiptStateClick(Context context, Message message) {
                return false;
            }
        });

        RongIMClient.getInstance().setMessageBlockListener(new IRongCoreListener.MessageBlockListener() {
            @Override
            public void onMessageBlock(BlockedMessageInfo info) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        StringBuilder builder = new StringBuilder();
                        builder.append("会话类型=" + info.getConversationType().getName())
                                .append("\n")
                                .append("会话ID=" + info.getTargetId())
                                .append("\n")
                                .append("被拦截的消息ID=" + info.getBlockMsgUId())
                                .append("\n")
                                .append("被拦截原因的类型=" + info.getType().value)
                                .append("\n");

                        new AlertDialog.Builder(CommonConversationTestActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                .setMessage(builder.toString())
                                .setCancelable(true)
                                .show();
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            Class clz = Class.forName(IMManager.class.getCanonicalName());
            Method method = clz.getDeclaredMethod("getInstance", new Class<?>[0]);
            Method method1 = clz.getDeclaredMethod("initConversation", new Class<?>[0]);
            method1.setAccessible(true);
            IMManager instance = (IMManager) method.invoke(null, new Object[0]);
            method1.invoke(instance, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}