package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.ArrayList;
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

public class GroupSetManagementsActivity extends TitleBaseActivity {
    private String groupId;
    private GroupManagerAdapter groupManagerAdapter;
    private GroupManagementViewModel groupManagementViewModel;
    private int managementLeftCount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_set_managements);
        groupId = getIntent().getStringExtra(IntentExtra.GROUP_ID);
        managementLeftCount = getIntent().getIntExtra(IntentExtra.MANAGEMENT_LEFT_SELECT_COUNT, 5);
        initView();
        initViewModel();
    }

    // 初始化布局
    private void initView() {

        getTitleBar().setTitle(R.string.seal_group_management_group_managements);
        getTitleBar().setOnBtnRightClickListener(getString(R.string.seal_group_manager_confirm), new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                List<GroupMember> selectedMember = groupManagerAdapter.getSelectedMember();
                if (selectedMember != null && selectedMember.size() > 0) {
                    showConfirmDialog(selectedMember);
                }
            }
        });

        ListView memberList = findViewById(R.id.lv_list);
        SideBar sideBarSb = findViewById(R.id.sb_sidrbar);
        TextView sideDialogTv = findViewById(R.id.tv_side_dialog);

        groupManagerAdapter = new GroupManagerAdapter();
        groupManagerAdapter.setMaxSelectSize(managementLeftCount);
        groupManagerAdapter.setUseCheck(true);
        groupManagerAdapter.setOnGroupManagerListener(new GroupManagerAdapter.OnGroupManagerListener() {
            @Override
            public void onSelected(int number, List<GroupMember> selected) {
                if (number > 0) {
                    getTitleBar().setRightText(getString(R.string.seal_group_manager_confirm) + "(" + number + ")");
                } else {
                    getTitleBar().setRightText(getString(R.string.seal_group_manager_confirm));
                }
            }

            @Override
            public void onAlreadyReachedMaxSize(List<GroupMember> selected, List<GroupMember> notSelected) {
                showToast(getString(R.string.seal_group_management_toast_management_already_reached_max_size, (selected.size() + notSelected.size())));
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

        groupManagementViewModel.getGroupMembersWithoutGroupOwner().observe(this, new Observer<List<GroupMember>>() {
            @Override
            public void onChanged(List<GroupMember> members) {
                groupManagerAdapter.updateList(members);
            }
        });

        // 群管理员
        groupManagementViewModel.getGroupManagements().observe(this, new Observer<Resource<List<GroupMember>>>() {
            @Override
            public void onChanged(Resource<List<GroupMember>> members) {
                groupManagerAdapter.setNotSelected(members.data);
            }
        });


        // 添加管理员
        groupManagementViewModel.getAddManagerResult().observe(this, new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> resource) {
                // 提醒
                if (resource.status == Status.SUCCESS) {
                    showToast(R.string.seal_group_manager_set_manager_toast_set_management_success);
                    finish();
                } else if (resource.status == Status.ERROR) {
                    if (!TextUtils.isEmpty(resource.message)) {
                        showToast(resource.message);
                    } else {
                        showToast(R.string.seal_group_manager_set_manager_toast_set_management_failed);
                    }
                } else {
                    // TODO loading
                }
            }
        });
    }

    /**
     * 添加管理员
     *
     * @param selectsIds
     */
    private void addManagemenet(List<String> selectsIds) {
        if (groupManagementViewModel != null) {
            groupManagementViewModel.addManagement(selectsIds);
        }
    }


    private void showConfirmDialog(List<GroupMember> members) {

        List<String> ids = new ArrayList<>();
        StringBuffer buffer = new StringBuffer();
        for (GroupMember member : members) {
            ids.add(member.getUserId());
            buffer.append(member.getName());
            buffer.append(",");
        }
        buffer.deleteCharAt(buffer.length() - 1);

        CommonDialog.Builder builder = new CommonDialog.Builder();
        String content = getString(R.string.seal_group_manager_select_managements_dialog_content, buffer.toString());
        builder.setContentMessage(content);
        builder.setDialogButtonClickListener(new CommonDialog.OnDialogButtonClickListener() {
            @Override
            public void onPositiveClick(View v, Bundle bundle) {
                addManagemenet(ids);
            }

            @Override
            public void onNegativeClick(View v, Bundle bundle) {

            }
        });

        CommonDialog dialog = builder.build();
        dialog.show(getSupportFragmentManager(), "management_dialog");
    }

}
