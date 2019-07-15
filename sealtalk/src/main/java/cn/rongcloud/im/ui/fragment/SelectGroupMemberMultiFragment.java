package cn.rongcloud.im.ui.fragment;

import cn.rongcloud.im.viewmodel.SelectBaseViewModel;

public class SelectGroupMemberMultiFragment extends SelectMultiFriendFragment {
    private String groupId;

    public SelectGroupMemberMultiFragment(String groupId) {
        this.groupId = groupId;
    }

    @Override
    protected void onLoadData(SelectBaseViewModel viewModel) {
        viewModel.loadGroupMemberExclude(groupId, excludeInitIdList);
    }
}
