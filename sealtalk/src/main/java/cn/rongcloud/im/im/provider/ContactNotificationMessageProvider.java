package cn.rongcloud.im.im.provider;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.model.ContactNotificationMessageData;
import io.rong.imkit.conversation.messgelist.provider.BaseMessageItemProvider;
import io.rong.imkit.model.UiMessage;
import io.rong.imkit.widget.adapter.BaseAdapter;
import io.rong.imkit.widget.adapter.IViewProviderListener;
import io.rong.imkit.widget.adapter.ViewHolder;
import io.rong.imlib.model.MessageContent;
import io.rong.message.ContactNotificationMessage;

public class ContactNotificationMessageProvider extends BaseMessageItemProvider<ContactNotificationMessage> {

    @Override
    protected ViewHolder onCreateMessageContentViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rc_item_information_notification_message, null);
        return new ViewHolder(view.getContext(), view);
    }

    @Override
    protected void bindMessageContentViewHolder(ViewHolder holder,ViewHolder parentHolder, ContactNotificationMessage content, UiMessage uiMessage, int position, List<UiMessage> list, IViewProviderListener<UiMessage> listener) {
        if (content != null) {
            if (!TextUtils.isEmpty(content.getExtra())) {
                ContactNotificationMessageData bean = null;
                try {
                    Gson gson = new Gson();
                    bean = gson.fromJson(content.getExtra(), ContactNotificationMessageData.class);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                } finally {
                    if (bean != null && !TextUtils.isEmpty(bean.getSourceUserNickname())) {
                        if (content.getOperation().equals("AcceptResponse")) {
                            holder.setText(R.id.rc_msg,holder.getContext().getResources().getString(R.string.msg_contact_notification_someone_agree_your_request));
                        }
                    } else {
                        if (content.getOperation().equals("AcceptResponse")) {
                            holder.setText(R.id.rc_msg,holder.getContext().getResources().getString(R.string.msg_contact_notification_agree_your_request));
                        }
                    }
                    if (content.getOperation().equals("Request")) {
                        holder.setText(R.id.rc_msg,content.getMessage());
                    }
                }
            }
        }
    }

    @Override
    protected boolean onItemClick(ViewHolder holder, ContactNotificationMessage contactNotificationMessage, UiMessage uiMessage, int position, List<UiMessage> list, IViewProviderListener<UiMessage> listener) {
        return false;
    }

    @Override
    protected boolean isMessageViewType(MessageContent messageContent) {
        return messageContent instanceof ContactNotificationMessage;
    }

    @Override
    public Spannable getSummarySpannable(Context context, ContactNotificationMessage content) {
        if (content != null && !TextUtils.isEmpty(content.getExtra())) {
            ContactNotificationMessageData bean = null;
            try {
                Gson gson = new Gson();
                bean = gson.fromJson(content.getExtra(), ContactNotificationMessageData.class);
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            } finally {
                if (bean != null && !TextUtils.isEmpty(bean.getSourceUserNickname())) {
                    if (content.getOperation().equals("AcceptResponse")) {
                        return new SpannableString(context.getString(R.string.msg_contact_notification_someone_agree_your_request));
                    }
                } else {
                    if (content.getOperation().equals("AcceptResponse")) {
                        return new SpannableString(context.getString(R.string.msg_contact_notification_agree_your_request));
                    }
                }
                if (content.getOperation().equals("Request")) {
                    return new SpannableString(content.getMessage());
                }
            }
        }
        return null;
    }
}
