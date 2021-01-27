package cn.rongcloud.im.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.Locale;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.db.model.FriendDescription;
import cn.rongcloud.im.db.model.FriendStatus;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.db.model.GroupMemberInfoDes;
import cn.rongcloud.im.db.model.UserInfo;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.ui.dialog.CommonDialog;
import cn.rongcloud.im.ui.dialog.OperatePhoneNumBottomDialog;
import cn.rongcloud.im.ui.dialog.SimpleInputDialog;
import cn.rongcloud.im.ui.view.SealTitleBar;
import cn.rongcloud.im.ui.view.SettingItemView;
import cn.rongcloud.im.ui.widget.SelectableRoundedImageView;
import cn.rongcloud.im.utils.ImageLoaderUtils;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.utils.log.SLog;
import cn.rongcloud.im.viewmodel.UserDetailViewModel;
import cn.rongcloud.im.viewmodel.UserInfoViewModel;
import io.rong.callkit.RongCallAction;
import io.rong.callkit.RongVoIPIntent;
import io.rong.calllib.RongCallClient;
import io.rong.calllib.RongCallCommon;
import io.rong.calllib.RongCallSession;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;

/**
 * 用户详细界面
 */
public class UserDetailActivity extends TitleBaseActivity implements View.OnClickListener {
    private final String TAG = "UserDetailActivity";

    private SealTitleBar titleBar;

    private SelectableRoundedImageView userPortraitIv;
    private TextView userDisplayNameTv;
    //2.1 版本电话号码不显示，替换为 SealTalk 账号
    private TextView phoneTv;
    private TextView userNameTv;
    private TextView groupProtectionTv;
    //    private TextView blackListTv;
    private TextView grouNickNameTv;
    private LinearLayout chatGroupLl;
    private LinearLayout friendMenuLl;
    private Button addFriendBtn;

    private SettingItemView sivDescription;
    private SettingItemView sivDesPhone;
    private SettingItemView sivGroupInfo;

    private UserDetailViewModel userDetailViewModel;
    private String userId;
    private String groupId;
    private String groupNickName;
    private UserInfo latestUserInfo;
    private UserInfo myUserInfo;
    private String fromGroupName;
    private boolean isInBlackList = false;
    /**
     * 是否在删除好友操作，如果在删除中则不更新好友状态，防止在删除时会有界面刷新
     */
    private boolean isInDeleteAction = false;
    private SettingItemView blacklistSiv;
    private SettingItemView deleteContactSiv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        titleBar = getTitleBar();
        titleBar.setTitle(R.string.profile_user_details);

        setContentView(R.layout.profile_activity_user_detail);

        Intent intent = getIntent();
        if (intent == null) {
            SLog.e(TAG, "intent is null, finish " + TAG);
            finish();
            return;
        }

        userId = intent.getStringExtra(IntentExtra.STR_TARGET_ID);
        if (userId == null) {
            SLog.e(TAG, "targetId is null, finish" + TAG);
            finish();
            return;
        }
        groupId = intent.getStringExtra(IntentExtra.GROUP_ID);
        // 如果从群中添加好友，则附带群名称
        fromGroupName = intent.getStringExtra(IntentExtra.STR_GROUP_NAME);

