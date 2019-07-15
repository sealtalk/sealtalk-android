package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.Constant;
import cn.rongcloud.im.common.ErrorCode;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.model.GroupMember;
import cn.rongcloud.im.model.GroupNoticeResult;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.model.qrcode.QrCodeDisplayType;
import cn.rongcloud.im.ui.adapter.GridGroupMemberAdapter;
import cn.rongcloud.im.ui.dialog.CommonDialog;
import cn.rongcloud.im.ui.dialog.GroupNoticeDialog;
import cn.rongcloud.im.ui.dialog.LoadingDialog;
import cn.rongcloud.im.ui.dialog.SelectPictureBottomDialog;
import cn.rongcloud.im.ui.dialog.SimpleInputDialog;
import cn.rongcloud.im.ui.view.SealTitleBar;
import cn.rongcloud.im.ui.view.SettingItemView;
import cn.rongcloud.im.ui.view.UserInfoItemView;
import cn.rongcloud.im.ui.widget.WrapHeightGridView;
import cn.rongcloud.im.utils.ImageLoaderUtils;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.viewmodel.GroupDetailViewModel;
import cn.rongcloud.im.utils.log.SLog;
import io.rong.imkit.emoticon.AndroidEmoji;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imkit.utilities.PromptPopupDialog;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Group;

/**
 * 群组详细界面
 */
public class GroupDetailActivity extends TitleBaseActivity implements View.OnClickListener {
    private final String TAG = "GroupDetailActivity";
    /**
     * 跳转界面请求添加群组成员
     */
    private final int REQUEST_ADD_GROUP_MEMBER = 1000;
    /**
     * 跳转界面请求移除群组成员
     */
    private final int REQUEST_REMOVE_GROUP_MEMBER = 1001;

    /**
     * 最大显示成员数
     */
    private final int SHOW_GROUP_MEMBER_LIMIT = 30;

    private SealTitleBar titleBar;
    private WrapHeightGridView groupMemberGv;

    private Button quitGroupBtn;
    private LoadingDialog loadingDialog;

    private String groupId;
    private Conversation.ConversationType conversationType;
    private GroupDetailViewModel groupDetailViewModel;
    private GridGroupMemberAdapter memberAdapter;
    private String groupName;
    private String grouportraitUrl;
    private UserInfoItemView groupPortraitUiv;
    private SettingItemView allGroupMemberSiv;
    private SettingItemView groupNameSiv;
    private SettingItemView notifyNoticeSiv;
    private SettingItemView onTopSiv;
    private SettingItemView isToContactSiv;
    private SettingItemView groupManagerSiv;
    private SettingItemView groupNoticeSiv;

    private String lastGroupNoticeContent;
    private long lastGroupNoticeTime;
    private String groupCreatorId;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        titleBar = getTitleBar();
        titleBar.setTitle(R.string.profile_group_info);

        setContentView(R.layout.profile_activity_group_detail);

        Intent intent = getIntent();
        if (intent == null) {
            SLog.e(TAG, "intent is null, finish " + TAG);
            return;
        }

        conversationType = (Conversation.ConversationType) intent.getSerializableExtra(IntentExtra.SERIA_CONVERSATION_TYPE);
        groupId = intent.getStringExtra(IntentExtra.STR_TARGET_ID);
        if (groupId == null || conversationType == null) {
            SLog.e(TAG, "targetId or conversationType is null, finish" + TAG);
            return;
        }

