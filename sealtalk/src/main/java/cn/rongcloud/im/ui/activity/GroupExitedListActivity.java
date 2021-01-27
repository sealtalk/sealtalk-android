package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.db.model.GroupExitedMemberInfo;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.ui.adapter.GroupExitedListAdapter;
import cn.rongcloud.im.viewmodel.GroupExitedInfoViewModel;

public class GroupExitedListActivity extends TitleBaseActivity {

    private ListView groupExitedList;
    private TextView isNull;
    private GroupExitedListAdapter mAdapter;
    private GroupExitedInfoViewModel groupExitedInfoViewModel;
    private String groupId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_exited);
        groupId = getIntent().getStringExtra(IntentExtra.GROUP_ID);
        initView();
        initViewModel();
    }

    private void initView() {
        getTitleBar().setTitle(getString(R.string.seal_group_manager_exited_title));
        getTitleBar().getBtnLeft().setText(getResources().getString(R.string.seal_group_exit_list_left_title));
        groupExitedList = findViewById(R.id.lv_group_exited_list);
        isNull = findViewById(R.id.tv_is_null);
        mAdapter = new GroupExitedListAdapter();
        groupExitedList.setAdapter(mAdapter);
    }

    private void initViewModel() {
        groupExitedInfoViewModel = ViewModelProviders.of(this).get(GroupExitedInfoViewModel.class);
        groupExitedInfoViewModel.requestExitedInfo(groupId);
        groupExitedInfoViewModel.getExitedInfo().observe(this, new Observer<Resource<List<GroupExitedMemberInfo>>>() {
            @Override
            public void onChanged(Resource<List<GroupExitedMemberInfo>> listResource) {
                if (listResource.status != Status.LOADING) {
                    if (listResource.data != null) {
                        mAdapter.updateList(listResource.data);
                        if (listResource.data.size() == 0) {
                            isNull.setVisibility(View.VISIBLE);
                        } else {
                            isNull.setVisibility(View.GONE);
                        }
                    }
                }
            }
        });
    }
}
