package cn.rongcloud.im.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import cn.rongcloud.im.R;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.utils.log.SLog;
import cn.rongcloud.im.viewmodel.UltraGroupViewModel;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imkit.userinfo.model.GroupUserInfo;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.ConversationIdentifier;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.UserInfo;
import java.util.ArrayList;
import java.util.List;

/** @author gusd @Date 2022/06/21 */
public class UltraGroupChannelMemberSetting extends TitleBaseActivity
        implements RongUserInfoManager.UserDataObserver {
    private static final String TAG = "UltraGroupChannelMemberSetting";

    private ConversationIdentifier conversationIdentifier;
    private UltraGroupViewModel groupDetailViewModel;
    private MyAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ultra_group_channel_member_setting);
        Intent intent = getIntent();
        String TAG = "UltraSettingActivity";
        if (intent == null) {
            SLog.e(TAG, "intent is null, finish " + TAG);
            finish();
            return;
        }
        conversationIdentifier = initConversationIdentifier();

        initView();
        initViewModel();
        initData();
    }

    private void initView() {
        RecyclerView rc_group_member_list = findViewById(R.id.rc_group_member_list);
        mAdapter = new MyAdapter(this);
        rc_group_member_list.setAdapter(mAdapter);
        getTitleBar()
                .setOnBtnRightClickListener(
                        "删除",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                List<String> list = new ArrayList<>();
                                for (UiGroupMemberModel data : mAdapter.getData()) {
                                    if (data.isSelect) {
                                        list.add(data.getGroupMember());
                                    }
                                }
                                if (list.isEmpty()) {
                                    return;
                                }
                                groupDetailViewModel
                                        .delChannelUsers(
                                                conversationIdentifier.getTargetId(),
                                                conversationIdentifier.getChannelId(),
                                                list)
                                        .observe(
                                                UltraGroupChannelMemberSetting.this,
                                                new Observer<Boolean>() {
                                                    @Override
                                                    public void onChanged(Boolean aBoolean) {
                                                        if (aBoolean) {
                                                            ToastUtils.showToast("删除成功");
                                                            finish();
                                                            if (list.contains(
                                                                    RongIMClient.getInstance()
                                                                            .getCurrentUserId())) {
                                                                Intent intent =
                                                                        new Intent(
                                                                                UltraGroupChannelMemberSetting
                                                                                        .this,
                                                                                MainActivity.class);
                                                                startActivity(intent);
                                                            }
                                                        } else {
                                                            ToastUtils.showToast("删除失败");
                                                        }
                                                    }
                                                });
                            }
                        });
    }

    private void initData() {
        groupDetailViewModel
                .obChannelMembersChange()
                .observe(
                        this,
                        new Observer<List<String>>() {
                            @Override
                            public void onChanged(List<String> groupMembers) {
                                List<UiGroupMemberModel> uiGroupMemberModels = new ArrayList<>();
                                for (String groupMember : groupMembers) {
                                    uiGroupMemberModels.add(new UiGroupMemberModel(groupMember));
                                }
                                mAdapter.setData(uiGroupMemberModels);
                            }
                        });
        groupDetailViewModel.queryChannelMembers(
                conversationIdentifier.getTargetId(), conversationIdentifier.getChannelId());
        RongUserInfoManager.getInstance().addUserDataObserver(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RongUserInfoManager.getInstance().removeUserDataObserver(this);
    }

    private void initViewModel() {
        groupDetailViewModel = ViewModelProviders.of(this).get(UltraGroupViewModel.class);
    }

    @Override
    public void onUserUpdate(UserInfo info) {
        List<String> list = new ArrayList<>();
        for (UiGroupMemberModel member : mAdapter.getData()) {
            list.add(member.getGroupMember());
        }
        if (list.contains(info.getUserId())) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onGroupUpdate(Group group) {}

    @Override
    public void onGroupUserInfoUpdate(GroupUserInfo groupUserInfo) {}

    private static class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        private Context mContext;
        private List<UiGroupMemberModel> data;

        public MyAdapter(Context context) {
            mContext = context;
            data = new ArrayList<>();
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view =
                    LayoutInflater.from(mContext)
                            .inflate(R.layout.select_fragment_contact_item, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
            final UiGroupMemberModel uiGroupMemberModel = data.get(position);
            holder.itemView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            uiGroupMemberModel.setSelect(!uiGroupMemberModel.isSelect);
                            notifyItemChanged(holder.getAdapterPosition());
                        }
                    });
            holder.iv_portrait.setVisibility(View.GONE);
            UserInfo userInfo =
                    RongUserInfoManager.getInstance()
                            .getUserInfo(uiGroupMemberModel.getGroupMember());
            if (userInfo != null) {
                holder.tv_contact_name.setText(userInfo.getName());
            } else {
                holder.tv_contact_name.setText(uiGroupMemberModel.getGroupMember());
            }
            if (uiGroupMemberModel.isSelect) {
                holder.cb_select.setImageResource(
                        R.drawable.seal_cb_select_friend_pic_btn_selected);
            } else {
                holder.cb_select.setImageResource(
                        R.drawable.seal_cb_select_contact_pic_btn_unselected);
            }
        }

        public List<UiGroupMemberModel> getData() {
            return data;
        }

        public void setData(List<UiGroupMemberModel> data) {
            this.data.clear();
            this.data.addAll(data);
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    private static class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView cb_select;
        public ImageView iv_portrait;
        public TextView tv_contact_name;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            cb_select = itemView.findViewById(R.id.cb_select);
            iv_portrait = itemView.findViewById(R.id.iv_portrait);
            tv_contact_name = itemView.findViewById(R.id.tv_contact_name);
        }
    }

    private static class UiGroupMemberModel {
        private boolean isSelect;
        private String mGroupMember;

        public UiGroupMemberModel(String groupMember) {
            mGroupMember = groupMember;
        }

        public boolean isSelect() {
            return isSelect;
        }

        public void setSelect(boolean select) {
            isSelect = select;
        }

        public String getGroupMember() {
            return mGroupMember;
        }

        public void setGroupMember(String groupMember) {
            mGroupMember = groupMember;
        }
    }
}
