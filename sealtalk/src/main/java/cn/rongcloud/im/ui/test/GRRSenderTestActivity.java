package cn.rongcloud.im.ui.test;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.activity.TitleBaseActivity;
import cn.rongcloud.im.utils.ToastUtils;
import io.rong.imkit.IMCenter;
import io.rong.imlib.ReadReceiptV2Manager;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.GroupMessageReader;
import io.rong.imlib.model.Message;
import io.rong.message.TextMessage;

/**
 * @decription 群已读回执V2功能测试页面
 * @autor yanke
 * @time 2021/2/25 5:43 PM
 */
public class GRRSenderTestActivity extends TitleBaseActivity {

    private Button btnSendGroupMsg;
    private Button btnGetReadReceiptUserList;
    private ListView readReceiptUserListView;
    private ArrayList<GroupMessageReader> contentList = new ArrayList<>();
    private Message message1;
    private GroupReadReceiptAdapter adapter;
    private TextView displayToGetReadReceiptUserList;
    private TextView displayToRecceiveReadReceiptCmd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_read_receipt_test);
        initView();
    }

    private void initView() {
        getTitleBar().setTitle(getResources().getString(R.string.seal_mine_about_group_read_receipt_test));

        readReceiptUserListView = (ListView) findViewById(R.id.lv_read_receipt_user_list_content);
        displayToRecceiveReadReceiptCmd = (TextView) findViewById(R.id.tv_receive_read_receipt_count);
        displayToGetReadReceiptUserList = (TextView) findViewById(R.id.tv_get_read_receipt_count);
        btnSendGroupMsg = (Button) findViewById(R.id.btn_send_group_msg);
        btnGetReadReceiptUserList = (Button) findViewById(R.id.btn_get_read_receipt_user_list);

        String groupId = getIntent().getStringExtra("groupId");
        Conversation.ConversationType conversationType = Conversation.ConversationType.setValue(getIntent().getIntExtra("conversationType", 0));
        displayToRecceiveReadReceiptCmd.setText(getDisplayCount(0, 0));

        adapter = new GroupReadReceiptAdapter();
        readReceiptUserListView.setAdapter(adapter);

        btnSendGroupMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (conversationType != Conversation.ConversationType.GROUP) {
                    ToastUtils.showToast("只支持群组会话");
                    return;
                }
                TextMessage textMessage = TextMessage.obtain("群已读回执:" + new Random().nextInt(10000));
                Message message = Message.obtain(groupId, conversationType, textMessage);
                IMCenter.getInstance().sendMessage(message, null, null, new IRongCallback.ISendMessageCallback() {
                    @Override
                    public void onAttached(Message message) {

                    }

                    @Override
                    public void onSuccess(Message message) {
                        ToastUtils.showToast("发送成功");
                        message1 = message;
                    }

                    @Override
                    public void onError(Message message, RongIMClient.ErrorCode errorCode) {
                        ToastUtils.showToast("发送失败" + errorCode.code);
                    }
                });
            }
        });

        btnGetReadReceiptUserList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReadReceiptV2Manager.getInstance().getGroupMessageReaderList(message1, new ReadReceiptV2Manager.IGetMessageReadUserListCallback() {
                    @Override
                    public void onSuccess(int members, List<GroupMessageReader> users) {
                        ToastUtils.showToast("获取成功");
                        String getDisplayCountTx = String.format(getString(R.string.debug_group_read_receipt_count_get_display_tx), members);
                        displayToGetReadReceiptUserList.setText(getDisplayCountTx);

                        contentList.clear();
                        contentList.addAll(users);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(IRongCoreEnum.CoreErrorCode coreErrorCode) {
                        ToastUtils.showToast("获取失败-" + coreErrorCode.code);
                    }
                });

            }
        });

        // 添加会话界面
        CustomConversationFragment conversationFragment = new CustomConversationFragment();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.receiver_container, conversationFragment);
        transaction.commit();
    }

    private String getDisplayCount(int readCount, int totalCount) {
        String displayCount = String.format(getResources().getString(R.string.debug_group_read_receipt_count_display_tx), readCount, totalCount);
        return displayCount;
    }


    private class GroupReadReceiptAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return contentList.size();
        }

        @Override
        public Object getItem(int position) {
            return contentList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_msg_extra_status, null);
            }
            TextView tvCotent = convertView.findViewById(R.id.tv_content);
            GroupMessageReader user = contentList.get(position);
            tvCotent.setText("已读用户ID：" + user.getUserId() + "  已读时间：" + user.getReadTime());
            return convertView;
        }
    }
}