package cn.rongcloud.im.ui.adapter;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;

import androidx.recyclerview.widget.DiffUtil;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.model.ContactNotificationMessageData;
import io.rong.imkit.conversationlist.ConversationListAdapter;
import io.rong.imkit.conversationlist.model.BaseUiConversation;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.MessageContent;
import io.rong.message.ContactNotificationMessage;

public class ConversationListAdapterEx extends ConversationListAdapter {
    @Override
    public void setDataCollection(final List<BaseUiConversation> data) {
        if (data!=null){
            for (BaseUiConversation baseUiConversation : data){
                Conversation conversation = baseUiConversation.mCore;
                MessageContent messageContent = conversation.getLatestMessage();
                if (messageContent instanceof ContactNotificationMessage){
                    ContactNotificationMessage content = (ContactNotificationMessage) messageContent;
                    String summaryContent = "";
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
                                    summaryContent = bean.getSourceUserNickname()+" "+IMManager.getInstance().getContext().getString(R.string.msg_contact_notification_someone_agree_your_request);
                                }
                            }
                        }
                    }
                    conversation.setConversationTitle(IMManager.getInstance().getContext().getString(R.string.seal_friend_message));
                    if (!TextUtils.isEmpty(summaryContent)){
                        baseUiConversation.mConversationContent = new SpannableString(summaryContent);
                    }
                }
            }
        }
        if (mDataList == null || mDataList.size() == 0) {
            mDataList = data;
            notifyDataSetChanged();
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mDataList.size();
                }

                @Override
                public int getNewListSize() {
                    return data.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    BaseUiConversation oldItem = mDataList.get(oldItemPosition);
                    BaseUiConversation newItem = data.get(newItemPosition);
                    return oldItem.mCore.getTargetId().equals(newItem.mCore.getTargetId())
                            && oldItem.mCore.getConversationType().equals(newItem.mCore.getConversationType());
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    return false;
                }
            });
            mDataList = data;
            notifyDataSetChanged();
        }
    }
}
