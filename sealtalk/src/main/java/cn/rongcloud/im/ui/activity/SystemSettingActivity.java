package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.view.SealTitleBar;
import cn.rongcloud.im.ui.view.SettingItemView;
import cn.rongcloud.im.utils.ToastUtils;
import io.rong.imkit.conversation.ConversationSettingViewModel;
import io.rong.imkit.model.OperationResult;
import io.rong.imkit.notification.RongNotificationManager;
import io.rong.imkit.widget.dialog.PromptPopupDialog;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.ConversationIdentifier;

public class SystemSettingActivity extends TitleBaseActivity implements View.OnClickListener {
    private ConversationSettingViewModel conversationSettingViewModel;

    private SettingItemView isNotifySb;
    private SettingItemView isTopSb;

    private ConversationIdentifier conversationIdentifier;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SealTitleBar titleBar = getTitleBar();
        titleBar.setTitle(R.string.profile_chat_details);

        setContentView(R.layout.profile_activity_system_chat_setting);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        conversationIdentifier = initConversationIdentifier();
        initView();
        initViewModel();
    }

    private void initView() {
        // 清除聊天记录
        findViewById(R.id.siv_clean_chat_message).setOnClickListener(this);

        isNotifySb = findViewById(R.id.siv_user_notification);
        isNotifySb.setSwitchCheckListener(
                (buttonView, isChecked) ->
                        conversationSettingViewModel.setNotificationStatus(
                                isChecked
                                        ? Conversation.ConversationNotificationStatus.DO_NOT_DISTURB
                                        : Conversation.ConversationNotificationStatus.NOTIFY));
        updateUserNotification();
        isTopSb = findViewById(R.id.siv_conversation_top);
        isTopSb.setSwitchCheckListener(
                (buttonView, isChecked) ->
                        conversationSettingViewModel.setConversationTop(isChecked, false));
    }

    private void initViewModel() {
        conversationSettingViewModel =
                ViewModelProviders.of(
                                this,
                                new ConversationSettingViewModel.Factory(
                                        getApplication(), conversationIdentifier))
                        .get(ConversationSettingViewModel.class);

        conversationSettingViewModel
                .getNotificationStatus()
                .observe(
                        this,
                        conversationNotificationStatus ->
                                isNotifySb.setCheckedImmediatelyWithOutEvent(
                                        conversationNotificationStatus.equals(
                                                Conversation.ConversationNotificationStatus
                                                        .DO_NOT_DISTURB)));

        conversationSettingViewModel
                .getTopStatus()
                .observe(this, isTop -> isTopSb.setCheckedImmediatelyWithOutEvent(isTop));

        conversationSettingViewModel
                .getOperationResult()
                .observe(
                        this,
                        operationResult -> {
                            if (operationResult.mResultCode == OperationResult.SUCCESS) {
                                if (operationResult.mAction.equals(
                                        OperationResult.Action.CLEAR_CONVERSATION_MESSAGES)) {
                                    ToastUtils.showToast(R.string.common_clear_success);
                                } else {
                                    ToastUtils.showToast(
                                            getString(R.string.seal_set_clean_time_success));
                                }
                            } else {
                                if (operationResult.mAction.equals(
                                        OperationResult.Action.CLEAR_CONVERSATION_MESSAGES)) {
                                    ToastUtils.showToast(R.string.common_clear_failure);
                                } else {
                                    ToastUtils.showToast(
                                            getString(R.string.seal_set_clean_time_fail));
                                }
                            }
                        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.siv_clean_chat_message) {
            showCleanMessageDialog();
        }
    }

    /** 显示清除聊天消息对话框 */
    private void showCleanMessageDialog() {
        PromptPopupDialog.newInstance(this, getString(R.string.profile_clean_private_chat_history))
                .setLayoutRes(io.rong.imkit.R.layout.rc_dialog_popup_prompt_warning)
                .setPromptButtonClickedListener(
                        () -> conversationSettingViewModel.clearMessages(0, false))
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void updateUserNotification() {
        RongNotificationManager.getInstance()
                .getConversationNotificationStatus(
                        conversationIdentifier,
                        new RongIMClient.ResultCallback<
                                Conversation.ConversationNotificationStatus>() {
                            @Override
                            public void onSuccess(
                                    Conversation.ConversationNotificationStatus
                                            conversationNotificationStatus) {
                                runOnUiThread(
                                        () ->
                                                isNotifySb.setCheckedImmediatelyWithOutEvent(
                                                        conversationNotificationStatus.equals(
                                                                Conversation
                                                                        .ConversationNotificationStatus
                                                                        .DO_NOT_DISTURB)));
                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode coreErrorCode) {
                                runOnUiThread(
                                        () -> ToastUtils.showToast("获取失败-" + coreErrorCode.code));
                            }
                        });
    }
}
