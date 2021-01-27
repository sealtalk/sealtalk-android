package cn.rongcloud.im.ui.fragment;

import cn.rongcloud.im.viewmodel.SelectBaseViewModel;

public class SelectFriendsExcludeGroupFragment extends SelectMultiFriendFragment {
    private String groupId;
    private SelectBaseViewModel selectBaseViewModel;

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    protected SelectBaseViewModel getViewModel() {
        selectBaseViewModel = super.getViewModel();
        return selectBaseViewModel;
    }

    @Override
    protected void onLoadData(SelectBaseViewModel viewModel) {
        viewModel.loadFriendShipExclude(groupId, uncheckableInitIdList);
    }

    @Override
    public void search(String keyword) {
        selectBaseViewModel.searchFriendshipExclude(groupId, keyword);
    }

    @Override
    public void loadAll() {
        selectBaseViewModel.loadFriendShipExclude(groupId);
    }
}
