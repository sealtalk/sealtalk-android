package cn.rongcloud.im.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.lifecycle.ViewModelProviders;
import cn.rongcloud.im.R;
import cn.rongcloud.im.common.Constant;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.ui.view.SealTitleBar;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.utils.log.SLog;
import cn.rongcloud.im.viewmodel.UltraGroupViewModel;
import io.rong.imkit.IMCenter;
import io.rong.imkit.conversation.extension.component.emoticon.AndroidEmoji;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.ConversationIdentifier;
import io.rong.imlib.model.Message;
import io.rong.message.InformationNotificationMessage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 创建群组 */
public class CreateChannelActivity extends TitleBaseActivity implements View.OnClickListener {
    private final String TAG = "CreateChannelActivity";

    private EditText groupNameEt;
    private UltraGroupViewModel ultraGroupViewModel;
    private String groupId;
    private String groupName;
    private AppCompatCheckBox cbIsPrivateChannel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SealTitleBar titleBar = getTitleBar();
        titleBar.setTitle(R.string.seal_main_title_create_channel);

        setContentView(R.layout.main_activity_create_channel);

        Intent intent = getIntent();
        if (intent == null) {
            SLog.e(TAG, "intent is null, finish " + TAG);
            return;
        }
        groupId = intent.getStringExtra("groupId");

        initView();
        initViewModel();
    }

    private void initView() {
        groupNameEt = findViewById(R.id.main_et_create_group_name);
        cbIsPrivateChannel = findViewById(R.id.cb_private_channel);
        groupNameEt.setHint(
                getString(
                        R.string.profile_channel_name_word_limit_format,
                        Constant.GROUP_NAME_MIN_LENGTH,
                        Constant.GROUP_NAME_MAX_LENGTH));
        groupNameEt.setFilters(new InputFilter[] {emojiFilter, new InputFilter.LengthFilter(10)});
        findViewById(R.id.main_btn_confirm_create).setOnClickListener(this);
    }

    private void initViewModel() {
        ultraGroupViewModel = ViewModelProviders.of(this).get(UltraGroupViewModel.class);

        ultraGroupViewModel
                .getChannelCreateResult()
                .observe(
                        this,
                        resource -> {
                            if (resource.status == Status.LOADING) {
                                return;
                            }
                            if (resource.status == Status.SUCCESS) {
                                if (resource.data == null) {
                                    ToastUtils.showToast("没有数据");
                                    return;
                                }
                                processCreateResult(resource.data);
                            } else if (resource.status == Status.ERROR) {
                                ToastUtils.showToast(resource.message);
                            }
                        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.main_btn_confirm_create) {
            createGroup();
        }
    }

    /** 创建群组 */
    private void createGroup() {
        groupName = groupNameEt.getText().toString();
        groupName = groupName.trim();

        if (groupName.length() < Constant.GROUP_NAME_MIN_LENGTH
                || groupName.length() > Constant.GROUP_NAME_MAX_LENGTH) {
            ToastUtils.showToast(
                    getString(
                            R.string.profile_group_name_word_limit_format,
                            Constant.GROUP_NAME_MIN_LENGTH,
                            Constant.GROUP_NAME_MAX_LENGTH));
            return;
        }

        if (AndroidEmoji.isEmoji(groupName)
                && groupName.length() < Constant.GROUP_NAME_EMOJI_MIN_LENGTH) {
            ToastUtils.showToast(getString(R.string.profile_group_name_emoji_too_short));
            return;
        }

        String limitEx = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@①#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Pattern pattern = Pattern.compile(limitEx);
        Matcher m = pattern.matcher(groupName);

        if (m.find()) {
            ToastUtils.showToast(getString(R.string.profile_group_name_invalid_word));
            return;
        }

        ultraGroupViewModel.createChannel(
                groupId,
                groupName,
                cbIsPrivateChannel.isChecked()
                        ? IRongCoreEnum.UltraGroupChannelType.ULTRA_GROUP_CHANNEL_TYPE_PRIVATE
                        : IRongCoreEnum.UltraGroupChannelType.ULTRA_GROUP_CHANNEL_TYPE_PUBLIC);
    }

    /** 处理创建结果 */
    private void processCreateResult(String channelId) {
        toGroupChat(channelId);
    }

    /** 跳转到群组聊天 */
    private void toGroupChat(String channelId) {
        Bundle bundle = new Bundle();
        bundle.putString(RouteUtils.TITLE, groupName);
        RouteUtils.routeToConversationActivity(
                this, ConversationIdentifier.obtainUltraGroup(groupId, channelId), bundle);
        InformationNotificationMessage informationNotificationMessage;
        informationNotificationMessage =
                InformationNotificationMessage.obtain(
                        RongUserInfoManager.getInstance()
                                        .getUserInfo(RongIMClient.getInstance().getCurrentUserId())
                                        .getName()
                                + "创建了频道");

        Message message =
                Message.obtain(
                        ConversationIdentifier.obtainUltraGroup(groupId, channelId),
                        informationNotificationMessage);

        IMCenter.getInstance()
                .sendMessage(
                        message,
                        null,
                        null,
                        new IRongCallback.ISendMessageCallback() {
                            @Override
                            public void onAttached(Message message) {}

                            @Override
                            public void onSuccess(Message message) {}

                            @Override
                            public void onError(
                                    Message message, RongIMClient.ErrorCode errorCode) {}
                        });
        finish();
    }

    /** 表情输入的过滤 */
    InputFilter emojiFilter =
            new InputFilter() {
                final Pattern emoji =
                        Pattern.compile(
                                "[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
                                Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);

                @Override
                public CharSequence filter(
                        CharSequence source,
                        int start,
                        int end,
                        Spanned dest,
                        int dstart,
                        int dend) {
                    Matcher emojiMatcher = emoji.matcher(source);
                    if (emojiMatcher.find()) {
                        ToastUtils.showToast("不支持输入表情");
                        return "";
                    }
                    return null;
                }
            };
}
