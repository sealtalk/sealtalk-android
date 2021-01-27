package cn.rongcloud.im.ui.fragment;

import androidx.lifecycle.ViewModelProviders;

import cn.rongcloud.im.viewmodel.SelectBaseViewModel;
import cn.rongcloud.im.viewmodel.SelectSingleViewModel;

public class SelectSingleFragment extends SelectBaseFragment {

    private static final String TAG = "SelectSingleFragment";
    @Override
    protected SelectBaseViewModel getViewModel() {
        return ViewModelProviders.of(this).get(SelectSingleViewModel.class);
    }
}
