package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.dialog.ClearCacheDialog;
import cn.rongcloud.im.ui.dialog.CommonDialog;
import cn.rongcloud.im.viewmodel.UserInfoViewModel;

/**
 * 账号设置
 */
public class AccountSettingActivity extends TitleBaseActivity implements View.OnClickListener {

    private UserInfoViewModel userInfoViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setting);
        initView();
        initViewModel();
    }

    private void initView() {
        getTitleBar().setTitle(R.string.seal_main_mine_set_account);

        findViewById(R.id.siv_reset_password).setOnClickListener(this);
        findViewById(R.id.siv_privacy).setOnClickListener(this);
        findViewById(R.id.siv_show_new_msg).setOnClickListener(this);
        findViewById(R.id.siv_clear_cache).setOnClickListener(this);
        findViewById(R.id.btn_logout).setOnClickListener(this);
        findViewById(R.id.siv_clear_message_cache).setOnClickListener(this);
        findViewById(R.id.siv_chat_bg).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.siv_reset_password:
                // 修改密码
                startActivity(new Intent(this, UpdatePasswordActivity.class));
                break;
            case R.id.siv_privacy:
                // 隐私
                startActivity(new Intent(this, PrivacyActivity.class));
                break;
            case R.id.siv_show_new_msg:
                //新消息通知
                startActivity(new Intent(this, NewMessageRemindActivity.class));
                break;
            case R.id.siv_clear_cache:
                //清理缓存
                showClearDialog();
                break;
            case R.id.btn_logout:
                //退出
                showExitDialog();
                break;
            case R.id.siv_clear_message_cache:
                startActivity(new Intent(this, ClearChatMessageActivity.class));
                break;
            case R.id.siv_chat_bg:
                startActivity(new Intent(this, SelectChatBgActivity.class));
                break;
            default:
                //DO nothing
                break;

        }
    }

    private void showClearDialog() {
        ClearCacheDialog.Builder builder = new ClearCacheDialog.Builder();
        builder.setContentMessage(getString(R.string.seal_set_account_dialog_clear_cache_message));
        CommonDialog dialog = builder.build();
        dialog.show(getSupportFragmentManager(), "clear_cache");
    }

    private void showExitDialog() {
        CommonDialog.Builder  builder = new CommonDialog.Builder();
        builder.setContentMessage(getString(R.string.seal_mine_set_account_dialog_logout_message));
        builder.setDialogButtonClickListener(new CommonDialog.OnDialogButtonClickListener() {
            @Override
            public void onPositiveClick(View v, Bundle bundle) {
                logout();
                // 通知退出
                sendLogoutNotify();
                Intent intent = new Intent(AccountSettingActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onNegativeClick(View v, Bundle bundle) {

            }
        });
        CommonDialog dialog = builder.build();
        dialog.show(getSupportFragmentManager(), "logout_dialog");
    }

    private void initViewModel() {
        userInfoViewModel = ViewModelProviders.of(this).get(UserInfoViewModel.class);
    }


    private void logout() {
        if (userInfoViewModel != null) {
            userInfoViewModel.logout();
        }
    }
}
