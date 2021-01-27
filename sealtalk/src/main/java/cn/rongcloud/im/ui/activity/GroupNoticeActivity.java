package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.Constant;
import cn.rongcloud.im.common.ErrorCode;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.model.GroupNoticeResult;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.ui.dialog.CommonDialog;
import cn.rongcloud.im.ui.view.SealTitleBar;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.viewmodel.GroupNoticeViewModel;
import cn.rongcloud.im.utils.log.SLog;
import io.rong.imkit.conversation.extension.component.emoticon.AndroidEmoji;

/**
 * 群公告发布界面
 */
public class GroupNoticeActivity extends TitleBaseActivity {
    private final String TAG = "GroupNoticeActivity";
    private SealTitleBar titleBar;

    private TextView titleConfirmTv;
    private TextView updateBulletinTv;
    private EditText noticeInputEt;

    private GroupNoticeViewModel groupNoticeViewModel;

    private String groupId;
    private String lastNotice;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        titleBar = getTitleBar();
        titleBar.setTitle(R.string.profile_group_notice);

        setContentView(R.layout.profile_activity_group_notice);

        Intent intent = getIntent();
        if (intent == null) {
            SLog.e(TAG, "intent is null, finish " + TAG);
            finish();
            return;
        }

        groupId = intent.getStringExtra(IntentExtra.STR_TARGET_ID);
        if (groupId == null) {
            SLog.e(TAG, "targetId or conversationType is null, finish" + TAG);
            finish();
            return;
        }

        initView();
        initViewModel();
        setSendEnable(false);
    }

    private void initView() {
        titleConfirmTv = titleBar.getTvRight();
        titleConfirmTv.setText(R.string.common_done);
        noticeInputEt = findViewById(R.id.profile_et_group_notice);
        noticeInputEt.addTextChangedListener(new TextWatcher() {
            private String lastTxt = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String currentTxt = noticeInputEt.getText().toString();
                if(currentTxt.length() > Constant.GROUP_NOTICE_MAX_LENGTH){
                    int selectionStart = noticeInputEt.getSelectionStart() - 1; // 向后退 1 位，减 1
                    noticeInputEt.setText(lastTxt);
                    if(selectionStart <= lastTxt.length()) {
                        noticeInputEt.setSelection(selectionStart);
                    } else {
                        noticeInputEt.setSelection(lastTxt.length());
                    }
                    ToastUtils.showToast(R.string.profile_group_notice_content_over_max_length);
                    return;
                } else {
                    lastTxt = currentTxt;
                }

                // 内容相同时不可确认发送
                if(currentTxt.equals(lastNotice)){
                    setSendEnable(false);
                } else {
                    setSendEnable(true);
                }

                if (s != null) {
                    int start = noticeInputEt.getSelectionStart();
                    int end = noticeInputEt.getSelectionEnd();
                    noticeInputEt.removeTextChangedListener(this);
                    noticeInputEt.setText(AndroidEmoji.ensure(s.toString()));
                    noticeInputEt.addTextChangedListener(this);
                    noticeInputEt.setSelection(start, end);
                }
            }
        });

        updateBulletinTv = findViewById(R.id.profile_tv_update_group_notice_time);

        titleConfirmTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestSendNotice();
            }
        });

        updateBulletin(null);
    }

    private void initViewModel() {
        groupNoticeViewModel = ViewModelProviders.of(this
                , new GroupNoticeViewModel.Factory(this.getApplication(), groupId))
                .get(GroupNoticeViewModel.class);

        // 获取群公告
        groupNoticeViewModel.getGroupNoticeResult().observe(this, new Observer<Resource<GroupNoticeResult>>() {
            @Override
            public void onChanged(Resource<GroupNoticeResult> resource) {
                if(resource.data != null){
                    updateBulletin(resource.data);
                }else if(resource.status == Status.ERROR){
                    if(resource.code != ErrorCode.NO_GROUP_BULLET.getCode()){
                        ToastUtils.showErrorToast(resource.code);
                    }
                }
            }
        }
    );

        // 监听发送群发消息结果
        groupNoticeViewModel.getPublishNoticeResult().observe(this, new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> resource) {
                if (resource.status == Status.SUCCESS) {
                    finish();
                } else if (resource.status == Status.ERROR) {
                    ToastUtils.showToast(resource.message);
                }
            }
        });
    }

    /**
     * 更新群公告
     *
     * @param noticeResult
     */
    private void updateBulletin(GroupNoticeResult noticeResult) {

        long bulletinTime = 0;
        if(noticeResult != null){
            bulletinTime = noticeResult.getTimestamp();
            String bulletin = noticeResult.getContent();
            lastNotice = bulletin;
            noticeInputEt.setText(bulletin);
            noticeInputEt.setSelection(noticeInputEt.length());
        }
        String updateTime = "0000-00-00 00:00:00";
        if(bulletinTime != 0){
            Date date = new Date(bulletinTime);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            updateTime = simpleDateFormat.format(date);
        }

        updateBulletinTv.setText(getString(R.string.profile_group_notice_update_time_format, updateTime));
    }

    /**
     * 设置是否可以发送
     *
     * @param isEnable
     */
    private void setSendEnable(boolean isEnable) {
        if (isEnable) {
            titleConfirmTv.setClickable(true);
            titleConfirmTv.setTextColor(getResources().getColor(R.color.color_black_111F2C));
        } else {
            titleConfirmTv.setClickable(false);
            titleConfirmTv.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
    }

    /**
     * 发布群公关
     */
    private void requestSendNotice() {
        String notice = noticeInputEt.getText().toString();
        String dialogMsg;
        if(notice.equals(lastNotice)){
            ToastUtils.showToast(R.string.profile_group_notice_not_changed, Toast.LENGTH_LONG);
            return;
        }

        if(TextUtils.isEmpty(notice)){
            dialogMsg = getString(R.string.profile_group_notice_clear_confirm);
        }else {
            dialogMsg = getString(R.string.profile_group_notice_post_confirm);
        }
        CommonDialog dialog = new CommonDialog.Builder()
                .setContentMessage(dialogMsg)
                .setButtonText(R.string.common_publish, R.string.common_cancel)
                .setDialogButtonClickListener(new CommonDialog.OnDialogButtonClickListener() {
                    @Override
                    public void onPositiveClick(View v, Bundle bundle) {
                        groupNoticeViewModel.publishNotice(notice);
                    }

                    @Override
                    public void onNegativeClick(View v, Bundle bundle) {
                    }
                })
                .build();
        dialog.show(getSupportFragmentManager(), null);
    }

}
