package cn.rongcloud.im.ui.fragment;

import android.view.View;

import androidx.lifecycle.ViewModelProviders;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.model.SimplePhoneContactInfo;
import cn.rongcloud.im.ui.adapter.CommonListAdapter;
import cn.rongcloud.im.ui.adapter.ListWithSideBarBaseAdapter;
import cn.rongcloud.im.ui.adapter.models.ListItemModel;
import cn.rongcloud.im.viewmodel.CommonListBaseViewModel;
import cn.rongcloud.im.viewmodel.InviteFriendFromContactViewModel;

public class InviteFriendFromContactFragment extends CommonListBaseFragment {
    private OnContactSelectedListener onContactSelectedListener;
    private InviteFriendFromContactViewModel inviteFriendFromContactViewModel;

    @Override
    protected CommonListBaseViewModel createViewModel() {
        inviteFriendFromContactViewModel = ViewModelProviders.of(this).get(InviteFriendFromContactViewModel.class);
        return inviteFriendFromContactViewModel;
    }

    @Override
    protected boolean isUseSideBar() {
        return true;
    }

    @Override
    protected ListWithSideBarBaseAdapter getAdapter() {
        CommonListAdapter adapter = (CommonListAdapter) super.getAdapter();
        adapter.setOnItemClickListener(new CommonListAdapter.OnItemClickListener() {
            @Override
            public void onClick(View v, int position, ListItemModel data) {
                if (onContactSelectedListener != null) {
                    int selectedSize = adapter.getSelectedOtherIds().size();
                    onContactSelectedListener.OnContactSelected(data, selectedSize);
                }
            }
        });
        return adapter;
    }

    /**
     * 获取当前选择的联系人
     *
     * @return
     */
    public List<SimplePhoneContactInfo> getCheckedContactInfo() {
        List<SimplePhoneContactInfo> result = new ArrayList<>();
        CommonListAdapter listAdapter = (CommonListAdapter) getListAdapter();
        List<ListItemModel> data = listAdapter.getData();
        List<String> selectedOtherIds = listAdapter.getSelectedOtherIds();
        if (data != null) {
            for (ListItemModel model : data) {
                if (model.getData() instanceof SimplePhoneContactInfo &&
                        (model.getCheckStatus() == ListItemModel.CheckStatus.CHECKED
                         || selectedOtherIds.contains(model.getId()) // 当 adapter 未执行 onBindViewHolder 时，部分选择状态没有被刷新
                        )) {
                    SimplePhoneContactInfo info = (SimplePhoneContactInfo) model.getData();
                    result.add(info);
                }
            }
        }

        return result;
    }

    public interface OnContactSelectedListener {
        void OnContactSelected(ListItemModel changedModel, int totalSelected);
    }

    public void setOnContactSelectedListener(OnContactSelectedListener listener) {
        this.onContactSelectedListener = listener;
    }

    public void search(String keyword) {
        inviteFriendFromContactViewModel.search(keyword);
    }

}
