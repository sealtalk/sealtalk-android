package cn.rongcloud.im.ui.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.lifecycle.ViewModelProviders;

import cn.rongcloud.im.ui.adapter.models.CheckableContactModel;
import cn.rongcloud.im.ui.interfaces.OnSelectCountChangeListener;
import cn.rongcloud.im.viewmodel.SelectBaseViewModel;
import cn.rongcloud.im.viewmodel.SelectMultiViewModel;

public class SelectMultiFriendFragment extends SelectBaseFragment {
    private static final String TAG = "SelectMultiFriendFragment";
    private OnSelectCountChangeListener onSelectCountChangeListener;

    @Override
    protected void onInitView(Bundle savedInstanceState, Intent intent) {
        super.onInitView(savedInstanceState, intent);
    }

    @Override
    protected SelectBaseViewModel getViewModel() {
        return ViewModelProviders.of(getActivity()).get(SelectMultiViewModel.class);
    }

    @Override
    public void onContactContactClick(CheckableContactModel contactModel) {
        super.onContactContactClick(contactModel);
        changeCheckCount();
    }

    public void setOnSelectCountChangeListener(OnSelectCountChangeListener listener) {
        onSelectCountChangeListener = listener;
    }

    @Override
    protected void onDataShowed() {
        changeCheckCount();
    }

    private void changeCheckCount() {
        if (onSelectCountChangeListener != null) {
            int groupCount = 0;
            if (getCheckedGroupList() != null) {
                groupCount = getCheckedGroupList().size();
            }
            onSelectCountChangeListener.onSelectCountChange(groupCount, getCheckedList().size());
        }
    }
}
