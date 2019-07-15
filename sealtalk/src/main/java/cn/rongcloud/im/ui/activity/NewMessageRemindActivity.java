package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.view.SettingItemView;
import cn.rongcloud.im.viewmodel.NewMessageViewModel;

/**
 * 新消息提醒
 */
public class NewMessageRemindActivity extends TitleBaseActivity {
    private NewMessageViewModel newMessageViewModel;
    private SettingItemView remindSiv;
    private SettingItemView noticeSiv;

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
        noticeSiv = findViewById(R.id.siv_notice);

        remindSiv.setSwitchCheckListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                noticeSiv.setEnabled(isChecked);
                setRemindStatus(isChecked);
            }
        });

        noticeSiv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NewMessageRemindActivity.this, MessageDonotDisturbSettingActivity.class));
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

    }

    /**
     * 设置新消息设置状态
     */
    public void setRemindStatus(boolean status) {
        if (newMessageViewModel != null) {
            newMessageViewModel.setRemindStatus(status);
        }
    }
}
