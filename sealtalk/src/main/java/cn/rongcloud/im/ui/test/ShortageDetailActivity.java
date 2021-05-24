package cn.rongcloud.im.ui.test;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import androidx.annotation.Nullable;
import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.BaseActivity;
import cn.rongcloud.im.ui.dialog.MsgExtraInputDialog;
import io.rong.common.RLog;
import io.rong.imlib.RongCoreClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.HistoryMessageOption;
import io.rong.imlib.model.Message;

/**
 * @author zhoujt
 */
public class ShortageDetailActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "ShortageDetailActivity";
    private ArrayList<String> contentList = new ArrayList<>();
    private MyAdapter mAdapter;
    private ListView lvContent;
    private String userId;
    private Conversation.ConversationType conversationType;
    private Context mContext;
    private Handler handler;
    private Button shortage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler(Looper.getMainLooper());
        setTitle("消息断档");
        setContentView(R.layout.activity_msg_shortage_detail);
        intData();
        initView();
        mContext = this;
    }

    private void intData() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        userId = intent.getStringExtra("uerid");
        conversationType = Conversation.ConversationType.setValue(intent.getIntExtra("conversationType", 1));
        RLog.i(TAG, "userId = " + userId);
        RLog.i(TAG, "conversationType = " + conversationType);
        lvContent = findViewById(R.id.lv_content);
        mAdapter = new MyAdapter();
        lvContent.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_set_key:
                MsgExtraInputDialog msgSetDialog = new MsgExtraInputDialog(mContext, MsgExtraInputDialog.TYPE_SHORTAGE);

                msgSetDialog.getSureView().setOnClickListener(v1 -> {
                    String editStr = msgSetDialog.getEtUID().getText().toString();
                    if (TextUtils.isEmpty(editStr)) {
                        Toast.makeText(getApplicationContext(), "必须输入时间", Toast.LENGTH_LONG).show();
                        return;
                    }
                    long datetime = Long.parseLong(msgSetDialog.getEtUID().getText().toString());
                    int count = Integer.parseInt(msgSetDialog.getEtKey().getText().toString());
                    int pullOrder = Integer.parseInt(msgSetDialog.getEtValue().getText().toString());
                    getLocalMsg(datetime, count, pullOrder);
                    msgSetDialog.cancel();
                });

                msgSetDialog.getCancelView().setOnClickListener(v12 -> msgSetDialog.cancel());
                msgSetDialog.show();
                break;
            default:
                break;
        }
    }

    private void getLocalMsg(long datetime, int count, int pullOrder) {
        HistoryMessageOption historyMessageOption = new HistoryMessageOption();
        historyMessageOption.setDataTime(datetime);
        historyMessageOption.setCount(count);
        HistoryMessageOption.PullOrder order = HistoryMessageOption.PullOrder.ASCEND;
        if (pullOrder == 0) {
            order = HistoryMessageOption.PullOrder.DESCEND;
        }
        historyMessageOption.setOrder(order);

        RongCoreClient.getInstance().getMessages(conversationType, userId, historyMessageOption, (messageList, errorCode) -> {
            addToList(getStringDate() + "获取消息是否成功:" + errorCode.getValue());
            if (messageList == null || messageList.isEmpty()) {
                return;
            }
            for (Message message : messageList) {
                addToList(getStringDate() + "获取消息内容:" + message.getContent().toString()
                        + ", 消息 uid :" + message.getUId() + ", 消息 id :" + message.getMessageId()
                        + "消息发送时间 ：" + message.getSentTime());
            }
        });
    }


    private void initView() {
        shortage = findViewById(R.id.btn_set_key);
        shortage.setOnClickListener(this);
    }

    private void addToList(String str) {
        contentList.add(str);
        handler.post(() -> mAdapter.notifyDataSetChanged());
        handler.postDelayed(() -> {
            if (lvContent != null && mAdapter != null) {
                lvContent.setSelection(mAdapter.getCount() - 1);
                RLog.e("addToList", "**" + mAdapter.getCount() + "**" + contentList.size());
            }
        }, 300);
    }


    private String getStringDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(currentTime);
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return contentList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
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
            tvCotent.setText(contentList.get(position));
            return convertView;
        }
    }
}
