package cn.rongcloud.im.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.utils.log.SLog;
import io.rong.imlib.ChannelClient;
import io.rong.imlib.IRongCoreCallback;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.model.ConversationUnreadInfo;
import java.util.List;

public class ConversationListByTargetIdsActivity extends TitleBaseActivity
        implements View.OnClickListener {

    public static final String TAG = "ConversationListByTargetIdsActivity";
    protected String title;
    protected RecyclerView rvConversationList;
    protected EditText edit_targetID;

    public static void start(Activity activity, String title) {
        Intent intent = new Intent(activity, ConversationListByTargetIdsActivity.class);
        intent.putExtra(IntentExtra.TITLE, title);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list_by_target);

        Intent intent = getIntent();
        if (intent == null) {
            SLog.e(TAG, "intent is null, finish " + TAG);
            return;
        }
        title = getIntent().getStringExtra(IntentExtra.TITLE);
        initView();
    }

    protected void initView() {
        if (!title.isEmpty()) {
            getTitleBar().setTitle(title);
        }
        edit_targetID = findViewById(R.id.edit_targetID);
        findViewById(R.id.btn_confirm).setOnClickListener(this);
        rvConversationList = findViewById(R.id.rv_conversation_list);
        rvConversationList.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    @Override
    public void onClick(View v) {
        String targetID = null;
        try {
            targetID = edit_targetID.getText().toString().trim();
        } catch (Exception e) {
            SLog.e(TAG, "targetID e: " + e);
        }
        if (targetID == null || targetID.isEmpty()) {
            ToastUtils.showToast("请输入targetID");
            return;
        }
        String[] targetIDs = targetID.split(",");
        ChannelClient.getInstance()
                .getUltraGroupConversationUnreadInfoList(
                        targetIDs,
                        new IRongCoreCallback.ResultCallback<List<ConversationUnreadInfo>>() {
                            @Override
                            public void onSuccess(List<ConversationUnreadInfo> conversations) {
                                ToastUtils.showToast("成功");
                                printConversationUnreadInfo(conversations);
                            }

                            @Override
                            public void onError(IRongCoreEnum.CoreErrorCode e) {
                                ToastUtils.showToast("失败，e: " + e);
                                SLog.e(
                                        TAG,
                                        "getUltraGroupConversationListOfTargetIds onError: " + e);
                            }
                        });
    }

    protected void printConversationUnreadInfo(
            List<ConversationUnreadInfo> conversationUnreadInfoList) {
        if (conversationUnreadInfoList == null) {
            SLog.e(TAG, "getUltraGroupConversationUnreadInfoList info null");
            return;
        }
        if (conversationUnreadInfoList.isEmpty()) {
            SLog.e(TAG, "getUltraGroupConversationUnreadInfoList info isEmpty");
            return;
        }
        SLog.e(TAG, "getUltraGroupConversationUnreadInfoList start");
        for (int i = 0; i < conversationUnreadInfoList.size(); i++) {
            ConversationUnreadInfo c = conversationUnreadInfoList.get(i);
            if (c != null) {
                SLog.e(
                        TAG,
                        "第"
                                + i
                                + "个 ConversationUnreadInfo："
                                + " targetId:"
                                + c.getTargetId()
                                + " type:"
                                + c.getType()
                                + " channelId:"
                                + c.getChannelId()
                                + " unreadCount:"
                                + c.getUnreadMessageCount()
                                + " mentionCount:"
                                + c.getMentionedCount()
                                + " mentionMsg:"
                                + c.getMentionedMeCount()
                                + " pushLevel:"
                                + c.getPushNotificationLevel());
            } else {
                SLog.e(TAG, "第" + i + "个 ConversationUnreadInfo: null");
            }
        }
        SLog.e(TAG, "getUltraGroupConversationUnreadInfoList end");
    }
}
