package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.model.UserSimpleInfo;
import cn.rongcloud.im.ui.adapter.BlackListAdapter;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.viewmodel.BlackListViewModel;
import io.rong.imkit.widget.dialog.OptionsPopupDialog;

public class BlackListActivity extends TitleBaseActivity {
    private BlackListAdapter adapter;
    private BlackListViewModel blackListViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blacklist);
        initView();
        initViewModel();
    }

    /**
     * 初始化布局
     */
    private void initView() {
        getTitleBar().setTitle(R.string.seal_privacy_blacklist);
        View isNullTv = findViewById(R.id.tv_is_null);
        ListView blackListLv = findViewById(R.id.lv_black_list);
        blackListLv.setEmptyView(isNullTv);
        adapter = new BlackListAdapter();
        blackListLv.setAdapter(adapter);
        blackListLv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                UserSimpleInfo userSimpleInfo = (UserSimpleInfo)adapter.getItem(position);
                String userId = userSimpleInfo.getId();
                showLongClickedDialog(userId);
                return true;
            }
        });
    }

    /**
     * 显示长按菜单
     *
     * @param userId
     */
    private void showLongClickedDialog(String userId) {
        String[] items = new String[]{getString(R.string.profile_detail_remove_from_blacklist)};
        OptionsPopupDialog.newInstance(this, items).setOptionsPopupDialogListener(new OptionsPopupDialog.OnOptionsItemClickedListener() {
            @Override
            public void onOptionsItemClicked(int i) {
                // 移除黑名单
                if(i == 0) {
                    blackListViewModel.removeFromBlackList(userId);
                }
            }
        }).show();
    }

    /**
     * 初始话Viewmodel
     */
    private void initViewModel() {
        blackListViewModel = ViewModelProviders.of(this).get(BlackListViewModel.class);
        blackListViewModel.getBlackListResult().observe(this, new Observer<Resource<List<UserSimpleInfo>>>() {
            @Override
            public void onChanged(Resource<List<UserSimpleInfo>> listResource) {
                if (listResource != null && listResource.data != null) {
                    adapter.updateData(listResource.data);
                }
            }
        });

        // 获取移除黑名单结果
        blackListViewModel.getRemoveBlackListResult().observe(this, new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> resource) {
                if (resource.status == Status.SUCCESS) {
                    ToastUtils.showToast(R.string.common_remove_successful);
                } else if (resource.status == Status.ERROR) {
                    ToastUtils.showToast(resource.message);
                }
            }
        });
    }
}
