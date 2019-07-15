package cn.rongcloud.im.ui.fragment;

import androidx.lifecycle.ViewModelProviders;

import cn.rongcloud.im.viewmodel.ForwardRecentListViewModel;
import cn.rongcloud.im.viewmodel.ForwardRecentSelectListViewModel;

/**
 *  转发最近联系人列表
 */
public class ForwordRecentMultiSelectListFragment extends ForwordRecentListFragment {

    @Override
    protected ForwardRecentListViewModel createViewModel() {
        return ViewModelProviders.of(this).get(ForwardRecentSelectListViewModel.class);
    }
}
