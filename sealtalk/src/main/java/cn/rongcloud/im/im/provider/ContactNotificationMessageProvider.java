package cn.rongcloud.im.im.provider;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import cn.rongcloud.im.R;
import cn.rongcloud.im.model.ContactNotificationMessageData;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.utilities.OptionsPopupDialog;
import io.rong.imkit.widget.provider.IContainerItemProvider;
import io.rong.message.ContactNotificationMessage;

@ProviderTag(messageContent = ContactNotificationMessage.class, showPortrait = false, centerInHorizontal = true, showProgress = false, showSummaryWithName = false)
public class ContactNotificationMessageProvider extends IContainerItemProvider.MessageProvider<ContactNotificationMessage> {
    @Override
    public void bindView(View v, int position, ContactNotificationMessage content, UIMessage message) {
        ViewHolder viewHolder = (ViewHolder) v.getTag();
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
                            viewHolder.contentTextView.setText(RongContext.getInstance().getResources().getString(R.string.msg_contact_notification_someone_agree_your_request));
                        }
                    } else {
                        if (content.getOperation().equals("AcceptResponse")) {
                            viewHolder.contentTextView.setText(RongContext.getInstance().getResources().getString(R.string.msg_contact_notification_agree_your_request));
                        }
                    }
                    if (content.getOperation().equals("Request")) {
                        viewHolder.contentTextView.setText(content.getMessage());
                    }
                }
            }
        }
    }


    @Override
    public Spannable getContentSummary(ContactNotificationMessage content) {
        return null;
    }

    @Override
    public Spannable getContentSummary(Context context, ContactNotificationMessage content) {
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

    @Override
    public void onItemClick(View view, int position, ContactNotificationMessage
            content, UIMessage message) {
    }

    @Override
    public void onItemLongClick(View view, int position, ContactNotificationMessage content, final UIMessage message) {
        String[] items;

        items = new String[]{view.getContext().getResources().getString(R.string.msg_dialog_item_message_delete)};

        OptionsPopupDialog.newInstance(view.getContext(), items).setOptionsPopupDialogListener(new OptionsPopupDialog.OnOptionsItemClickedListener() {
            @Override
            public void onOptionsItemClicked(int which) {
                if (which == 0)
                    RongIM.getInstance().deleteMessages(new int[]{message.getMessageId()}, null);
            }
        }).show();
    }

    @Override
    public View newView(Context context, ViewGroup group) {
        View view = LayoutInflater.from(context).inflate(R.layout.rc_item_group_information_notification_message, null);
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.contentTextView = (TextView) view.findViewById(R.id.rc_msg);
        viewHolder.contentTextView.setMovementMethod(LinkMovementMethod.getInstance());
        view.setTag(viewHolder);

        return view;
    }


    private static class ViewHolder {
        TextView contentTextView;
    }
}
