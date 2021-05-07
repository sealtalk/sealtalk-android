package cn.rongcloud.im.ui.test;

import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import cn.rongcloud.im.R;
import cn.rongcloud.im.im.provider.SealGroupNotificationMessageItemProvider;
import cn.rongcloud.im.ui.BaseActivity;
import cn.rongcloud.im.ui.test.provider.GroupReadReceiptTextMessageItemProvider;
import io.rong.imkit.config.RongConfigCenter;
import io.rong.imkit.conversation.messgelist.provider.GroupNotificationMessageItemProvider;
import io.rong.imkit.conversation.messgelist.provider.TextMessageItemProvider;

/**
 * @decription 群已读回执V2测试页面  群消息接收者进入此界面
 * @autor yanke
 * @time 2021/2/25 5:43 PM
 */
public class GRRReceiverTestActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_read_receipt_receiver_test);
        init();

        // 添加会话界面
        CustomConversationFragment conversationFragment = new CustomConversationFragment();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.receiver_container, conversationFragment);
        transaction.commit();

    }

    private void init() {
    }
}