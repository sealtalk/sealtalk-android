package cn.rongcloud.im.ui.test.provider;

import android.content.Intent;
import android.view.View;

import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.ui.activity.GroupReadReceiptDetailActivity;
import cn.rongcloud.im.ui.test.GRRDetailTestActivity;
import io.rong.common.rlog.RLog;
import io.rong.imkit.config.ConversationClickListener;
import io.rong.imkit.config.RongConfigCenter;
import io.rong.imkit.conversation.messgelist.provider.MessageClickType;
import io.rong.imkit.conversation.messgelist.provider.TextMessageItemProvider;
import io.rong.imkit.model.UiMessage;
import io.rong.imkit.widget.adapter.IViewProviderListener;
import io.rong.imkit.widget.adapter.ViewHolder;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.TextMessage;

/**
 * Created by yanke on 2021/3/11
 */
public class GroupReadReceiptTextMessageItemProvider extends TextMessageItemProvider {

    @Override
    protected void bindMessageContentViewHolder(ViewHolder holder, ViewHolder parentHolder, TextMessage textMessage, UiMessage uiMessage, int position, List<UiMessage> list, IViewProviderListener<UiMessage> listener) {
        super.bindMessageContentViewHolder(holder, parentHolder, textMessage, uiMessage, position, list, listener);

        boolean isSender = uiMessage.getMessage().getMessageDirection().equals(Message.MessageDirection.SEND);
        if (!isSender) {
            return;
        }
        parentHolder.setVisible(R.id.rc_read_receipt_request, false);
        parentHolder.setVisible(R.id.rc_read_receipt_status, true);

        Message message = uiMessage.getMessage();
        if (message.getGroupReadReceiptInfoV2() != null && message.getGroupReadReceiptInfoV2().getRespondUserIdList() != null) {
            parentHolder.setText(R.id.rc_read_receipt_status, message.getGroupReadReceiptInfoV2().getReadCount() + " " + holder.getContext().getString(io.rong.imkit.R.string.rc_read_receipt_status));
        } else {
            parentHolder.setText(R.id.rc_read_receipt_status, 0 + " " + holder.getContext().getString(io.rong.imkit.R.string.rc_read_receipt_status));
        }
        parentHolder.setOnClickListener(R.id.rc_read_receipt_status, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (message.getConversationType() == Conversation.ConversationType.GROUP) { //目前只适配了群组会话
                    // 群组显示未读消息的人的信息
                    Intent intent = new Intent(holder.getContext(), GRRDetailTestActivity.class);
                    intent.putExtra(IntentExtra.PARCEL_MESSAGE, message);
                    holder.getContext().startActivity(intent);
                }
            }
        });
    }
}
