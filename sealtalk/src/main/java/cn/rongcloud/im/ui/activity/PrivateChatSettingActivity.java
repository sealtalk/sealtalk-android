package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import java.util.ArrayList;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.db.model.FriendDetailInfo;
import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.ui.view.SealTitleBar;
import cn.rongcloud.im.ui.view.SettingItemView;
import cn.rongcloud.im.ui.widget.SelectableRoundedImageView;
import cn.rongcloud.im.utils.ImageLoaderUtils;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.viewmodel.PrivateChatSettingViewModel;
import cn.rongcloud.im.utils.log.SLog;
import io.rong.imkit.utilities.PromptPopupDialog;
import io.rong.imlib.model.Conversation;

public class PrivateChatSettingActivity extends TitleBaseActivity implements View.OnClickListener {
    private final String TAG = "PrivateChatSettingActivity";
    /**
     * 发起创建群组
     */
    private final int REQUEST_START_GROUP = 1000;

    private PrivateChatSettingViewModel privateChatSettingViewModel;

    private SettingItemView isNotifySb;
    private SettingItemView isTopSb;

    private String targetId;
    private String name;
    private String portraitUrl;
    private Conversation.ConversationType conversationType;
    private SelectableRoundedImageView portraitIv;
    private TextView nameTv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SealTitleBar titleBar = getTitleBar();
        titleBar.setTitle(R.string.profile_user_details);

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
                privateChatSettingViewModel.setIsNotifyConversation(!isChecked));
        isTopSb = findViewById(R.id.siv_conversation_top);
        isTopSb.setSwitchCheckListener((buttonView, isChecked) ->
                privateChatSettingViewModel.setConversationOnTop(isChecked));


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

        // 获取是否通知消息状态
        privateChatSettingViewModel.getIsNotify().observe(this, resource -> {
            if (resource.data != null) {
                if (resource.status == Status.SUCCESS) {
                    isNotifySb.setChecked(!resource.data);
                } else {
                    isNotifySb.setCheckedImmediately(!resource.data);
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
        privateChatSettingViewModel.getIsTop().observe(this, resource -> {
            if (resource.data != null) {
                if (resource.status == Status.SUCCESS) {
                    isTopSb.setChecked(resource.data);
                } else {
                    isTopSb.setCheckedImmediately(resource.data);
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
        privateChatSettingViewModel.getCleanHistoryMessageResult().observe(this, resource -> {
            if (resource.status == Status.SUCCESS) {
                ToastUtils.showToast(R.string.common_clear_success);
            } else if (resource.status == Status.ERROR) {
                ToastUtils.showToast(R.string.common_clear_failure);
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
                    privateChatSettingViewModel.cleanHistoryMessage();
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
}
