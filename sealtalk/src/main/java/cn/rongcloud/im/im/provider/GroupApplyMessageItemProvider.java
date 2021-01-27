package cn.rongcloud.im.im.provider;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.ViewGroup;

import com.google.gson.Gson;

import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.im.message.GroupApplyMessage;
import cn.rongcloud.im.model.GroupApplyMessageData;
import io.rong.imkit.conversation.messgelist.provider.BaseMessageItemProvider;
import io.rong.imkit.model.UiMessage;
import io.rong.imkit.widget.adapter.BaseAdapter;
import io.rong.imkit.widget.adapter.IViewProviderListener;
import io.rong.imkit.widget.adapter.ViewHolder;
import io.rong.imlib.model.MessageContent;

public class GroupApplyMessageItemProvider extends BaseMessageItemProvider<GroupApplyMessage> {
    @Override
    protected ViewHolder onCreateMessageContentViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    protected void bindMessageContentViewHolder(ViewHolder holder,ViewHolder parentHolder, GroupApplyMessage groupApplyMessage, UiMessage uiMessage, int position, List<UiMessage> list, IViewProviderListener<UiMessage> listener) {

    }

    @Override
    protected boolean onItemClick(ViewHolder holder, GroupApplyMessage groupApplyMessage, UiMessage uiMessage, int position, List<UiMessage> list, IViewProviderListener<UiMessage> listener) {
        return false;
    }

    @Override
    protected boolean isMessageViewType(MessageContent messageContent) {
        return messageContent instanceof GroupApplyMessage;
    }

    @Override
    public Spannable getSummarySpannable(Context context, GroupApplyMessage groupApplyMessage) {
        String content = "";
        Gson gson = new Gson();
        GroupApplyMessageData groupApplyMessageData = gson.fromJson(groupApplyMessage.getData(), GroupApplyMessageData.class);
        if (groupApplyMessageData.getType() == 2) {
            content = context.getString(R.string.seal_conversation_notification_group_tips_add) + " " + groupApplyMessageData.getTargetGroupName();
        } else {
            content = groupApplyMessageData.getOperatorNickname() + " " + context.getString(R.string.seal_conversation_notification_group_tips);
        }
        return new SpannableString(content);
    }
}
