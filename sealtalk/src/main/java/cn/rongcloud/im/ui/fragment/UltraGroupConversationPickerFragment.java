package cn.rongcloud.im.ui.fragment;

import android.view.View;
import cn.rongcloud.im.ui.test.MyConversationListFragment;
import cn.rongcloud.im.utils.log.SLog;
import io.rong.imkit.config.ConversationListBehaviorListener;
import io.rong.imkit.config.RongConfigCenter;
import io.rong.imkit.conversationlist.model.BaseUiConversation;
import io.rong.imkit.widget.adapter.ViewHolder;

public class UltraGroupConversationPickerFragment extends MyConversationListFragment {

    private static final String TAG = "MyConversationListPickerFragment";

    @Override
    public void onItemClick(View view, ViewHolder holder, int position) {
        if (position < 0) {
            return;
        }
        BaseUiConversation baseUiConversation = mAdapter.getItem(position);
        ConversationListBehaviorListener listBehaviorListener =
                RongConfigCenter.conversationListConfig().getListener();
        if (listBehaviorListener != null
                && listBehaviorListener.onConversationClick(
                        view.getContext(), view, baseUiConversation)) {
            SLog.i(TAG, "ConversationList item click event has been intercepted by App.");
        }
    }
}
