package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.UserSimpleInfo;
import cn.rongcloud.im.ui.adapter.BlackListAdapter;
import cn.rongcloud.im.viewmodel.BlackListViewModel;

public class BlackListActivity extends TitleBaseActivity{
    private BlackListAdapter adapter;

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
    }

    /**
     * 初始话Viewmodel
     */
    private void initViewModel() {
        BlackListViewModel blackListViewModel = ViewModelProviders.of(this).get(BlackListViewModel.class);
        blackListViewModel.getBlackListResult().observe(this, new Observer<Resource<List<UserSimpleInfo>>>() {
            @Override
            public void onChanged(Resource<List<UserSimpleInfo>> listResource) {
                if (listResource != null && listResource.data != null) {
                    adapter.updateData(listResource.data);
                }
            }
        });
    }
}
