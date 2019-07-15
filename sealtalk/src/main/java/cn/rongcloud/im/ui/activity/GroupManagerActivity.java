package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.model.GroupMember;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.ui.view.SettingItemView;
import cn.rongcloud.im.viewmodel.GroupManagementViewModel;

public class GroupManagerActivity extends TitleBaseActivity implements View.OnClickListener {

    private static final int MANAGEMENT_MAX = 5;
    private static final int REQUEST_SET_NEW_OWNER = 1000;
    private String groupId;
    private GroupManagementViewModel groupManagementViewModel;
    private SettingItemView setGroupManagerSiv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_manager);
        groupId = getIntent().getStringExtra(IntentExtra.GROUP_ID);
        initView();
        initViewModel();
    }

    // 初始化布局
    private void initView() {
        getTitleBar().setTitle(R.string.seal_group_detail_group_manager);
        setGroupManagerSiv = findViewById(R.id.siv_set_group_manager);
        setGroupManagerSiv.setOnClickListener(this);
        SettingItemView transferSiv  = findViewById(R.id.siv_transfer);
        transferSiv.setOnClickListener(this);
    }

    private void initViewModel() {
        groupManagementViewModel = ViewModelProviders.of(this, new GroupManagementViewModel.Factory(groupId, getApplication())).get(GroupManagementViewModel.class);

        groupManagementViewModel.getGroupManagements().observe(this, new Observer<Resource<List<GroupMember>>>() {
            @Override
            public void onChanged(Resource<List<GroupMember>> resource) {
                int managementNumber = resource.data == null? 0 : resource.data.size();
                // 减掉1 ， 因为有一条是添加管理员的item
                setGroupManagerSiv.setValue(managementNumber +"/" + MANAGEMENT_MAX);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.siv_set_group_manager:
                Intent intent = new Intent(this, GroupManagementsActivity.class);
                intent.putExtra(IntentExtra.GROUP_ID, groupId);
                startActivity(intent);
                break;
            case R.id.siv_transfer:
                Intent intentTransfer = new Intent(this, GroupSetNewGroupOwnerActivity.class);
                intentTransfer.putExtra(IntentExtra.GROUP_ID, groupId);
                startActivityForResult(intentTransfer, REQUEST_SET_NEW_OWNER);
                break;
            default:
                // Do nothing
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SET_NEW_OWNER && resultCode == RESULT_OK) {
            finish();
        }
    }
}
