package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.VersionInfo;
import cn.rongcloud.im.ui.dialog.DownloadAppDialog;
import cn.rongcloud.im.ui.view.SettingItemView;
import cn.rongcloud.im.viewmodel.AppViewModel;

/**
 * 关于 SealTalk 的界面
 */
public class AboutSealTalkActivity extends TitleBaseActivity implements View.OnClickListener {
    private SettingItemView sealtalkVersionSiv;
    private SettingItemView sdkVersionSiv;
    private String url;
    private SettingItemView debufModeSiv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_sealtalk);
        url = getIntent().getStringExtra(IntentExtra.URL);
        initView();
        initViewModel();
    }


    /**
     * 初始化布局
     */
    private void initView() {
        getTitleBar().setTitle(R.string.seal_main_mine_about);

        findViewById(R.id.siv_update_log).setOnClickListener(this);
        findViewById(R.id.siv_func_introduce).setOnClickListener(this);
        findViewById(R.id.siv_rongcloud_web).setOnClickListener(this);
        sealtalkVersionSiv = findViewById(R.id.siv_sealtalk_version);
        sealtalkVersionSiv.setOnClickListener(this);
        sdkVersionSiv = findViewById(R.id.siv_sdk_version);
        sdkVersionSiv.setOnClickListener(this);
        debufModeSiv = findViewById(R.id.siv_close_debug_mode);
        debufModeSiv.setOnClickListener(this);
        findViewById(R.id.siv_online_status).setOnClickListener(this);
        sealtalkVersionSiv.setClickable(false);
    }

    /**
     * 初始化 ViewModel
     */
    private void initViewModel() {
        AppViewModel appViewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        // 是否有新版本
        appViewModel.getHasNewVersion().observe(this, new Observer<Resource<VersionInfo.AndroidVersion>>() {
            @Override
            public void onChanged(Resource<VersionInfo.AndroidVersion> resource) {
                if (resource.data != null) {
                    sealtalkVersionSiv.setClickable(true);
                    sealtalkVersionSiv.setTagImageVisibility(View.VISIBLE);
                }
            }
        });

        // sdk 版本
        appViewModel.getSDKVersion().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String version) {
                sdkVersionSiv.setValue(version);
            }
        });

        // sealtalk 版本
        appViewModel.getSealTalkVersion().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String version) {
                sealtalkVersionSiv.setValue(version);
            }
        });

        appViewModel.getDebugMode().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean result) {
                if (result) {
                    sdkVersionSiv.setClickable(false);
                    debufModeSiv.setVisibility(View.VISIBLE);
                } else {
                    sdkVersionSiv.setClickable(true);
                    debufModeSiv.setVisibility(View.GONE);
                }

            }
        });

    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.siv_update_log:
                toWeb(getString(R.string.seal_mine_about_update_log), "http://www.rongcloud.cn/changelog");
                break;
            case R.id.siv_func_introduce:
                toWeb(getString(R.string.seal_mine_about_function_introduce), "http://rongcloud.cn/features");
                break;
            case R.id.siv_rongcloud_web:
                toWeb(getString(R.string.seal_mine_about_rongcloud_web), "http://rongcloud.cn/");
                break;
            case R.id.siv_sealtalk_version:
                showDownloadDialog(url);
                break;
            case R.id.siv_sdk_version:
                // TODO 开启 debug 模式规则
                break;
            case R.id.siv_close_debug_mode:
                sdkVersionSiv.setClickable(true);
                debufModeSiv.setVisibility(View.GONE);
                // TODO 关闭 debug 模式
                break;
            case R.id.siv_online_status: // 目前废弃
                break;
            default:
                //Do nothing
                break;
        }
    }

    private void toWeb(String title, String url) {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(WebViewActivity.PARAMS_TITLE, title);
        intent.putExtra(WebViewActivity.PARAMS_URL, url);
        startActivity(intent);
    }

    /**
     * 提示下载
     */
    private void showDownloadDialog(String url) {
        DownloadAppDialog dialog = new DownloadAppDialog();
        Bundle bundle = new Bundle();
        bundle.putString(IntentExtra.URL, url);
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "download_dialog");
    }
}
