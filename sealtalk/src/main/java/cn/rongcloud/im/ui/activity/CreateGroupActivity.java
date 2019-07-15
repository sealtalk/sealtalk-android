package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.Constant;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.ui.dialog.SelectPictureBottomDialog;
import cn.rongcloud.im.ui.view.SealTitleBar;
import cn.rongcloud.im.ui.widget.ClearWriteEditText;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.viewmodel.CreateGroupViewModel;
import cn.rongcloud.im.utils.log.SLog;
import io.rong.imkit.RongIM;
import io.rong.imkit.emoticon.AndroidEmoji;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imlib.model.Conversation;

/**
 * 创建群组
 */
public class CreateGroupActivity extends TitleBaseActivity implements View.OnClickListener {
    private final String TAG = "CreateGroupActivity";

    private ClearWriteEditText groupNameEt;
    private AsyncImageView groupPortraitIv;

    private Uri groupPortraitUri;
    private CreateGroupViewModel createGroupViewModel;

    private List<String> memberList;
    private String createGroupName;
    /**
     * 是否返回创建群组结果
     */
    private boolean isReturnResult;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SealTitleBar titleBar = getTitleBar();
        titleBar.setTitle(R.string.seal_main_title_create_group);

        setContentView(R.layout.main_activity_create_group);


        Intent intent = getIntent();
        if (intent == null) {
            SLog.e(TAG, "intent is null, finish " + TAG);
            return;
        }

        memberList = intent.getStringArrayListExtra(IntentExtra.LIST_STR_ID_LIST);
        isReturnResult = intent.getBooleanExtra(IntentExtra.BOOLEAN_CREATE_GROUP_RETURN_RESULT, false);
        if (memberList == null || memberList.size() == 0) {
            SLog.e(TAG, "memberList is 0, finish" + TAG);
            return;
        }
        //加入自己
        memberList.add(0,RongIM.getInstance().getCurrentUserId());

        initView();
        initViewModel();
    }

    private void initView() {
        groupNameEt = findViewById(R.id.main_et_create_group_name);
        groupNameEt.setHint(getString(R.string.profile_group_name_word_limit_format, Constant.GROUP_NAME_MIN_LENGTH, Constant.GROUP_NAME_MAX_LENGTH));
        groupPortraitIv = findViewById(R.id.main_iv_create_group_portrait);
        groupPortraitIv.setOnClickListener(this);
        findViewById(R.id.main_btn_confirm_create).setOnClickListener(this);
    }

    private void initViewModel() {
        createGroupViewModel = ViewModelProviders.of(this).get(CreateGroupViewModel.class);

        createGroupViewModel.getCreateGroupResult().observe(this, resource -> {
            if (resource.status == Status.SUCCESS) {
                // 处理创建群组结果
                processCreateResult(resource.data);
            } else if (resource.status == Status.ERROR) {
                // 当有结果时代表群组创建成功，但上传图片失败
                if (resource.data != null) {
                    // 处理创建群组结果
                    processCreateResult(resource.data);
                }

                ToastUtils.showToast(resource.message);
            }
        });
    }

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

    /**
     * 显示选择图片对话框
     */
    private void showSelectPortraitDialog() {
        SelectPictureBottomDialog.Builder builder = new SelectPictureBottomDialog.Builder();
        builder.setOnSelectPictureListener(uri -> {
            SLog.d(TAG, "select picture, uri:" + uri);
            groupPortraitIv.setResource(uri);
            groupPortraitUri = uri;
        });
        SelectPictureBottomDialog dialog = builder.build();
        dialog.show(getSupportFragmentManager(), null);
    }

    /**
     * 创建群组
     */
    private void createGroup() {
        String groupName = groupNameEt.getText().toString();
        groupName = groupName.trim();

        if (groupName.length() < Constant.GROUP_NAME_MIN_LENGTH || groupName.length() > Constant.GROUP_NAME_MAX_LENGTH) {
            ToastUtils.showToast(getString(R.string.profile_group_name_word_limit_format, Constant.GROUP_NAME_MIN_LENGTH, Constant.GROUP_NAME_MAX_LENGTH));
            return;
        }

        if (AndroidEmoji.isEmoji(groupName) && groupName.length() < Constant.GROUP_NAME_EMOJI_MIN_LENGTH) {
            ToastUtils.showToast(getString(R.string.profile_group_name_emoji_too_short));
            return;
        }

        createGroupName = groupName;

        // 重命名群名称
        createGroupViewModel.createGroup(groupName, groupPortraitUri, memberList);
    }

    /**
     * 处理创建结果
     *
     * @param groupId
     */
    private void processCreateResult(String groupId){
        // 返回结果时候设置结果并结束
        if(isReturnResult){
            Intent resultIntent = new Intent();
            resultIntent.putExtra(IntentExtra.GROUP_ID, groupId);
            setResult(RESULT_OK, resultIntent);
            finish();
        }else {
            //不返回结果时，创建成功后跳转到群组聊天中
            toGroupChat(groupId);
        }
    }

    /**
     * 跳转到群组聊天
     */
    private void toGroupChat(String groupId) {
        RongIM.getInstance().startConversation(this, Conversation.ConversationType.GROUP, groupId, createGroupName);
        finish();
    }
}
