package cn.rongcloud.im.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.model.GroupMember;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.model.UltraChannelInfo;
import cn.rongcloud.im.model.UltraGroupMemberListResult;
import cn.rongcloud.im.ui.adapter.GridGroupMemberAdapter;
import cn.rongcloud.im.ui.dialog.CommonDialog;
import cn.rongcloud.im.ui.view.SealTitleBar;
import cn.rongcloud.im.ui.view.SettingItemView;
import cn.rongcloud.im.ui.widget.WrapHeightGridView;
import cn.rongcloud.im.ultraGroup.UltraGroupManager;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.utils.log.SLog;
import cn.rongcloud.im.viewmodel.UltraGroupViewModel;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.ConversationIdentifier;
import io.rong.imlib.model.Group;
import java.util.ArrayList;
import java.util.List;

public class UltraSettingActivity extends TitleBaseActivity
        implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private ConversationIdentifier conversationIdentifier;
    private UltraGroupViewModel groupDetailViewModel;
    private GridGroupMemberAdapter memberAdapter;

    private SharedPreferences sharedPreferences;
    private static final int SHOW_GROUP_MEMBER_LIMIT = 100;

    private SettingItemView groupNameSiv;
    private SettingItemView channelNameSiv;
    private SettingItemView userGroupSiv;
    private CheckBox mCbPrivateChannel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SealTitleBar titleBar = getTitleBar();
        titleBar.setTitle(R.string.profile_group_info);

        setContentView(R.layout.profile_activity_ultra_group_detail);
        sharedPreferences = getSharedPreferences("ultra", Context.MODE_PRIVATE);
        Intent intent = getIntent();
        String TAG = "UltraSettingActivity";
        if (intent == null) {
            SLog.e(TAG, "intent is null, finish " + TAG);
            return;
        }

        conversationIdentifier = initConversationIdentifier();
        if (conversationIdentifier.isValid()) {
            SLog.e(TAG, "targetId or conversationType is null, finish" + TAG);
            return;
        }

        initView();
        initViewModel();
    }

    // 初始化布局
    private void initView() {
        // 群组成员网格
        WrapHeightGridView groupMemberGv = findViewById(R.id.profile_gv_group_member);
        memberAdapter = new GridGroupMemberAdapter(this, SHOW_GROUP_MEMBER_LIMIT);
        memberAdapter.setAllowAddMember(false);
        groupMemberGv.setAdapter(memberAdapter);

        // 群名称
        groupNameSiv = findViewById(R.id.profile_ultra_siv_group_name_container);
        channelNameSiv = findViewById(R.id.profile_ultra_siv_channel_name_container);
        userGroupSiv = findViewById(R.id.profile_ultra_siv_user_group_container);
        mCbPrivateChannel = findViewById(R.id.cb_private_channel);
        View siv_channel_members = findViewById(R.id.siv_channel_members);
        siv_channel_members.setOnClickListener(this);
        findViewById(R.id.siv_channel_add_members).setOnClickListener(this);
        userGroupSiv.setOnClickListener(this);
        // groupNameSiv.setOnClickListener(this);

        // 退出群组
        Button quitGroupBtn = findViewById(R.id.profile_btn_group_quit);
        Button deleteChannel = findViewById(R.id.btn_group_channel_delete);
        quitGroupBtn.setOnClickListener(this);
        deleteChannel.setOnClickListener(this);

        UltraChannelInfo ultraChannelInfo =
                UltraGroupManager.getInstance()
                        .getUltraChannelInfo(
                                conversationIdentifier.getTargetId(),
                                conversationIdentifier.getChannelId());
        if (ultraChannelInfo != null) {
            mCbPrivateChannel.setEnabled(true);
            mCbPrivateChannel.setChecked(ultraChannelInfo.getType() == 1);
            mCbPrivateChannel.setOnCheckedChangeListener(this);
            quitGroupBtn.setVisibility(View.GONE);
            deleteChannel.setVisibility(View.VISIBLE);
        } else {
            mCbPrivateChannel.setEnabled(false);
            quitGroupBtn.setVisibility(View.VISIBLE);
            deleteChannel.setVisibility(View.GONE);
        }

        if (isGroupOwner()) {
            quitGroupBtn.setText(R.string.profile_dismiss_group);
        } else {
            quitGroupBtn.setText(R.string.profile_quit_group);
        }

        String name = sharedPreferences.getString("name", "");
        groupNameSiv.setValue(name);

        if (!TextUtils.isEmpty(conversationIdentifier.getChannelId())) {
            Group group =
                    RongUserInfoManager.getInstance()
                            .getGroupInfo(
                                    conversationIdentifier.getTargetId()
                                            + conversationIdentifier.getChannelId());
            if (group != null && !TextUtils.isEmpty(group.getName())) {
                channelNameSiv.setVisibility(View.VISIBLE);
                channelNameSiv.setValue(group.getName());
            }
        }
    }

    private void initViewModel() {
        groupDetailViewModel = ViewModelProviders.of(this).get(UltraGroupViewModel.class);

        groupDetailViewModel.getUltraGroupMemberInfoList(
                conversationIdentifier.getTargetId(), 1, SHOW_GROUP_MEMBER_LIMIT);
        // 获取群组成员信息
        groupDetailViewModel
                .getUltraGroupMemberInfoListResult()
                .observe(
                        this,
                        resource -> {
                            if (resource.status == Status.LOADING) {
                                return;
                            }
                            if (resource.data != null && resource.data.size() > 0) {
                                List<GroupMember> groupMemberArrayList = new ArrayList<>();
                                List<UltraGroupMemberListResult> ultraGroupMemberListResults =
                                        resource.data;
                                for (UltraGroupMemberListResult ultraGroupMemberListResult :
                                        ultraGroupMemberListResults) {
                                    GroupMember groupMember = new GroupMember();
                                    groupMember.setName(ultraGroupMemberListResult.user.nickname);
                                    groupMember.setPortraitUri(
                                            ultraGroupMemberListResult.user.portraitUri);
                                    groupMemberArrayList.add(groupMember);
                                }
                                updateGroupMemberList(groupMemberArrayList);
                            }

                            if (resource.status == Status.ERROR) {
                                ToastUtils.showErrorToast(resource.code);
                            }

                            if (resource.status == Status.SUCCESS && resource.data == null) {
                                backToMain();
                            }
                        });

        // 解散超级群组
        groupDetailViewModel
                .getDismissGroupResult()
                .observe(
                        this,
                        voidResource -> {
                            if (voidResource.status == Status.SUCCESS) {
                                sharedPreferences.edit().clear().commit();
                                backToMain();
                            } else if (voidResource.status == Status.ERROR) {
                                ToastUtils.showToast(voidResource.message);
                            }
                        });

        // 退出群组结果
        groupDetailViewModel
                .getExitGroupResult()
                .observe(
                        this,
                        resource -> {
                            if (resource.status == Status.SUCCESS) {
                                sharedPreferences.edit().clear().commit();
                                backToMain();
                            } else if (resource.status == Status.ERROR) {
                                ToastUtils.showToast(resource.message);
                            }
                        });
    }

    /** 是否是群主 */
    private boolean isGroupOwner() {
        String creatorId = sharedPreferences.getString("creatorId", "");
        if (!TextUtils.isEmpty(creatorId)) {
            return creatorId.equals(RongIMClient.getInstance().getCurrentUserId());
        } else {
            return false;
        }
    }

    /** 更新群成员列表 */
    private void updateGroupMemberList(List<GroupMember> groupMemberList) {
        memberAdapter.updateListView(groupMemberList);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.profile_btn_group_quit) {
            quitOrDeleteGroup();
        } else if (v.getId() == R.id.siv_channel_members) {
            Intent intent = new Intent(this, UltraGroupChannelMemberSetting.class);
            intent.putExtra(IntentExtra.SERIA_CONVERSATION_IDENTIFIER, conversationIdentifier);
            startActivity(intent);
        } else if (v.getId() == R.id.siv_channel_add_members) {
            Intent intent = new Intent(this, UltraGroupChannelAddMemberActivity.class);
            intent.putExtra(IntentExtra.SERIA_CONVERSATION_IDENTIFIER, conversationIdentifier);
            startActivity(intent);
        } else if (v.getId() == R.id.btn_group_channel_delete) {
            deleteChannel();
        } else if (v.getId() == R.id.profile_ultra_siv_user_group_container) {
            String title =
                    channelNameSiv.getValue().isEmpty()
                            ? "(超级群)" + groupNameSiv.getValue()
                            : "(频道)" + channelNameSiv.getValue();
            if (conversationIdentifier.getChannelId().isEmpty()) {
                UserGroupListActivity.start(this, conversationIdentifier, title);
            } else {
                UserGroupChannelListActivity.start(this, conversationIdentifier, title);
            }
        }
    }

    private void deleteChannel() {
        CommonDialog.Builder builder = new CommonDialog.Builder();
        // 根据是否群组显示不同的提示

        builder.setContentMessage("是否要删除频道？");

        builder.setDialogButtonClickListener(
                new CommonDialog.OnDialogButtonClickListener() {
                    @Override
                    public void onPositiveClick(View v, Bundle bundle) {
                        // 根据是否是群组，选择解散还是退出群组
                        groupDetailViewModel
                                .delChannel(
                                        conversationIdentifier.getTargetId(),
                                        conversationIdentifier.getChannelId())
                                .observe(
                                        UltraSettingActivity.this,
                                        new Observer<Boolean>() {
                                            @Override
                                            public void onChanged(Boolean aBoolean) {
                                                if (aBoolean) {
                                                    ToastUtils.showToast("删除成功");
                                                    backToMain();
                                                } else {
                                                    ToastUtils.showToast("删除失败");
                                                }
                                            }
                                        });
                    }

                    @Override
                    public void onNegativeClick(View v, Bundle bundle) {}
                });
        builder.build().show(getSupportFragmentManager(), null);
    }

    /** 退出或删除群组 */
    private void quitOrDeleteGroup() {

        CommonDialog.Builder builder = new CommonDialog.Builder();
        // 根据是否群组显示不同的提示
        if (isGroupOwner()) {
            builder.setContentMessage(getString(R.string.profile_confirm_dismiss_group));
        } else {
            builder.setContentMessage(getString(R.string.profile_confirm_quit_group));
        }
        builder.setDialogButtonClickListener(
                new CommonDialog.OnDialogButtonClickListener() {
                    @Override
                    public void onPositiveClick(View v, Bundle bundle) {
                        // 根据是否是群组，选择解散还是退出群组
                        if (isGroupOwner()) {
                            groupDetailViewModel.dismissUltraGroup(
                                    conversationIdentifier.getTargetId());
                        } else {
                            groupDetailViewModel.exitUltraGroup(
                                    conversationIdentifier.getTargetId());
                        }
                    }

                    @Override
                    public void onNegativeClick(View v, Bundle bundle) {}
                });
        builder.build().show(getSupportFragmentManager(), null);
    }

    private void backToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mCbPrivateChannel.setOnCheckedChangeListener(null);
        groupDetailViewModel
                .changeChannelType(
                        conversationIdentifier.getTargetId(),
                        conversationIdentifier.getChannelId(),
                        isChecked ? 1 : 0)
                .observe(
                        this,
                        new Observer<Boolean>() {
                            @Override
                            public void onChanged(Boolean result) {
                                if (result == Boolean.TRUE) {
                                    ToastUtils.showToast("设置成功");
                                } else {
                                    mCbPrivateChannel.setChecked(!isChecked);
                                    ToastUtils.showToast("设置失败");
                                }
                                mCbPrivateChannel.setOnCheckedChangeListener(
                                        UltraSettingActivity.this);
                            }
                        });
    }
}
