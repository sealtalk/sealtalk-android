package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import cn.rongcloud.im.R;

public class PrivacyActivity extends TitleBaseActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);
        initView();
    }

    private void initView() {
        getTitleBar().setTitle(R.string.seal_mine_set_account_privacy);
        findViewById(R.id.siv_blacklist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PrivacyActivity.this, BlackListActivity.class));
            }
        });
    }
}
