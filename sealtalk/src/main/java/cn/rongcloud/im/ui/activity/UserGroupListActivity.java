package cn.rongcloud.im.ui.activity;

import static cn.rongcloud.im.ui.adapter.AbsSelectedAdapter.VIEW_TYPE_NORMAL;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.model.UserGroupInfo;
import cn.rongcloud.im.sp.UltraGroupCache;
import cn.rongcloud.im.ui.adapter.UserGroupListAdapter;
import cn.rongcloud.im.ui.adapter.models.UserGroupModel;
import cn.rongcloud.im.ui.dialog.CommonDialog;
import cn.rongcloud.im.ui.interfaces.OnAdapterItemClickListener;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.utils.log.SLog;
import cn.rongcloud.im.viewmodel.UserGroupViewModel;
import io.rong.imlib.model.ConversationIdentifier;
import java.util.List;

public class UserGroupListActivity extends TitleBaseActivity
        implements OnAdapterItemClickListener<UserGroupModel> {

    public static final String TAG = "UserGroupListActivity";
    protected ConversationIdentifier conversationIdentifier;
    protected String title;
    protected UserGroupViewModel userGroupViewModel;
    protected RecyclerView rvContent;
    protected UserGroupListAdapter adapter;
    protected TextView confirm;

    public static void start(Activity activity, ConversationIdentifier identifier, String title) {
        Intent intent = new Intent(activity, UserGroupListActivity.class);
        intent.putExtra(IntentExtra.SERIA_CONVERSATION_IDENTIFIER, identifier);
        intent.putExtra(IntentExtra.TITLE, title);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_group_list);

        Intent intent = getIntent();
        if (intent == null) {
            SLog.e(TAG, "intent is null, finish " + TAG);
            return;
        }
        title = getIntent().getStringExtra(IntentExtra.TITLE);
        conversationIdentifier = initConversationIdentifier();
        if (conversationIdentifier.isValid()) {
            SLog.e(TAG, "targetId or conversationType is null, finish" + TAG);
            return;
        }

        initView();
        initViewModel();
    }

    protected void initView() {
        if (!title.isEmpty()) {
            getTitleBar().setTitle(title);
        } else {
            getTitleBar().setTitle(R.string.rc_user_group_list);
        }
        confirm = findViewById(R.id.btn_confirm);
        confirm.setText(R.string.rc_create_user_group);
        confirm.setOnClickListener(
                view -> {
                    UserGroupInfo userGroupInfo = new UserGroupInfo();
                    userGroupInfo.userGroupName = "";
                    UserGroupEditActivity.start(
                            this,
                            conversationIdentifier,
                            userGroupInfo,
                            "创建用户组",
                            true,
                            sourceCode());
                });
        // 非群主不显示
        confirm.setVisibility(UltraGroupCache.isGroupOwner(this) ? View.VISIBLE : View.GONE);
        rvContent = findViewById(R.id.rv_content);
        adapter = new UserGroupListAdapter();
        adapter.setItemClickListener(this);
        rvContent.setAdapter(adapter);
        rvContent.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    protected void initViewModel() {
        userGroupViewModel = ViewModelProviders.of(this).get(UserGroupViewModel.class);
        userGroupViewModel.inject(conversationIdentifier);
        userGroupViewModel
                .getUserGroupListResult()
                .observe(
                        this,
                        new Observer<Resource<List<UserGroupInfo>>>() {
                            @Override
                            public void onChanged(Resource<List<UserGroupInfo>> listResource) {
                                if (listResource.status == Status.LOADING) {
                                    showLoadingDialog("加载中");
                                    return;
                                }
                                dismissLoadingDialog();
                                if (listResource.status == Status.SUCCESS) {
                                    adapter.setUserGroupInfoList(
                                            listResource.data, VIEW_TYPE_NORMAL);
                                } else {
                                    ToastUtils.showToast("获取失败，请退出重试");
                                }
                            }
                        });
        userGroupViewModel
                .getUserGroupDelResult()
                .observe(
                        this,
                        new Observer<Resource<String>>() {
                            @Override
                            public void onChanged(Resource<String> delResource) {
                                if (delResource.status == Status.LOADING) {
                                    showLoadingDialog("加载中");
                                    return;
                                }
                                dismissLoadingDialog();
                                if (delResource.status == Status.SUCCESS) {
                                    ToastUtils.showToast("删除用户组成功");
                                    userGroupViewModel.getUserGroupList(conversationIdentifier);
                                } else {
                                    ToastUtils.showToast("删除用户组失败");
                                }
                            }
                        });
        userGroupViewModel.getUserGroupList(conversationIdentifier);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        userGroupViewModel.getUserGroupList(conversationIdentifier);
    }

    @Override
    public void onItemClick(int position, UserGroupModel model) {
        if (!UltraGroupCache.isGroupOwner(this)) {
            return;
        }
        UserGroupEditActivity.start(
                this, conversationIdentifier, model.getBean(), "编辑用户组", false, sourceCode());
    }

    protected int sourceCode() {
        return UserGroupEditActivity.SOURCE_USER_GROUP_LIST;
    }

    @Override
    public void onItemLongClick(int position, UserGroupModel model) {
        if (!UltraGroupCache.isGroupOwner(this)) {
            return;
        }
        showDelUserGroupDialog(model.getBean());
    }

    private void showDelUserGroupDialog(UserGroupInfo userGroupInfo) {
        new CommonDialog.Builder()
                .setContentMessage("是否删除用户组?")
                .setDialogButtonClickListener(
                        new CommonDialog.OnDialogButtonClickListener() {
                            @Override
                            public void onPositiveClick(View v, Bundle bundle) {
                                userGroupViewModel.userGroupDel(
                                        conversationIdentifier.getTargetId(),
                                        userGroupInfo.userGroupId);
                            }

                            @Override
                            public void onNegativeClick(View v, Bundle bundle) {}
                        })
                .build()
                .show(getSupportFragmentManager(), "del_user_group_dialog");
    }
}
