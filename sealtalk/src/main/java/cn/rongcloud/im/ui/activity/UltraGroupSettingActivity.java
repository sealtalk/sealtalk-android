package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.dialog.UltraGroupNotifyTestInputDialog;
import cn.rongcloud.im.ui.view.SealTitleBar;
import cn.rongcloud.im.ui.view.SettingItemView;
import cn.rongcloud.im.utils.ToastUtils;
import io.rong.common.RLog;
import io.rong.imkit.conversation.ConversationSettingViewModel;
import io.rong.imkit.model.OperationResult;
import io.rong.imkit.widget.dialog.PromptPopupDialog;
import io.rong.imlib.ChannelClient;
import io.rong.imlib.IRongCoreCallback;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.ConversationIdentifier;
import java.util.ArrayList;
import java.util.List;

public class UltraGroupSettingActivity extends TitleBaseActivity implements View.OnClickListener {
    private ConversationSettingViewModel conversationSettingViewModel;

    private SettingItemView isNotifySb;
    private SettingItemView isTopSb;

    private ConversationIdentifier conversationIdentifier;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SealTitleBar titleBar = getTitleBar();
        titleBar.setTitle(R.string.profile_chat_details);

        setContentView(R.layout.profile_activity_ultra_group_chat_setting);

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
        findViewById(R.id.siv_clean_all_channel).setOnClickListener(this);
        findViewById(R.id.siv_clean_channel).setOnClickListener(this);
        findViewById(R.id.siv_clean_remote_channel).setOnClickListener(this);
        findViewById(R.id.siv_get_channel_list).setOnClickListener(this);

        // 免打扰
        findViewById(R.id.siv_set_notify_level).setOnClickListener(this);
        findViewById(R.id.siv_remove_notify_level).setOnClickListener(this);
        findViewById(R.id.siv_query_notify_level).setOnClickListener(this);
        findViewById(R.id.siv_conversation_channel_level).setOnClickListener(this);
        findViewById(R.id.siv_query_conversation_channel_level).setOnClickListener(this);
        findViewById(R.id.siv_set_conversation_level).setOnClickListener(this);
        findViewById(R.id.siv_query_conversation_level).setOnClickListener(this);
        findViewById(R.id.siv_set_conversation_type_level).setOnClickListener(this);
        findViewById(R.id.siv_query_conversation_type_level).setOnClickListener(this);
        findViewById(R.id.siv_set_ultra_group_type_level).setOnClickListener(this);
        findViewById(R.id.siv_query_ultra_group_type_level).setOnClickListener(this);
        findViewById(R.id.siv_set_ultra_group_channel_level).setOnClickListener(this);
        findViewById(R.id.siv_query_ultra_group_channel_level).setOnClickListener(this);
        findViewById(R.id.siv_query_ultra_group_unread_count).setOnClickListener(this);
        findViewById(R.id.siv_query_ultra_group_all_unread_count).setOnClickListener(this);
        findViewById(R.id.siv_query_ultra_group_all_mention_unread_count).setOnClickListener(this);
        // 按会话免打扰类型，获取未读消息数
        findViewById(R.id.siv_query_ultra_group_unread_count_by_level).setOnClickListener(this);
        findViewById(R.id.siv_query_ultra_group_mention_count_by_level).setOnClickListener(this);
        findViewById(R.id.siv_query_target_ultra_group_unread_count_by_level)
                .setOnClickListener(this);
        findViewById(R.id.siv_query_target_ultra_group_mention_count_by_level)
                .setOnClickListener(this);
        findViewById(R.id.siv_query_target_ultra_group_cur_channel_message)
                .setOnClickListener(this);
        findViewById(R.id.siv_query_target_ultra_group_all_channel_message)
                .setOnClickListener(this);

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

