package cn.rongcloud.im.ui.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.db.model.FriendDetailInfo;
import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.ScreenCaptureResult;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.ui.view.SealTitleBar;
import cn.rongcloud.im.ui.view.SettingItemView;
import cn.rongcloud.im.ui.widget.SelectableRoundedImageView;
import cn.rongcloud.im.utils.CheckPermissionUtils;
import cn.rongcloud.im.utils.ImageLoaderUtils;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.utils.log.SLog;
import cn.rongcloud.im.viewmodel.PrivateChatSettingViewModel;
import io.rong.imkit.conversation.ConversationSettingViewModel;
import io.rong.imkit.model.OperationResult;
import io.rong.imkit.widget.dialog.PromptPopupDialog;
import io.rong.imlib.model.Conversation;

public class PrivateChatSettingActivity extends TitleBaseActivity implements View.OnClickListener {
    private final String TAG = "PrivateChatSettingActivity";
    /**
     * 发起创建群组
     */
    private final int REQUEST_START_GROUP = 1000;

    private PrivateChatSettingViewModel privateChatSettingViewModel;
    private ConversationSettingViewModel conversationSettingViewModel;

    private SettingItemView isNotifySb;
    private SettingItemView isTopSb;
    private SettingItemView isScreenShotSiv;

    private String targetId;
    private String name;
    private String portraitUrl;
    private Conversation.ConversationType conversationType;
    private SelectableRoundedImageView portraitIv;
    private TextView nameTv;
    private boolean isScreenShotSivClicked = false;

    private final int REQUEST_CODE_PERMISSION = 114;
    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SealTitleBar titleBar = getTitleBar();
        titleBar.setTitle(R.string.profile_chat_details);

