package cn.rongcloud.im.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import cn.rongcloud.im.R;
import cn.rongcloud.im.im.message.GroupApplyMessage;
import cn.rongcloud.im.ui.activity.GroupNoticeListActivity;
import cn.rongcloud.im.ui.activity.NewFriendListActivity;
import cn.rongcloud.im.ui.adapter.ConversationListAdapterEx;
import io.rong.common.RLog;
import io.rong.imkit.config.ConversationListBehaviorListener;
import io.rong.imkit.config.RongConfigCenter;
import io.rong.imkit.conversationlist.ConversationListAdapter;
import io.rong.imkit.conversationlist.model.BaseUiConversation;
import io.rong.imkit.conversationlist.model.GatheredConversation;
import io.rong.imkit.subconversationlist.SubConversationListFragment;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imkit.widget.adapter.BaseAdapter;
import io.rong.imkit.widget.adapter.ViewHolder;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.MessageContent;
import io.rong.message.ContactNotificationMessage;

public class SubConversationListFragmentEx extends SubConversationListFragment {

    private final static String TAG = "SubConversationListFragmentEx";

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter.setItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, ViewHolder holder, int position) {
                BaseUiConversation baseUiConversation = mAdapter.getItem(position);
                MessageContent messageContent = baseUiConversation.mCore.getLatestMessage();
                if (messageContent instanceof ContactNotificationMessage) {
//                    ContactNotificationMessage contactNotificationMessage = (ContactNotificationMessage) messageContent;
//                    if (contactNotificationMessage.getOperation().equals("AcceptResponse")) {
//                        // 被加方同意请求后
//                        if (contactNotificationMessage.getExtra() != null) {
//                            ContactNotificationMessageData bean = null;
//                            try {
//                                Gson gson = new Gson();
//                                bean = gson.fromJson(contactNotificationMessage.getExtra(), ContactNotificationMessageData.class);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                            Bundle bundle = new Bundle();
//                            bundle.putString(RouteUtils.TITLE, getString(R.string.seal_friend_message));
//                            RouteUtils.routeToConversationActivity(getActivity(), baseUiConversation.mCore.getConversationType(), baseUiConversation.mCore.getTargetId(), bundle);
//                        }
//                    } else {
                        getActivity().startActivity(new Intent(getActivity(), NewFriendListActivity.class));
//                    }
                } else if (messageContent instanceof GroupApplyMessage) {
                    Intent noticeListIntent = new Intent(getActivity(), GroupNoticeListActivity.class);
                    getActivity().startActivity(noticeListIntent);
                } else {
                    if (position < 0) {
                        return;
                    }
                    ConversationListBehaviorListener listBehaviorListener = RongConfigCenter.conversationListConfig().getListener();
                    if (listBehaviorListener != null && listBehaviorListener.onConversationClick(view.getContext(), view, baseUiConversation)) {
                        RLog.d(TAG, "ConversationList item click event has been intercepted by App.");
                        return;
                    }
                    if (baseUiConversation != null && baseUiConversation.mCore != null) {
                        if (baseUiConversation instanceof GatheredConversation) {
                            RouteUtils.routeToSubConversationListActivity(view.getContext(), ((GatheredConversation) baseUiConversation).mGatheredType, baseUiConversation.mCore.getConversationTitle());
                        } else {
                            RouteUtils.routeToConversationActivity(view.getContext(), baseUiConversation.mCore.getConversationType(), baseUiConversation.mCore.getTargetId());
                        }
                    } else {
                        RLog.e(TAG, "invalid conversation.");
                    }
                }
                RongConfigCenter.gatheredConversationConfig().setConversationTitle(Conversation.ConversationType.SYSTEM, R.string.seal_conversation_title_system);
            }

            @Override
            public boolean onItemLongClick(View view, ViewHolder holder, int position) {
                return false;
            }
        });
    }

    @Override
    protected ConversationListAdapter onResolveAdapter() {
        return new ConversationListAdapterEx();
    }

}
