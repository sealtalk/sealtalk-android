package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.model.GroupMember;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.ui.adapter.ListGroupMemberAdapter;
import cn.rongcloud.im.ui.view.SealTitleBar;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.viewmodel.GroupMemberListViewModel;
import cn.rongcloud.im.utils.log.SLog;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imlib.model.Group;

/**
 * 群组成员一览界面
 */
public class GroupMemberListActivity extends TitleAndSearchBaseActivity {
    private final static String TAG = "GroupMemberListActivity";
    private SealTitleBar sealTitleBar;
    private ListView groupMemberList;
    private ListGroupMemberAdapter listGroupMemberAdapter;
    private GroupMemberListViewModel groupMemberListViewModel;

    private String groupId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sealTitleBar = getTitleBar();
        sealTitleBar.setTitle(R.string.profile_group_total_member);

        setContentView(R.layout.profile_activity_group_all_member);

        Intent intent = getIntent();
        if(intent == null) {
            SLog.e(TAG, "intent can not null ,to finish " + TAG);
            finish();
            return;
        }

        groupId = intent.getStringExtra(IntentExtra.STR_TARGET_ID);
        if(groupId == null) {
            SLog.e(TAG, "groupId can not null, to finish " + TAG);
            finish();
            return;
        }

        initView();
        initViewModel();
    }

    @Override
    public void onSearch(String keyword) {
        groupMemberListViewModel.requestGroupMember(keyword);

    }

    private void initView(){
        groupMemberList = findViewById(R.id.profile_lv_group_member_list);
        listGroupMemberAdapter = new ListGroupMemberAdapter(this);
        groupMemberList.setAdapter(listGroupMemberAdapter);
        groupMemberList.setOnItemClickListener((parent, view, position, id) -> {
            GroupMember groupMember = listGroupMemberAdapter.getItem(position);
            showGroupMemberInfo(groupMember);
        });

    }

    private void initViewModel(){
        groupMemberListViewModel = ViewModelProviders.of(this
                , new GroupMemberListViewModel.Factory(this.getApplication(), groupId)).get(GroupMemberListViewModel.class);

        // 获取并显示群组列表
        groupMemberListViewModel.getGroupMemberList().observe(this, resource -> {
            listGroupMemberAdapter.updateListView(resource.data);
        });

        // 获取群组信息
        groupMemberListViewModel.getGroupInfo().observe(this, resource -> {
            if (resource.data != null) {
                updateGroupInfoView(resource.data);
            }

            if (resource.status == Status.ERROR) {
                ToastUtils.showErrorToast(resource.code);
            }
        });
    }

    /**
     * 更新群信息
     *
     * @param groupInfo
     */
    private void updateGroupInfoView(GroupEntity groupInfo) {
        // 标题
        String title = getString(R.string.profile_group_total_member) + "(" + groupInfo.getMemberCount() + ")";
        sealTitleBar.setTitle(title);
    }

    /**
     * 显示群成员信息
     *
     * @param groupMember
     */
    private void showGroupMemberInfo(GroupMember groupMember){
        Intent intent = new Intent(this, UserDetailActivity.class);
        intent.putExtra(IntentExtra.STR_TARGET_ID, groupMember.getUserId());
        Group groupInfo = RongUserInfoManager.getInstance().getGroupInfo(groupId);
        if (groupInfo != null) {
            intent.putExtra(IntentExtra.STR_GROUP_NAME, groupInfo.getName());
        }
        startActivity(intent);
    }
}
