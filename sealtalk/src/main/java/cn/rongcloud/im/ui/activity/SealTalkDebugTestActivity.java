package cn.rongcloud.im.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.test.ChatRoomTestActivity;
import cn.rongcloud.im.ui.test.CommonConversationListTestActivity;
import cn.rongcloud.im.ui.test.DeviceInfoActivity;
import cn.rongcloud.im.ui.test.GRRConversationListTestActivity;
import cn.rongcloud.im.ui.test.DiscussionActivity;
import cn.rongcloud.im.ui.test.MsgExpansionConversationListActivity;
import cn.rongcloud.im.ui.test.PushConfigActivity;
import cn.rongcloud.im.ui.test.ShortageConversationListActivity;
import cn.rongcloud.im.ui.test.TagTestActivity;
import cn.rongcloud.im.ui.view.SettingItemView;
import cn.rongcloud.im.utils.ToastUtils;
import io.rong.imlib.RongIMClient;

public class SealTalkDebugTestActivity extends TitleBaseActivity implements View.OnClickListener {
    private SettingItemView pushConfigModeSiv;
    private SettingItemView pushDiscussion;
    private SettingItemView pushLanguageSiv;
    private SettingItemView chatRoomSiv;
    private SettingItemView messageExpansion;
    private SettingItemView tag;
    private SettingItemView shortage;
    private SettingItemView groupReadReceiptV2Siv;
    private SettingItemView deviceInfo;
    private SettingItemView referMsgTest;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sealtalk_debug_test);
        initView();
    }


    /**
     * 初始化布局
     */
    private void initView() {
        getTitleBar().setTitle(R.string.seal_main_mine_about);

        groupReadReceiptV2Siv = findViewById(R.id.siv_grr_v2_sender_test);
        pushLanguageSiv = findViewById(R.id.siv_push_language);
        pushConfigModeSiv = findViewById(R.id.siv_push_config);
        chatRoomSiv = findViewById(R.id.siv_chatroom);
        pushConfigModeSiv.setOnClickListener(this);
        pushLanguageSiv.setOnClickListener(this);
        chatRoomSiv.setOnClickListener(this);

        pushDiscussion = findViewById(R.id.siv_discussion);
        pushDiscussion.setOnClickListener(this);

        messageExpansion = findViewById(R.id.siv_message_expansion);
        messageExpansion.setOnClickListener(this);

        shortage = findViewById(R.id.siv_shortage);
        shortage.setOnClickListener(this);

        tag = findViewById(R.id.siv_tag);
        tag.setOnClickListener(this);

        deviceInfo = findViewById(R.id.siv_umeng_info);
        deviceInfo.setOnClickListener(this);

        referMsgTest = findViewById(R.id.siv_reference_msg_test);
        referMsgTest.setOnClickListener(this);

        groupReadReceiptV2Siv.setOnClickListener(this);
        findViewById(R.id.siv_block_msg_test).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.siv_push_config:
                toPushConfig();
                break;
            case R.id.siv_discussion:
                toDiscussion();
                break;
            case R.id.siv_push_language:
                toInputTitleDialog();
                break;
            case R.id.siv_chatroom:
                toChatRoom();
                break;
            case R.id.siv_message_expansion:
                toMessageExpansion();
                break;
            case R.id.siv_tag:
                toTagTest();
                break;
            case R.id.siv_shortage:
                toShortage();
                break;
            case R.id.siv_grr_v2_sender_test:
                toGroupReadReceiptTest(1);
                break;
            case R.id.siv_umeng_info:
                toDeviceInfo();
                break;
            case R.id.siv_reference_msg_test:
                toReferMsgTest();
                break;
            case R.id.siv_block_msg_test:
                toReferMsgTest();
                break;
            default:
                //Do nothing
                break;
        }
    }

    private void toReferMsgTest() {
        Intent intent = new Intent(this, CommonConversationListTestActivity.class);
        startActivity(intent);
    }

    private void toDeviceInfo() {
        Intent intent = new Intent(this, DeviceInfoActivity.class);
        startActivity(intent);
    }

    private void toShortage() {
        Intent intent = new Intent(this, ShortageConversationListActivity.class);
        startActivity(intent);
    }

    private void toGroupReadReceiptTest(int type) {
        Intent intent = new Intent(this, GRRConversationListTestActivity.class);
        startActivity(intent);
    }

    private void toTagTest() {
        Intent intent = new Intent(this, TagTestActivity.class);
        startActivity(intent);
    }

    private void toMessageExpansion() {
        Intent intent = new Intent(this, MsgExpansionConversationListActivity.class);
        startActivity(intent);
    }

    private void toChatRoom() {
        Intent intent = new Intent(this, ChatRoomTestActivity.class);
        startActivity(intent);
    }

    private void toInputTitleDialog() {
        final EditText inputLanguage = new EditText(this);
        inputLanguage.setFocusable(true);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("设置推送语言")
                .setView(inputLanguage)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String languageCode = inputLanguage.getText().toString();
                        RongIMClient.getInstance().setPushLanguageCode(languageCode, new RongIMClient.OperationCallback() {
                            @Override
                            public void onSuccess() {
                                ToastUtils.showToast("设置成功");
                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode errorCode) {
                                ToastUtils.showToast("设置失败");
                            }
                        });
                    }
                }).show();
    }

    private void toPushConfig() {
        Intent intent = new Intent(this, PushConfigActivity.class);
        startActivity(intent);
    }

    private void toDiscussion() {
        Intent intent = new Intent(this, DiscussionActivity.class);
        startActivity(intent);
    }
}
