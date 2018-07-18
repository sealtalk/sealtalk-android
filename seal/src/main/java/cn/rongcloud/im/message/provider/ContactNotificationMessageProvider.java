package cn.rongcloud.im.message.provider;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import cn.rongcloud.im.R;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.ContactNotificationMessageData;
import cn.rongcloud.im.server.utils.json.JsonMananger;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.utilities.OptionsPopupDialog;
import io.rong.imkit.widget.provider.IContainerItemProvider;
import io.rong.message.ContactNotificationMessage;

/**
 * Created by Bob on 2015/4/17.
 * 如何自定义消息模板
 */
@ProviderTag(messageContent = ContactNotificationMessage.class, showPortrait = false, centerInHorizontal = true, showProgress = false, showSummaryWithName = false)
public class ContactNotificationMessageProvider extends IContainerItemProvider.MessageProvider<ContactNotificationMessage> {
    @Override
    public void bindView(View v, int position, ContactNotificationMessage content, UIMessage message) {
        ViewHolder viewHolder = (ViewHolder) v.getTag();
        if (content != null) {
            if (!TextUtils.isEmpty(content.getExtra())) {
                ContactNotificationMessageData bean = null;
                try {
                    JSONObject jsonObject = new JSONObject(content.getExtra());
                    try {
                        bean = JsonMananger.jsonToBean(content.getExtra(), ContactNotificationMessageData.class);
                    } catch (HttpException e) {
                        e.printStackTrace();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    if (bean != null && !TextUtils.isEmpty(bean.getSourceUserNickname())) {
                        if (content.getOperation().equals("AcceptResponse")) {
                            viewHolder.contentTextView.setText(RongContext.getInstance().getResources().getString(R.string.contact_notification_someone_agree_your_request, bean.getSourceUserNickname()));
                        }
                    } else {
                        if (content.getOperation().equals("AcceptResponse")) {
                            viewHolder.contentTextView.setText(RongContext.getInstance().getResources().getString(R.string.contact_notification_agree_your_request));
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
                bean = JsonMananger.jsonToBean(content.getExtra(), ContactNotificationMessageData.class);
            } catch (HttpException e) {
                e.printStackTrace();
            } finally {
                if (bean != null && !TextUtils.isEmpty(bean.getSourceUserNickname())) {
                    if (content.getOperation().equals("AcceptResponse")) {
                        return new SpannableString(bean.getSourceUserNickname() + "已同意你的好友请求");
                    }
                } else {
                    if (content.getOperation().equals("AcceptResponse")) {
                        return new SpannableString("对方已同意你的好友请求");
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

        items = new String[]{view.getContext().getResources().getString(R.string.de_dialog_item_message_delete)};

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
