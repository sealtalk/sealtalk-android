package cn.rongcloud.im.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import io.rong.imkit.model.UIConversation;
import io.rong.imkit.widget.adapter.ConversationListAdapter;
import io.rong.imlib.model.Conversation;

/**
 * Created by weiqinxiao on 15/11/5.
 */
public class ConversationListAdapterEx extends ConversationListAdapter {
    public ConversationListAdapterEx(Context context) {
        super(context);
    }

    @Override
    protected View newView(Context context, int position, ViewGroup group) {
        return super.newView(context, position, group);
    }

    @Override
    protected void bindView(View v, int position, UIConversation data) {
        if (data != null) {
            if (data.getConversationType().equals(Conversation.ConversationType.DISCUSSION))
                data.setUnreadType(UIConversation.UnreadRemindType.REMIND_ONLY);
        }
        super.bindView(v, position, data);
    }
}