        setContentView(R.layout.profile_activity_private_chat_setting);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        targetId = intent.getStringExtra(IntentExtra.STR_TARGET_ID);
        conversationType = (Conversation.ConversationType) intent.getSerializableExtra(IntentExtra.SERIA_CONVERSATION_TYPE);
        initView();
        initViewModel();
        initData();
    }

    private void initView() {
        portraitIv = findViewById(R.id.profile_siv_user_header);
        portraitIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PrivateChatSettingActivity.this, UserDetailActivity.class);
                intent.putExtra(IntentExtra.STR_TARGET_ID, targetId);
                startActivity(intent);
            }
        });

        // 用户名
        nameTv = findViewById(R.id.profile_tv_user_name);

        // 添加用户至群聊
        findViewById(R.id.profile_iv_add_member).setOnClickListener(this);

        // 查询聊天记录
        findViewById(R.id.siv_search_messages).setOnClickListener(this);
        // 清除聊天记录
        findViewById(R.id.siv_clean_chat_message).setOnClickListener(this);

        isNotifySb = findViewById(R.id.siv_user_notification);
        isNotifySb.setSwitchCheckListener((buttonView, isChecked) ->
                conversationSettingViewModel.setNotificationStatus(isChecked ? Conversation.ConversationNotificationStatus.DO_NOT_DISTURB : Conversation.ConversationNotificationStatus.NOTIFY));
        isTopSb = findViewById(R.id.siv_conversation_top);
        isTopSb.setSwitchCheckListener((buttonView, isChecked) ->
                conversationSettingViewModel.setConversationTop(isChecked, false));

        isScreenShotSiv = findViewById(R.id.profile_siv_group_screen_shot_notification);
        isScreenShotSiv.setSwitchTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!isScreenShotSivClicked) {
                    isScreenShotSivClicked = true;
                }
                return false;
            }
        });
        isScreenShotSiv.setSwitchCheckListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //初始化不触发逻辑
                if (!isScreenShotSivClicked) {
                    return;
                }
                // 0 关闭 1 打开
                if (isChecked) {
                    //没有权限不开启设置
                    if (!requestReadPermissions()) {
                        return;
                    }
                    privateChatSettingViewModel.setScreenCaptureStatus(1);
                } else {
                    privateChatSettingViewModel.setScreenCaptureStatus(0);
                }
            }
        });
    }

    private boolean requestReadPermissions() {
        return CheckPermissionUtils.requestPermissions(this, permissions, REQUEST_CODE_PERMISSION);
    }

    private void initViewModel() {
        privateChatSettingViewModel = ViewModelProviders.of(this, new PrivateChatSettingViewModel.Factory(getApplication(), targetId, conversationType)).get(PrivateChatSettingViewModel.class);
        privateChatSettingViewModel.getFriendInfo().observe(this, friendShipInfoResource -> {
            FriendShipInfo data = friendShipInfoResource.data;
            if (data == null) return;

            String displayName = data.getDisplayName();
            FriendDetailInfo user = data.getUser();

            // 设置备注名
            if (!TextUtils.isEmpty(displayName)) {
                nameTv.setText(displayName);
                name = displayName;
            } else if (user != null) {
                nameTv.setText(user.getNickname());
                name = user.getNickname();
            }

            if (user != null) {
                ImageLoaderUtils.displayUserPortraitImage(user.getPortraitUri(), portraitIv);
                portraitUrl = user.getPortraitUri();
            }
        });

        conversationSettingViewModel = ViewModelProviders.of(this, new ConversationSettingViewModel.Factory(getApplication(), conversationType, targetId)).get(ConversationSettingViewModel.class);

        conversationSettingViewModel.getNotificationStatus().observe(this, conversationNotificationStatus -> {
            isNotifySb.setCheckedImmediatelyWithOutEvent(conversationNotificationStatus.equals(Conversation.ConversationNotificationStatus.DO_NOT_DISTURB));
        });

        conversationSettingViewModel.getTopStatus().observe(this, isTop -> {
            isTopSb.setCheckedImmediatelyWithOutEvent(isTop);
        });

        conversationSettingViewModel.getOperationResult().observe(this, operationResult -> {
            if (operationResult.mResultCode == OperationResult.SUCCESS) {
                if (operationResult.mAction.equals(OperationResult.Action.CLEAR_CONVERSATION_MESSAGES)) {
                    ToastUtils.showToast(R.string.common_clear_success);
                } else {
                    ToastUtils.showToast(getString(R.string.seal_set_clean_time_success));
                }
            } else {
                if (operationResult.mAction.equals(OperationResult.Action.CLEAR_CONVERSATION_MESSAGES)) {
                    ToastUtils.showToast(R.string.common_clear_failure);
                } else {
                    ToastUtils.showToast(getString(R.string.seal_set_clean_time_fail));
                }
            }
        });

        // 获取截屏通知结果
        privateChatSettingViewModel.getScreenCaptureStatusResult().observe(this, new Observer<Resource<ScreenCaptureResult>>() {
            @Override
            public void onChanged(Resource<ScreenCaptureResult> screenCaptureResultResource) {
                if (screenCaptureResultResource.status == Status.SUCCESS) {
                    //0 关闭 1 打开
                    if (screenCaptureResultResource.data != null && screenCaptureResultResource.data.status == 1) {
                        isScreenShotSiv.setCheckedImmediately(true);
                    }
                }
            }
        });
        // 获取设置截屏通知结果
        privateChatSettingViewModel.getSetScreenCaptureResult().observe(this, new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> voidResource) {
                if (voidResource.status == Status.SUCCESS) {
                } else if (voidResource.status == Status.ERROR) {

                }
            }
        });
    }

    private void initData() {
        privateChatSettingViewModel.requestFriendInfo();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.siv_clean_chat_message:
                showCleanMessageDialog();
                break;
            case R.id.siv_search_messages:
                goSearchChatMessage();
                break;
            case R.id.profile_iv_add_member:
                addOtherMemberToGroup();
                break;
            default:
        }
    }

    /**
     * 显示清除聊天消息对话框
     */
    private void showCleanMessageDialog() {
        PromptPopupDialog.newInstance(this,
                getString(R.string.profile_clean_private_chat_history)).setLayoutRes(io.rong.imkit.R.layout.rc_dialog_popup_prompt_warning)
                .setPromptButtonClickedListener(() -> {
                    conversationSettingViewModel.clearMessages(0, false);
                }).show();
    }

    /**
     * 跳转到聊天记录搜索界面
     */
    private void goSearchChatMessage() {
        Intent intent = new Intent(this, SearchHistoryMessageActivity.class);
        intent.putExtra(IntentExtra.STR_TARGET_ID, targetId);
        intent.putExtra(IntentExtra.SERIA_CONVERSATION_TYPE, conversationType);
        intent.putExtra(IntentExtra.STR_CHAT_NAME, name);
        intent.putExtra(IntentExtra.STR_CHAT_PORTRAIT, portraitUrl);
        startActivity(intent);
    }

    /**
     * 添加其他人发起群聊
     */
    private void addOtherMemberToGroup() {
        Intent intent = new Intent(this, SelectCreateGroupActivity.class);
        ArrayList<String> friendIdList = new ArrayList<>();
        friendIdList.add(targetId);
        intent.putStringArrayListExtra(IntentExtra.LIST_CAN_NOT_CHECK_ID_LIST, friendIdList);
        startActivityForResult(intent, REQUEST_START_GROUP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_START_GROUP && resultCode == RESULT_OK) {
            ArrayList<String> memberList = data.getStringArrayListExtra(IntentExtra.LIST_STR_ID_LIST);
            // 添加该好友的id
            memberList.add(targetId);
            SLog.i(TAG, "memberList.size = " + memberList.size());
            Intent intent = new Intent(this, CreateGroupActivity.class);
            intent.putExtra(IntentExtra.LIST_STR_ID_LIST, memberList);
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSION && !CheckPermissionUtils.allPermissionGranted(grantResults)) {
            List<String> permissionsNotGranted = new ArrayList<>();
            for (String permission : permissions) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    permissionsNotGranted.add(permission);
                }
            }
            if (permissionsNotGranted.size() > 0) {
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivityForResult(intent, requestCode);
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                            default:
                                break;
                        }
                    }
                };
                CheckPermissionUtils.showPermissionAlert(this, getResources().getString(R.string.seal_grant_permissions) + CheckPermissionUtils.getNotGrantedPermissionMsg(this, permissionsNotGranted), listener);
            } else {
                ToastUtils.showToast(getString(R.string.seal_set_clean_time_fail));
            }
        } else {
            //权限获得后在请求次网络设置状态
            privateChatSettingViewModel.setScreenCaptureStatus(1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
