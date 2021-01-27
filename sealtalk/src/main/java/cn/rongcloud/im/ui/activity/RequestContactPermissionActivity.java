package cn.rongcloud.im.ui.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import cn.rongcloud.im.R;
import cn.rongcloud.im.utils.CheckPermissionUtils;
import cn.rongcloud.im.utils.ToastUtils;

/**
 * 请求通讯录权限界面
 */
public class RequestContactPermissionActivity extends TitleBaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getTitleBar().setTitle(R.string.new_friend_request_permission_title);
        setContentView(R.layout.add_friend_activity_request_contact_permission);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            finish();
        }
    }

    private void initView() {
        findViewById(R.id.request_permission_tv_to_setting).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.request_permission_tv_to_setting:
                toSetting();
                break;
        }
    }

    /**
     * 跳转到权限设置界面
     */
    private void toSetting() {
        try {
            CheckPermissionUtils.toPermissionSetting(this);
        } catch (Exception e) {
            try {
                CheckPermissionUtils.startAppSetting(this);
            } catch (Exception e2) {
                ToastUtils.showToast(R.string.new_friend_no_permission_can_not_to_setting, Toast.LENGTH_LONG);
            }
        }
    }
}
