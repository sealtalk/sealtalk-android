package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import cn.rongcloud.im.R;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.qrcode.SealQrCodeUISelector;
import cn.rongcloud.im.ui.BaseActivity;
import cn.rongcloud.im.utils.StatusBarUtil;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.viewmodel.SplashViewModel;
import cn.rongcloud.im.utils.log.SLog;
import io.rong.push.RongPushClient;

public class SplashActivity extends BaseActivity {
    private Uri intentUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 处理小米手机按 home 键重新进入会重新打开初始化的页面
        if (!this.isTaskRoot()) {
            Intent mainIntent = getIntent();
            String action = mainIntent.getAction();
            if (mainIntent.hasCategory(Intent.CATEGORY_LAUNCHER) && action.equals(Intent.ACTION_MAIN)) {
                finish();
                return;
            }
        }
//        initStatusBar();
        setContentView(R.layout.activity_splash);

        Intent intent = getIntent();
        if (intent != null) {
            intentUri = intent.getData();
            recordHWNotificationEvent(intent);
        }
        initViewModel();
    }

    private void recordHWNotificationEvent(Intent intent) {
        if (Build.MANUFACTURER.equalsIgnoreCase("HUAWEI")
                && intent.getData() != null && "true".equals(intent.getData().getQueryParameter("isFromPush"))) {
            RongPushClient.recordHWNotificationEvent(intent);
        }
    }

    private void initStatusBar() {
        //这里注意下 因为在评论区发现有网友调用setRootViewFitsSystemWindows 里面 winContent.getChildCount()=0 导致代码无法继续
        //是因为你需要在setContentView之后才可以调用 setRootViewFitsSystemWindows
        //当FitsSystemWindows设置 true 时，会在屏幕最上方预留出状态栏高度的 padding
        StatusBarUtil.setRootViewFitsSystemWindows(this, true);
        //设置状态栏透明
        StatusBarUtil.setTranslucentStatus(this);
        //一般的手机的状态栏文字和图标都是白色的, 可如果你的应用也是纯白色的, 或导致状态栏文字看不清
        //所以如果你是这种情况,请使用以下代码, 设置状态使用深色文字图标风格, 否则你可以选择性注释掉这个if内容
        if (!StatusBarUtil.setStatusBarDarkTheme(this, false)) {
            //如果不支持设置深色风格 为了兼容总不能让状态栏白白的看不清, 于是设置一个状态栏颜色为半透明,
            //这样半透明+白=灰, 状态栏的文字能看得清
            StatusBarUtil.setStatusBarColor(this, 0x000000);
        }
    }

    /**
     * 初始化ViewModel
     */
    private void initViewModel() {
        SplashViewModel splashViewModel = ViewModelProviders.of(this).get(SplashViewModel.class);
        splashViewModel.getAutoLoginResult().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean result) {
                SLog.d("ss_auto", "result = " + result);

                if (result) {
                    goToMain();
                } else {
                    if (intentUri != null) {
                        ToastUtils.showToast(R.string.seal_qrcode_jump_without_login);
                    }
                    goToLogin();
                }
            }
        });
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        if (intentUri != null) {
            goWithUri();
        } else {
            finish();
        }
    }

    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    /**
     * 通过 uri 进行跳转
     */
    private void goWithUri() {
        String uri = intentUri.toString();

        // 判断是否是二维码跳转产生的 uri
        SealQrCodeUISelector uiSelector = new SealQrCodeUISelector(this);
        LiveData<Resource<String>> result = uiSelector.handleUri(uri);

        result.observe(this, new Observer<Resource<String>>() {
            @Override
            public void onChanged(Resource<String> resource) {
                if (resource.status != Status.LOADING) {
                    result.removeObserver(this);
                }

                if (resource.status == Status.SUCCESS) {
                    finish();
                }
            }
        });

    }
}
