package cn.rongcloud.im.im.provider;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cn.rongcloud.im.R;
import io.rong.imkit.conversation.messgelist.provider.GroupNotificationMessageItemProvider;
import io.rong.imkit.model.UiMessage;
import io.rong.imkit.widget.adapter.IViewProviderListener;
import io.rong.imkit.widget.adapter.ViewHolder;
import io.rong.message.GroupNotificationMessage;

public class SealGroupNotificationMessageItemProvider extends GroupNotificationMessageItemProvider {

    @Override
    protected void bindMessageContentViewHolder(ViewHolder holder, ViewHolder parentHolder, GroupNotificationMessage content, UiMessage message, int position, List<UiMessage> list, IViewProviderListener<UiMessage> listener) {
        if (content != null && message != null) {
            if (content != null && content.getData() == null) {
                return;
            }

            if (!TextUtils.isEmpty(content.getOperation())) {
                if (content.getOperation().equals("Transfer")) {
                    String data = content.getData();
                    if (!TextUtils.isEmpty(data)) {
                        TextView textView = holder.getView(R.id.rc_msg);
                        try {
                            JSONObject jsonObject = new JSONObject(data);
                            JSONArray targetUserDisplayNames = jsonObject.getJSONArray("targetUserDisplayNames");
                            StringBuffer buffer = new StringBuffer();
                            for (int i = 0; i < targetUserDisplayNames.length(); i++) {
                                buffer.append(targetUserDisplayNames.get(i));
                                buffer.append(",");
                            }
                            buffer.deleteCharAt(buffer.length() - 1);
                            String contentStr = holder.getContext().getResources().getString(R.string.seal_group_action_transfer_group_owner, buffer.toString());
                            textView.setText(contentStr);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                } else if (content.getOperation().equals("SetManager")) {
                    String data = content.getData();
                    if (!TextUtils.isEmpty(data)) {
                        TextView textView = holder.getView(R.id.rc_msg);
                        try {
                            JSONObject jsonObject = new JSONObject(data);
                            JSONArray targetUserDisplayNames = jsonObject.getJSONArray("targetUserDisplayNames");
                            StringBuffer buffer = new StringBuffer();
                            for (int i = 0; i < targetUserDisplayNames.length(); i++) {
                                buffer.append(targetUserDisplayNames.get(i));
                                buffer.append(",");
                            }
                            buffer.deleteCharAt(buffer.length() - 1);
                            String contentStr = holder.getContext().getResources().getString(R.string.seal_group_action_set_manager, buffer.toString());
                            textView.setText(contentStr);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                } else if (content.getOperation().equals("RemoveManager")) {

                    String data = content.getData();
                    if (!TextUtils.isEmpty(data)) {
                        TextView textView = holder.getView(R.id.rc_msg);
                        try {
                            JSONObject jsonObject = new JSONObject(data);
                            JSONArray targetUserDisplayNames = jsonObject.getJSONArray("targetUserDisplayNames");
                            StringBuffer buffer = new StringBuffer();
                            for (int i = 0; i < targetUserDisplayNames.length(); i++) {
                                buffer.append(targetUserDisplayNames.get(i));
                                buffer.append(",");
                            }
                            buffer.deleteCharAt(buffer.length() - 1);
                            String contentStr = holder.getContext().getResources().getString(R.string.seal_group_action_remove_manager, buffer.toString());
                            textView.setText(contentStr);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (content.getOperation().equals("openMemberProtection")) {
                    TextView textView = holder.getView(R.id.rc_msg);
                    String contentStr = holder.getContext().getResources().getString(R.string.seal_group_member_protection_open);
                    textView.setText(contentStr);
                } else if (content.getOperation().equals("closeMemberProtection")) {
                    TextView textView = holder.getView(R.id.rc_msg);
                    String contentStr = holder.getContext().getResources().getString(R.string.seal_group_member_protection_close);
                    textView.setText(contentStr);
                } else {
                    super.bindMessageContentViewHolder(holder, parentHolder, content, message, position, list, listener);
                }
            } else {
                super.bindMessageContentViewHolder(holder, parentHolder, content, message, position, list, listener);
            }
        }
    }

    @Override
    public Spannable getSummarySpannable(Context context, GroupNotificationMessage message) {
        if (message != null && message.getData() == null) {
            return null;
        }
        String operation = message.getOperation();
        if (!TextUtils.isEmpty(operation)) {
            if (message.getOperation().equals("Transfer")) {
                String data = message.getData();
                if (!TextUtils.isEmpty(data)) {
                    try {
                        JSONObject jsonObject = new JSONObject(data);
                        JSONArray targetUserDisplayNames = jsonObject.getJSONArray("targetUserDisplayNames");
                        StringBuffer buffer = new StringBuffer();
                        for (int i = 0; i < targetUserDisplayNames.length(); i++) {
                            buffer.append(targetUserDisplayNames.get(i));
                            buffer.append(",");
                        }
                        buffer.deleteCharAt(buffer.length() - 1);
                        String content = context.getResources().getString(R.string.seal_group_action_transfer_group_owner, buffer.toString());
                        return new SpannableString(content);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else if (message.getOperation().equals("SetManager")) {
                String data = message.getData();
                if (!TextUtils.isEmpty(data)) {
                    try {
                        JSONObject jsonObject = new JSONObject(data);
                        JSONArray targetUserDisplayNames = jsonObject.getJSONArray("targetUserDisplayNames");
                        StringBuffer buffer = new StringBuffer();
                        for (int i = 0; i < targetUserDisplayNames.length(); i++) {
                            buffer.append(targetUserDisplayNames.get(i));
                            buffer.append(",");
                        }
                        buffer.deleteCharAt(buffer.length() - 1);
                        String contentStr = context.getResources().getString(R.string.seal_group_action_set_manager, buffer.toString());
                        return new SpannableString(contentStr);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            } else if (message.getOperation().equals("RemoveManager")) {
                String data = message.getData();
                if (!TextUtils.isEmpty(data)) {
                    try {
                        JSONObject jsonObject = new JSONObject(data);
                        JSONArray targetUserDisplayNames = jsonObject.getJSONArray("targetUserDisplayNames");
                        StringBuffer buffer = new StringBuffer();
                        for (int i = 0; i < targetUserDisplayNames.length(); i++) {
                            buffer.append(targetUserDisplayNames.get(i));
                            buffer.append(",");
                        }
                        buffer.deleteCharAt(buffer.length() - 1);
                        String contentStr = context.getResources().getString(R.string.seal_group_action_remove_manager, buffer.toString());
                        return new SpannableString(contentStr);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else if (message.getOperation().equals("openMemberProtection")) {
                String contentStr = context.getResources().getString(R.string.seal_group_member_protection_open);
                return new SpannableString(contentStr);
            } else if (message.getOperation().equals("closeMemberProtection")) {
                String contentStr = context.getResources().getString(R.string.seal_group_member_protection_close);
                return new SpannableString(contentStr);
            }

        }
        return super.getSummarySpannable(context, message);
    }
}
