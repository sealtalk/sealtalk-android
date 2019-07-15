package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import cn.rongcloud.im.R;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.qrcode.SealQrCodeUISelector;
import cn.rongcloud.im.ui.BaseActivity;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.viewmodel.SplashViewModel;
import cn.rongcloud.im.utils.log.SLog;

public class SplashActivity extends BaseActivity {
    private Uri intentUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Intent intent = getIntent();
        if(intent != null){
            intentUri = intent.getData();
        }
        initViewModel();
    }

    /**
     * 初始化ViewModel
     */
    private  void initViewModel() {
        SplashViewModel splashViewModel = ViewModelProviders.of(this).get(SplashViewModel.class);
        splashViewModel.getAutoLoginResult().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean result) {
                SLog.d("ss_auto", "result = " + result);

                if (result) {
                    goToMain();
                } else {
                    if(intentUri != null){
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
                if(resource.status != Status.LOADING){
                    result.removeObserver(this);
                }

                if(resource.status == Status.SUCCESS){
                    finish();
                }else if(resource.status == Status.ERROR){
                    ToastUtils.showToast(resource.data);
                }
            }
        });

    }
}
