package cn.rongcloud.im.im.provider;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.im.message.SealGroupConNtfMessage;
import io.rong.imkit.RongIM;
import io.rong.imkit.conversation.messgelist.provider.BaseMessageItemProvider;
import io.rong.imkit.conversation.messgelist.provider.BaseNotificationMessageItemProvider;
import io.rong.imkit.model.UiMessage;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imkit.widget.adapter.BaseAdapter;
import io.rong.imkit.widget.adapter.IViewProviderListener;
import io.rong.imkit.widget.adapter.ViewHolder;
import io.rong.imlib.model.MessageContent;

public class SealGroupConNtfMessageProvider extends BaseNotificationMessageItemProvider<SealGroupConNtfMessage> {

    @Override
    protected ViewHolder onCreateMessageContentViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rc_group_regular_clear_notification_message, parent, false);
        return new ViewHolder(view.getContext(), view);
    }

    @Override
    protected void bindMessageContentViewHolder(ViewHolder holder,ViewHolder parentHolder, SealGroupConNtfMessage content, UiMessage message, int position, List<UiMessage> list, IViewProviderListener<UiMessage> listener) {
        if (content != null && message != null) {
            if (!TextUtils.isEmpty(content.getOperation())) {
                TextView textView = holder.getView(R.id.rc_msg);
                String contentStr = "";
                if (content.getOperation().equals("closeRegularClear")) {
                    textView.setText(holder.getContext().getResources().getString(R.string.seal_set_clean_time_notification_close));
                } else if (content.getOperation().equals("openRegularClear")) {
                    contentStr = holder.getContext().getResources().getString(R.string.seal_set_clean_time_notification_open);
                } else {
                    String operatorUserName = "";
                    if (RongUserInfoManager.getInstance().getUserInfo(content.getOperatorUserId()) != null) {
                        operatorUserName = RongUserInfoManager.getInstance().getUserInfo(content.getOperatorUserId()).getName();
                    }
                    if (content.getOperation().equals("openScreenNtf")) {
                        contentStr = holder.getContext().getResources().getString(R.string.seal_set_screen_capture_open);
                    } else if (content.getOperation().equals("closeScreenNtf")) {
                        contentStr = holder.getContext().getResources().getString(R.string.seal_set_screen_capture_close);
                    } else if (content.getOperation().equals("sendScreenNtf")) {
                        contentStr = holder.getContext().getResources().getString(R.string.seal_set_screen_capture_use);
                    }
                    if (!TextUtils.isEmpty(operatorUserName)) {
                        if (content.getOperatorUserId().equals(RongIM.getInstance().getCurrentUserId())) {
                            contentStr = holder.getContext().getResources().getString(R.string.seal_set_screen_capture_you) + contentStr;
                        } else {
                            contentStr = operatorUserName + " " + contentStr;
                        }
                    }
                }
                if (!TextUtils.isEmpty(contentStr)) {
                    textView.setText(contentStr);
                }
            }
        }
    }

    @Override
    protected boolean isMessageViewType(MessageContent messageContent) {
        return messageContent instanceof SealGroupConNtfMessage;
    }

    @Override
    public Spannable getSummarySpannable(Context context, SealGroupConNtfMessage message) {
        if (message == null) {
            return null;
        }
        if (!TextUtils.isEmpty(message.getOperation())) {
            String content = "";
            if (message.getOperation().equals("closeRegularClear")) {
                content = context.getResources().getString(R.string.seal_set_clean_time_notification_close);
            } else if (message.getOperation().equals("openRegularClear")) {
                content = context.getResources().getString(R.string.seal_set_clean_time_notification_open);
            } else {
                String operatorUserName = "";
                if (RongUserInfoManager.getInstance().getUserInfo(message.getOperatorUserId()) != null) {
                    operatorUserName = RongUserInfoManager.getInstance().getUserInfo(message.getOperatorUserId()).getName();
                }
                if (message.getOperation().equals("openScreenNtf")) {
                    content = context.getResources().getString(R.string.seal_set_screen_capture_open);
                } else if (message.getOperation().equals("closeScreenNtf")) {
                    content = context.getResources().getString(R.string.seal_set_screen_capture_close);
                } else if (message.getOperation().equals("sendScreenNtf")) {
                    content = context.getResources().getString(R.string.seal_set_screen_capture_use);
                }
                if (!TextUtils.isEmpty(operatorUserName)) {
                    if (message.getOperatorUserId().equals(RongIM.getInstance().getCurrentUserId())) {
                        operatorUserName = context.getResources().getString(R.string.seal_set_screen_capture_you);
                    }
                    content = operatorUserName + " " + content;
                }
            }
            return new SpannableString(content);

        }
        return null;
    }
}
