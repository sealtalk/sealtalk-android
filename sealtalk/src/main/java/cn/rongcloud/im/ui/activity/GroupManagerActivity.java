package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.model.GroupMember;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.ui.dialog.CommonDialog;
import cn.rongcloud.im.ui.view.SettingItemView;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.viewmodel.GroupManagementViewModel;

public class GroupManagerActivity extends TitleBaseActivity implements View.OnClickListener {

    private static final int MANAGEMENT_MAX = 5;
    private static final int REQUEST_SET_NEW_OWNER = 1000;
    private String groupId;
    private GroupManagementViewModel groupManagementViewModel;
    private SettingItemView setGroupManagerSiv;
    private SettingItemView muteAllSiv;
    private SettingItemView addCertifiSiv;
    private SettingItemView copyGroupSiv;
    private SettingItemView groupExitedSiv;
    private SettingItemView groupMemberProtectSiv;

    private final int SWITCH_OPEN = 0;
    private final int SWITCH_CLOSE = 1;

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
        groupExitedSiv = findViewById(R.id.siv_group_exited);
        groupExitedSiv.setOnClickListener(this);
        copyGroupSiv = findViewById(R.id.siv_copy_group);
        copyGroupSiv.setOnClickListener(this);
        setGroupManagerSiv = findViewById(R.id.siv_set_group_manager);
        setGroupManagerSiv.setOnClickListener(this);
        SettingItemView transferSiv = findViewById(R.id.siv_transfer);
        transferSiv.setOnClickListener(this);
        muteAllSiv = findViewById(R.id.siv_mute_all);
        muteAllSiv.setSwitchCheckListener(muteAllSivListener);
        addCertifiSiv = findViewById(R.id.siv_add_certification);
        addCertifiSiv.setSwitchCheckListener(certifiSivListener);
        groupMemberProtectSiv = findViewById(R.id.siv_group_member_protect);
        groupMemberProtectSiv.setSwitchCheckListener(memberProtectSivListener);
    }

    private void initViewModel() {
        groupManagementViewModel = ViewModelProviders.of(this, new GroupManagementViewModel.Factory(groupId, getApplication())).get(GroupManagementViewModel.class);

        groupManagementViewModel.getGroupManagements().observe(this, new Observer<Resource<List<GroupMember>>>() {
            @Override
            public void onChanged(Resource<List<GroupMember>> resource) {
                int managementNumber = resource.data == null ? 0 : resource.data.size();
                // 减掉1 ， 因为有一条是添加管理员的item
                setGroupManagerSiv.setValue(managementNumber + "/" + MANAGEMENT_MAX);
            }
        });

        groupManagementViewModel.getGroupInfo().observe(this, new Observer<GroupEntity>() {
            @Override
            public void onChanged(GroupEntity groupEntity) {
                if (groupEntity != null) {
                    if (groupEntity.getIsMute() == 1) {
                        setCheckChangeWithoutListener(true, muteAllSiv, muteAllSivListener);
                    }
                    if (groupEntity.getCertiStatus() == SWITCH_OPEN) {
                        setCheckChangeWithoutListener(true, addCertifiSiv, certifiSivListener);
                    }
                    if (groupEntity.getMemberProtection() == 1) {
                        setCheckChangeWithoutListener(true, groupMemberProtectSiv, memberProtectSivListener);
                    }
                }
            }
        });

        groupManagementViewModel.getMuteAllResult().observe(this, new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> voidResource) {
                if (voidResource.status == Status.SUCCESS) {
                    ToastUtils.showToast(R.string.seal_group_manager_set_success);
                } else if (voidResource.status == Status.ERROR) {
                    ToastUtils.showToast(R.string.seal_group_manager_set_fail);
                }
            }
        });

        groupManagementViewModel.getCerifiResult().observe(this, new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> voidResource) {
                if (voidResource.status == Status.SUCCESS) {
                    ToastUtils.showToast(R.string.seal_group_manager_set_success);
                } else if (voidResource.status == Status.ERROR) {
                    ToastUtils.showToast(R.string.seal_group_manager_set_fail);
                }
            }
        });

        groupManagementViewModel.getMemberProtectionResult().observe(this, new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> voidResource) {
                if (voidResource.status == Status.SUCCESS) {
                    ToastUtils.showToast(R.string.seal_group_manager_set_success);
                } else if (voidResource.status == Status.ERROR) {
                    ToastUtils.showToast(R.string.seal_group_manager_set_fail);
                }
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
            case R.id.siv_copy_group:
                Intent intentCopy = new Intent(this, GroupCopyActivity.class);
                intentCopy.putExtra(IntentExtra.GROUP_ID, groupId);
                startActivity(intentCopy);
                break;
            case R.id.siv_group_exited:
                Intent intentExited = new Intent(this, GroupExitedListActivity.class);
                intentExited.putExtra(IntentExtra.GROUP_ID, groupId);
                startActivity(intentExited);
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

    private void showCertifiSivConfirmDialog() {
        CommonDialog.Builder builder = new CommonDialog.Builder();
        builder.setContentMessage(getString(R.string.seal_add_certification_close));
        builder.setDialogButtonClickListener(new CommonDialog.OnDialogButtonClickListener() {
            @Override
            public void onPositiveClick(View v, Bundle bundle) {
                groupManagementViewModel.setCerification(SWITCH_CLOSE);
            }

            @Override
            public void onNegativeClick(View v, Bundle bundle) {
                setCheckChangeWithoutListener(true, addCertifiSiv, certifiSivListener);
            }
        });
        CommonDialog dialog = builder.build();
        dialog.show(getSupportFragmentManager(), "certifi_close");
    }

    private void showMemberProtectionConfirmDialog() {
        CommonDialog.Builder builder = new CommonDialog.Builder();
        builder.setContentMessage(getString(R.string.seal_add_friend_protect));
        builder.setDialogButtonClickListener(new CommonDialog.OnDialogButtonClickListener() {
            @Override
            public void onPositiveClick(View v, Bundle bundle) {
                //1为打开，0为关闭
                groupManagementViewModel.setMemberProtection(0);
            }

            @Override
            public void onNegativeClick(View v, Bundle bundle) {
                setCheckChangeWithoutListener(true, groupMemberProtectSiv, memberProtectSivListener);
            }
        });
        CommonDialog dialog = builder.build();
        dialog.show(getSupportFragmentManager(), null);
    }

    //转变状态时防止重复发送网络请求
    private void setCheckChangeWithoutListener(boolean isChecked, SettingItemView view, CompoundButton.OnCheckedChangeListener listener) {
        view.setSwitchCheckListener(null);
        view.setCheckedImmediately(isChecked);
        view.setSwitchCheckListener(listener);
    }

    private CompoundButton.OnCheckedChangeListener memberProtectSivListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            //1为打开，0为关闭
            if (isChecked) {
                groupManagementViewModel.setMemberProtection(1);
            } else {
                showMemberProtectionConfirmDialog();
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener certifiSivListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                groupManagementViewModel.setCerification(SWITCH_OPEN);
            } else {
                showCertifiSivConfirmDialog();
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener muteAllSivListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            //1为打开，0为关闭，全员禁言
            if (isChecked) {
                groupManagementViewModel.setMuteAll(1);
            } else {
                groupManagementViewModel.setMuteAll(0);
            }
        }
    };


}
