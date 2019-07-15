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
import cn.rongcloud.im.model.GroupMember;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.ui.adapter.GroupManagerAdapter;
import cn.rongcloud.im.ui.dialog.CommonDialog;
import cn.rongcloud.im.ui.widget.SideBar;
import cn.rongcloud.im.viewmodel.GroupManagementViewModel;

public class GroupSetNewGroupOwnerActivity extends TitleBaseActivity {
    private String groupId;
    private GroupManagerAdapter groupManagerAdapter;
    private GroupManagementViewModel groupManagementViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_set_managements);
        groupId = getIntent().getStringExtra(IntentExtra.GROUP_ID);
        initView();
        initViewModel();
    }

    // 初始化布局
    private void initView() {

        getTitleBar().setTitle(R.string.seal_group_manager_transfer_select_new_group_owner);
        ListView memberList = findViewById(R.id.lv_list);
        SideBar sideBarSb = findViewById(R.id.sb_sidrbar);
        TextView sideDialogTv = findViewById(R.id.tv_side_dialog);

        groupManagerAdapter = new GroupManagerAdapter();
        groupManagerAdapter.setMaxSelectSize(1);
        groupManagerAdapter.setOnGroupManagerListener(new GroupManagerAdapter.OnGroupManagerListener() {
            @Override
            public void onSelected(int number, List<GroupMember> selected) {
                if (number > 0) {
                    showConfirmDialog(selected);
                }
            }

            @Override
            public void onAlreadyReachedMaxSize(List<GroupMember> selected, List<GroupMember> notSelected) {
                //Do nothing
            }
        });
        memberList.setAdapter(groupManagerAdapter);

        sideBarSb.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                //该字母首次出现的位置
                int position = groupManagerAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    memberList.setSelection(position);
                }
            }
        });
        sideBarSb.setTextView(sideDialogTv);

    }

    // 初始化 ViewModel
    private void initViewModel() {
        groupManagementViewModel = ViewModelProviders.of(this, new GroupManagementViewModel.Factory(groupId, getApplication())).get(GroupManagementViewModel.class);
        // 除群主之外的群成员
        groupManagementViewModel.getGroupMembersWithoutGroupOwner().observe(this, new Observer<List<GroupMember>>() {
            @Override
            public void onChanged(List<GroupMember> members) {
                groupManagerAdapter.updateList(members);
            }
        });
        // 转移圈住结果
        groupManagementViewModel.getTransferResult().observe(this, new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> resource) {
                //提示
                if (resource.status == Status.SUCCESS) {
                    setResult(RESULT_OK);
                    finish();
                    showToast(R.string.seal_group_management_toast_set_new_group_onwer_success);
                } else if (resource.status == Status.ERROR) {
                    showToast(R.string.seal_group_management_toast_set_new_group_onwer_failed);
                } else {

                }
            }
        });

    }

    /**
     * 转移角色
     *
     * @param groupId
     * @param userId
     */
    private void transferGroupOwner(String groupId, String userId) {
        if (groupManagementViewModel != null) {
            groupManagementViewModel.transferGroupOwner(groupId, userId);
        }
    }

    /**
     * 显示确定选择的 dialog
     *
     * @param selectedMember
     */
    private void showConfirmDialog(List<GroupMember> selectedMember) {
        GroupMember member = selectedMember.get(0);
        CommonDialog.Builder builder = new CommonDialog.Builder();
        builder.setContentMessage(getString(R.string.seal_group_manager_transfer_group_owner_dialog_content, member.getName()));
        builder.setDialogButtonClickListener(new CommonDialog.OnDialogButtonClickListener() {
            @Override
            public void onPositiveClick(View v, Bundle bundle) {
                transferGroupOwner(groupId, member.getUserId());
            }

            @Override
            public void onNegativeClick(View v, Bundle bundle) {

            }
        });
        CommonDialog dialog = builder.build();
        dialog.show(getSupportFragmentManager(), "transfer_dialog");

    }

}
