package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import cn.rongcloud.im.R;
import cn.rongcloud.im.model.PrivacyResult;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.ui.view.SettingItemView;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.viewmodel.PrivacyViewModel;

public class PrivacyActivity extends TitleBaseActivity {

    private SettingItemView phoneSiv;
    private SettingItemView stAccountSiv;
    private SettingItemView friendVerifySiv;
    private SettingItemView groupVerifySiv;
    private PrivacyViewModel privacyViewModel;
    private boolean isPhoneSivClicked = false;
    private boolean isStAccountSivClicked = false;
    private boolean isFriendVerifySivClicked = false;
    private boolean isGroupVerifySivClicked = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);
        initView();
        initViewModel();
    }

    private void initView() {
        getTitleBar().setTitle(R.string.seal_mine_set_account_privacy);
        findViewById(R.id.siv_blacklist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PrivacyActivity.this, BlackListActivity.class));
            }
        });
        phoneSiv = findViewById(R.id.siv_search_phone);
        phoneSiv.setSwitchTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!isPhoneSivClicked) {
                    isPhoneSivClicked = true;
                }
                return false;
            }
        });
        phoneSiv.setSwitchCheckListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //初始化不触发逻辑
                if (!isPhoneSivClicked) {
                    return;
                }
                if (isChecked) {
                    setPrivace(1, -1, -1, -1);
                } else {
                    setPrivace(0, -1, -1, -1);
                }
            }
        });
        stAccountSiv = findViewById(R.id.siv_search_st_account);
        stAccountSiv.setSwitchTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!isStAccountSivClicked) {
                    isStAccountSivClicked = true;
                }
                return false;
            }
        });
        stAccountSiv.setSwitchCheckListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //初始化不触发逻辑
                if (!isStAccountSivClicked) {
                    return;
                }
                if (isChecked) {
                    setPrivace(-1, 1, -1, -1);
                } else {
                    setPrivace(-1, 0, -1, -1);
                }
            }
        });
        friendVerifySiv = findViewById(R.id.siv_friend_verify);
        friendVerifySiv.setSwitchTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!isFriendVerifySivClicked) {
                    isFriendVerifySivClicked = true;
                }
                return false;
            }
        });
        friendVerifySiv.setSwitchCheckListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //初始化不触发逻辑
                if (!isFriendVerifySivClicked) {
                    return;
                }
                if (isChecked) {
                    setPrivace(-1, -1, 1, -1);
                } else {
                    setPrivace(-1, -1, 0, -1);
                }
            }
        });
        groupVerifySiv = findViewById(R.id.siv_group_verify);
        groupVerifySiv.setSwitchTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!isGroupVerifySivClicked) {
                    isGroupVerifySivClicked = true;
                }
                return false;
            }
        });
        groupVerifySiv.setSwitchCheckListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //初始化不触发逻辑
                if (!isGroupVerifySivClicked) {
                    return;
                }
                if (isChecked) {
                    setPrivace(-1, -1, -1, 1);
                } else {
                    setPrivace(-1, -1, -1, 0);
                }
            }
        });
    }

    private void initViewModel() {
        privacyViewModel = ViewModelProviders.of(this).get(PrivacyViewModel.class);
        privacyViewModel.getPrivacyState().observe(this, new Observer<Resource<PrivacyResult>>() {
            @Override
            public void onChanged(Resource<PrivacyResult> privacyResultResource) {
                updateView(privacyResultResource);
            }
        });
        privacyViewModel.getSetPrivacyResult().observe(this, new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> voidResource) {
                if (voidResource.status == Status.SUCCESS) {
                    ToastUtils.showToast(getString(R.string.seal_set_clean_time_success));
                } else if (voidResource.status == Status.ERROR){
                    ToastUtils.showToast(getString(R.string.seal_set_clean_time_fail));
                }
            }
        });
    }

    private void updateView(Resource<PrivacyResult> privacyResultResource) {
        if (privacyResultResource.status == Status.SUCCESS) {
            if (privacyResultResource.data != null) {
                if (privacyResultResource.data.phoneVerify == PrivacyResult.State.ALLOW.getValue()) {
                    phoneSiv.setCheckedImmediately(true);
                } else {
                    phoneSiv.setCheckedImmediately(false);
                }
                if (privacyResultResource.data.stSearchVerify == PrivacyResult.State.ALLOW.getValue()) {
                    stAccountSiv.setCheckedImmediately(true);
                } else {
                    stAccountSiv.setCheckedImmediately(false);
                }
                if (privacyResultResource.data.friVerify == PrivacyResult.State.ALLOW.getValue()) {
                    friendVerifySiv.setCheckedImmediately(true);
                } else {
                    friendVerifySiv.setCheckedImmediately(false);
                }
                if (privacyResultResource.data.groupVerify == PrivacyResult.State.ALLOW.getValue()) {
                    groupVerifySiv.setCheckedImmediately(true);
                } else {
                    groupVerifySiv.setCheckedImmediately(false);
                }
            }
        }
    }

    private void setPrivace(int phoneVerify, int stSearchVerify,
                            int friVerify, int groupVerify) {
        privacyViewModel.setPrivacy(phoneVerify, stSearchVerify, friVerify, groupVerify);
    }
}
