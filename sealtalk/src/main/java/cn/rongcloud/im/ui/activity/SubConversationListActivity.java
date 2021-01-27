package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.FragmentTransaction;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.fragment.SubConversationListFragmentEx;
import io.rong.imkit.subconversationlist.SubConversationListFragment;
import io.rong.imkit.utils.RongUtils;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imlib.model.Conversation;

/**
 *  聚合会话列表
 */
public class SubConversationListActivity extends TitleBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation_activity_subconversation_list);
        SubConversationListFragmentEx fragment = new SubConversationListFragmentEx();
//        fragment.setAdapter(new SubConversationListAdapterEx(RongContext.getInstance()));
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.rong_content, fragment);
        transaction.commit();

        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        //聚合会话参数
        Conversation.ConversationType conversationType = (Conversation.ConversationType) intent.getSerializableExtra(RouteUtils.CONVERSATION_TYPE);

        if (conversationType == null)
            return;
        String type = conversationType.getName();
        if (type.equals("group")) {
            getTitleBar().setTitle(R.string.seal_conversation_sub_group);
        } else if (type.equals("private")) {
            getTitleBar().setTitle(R.string.seal_conversation_sub_private);
        } else if (type.equals("discussion")) {
            getTitleBar().setTitle(R.string.seal_conversation_sub_discussion);
        } else if (type.equals("system")) {
            getTitleBar().setTitle(R.string.seal_conversation_sub_system);
        } else {
            getTitleBar().setTitle(R.string.seal_conversation_sub_defult);
        }
    }
}
