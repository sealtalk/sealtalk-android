package cn.rongcloud.im.ui.activity;

import static cn.rongcloud.im.ui.adapter.AbsSelectedAdapter.VIEW_TYPE_NORMAL;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.model.UserGroupInfo;
import cn.rongcloud.im.sp.UltraGroupCache;
import cn.rongcloud.im.utils.IntentDataTransferBinder;
import cn.rongcloud.im.utils.ToastUtils;
import io.rong.imlib.model.ConversationIdentifier;
import java.util.ArrayList;
import java.util.List;

public class UserGroupChannelListActivity extends UserGroupListActivity {

    public static final String TAG = "UserGroupChannelList";

    public static void start(Activity activity, ConversationIdentifier identifier, String title) {
        Intent intent = new Intent(activity, UserGroupChannelListActivity.class);
        intent.putExtra(IntentExtra.SERIA_CONVERSATION_IDENTIFIER, identifier);
        intent.putExtra(IntentExtra.TITLE, title);
        activity.startActivity(intent);
    }

    @Override
    protected void initView() {
        super.initView();
        // 非群主不显示
        if (!UltraGroupCache.isGroupOwner(this)) {
            return;
        }
        getTitleBar()
                .setOnBtnRightClickListener(
                        "提交",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                List<String> userGroupIds = new ArrayList<>();
                                for (UserGroupInfo groupInfo : adapter.getList()) {
                                    userGroupIds.add(groupInfo.userGroupId);
                                }
                                boolean result =
                                        userGroupViewModel.editChannelUserGroup(
                                                conversationIdentifier, userGroupIds);
                                if (!result) {
                                    ToastUtils.showToast("数据未变化，不需要处理");
                                }
                            }
                        });
        confirm.setText("编辑");
        confirm.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UserGroupBindChannelActivity.startActivityForResult(
                                UserGroupChannelListActivity.this,
                                conversationIdentifier,
                                new UserGroupInfo(),
                                adapter.getList());
                    }
                });
    }

    @Override
    protected void initViewModel() {
        super.initViewModel();
        userGroupViewModel
                .getChannelUserGroupBindResult()
                .observe(
                        this,
                        new Observer<Resource>() {
                            @Override
                            public void onChanged(Resource resource) {
                                if (resource.status == Status.LOADING) {
                                    showLoadingDialog("加载中");
                                    return;
                                }
                                dismissLoadingDialog();
                                if (resource.status == Status.SUCCESS) {
                                    ToastUtils.showToast("用户组绑定频道成功");
                                    userGroupViewModel.getUserGroupList(conversationIdentifier);
                                } else {
                                    ToastUtils.showToast("用户组绑定频道失败");
                                }
                            }
                        });
        userGroupViewModel
                .getChannelUserGroupUnBindResult()
                .observe(
                        this,
                        new Observer<Resource>() {
                            @Override
                            public void onChanged(Resource resource) {
                                if (resource.status == Status.LOADING) {
                                    showLoadingDialog("加载中");
                                    return;
                                }
                                dismissLoadingDialog();
                                if (resource.status == Status.SUCCESS) {
                                    ToastUtils.showToast("用户组解除绑定频道成功");
                                    userGroupViewModel.getUserGroupList(conversationIdentifier);
                                } else {
                                    ToastUtils.showToast("用户组解除绑定频道失败");
                                }
                            }
                        });
    }

    @Override
    protected int sourceCode() {
        return UserGroupEditActivity.SOURCE_USER_GROUP_CHANNEL_LIST;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == UserGroupBindChannelActivity.REQUEST_CODE) {
                IntentDataTransferBinder transferBinder = IntentExtra.extractIntentBinder(data);
                ArrayList<UserGroupInfo> list = (ArrayList<UserGroupInfo>) transferBinder.data;
                if (list == null) {
                    // 说明是从左上角按钮返回的，不处理
                } else {
                    Log.e(TAG, "onActivityResult:" + list.size() + " , " + list);
                    adapter.setUserGroupInfoList(list, VIEW_TYPE_NORMAL);
                }
            }
        }
    }
}
