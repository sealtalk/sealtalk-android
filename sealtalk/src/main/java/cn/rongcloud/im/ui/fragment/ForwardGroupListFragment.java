package cn.rongcloud.im.ui.fragment;

import androidx.lifecycle.ViewModelProviders;

import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.viewmodel.CommonListBaseViewModel;
import cn.rongcloud.im.viewmodel.ForwardGroupListViewModel;

public class ForwardGroupListFragment extends CommonListBaseFragment {

    @Override
    protected CommonListBaseViewModel createViewModel() {
        boolean isSelect = getArguments().getBoolean(IntentExtra.IS_SELECT, false);
        return ViewModelProviders.of(this, new ForwardGroupListViewModel.Factory(isSelect, getActivity().getApplication())).get(ForwardGroupListViewModel.class);
    }
}