        switch (v.getId()) {
            case R.id.siv_clean_chat_message:
                showCleanMessageDialog();
                break;
            case R.id.siv_clean_all_channel:
                ChannelClient.getInstance()
                        .deleteUltraGroupMessagesForAllChannel(
                                conversationIdentifier.getTargetId(),
                                System.currentTimeMillis(),
                                new IRongCoreCallback.ResultCallback<Boolean>() {
                                    @Override
                                    public void onSuccess(Boolean aBoolean) {
                                        runOnUiThread(
                                                new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        ToastUtils.showToast("删除成功");
                                                    }
                                                });
                                    }

                                    @Override
                                    public void onError(IRongCoreEnum.CoreErrorCode e) {
                                        runOnUiThread(
                                                new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        ToastUtils.showToast(
                                                                "删除失败-" + e.getValue());
                                                    }
                                                });
                                    }
                                });
                break;
            case R.id.siv_clean_channel:
                ChannelClient.getInstance()
                        .deleteUltraGroupMessages(
                                conversationIdentifier.getTargetId(),
                                conversationIdentifier.getChannelId(),
                                System.currentTimeMillis(),
                                new IRongCoreCallback.ResultCallback<Boolean>() {
                                    @Override
                                    public void onSuccess(Boolean aBoolean) {
                                        runOnUiThread(
                                                new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        ToastUtils.showToast("删除成功");
                                                    }
                                                });
                                    }

                                    @Override
                                    public void onError(IRongCoreEnum.CoreErrorCode e) {
                                        runOnUiThread(
                                                new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        ToastUtils.showToast(
                                                                "删除失败-" + e.getValue());
                                                    }
                                                });
                                    }
                                });
                break;
            case R.id.siv_clean_remote_channel:
                ChannelClient.getInstance()
                        .deleteRemoteUltraGroupMessages(
                                conversationIdentifier.getTargetId(),
                                conversationIdentifier.getChannelId(),
                                System.currentTimeMillis(),
                                new IRongCoreCallback.OperationCallback() {
                                    @Override
                                    public void onSuccess() {
                                        runOnUiThread(
                                                new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        ToastUtils.showToast("删除成功");
                                                    }
                                                });
                                    }

                                    @Override
                                    public void onError(IRongCoreEnum.CoreErrorCode coreErrorCode) {
                                        runOnUiThread(
                                                new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        ToastUtils.showToast(
                                                                "删除失败-" + coreErrorCode.getValue());
                                                    }
                                                });
                                    }
                                });
                break;

            case R.id.siv_get_channel_list:
                ChannelClient.getInstance()
                        .getConversationListForAllChannel(
                                conversationIdentifier.getType(),
                                conversationIdentifier.getChannelId(),
                                new IRongCoreCallback.ResultCallback<List<Conversation>>() {
                                    @Override
                                    public void onSuccess(List<Conversation> conversations) {
                                        if (conversations == null) {
                                            return;
                                        }
                                        runOnUiThread(
                                                new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        StringBuilder stringBuilder =
                                                                new StringBuilder();
                                                        stringBuilder
                                                                .append("列表个数为： ")
                                                                .append(conversations.size())
                                                                .append(", 获取列表通道 ：");
                                                        for (Conversation conversation :
                                                                conversations) {
                                                            stringBuilder
                                                                    .append(
                                                                            conversation
                                                                                    .getChannelId())
                                                                    .append(",");
                                                        }
                                                        ToastUtils.showToast(
                                                                stringBuilder.toString());
                                                    }
                                                });
                                    }

                                    @Override
                                    public void onError(IRongCoreEnum.CoreErrorCode e) {
                                        runOnUiThread(
                                                new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        ToastUtils.showToast(
                                                                "获取列表失败-" + e.getValue());
                                                    }
                                                });
                                    }
                                });
                break;
            case R.id.siv_set_notify_level:
                UltraGroupNotifyTestInputDialog ultraGroupNotifyTestInputDialog =
                        new UltraGroupNotifyTestInputDialog(
                                this,
                                UltraGroupNotifyTestInputDialog.TYPE_NOTIFICATION_QUIET_HOUR_LEVEL);
                ultraGroupNotifyTestInputDialog
                        .getSureView()
                        .setOnClickListener(
                                v1 -> {
                                    String time =
                                            ultraGroupNotifyTestInputDialog
                                                    .getEtTagId()
                                                    .getText()
                                                    .toString();
                                    String timeSpan =
                                            ultraGroupNotifyTestInputDialog
                                                    .getEtTagName()
                                                    .getText()
                                                    .toString();
                                    String level =
                                            ultraGroupNotifyTestInputDialog
                                                    .getEtType()
                                                    .getText()
                                                    .toString();
                                    if (TextUtils.isEmpty(time)
                                            || TextUtils.isEmpty(timeSpan)
                                            || TextUtils.isEmpty(level)) {
                                        ToastUtils.showToast("请输入正确的值");
                                        return;
                                    }
                                    setNotificationQuietHoursLevel(time, timeSpan, level);
                                    ultraGroupNotifyTestInputDialog.cancel();
                                });

                ultraGroupNotifyTestInputDialog
                        .getCancelView()
                        .setOnClickListener(v12 -> ultraGroupNotifyTestInputDialog.cancel());
                ultraGroupNotifyTestInputDialog.show();
                break;
            case R.id.siv_remove_notify_level:
                removeNotificationQuietHours();
                break;
            case R.id.siv_query_notify_level:
                getNotificationQuietHours();
                break;
            case R.id.siv_conversation_channel_level:
                UltraGroupNotifyTestInputDialog conversationDialog =
                        new UltraGroupNotifyTestInputDialog(
                                this,
                                UltraGroupNotifyTestInputDialog.TYPE_CONVERSATION_CHANNEL_LEVEL);
                conversationDialog
                        .getSureView()
                        .setOnClickListener(
                                v1 -> {
                                    String type =
                                            conversationDialog.getEtTagId().getText().toString();
                                    String level =
                                            conversationDialog.getEtTagName().getText().toString();
                                    String channel =
                                            conversationDialog.getEtType().getText().toString();

                                    String targetId =
                                            conversationDialog.getEtTargetId().getText().toString();
                                    if (TextUtils.isEmpty(type)
                                            || TextUtils.isEmpty(level)
                                            || TextUtils.isEmpty(targetId)) {
                                        ToastUtils.showToast("请输入正确的值");
                                        return;
                                    }
                                    if (TextUtils.isEmpty(channel)) {
                                        channel = "";
                                    }
                                    setConversationChannelNotificationLevel(
                                            type, targetId, channel, level);
                                    conversationDialog.cancel();
                                });

                conversationDialog
                        .getCancelView()
                        .setOnClickListener(v12 -> conversationDialog.cancel());
                conversationDialog.show();
                break;
            case R.id.siv_query_conversation_channel_level:
                UltraGroupNotifyTestInputDialog queryDialog =
                        new UltraGroupNotifyTestInputDialog(
                                this,
                                UltraGroupNotifyTestInputDialog
                                        .TYPE_QUERY_CONVERSATION_CHANNEL_LEVEL);
                queryDialog
                        .getSureView()
                        .setOnClickListener(
                                v1 -> {
                                    String type = queryDialog.getEtTagId().getText().toString();
                                    String channel = queryDialog.getEtType().getText().toString();

                                    String targetId =
                                            queryDialog.getEtTargetId().getText().toString();
                                    if (TextUtils.isEmpty(type) || TextUtils.isEmpty(targetId)) {
                                        ToastUtils.showToast("请输入正确的值");
                                        return;
                                    }
                                    if (TextUtils.isEmpty(channel)) {
                                        channel = "";
                                    }
                                    getConversationChannelNotificationLevel(
                                            type, targetId, channel);
                                    queryDialog.cancel();
                                });

                queryDialog.getCancelView().setOnClickListener(v12 -> queryDialog.cancel());
                queryDialog.show();
                break;
            case R.id.siv_set_conversation_level:
                UltraGroupNotifyTestInputDialog setConversation =
                        new UltraGroupNotifyTestInputDialog(
                                this, UltraGroupNotifyTestInputDialog.TYPE_CONVERSATION_LEVEL);
                setConversation
                        .getSureView()
                        .setOnClickListener(
                                v1 -> {
                                    String type = setConversation.getEtTagId().getText().toString();
                                    String level =
                                            setConversation.getEtTagName().getText().toString();

                                    String targetId =
                                            setConversation.getEtTargetId().getText().toString();
                                    if (TextUtils.isEmpty(type)
                                            || TextUtils.isEmpty(level)
                                            || TextUtils.isEmpty(targetId)) {
                                        ToastUtils.showToast("请输入正确的值");
                                        return;
                                    }
                                    setConversationNotificationLevel(type, targetId, level);
                                    setConversation.cancel();
                                });

                setConversation.getCancelView().setOnClickListener(v12 -> setConversation.cancel());
                setConversation.show();
                break;
            case R.id.siv_query_conversation_level:
                UltraGroupNotifyTestInputDialog queryConversation =
                        new UltraGroupNotifyTestInputDialog(
                                this,
                                UltraGroupNotifyTestInputDialog.TYPE_QUERY_CONVERSATION_LEVEL);
                queryConversation
                        .getSureView()
                        .setOnClickListener(
                                v1 -> {
                                    String type =
                                            queryConversation.getEtTagId().getText().toString();

                                    String targetId =
                                            queryConversation.getEtTargetId().getText().toString();
                                    if (TextUtils.isEmpty(type) || TextUtils.isEmpty(targetId)) {
                                        ToastUtils.showToast("请输入正确的值");
                                        return;
                                    }
                                    getConversationNotificationLevel(type, targetId);
                                    queryConversation.cancel();
                                });

                queryConversation
                        .getCancelView()
                        .setOnClickListener(v12 -> queryConversation.cancel());
                queryConversation.show();
                break;
            case R.id.siv_set_conversation_type_level:
                UltraGroupNotifyTestInputDialog conversationType =
                        new UltraGroupNotifyTestInputDialog(
                                this,
                                UltraGroupNotifyTestInputDialog.TYPE_SET_CONVERSATION_TYPE_LEVEL);
                conversationType
                        .getSureView()
                        .setOnClickListener(
                                v1 -> {
                                    String type =
                                            conversationType.getEtTagId().getText().toString();

                                    String level =
                                            conversationType.getEtTagName().getText().toString();
                                    if (TextUtils.isEmpty(type) || TextUtils.isEmpty(level)) {
                                        ToastUtils.showToast("请输入正确的值");
                                        return;
                                    }
                                    setConversationTypeNotificationLevel(type, level);
                                    conversationType.cancel();
                                });

                conversationType
                        .getCancelView()
                        .setOnClickListener(v12 -> conversationType.cancel());
                conversationType.show();
                break;
            case R.id.siv_query_conversation_type_level:
                UltraGroupNotifyTestInputDialog queryConversationType =
                        new UltraGroupNotifyTestInputDialog(
                                this,
                                UltraGroupNotifyTestInputDialog.TYPE_QUERY_CONVERSATION_TYPE_LEVEL);
                queryConversationType
                        .getSureView()
                        .setOnClickListener(
                                v1 -> {
                                    String type =
                                            queryConversationType.getEtTagId().getText().toString();
                                    if (TextUtils.isEmpty(type)) {
                                        ToastUtils.showToast("请输入正确的值");
                                        return;
                                    }
                                    getConversationTypeNotificationLevel(type);
                                    queryConversationType.cancel();
                                });

                queryConversationType
                        .getCancelView()
                        .setOnClickListener(v12 -> queryConversationType.cancel());
                queryConversationType.show();
                break;
            case R.id.siv_set_ultra_group_type_level:
                UltraGroupNotifyTestInputDialog setUltraConversationType =
                        new UltraGroupNotifyTestInputDialog(
                                this,
                                UltraGroupNotifyTestInputDialog.TYPE_SET_ULTRA_GROUP_TYPE_LEVEL);
                setUltraConversationType
                        .getSureView()
                        .setOnClickListener(
                                v1 -> {
                                    String targetId =
                                            setUltraConversationType
                                                    .getEtTargetId()
                                                    .getText()
                                                    .toString();
                                    String level =
                                            setUltraConversationType
                                                    .getEtTagName()
                                                    .getText()
                                                    .toString();
                                    if (TextUtils.isEmpty(targetId) || TextUtils.isEmpty(level)) {
                                        ToastUtils.showToast("请输入正确的值");
                                        return;
                                    }
                                    setUltraGroupConversationDefaultNotificationLevel(
                                            targetId, level);
                                    setUltraConversationType.cancel();
                                });

                setUltraConversationType
                        .getCancelView()
                        .setOnClickListener(v12 -> setUltraConversationType.cancel());
                setUltraConversationType.show();
                break;
            case R.id.siv_query_ultra_group_type_level:
                UltraGroupNotifyTestInputDialog queryUltraConversationType =
                        new UltraGroupNotifyTestInputDialog(
                                this,
                                UltraGroupNotifyTestInputDialog.TYPE_QUERY_ULTRA_GROUP_TYPE_LEVEL);
                queryUltraConversationType
                        .getSureView()
                        .setOnClickListener(
                                v1 -> {
                                    String targetId =
                                            queryUltraConversationType
                                                    .getEtTargetId()
                                                    .getText()
                                                    .toString();
                                    if (TextUtils.isEmpty(targetId)) {
                                        ToastUtils.showToast("请输入正确的值");
                                        return;
                                    }
                                    getUltraGroupConversationDefaultNotificationLevel(targetId);
                                    queryUltraConversationType.cancel();
                                });

                queryUltraConversationType
                        .getCancelView()
                        .setOnClickListener(v12 -> queryUltraConversationType.cancel());
                queryUltraConversationType.show();
                break;
            case R.id.siv_set_ultra_group_channel_level:
                UltraGroupNotifyTestInputDialog setUltraConversationChannel =
                        new UltraGroupNotifyTestInputDialog(
                                this,
                                UltraGroupNotifyTestInputDialog.TYPE_SET_ULTRA_GROUP_CHANNEL_LEVEL);
                setUltraConversationChannel
                        .getSureView()
                        .setOnClickListener(
                                v1 -> {
                                    String targetId =
                                            setUltraConversationChannel
                                                    .getEtTargetId()
                                                    .getText()
                                                    .toString();
                                    String level =
                                            setUltraConversationChannel
                                                    .getEtTagName()
                                                    .getText()
                                                    .toString();
                                    String channel =
                                            setUltraConversationChannel
                                                    .getEtType()
                                                    .getText()
                                                    .toString();
                                    if (TextUtils.isEmpty(targetId) || TextUtils.isEmpty(level)) {
                                        ToastUtils.showToast("请输入正确的值");
                                        return;
                                    }
                                    if (TextUtils.isEmpty(channel)) {
                                        channel = "";
                                    }
                                    setUltraGroupConversationChannelDefaultNotificationLevel(
                                            targetId, level, channel);
                                    setUltraConversationChannel.cancel();
                                });

                setUltraConversationChannel
                        .getCancelView()
                        .setOnClickListener(v12 -> setUltraConversationChannel.cancel());
                setUltraConversationChannel.show();
                break;
            case R.id.siv_query_ultra_group_channel_level:
                UltraGroupNotifyTestInputDialog queryUltraConversationChannel =
                        new UltraGroupNotifyTestInputDialog(
                                this,
                                UltraGroupNotifyTestInputDialog
                                        .TYPE_QUERY_ULTRA_GROUP_CHANNEL_LEVEL);
                queryUltraConversationChannel
                        .getSureView()
                        .setOnClickListener(
                                v1 -> {
                                    String targetId =
                                            queryUltraConversationChannel
                                                    .getEtTargetId()
                                                    .getText()
                                                    .toString();
                                    String channel =
                                            queryUltraConversationChannel
                                                    .getEtType()
                                                    .getText()
                                                    .toString();
                                    if (TextUtils.isEmpty(targetId)) {
                                        ToastUtils.showToast("请输入正确的值");
                                        return;
                                    }
                                    if (TextUtils.isEmpty(channel)) {
                                        channel = "";
                                    }
                                    getUltraGroupConversationChannelDefaultNotificationLevel(
                                            targetId, channel);
                                    queryUltraConversationChannel.cancel();
                                });

                queryUltraConversationChannel
                        .getCancelView()
                        .setOnClickListener(v12 -> queryUltraConversationChannel.cancel());
                queryUltraConversationChannel.show();
                break;

            case R.id.siv_query_ultra_group_unread_count:
                UltraGroupNotifyTestInputDialog queryUltraUnreadCount =
                        new UltraGroupNotifyTestInputDialog(
                                this,
                                UltraGroupNotifyTestInputDialog
                                        .TYPE_QUERY_ULTRA_GROUP_UNREAD_COUNT);
                queryUltraUnreadCount
                        .getSureView()
                        .setOnClickListener(
                                v1 -> {
                                    String targetId =
                                            queryUltraUnreadCount
                                                    .getEtTargetId()
                                                    .getText()
                                                    .toString();
                                    if (TextUtils.isEmpty(targetId)) {
                                        ToastUtils.showToast("请输入正确的值");
                                        return;
                                    }
                                    queryUltraUnreadCount(targetId);
                                    queryUltraUnreadCount.cancel();
                                });

                queryUltraUnreadCount
                        .getCancelView()
                        .setOnClickListener(v12 -> queryUltraUnreadCount.cancel());
                queryUltraUnreadCount.show();
                break;
            case R.id.siv_query_ultra_group_all_unread_count:
                queryUltraGroupAllUnreadCount();
                break;
            case R.id.siv_query_ultra_group_all_mention_unread_count:
                queryUltraGroupAllMentionUnreadCount();
                break;
            case R.id.siv_query_ultra_group_unread_count_by_level:
                UltraGroupNotifyTestInputDialog queryUltraGroupUnread =
                        new UltraGroupNotifyTestInputDialog(
                                this,
                                UltraGroupNotifyTestInputDialog
                                        .TYPE_QUERY_ULTRA_GROUP_UNREAD_COUNT_BY_LEVEL);
                queryUltraGroupUnread
                        .getSureView()
                        .setOnClickListener(
                                v1 -> {
                                    String types =
                                            queryUltraGroupUnread.getEtTagId().getText().toString();
                                    String levels =
                                            queryUltraGroupUnread
                                                    .getEtTagName()
                                                    .getText()
                                                    .toString();
                                    if (TextUtils.isEmpty(types)) {
                                        ToastUtils.showToast("请输入正确的值");
                                        return;
                                    }
                                    if (TextUtils.isEmpty(levels)) {
                                        ToastUtils.showToast("请输入正确的值");
                                        return;
                                    }
                                    getLevelUnreadCount(types, levels);
                                    queryUltraGroupUnread.cancel();
                                });

                queryUltraGroupUnread
                        .getCancelView()
                        .setOnClickListener(v12 -> queryUltraGroupUnread.cancel());
                queryUltraGroupUnread.show();
                break;
            case R.id.siv_query_ultra_group_mention_count_by_level:
                UltraGroupNotifyTestInputDialog queryUltraGroupMentionUnread =
                        new UltraGroupNotifyTestInputDialog(
                                this,
                                UltraGroupNotifyTestInputDialog
                                        .TYPE_QUERY_ULTRA_GROUP_MENTION_COUNT_BY_LEVEL);
                queryUltraGroupMentionUnread
                        .getSureView()
                        .setOnClickListener(
                                v1 -> {
                                    String types =
                                            queryUltraGroupMentionUnread
                                                    .getEtTagId()
                                                    .getText()
                                                    .toString();
                                    String levels =
                                            queryUltraGroupMentionUnread
                                                    .getEtTagName()
                                                    .getText()
                                                    .toString();
                                    if (TextUtils.isEmpty(types)) {
                                        ToastUtils.showToast("请输入正确的值");
                                        return;
                                    }
                                    if (TextUtils.isEmpty(levels)) {
                                        ToastUtils.showToast("请输入正确的值");
                                        return;
                                    }
                                    getLevelMentionUnreadCount(types, levels);
                                    queryUltraGroupMentionUnread.cancel();
                                });

                queryUltraGroupMentionUnread
                        .getCancelView()
                        .setOnClickListener(v12 -> queryUltraGroupMentionUnread.cancel());
                queryUltraGroupMentionUnread.show();
                break;
            case R.id.siv_query_target_ultra_group_unread_count_by_level:
                UltraGroupNotifyTestInputDialog queryTargetUltraGroupUnread =
                        new UltraGroupNotifyTestInputDialog(
                                this,
                                UltraGroupNotifyTestInputDialog
                                        .TYPE_QUERY_TARGET_ULTRA_GROUP_UNREAD_COUNT_BY_LEVEL);
                queryTargetUltraGroupUnread
                        .getSureView()
                        .setOnClickListener(
                                v1 -> {
                                    String targetID =
                                            queryTargetUltraGroupUnread
                                                    .getEtTargetId()
                                                    .getText()
                                                    .toString();
                                    String levels =
                                            queryTargetUltraGroupUnread
                                                    .getEtTagName()
                                                    .getText()
                                                    .toString();
                                    if (TextUtils.isEmpty(targetID)) {
                                        ToastUtils.showToast("请输入正确的值");
                                        return;
                                    }
                                    if (TextUtils.isEmpty(levels)) {
                                        ToastUtils.showToast("请输入正确的值");
                                        return;
                                    }
                                    getTargetLevelUnreadCount(
                                            conversationIdentifier.getTargetId(), levels);
                                    queryTargetUltraGroupUnread.cancel();
                                });

                queryTargetUltraGroupUnread
                        .getCancelView()
                        .setOnClickListener(v12 -> queryTargetUltraGroupUnread.cancel());
                queryTargetUltraGroupUnread.show();
                break;
            case R.id.siv_query_target_ultra_group_mention_count_by_level:
                UltraGroupNotifyTestInputDialog queryTargetUltraGroupMentionUnread =
                        new UltraGroupNotifyTestInputDialog(
                                this,
                                UltraGroupNotifyTestInputDialog
                                        .TYPE_QUERY_TARGET_ULTRA_GROUP_MENTION_COUNT_BY_LEVEL);
                queryTargetUltraGroupMentionUnread
                        .getSureView()
                        .setOnClickListener(
                                v1 -> {
                                    String targetID =
                                            queryTargetUltraGroupMentionUnread
                                                    .getEtTargetId()
                                                    .getText()
                                                    .toString();
                                    String levels =
                                            queryTargetUltraGroupMentionUnread
                                                    .getEtTagName()
                                                    .getText()
                                                    .toString();
                                    if (TextUtils.isEmpty(targetID)) {
                                        ToastUtils.showToast("请输入正确的值");
                                        return;
                                    }
                                    if (TextUtils.isEmpty(levels)) {
                                        ToastUtils.showToast("请输入正确的值");
                                        return;
                                    }
                                    getTargetLevelMentionUnreadCount(
                                            conversationIdentifier.getTargetId(), levels);
                                    queryTargetUltraGroupMentionUnread.cancel();
                                });

                queryTargetUltraGroupMentionUnread
                        .getCancelView()
                        .setOnClickListener(v12 -> queryTargetUltraGroupMentionUnread.cancel());
                queryTargetUltraGroupMentionUnread.show();
                break;
            case R.id.siv_query_target_ultra_group_all_channel_message:
                SealSearchUltraGroupActivity.start(
                        this,
                        SealSearchUltraGroupActivity.TYPE_TARGET,
                        ConversationIdentifier.obtain(
                                conversationIdentifier.getType(),
                                conversationIdentifier.getTargetId(),
                                ""));
                break;
            case R.id.siv_query_target_ultra_group_cur_channel_message:
                SealSearchUltraGroupActivity.start(
                        this, SealSearchUltraGroupActivity.TYPE_TARGET, conversationIdentifier);
                break;
            default:
                break;
        }
    }

    private void getTargetLevelMentionUnreadCount(String targetId, String levels) {
        List<IRongCoreEnum.PushNotificationLevel> LevelList = new ArrayList<>();
        String[] lvs = levels.split(",");
        for (String lv : lvs) {
            LevelList.add(IRongCoreEnum.PushNotificationLevel.setValue(Integer.parseInt(lv)));
        }

        RLog.e("ULTRA_TEST", "getTargetLevelMentionUnreadCount targetId = " + targetId);
        RLog.e("ULTRA_TEST", "getTargetLevelMentionUnreadCount LevelList = " + LevelList);

        ChannelClient.getInstance()
                .getUltraGroupUnreadMentionedCount(
                        targetId,
                        LevelList,
                        new IRongCoreCallback.ResultCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer integer) {
                                runOnUiThread(
                                        () ->
                                                ToastUtils.showToast(
                                                        "getTargetLevelMentionUnreadCount :"
                                                                + integer));
                            }

                            @Override
                            public void onError(IRongCoreEnum.CoreErrorCode e) {
                                runOnUiThread(
                                        () ->
                                                ToastUtils.showToast(
                                                        "getTargetLevelMentionUnreadCount :"
                                                                + e.getValue()));
                            }
                        });
    }

    private void getTargetLevelUnreadCount(String targetId, String levels) {
        List<IRongCoreEnum.PushNotificationLevel> LevelList = new ArrayList<>();
        String[] lvs = levels.split(",");
        for (String lv : lvs) {
            LevelList.add(IRongCoreEnum.PushNotificationLevel.setValue(Integer.parseInt(lv)));
        }

        RLog.e("ULTRA_TEST", "getTargetLevelUnreadCount typeList = " + targetId);
        RLog.e("ULTRA_TEST", "getTargetLevelUnreadCount LevelList = " + LevelList);

        ChannelClient.getInstance()
                .getUltraGroupUnreadCount(
                        targetId,
                        LevelList,
                        new IRongCoreCallback.ResultCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer integer) {
                                runOnUiThread(
                                        () ->
                                                ToastUtils.showToast(
                                                        "getTargetLevelUnreadCount :" + integer));
                            }

                            @Override
                            public void onError(IRongCoreEnum.CoreErrorCode e) {
                                runOnUiThread(
                                        () ->
                                                ToastUtils.showToast(
                                                        "getTargetLevelUnreadCount :"
                                                                + e.getValue()));
                            }
                        });
    }

    private void getLevelMentionUnreadCount(String types, String levels) {
        List<Conversation.ConversationType> typeList = new ArrayList<>();
        List<IRongCoreEnum.PushNotificationLevel> LevelList = new ArrayList<>();
        String[] tps = types.split(",");
        for (String tp : tps) {
            typeList.add(Conversation.ConversationType.setValue(Integer.parseInt(tp)));
        }
        String[] lvs = levels.split(",");
        for (String lv : lvs) {
            LevelList.add(IRongCoreEnum.PushNotificationLevel.setValue(Integer.parseInt(lv)));
        }

        RLog.e("ULTRA_TEST", "getLevelMentionUnreadCount typeList = " + typeList);
        RLog.e("ULTRA_TEST", "getLevelMentionUnreadCount LevelList = " + LevelList);

        ChannelClient.getInstance()
                .getUnreadMentionedCount(
                        typeList,
                        LevelList,
                        new IRongCoreCallback.ResultCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer integer) {
                                runOnUiThread(
                                        () ->
                                                ToastUtils.showToast(
                                                        "getUnreadMentionedCount :" + integer));
                            }

                            @Override
                            public void onError(IRongCoreEnum.CoreErrorCode e) {
                                runOnUiThread(
                                        () ->
                                                ToastUtils.showToast(
                                                        "getUnreadMentionedCount :"
                                                                + e.getValue()));
                            }
                        });
    }

    private void getLevelUnreadCount(String types, String levels) {
        List<Conversation.ConversationType> typeList = new ArrayList<>();
        List<IRongCoreEnum.PushNotificationLevel> LevelList = new ArrayList<>();
        String[] tps = types.split(",");
        for (String tp : tps) {
            typeList.add(Conversation.ConversationType.setValue(Integer.parseInt(tp)));
        }
        String[] lvs = levels.split(",");
        for (String lv : lvs) {
            LevelList.add(IRongCoreEnum.PushNotificationLevel.setValue(Integer.parseInt(lv)));
        }

        RLog.e("ULTRA_TEST", "typeList = " + typeList);
        RLog.e("ULTRA_TEST", "LevelList = " + LevelList);

        ChannelClient.getInstance()
                .getUnreadCount(
                        typeList,
                        LevelList,
                        new IRongCoreCallback.ResultCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer integer) {
                                runOnUiThread(
                                        () ->
                                                ToastUtils.showToast(
                                                        "getLevelUnreadCount :" + integer));
                            }

                            @Override
                            public void onError(IRongCoreEnum.CoreErrorCode e) {
                                runOnUiThread(
                                        () ->
                                                ToastUtils.showToast(
                                                        "getLevelUnreadCount :" + e.getValue()));
                            }
                        });
    }

    private void setUltraGroupConversationChannelDefaultNotificationLevel(
            String targetId, String level, String channel) {
        ChannelClient.getInstance()
                .setUltraGroupConversationChannelDefaultNotificationLevel(
                        targetId,
                        channel,
                        IRongCoreEnum.PushNotificationLevel.setValue(Integer.parseInt(level)),
                        new IRongCoreCallback.OperationCallback() {
                            @Override
                            public void onSuccess() {
                                runOnUiThread(() -> ToastUtils.showToast("set level success"));
                            }

                            @Override
                            public void onError(IRongCoreEnum.CoreErrorCode coreErrorCode) {
                                runOnUiThread(
                                        () ->
                                                ToastUtils.showToast(
                                                        "set level error :"
                                                                + coreErrorCode.getValue()));
                            }
                        });
    }

    private void setUltraGroupConversationDefaultNotificationLevel(String targetId, String level) {
        ChannelClient.getInstance()
                .setUltraGroupConversationDefaultNotificationLevel(
                        targetId,
                        IRongCoreEnum.PushNotificationLevel.setValue(Integer.parseInt(level)),
                        new IRongCoreCallback.OperationCallback() {
                            @Override
                            public void onSuccess() {
                                runOnUiThread(() -> ToastUtils.showToast("set level success"));
                            }

                            @Override
                            public void onError(IRongCoreEnum.CoreErrorCode coreErrorCode) {
                                runOnUiThread(
                                        () ->
                                                ToastUtils.showToast(
                                                        "set level error :"
                                                                + coreErrorCode.getValue()));
                            }
                        });
    }

    private void queryUltraGroupAllMentionUnreadCount() {
        ChannelClient.getInstance()
                .getUltraGroupAllUnreadMentionedCount(
                        new IRongCoreCallback.ResultCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer integer) {
                                runOnUiThread(
                                        () -> ToastUtils.showToast("all unread count :" + integer));
                            }

                            @Override
                            public void onError(IRongCoreEnum.CoreErrorCode e) {
                                runOnUiThread(
                                        () -> ToastUtils.showToast("error : " + e.getValue()));
                            }
                        });
    }

    private void queryUltraGroupAllUnreadCount() {
        ChannelClient.getInstance()
                .getUltraGroupAllUnreadCount(
                        new IRongCoreCallback.ResultCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer integer) {
                                runOnUiThread(
                                        () -> ToastUtils.showToast("all unread count :" + integer));
                            }

                            @Override
                            public void onError(IRongCoreEnum.CoreErrorCode e) {
                                runOnUiThread(
                                        () -> ToastUtils.showToast("error : " + e.getValue()));
                            }
                        });
    }

    private void queryUltraUnreadCount(String targetId) {
        ChannelClient.getInstance()
                .getUltraGroupUnreadCount(
                        targetId,
                        new IRongCoreCallback.ResultCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer integer) {
                                runOnUiThread(
                                        () -> ToastUtils.showToast("unread count :" + integer));
                            }

                            @Override
                            public void onError(IRongCoreEnum.CoreErrorCode e) {
                                runOnUiThread(
                                        () -> ToastUtils.showToast("error : " + e.getValue()));
                            }
                        });
    }

    private void getUltraGroupConversationChannelDefaultNotificationLevel(
            String targetId, String channel) {
        ChannelClient.getInstance()
                .getUltraGroupConversationChannelDefaultNotificationLevel(
                        targetId,
                        channel,
                        new IRongCoreCallback.ResultCallback<
                                IRongCoreEnum.PushNotificationLevel>() {
                            @Override
                            public void onSuccess(IRongCoreEnum.PushNotificationLevel level) {
                                runOnUiThread(
                                        () ->
                                                ToastUtils.showToast(
                                                        "success level :" + level.getValue()));
                            }

                            @Override
                            public void onError(IRongCoreEnum.CoreErrorCode e) {
                                runOnUiThread(
                                        () -> ToastUtils.showToast("error : " + e.getValue()));
                            }
                        });
    }

    private void getUltraGroupConversationDefaultNotificationLevel(String targetId) {
        ChannelClient.getInstance()
                .getUltraGroupConversationDefaultNotificationLevel(
                        targetId,
                        new IRongCoreCallback.ResultCallback<
                                IRongCoreEnum.PushNotificationLevel>() {
                            @Override
                            public void onSuccess(IRongCoreEnum.PushNotificationLevel level) {
                                runOnUiThread(
                                        () ->
                                                ToastUtils.showToast(
                                                        "success level :" + level.getValue()));
                            }

                            @Override
                            public void onError(IRongCoreEnum.CoreErrorCode e) {
                                runOnUiThread(
                                        () -> ToastUtils.showToast("error : " + e.getValue()));
                            }
                        });
    }

    private void getConversationTypeNotificationLevel(String type) {
        ChannelClient.getInstance()
                .getConversationTypeNotificationLevel(
                        Conversation.ConversationType.setValue(Integer.parseInt(type)),
                        new IRongCoreCallback.ResultCallback<
                                IRongCoreEnum.PushNotificationLevel>() {
                            @Override
                            public void onSuccess(IRongCoreEnum.PushNotificationLevel level) {
                                runOnUiThread(
                                        () ->
                                                ToastUtils.showToast(
                                                        "success level :" + level.getValue()));
                            }

                            @Override
                            public void onError(IRongCoreEnum.CoreErrorCode e) {
                                runOnUiThread(
                                        () -> ToastUtils.showToast("error : " + e.getValue()));
                            }
                        });
    }

    private void setConversationTypeNotificationLevel(String type, String level) {
        ChannelClient.getInstance()
                .setConversationTypeNotificationLevel(
                        Conversation.ConversationType.setValue(Integer.parseInt(type)),
                        IRongCoreEnum.PushNotificationLevel.setValue(Integer.parseInt(level)),
                        new IRongCoreCallback.OperationCallback() {
                            @Override
                            public void onSuccess() {
                                runOnUiThread(() -> ToastUtils.showToast("success"));
                            }

                            @Override
                            public void onError(IRongCoreEnum.CoreErrorCode coreErrorCode) {
                                runOnUiThread(
                                        () -> ToastUtils.showToast("error : " + coreErrorCode));
                            }
                        });
    }

    private void getConversationNotificationLevel(String type, String targetId) {
        ChannelClient.getInstance()
                .getConversationNotificationLevel(
                        Conversation.ConversationType.setValue(Integer.parseInt(type)),
                        targetId,
                        new IRongCoreCallback.ResultCallback<
                                IRongCoreEnum.PushNotificationLevel>() {
                            @Override
                            public void onSuccess(IRongCoreEnum.PushNotificationLevel level) {
                                runOnUiThread(
                                        () ->
                                                ToastUtils.showToast(
                                                        "success level :" + level.getValue()));
                            }

                            @Override
                            public void onError(IRongCoreEnum.CoreErrorCode e) {
                                runOnUiThread(
                                        () -> ToastUtils.showToast("error : " + e.getValue()));
                            }
                        });
    }

    private void setConversationNotificationLevel(String type, String targetId, String level) {
        ChannelClient.getInstance()
                .setConversationNotificationLevel(
                        Conversation.ConversationType.setValue(Integer.parseInt(type)),
                        targetId,
                        IRongCoreEnum.PushNotificationLevel.setValue(Integer.parseInt(level)),
                        new IRongCoreCallback.OperationCallback() {
                            @Override
                            public void onSuccess() {
                                runOnUiThread(() -> ToastUtils.showToast("success"));
                            }

                            @Override
                            public void onError(IRongCoreEnum.CoreErrorCode coreErrorCode) {
                                runOnUiThread(
                                        () -> ToastUtils.showToast("error :" + coreErrorCode));
                            }
                        });
    }

    private void getConversationChannelNotificationLevel(
            String type, String targetId, String channel) {
        ChannelClient.getInstance()
                .getConversationChannelNotificationLevel(
                        Conversation.ConversationType.setValue(Integer.parseInt(type)),
                        targetId,
                        channel,
                        new IRongCoreCallback.ResultCallback<
                                IRongCoreEnum.PushNotificationLevel>() {
                            @Override
                            public void onSuccess(IRongCoreEnum.PushNotificationLevel level) {
                                runOnUiThread(
                                        () ->
                                                ToastUtils.showToast(
                                                        "success level " + level.getValue()));
                            }

                            @Override
                            public void onError(IRongCoreEnum.CoreErrorCode e) {
                                runOnUiThread(() -> ToastUtils.showToast("error: " + e));
                            }
                        });
    }

    private void setConversationChannelNotificationLevel(
            String type, String targetId, String channel, String level) {
        ChannelClient.getInstance()
                .setConversationChannelNotificationLevel(
                        Conversation.ConversationType.setValue(Integer.parseInt(type)),
                        targetId,
                        channel,
                        IRongCoreEnum.PushNotificationLevel.setValue(Integer.parseInt(level)),
                        new IRongCoreCallback.OperationCallback() {
                            @Override
                            public void onSuccess() {
                                runOnUiThread(() -> ToastUtils.showToast("success"));
                            }

                            @Override
                            public void onError(IRongCoreEnum.CoreErrorCode coreErrorCode) {
                                runOnUiThread(
                                        () -> ToastUtils.showToast("error : " + coreErrorCode));
                            }
                        });
    }

    private void getNotificationQuietHours() {
        ChannelClient.getInstance()
                .getNotificationQuietHoursLevel(
                        new IRongCoreCallback.GetNotificationQuietHoursCallbackEx() {
                            @Override
                            public void onSuccess(
                                    String startTime,
                                    int spanMinutes,
                                    IRongCoreEnum.PushNotificationQuietHoursLevel level) {
                                runOnUiThread(
                                        () ->
                                                ToastUtils.showToast(
                                                        "success"
                                                                + "startTime : "
                                                                + startTime
                                                                + " , spanMinutes : "
                                                                + spanMinutes
                                                                + ", level : "
                                                                + level.toString()));
                            }

                            @Override
                            public void onError(IRongCoreEnum.CoreErrorCode coreErrorCode) {
                                runOnUiThread(
                                        () -> ToastUtils.showToast("error : " + coreErrorCode));
                            }
                        });
    }

    private void removeNotificationQuietHours() {
        ChannelClient.getInstance()
                .removeNotificationQuietHours(
                        new IRongCoreCallback.OperationCallback() {
                            @Override
                            public void onSuccess() {
                                runOnUiThread(
                                        () ->
                                                ToastUtils.showToast(
                                                        "removeNotificationQuietHours success"));
                            }

                            @Override
                            public void onError(IRongCoreEnum.CoreErrorCode coreErrorCode) {
                                runOnUiThread(
                                        () ->
                                                ToastUtils.showToast(
                                                        "removeNotificationQuietHours error : "
                                                                + coreErrorCode));
                            }
                        });
    }

    private void setNotificationQuietHoursLevel(String time, String timeSpan, String level) {
        ChannelClient.getInstance()
                .setNotificationQuietHoursLevel(
                        time,
                        Integer.parseInt(timeSpan),
                        IRongCoreEnum.PushNotificationQuietHoursLevel.setValue(
                                Integer.parseInt(level)),
                        new IRongCoreCallback.OperationCallback() {
                            @Override
                            public void onSuccess() {
                                runOnUiThread(
                                        () ->
                                                ToastUtils.showToast(
                                                        "setNotificationQuietHoursLevel success"));
                            }

                            @Override
                            public void onError(IRongCoreEnum.CoreErrorCode coreErrorCode) {
                                runOnUiThread(
                                        () ->
                                                ToastUtils.showToast(
                                                        "setNotificationQuietHoursLevel error : "
                                                                + coreErrorCode));
                            }
                        });
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
        ChannelClient.getInstance()
                .getConversationNotificationStatus(
                        conversationIdentifier.getType(),
                        conversationIdentifier.getTargetId(),
                        conversationIdentifier.getChannelId(),
                        new IRongCoreCallback.ResultCallback<
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
                            public void onError(IRongCoreEnum.CoreErrorCode e) {
                                runOnUiThread(() -> ToastUtils.showToast("获取失败-" + e.getValue()));
                            }
                        });
    }
}
