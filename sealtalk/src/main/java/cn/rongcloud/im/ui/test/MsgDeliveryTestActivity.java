package cn.rongcloud.im.ui.test;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.BaseActivity;
import io.rong.common.RLog;
import io.rong.imkit.RongIM;
import io.rong.imlib.ChannelClient;
import io.rong.imlib.IRongCoreCallback;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.IRongCoreListener;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.GroupMessageDeliverUser;
import io.rong.imlib.model.Message;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressLint("SimpleDateFormat")
public class MsgDeliveryTestActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "MsgDeliveryTestActivity";
    Button getMessages;
    private final ArrayList<String> contentList = new ArrayList<>();
    private final ArrayList<Message> messageArrayList = new ArrayList<>();
    private MsgDeliveryTestActivity.MyAdapter mAdapter;
    private ListView lvContent;
    private final Handler handler = new Handler();
    private String userId;
    private Conversation.ConversationType conversationType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("消息送达");
        setContentView(R.layout.activity_msg_delivery);
        intData();
        initView();
    }

    private void intData() {
        Intent intent = getIntent();
        if (intent == null) return;

        userId = intent.getStringExtra("uerid");
        conversationType =
                Conversation.ConversationType.setValue(intent.getIntExtra("conversationType", 1));
        RLog.i(TAG, "userId = " + userId);
        RLog.i(TAG, "conversationType = " + conversationType);
        lvContent = findViewById(R.id.lv_content);
        mAdapter = new MsgDeliveryTestActivity.MyAdapter();
        lvContent.setAdapter(mAdapter);

        lvContent.setOnItemClickListener(
                (parent, view, position, id) -> {
                    String uid = messageArrayList.get(position).getUId();
                    String targetId = messageArrayList.get(position).getTargetId();
                    RLog.i(TAG, "uid = " + uid);
                    RLog.i(TAG, "targetId = " + targetId);
                    if (Conversation.ConversationType.PRIVATE.equals(conversationType)) {
                        getPrivateDeliveryTime(uid);
                    } else if (Conversation.ConversationType.GROUP.equals(conversationType)) {
                        getGroupDeliveryTime(uid, targetId);
                    }
                });
    }

    private void getGroupDeliveryTime(String uid, String targetId) {
        ChannelClient.getInstance()
                .getGroupMessageDeliverList(
                        uid,
                        targetId,
                        "",
                        new IRongCoreListener.IGetGroupMessageDeliverListCallback() {
                            @Override
                            public void onSuccess(
                                    int totalCount, List<GroupMessageDeliverUser> users) {
                                if (users == null || users.isEmpty()) {
                                    return;
                                }
                                StringBuilder content = new StringBuilder();
                                for (GroupMessageDeliverUser groupMessageDeliverUser : users) {
                                    content.append("当前 userId : ")
                                            .append(groupMessageDeliverUser.getUserId())
                                            .append("送达时间 : ")
                                            .append(
                                                    formatTime(
                                                            groupMessageDeliverUser
                                                                    .getDeliverTime()))
                                            .append(";");
                                }
                                String alertStr =
                                        "当前群聊消息的人数为: "
                                                + totalCount
                                                + ", 送达信息 ： "
                                                + content.toString();
                                AlertDialog.Builder builder =
                                        new AlertDialog.Builder(
                                                        MsgDeliveryTestActivity.this,
                                                        AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                                .setMessage(alertStr)
                                                .setPositiveButton(
                                                        getString(
                                                                io.rong
                                                                        .imkit
                                                                        .R
                                                                        .string
                                                                        .rc_dialog_ok),
                                                        (dialog, which) -> dialog.dismiss())
                                                .setCancelable(false);

                                final AlertDialog dialog = builder.create();
                                dialog.show();
                            }

                            @Override
                            public void onError(IRongCoreEnum.CoreErrorCode coreErrorCode) {
                                String alertStr = "获取消息送达时间失败， 错误码是 ： " + coreErrorCode;
                                AlertDialog.Builder builder =
                                        new AlertDialog.Builder(
                                                        MsgDeliveryTestActivity.this,
                                                        AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                                .setMessage(alertStr)
                                                .setPositiveButton(
                                                        getString(
                                                                io.rong
                                                                        .imkit
                                                                        .R
                                                                        .string
                                                                        .rc_dialog_ok),
                                                        (dialog, which) -> dialog.dismiss())
                                                .setCancelable(false);

                                final AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        });
    }

    private void getPrivateDeliveryTime(String uid) {
        ChannelClient.getInstance()
                .getPrivateMessageDeliverTime(
                        uid,
                        " ",
                        new IRongCoreCallback.ResultCallback<Long>() {
                            @Override
                            public void onSuccess(Long aLong) {
                                String alertStr = "当前消息的送达时间为: " + formatTime(aLong);
                                AlertDialog.Builder builder =
                                        new AlertDialog.Builder(
                                                        MsgDeliveryTestActivity.this,
                                                        AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                                .setMessage(alertStr)
                                                .setPositiveButton(
                                                        getString(
                                                                io.rong
                                                                        .imkit
                                                                        .R
                                                                        .string
                                                                        .rc_dialog_ok),
                                                        (dialog, which) -> dialog.dismiss())
                                                .setCancelable(false);

                                final AlertDialog dialog = builder.create();
                                dialog.show();
                            }

                            @Override
                            public void onError(IRongCoreEnum.CoreErrorCode e) {
                                String alertStr = "获取消息送达时间失败， 错误码是 ： " + e;
                                AlertDialog.Builder builder =
                                        new AlertDialog.Builder(
                                                        MsgDeliveryTestActivity.this,
                                                        AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                                .setMessage(alertStr)
                                                .setPositiveButton(
                                                        getString(
                                                                io.rong
                                                                        .imkit
                                                                        .R
                                                                        .string
                                                                        .rc_dialog_ok),
                                                        (dialog, which) -> dialog.dismiss())
                                                .setCancelable(false);

                                final AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        });
    }

    private void initView() {
        getMessages = findViewById(R.id.btn_messages);
        getMessages.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_messages) {
            getMessages();
        }
    }

    private void getMessages() {
        RongIM.getInstance()
                .getHistoryMessages(
                        conversationType,
                        userId,
                        -1,
                        20,
                        new RongIMClient.ResultCallback<List<Message>>() {
                            @Override
                            public void onSuccess(List<Message> messages) {
                                for (Message message : messages) {
                                    addToList(
                                            getStringDate()
                                                    + ", UID "
                                                    + message.getUId()
                                                    + ", 消息内容: "
                                                    + message.getContent());
                                }
                                messageArrayList.addAll(messages);
                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode e) {}
                        });
    }

    private void addToList(String str) {
        contentList.add(str);
        handler.post(() -> mAdapter.notifyDataSetChanged());
        handler.postDelayed(
                () -> {
                    if (lvContent != null && mAdapter != null) {
                        lvContent.setSelection(mAdapter.getCount() - 1);
                        Log.e("addToList", "**" + mAdapter.getCount() + "**" + contentList.size());
                    }
                },
                300);
    }

    public String getStringDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(currentTime);
    }

    public String formatTime(long time) {
        Date timeDate = new Date(time);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(timeDate);
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
                convertView =
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.item_msg_delivery_status, null);
            }
            TextView tvCotent = convertView.findViewById(R.id.tv_content);
            tvCotent.setText(contentList.get(position));
            return convertView;
        }
    }
}
