package cn.rongcloud.im.ui.dialog;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.ota.OTAUtils;
import cn.rongcloud.im.utils.ToastUtils;

public class DownloadAppDialog extends DialogFragment {

    @Override
    public void onStart() {
        super.onStart();

        // 设置宽度为屏宽, 靠近屏幕底部。
        Window win = getDialog().getWindow();
        // 一定要设置Background，如果不设置，window属性设置无效
        win.setBackgroundDrawable( new ColorDrawable(Color.TRANSPARENT));

        //全屏化对话框
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

        WindowManager.LayoutParams params = win.getAttributes();
        // 使用ViewGroup.LayoutParams，以便Dialog 宽度充满整个屏幕
        params.width =  ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        win.setAttributes(params);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        String url = getArguments().getString(IntentExtra.URL);
        View view = inflater.inflate(R.layout.dialog_download_app, null);
        view.findViewById(R.id.siv_local).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtils.showToast(R.string.seal_mine_about_toast_app_downloading);
                // 本地下载
                OTAUtils.DownLoadConfig downLoadConfig = new OTAUtils.DownLoadConfig();
                downLoadConfig.url = url;
                downLoadConfig.isShowNotification = true;
                downLoadConfig.notificationTitle = getString(R.string.seal_mine_about_notifi_title);
                downLoadConfig.notificationDescription = getString(R.string.seal_mine_about_notifi_loading);
                OTAUtils otaUtils = new OTAUtils(getActivity().getApplicationContext(),downLoadConfig);
                otaUtils.startDownloadAndInstall();
                dismiss();
            }
        });

        view.findViewById(R.id.siv_web).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 浏览器下载
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse(url);
                intent.setData(content_url);
                startActivity(intent);
                dismiss();
            }
        });
        return view;
    }
}
