package cn.rongcloud.im.ui.test;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.BaseActivity;
import cn.rongcloud.im.ui.dialog.MsgExtraInputDialog;
import io.rong.common.RLog;
import io.rong.imkit.RongIM;
import io.rong.imkit.feature.forward.CombineMessage;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.message.RecallNotificationMessage;
import io.rong.message.ReferenceMessage;
import io.rong.message.TextMessage;

public class MessageExpansionDetailActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "MessageDetailActivity";
    Button setKeyBtn, deleteKeyBtn, getLocalMsgBtn, getRemoteMsgBtn, sendMsgBtn;
    private ArrayList<String> contentList = new ArrayList<>();
    private MyAdapter mAdapter;
    private ListView lvContent;
    private Handler handler = new Handler();
    private String userId;
    private Conversation.ConversationType conversationType;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("消息扩展");
        setContentView(R.layout.activity_msg_extra_detail);
        intData();
        initView();
        mContext = this;
    }


    private void intData() {
        Intent intent = getIntent();
        if (intent == null) return;
        userId = intent.getStringExtra("uerid");
        conversationType = Conversation.ConversationType.setValue(intent.getIntExtra("conversationType", 1));
        RLog.i(TAG, "userId = " + userId);
        RLog.i(TAG, "conversationType = " + conversationType);
        lvContent = findViewById(R.id.lv_content);
        mAdapter = new MyAdapter();
        lvContent.setAdapter(mAdapter);

        RongIMClient.getInstance().setMessageExpansionListener(new RongIMClient.MessageExpansionListener() {
            @Override
            public void onMessageExpansionUpdate(Map<String, String> expansion, Message message) {
                addToList(getStringDate() + "扩展消息设置监听: UID " + message.getUId() + "设置 keys ：" + expansion
                        + "Expansion: " + message.getExpansion());
            }

            @Override
            public void onMessageExpansionRemove(List<String> keyArray, Message message) {
                addToList(getStringDate() + "扩展消息删除监听: UID " + message.getUId() + "删除 keys ：" + keyArray
                        + "Expansion: " + message.getExpansion());
            }
        });
    }

    private void initView() {
        setKeyBtn = findViewById(R.id.btn_set_key);
        deleteKeyBtn = findViewById(R.id.btn_delete_key);
        getLocalMsgBtn = findViewById(R.id.btn_local_msg);
        getRemoteMsgBtn = findViewById(R.id.btn_get_remote_msg);
        sendMsgBtn = findViewById(R.id.btn_send_msg);
        setKeyBtn.setOnClickListener(this);
        deleteKeyBtn.setOnClickListener(this);
        getLocalMsgBtn.setOnClickListener(this);
        getRemoteMsgBtn.setOnClickListener(this);
        sendMsgBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_set_key:
                Map<String, String> map = new HashMap<>();
                MsgExtraInputDialog msgSetDialog = new MsgExtraInputDialog(mContext, MsgExtraInputDialog.TYPE_SET);
                msgSetDialog.getSureView().setOnClickListener(v1 -> {
                    String uid = msgSetDialog.getEtUID().getText().toString();
                    String key = msgSetDialog.getEtKey().getText().toString();
                    String value = msgSetDialog.getEtValue().getText().toString();
                    if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
                        map.put(key, value);
                    }
                    RLog.i(TAG, "uid :" + uid + ",map :" + map);
                    // map.put("9", "yi988");
                    // uid = "BK3C-9V4M-L8U7-QRCS";
                    setMsgExtra(map, uid);
                    msgSetDialog.cancel();
                });

                msgSetDialog.getmTVAdd().setOnClickListener(v13 -> {
                    String key = msgSetDialog.getEtKey().getText().toString();
                    String value = msgSetDialog.getEtValue().getText().toString();
                    map.put(key, value);
                    msgSetDialog.getEtKey().getText().clear();
                    msgSetDialog.getEtValue().getText().clear();

                });
                msgSetDialog.getCancelView().setOnClickListener(v12 -> msgSetDialog.cancel());

                msgSetDialog.show();
                break;
            case R.id.btn_delete_key:
                List<String> list = new ArrayList<>();
                MsgExtraInputDialog msgDeleteDialog = new MsgExtraInputDialog(mContext, MsgExtraInputDialog.TYPE_DELETE);
                msgDeleteDialog.getSureView().setOnClickListener(v1 -> {
                    String uid = msgDeleteDialog.getEtUID().getText().toString();
                    String key = msgDeleteDialog.getEtKey().getText().toString();
                    if (!TextUtils.isEmpty(key)) {
                        list.add(key);
                    }
                    //list.add("9");
                    // uid = "BK3C-9V4M-L8U7-QRCS";
                    deleteKey(list, uid);
                    RLog.i(TAG, "uid : " + uid + ",key = " + key + ",list :" + list);

                    msgDeleteDialog.cancel();
                });

                msgDeleteDialog.getCancelView().setOnClickListener(v12 -> msgDeleteDialog.cancel());

                msgDeleteDialog.getmTVAdd().setOnClickListener(v13 -> {
                    list.add(msgDeleteDialog.getEtKey().getText().toString());
                    msgDeleteDialog.getEtKey().getText().clear();
                });

                msgDeleteDialog.show();
                break;
            case R.id.btn_local_msg:
                getLocalMsg();
                break;
            case R.id.btn_get_remote_msg:
                getRemoteMsg();
                break;
            case R.id.btn_send_msg:
                Map<String, String> mapSend = new HashMap<>();
                MsgExtraInputDialog msgExtraInputDialog = new MsgExtraInputDialog(mContext, MsgExtraInputDialog.TYPE_SEND_MESSAGE);
                msgExtraInputDialog.getSureView().setOnClickListener(v1 -> {
                    String key = msgExtraInputDialog.getEtKey().getText().toString();
                    String value = msgExtraInputDialog.getEtValue().getText().toString();
                    mapSend.put(key, value);
                    RLog.i(TAG, "mapSend :" + mapSend);
                    sendTextMsg(msgExtraInputDialog.getEtMsgContent().getText().toString(), mapSend);
                    msgExtraInputDialog.cancel();
                });

                msgExtraInputDialog.getmTVAdd().setOnClickListener(v13 -> {
                    String key = msgExtraInputDialog.getEtKey().getText().toString();
                    String value = msgExtraInputDialog.getEtValue().getText().toString();
                    mapSend.put(key, value);
                    msgExtraInputDialog.getEtKey().getText().clear();
                    msgExtraInputDialog.getEtValue().getText().clear();

                });

                msgExtraInputDialog.getCancelView().setOnClickListener(v12 -> msgExtraInputDialog.cancel());

                msgExtraInputDialog.show();
                break;
            default:
                break;

        }
    }

    private void getRemoteMsg() {
        RongIM.getInstance().getRemoteHistoryMessages(conversationType, userId, 0, 3, new RongIMClient.ResultCallback<List<Message>>() {
            @Override
            public void onSuccess(List<Message> messages) {
                if (messages == null) return;
                for (Message message : messages) {
                    String messageContent = "";
                    if (message.getContent() instanceof TextMessage) {
                        messageContent = ((TextMessage) message.getContent()).getContent();
                    }
                    addToList(getStringDate() + "获取远端消息内容:" + messageContent
                            + ", " + message.getUId() + ", Expansion :" + message.getExpansion());
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode e) {

            }
        });
    }

    private void getLocalMsg() {
        RongIM.getInstance().getHistoryMessages(conversationType, userId, -1, 5, new RongIMClient.ResultCallback<List<Message>>() {
            @Override
            public void onSuccess(List<Message> messages) {
                for (Message message : messages) {
                    if (message.getContent() instanceof TextMessage) {
                        RLog.i(TAG, "message.getUId() = " + message.getUId());
                        RLog.i(TAG, "message.getContent = " + (((TextMessage) message.getContent())).getContent());
                        String messageContent = ((TextMessage) message.getContent()).getContent();
                        addToList(getStringDate() + "获取本地消息内容 Expansion:" + message.getExpansion()
                                + ", UID " + message.getUId() + ", Content: " + messageContent);
                    } else if (message.getContent() instanceof CombineMessage) {
                        addToList(getStringDate() + "获取本地合并转发消息内容 Expansion:" + message.getExpansion()
                                + ", UID " + message.getUId());
                    } else if (message.getContent() instanceof ReferenceMessage) {
                        MessageContent messageContent = ((ReferenceMessage) message.getContent()).getReferenceContent();
                        if (messageContent instanceof TextMessage) {
                            addToList(getStringDate() + "获取本地引用消息内容 Expansion:" + message.getExpansion()
                                    + ", UID " + message.getUId() + ", Content: " + ((ReferenceMessage) message.getContent()).getEditSendText());
                        }
                    } else if (message.getContent() instanceof RecallNotificationMessage) {
                        addToList(getStringDate() + "获取本地撤回消息内容 Expansion:" + message.getExpansion()
                                + ", UID " + message.getUId() + ", 撤回内容: " + ((RecallNotificationMessage)message.getContent()).getRecallContent());
                    }

                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode e) {

            }
        });
    }

    private void setMsgExtra(Map<String, String> map, String uid) {
        RongIMClient.getInstance().updateMessageExpansion(map, uid, new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
                RongIMClient.getInstance().getMessageByUid(uid, new RongIMClient.ResultCallback<Message>() {
                    @Override
                    public void onSuccess(Message message) {
                        addToList(getStringDate() + "设置 Key：:" + map
                                + ", " + message.getUId() + ", " + message.getTargetId() + " , expansion " + message.getExpansion());
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode e) {

                    }
                });
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                Toast.makeText(getApplicationContext(), "设置失败，ErrorCode : " + errorCode.getValue(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void deleteKey(List<String> list, String uid) {
        RongIMClient.getInstance().removeMessageExpansion(list, uid, new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
                RongIMClient.getInstance().getMessageByUid(uid, new RongIMClient.ResultCallback<Message>() {
                    @Override
                    public void onSuccess(Message message) {
                        addToList(getStringDate() + "删除 Key:" + list
                                + ", " + message.getUId() + ", " + message.getTargetId() + " , messageExpansion" +
                                message.getExpansion());
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode e) {

                    }
                });
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                Toast.makeText(getApplicationContext(), "删除失败，ErrorCode : " + errorCode.getValue(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addToList(String str) {
        contentList.add(str);
        handler.post(() -> mAdapter.notifyDataSetChanged());
        handler.postDelayed(() -> {
            if (lvContent != null && mAdapter != null) {
                lvContent.setSelection(mAdapter.getCount() - 1);
                Log.e("addToList", "**" + mAdapter.getCount() + "**" + contentList.size());
            }
        }, 300);
    }

    private void sendTextMsg(String content, Map<String, String> mapSend) {
        Log.i(TAG, "sendTextMsg");
        TextMessage textMessage = TextMessage.obtain(content);
        Log.i(TAG, "userId: " + userId);
        Log.i(TAG, "conversationType: " + conversationType);
        Message message = Message.obtain(userId, conversationType, textMessage);
        message.setCanIncludeExpansion(true);

        if (mapSend != null && mapSend.size() > 0) {
            message.setExpansion((HashMap<String, String>) mapSend);
        }
        RongIM.getInstance().sendMessage(message, null, null, new IRongCallback.ISendMessageCallback() {
            @Override
            public void onAttached(io.rong.imlib.model.Message message) {

            }

            @Override
            public void onSuccess(io.rong.imlib.model.Message message) {
                Toast.makeText(MessageExpansionDetailActivity.this, "发送消息成功", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "uid :" + message.getUId());
                MessageContent messageContent = message.getContent();
                if (messageContent instanceof TextMessage) {
                    addToList(formatTime(message.getSentTime()) + "发送消息: UID " + message.getUId()
                            + ", Content: " + ((TextMessage) messageContent).getContent() + ", Expansion: " + message.getExpansion());
                } else if (messageContent instanceof CombineMessage) {
                    addToList(formatTime(message.getSentTime()) + "发送消息: UID " + message.getUId()
                            + ", Expansion: " + message.getExpansion());
                }

            }

            @Override
            public void onError(io.rong.imlib.model.Message message, RongIMClient.ErrorCode errorCode) {
                Toast.makeText(MessageExpansionDetailActivity.this, "发送消息失败" + errorCode.getValue(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    public String getStringDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    public String formatTime(long time) {
        Date timeDate = new Date(time);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(timeDate);
        return dateString;
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
