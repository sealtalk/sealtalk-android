package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.ultraGroup.UltraGroupManager;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.viewmodel.UltraGroupViewModel;
import io.rong.imkit.IMCenter;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imlib.ChannelClient;
import io.rong.imlib.IRongCoreCallback;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.InformationNotificationMessage;
import java.util.ArrayList;
import java.util.List;

public class SelectUltraCreateGroupActivity extends SelectMultiFriendsActivity {
    private UltraGroupViewModel ultraGroupViewModel;
    private String groupId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().setTitle(getString(R.string.seal_select_group_member));
        initUltraViewModel();
        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");
    }

    @Override
    protected void onConfirmClicked(ArrayList<String> selectIds, ArrayList<String> selectGroups) {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(IntentExtra.LIST_STR_ID_LIST, selectIds);
        setResult(RESULT_OK, intent);
        if (ultraGroupViewModel != null) {
            ultraGroupViewModel.addUltraGroupMember(groupId, selectIds);
        }
    }

    public void initUltraViewModel() {
        ultraGroupViewModel = ViewModelProviders.of(this).get(UltraGroupViewModel.class);
        ultraGroupViewModel
                .getUltraGroupMemberAddResult()
                .observe(
                        this,
                        listResource -> {
                            if (listResource.status == Status.LOADING
                                    || listResource.data == null) {
                                return;
                            }

                            if (listResource.status == Status.SUCCESS) {
                                processCreateResult(listResource.data);
                            } else {
                                ToastUtils.showToast("邀请新居民失败");
                            }
                            finish();
                        });
    }

    private void processCreateResult(List<String> memberIds) {
        // RouteUtils.routeToConversationActivity(this, Conversation.ConversationType.ULTRA_GROUP,
        // groupId, "default");
        InformationNotificationMessage informationNotificationMessage;
        String currentName =
                RongUserInfoManager.getInstance()
                        .getUserInfo(RongIMClient.getInstance().getCurrentUserId())
                        .getName();
        StringBuilder stringBuilder = new StringBuilder();
        for (String memberId : memberIds) {
            stringBuilder
                    .append(RongUserInfoManager.getInstance().getUserInfo(memberId).getName())
                    .append(",");
        }
        informationNotificationMessage =
                InformationNotificationMessage.obtain(
                        currentName
                                + "邀请"
                                + stringBuilder.substring(0, stringBuilder.length() - 1)
                                + "进入本群");

        ChannelClient.getInstance()
                .getConversationListForAllChannel(
                        Conversation.ConversationType.ULTRA_GROUP,
                        groupId,
                        new IRongCoreCallback.ResultCallback<List<Conversation>>() {
                            @Override
                            public void onSuccess(List<Conversation> conversations) {
                                if (conversations == null) return;
                                new Thread(
                                                () -> {
                                                    for (Conversation conversation :
                                                            conversations) {
                                                        try {
                                                            Thread.sleep(200);
                                                        } catch (InterruptedException e) {
                                                            e.printStackTrace();
                                                        }
                                                        Message message =
                                                                Message.obtain(
                                                                        groupId,
                                                                        conversation
                                                                                .getConversationType(),
                                                                        conversation.getChannelId(),
                                                                        informationNotificationMessage);
                                                        IMCenter.getInstance()
                                                                .sendMessage(
                                                                        message, null, null, null);
                                                    }
                                                    runOnUiThread(
                                                            () ->
                                                                    UltraGroupManager.getInstance()
                                                                            .notifyGroupChange());
                                                    finish();
                                                })
                                        .start();
                            }

                            @Override
                            public void onError(IRongCoreEnum.CoreErrorCode e) {}
                        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
