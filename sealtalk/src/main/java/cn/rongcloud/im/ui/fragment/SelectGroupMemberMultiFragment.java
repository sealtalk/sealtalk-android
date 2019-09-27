package cn.rongcloud.im.ui.fragment;

import cn.rongcloud.im.viewmodel.SelectBaseViewModel;

public class SelectGroupMemberMultiFragment extends SelectMultiFriendFragment {
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
        viewModel.loadGroupMemberExclude(groupId, excludeInitIdList, checkedInitIdList);
    }

    @Override
    public void search(String keyword) {
        selectBaseViewModel.searchGroupMemberExclude(groupId, keyword);
    }

    @Override
    public void loadAll() {
        selectBaseViewModel.loadGroupMemberExclude(groupId);
    }
}
