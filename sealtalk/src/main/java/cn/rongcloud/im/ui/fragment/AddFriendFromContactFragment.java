package cn.rongcloud.im.ui.fragment;

import androidx.lifecycle.ViewModelProviders;

import cn.rongcloud.im.ui.adapter.AddFriendFromContactListAdapter;
import cn.rongcloud.im.ui.adapter.ListWithSideBarBaseAdapter;
import cn.rongcloud.im.ui.adapter.viewholders.AddFriendFromContactItemViewHolder;
import cn.rongcloud.im.viewmodel.AddFriendFromContactViewModel;
import cn.rongcloud.im.viewmodel.CommonListBaseViewModel;

public class AddFriendFromContactFragment extends CommonListBaseFragment {
    private AddFriendFromContactItemViewHolder.OnAddFriendClickedListener addFriendClickedListener;
    private AddFriendFromContactViewModel addFriendFromContactViewModel;
    @Override
    protected CommonListBaseViewModel createViewModel() {
        addFriendFromContactViewModel = ViewModelProviders.of(this).get(AddFriendFromContactViewModel.class);
        return addFriendFromContactViewModel;
    }

    @Override
    protected boolean isUseSideBar() {
        return true;
    }

    @Override
    protected ListWithSideBarBaseAdapter getListAdapter() {
        AddFriendFromContactListAdapter listAdapter = new AddFriendFromContactListAdapter();
        listAdapter.setAddFriendClickedListener(addFriendClickedListener);
        return listAdapter;
    }

    public void setAddFriendClickedListener(AddFriendFromContactItemViewHolder.OnAddFriendClickedListener listener) {
        this.addFriendClickedListener = listener;
    }

    public void search(String keyword){
        addFriendFromContactViewModel.search(keyword);
    }
}
