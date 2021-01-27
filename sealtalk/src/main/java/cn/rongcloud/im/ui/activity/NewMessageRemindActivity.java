package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import cn.rongcloud.im.R;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.model.GetPokeResult;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.ui.view.SettingItemView;
import cn.rongcloud.im.viewmodel.NewMessageViewModel;

/**
 * 新消息提醒
 */
public class NewMessageRemindActivity extends TitleBaseActivity {
    private NewMessageViewModel newMessageViewModel;
    private SettingItemView remindSiv;  // 接受新消息通知
    private SettingItemView detailSiv;  // 推送消息显示详情
    private SettingItemView noticeSiv;  // 消息免打扰
    private SettingItemView pokeSiv;    // 接受戳一下消息

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message_remind);
        initView();
        initViewModel();
    }

    /**
     * 初始化布局
     */
    private void initView() {
        getTitleBar().setTitle(R.string.seal_mine_set_account_new_message_show);
        remindSiv = findViewById(R.id.siv_remind);
        detailSiv = findViewById(R.id.siv_detail);
        noticeSiv = findViewById(R.id.siv_notice);
        pokeSiv = findViewById(R.id.siv_poke);

        remindSiv.setSwitchCheckListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                noticeSiv.setEnabled(isChecked);
                setRemindStatus(isChecked);
            }
        });

        detailSiv.setSwitchCheckListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setNoticeDetail(isChecked);
            }
        });

        noticeSiv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NewMessageRemindActivity.this, MessageDonotDisturbSettingActivity.class));
            }
        });

        pokeSiv.setSwitchCheckListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setReceivePokeMessage(isChecked);
            }
        });

    }

    /**
     * 初始话ViModel
     */
    private void initViewModel() {
        newMessageViewModel = ViewModelProviders.of(this).get(NewMessageViewModel.class);

        // Remind 通知状态
        newMessageViewModel.getRemindStatus().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean status) {
                remindSiv.setChecked(status);
                noticeSiv.setEnabled(status);
            }
        });

        // 推送消息通知详情状态
        newMessageViewModel.getPushMsgDetailStatus().observe(this, new Observer<Resource<Boolean>>() {
            @Override
            public void onChanged(Resource<Boolean> resource) {
                if (resource.status == Status.SUCCESS) {
                    Boolean isDetailStatus = resource.data;
                    if (isDetailStatus != null) {
                        detailSiv.setCheckedImmediatelyWithOutEvent(isDetailStatus);
                    }
                } else if (resource.status == Status.ERROR) {
                    detailSiv.setCheckedImmediatelyWithOutEvent(!detailSiv.isChecked());
                    showToast(resource.message);
                }
            }
        });

        // 获取接受戳一下消息状态结果
        newMessageViewModel.getReceivePokeMsgStatusResult().observe(this, new Observer<Resource<GetPokeResult>>() {
            @Override
            public void onChanged(Resource<GetPokeResult> resultResource) {
                if (resultResource.status == Status.SUCCESS) {
                    GetPokeResult data = resultResource.data;
                    if (data != null) {
                        pokeSiv.setCheckedImmediatelyWithOutEvent(data.isReceivePokeMessage());
                    }
                } else if (resultResource.status == Status.LOADING) {
                    pokeSiv.setCheckedImmediatelyWithOutEvent(IMManager.getInstance().getReceivePokeMessageStatus());
                }
            }
        });

        // 获取设置戳一下消息状态结果
        newMessageViewModel.getSetReceivePokeMessageStatusResult().observe(this, new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> resource) {
                if (resource.status == Status.SUCCESS) {
                } else if (resource.status == Status.ERROR) {
                    pokeSiv.setCheckedImmediatelyWithOutEvent(!pokeSiv.isChecked());
                    showToast(resource.message);
                }
            }
        });

        // 更新接受戳一下消息状态
        newMessageViewModel.requestReceivePokeMessageStatus();
    }

    /**
     * 设置新消息设置状态
     */
    public void setRemindStatus(boolean status) {
        if (newMessageViewModel != null) {
            newMessageViewModel.setRemindStatus(status);
        }
    }

    /**
     * 设置消息通知显示详情
     *
     * @param status 是否显示详情
     */
    public void setNoticeDetail(boolean status) {
        if (newMessageViewModel != null) {
            newMessageViewModel.setPushMsgDetailStatus(status);
        }
    }

    /**
     * 设置接受戳一下消息
     *
     * @param isReceive
     */
    public void setReceivePokeMessage(boolean isReceive) {
        if (newMessageViewModel != null) {
            newMessageViewModel.setReceivePokeMessageStatus(isReceive);
        }
    }
}
