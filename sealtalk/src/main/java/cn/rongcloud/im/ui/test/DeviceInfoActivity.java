package cn.rongcloud.im.ui.test;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import com.umeng.commonsdk.UMConfigure;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.BaseActivity;

/**
 * 设备相关信息，友盟添加测试设备时需要
 */
public class DeviceInfoActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);
        TextView deviceInfoTx = findViewById(R.id.rc_device_info);
        String[] deviceInfos = UMConfigure.getTestDeviceInfo(this);
        StringBuilder sb = new StringBuilder();
        if (deviceInfos[0] != null) {
            sb.append("deviceId=" + deviceInfos[0]);
        }

        if (deviceInfos[1] != null) {
            sb.append("mac=" + deviceInfos[1]);
        }
        deviceInfoTx.setText(sb.toString());
    }
}