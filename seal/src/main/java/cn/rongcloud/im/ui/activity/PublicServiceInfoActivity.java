package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import android.util.Log;

import cn.rongcloud.im.R;

public class PublicServiceInfoActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("", "---------PublicServiceInfoActivity------------");
        setContentView(R.layout.pub_account_info);
        setTitle("公众号信息");

    }

}
