package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import cn.rongcloud.im.R;
import cn.rongcloud.im.model.ProxyModel;
import cn.rongcloud.im.task.AppTask;
import cn.rongcloud.im.ui.BaseActivity;
import cn.rongcloud.im.utils.StatusBarUtil;
import cn.rongcloud.im.utils.ToastUtils;
import com.google.android.material.textfield.TextInputLayout;
import io.rong.common.RLog;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.RCIMProxy;

public class ProxySettingActivity extends BaseActivity {

    private static final String TAG = "ProxySettingActivity";

    private TextInputLayout tilTestHost;
    private AppCompatEditText etTestUri;
    private TextInputLayout tilProxyIp;
    private AppCompatEditText etProxyIp;
    private TextInputLayout tilProxyPort;
    private AppCompatEditText etProxyPort;
    private TextInputLayout tilUserName;
    private AppCompatEditText etUserName;
    private TextInputLayout tilPassword;
    private AppCompatEditText etPassword;
    private View btnTest;
    private View btnSave;
    private AppTask mAppTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStatusBar();
        setContentView(R.layout.activity_proxy_setting);
        startBgAnimation();
        initTitleBar();
        initView();
        initData();
    }

    private void initData() {
        mAppTask = new AppTask(this);

        ProxyModel proxy = mAppTask.getProxy();
        if (proxy != null) {
            etProxyIp.setText(proxy.getProxyHost());
            etProxyPort.setText(String.valueOf(proxy.getPort()));
            etTestUri.setText(proxy.getTestHost());
            etUserName.setText(proxy.getUserName());
            etPassword.setText(proxy.getPassword());
        }
    }

    private void initView() {
        tilTestHost = (TextInputLayout) findViewById(R.id.til_test_uri);
        etTestUri = ((AppCompatEditText) findViewById(R.id.et_test_uri));

        tilProxyIp = (TextInputLayout) findViewById(R.id.til_proxy_ip);
        etProxyIp = ((AppCompatEditText) findViewById(R.id.et_proxy_ip));

        tilProxyPort = (TextInputLayout) findViewById(R.id.til_proxy_port);
        etProxyPort = ((AppCompatEditText) findViewById(R.id.et_proxy_port));

        tilUserName = (TextInputLayout) findViewById(R.id.til_proxy_user_name);
        etUserName = ((AppCompatEditText) findViewById(R.id.et_proxy_user_name));

        tilPassword = (TextInputLayout) findViewById(R.id.til_proxy_password);
        etPassword = ((AppCompatEditText) findViewById(R.id.et_proxy_password));

        btnTest = findViewById(R.id.btn_test_proxy);
        btnTest.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        testProxy();
                    }
                });

        btnSave = findViewById(R.id.btn_save_proxy);
        btnSave.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        saveProxy();
                    }
                });

        findViewById(R.id.btn_clear_proxy)
                .setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                clearProxy();
                            }
                        });
    }

    private void clearProxy() {
        mAppTask.saveProxyMode(null);
        etProxyIp.setText(null);
        etProxyPort.setText(null);
        etUserName.setText(null);
        etPassword.setText(null);
        etTestUri.setText(null);
    }

    private void saveProxy() {
        if (!checkProxyParam(false)) {
            return;
        }
        ProxyModel proxyModel = new ProxyModel();
        proxyModel.setProxyHost(etProxyIp.getText().toString());
        proxyModel.setPort(Integer.parseInt(etProxyPort.getText().toString()));
        proxyModel.setUserName(etUserName.getText().toString());
        proxyModel.setPassword(etPassword.getText().toString());
        proxyModel.setTestHost(etTestUri.getText().toString());
        mAppTask.saveProxyMode(proxyModel);
        ToastUtils.showToast("保存成功");
        finish();
    }

    private void testProxy() {
        if (!checkProxyParam(true)) {
            return;
        }
        btnTest.setEnabled(false);
        RCIMProxy rcimProxy =
                new RCIMProxy(
                        etProxyIp.getText().toString(),
                        Integer.parseInt(etProxyPort.getText().toString()),
                        etUserName.getText().toString(),
                        etPassword.getText().toString());
        tilTestHost.setHelperTextEnabled(false);
        tilTestHost.setErrorEnabled(false);
        RongIMClient.getInstance()
                .testProxy(
                        rcimProxy,
                        etTestUri.getText().toString(),
                        new RongIMClient.Callback() {
                            @Override
                            public void onSuccess() {
                                runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                RLog.d(TAG, "onSuccess: ");
                                                btnTest.setEnabled(true);
                                                tilTestHost.setHelperTextEnabled(true);
                                                tilTestHost.setErrorEnabled(false);
                                                tilTestHost.setError(null);
                                                tilTestHost.setHelperText("连接成功");
                                            }
                                        });
                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode errorCode) {
                                runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                RLog.d(TAG, "onError: " + errorCode.getValue());
                                                btnTest.setEnabled(true);
                                                tilTestHost.setErrorEnabled(true);
                                                tilTestHost.setHelperTextEnabled(false);
                                                tilTestHost.setHelperText(null);
                                                tilTestHost.setError(
                                                        "连接失败:" + errorCode.getValue());
                                            }
                                        });
                            }
                        });
    }

    private boolean checkProxyParam(boolean isTest) {
        boolean flag = true;
        if (isTest) {
            if (TextUtils.isEmpty(etTestUri.getText())) {
                tilTestHost.setError("测试地址不能为空");
                flag = false;
            } else {
                tilTestHost.setError(null);
            }
        }

        if (TextUtils.isEmpty(etProxyIp.getText())) {
            tilProxyIp.setError("代理地址不能为空");
            flag = false;
        } else {
            tilProxyIp.setError(null);
        }

        Editable text = etProxyPort.getText();
        if (TextUtils.isEmpty(text) || !isAvailablePort(text.toString())) {
            tilProxyPort.setError("端口号不正确");
            flag = false;
        } else {
            tilProxyPort.setError(null);
        }

        if (TextUtils.isEmpty(etUserName.getText()) && !TextUtils.isEmpty(etPassword.getText())) {
            tilUserName.setError("用户名不能为空");
            flag = false;
        } else {
            tilUserName.setError(null);
        }
        if (!TextUtils.isEmpty(etUserName.getText()) && TextUtils.isEmpty(etPassword.getText())) {
            tilPassword.setError("密码不能为空");
            flag = false;
        } else {
            tilPassword.setError(null);
        }

        return flag;
    }

    private void initTitleBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_title);
        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setDisplayShowTitleEnabled(true);
        }
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
    }

    private void initStatusBar() {
        // 这里注意下 因为在评论区发现有网友调用setRootViewFitsSystemWindows 里面 winContent.getChildCount()=0 导致代码无法继续
        // 是因为你需要在setContentView之后才可以调用 setRootViewFitsSystemWindows
        // 当FitsSystemWindows设置 true 时，会在屏幕最上方预留出状态栏高度的 padding
        StatusBarUtil.setRootViewFitsSystemWindows(this, true);
        // 设置状态栏透明
        StatusBarUtil.setTranslucentStatus(this);
        // 一般的手机的状态栏文字和图标都是白色的, 可如果你的应用也是纯白色的, 或导致状态栏文字看不清
        // 所以如果你是这种情况,请使用以下代码, 设置状态使用深色文字图标风格, 否则你可以选择性注释掉这个if内容
        if (!StatusBarUtil.setStatusBarDarkTheme(this, false)) {
            // 如果不支持设置深色风格 为了兼容总不能让状态栏白白的看不清, 于是设置一个状态栏颜色为半透明,
            // 这样半透明+白=灰, 状态栏的文字能看得清
            StatusBarUtil.setStatusBarColor(this, 0x000000);
        }
    }

    private void startBgAnimation() {
        Animation animation =
                AnimationUtils.loadAnimation(this, R.anim.seal_login_bg_translate_anim);
        findViewById(R.id.iv_background).startAnimation(animation);
    }

    private boolean isAvailablePort(String portStr) {
        try {
            int port = Integer.parseInt(portStr);
            if (port <= 0 || port > 65535) {
                return false;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return true;
    }
}
