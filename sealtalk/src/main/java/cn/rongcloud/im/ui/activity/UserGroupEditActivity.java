package cn.rongcloud.im.ui.activity;

import static cn.rongcloud.im.ui.adapter.AbsSelectedAdapter.VIEW_TYPE_NORMAL;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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
import cn.rongcloud.im.model.UserGroupMemberInfo;
import cn.rongcloud.im.ui.adapter.UserGroupMemberAdapter;
import cn.rongcloud.im.ui.adapter.models.CheckModel;
import cn.rongcloud.im.ui.adapter.models.SearchUserGroupMemberModel;
import cn.rongcloud.im.utils.IntentDataTransferBinder;
import cn.rongcloud.im.utils.SingleSourceLiveData;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.utils.log.SLog;
import cn.rongcloud.im.viewmodel.UserGroupViewModel;
import io.rong.imlib.model.ConversationIdentifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class UserGroupEditActivity extends TitleBaseActivity implements View.OnClickListener {

    public static final String TAG = "UserGroupEditActivity";
    public static final int SOURCE_USER_GROUP_LIST = 1;
    public static final int SOURCE_USER_GROUP_CHANNEL_LIST = 2;

    protected ConversationIdentifier conversationIdentifier;
    protected UserGroupInfo userGroupInfo;
    protected String title;
    protected boolean canEditName;
    protected int sourceCode;

    protected UserGroupViewModel userGroupViewModel;
    protected EditText etUserGroupName;
    protected RecyclerView rvContent;
    protected UserGroupMemberAdapter adapter;

    public static void start(
            Activity activity,
            ConversationIdentifier identifier,
            UserGroupInfo userGroupInfo,
            String title,
            boolean canEditName,
            int sourceCode) {
        Intent intent = new Intent(activity, UserGroupEditActivity.class);
        intent.putExtra(IntentExtra.SERIA_CONVERSATION_IDENTIFIER, identifier);
        intent.putExtra(IntentExtra.SERIA_USER_GROUP_INFO, userGroupInfo);
        intent.putExtra(IntentExtra.SERIA_USER_GROUP_TITLE, title);
        intent.putExtra(IntentExtra.SERIA_USER_GROUP_CAN_EDIT, canEditName);
        intent.putExtra(IntentExtra.SERIA_USER_GROUP_SOURCE_CODE, sourceCode);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_group_edit);

        Intent intent = getIntent();
        if (intent == null) {
            SLog.e(TAG, "intent is null, finish " + TAG);
            return;
        }
        conversationIdentifier = initConversationIdentifier();
        userGroupInfo =
                (UserGroupInfo) intent.getSerializableExtra(IntentExtra.SERIA_USER_GROUP_INFO);
        title = intent.getStringExtra(IntentExtra.SERIA_USER_GROUP_TITLE);
        canEditName = intent.getBooleanExtra(IntentExtra.SERIA_USER_GROUP_CAN_EDIT, false);
        sourceCode =
                intent.getIntExtra(
                        IntentExtra.SERIA_USER_GROUP_SOURCE_CODE, SOURCE_USER_GROUP_LIST);
        if (conversationIdentifier.isValid()) {
            SLog.e(TAG, "targetId or conversationType is null, finish" + TAG);
            return;
        }

        initView();
        initViewModel();
    }

    protected void initView() {
        getTitleBar().setTitle(title);
        getTitleBar().setOnBtnRightClickListener("提交", v -> submit());
        etUserGroupName = findViewById(R.id.rc_user_group_name);
        etUserGroupName.setText(userGroupInfo.userGroupName);
        if (!canEditName) {
            onEditTextStatus(etUserGroupName);
        }
        rvContent = findViewById(R.id.rc_rv_content);
        adapter = new UserGroupMemberAdapter();
        rvContent.setAdapter(adapter);
        rvContent.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        findViewById(R.id.rc_btn_confirm).setOnClickListener(this);
    }

    protected void onEditTextStatus(EditText et) {
        et.setFocusable(false);
        et.setFocusableInTouchMode(false);
        et.setOnClickListener(null);
    }

    protected void submit() {
        if (etUserGroupName.getText() == null) {
            ToastUtils.showToast("请输入用户组名");
            return;
        }
        if (TextUtils.isEmpty(userGroupInfo.userGroupId)) {
            String groupName = etUserGroupName.getText().toString().trim();
            userGroupViewModel.userGroupAdd(conversationIdentifier, groupName);
        } else {
            userGroupMemberAdd(conversationIdentifier.getTargetId(), userGroupInfo.userGroupId);
        }
    }

    protected void initViewModel() {
        userGroupViewModel = ViewModelProviders.of(this).get(UserGroupViewModel.class);
        userGroupViewModel
                .getUserGroupAddResult()
                .observe(
                        this,
                        new Observer<Resource<String>>() {
                            @Override
                            public void onChanged(Resource<String> resource) {
                                if (resource.status == Status.LOADING) {
                                    showLoadingDialog("处理中");
                                    return;
                                }

                                if (resource.status == Status.SUCCESS) {
                                    ToastUtils.showToast("用户组创建成功");
                                    ArrayList<UserGroupMemberInfo> checkedList = getCheckedList();
                                    if (checkedList.isEmpty()) {
                                        refresh();
                                    } else {
                                        userGroupMemberAdd(
                                                conversationIdentifier.getTargetId(),
                                                resource.data);
                                    }
                                } else {
                                    ToastUtils.showToast("用户组创建失败");
                                    dismissLoadingDialog();
                                }
                            }
                        });

        userGroupViewModel
                .getUserGroupMemberAddResult()
                .observe(
                        this,
                        new Observer<Resource<String>>() {
                            @Override
                            public void onChanged(Resource<String> resource) {
                                if (resource.status == Status.SUCCESS) {
                                    ToastUtils.showToast("用户组成员添加成功");
                                    List<String> delList = getDelIdList();
                                    if (delList.isEmpty()) {
                                        dismissLoadingDialog();
                                        refresh();
                                    } else {
                                        userGroupViewModel.userGroupMemberDel(
                                                conversationIdentifier, resource.data, delList);
                                    }
                                } else if (resource.status == Status.ERROR) {
                                    dismissLoadingDialog();
                                    ToastUtils.showToast("用户组成员添加失败");
                                }
                            }
                        });

        userGroupViewModel
                .getUserGroupMemberDelResult()
                .observe(
                        this,
                        new Observer<Resource<String>>() {
                            @Override
                            public void onChanged(Resource<String> resource) {
                                dismissLoadingDialog();
                                if (resource.status == Status.SUCCESS) {
                                    ToastUtils.showToast("用户组成员删除成功");
                                    refresh();
                                } else if (resource.status == Status.ERROR) {
                                    ToastUtils.showToast("用户组成员添加失败");
                                }
                            }
                        });

        userGroupViewModel
                .getUserGroupMemberListResult()
                .observe(
                        this,
                        new Observer<Resource<List<UserGroupMemberInfo>>>() {
                            @Override
                            public void onChanged(Resource<List<UserGroupMemberInfo>> resource) {
                                if (resource.status == Status.LOADING) {
                                    showLoadingDialog("处理中");
                                    return;
                                }
                                dismissLoadingDialog();
                                if (resource.status == Status.SUCCESS) {
                                    setCheckedList(resource.data);
                                } else {
                                    ToastUtils.showToast("用户组成员列表获取异常");
                                }
                            }
                        });
        userGroupViewModel.userGroupMemberList(
                conversationIdentifier.getTargetId(), userGroupInfo.userGroupId);
    }

    protected void userGroupMemberAdd(String groupId, String userGroupId) {
        ArrayList<UserGroupMemberInfo> checkedList = getCheckedList();
        if (checkedList.isEmpty()) {
            return;
        }
        ArrayList<String> checkedIdList = new ArrayList<>();
        for (UserGroupMemberInfo info : checkedList) {
            checkedIdList.add(info.id);
        }
        userGroupViewModel.userGroupMemberAdd(groupId, userGroupId, checkedIdList);
    }

    protected void setCheckedList(List<UserGroupMemberInfo> checkedList) {
        if (checkedList == null || checkedList.isEmpty()) {
            adapter.clearData();
            return;
        }
        List<CheckModel> list = new ArrayList<>();
        for (UserGroupMemberInfo info : checkedList) {
            list.add(new SearchUserGroupMemberModel(info, VIEW_TYPE_NORMAL));
        }
        adapter.setData(list);
    }

    protected ArrayList<UserGroupMemberInfo> getCheckedList() {
        ArrayList<UserGroupMemberInfo> checkedList = new ArrayList<>();
        for (CheckModel model : adapter.getModelList(null)) {
            checkedList.add((UserGroupMemberInfo) model.getBean());
        }
        return checkedList;
    }

    protected List<String> getDelIdList() {
        ArrayList<String> delList = new ArrayList<>();

        SingleSourceLiveData<Resource<List<UserGroupMemberInfo>>> memberListResult =
                userGroupViewModel.getUserGroupMemberListResult();
        if (memberListResult == null || memberListResult.getValue() == null) {
            return delList;
        }
        List<UserGroupMemberInfo> memberList = memberListResult.getValue().data;
        if (memberList == null || memberList.isEmpty()) {
            return delList;
        }
        HashSet<String> checkedSet = new HashSet<>();
        for (UserGroupMemberInfo info : getCheckedList()) {
            checkedSet.add(info.id);
        }
        for (UserGroupMemberInfo info : memberList) {
            if (!checkedSet.contains(info.id)) {
                delList.add(info.id);
            }
        }
        return delList;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        onProcessActivityResult(requestCode, resultCode, data);
    }

    protected void onProcessActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == UserGroupEditMemberActivity.REQUEST_CODE) {
                IntentDataTransferBinder transferBinder = IntentExtra.extractIntentBinder(data);
                ArrayList<UserGroupMemberInfo> list =
                        (ArrayList<UserGroupMemberInfo>) transferBinder.data;
                if (list == null) {
                    // 说明是从左上角按钮返回的，不处理
                } else {
                    setCheckedList(list);
                    Log.e(TAG, "onActivityResult:" + list.size() + " , " + list);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.rc_btn_confirm) {
            UserGroupEditMemberActivity.startActivityForResult(
                    UserGroupEditActivity.this,
                    conversationIdentifier,
                    userGroupInfo,
                    getCheckedList());
        }
    }

    private void refresh() {
        finish();
        Class<?> cls =
                sourceCode == SOURCE_USER_GROUP_LIST
                        ? UserGroupListActivity.class
                        : UserGroupChannelListActivity.class;
        Intent intent = new Intent(this, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}
