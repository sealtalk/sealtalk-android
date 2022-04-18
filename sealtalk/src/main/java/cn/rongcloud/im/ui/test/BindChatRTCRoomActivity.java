package cn.rongcloud.im.ui.test;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.widget.AppCompatEditText;
import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.activity.TitleBaseActivity;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.RongIMClient.ErrorCode;
import io.rong.imlib.RongIMClient.OperationCallback;

public class BindChatRTCRoomActivity extends TitleBaseActivity {

    private AppCompatEditText etBindRTCRoom;
    private AppCompatEditText etBindIMRoom;
    private Button btnBind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_chat_rtcroom);
        getTitleBar().setTitle("绑定RTC和Chat Room");
        initView();
    }

    private void initView() {
        etBindIMRoom = findViewById(R.id.et_bind_chat_room);
        etBindRTCRoom = findViewById(R.id.et_bind_rtc_room);
        btnBind = findViewById(R.id.btn_bind_chat_rtc);
    }

    public void bindClick(View view) {

        String imRoom = etBindIMRoom.getText().toString().trim();
        String rtcRoom = etBindRTCRoom.getText().toString().trim();

        if (TextUtils.isEmpty(imRoom) || TextUtils.isEmpty(rtcRoom)) {
            Toast.makeText(this, "房间号不可为空", Toast.LENGTH_SHORT).show();
            return;
        }

        RongIMClient.getInstance()
                .bindChatRoomWithRTCRoom(
                        imRoom,
                        rtcRoom,
                        new OperationCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(
                                                BindChatRTCRoomActivity.this,
                                                "绑定成功",
                                                Toast.LENGTH_SHORT)
                                        .show();
                            }

                            @Override
                            public void onError(ErrorCode errorCode) {
                                Toast.makeText(
                                                BindChatRTCRoomActivity.this,
                                                "绑定失败",
                                                Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });
    }
}
