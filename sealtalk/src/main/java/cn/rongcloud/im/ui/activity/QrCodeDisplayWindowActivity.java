package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import cn.rongcloud.im.R;

/**
 * 显示二维码窗口界面
 */
public class QrCodeDisplayWindowActivity extends QrCodeDisplayActivity {
    private final String TAG = "QrCodeDisplayWindowActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity_show_qrcode_window);
        initView();
    }

    @Override
    public int getContentViewId(){
        return R.layout.profile_activity_show_qrcode_window;
    }

    private void initView(){
        getTitleBar().setVisibility(View.GONE);
        View mainView = findViewById(R.id.profile_ll_qr_main);
        mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
