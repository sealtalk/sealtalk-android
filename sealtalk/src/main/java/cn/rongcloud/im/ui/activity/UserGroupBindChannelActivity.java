package cn.rongcloud.im.ui.activity;

import static cn.rongcloud.im.ui.adapter.AbsSelectedAdapter.VIEW_TYPE_EDIT;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.UserGroupInfo;
import cn.rongcloud.im.ui.adapter.UserGroupListAdapter;
import cn.rongcloud.im.ui.adapter.models.CheckModel;
import cn.rongcloud.im.ui.adapter.models.CheckType;
import cn.rongcloud.im.ui.adapter.models.UserGroupModel;
import cn.rongcloud.im.ui.interfaces.OnAdapterItemClickListener;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.utils.log.SLog;
import cn.rongcloud.im.viewmodel.UserGroupViewModel;
import io.rong.imkit.utils.CharacterParser;
import io.rong.imlib.model.ConversationIdentifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class UserGroupBindChannelActivity extends TitleBaseActivity
        implements OnAdapterItemClickListener<UserGroupModel>, View.OnClickListener {

    public static final String TAG = "UserGroupListActivity";
    public static final int REQUEST_CODE = 997;

    private ConversationIdentifier conversationIdentifier;
    private HashSet<String> checkedSet = new HashSet<>();

    protected EditText etName;
    protected RecyclerView rvContent;
    protected UserGroupListAdapter adapter;
    protected UserGroupViewModel userGroupViewModel;

    public static void startActivityForResult(
            Activity activity,
            ConversationIdentifier identifier,
            UserGroupInfo userGroupInfo,
            ArrayList<UserGroupInfo> checkedList) {
        Intent intent = new Intent(activity, UserGroupBindChannelActivity.class);
        intent.putExtra(IntentExtra.SERIA_CONVERSATION_IDENTIFIER, identifier);
        intent.putExtra(IntentExtra.SERIA_USER_GROUP_INFO, userGroupInfo);
        intent.putExtra(
                IntentExtra.SERIA_USER_GROUP_CHECKED_LIST,
                checkedList != null ? checkedList : new ArrayList<UserGroupInfo>());
        activity.startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_group_bind_channel);

        Intent intent = getIntent();
        if (intent == null) {
            SLog.e(TAG, "intent is null, finish " + TAG);
            return;
        }
        conversationIdentifier = initConversationIdentifier();
        ArrayList<UserGroupInfo> checkedList =
                (ArrayList<UserGroupInfo>)
                        getIntent().getSerializableExtra(IntentExtra.SERIA_USER_GROUP_CHECKED_LIST);
        if (checkedList != null && !checkedList.isEmpty()) {
            for (UserGroupInfo memberInfo : checkedList) {
                checkedSet.add(memberInfo.userGroupId);
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
        getTitleBar().setTitle("绑定/解绑用户组");
        getTitleBar().setOnBtnLeftClickListener(v -> setCheckedResult(null));

        findViewById(R.id.rc_tv_finish).setOnClickListener(this);
        etName = findViewById(R.id.rc_edit_text);
        rvContent = findViewById(R.id.rc_rv_content);

        // 当前展示用户列表
        adapter = new UserGroupListAdapter();
        adapter.setItemClickListener(this);
        rvContent.setAdapter(adapter);
        rvContent.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

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
                            UserGroupInfo userGroupInfo = (UserGroupInfo) model.getBean();
                            String name = userGroupInfo.userGroupName;
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

    protected void initViewModel() {
        userGroupViewModel = ViewModelProviders.of(this).get(UserGroupViewModel.class);
        Log.e(TAG, "initViewModel: " + userGroupViewModel.hashCode());
        userGroupViewModel
                .getUserGroupListResult()
                .observe(
                        this,
                        (Resource<List<UserGroupInfo>> resource) -> {
                            adapter.setUserGroupInfoList(resource.data, checkedSet, VIEW_TYPE_EDIT);
                            Log.e(TAG, "initViewModel onChanged: ");
                        });
        userGroupViewModel.getUserGroupList(
                ConversationIdentifier.obtainUltraGroup(conversationIdentifier.getTargetId(), ""));
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.rc_tv_finish) {
            if (adapter.getModelList(CheckType.CHECKED).isEmpty()) {
                ToastUtils.showToast("未选择任何成员");
            }
            ArrayList<UserGroupInfo> checkedMemberInfoList = adapter.getCheckedUserGroupList();
            Log.e(
                    TAG,
                    "已选择:"
                            + checkedMemberInfoList.size()
                            + " , "
                            + adapter.getModelList(CheckType.CHECKED).toString());
            setCheckedResult(checkedMemberInfoList);
        }
    }

    private void setCheckedResult(ArrayList checkedList) {
        IntentExtra.setResultWithBinder(this, checkedList);
    }

    @Override
    public void onBackPressed() {}

    @Override
    public void onItemClick(int position, UserGroupModel model) {
        adapter.onItemClick(position, model);
    }
}
