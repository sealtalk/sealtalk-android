package cn.rongcloud.im.ui.activity;

import android.os.Bundle;

import cn.rongcloud.im.R;

public class PublicServiceSearchActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pub_search);
        setTitle(R.string.rc_search);
    }
}
