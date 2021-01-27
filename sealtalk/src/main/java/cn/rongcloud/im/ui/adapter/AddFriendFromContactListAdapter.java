package cn.rongcloud.im.ui.adapter;

import androidx.annotation.NonNull;

import cn.rongcloud.im.ui.adapter.viewholders.AddFriendFromContactItemViewHolder;
import cn.rongcloud.im.ui.adapter.viewholders.BaseItemViewHolder;

/**
 * 从通讯录加好友列表适配
 */
public class AddFriendFromContactListAdapter extends CommonListAdapter {
    private AddFriendFromContactItemViewHolder.OnAddFriendClickedListener addFriendClickedListener;


    @Override
    public void onBindViewHolder(@NonNull BaseItemViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (holder instanceof AddFriendFromContactItemViewHolder){
            AddFriendFromContactItemViewHolder contactItemViewHolder = (AddFriendFromContactItemViewHolder) holder;
            contactItemViewHolder.setAddFriendClickedListener(addFriendClickedListener);
        }
    }

    public void setAddFriendClickedListener(AddFriendFromContactItemViewHolder.OnAddFriendClickedListener addFriendClickedListener) {
        this.addFriendClickedListener = addFriendClickedListener;
    }
}
