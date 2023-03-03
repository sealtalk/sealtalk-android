package cn.rongcloud.im.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import cn.rongcloud.im.R;
import cn.rongcloud.im.common.Constant;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.model.UltraGroupInfo;
import cn.rongcloud.im.ui.dialog.SelectPictureBottomDialog;
import cn.rongcloud.im.ui.view.SealTitleBar;
import cn.rongcloud.im.ultraGroup.UltraGroupManager;
import cn.rongcloud.im.utils.RongGenerate;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.utils.log.SLog;
import cn.rongcloud.im.viewmodel.UltraGroupViewModel;
import io.rong.imkit.IMCenter;
import io.rong.imkit.conversation.extension.component.emoticon.AndroidEmoji;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.ConversationIdentifier;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.Message;
import io.rong.message.InformationNotificationMessage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 创建群组 */
public class CreateUltraGroupActivity extends TitleBaseActivity implements View.OnClickListener {
    private final String TAG = "CreateGroupActivity";

    private EditText groupNameEt;
    private ImageView groupPortraitIv;

    private Uri groupPortraitUri;
    private UltraGroupViewModel createGroupViewModel;

    private String createGroupName;
    private SharedPreferences sharedPreferences;
    /** 是否返回创建群组结果 */
    private boolean isCreatingGroup;

    private String groupName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SealTitleBar titleBar = getTitleBar();
        titleBar.setTitle(R.string.seal_main_title_create_group);

        setContentView(R.layout.main_activity_create_urtra_group);

        Intent intent = getIntent();
        if (intent == null) {
            SLog.e(TAG, "intent is null, finish " + TAG);
            return;
        }

        initView();
        initViewModel();
    }

    private void initView() {
        groupNameEt = findViewById(R.id.main_et_create_group_name);
        groupNameEt.setHint(
                getString(
                        R.string.profile_group_name_word_limit_format,
                        Constant.GROUP_NAME_MIN_LENGTH,
                        Constant.GROUP_NAME_MAX_LENGTH));
        groupNameEt.setFilters(new InputFilter[] {emojiFilter, new InputFilter.LengthFilter(10)});
        groupPortraitIv = findViewById(R.id.main_iv_create_group_portrait);
        groupPortraitIv.setOnClickListener(this);
        findViewById(R.id.main_btn_confirm_create).setOnClickListener(this);
        sharedPreferences = getSharedPreferences("ultra", Context.MODE_PRIVATE);
    }

    private void initViewModel() {
        createGroupViewModel = ViewModelProviders.of(this).get(UltraGroupViewModel.class);

        createGroupViewModel
                .getCreateGroupResult()
                .observe(
                        this,
                        resource -> {
                            if (resource.status == Status.LOADING) {
                                return;
                            }
                            if (resource.status == Status.SUCCESS) {
                                // 处理创建群组结果
                                if (resource.data == null) {
                                    ToastUtils.showToast("没有数据");
                                    return;
                                }
                                processCreateResult(resource.data);
                            } else if (resource.status == Status.ERROR) {
                                // 当有结果时代表群组创建成功，但上传图片失败
                                // 处理创建群组结果
                                ToastUtils.showToast(resource.message);
                            }
                        });

        createGroupViewModel
                .getUploadPortrait()
                .observe(
                        this,
                        resource -> {
                            if (resource.status == Status.LOADING) {
                                return;
                            }
                            if (resource.status == Status.SUCCESS) {
                                // 处理创建群组结果
                                if (resource.data == null) {
                                    ToastUtils.showToast("没有数据");
                                    return;
                                }
                                createGroupViewModel.createUltraGroup(
                                        groupName, Uri.parse(resource.data), "memberList");
                            } else if (resource.status == Status.ERROR) {
                                // 当有结果时代表群组创建成功，但上传图片失败
                                // 处理创建群组结果
                                ToastUtils.showToast(resource.message);
                            }
                        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_iv_create_group_portrait:
                showSelectPortraitDialog();
                break;
            case R.id.main_btn_confirm_create:
                createGroup();
                break;
            default:
                break;
        }
    }

    /** 显示选择图片对话框 */
    private void showSelectPortraitDialog() {
        SelectPictureBottomDialog.Builder builder = new SelectPictureBottomDialog.Builder();
        builder.setOnSelectPictureListener(
                uri -> {
                    SLog.d(TAG, "select picture, uri:" + uri);
                    groupPortraitIv.setImageURI(null);
                    groupPortraitIv.setImageURI(uri);
                    groupPortraitUri = uri;
                });
        SelectPictureBottomDialog dialog = builder.build();
        dialog.show(getSupportFragmentManager(), null);
    }

    /** 创建群组 */
    private void createGroup() {
        if (isCreatingGroup) return;

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

        createGroupName = groupName;

        // 标记创建群组状态
        isCreatingGroup = true;
        if (groupPortraitUri == null || TextUtils.isEmpty(groupPortraitUri.toString())) {
            createGroupViewModel.createUltraGroup(groupName, groupPortraitUri, "memberList");
        } else {
            createGroupViewModel.nextToUploadPortraitResult(groupPortraitUri);
        }
    }

    /** 处理创建结果 */
    private void processCreateResult(String groupId) {
        // 返回结果时候设置结果并结束
        UltraGroupInfo ultraGroupInfo = new UltraGroupInfo();
        ultraGroupInfo.creatorId = RongIMClient.getInstance().getCurrentUserId();
        ultraGroupInfo.groupId = groupId;
        ultraGroupInfo.groupName = createGroupName;
        if (groupPortraitUri != null) {
            ultraGroupInfo.portraitUri = groupPortraitUri.toString();
        }
        ultraGroupInfo.summary = "test";
        UltraGroupManager.getInstance().notifyGroupCreate(ultraGroupInfo);
        finish();
        // 不返回结果时，创建成功后跳转到群组聊天中
        toGroupChat(groupId);
    }

    /** 跳转到群组聊天 */
    private void toGroupChat(String groupId) {
        Bundle bundle = new Bundle();
        bundle.putString(RouteUtils.TITLE, "综合");
        // RouteUtils.routeToConversationActivity(this, Conversation.ConversationType.ULTRA_GROUP,
        // groupId, "default", bundle);
        InformationNotificationMessage informationNotificationMessage;
        long serverTime = System.currentTimeMillis() - RongIMClient.getInstance().getDeltaTime();
        informationNotificationMessage =
                InformationNotificationMessage.obtain(
                        RongUserInfoManager.getInstance()
                                        .getUserInfo(RongIMClient.getInstance().getCurrentUserId())
                                        .getName()
                                + "创建超级群成功");

        IMCenter.getInstance()
                .insertOutgoingMessage(
                        ConversationIdentifier.obtainUltraGroup(groupId, "default"),
                        Message.SentStatus.SENT,
                        informationNotificationMessage,
                        serverTime,
                        new RongIMClient.ResultCallback<Message>() {
                            @Override
                            public void onSuccess(Message message) {
                                groupPortraitUri =
                                        Uri.parse(
                                                RongGenerate.generateDefaultAvatar(
                                                        CreateUltraGroupActivity.this,
                                                        "default",
                                                        "综合"));
                                sharedPreferences.edit().clear().commit();
                                RongUserInfoManager.getInstance()
                                        .refreshGroupInfoCache(
                                                new Group(
                                                        groupId + "default",
                                                        "综合",
                                                        groupPortraitUri));
                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode e) {}
                        });
        finish();
    }

    /** 表情输入的过滤 */
    InputFilter emojiFilter =
            new InputFilter() {
                final Pattern emoji =
                        Pattern.compile(
                                "[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
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
