package cn.rongcloud.im.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.test.DataTimePickerDialog;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.utils.log.SLog;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imlib.ChannelClient;
import io.rong.imlib.IRongCoreCallback;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageDigestInfo;
import io.rong.imlib.model.UserInfo;
import io.rong.message.TextMessage;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** 超级群未读@消息摘要测试页面 */
public class UltraGroupUnreadMentionDigestsActivity extends FragmentActivity {

    private static final String TAG = "UltraMentionDigests";

    private static final String format = "yyyy-MM-dd HH:mm:ss";

    EditText edit_targetID;
    EditText edit_channelID;
    TextView tv_sendTime;
    EditText edit_count;

    RecyclerView rv_digestsList;
    DigestAdapter digestAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity_ultra_group_unread_digests);
        edit_targetID = findViewById(R.id.edit_targetID);
        edit_channelID = findViewById(R.id.edit_channelID);
        tv_sendTime = findViewById(R.id.edit_sendTime);
        tv_sendTime.setOnClickListener(v -> showSelectStartTime());
        edit_count = findViewById(R.id.edit_count);
        rv_digestsList = findViewById(R.id.rc_digests_list);

        digestAdapter = new DigestAdapter(this);
        rv_digestsList.setAdapter(digestAdapter);
        rv_digestsList.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        findViewById(R.id.btn_1)
                .setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                long sendTime = 0;
                                if (tv_sendTime.getText() != null
                                        && !TextUtils.isEmpty(
                                                tv_sendTime.getText().toString().trim())) {
                                    sendTime =
                                            date2TimeStampMillis(tv_sendTime.getText().toString());
                                }
                                int count = 0;
                                if (edit_count.getText() != null
                                        && !TextUtils.isEmpty(
                                                edit_count.getText().toString().trim())) {
                                    try {
                                        count =
                                                Integer.parseInt(
                                                        edit_count.getText().toString().trim());
                                    } catch (NumberFormatException e) {
                                    }
                                }
                                String targetID = "";
                                String channelID = "";
                                try {
                                    targetID = edit_targetID.getText().toString().trim();
                                    channelID = edit_channelID.getText().toString().trim();
                                } catch (Exception e) {
                                }
                                SLog.i(
                                        TAG,
                                        "onClick: btn_1: "
                                                + " , targetID："
                                                + targetID
                                                + " , channelID："
                                                + channelID
                                                + " , sendTime："
                                                + sendTime
                                                + " , sendTime："
                                                + tv_sendTime.getText().toString()
                                                + " , count："
                                                + count);

                                ChannelClient.getInstance()
                                        .getUltraGroupUnreadMentionedDigests(
                                                targetID,
                                                channelID,
                                                sendTime,
                                                count,
                                                new IRongCoreCallback.ResultCallback<
                                                        List<MessageDigestInfo>>() {
                                                    @Override
                                                    public void onSuccess(
                                                            List<MessageDigestInfo>
                                                                    messageDigestInfos) {
                                                        SLog.i(
                                                                TAG,
                                                                "getUltraGroupUnreadMentionedDigests onSuccess: "
                                                                        + messageDigestInfos);
                                                        digestAdapter.setData(null, null);
                                                        if (!messageDigestInfos.isEmpty()) {
                                                            getDigestMsgList(messageDigestInfos);
                                                        } else {
                                                            ToastUtils.showToast(
                                                                    "这次拉取到 "
                                                                            + messageDigestInfos
                                                                                    .size()
                                                                            + " 条数据");
                                                        }
                                                    }

                                                    @Override
                                                    public void onError(
                                                            IRongCoreEnum.CoreErrorCode e) {
                                                        SLog.i(
                                                                TAG,
                                                                "getUltraGroupUnreadMentionedDigests onError: "
                                                                        + e);
                                                        ToastUtils.showToast("请求接口报错：" + e);
                                                    }
                                                });
                            }
                        });
        findViewById(R.id.btn_2)
                .setOnClickListener(
                        v ->
                                startActivity(
                                        new Intent(
                                                UltraGroupUnreadMentionDigestsActivity.this,
                                                UltraGroupConversationListPickerActivity.class)));
    }

    /** 开始时间选择 */
    private void showSelectStartTime() {
        final DataTimePickerDialog testPopWindow = new DataTimePickerDialog(this);
        testPopWindow.setPositiveListener(curDate -> tv_sendTime.setText(curDate));
        testPopWindow.show();
    }

    public static long date2TimeStampMillis(String date) {
        try {
            return new SimpleDateFormat(format).parse(date).getTime();
        } catch (ParseException e) {
        }
        return 0L;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (UltraGroupConversationListPickerActivity.conversation != null) {
            edit_targetID.setText(
                    UltraGroupConversationListPickerActivity.conversation.mCore.getTargetId());
            edit_channelID.setText(
                    UltraGroupConversationListPickerActivity.conversation.mCore.getChannelId());
            UltraGroupConversationListPickerActivity.conversation = null;
        }
    }

    private void getDigestMsgList(List<MessageDigestInfo> messageDigestInfos) {
        List<Message> list = new ArrayList<>();
        for (MessageDigestInfo info : messageDigestInfos) {
            Message message =
                    Message.obtain(
                            info.getTargetId(),
                            info.getConversationType(),
                            info.getChannelId(),
                            null);
            message.setUId(info.getMessageUid());
            message.setSentTime(info.getSentTime());
            list.add(message);
        }
        ChannelClient.getInstance()
                .getBatchRemoteUltraGroupMessages(
                        list,
                        new IRongCoreCallback.IGetBatchRemoteUltraGroupMessageCallback() {
                            @Override
                            public void onSuccess(
                                    List<Message> matchedMsgList, List<Message> notMatchedMsgList) {
                                SLog.i(
                                        TAG,
                                        "getBatchRemoteUltraGroupMessages onSuccess: "
                                                + matchedMsgList);
                                new Handler(Looper.getMainLooper())
                                        .post(
                                                new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        ToastUtils.showToast(
                                                                "这次拉取到 "
                                                                        + matchedMsgList.size()
                                                                        + " 条数据");
                                                        digestAdapter.setData(
                                                                matchedMsgList, messageDigestInfos);
                                                    }
                                                });
                            }

                            @Override
                            public void onError(IRongCoreEnum.CoreErrorCode errorCode) {
                                SLog.i(
                                        TAG,
                                        "getBatchRemoteUltraGroupMessages onError: " + errorCode);
                            }
                        });
    }

    private static class DigestAdapter extends RecyclerView.Adapter<DigestHolder> {

        private Context mContext;
        private List<Message> data;
        private List<MessageDigestInfo> messageDigestInfos;

        public DigestAdapter(Context context) {
            mContext = context;
            data = new ArrayList<>();
            messageDigestInfos = new ArrayList<>();
        }

        @NonNull
        @Override
        public DigestHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new DigestHolder(
                    LayoutInflater.from(mContext)
                            .inflate(R.layout.select_unread_digest_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull DigestHolder holder, final int position) {
            final Message message = data.get(position);
            holder.itemView.setOnClickListener(
                    v -> {
                        // TODO: 2022/8/3
                    });
            UserInfo userInfo =
                    RongUserInfoManager.getInstance().getUserInfo(message.getSenderUserId());
            holder.tv_name.setText(
                    userInfo != null ? userInfo.getName() : message.getSenderUserId());
            holder.tv_time.setText(
                    new SimpleDateFormat(format).format(new Date(message.getSentTime())));
            holder.tv_msg_content.setText(
                    message.getContent() instanceof TextMessage
                            ? "消息内容：" + ((TextMessage) message.getContent()).getContent()
                            : message.getContent().getClass().getSimpleName());
            holder.tv_is_all.setText(
                    "类型是否为@所有人：" + messageDigestInfos.get(position).isMentionAll());
        }

        public void setData(
                List<Message> matchedMsgList, List<MessageDigestInfo> messageDigestInfos) {
            this.data.clear();
            if (matchedMsgList != null && !matchedMsgList.isEmpty()) {
                this.data.addAll(matchedMsgList);
            }
            this.messageDigestInfos.clear();
            if (messageDigestInfos != null && !messageDigestInfos.isEmpty()) {
                this.messageDigestInfos.addAll(messageDigestInfos);
            }
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    private static class DigestHolder extends RecyclerView.ViewHolder {
        public TextView tv_name;
        public TextView tv_time;
        public TextView tv_msg_content;
        public TextView tv_is_all;

        public DigestHolder(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_time = itemView.findViewById(R.id.tv_time);
            tv_msg_content = itemView.findViewById(R.id.tv_msg_content);
            tv_is_all = itemView.findViewById(R.id.tv_is_all);
        }
    }
}
