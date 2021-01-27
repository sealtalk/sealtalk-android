package cn.rongcloud.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.Date;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.ErrorCode;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.ui.view.SealTitleBar;
import cn.rongcloud.im.utils.ImageLoaderUtils;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.viewmodel.JoinGroupViewModel;
import cn.rongcloud.im.utils.log.SLog;
import io.rong.imkit.RongIM;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imlib.model.Conversation;

/**
 * 加入群组界面
 */
public class JoinGroupActivity extends TitleBaseActivity implements View.OnClickListener {
    private final String TAG = "JoinGroupActivity";

    private ImageView groupPortaritIv;
    private TextView groupNameTv;
    private TextView groupMemberTv;
    private Button joinGroupBtn;

    private String groupId;
    private String groupName;
    private JoinGroupViewModel joinGroupViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SealTitleBar titleBar = getTitleBar();
        titleBar.setTitle(R.string.profile_join_the_group_chat);

        Intent intent = getIntent();
        if (intent == null) {
            SLog.e(TAG, "intent is null, to finish.");
            finish();
            return;
        }
        groupId = intent.getStringExtra(IntentExtra.STR_TARGET_ID);
        if (groupId == null) {
            SLog.e(TAG, "groupId is null, to finish.");
            finish();
            return;
        }


        setContentView(R.layout.profile_activity_join_group);

        initView();
        initViewModel();
    }

    private void initView() {
        groupPortaritIv = findViewById(R.id.profile_iv_join_group_portrait);
        groupNameTv = findViewById(R.id.profile_tv_join_group_name);
        groupMemberTv = findViewById(R.id.profile_tv_join_group_member);
        joinGroupBtn = findViewById(R.id.profile_btn_join_group_confirm);
        joinGroupBtn.setOnClickListener(this);
    }

    private void initViewModel() {
        joinGroupViewModel = ViewModelProviders.of(this, new JoinGroupViewModel.Factory(getApplication(), groupId)).get(JoinGroupViewModel.class);

        // 获取群组信息
        joinGroupViewModel.getGroupInfo().observe(this, new Observer<Resource<GroupEntity>>() {
            @Override
            public void onChanged(Resource<GroupEntity> resource) {
                if (resource.data != null) {
                    updateGroupInfo(resource.data);
                }

                if (resource.status == Status.ERROR) {
                    if (resource.code == ErrorCode.API_COMMON_ERROR.getCode()) {
                        ToastUtils.showToast(getString(R.string.profile_group_not_exist));
                    } else {
                        ToastUtils.showToast(resource.message);
                    }
                }
            }
        });


        // 获取加入群组结果
        joinGroupViewModel.getJoinGroupInfo().observe(this, new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> resource) {
                if (resource.status == Status.SUCCESS) {
                    // 群组中包含自己则跳转到群聊天界面
                    toGroupChat();
                } else if (resource.status == Status.ERROR) {
                    ToastUtils.showToast(resource.code);
                }
            }
        });
    }

    private void toGroupChat() {
        Bundle bundle = new Bundle();
        bundle.putString("title", groupName);
        RouteUtils.routeToConversationActivity(this, Conversation.ConversationType.GROUP, groupId, bundle);
        finish();
    }

    /**
     * 更新群信息
     *
     * @param groupInfo
     */
    private void updateGroupInfo(GroupEntity groupInfo) {
        Date deletedAt = groupInfo.getDeletedAt();
        if (deletedAt != null) {
            // 隐藏加入群组
            joinGroupBtn.setVisibility(View.VISIBLE);
            joinGroupBtn.setText(R.string.profile_group_has_dismissed);
            joinGroupBtn.setEnabled(false);
        } else {
            // 显示加入群组
            joinGroupBtn.setVisibility(View.VISIBLE);
        }

        // 加载群头像
        ImageLoaderUtils.displayGroupPortraitImage(groupInfo.getPortraitUri(), groupPortaritIv);

        // 设置群名称
        groupNameTv.setText(groupInfo.getName());

        // 设置群人数
        groupMemberTv.setText(getString(R.string.profile_group_has_members_format, groupInfo.getMemberCount()));

        // 保存群组名
        groupName = groupInfo.getName();


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profile_btn_join_group_confirm:
                joinGroupViewModel.joinToGroup();
                break;
            default:
                // do nothing
        }
    }
}
