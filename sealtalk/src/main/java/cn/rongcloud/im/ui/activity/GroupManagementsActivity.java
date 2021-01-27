package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
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
import cn.rongcloud.im.ui.adapter.GroupManagementAdapter;
import cn.rongcloud.im.ui.dialog.CommonDialog;
import cn.rongcloud.im.ui.view.UserInfoItemView;
import cn.rongcloud.im.utils.ImageLoaderUtils;
import cn.rongcloud.im.viewmodel.GroupManagementViewModel;

public class GroupManagementsActivity extends TitleBaseActivity {
    private static final int REQUEST_CODE = 1000;
    private static final int MANAGEMENT_MAX = 5;
    private UserInfoItemView groupOwnerUiv;
    private TextView managementsValueTv;
    private GroupManagementAdapter groupManagementAdapter;
    private GroupManagementViewModel groupManagementViewModel;
    private String groupId;
    private int managementNumber;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_managements);
        groupId = getIntent().getStringExtra(IntentExtra.GROUP_ID);
        initView();
        initViewModel();
    }

    /**
     * 初始化布局
     */
    private void initView() {
        getTitleBar().setTitle(R.string.seal_group_management_group_managements);
        groupOwnerUiv = findViewById(R.id.uiv_group_owner);
        managementsValueTv = findViewById(R.id.tv_managements);
        ListView managementList = findViewById(R.id.lv_managements);
        groupManagementAdapter = new GroupManagementAdapter();
        groupManagementAdapter.setOnManagementClickListener(new GroupManagementAdapter.OnManagementClickListener() {

            @Override
            public void onClick(View view, GroupMember member) {
                // 点击删除
                showDeleteDialog(member);
            }

            @Override
            public void onAdd(View view, GroupMember member) {
                //跳转选择界面
                Intent intent = new Intent(GroupManagementsActivity.this, GroupSetManagementsActivity.class);
                intent.putExtra(IntentExtra.GROUP_ID, groupId);
                intent.putExtra(IntentExtra.MANAGEMENT_LEFT_SELECT_COUNT, MANAGEMENT_MAX -managementNumber );
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
        managementList.setAdapter(groupManagementAdapter);
        managementsValueTv.setText(getString(R.string.seal_select_group_member) + "(0/" + MANAGEMENT_MAX +")");

    }

    /**
     * 删除管理员dialog
     * @param member
     */
    private void showDeleteDialog(final GroupMember member) {
        CommonDialog.Builder builder = new CommonDialog.Builder();
        builder.setDialogButtonClickListener(new CommonDialog.OnDialogButtonClickListener() {
            @Override
            public void onPositiveClick(View v, Bundle bundle) {
                deleteManagement(member);
            }

            @Override
            public void onNegativeClick(View v, Bundle bundle) {

            }
        });
        builder.setContentMessage(getString(R.string.seal_group_management_dialog_delete_content, member.getName()));
        CommonDialog dialog = builder.build();
        dialog.show(getSupportFragmentManager(), "del_dialog");
    }

    /**
     * 初始化ViewModel
     */
    private void initViewModel() {
        groupManagementViewModel = ViewModelProviders.of(this, new GroupManagementViewModel.Factory(groupId, getApplication())).get(GroupManagementViewModel.class);
        // 群主
        groupManagementViewModel.getGroupOwner().observe(this, new Observer<GroupMember>() {
            @Override
            public void onChanged(GroupMember groupMember) {
                groupOwnerUiv.setName(groupMember.getName());
                ImageLoaderUtils.displayUserPortraitImage(groupMember.getPortraitUri(), groupOwnerUiv.getHeaderImageView());
            }
        });

        // 群管理
        groupManagementViewModel.getGroupManagements().observe(this, new Observer<Resource<List<GroupMember>>>() {
            @Override
            public void onChanged(Resource<List<GroupMember>> resource) {

                managementNumber = resource.data == null? 0 : resource.data.size();

                // 减掉1 ， 因为有一条是添加管理员的item
                managementsValueTv.setText(getString(R.string.seal_select_group_member) + "(" + managementNumber +"/" + MANAGEMENT_MAX +")");
                List<GroupMember> groupMembers = resource.data;
                if (groupMembers == null) {
                    groupMembers = new ArrayList<>();
                }

                if (groupMembers.size() < 5) {
                    GroupMember member = new GroupMember();
                    member.setUserId("-1");
                    member.setName(getApplication().getResources().getString(R.string.seal_group_management_add_group_managements));
                    groupMembers.add(member);
                }
                groupManagementAdapter.updateList(groupMembers);



            }
        });
        // 删除管理员
        groupManagementViewModel.getRemoveManagerResult().observe(this, new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> resource) {
                // 提醒
                if (resource.status == Status.SUCCESS) {
                    showToast(R.string.seal_group_manager_toast_remove_manager_success);
                } else if (resource.status == Status.ERROR){
                    showToast(R.string.seal_group_manager_toast_remove_manager_failed);
                } else {

                }
            }
        });

    }


    /**
     * 删除管理员
     * @param member
     */
    private void deleteManagement(GroupMember member) {
        if (groupManagementViewModel != null) {
            groupManagementViewModel.deleteManagement(member);
        }
    }


}
