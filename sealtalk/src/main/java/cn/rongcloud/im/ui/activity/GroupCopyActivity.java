package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.List;
import java.util.Random;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.ErrorCode;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.model.AddMemberResult;
import cn.rongcloud.im.model.CopyGroupResult;
import cn.rongcloud.im.model.GroupMember;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.ui.dialog.CommonDialog;
import cn.rongcloud.im.ui.widget.SelectableRoundedImageView;
import cn.rongcloud.im.utils.ImageLoaderUtils;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.viewmodel.CopyGroupViewModel;
import io.rong.imkit.RongIM;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.UserInfo;

public class GroupCopyActivity extends TitleBaseActivity implements View.OnClickListener {

    private final String TAG = GroupCopyActivity.class.getSimpleName();
    private String groupId;
    private SelectableRoundedImageView groupPortrait;
    private TextView tvGroupMemberNum;
    private Button btnCopy;
    private CopyGroupViewModel copyGroupViewModel;
    private String currentUserName;
    private String copyGroupName;
    private boolean isCopyNameGoing;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_copy);
        groupId = getIntent().getStringExtra(IntentExtra.GROUP_ID);
        UserInfo info = RongUserInfoManager.getInstance().getUserInfo(RongIM.getInstance().getCurrentUserId());
        if (info != null) {
            currentUserName = info.getName();
        } else {
            currentUserName = "";
        }
        initView();
        initViewModel();
    }

    private void initView() {
        getTitleBar().setTitle(getString(R.string.seal_group_manager_copy_title));
        groupPortrait = findViewById(R.id.iv_group_portrait);
        tvGroupMemberNum = findViewById(R.id.tv_group_member_num);
        btnCopy = findViewById(R.id.btn_copy);
        btnCopy.setOnClickListener(this);
    }

    private void initViewModel() {
        copyGroupViewModel = ViewModelProviders.of(this).get(CopyGroupViewModel.class);
        copyGroupViewModel.requestGroupInfo(groupId);
        copyGroupViewModel.getGroupInfo().observe(this, new Observer<Resource<GroupEntity>>() {
            @Override
            public void onChanged(Resource<GroupEntity> groupEntityResource) {
                if (groupEntityResource.status != Status.LOADING && groupEntityResource.data != null) {
                    updateGroupInfo(groupEntityResource.data);
                }
            }
        });
        copyGroupViewModel.getCopyGroupResult().observe(this, new Observer<Resource<CopyGroupResult>>() {
            @Override
            public void onChanged(Resource<CopyGroupResult> copyGroupResultResource) {
                if (copyGroupResultResource.status == Status.SUCCESS) {
                    dismissLoadingDialog();
                    Log.e("CopyGroupResult", "scuccess==");
                    isCopyNameGoing = false;
                    if (copyGroupResultResource.data != null) {
                        RongIM.getInstance().startConversation(GroupCopyActivity.this, Conversation.ConversationType.GROUP, copyGroupResultResource.data.id, copyGroupName);
                        if (copyGroupResultResource.data.userStatus != null && copyGroupResultResource.data.userStatus.size() > 0) {
                            showAddMemberTips(copyGroupResultResource.data.userStatus);
                        }
                    }
                } else if (copyGroupResultResource.status == Status.ERROR) {
                    dismissLoadingDialog();
                    isCopyNameGoing = false;
                    ToastUtils.showErrorToast(copyGroupResultResource.code);
                }
            }
        });
    }

    private void updateGroupInfo(GroupEntity groupInfo) {
        ImageLoaderUtils.displayUserPortraitImage(groupInfo.getPortraitUri(), groupPortrait);
        tvGroupMemberNum.setText(getString(R.string.common_member_count, groupInfo.getMemberCount()));
    }

    private void copyGroup() {
        showLoadingDialog("");
        if (isCopyNameGoing) {
            return;
        }
        isCopyNameGoing = true;
        currentUserName = currentUserName + ",";
        //选取当前用户名和随机一位群成员用户名，凑齐群名，不超过6位数
        if (currentUserName.length() >= 6) {
            copyGroupName = currentUserName.substring(0, 6) + "...";
            copyGroupViewModel.copyGroup(groupId, copyGroupName, "");
        } else {
            copyGroupViewModel.getGroupMemberInfoList(groupId).observe(this, new Observer<List<GroupMember>>() {
                @Override
                public void onChanged(List<GroupMember> listResource) {
                    copyGroupName = currentUserName;
                    if (listResource != null && listResource.size() > 0) {
                        copyGroupName = copyGroupName + listResource.get(new Random().nextInt(listResource.size())).getName();
                        if (copyGroupName.length() > 6) {
                            copyGroupName = copyGroupName.substring(0, 6);
                        }
                        copyGroupName = copyGroupName + "...";
                    }
                    copyGroupViewModel.copyGroup(groupId, copyGroupName, "");
                }
            });
        }
    }

    /**
     * 是否复制群提示
     */
    private synchronized void showCopyCertifiDialog() {
        CommonDialog.Builder builder = new CommonDialog.Builder();
        builder.setContentMessage(getString(R.string.seal_group_manager_copy_tips));
        builder.setDialogButtonClickListener(new CommonDialog.OnDialogButtonClickListener() {
            @Override
            public void onPositiveClick(View v, Bundle bundle) {
                copyGroup();
            }

            @Override
            public void onNegativeClick(View v, Bundle bundle) {

            }
        });
        CommonDialog certifiTipsDialog = builder.build();
        certifiTipsDialog.show(getSupportFragmentManager().beginTransaction(), "CopyCertifi");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_copy:
                showCopyCertifiDialog();
                break;
        }
    }

    /**
     * 复制群后，邀请成员入群确认提示
     *
     * @param results
     */
    private void showAddMemberTips(List<AddMemberResult> results) {
        String tips = getString(R.string.seal_add_success);
        //1 为已加入, 2 为等待管理员同意, 3 为等待被邀请者同意
        //只要有状态 3 ，就提示'已邀请，等待确认'
        for (AddMemberResult result : results) {
            if (result.status == 3) {
                tips = getString(R.string.seal_add_need_member_agree);
                break;
            } else if (result.status == 2) {
                if (!tips.equals(getString(R.string.seal_add_need_member_agree))) {
                    tips = getString(R.string.seal_add_need_manager_agree);
                }
            }
        }
        ToastUtils.showToast(tips);
    }
}
