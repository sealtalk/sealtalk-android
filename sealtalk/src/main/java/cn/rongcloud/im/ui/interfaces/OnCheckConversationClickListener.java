package cn.rongcloud.im.ui.interfaces;

import cn.rongcloud.im.ui.adapter.models.CheckableContactModel;
import io.rong.imlib.model.Conversation;

public interface OnCheckConversationClickListener {
    void onCheckConversationClick(CheckableContactModel<Conversation> conversation);
}
