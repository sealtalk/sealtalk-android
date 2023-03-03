package cn.rongcloud.im.ui.activity;

import static cn.rongcloud.im.ui.adapter.AbsSelectedAdapter.VIEW_TYPE_EDIT;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.model.UltraGroupMemberListResult;
import cn.rongcloud.im.model.UserGroupInfo;
import cn.rongcloud.im.model.UserGroupMemberInfo;
import cn.rongcloud.im.ui.adapter.MemberPortraitAdapter;
import cn.rongcloud.im.ui.adapter.UserGroupMemberAdapter;
import cn.rongcloud.im.ui.adapter.models.CheckModel;
import cn.rongcloud.im.ui.adapter.models.CheckType;
import cn.rongcloud.im.ui.adapter.models.SearchUserGroupMemberModel;
import cn.rongcloud.im.ui.interfaces.OnAdapterItemClickListener;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.utils.log.SLog;
import cn.rongcloud.im.viewmodel.UserGroupViewModel;
import io.rong.imkit.utils.CharacterParser;
import io.rong.imlib.model.ConversationIdentifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UserGroupEditMemberActivity extends TitleBaseActivity
        implements View.OnClickListener, OnAdapterItemClickListener<SearchUserGroupMemberModel> {

    public static final String TAG = "UserGroupEditMemberActivity";
    public static final int REQUEST_CODE = 998;

    private ConversationIdentifier conversationIdentifier;
    private UserGroupInfo userGroupInfo;
    private HashMap<String, UserGroupMemberInfo> checkedMap = new HashMap<>();

    UserGroupViewModel userGroupViewModel;

    EditText etName;
    RecyclerView rvContent;
    UserGroupMemberAdapter adapter;

    RecyclerView rvChecked;
    MemberPortraitAdapter portraitAdapter;

    public static void startActivityForResult(
            Activity activity,
            ConversationIdentifier identifier,
            UserGroupInfo userGroupInfo,
            ArrayList<UserGroupMemberInfo> checkedList) {
        Intent intent = new Intent(activity, UserGroupEditMemberActivity.class);
        intent.putExtra(IntentExtra.SERIA_CONVERSATION_IDENTIFIER, identifier);
        intent.putExtra(IntentExtra.SERIA_USER_GROUP_INFO, userGroupInfo);
        intent.putExtra(
                IntentExtra.SERIA_USER_GROUP_CHECKED_LIST,
                checkedList != null ? checkedList : new ArrayList<UserGroupMemberInfo>());
        activity.startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_group_edit_member);

        Intent intent = getIntent();
        if (intent == null) {
            SLog.e(TAG, "intent is null, finish " + TAG);
            return;
        }
        conversationIdentifier = initConversationIdentifier();
        userGroupInfo =
                (UserGroupInfo) getIntent().getSerializableExtra(IntentExtra.SERIA_USER_GROUP_INFO);
        ArrayList<UserGroupMemberInfo> checkedList =
                (ArrayList<UserGroupMemberInfo>)
                        getIntent().getSerializableExtra(IntentExtra.SERIA_USER_GROUP_CHECKED_LIST);
        if (checkedList != null) {
            for (UserGroupMemberInfo memberInfo : checkedList) {
                checkedMap.put(memberInfo.id, memberInfo);
            }
        }

        if (conversationIdentifier.isValid()) {
            SLog.e(TAG, "targetId or conversationType is null, finish" + TAG);
            return;
        }
        initView();
        initViewModel();
    }

    private void initView() {
        getTitleBar().setTitle("编辑用户组成员");
        getTitleBar().setOnBtnLeftClickListener(v -> setCheckedResult(null));

        findViewById(R.id.rc_tv_finish).setOnClickListener(this);
        etName = findViewById(R.id.rc_edit_text);
        rvContent = findViewById(R.id.rc_rv_content);
        rvChecked = findViewById(R.id.rc_rv_checked);

        // 当前展示用户列表
        adapter = new UserGroupMemberAdapter();
        adapter.setItemClickListener(this);
        rvContent.setAdapter(adapter);
        rvContent.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        // 已选择用户列表
        portraitAdapter = new MemberPortraitAdapter();
        rvChecked.setAdapter(portraitAdapter);
        rvChecked.setHasFixedSize(true);
        rvChecked.setLayoutManager(new GridLayoutManager(this, 6));

        etName.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        // do nothing
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        // 当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
                        if (TextUtils.isEmpty(s.toString())) {
                            adapter.resetOriginData();
                            return;
                        }

                        List<CheckModel> filterDataList = new ArrayList<>();
                        for (CheckModel model : adapter.getModelList(null)) {
                            UserGroupMemberInfo memberInfo = (UserGroupMemberInfo) model.getBean();
                            String name = memberInfo.nickname;
                            if (name != null
                                    && (name.contains(s)
                                            || CharacterParser.getInstance()
                                                    .getSelling(name)
                                                    .startsWith(s.toString()))) {
                                filterDataList.add(model);
                            }
                        }
                        adapter.setFilterData(filterDataList);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        // do nothing
                    }
                });
    }

    private void initViewModel() {
        userGroupViewModel = ViewModelProviders.of(this).get(UserGroupViewModel.class);
        userGroupViewModel.getUltraGroupMemberInfoList(conversationIdentifier.getTargetId());
        // 获取群组成员信息
        userGroupViewModel
                .getUltraGroupMemberInfoListResult()
                .observe(
                        this,
                        resource -> {
                            if (resource.status == Status.LOADING) {
                                return;
                            }
                            if (resource.data != null && !resource.data.isEmpty()) {
                                List<CheckModel> list = new ArrayList<>();
                                for (UltraGroupMemberListResult result : resource.data) {
                                    UserGroupMemberInfo info =
                                            new UserGroupMemberInfo(
                                                    result.user.id,
                                                    result.memberName,
                                                    result.user.portraitUri);
                                    CheckModel model =
                                            new SearchUserGroupMemberModel(info, VIEW_TYPE_EDIT);
                                    if (checkedMap.containsKey(info.id)) {
                                        model.setCheckType(CheckType.CHECKED);
                                    }
                                    list.add(model);
                                }
                                adapter.setData(list);
                                portraitAdapter.setData(adapter.getModelList(CheckType.CHECKED));
                            }

                            if (resource.status == Status.ERROR) {
                                ToastUtils.showErrorToast(resource.code);
                            }
                            if (resource.status == Status.SUCCESS && resource.data == null) {
                                //                                backToMain();
                                // TODO: 2023/1/11 空成员
                            }
                        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.rc_tv_finish) {
            if (adapter.getModelList(CheckType.CHECKED).isEmpty()) {
                ToastUtils.showToast("未选择任何成员");
            }
            setCheckedResult(adapter.getCheckedMemberInfoList());
        }
    }

    private void setCheckedResult(ArrayList<UserGroupMemberInfo> checkedList) {
        IntentExtra.setResultWithBinder(this, checkedList);
    }

    @Override
    public void onBackPressed() {}

    @Override
    public void onItemClick(int position, SearchUserGroupMemberModel model) {
        adapter.onItemClick(position, model);
        portraitAdapter.setData(adapter.getModelList(CheckType.CHECKED));
    }
}