        initView();
        initViewModel();
    }

    private void initView() {
        // 用户信息相关内容
        userPortraitIv = findViewById(R.id.profile_iv_detail_user_portrait);
        userDisplayNameTv = findViewById(R.id.profile_tv_detail_display_name);
        phoneTv = findViewById(R.id.profile_tv_detail_phone);
        userNameTv = findViewById(R.id.profile_tv_detail_name);
        // 好友相关设置
        friendMenuLl = findViewById(R.id.profile_ll_detail_friend_menu_container);

        //设置群信息
        sivGroupInfo = findViewById(R.id.profile_siv_detail_group);
        if (!TextUtils.isEmpty(groupId)) {
            sivGroupInfo.setVisibility(View.VISIBLE);
            sivGroupInfo.setOnClickListener(this);
        }
        // 设置备注
        sivDescription = findViewById(R.id.profile_siv_detail_alias);
        sivDescription.setOnClickListener(this);
        //设置号码
        sivDesPhone = findViewById(R.id.profile_siv_detail_phone);
        sivDesPhone.setOnClickListener(this);
        // 加入，移除黑名单
        blacklistSiv = findViewById(R.id.profile_siv_detail_blacklist);
        blacklistSiv.setOnClickListener(this);
        // 删除联系人
        deleteContactSiv = findViewById(R.id.profile_siv_detail_delete_contact);
        deleteContactSiv.setOnClickListener(this);
        // 发起聊天相关
        chatGroupLl = findViewById(R.id.profile_ll_detail_chat_button_group);

        findViewById(R.id.profile_btn_detail_start_chat).setOnClickListener(this);
        findViewById(R.id.profile_btn_detail_start_voice).setOnClickListener(this);
        findViewById(R.id.profile_btn_detail_start_video).setOnClickListener(this);
        // 添加好友
        addFriendBtn = findViewById(R.id.profile_btn_detail_add_friend);
        addFriendBtn.setOnClickListener(this);

        //群昵称
        grouNickNameTv = findViewById(R.id.profile_tv_group_nick_name);

        groupProtectionTv = findViewById(R.id.tv_group_protection_tips);
    }

    private void initViewModel() {
        userDetailViewModel = ViewModelProviders.of(this, new UserDetailViewModel.Factory(getApplication(), userId)).get(UserDetailViewModel.class);

        // 获取用户状态
        userDetailViewModel.getUserInfo().observe(this, resource -> {
            if (resource.data != null
                    && !isInDeleteAction) { //在删除好友时不进行好友信息的刷新，防止删除成功时画面会刷新再关闭
                updateUserInfo(resource.data);
            }
        });

        // 获取添加好友结果
        userDetailViewModel.getInviteFriendResult().observe(this, resource -> {
            if (resource.status == Status.SUCCESS) {
                ToastUtils.showToast(R.string.common_request_success);
                finish();
            } else if (resource.status == Status.ERROR) {
                ToastUtils.showToast(resource.message);
            }
        });

        // 获取黑名单状态
        userDetailViewModel.getIsInBlackList().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isInBlackList) {
                if (!isInDeleteAction) {
                    updateBlackListItem(isInBlackList);
                }
            }
        });

        // 获取添加到黑名单结果
        userDetailViewModel.getAddBlackListResult().observe(this, new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> resource) {
                if (!isInDeleteAction) {
                    if (resource.status == Status.SUCCESS) {
                        ToastUtils.showToast(R.string.common_add_successful);
                    } else if (resource.status == Status.ERROR) {
                        ToastUtils.showToast(resource.message);
                    }
                }
            }
        });

        // 获取移除黑名单结果
        userDetailViewModel.getRemoveBlackListResult().observe(this, new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> resource) {
                if (resource.status == Status.SUCCESS) {
                    ToastUtils.showToast(R.string.common_remove_successful);
                } else if (resource.status == Status.ERROR) {
                    ToastUtils.showToast(resource.message);
                }
            }
        });

        // 获取删除好友
        userDetailViewModel.getDeleteFriendResult().observe(this, new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> resource) {
                if (resource.status == Status.SUCCESS) {
                    ToastUtils.showToast(R.string.common_delete_successful);
                    // 删除成功后关闭界面
                    finish();
                } else if (resource.status == Status.ERROR) {
                    // 删除失败时置回删除行为
                    isInDeleteAction = false;
                    ToastUtils.showToast(resource.message);
                }
            }
        });

        // 获取自己的信息
        UserInfoViewModel userInfoViewModel = ViewModelProviders.of(this).get(UserInfoViewModel.class);
        userInfoViewModel.getUserInfo().observe(this, new Observer<Resource<UserInfo>>() {
            @Override
            public void onChanged(Resource<UserInfo> resource) {
                if (resource.data != null) {
                    myUserInfo = resource.data;
                }
            }
        });

        userDetailViewModel.getFriendDescription().observe(this, new Observer<Resource<FriendDescription>>() {
            @Override
            public void onChanged(Resource<FriendDescription> friendDescriptionResource) {
                if (friendDescriptionResource.status != Status.LOADING && friendDescriptionResource.data != null) {
                    updateDescription(friendDescriptionResource.data);
                }
            }
        });
        // 如果来自群聊，获取群信息
        if (!TextUtils.isEmpty(groupId)) {
            userDetailViewModel.requestMemberInfoDes(groupId, userId);
            userDetailViewModel.getGroupMemberInfoDes().observe(this, new Observer<Resource<GroupMemberInfoDes>>() {
                @Override
                public void onChanged(Resource<GroupMemberInfoDes> groupMemberInfoDesResource) {
                    if (groupMemberInfoDesResource.status != Status.LOADING && groupMemberInfoDesResource.data != null) {
                        updateGroupInfoDes(groupMemberInfoDesResource.data);
                    }
                }
            });
            userDetailViewModel.groupInfoResult().observe(this, new Observer<GroupEntity>() {
                @Override
                public void onChanged(GroupEntity groupEntity) {
                    updateMemberProtectStatus(groupEntity);
                }
            });
        }
    }

    private void updateMemberProtectStatus(GroupEntity groupEntity) {
        if (groupEntity != null) {
            // 1 为开启了群成员保护模式
            if (groupEntity.getMemberProtection() == 1) {
                FriendStatus friendStatus = FriendStatus.getStatus(latestUserInfo.getFriendStatus());
                if (!(friendStatus == FriendStatus.IS_FRIEND
                        || friendStatus == FriendStatus.IN_BLACK_LIST)) {
                    // 隐藏好友相关内容,
//                    friendMenuLl.setVisibility(View.GONE);
                    friendMenuLl.setVisibility(View.VISIBLE);
                    sivDesPhone.setVisibility(View.GONE);
                    sivDescription.setVisibility(View.GONE);
                    blacklistSiv.setVisibility(View.GONE);
                    deleteContactSiv.setVisibility(View.GONE);
                    // 不显示添加好友
                    addFriendBtn.setVisibility(View.GONE);
                    //不显示 SealTalk 号
                    phoneTv.setVisibility(View.GONE);
                    //提示群保护(自己不显示),自己不显示个人信息
                    if (!latestUserInfo.getId().equals(RongIMClient.getInstance().getCurrentUserId())) {
                        groupProtectionTv.setVisibility(View.VISIBLE);
                    } else {
                        sivGroupInfo.setVisibility(View.GONE);
                    }
                }
            } else {
                // 自己时不显示添加好友
                friendMenuLl.setVisibility(View.VISIBLE);
                sivDesPhone.setVisibility(View.GONE);
                sivDescription.setVisibility(View.GONE);
                blacklistSiv.setVisibility(View.GONE);
                deleteContactSiv.setVisibility(View.GONE);
                //是自己的时候不显示添加好友按钮
                if (latestUserInfo.getId().equals(RongIMClient.getInstance().getCurrentUserId())) {
                    addFriendBtn.setVisibility(View.GONE);
                } else {
                    addFriendBtn.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    //更新群昵称
    private void updateGroupInfoDes(GroupMemberInfoDes data) {
        if (!TextUtils.isEmpty(data.getGroupNickname())) {
            groupNickName = data.getGroupNickname();
            grouNickNameTv.setText(getString(R.string.seal_mine_my_account_group_nick_name) + groupNickName);
            grouNickNameTv.setVisibility(View.VISIBLE);
        } else {
            grouNickNameTv.setVisibility(View.GONE);
        }
    }

    private void updateDescription(FriendDescription data) {
        if (!TextUtils.isEmpty(data.getDescription())) {
            sivDesPhone.setVisibility(View.VISIBLE);
            sivDescription.setContent(R.string.profile_set_display_des);
            sivDescription.setValue(data.getDescription());
            sivDescription.getValueView().setSingleLine();
            sivDescription.getValueView().setMaxEms(10);
            sivDescription.getValueView().setEllipsize(TextUtils.TruncateAt.END);
        } else {
            // 同时为空显示'设置备注和描述'
            if (TextUtils.isEmpty(data.getPhone())) {
                sivDescription.setContent(R.string.profile_set_display_name);
            } else {
                sivDescription.setContent(R.string.profile_set_display_des);
            }
            sivDescription.setValue("");
        }
        if (!TextUtils.isEmpty(data.getPhone())) {
            sivDesPhone.setVisibility(View.VISIBLE);
            sivDesPhone.setValue(data.getPhone());
            sivDesPhone.getValueView().setTextColor(getResources().getColor(R.color.color_blue_9F));
            sivDesPhone.getValueView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showOperatePhoneNumDialog();
                }
            });
        } else {
            //描述和电话好吗同时为空的时候才消失
            if (TextUtils.isEmpty(data.getDescription())) {
                sivDesPhone.setVisibility(View.GONE);
            }
            sivDesPhone.setValue("");
        }
    }

    /**
     * 更新用户信息
     *
     * @param userInfo
     */
    private void updateUserInfo(UserInfo userInfo) {
        latestUserInfo = userInfo;

        FriendStatus friendStatus = FriendStatus.getStatus(userInfo.getFriendStatus());
        if (friendStatus == FriendStatus.IS_FRIEND
                || friendStatus == FriendStatus.IN_BLACK_LIST) {
            // 显示好友相关设置
            friendMenuLl.setVisibility(View.VISIBLE);

            // 当为好友时显示聊天按钮组
            chatGroupLl.setVisibility(View.VISIBLE);
            // 隐藏添加好友按钮
            addFriendBtn.setVisibility(View.GONE);

            String alias = userInfo.getAlias();
            if (!TextUtils.isEmpty(alias)) {
                // 当有备注名时，主显示备注名，在底部显示额外的用户昵称
                userDisplayNameTv.setText(alias);
                userNameTv.setVisibility(View.VISIBLE);
                String name = getString(R.string.seal_mine_my_account_nickname) + ":" + userInfo.getName();
                userNameTv.setText(name);
            } else {
                // 当没有备注名时，主显示用户昵称，隐藏底部额外显示
                userDisplayNameTv.setText(userInfo.getName());
                userNameTv.setVisibility(View.GONE);
            }

            // 当好友时显示手机号码
//            String phone = getString(R.string.seal_mine_my_account_phone_number) + ":" + userInfo.getPhoneNumber();
//            phoneTv.setText(phone);
        } else {
            // 当非好友时隐藏聊天按钮组，显示添加好友
            chatGroupLl.setVisibility(View.GONE);
            // 隐藏好友相关内容,
            if (!TextUtils.isEmpty(groupId)) {
                //获取是否开启群保护(来自群)
                userDetailViewModel.getGroupInfo(groupId);
            } else {
                friendMenuLl.setVisibility(View.GONE);

                // 自己时不显示添加好友
                if (userInfo.getId().equals(RongIM.getInstance().getCurrentUserId())) {
                    addFriendBtn.setVisibility(View.GONE);
                } else {
                    addFriendBtn.setVisibility(View.VISIBLE);
                }
            }
            userDisplayNameTv.setText(userInfo.getName());
            userNameTv.setVisibility(View.GONE);
        }
        //显示性别图标
        Drawable drawable;
        if (TextUtils.isEmpty(userInfo.getGender()) || "male".equals(userInfo.getGender())) {
            drawable = getResources().getDrawable(R.drawable.seal_account_man);
        } else {
            drawable = getResources().getDrawable(R.drawable.seal_account_female);
        }
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight());
        userDisplayNameTv.setCompoundDrawablePadding(14);
        userDisplayNameTv.setCompoundDrawables(null, null, drawable, null);
        //显示 SealTalk 账号
        if (!TextUtils.isEmpty(userInfo.getStAccount())) {
            phoneTv.setVisibility(View.VISIBLE);
            String stAccount = getString(R.string.seal_mine_my_account_st_account) + userInfo.getStAccount();
            phoneTv.setText(stAccount);
        }
        // 更新头像
        ImageLoaderUtils.displayUserPortraitImage(userInfo.getPortraitUri(), userPortraitIv);
    }

    /**
     * 刷新更多中黑名单选项
     *
     * @param isInBlackList
     */
    private void updateBlackListItem(boolean isInBlackList) {
        this.isInBlackList = isInBlackList;
        if (isInBlackList) {
            blacklistSiv.setContent(R.string.profile_detail_remove_from_blacklist);
        } else {
            blacklistSiv.setContent(R.string.profile_detail_join_the_blacklist);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profile_ll_detail_info_group:
                break;
            case R.id.profile_siv_detail_group:
                toGroupUserInfoDetail();
                break;
            case R.id.profile_siv_detail_phone:
                toSetAliasName();
                break;
            case R.id.profile_siv_detail_alias:
                toSetAliasName();
                break;
            case R.id.profile_siv_detail_blacklist:
                toBlackList(!isInBlackList);
                break;
            case R.id.profile_siv_detail_delete_contact:
                deleteFromContact();
                break;
            case R.id.profile_btn_detail_start_chat:
                startChat();
                break;
            case R.id.profile_btn_detail_start_voice:
                startVoice();
                break;
            case R.id.profile_btn_detail_start_video:
                startVideo();
                break;
            case R.id.profile_btn_detail_add_friend:
                showAddFriendDialog();
                break;
            case R.id.profile_tv_detail_phone:
            default:
                break;
        }
    }

    private void toGroupUserInfoDetail() {
        Intent intentUserInfo = new Intent(this, GroupUserInfoActivity.class);
        intentUserInfo.putExtra(IntentExtra.START_FROM_ID, GroupUserInfoActivity.FROM_USER_DETAIL);
        intentUserInfo.putExtra(IntentExtra.GROUP_ID, groupId);
        intentUserInfo.putExtra(IntentExtra.STR_TARGET_ID, userId);
        startActivity(intentUserInfo);
    }

    private void showOperatePhoneNumDialog() {
        OperatePhoneNumBottomDialog dialog = new OperatePhoneNumBottomDialog(sivDesPhone.getValueView().getText().toString());
        dialog.show(getSupportFragmentManager(), null);
    }

    /**
     * 跳转到设置备注名
     */
    private void toSetAliasName() {
        Intent intent = new Intent(this, EditUserDescribeActivity.class);//EditAliasActivity
        intent.putExtra(IntentExtra.STR_TARGET_ID, userId);
        startActivity(intent);
    }

    /**
     * 是否加到黑名单
     *
     * @param isToBlack true 代表加到黑名单，false 代表移除掉黑名单
     */
    private void toBlackList(boolean isToBlack) {
        if (isToBlack) {
            // 显示确认对话框
            CommonDialog commonDialog = new CommonDialog.Builder()
                    .setContentMessage(getString(R.string.profile_add_to_blacklist_tips))
                    .setDialogButtonClickListener(new CommonDialog.OnDialogButtonClickListener() {
                        @Override
                        public void onPositiveClick(View v, Bundle bundle) {
                            userDetailViewModel.addToBlackList();
                        }

                        @Override
                        public void onNegativeClick(View v, Bundle bundle) {
                        }
                    })
                    .build();
            commonDialog.show(getSupportFragmentManager(), null);
        } else {
            userDetailViewModel.removeFromBlackList();
        }
    }

    /**
     * 从通讯录中删除
     */
    private void deleteFromContact() {
        //弹出删除好友确认对话框
        CommonDialog dialog = new CommonDialog.Builder()
                .setContentMessage(getString(R.string.profile_remove_from_contact_tips_html_format, latestUserInfo.getName()))
                .setDialogButtonClickListener(new CommonDialog.OnDialogButtonClickListener() {
                    @Override
                    public void onPositiveClick(View v, Bundle bundle) {
                        // 标记正在删除好友
                        isInDeleteAction = true;
                        userDetailViewModel.deleteFriend(userId);
                        userDetailViewModel.addToBlackList();
                    }

                    @Override
                    public void onNegativeClick(View v, Bundle bundle) {
                    }
                })
                .build();
        dialog.show(getSupportFragmentManager(), null);
    }

    /**
     * 发起聊天
     */
    private void startChat() {
        String alias = latestUserInfo.getAlias();
        if (!TextUtils.isEmpty(alias)) {
            RongIM.getInstance().startPrivateChat(this, latestUserInfo.getId(), alias);
        } else {
            RongIM.getInstance().startPrivateChat(this, latestUserInfo.getId(), latestUserInfo.getName());
        }
        finish();
    }

    /**
     * 发起音频通话
     */
    public void startVoice() {
        if (latestUserInfo == null) return;

        //todo
        RongCallSession profile = RongCallClient.getInstance().getCallSession();
        if (profile != null && profile.getStartTime() > 0) {
            ToastUtils.showToast(profile.getMediaType() == RongCallCommon.CallMediaType.AUDIO ?
                            getString(io.rong.callkit.R.string.rc_voip_call_audio_start_fail) :
                            getString(io.rong.callkit.R.string.rc_voip_call_video_start_fail),
                    Toast.LENGTH_SHORT);
            return;
        }
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected() || !networkInfo.isAvailable()) {
            ToastUtils.showToast(getString(io.rong.callkit.R.string.rc_voip_call_network_error), Toast.LENGTH_SHORT);
            return;
        }

        Intent intent = new Intent(RongVoIPIntent.RONG_INTENT_ACTION_VOIP_SINGLEAUDIO);
        intent.putExtra("conversationType", Conversation.ConversationType.PRIVATE.getName().toLowerCase(Locale.US));
        intent.putExtra("targetId", latestUserInfo.getId());
        intent.putExtra("callAction", RongCallAction.ACTION_OUTGOING_CALL.getName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage(getPackageName());
        getApplicationContext().startActivity(intent);
    }

    /**
     * 发起视频聊天
     */
    public void startVideo() {
        if (latestUserInfo == null) return;
        RongCallSession profile = RongCallClient.getInstance().getCallSession();
        if (profile != null && profile.getStartTime() > 0) {
            ToastUtils.showToast(
                    profile.getMediaType() == RongCallCommon.CallMediaType.AUDIO ?
                            getString(io.rong.callkit.R.string.rc_voip_call_audio_start_fail) :
                            getString(io.rong.callkit.R.string.rc_voip_call_video_start_fail),
                    Toast.LENGTH_SHORT);
            return;
        }
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected() || !networkInfo.isAvailable()) {
            ToastUtils.showToast(getString(io.rong.callkit.R.string.rc_voip_call_network_error), Toast.LENGTH_SHORT);
            return;
        }
        Intent intent = new Intent(RongVoIPIntent.RONG_INTENT_ACTION_VOIP_SINGLEVIDEO);
        intent.putExtra("conversationType", Conversation.ConversationType.PRIVATE.getName().toLowerCase(Locale.US));
        intent.putExtra("targetId", latestUserInfo.getId());
        intent.putExtra("callAction", RongCallAction.ACTION_OUTGOING_CALL.getName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage(getPackageName());
        getApplicationContext().startActivity(intent);
    }

    /**
     * 显示添加好友对话框
     */
    private void showAddFriendDialog() {
        SimpleInputDialog dialog = new SimpleInputDialog();
        dialog.setInputHint(getString(R.string.profile_add_friend_hint));
        dialog.setInputDialogListener(new SimpleInputDialog.InputDialogListener() {
            @Override
            public boolean onConfirmClicked(EditText input) {
                String inviteMsg = input.getText().toString();
                // 如果邀请信息为空则使用默认邀请语
                if (TextUtils.isEmpty(inviteMsg) && myUserInfo != null) {
                    // 当有附带群组名时显示来自哪个群组，没有时仅带自己的昵称
                    if (!TextUtils.isEmpty(fromGroupName)) {
                        inviteMsg = getString(R.string.profile_invite_friend_description_has_group_format, fromGroupName, myUserInfo.getName());
                    } else {
                        inviteMsg = getString(R.string.profile_invite_friend_description_format, myUserInfo.getName());
                    }
                }
                userDetailViewModel.inviteFriend(inviteMsg);
                return true;
            }
        });
        dialog.show(getSupportFragmentManager(), null);
    }
}
