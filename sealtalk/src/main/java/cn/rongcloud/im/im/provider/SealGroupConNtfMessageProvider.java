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

import cn.rongcloud.im.R;
import cn.rongcloud.im.im.message.SealGroupConNtfMessage;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imkit.widget.provider.IContainerItemProvider;

@ProviderTag(
        messageContent = SealGroupConNtfMessage.class,
        showPortrait = false,
        centerInHorizontal = true,
        showProgress = false,
        showSummaryWithName = false
)
public class SealGroupConNtfMessageProvider extends IContainerItemProvider.MessageProvider<SealGroupConNtfMessage> {


    @Override
    public void bindView(View view, int i, SealGroupConNtfMessage content, UIMessage message) {
        if (content != null && message != null) {
            if (!TextUtils.isEmpty(content.getOperation())) {
                TextView textView = view.findViewById(R.id.rc_msg);
                String contentStr = "";
                if (content.getOperation().equals("closeRegularClear")) {
                    textView.setText(view.getContext().getResources().getString(R.string.seal_set_clean_time_notification_close));
                } else if (content.getOperation().equals("openRegularClear")) {
                    contentStr = view.getContext().getResources().getString(R.string.seal_set_clean_time_notification_open);
                } else {
                    String operatorUserName = "";
                    if (RongUserInfoManager.getInstance().getUserInfo(content.getOperatorUserId()) != null) {
                        operatorUserName = RongUserInfoManager.getInstance().getUserInfo(content.getOperatorUserId()).getName();
                    }
                    if (content.getOperation().equals("openScreenNtf")) {
                        contentStr = view.getContext().getResources().getString(R.string.seal_set_screen_capture_open);
                    } else if (content.getOperation().equals("closeScreenNtf")) {
                        contentStr = view.getContext().getResources().getString(R.string.seal_set_screen_capture_close);
                    }else if (content.getOperation().equals("sendScreenNtf")){
                        contentStr = view.getContext().getResources().getString(R.string.seal_set_screen_capture_use);
                    }
                    if (!TextUtils.isEmpty(operatorUserName)) {
                        if (content.getOperatorUserId().equals(RongIM.getInstance().getCurrentUserId())){
                            contentStr = view.getContext().getResources().getString(R.string.seal_set_screen_capture_you)+ contentStr;
                        } else{
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
    public Spannable getContentSummary(SealGroupConNtfMessage sealGroupConNtfMessage) {
        return null;
    }

    @Override
    public Spannable getContentSummary(Context context, SealGroupConNtfMessage message) {
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
                    if (message.getOperatorUserId().equals(RongIM.getInstance().getCurrentUserId())){
                        operatorUserName = context.getResources().getString(R.string.seal_set_screen_capture_you);
                    }
                    content = operatorUserName + " " + content;
                }
            }
            return new SpannableString(content);

        }
        return super.getContentSummary(context, message);
    }

    @Override
    public void onItemClick(View view, int i, SealGroupConNtfMessage sealGroupConNtfMessage, UIMessage uiMessage) {

    }

    @Override
    public void onItemLongClick(View view, int position, SealGroupConNtfMessage content, UIMessage message) {

    }

    @Override
    public View newView(Context context, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.rc_group_regular_clear_notification_message, null);
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.contentTextView = view.findViewById(R.id.rc_msg);
        viewHolder.contentTextView.setMovementMethod(LinkMovementMethod.getInstance());
        view.setTag(viewHolder);
        return view;
    }

    private static class ViewHolder {
        TextView contentTextView;

        private ViewHolder() {
        }
    }
}
