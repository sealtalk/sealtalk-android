package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;

import cn.rongcloud.im.R;
import cn.rongcloud.im.server.utils.NToast;
import cn.rongcloud.im.ui.widget.switchbutton.SwitchButton;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;


public class NewMessageRemindActivity extends BaseActivity {

    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message_remind);
        setTitle(R.string.new_message_notice);
        SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
        editor = sp.edit();
        boolean isOpenDisturb = sp.getBoolean("isOpenDisturb", true);

        final RelativeLayout mNotice = (RelativeLayout) findViewById(R.id.seal_notice);
        SwitchButton switchButton = (SwitchButton) findViewById(R.id.remind_switch);

        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mNotice.setClickable(true);
                    mNotice.setBackgroundColor(Color.parseColor("#ffffff"));

                    RongIM.getInstance().removeNotificationQuietHours(new RongIMClient.OperationCallback() {
                        @Override
                        public void onSuccess() {
                            editor.putBoolean("isOpenDisturb", true);
                            editor.commit();
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode errorCode) {

                        }
                    });

                } else {
                    mNotice.setClickable(false);
                    mNotice.setBackgroundColor(Color.parseColor("#f0f0f6"));

                    RongIM.getInstance().setNotificationQuietHours("00:00:00", 1439, new RongIMClient.OperationCallback() {
                        @Override
                        public void onSuccess() {
                            editor.putBoolean("isOpenDisturb", false);
                            editor.commit();
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode errorCode) {

                        }
                    });
                }
            }
        });

        switchButton.setChecked(isOpenDisturb);
        if (isOpenDisturb) {
            mNotice.setClickable(true);
            mNotice.setBackgroundColor(Color.parseColor("#ffffff"));
        } else {
            mNotice.setClickable(false);
            mNotice.setBackgroundColor(Color.parseColor("#f0f0f6"));
        }

        mNotice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(NewMessageRemindActivity.this, MessageDisturbActivity.class));
            }
        });
    }
}
