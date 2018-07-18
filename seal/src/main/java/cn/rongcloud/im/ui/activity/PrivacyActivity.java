package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import cn.rongcloud.im.R;


public class PrivacyActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);
        setTitle(R.string.set_privacy);

        RelativeLayout mTheBlackList = (RelativeLayout) findViewById(R.id.rl_the_blacklist);

        mTheBlackList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PrivacyActivity.this, BlackListActivity.class));
            }
        });
    }


}
