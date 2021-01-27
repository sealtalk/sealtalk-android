package cn.rongcloud.im.ui.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.lifecycle.ViewModelProviders;

import java.util.ArrayList;

import cn.rongcloud.im.ui.adapter.models.CheckableContactModel;
import cn.rongcloud.im.ui.interfaces.OnSelectCountChangeListener;
import cn.rongcloud.im.viewmodel.SelectBaseViewModel;
import cn.rongcloud.im.viewmodel.SelectMultiViewModel;

public class SelectMultiFriendFragment extends SelectBaseFragment {
    private static final String TAG = "SelectMultiFriendFragment";
    private OnSelectCountChangeListener onSelectCountChangeListener;
    private SelectMultiViewModel selectMultiViewModel;

    @Override
    protected void onInitView(Bundle savedInstanceState, Intent intent) {
        super.onInitView(savedInstanceState, intent);
    }

    @Override
    protected SelectBaseViewModel getViewModel() {
        selectMultiViewModel = ViewModelProviders.of(getActivity()).get(SelectMultiViewModel.class);
        return selectMultiViewModel;
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
            ArrayList<String> checkedGroupList = getCheckedGroupList();
            if (checkedGroupList != null) {
                groupCount = checkedGroupList.size();
            }

            int friendCount = 0;
            ArrayList<String> checkedFriendList = getCheckedFriendList();
            if(checkedFriendList != null){
                friendCount = checkedFriendList.size();
            }

            onSelectCountChangeListener.onSelectCountChange(groupCount, friendCount);
        }
    }

    @Override
    protected void onLoadData(SelectBaseViewModel viewModel) {
        viewModel.loadFriendShip(uncheckableInitIdList, checkedInitIdList, checkedInitGroupList);
    }

    public void search(String keyword){
        selectMultiViewModel.searchFriend(keyword);
    }

    public void loadAll(){
        selectMultiViewModel.loadFriendShip();
    }

}