        initView();
        initViewModel();
    }

    // 初始化布局
    private void initView() {
        // 群组成员网格
        groupMemberGv = findViewById(R.id.profile_gv_group_member);
        memberAdapter = new GridGroupMemberAdapter(this, SHOW_GROUP_MEMBER_LIMIT);
        memberAdapter.setAllowAddMember(true);
        groupMemberGv.setAdapter(memberAdapter);
        memberAdapter.setOnItemClickedListener(new GridGroupMemberAdapter.OnItemClickedListener() {
            @Override
            public void onAddOrDeleteMemberClicked(boolean isAdd) {
                toMemberManage(isAdd);
            }

            @Override
            public void onMemberClicked(GroupMember groupMember) {
                showMemberInfo(groupMember);
            }
        });

        // 全部群成员
        allGroupMemberSiv = findViewById(R.id.profile_siv_all_group_member);
        allGroupMemberSiv.setOnClickListener(this);

        // 查询历史消息
        findViewById(R.id.profile_siv_group_search_history_message).setOnClickListener(this);
        // 群头像
        groupPortraitUiv = findViewById(R.id.profile_uiv_group_portrait_container);
        groupPortraitUiv.setOnClickListener(this);
        // 群名称
        groupNameSiv = findViewById(R.id.profile_siv_group_name_container);
        groupNameSiv.setOnClickListener(this);
        // 群二维码
        findViewById(R.id.profile_siv_group_qrcode).setOnClickListener(this);
        // 群公告
        groupNoticeSiv = findViewById(R.id.profile_siv_group_notice);
        groupNoticeSiv.setOnClickListener(this);

        groupManagerSiv = findViewById(R.id.profile_siv_group_manager);


        // 消息免打扰
        notifyNoticeSiv = findViewById(R.id.profile_siv_message_notice);
        notifyNoticeSiv.setSwitchCheckListener((buttonView, isChecked) ->
                groupDetailViewModel.setIsNotifyConversation(!isChecked));

        // 会话置顶
        onTopSiv = findViewById(R.id.profile_siv_group_on_top);
        onTopSiv.setSwitchCheckListener((buttonView, isChecked) ->
                groupDetailViewModel.setConversationOnTop(isChecked));

        // 保存到通讯录
        isToContactSiv = findViewById(R.id.profile_siv_group_save_to_contact);
        isToContactSiv.setSwitchCheckListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    groupDetailViewModel.saveToContact();
                } else {
                    groupDetailViewModel.removeFromContact();
                }
            }
        });

        // 消息删除
        findViewById(R.id.profile_siv_group_clean_message).setOnClickListener(this);

        // 退出群组
        quitGroupBtn = findViewById(R.id.profile_btn_group_quit);
        quitGroupBtn.setOnClickListener(this);


        groupManagerSiv.setOnClickListener(this);
    }

    private void initViewModel() {
        groupDetailViewModel = ViewModelProviders.of(this,
                new GroupDetailViewModel.Factory(this.getApplication(), groupId, conversationType))
                .get(GroupDetailViewModel.class);

        // 获取自己的信息
        groupDetailViewModel.getMyselfInfo().observe(this, new Observer<GroupMember>() {
            @Override
            public void onChanged(GroupMember member) {
                // 更具身份去控制对应的操作
                if (member.getMemberRole() == GroupMember.Role.GROUP_OWNER) {
                    quitGroupBtn.setText(R.string.profile_dismiss_group);
                    // 根据是否是群组判断是否可以选择删除成员
                    memberAdapter.setAllowDeleteMember(true);
                    groupManagerSiv.setVisibility(View.VISIBLE);
                } else if (member.getMemberRole() == GroupMember.Role.MANAGEMENT) {
                    groupPortraitUiv.setClickable(false);
                    groupNameSiv.setClickable(false);
                    quitGroupBtn.setText(R.string.profile_quit_group);
                    memberAdapter.setAllowDeleteMember(true);
                    groupManagerSiv.setVisibility(View.GONE);
                } else {
                    groupPortraitUiv.setClickable(false);
                    groupNameSiv.setClickable(false);
                    quitGroupBtn.setText(R.string.profile_quit_group);
                    memberAdapter.setAllowDeleteMember(false);
                    groupManagerSiv.setVisibility(View.GONE);
                }
            }
        });

        // 获取群组信息
        groupDetailViewModel.getGroupInfo().observe(this, resource -> {
            if (resource.data != null) {
                updateGroupInfoView(resource.data);
            }

            if (resource.status == Status.ERROR) {
                ToastUtils.showErrorToast(resource.code);
            }

            if(resource.status == Status.SUCCESS && resource.data == null){
                backToMain();
            }
        });

        // 获取群组成员信息
        groupDetailViewModel.getGroupMemberList().observe(this, resource -> {
            if (resource.data != null && resource.data.size() > 0) {
                updateGroupMemberList(resource.data);
            }

            if (resource.status == Status.ERROR) {
                ToastUtils.showErrorToast(resource.code);
            }

            if(resource.status == Status.SUCCESS && resource.data == null){
                backToMain();
            }
        });

        // 获取是否通知消息状态
        groupDetailViewModel.getIsNotify().observe(this, resource -> {
            if (resource.data != null) {
                if (resource.status == Status.SUCCESS) {
                    notifyNoticeSiv.setChecked(!resource.data);
                } else {
                    notifyNoticeSiv.setCheckedImmediately(!resource.data);
                }
            }

            if (resource.status == Status.ERROR) {
                if (resource.data != null) {
                    ToastUtils.showToast(R.string.common_set_failed);
                } else {
                    // do nothing
                }
            }
        });

        // 获取是否消息置顶状态
        groupDetailViewModel.getIsTop().observe(this, resource -> {
            if (resource.data != null) {
                if (resource.status == Status.SUCCESS) {
                    onTopSiv.setChecked(resource.data);
                } else {
                    onTopSiv.setCheckedImmediately(resource.data);
                }
            }

            if (resource.status == Status.ERROR) {
                if (resource.data != null) {
                    ToastUtils.showToast(R.string.common_set_failed);
                } else {
                    // do nothing
                }
            }
        });

        // 获取清除历史消息结果
        groupDetailViewModel.getCleanHistoryMessageResult().observe(this, resource -> {
            if (resource.status == Status.SUCCESS) {
                ToastUtils.showToast(R.string.common_clear_success);
            } else if (resource.status == Status.ERROR) {
                ToastUtils.showToast(R.string.common_clear_failure);
            }
        });

        // 获取上传群头像结果
        groupDetailViewModel.getUploadPortraitResult().observe(this, resource -> {
            if (resource.status == Status.LOADING) {
                if (loadingDialog != null) {
                    loadingDialog = new LoadingDialog();
                    loadingDialog.show(getSupportFragmentManager(), null);
                }
            } else {
                if (loadingDialog != null) {
                    loadingDialog.dismiss();
                    loadingDialog = null;
                }
            }

            if (resource.status == Status.ERROR) {
                ToastUtils.showToast(R.string.profile_upload_portrait_failed);
            }
        });

        // 添加群组成员结果
        groupDetailViewModel.getAddGroupMemberResult().observe(this, new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> resource) {
                if (resource.status == Status.ERROR) {
                    ToastUtils.showToast(resource.message);
                }
            }
        });

        // 移除群组成员结果
        groupDetailViewModel.getRemoveGroupMemberResult().observe(this, new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> resource) {
                if (resource.status == Status.ERROR) {
                    ToastUtils.showToast(resource.message);
                }
            }
        });

        // 修改群组名称结果
        groupDetailViewModel.getRenameGroupResult().observe(this, new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> resource) {
                if (resource.status == Status.ERROR) {
                    ToastUtils.showToast(resource.message);
                } else if(resource.status == Status.SUCCESS){
                    // 考虑到群名称修改时，默认的群头像也要更新，所以直接刷新群信息
                    groupDetailViewModel.refreshGroupInfo();
                }
            }
        });

        // 退出群组结果
        groupDetailViewModel.getExitGroupResult().observe(this, new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> resource) {
                if (resource.status == Status.SUCCESS) {
                    backToMain();
                } else if (resource.status == Status.ERROR) {
                    ToastUtils.showToast(resource.message);
                }
            }
        });

        // 保存到通讯录结果
        groupDetailViewModel.getSaveToContact().observe(this, new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> resource) {
                if (resource.status == Status.SUCCESS) {
                    ToastUtils.showToast(R.string.common_add_successful);
                } else if (resource.status == Status.ERROR) {
                    ToastUtils.showErrorToast(resource.code);
                }
            }
        });

        // 从通讯录中移除的结果
        groupDetailViewModel.getRemoveFromContactResult().observe(this, new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> resource) {
                if (resource.status == Status.SUCCESS) {
                    ToastUtils.showToast(R.string.common_remove_successful);
                } else if (resource.status == Status.ERROR) {
                    ToastUtils.showErrorToast(resource.code);
                }
            }
        });

        // 获取群公告
        groupDetailViewModel.getGroupNoticeResult().observe(this, new Observer<Resource<GroupNoticeResult>>() {
            @Override
            public void onChanged(Resource<GroupNoticeResult> resource) {
                if(resource.status == Status.SUCCESS){
                    GroupNoticeResult lastGroupNotice = resource.data;
                    if(lastGroupNotice != null){
                        lastGroupNoticeContent = lastGroupNotice.getContent();
                        lastGroupNoticeTime = lastGroupNotice.getTimestamp();
                    }
                }
            }
        });
    }

    /**
     * 是否是群主
     * @return
     */
    private boolean isGroupOwner() {
        if (groupDetailViewModel != null) {
            GroupMember selfGroupInfo = groupDetailViewModel.getMyselfInfo().getValue();
            if(selfGroupInfo != null){
                return selfGroupInfo.getMemberRole() == GroupMember.Role.GROUP_OWNER;
            } else {
                return false;
            }
        }

        return false;
    }

    /**
     * 是否是群管理员
     *
     * @return
     */
    private boolean isGroupManager() {
        if(groupDetailViewModel != null){
            GroupMember selfGroupInfo = groupDetailViewModel.getMyselfInfo().getValue();
            if(selfGroupInfo != null){
                return selfGroupInfo.getMemberRole() == GroupMember.Role.MANAGEMENT;
            }
        }
        return false;
    }

    /**
     * 更新群信息
     *
     * @param groupInfo
     */
    private void updateGroupInfoView(GroupEntity groupInfo) {
        // 标题
        String title = getString(R.string.profile_group_info) + "(" + groupInfo.getMemberCount() + ")";
        titleBar.setTitle(title);

        // 群成员数量
        String allMemberTxt = getString(R.string.profile_all_group_member) + "(" + groupInfo.getMemberCount() + ")";
        allGroupMemberSiv.setContent(allMemberTxt);
        // 显示群组头像
        grouportraitUrl = groupInfo.getPortraitUri();
        ImageLoaderUtils.displayGroupPortraitImage(groupInfo.getPortraitUri(), groupPortraitUiv.getHeaderImageView());
        // 群名称
        groupName = groupInfo.getName();
        groupNameSiv.setValue(groupInfo.getName());

        // 是否在通讯录中
        int isInContact = groupInfo.getIsInContact();
        if (isInContact == 0) {
            isToContactSiv.setCheckedImmediately(false);
        } else {
            isToContactSiv.setCheckedImmediately(true);
        }

        groupCreatorId = groupInfo.getCreatorId();

        //TODO 目前借口群组中返回的群公告信息为空
        // 最后群组通知消息
        //lastGroupNoticeContent = groupInfo.getBulletin();
        //lastGroupNoticeTime = groupInfo.getBulletinTime();
    }

    /**
     * 更新群成员列表
     *
     * @param groupMemberList
     */
    private void updateGroupMemberList(List<GroupMember> groupMemberList) {
        memberAdapter.updateListView(groupMemberList);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profile_siv_all_group_member:
                showAllGroupMember();
                break;
            case R.id.profile_siv_group_search_history_message:
                showSearchHistoryMessage();
                break;
            case R.id.profile_uiv_group_portrait_container:
                setGroupPortrait();
                break;
            case R.id.profile_siv_group_name_container:
                editGroupName();
                break;
            case R.id.profile_siv_group_qrcode:
                showGroupQrCode();
                break;
            case R.id.profile_siv_group_notice:
                showGroupNotice();
                break;
            case R.id.profile_siv_group_clean_message:
                showCleanMessageDialog();
                break;
            case R.id.profile_btn_group_quit:
                quitOrDeleteGroup();
                break;
            case R.id.profile_siv_group_manager:
                Intent intent = new Intent(this, GroupManagerActivity.class);
                intent.putExtra(IntentExtra.GROUP_ID, groupId);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    /**
     * 显示成员信息
     *
     * @param groupMember
     */
    private void showMemberInfo(GroupMember groupMember) {
        Intent intent = new Intent(this, UserDetailActivity.class);
        intent.putExtra(IntentExtra.STR_TARGET_ID, groupMember.getUserId());
        Group groupInfo = RongUserInfoManager.getInstance().getGroupInfo(groupId);
        if (groupInfo != null) {
            intent.putExtra(IntentExtra.STR_GROUP_NAME, groupInfo.getName());
        }
        startActivity(intent);
    }

    /**
     * 显示管理成员界面
     *
     * @param isAdd
     */
    private void toMemberManage(boolean isAdd) {
        if (isAdd) {
            Intent intent = new Intent(this, SelectFriendExcluedGroupActivity.class);
            intent.putExtra(IntentExtra.STR_TARGET_ID, groupId);
            startActivityForResult(intent, REQUEST_ADD_GROUP_MEMBER);
        } else {
            Intent intent = new Intent(this, SelectGroupMemberActivity.class);
            intent.putExtra(IntentExtra.STR_TARGET_ID, groupId);
            ArrayList<String> excludeList = new ArrayList<>();  // 不可选择的成员 id 列表
            String currentId = IMManager.getInstance().getCurrentId();
            excludeList.add(currentId);

            // 判断自己是否为群主，若非群主则添加群主至不可选择列表
            if(groupCreatorId != null && !currentId.equals(groupCreatorId)){
                excludeList.add(groupCreatorId);
            }

            intent.putExtra(IntentExtra.LIST_EXCLUDE_ID_LIST, excludeList);
            startActivityForResult(intent, REQUEST_REMOVE_GROUP_MEMBER);
        }
    }

    /**
     * 显示全部群组成员
     */
    public void showAllGroupMember() {
        Intent intent = new Intent(this, GroupMemberListActivity.class);
        intent.putExtra(IntentExtra.STR_TARGET_ID, groupId);
        startActivity(intent);
    }

    /**
     * 显示搜索历史消息
     */
    public void showSearchHistoryMessage() {
        Intent intent = new Intent(this, SearchHistoryMessageActivity.class);
        intent.putExtra(IntentExtra.STR_TARGET_ID, groupId);
        intent.putExtra(IntentExtra.SERIA_CONVERSATION_TYPE, Conversation.ConversationType.GROUP);
        intent.putExtra(IntentExtra.STR_CHAT_NAME, groupName);
        intent.putExtra(IntentExtra.STR_CHAT_PORTRAIT, grouportraitUrl);
        startActivity(intent);
    }

    /**
     * 显示群二维码
     */
    private void showGroupQrCode() {
        Intent intent = new Intent(this, QrCodeDisplayActivity.class);
        intent.putExtra(IntentExtra.STR_TARGET_ID, groupId);
        intent.putExtra(IntentExtra.START_FROM_ID, IMManager.getInstance().getCurrentId());
        intent.putExtra(IntentExtra.SERIA_QRCODE_DISPLAY_TYPE, QrCodeDisplayType.GROUP);
        startActivity(intent);
    }

    /**
     * 编辑群名称
     */
    private void editGroupName() {

        SimpleInputDialog dialog = new SimpleInputDialog();
        dialog.setInputHint(getString(R.string.profile_hint_new_group_name));
        dialog.setInputDialogListener(new SimpleInputDialog.InputDialogListener() {
            @Override
            public boolean onConfirmClicked(EditText input) {
                String name = input.getText().toString();

                if (name.length() < Constant.GROUP_NAME_MIN_LENGTH || name.length() > Constant.GROUP_NAME_MAX_LENGTH) {
                    ToastUtils.showToast(getString(R.string.profile_group_name_word_limit_format, Constant.GROUP_NAME_MIN_LENGTH, Constant.GROUP_NAME_MAX_LENGTH));
                    return true;
                }

                if (AndroidEmoji.isEmoji(name) && name.length() < Constant.GROUP_NAME_EMOJI_MIN_LENGTH) {
                    ToastUtils.showToast(getString(R.string.profile_group_name_emoji_too_short));
                    return true;
                }

                // 重命名群名称
                groupDetailViewModel.renameGroupName(name);
                return true;
            }
        });
        dialog.show(getSupportFragmentManager(), null);
    }

    /**
     * 编辑群组头像
     */
    private void setGroupPortrait() {

        SelectPictureBottomDialog.Builder builder = new SelectPictureBottomDialog.Builder();
        builder.setOnSelectPictureListener(new SelectPictureBottomDialog.OnSelectPictureListener() {
            @Override
            public void onSelectPicture(Uri uri) {
                SLog.d(TAG, "select picture, uri:" + uri);
                groupDetailViewModel.setGroupPortrait(uri);
            }
        });
        SelectPictureBottomDialog dialog = builder.build();
        dialog.show(getSupportFragmentManager(), null);
    }

    /**
     * 编辑群公告
     */
    private void showGroupNotice() {
        // 判断是否是群组或管理员
        if(isGroupOwner() || isGroupManager()){
            Intent intent = new Intent(this, GroupNoticeActivity.class);
            intent.putExtra(IntentExtra.STR_TARGET_ID, groupId);
            intent.putExtra(IntentExtra.SERIA_CONVERSATION_TYPE, Conversation.ConversationType.GROUP);
            startActivity(intent);
        } else {
            GroupNoticeDialog commonDialog = new GroupNoticeDialog();
            commonDialog.setNoticeContent(lastGroupNoticeContent);
            commonDialog.setNoticeUpdateTime(lastGroupNoticeTime);
            commonDialog.show(getSupportFragmentManager(), null);
        }
    }

    /**
     * 退出或删除群组
     */
    private void quitOrDeleteGroup() {
        CommonDialog.Builder builder = new CommonDialog.Builder();
        // 根据是否群组显示不同的提示
        if (isGroupOwner()) {
            builder.setContentMessage(getString(R.string.profile_confirm_dismiss_group));
        } else {
            builder.setContentMessage(getString(R.string.profile_confirm_quit_group));
        }
        builder.setDialogButtonClickListener(new CommonDialog.OnDialogButtonClickListener() {
            @Override
            public void onPositiveClick(View v, Bundle bundle) {
                // 根据是否是群组，选择解散还是退出群组
                if (isGroupOwner()) {
                    groupDetailViewModel.dismissGroup();
                } else {
                    groupDetailViewModel.exitGroup();
                }
            }

            @Override
            public void onNegativeClick(View v, Bundle bundle) {
            }
        });
        builder.build().show(getSupportFragmentManager(), null);
    }

    /**
     * 显示清除聊天消息对话框
     */
    private void showCleanMessageDialog() {
        PromptPopupDialog.newInstance(this,
                getString(R.string.profile_clean_group_chat_history)).setLayoutRes(io.rong.imkit.R.layout.rc_dialog_popup_prompt_warning)
                .setPromptButtonClickedListener(() -> {
                    groupDetailViewModel.cleanHistoryMessage();
                }).show();
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

        if (data == null) return;

        if (requestCode == REQUEST_ADD_GROUP_MEMBER && resultCode == RESULT_OK) {
            // 添加群组成员
            List<String> addMemberList = data.getStringArrayListExtra(IntentExtra.LIST_STR_ID_LIST);
            SLog.i(TAG, "addMemberList.size(): " + addMemberList.size());
            groupDetailViewModel.addGroupMember(addMemberList);
        } else if (requestCode == REQUEST_REMOVE_GROUP_MEMBER && resultCode == RESULT_OK) {
            // 删除群组成员
            List<String> removeMemberList = data.getStringArrayListExtra(IntentExtra.LIST_STR_ID_LIST);
            SLog.i(TAG, "removeMemberList.size(): " + removeMemberList.size());
            groupDetailViewModel.removeGroupMember(removeMemberList);
        }
    }
}
