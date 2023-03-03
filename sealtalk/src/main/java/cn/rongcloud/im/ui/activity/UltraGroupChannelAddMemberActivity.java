package cn.rongcloud.im.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.UltraGroupMemberListResult;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.utils.log.SLog;
import cn.rongcloud.im.viewmodel.UltraGroupViewModel;
import io.rong.imlib.model.ConversationIdentifier;
import java.util.ArrayList;
import java.util.List;

/** @author gusd @Date 2022/06/21 */
public class UltraGroupChannelAddMemberActivity extends TitleBaseActivity {

    private ConversationIdentifier conversationIdentifier;
    private MyAdapter mAdapter;

    private UltraGroupViewModel groupDetailViewModel;

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

    private void initData() {
        groupDetailViewModel.getUltraGroupMemberInfoList(
                conversationIdentifier.getTargetId(), 0, 100);
        groupDetailViewModel
                .getUltraGroupMemberInfoListResult()
                .observe(
                        this,
                        new Observer<Resource<List<UltraGroupMemberListResult>>>() {
                            @Override
                            public void onChanged(
                                    Resource<List<UltraGroupMemberListResult>> listResource) {
                                List<UiGroupMemberModel> list = new ArrayList<>();
                                if (listResource.data != null) {
                                    for (UltraGroupMemberListResult result : listResource.data) {
                                        list.add(new UiGroupMemberModel(result));
                                    }
                                }
                                mAdapter.setData(list);
                            }
                        });
    }

    private void initView() {
        RecyclerView rc_group_member_list = findViewById(R.id.rc_group_member_list);
        mAdapter = new MyAdapter(this);
        rc_group_member_list.setAdapter(mAdapter);
        getTitleBar().setRightText("完成");
        getTitleBar()
                .setOnBtnRightClickListener(
                        "完成",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                List<String> list = new ArrayList<>();
                                for (UiGroupMemberModel model : mAdapter.getData()) {
                                    if (model.isSelect) {
                                        list.add(model.mGroupMember.user.id);
                                    }
                                }
                                if (list.isEmpty()) {
                                    return;
                                }
                                groupDetailViewModel
                                        .addChannelUsers(
                                                conversationIdentifier.getTargetId(),
                                                conversationIdentifier.getChannelId(),
                                                list)
                                        .observe(
                                                UltraGroupChannelAddMemberActivity.this,
                                                new Observer<Boolean>() {
                                                    @Override
                                                    public void onChanged(Boolean aBoolean) {
                                                        if (aBoolean) {
                                                            ToastUtils.showToast("添加成功");
                                                            finish();
                                                        } else {
                                                            ToastUtils.showToast("添加失败");
                                                        }
                                                    }
                                                });
                            }
                        });
    }

    private void initViewModel() {
        groupDetailViewModel = ViewModelProviders.of(this).get(UltraGroupViewModel.class);
    }

    private static class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        private List<UiGroupMemberModel> data = new ArrayList<>();
        private Context mContext;

        public MyAdapter(Context context) {
            mContext = context;
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
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            UiGroupMemberModel uiGroupMemberModel = data.get(position);
            holder.itemView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            uiGroupMemberModel.setSelect(!uiGroupMemberModel.isSelect);
                            notifyItemChanged(holder.getAdapterPosition());
                        }
                    });

            holder.iv_portrait.setVisibility(View.GONE);
            if (TextUtils.isEmpty(uiGroupMemberModel.getGroupMember().memberName)) {
                holder.tv_contact_name.setText(uiGroupMemberModel.getGroupMember().user.id);
            } else {
                holder.tv_contact_name.setText(uiGroupMemberModel.getGroupMember().memberName);
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
        private UltraGroupMemberListResult mGroupMember;

        public UiGroupMemberModel(UltraGroupMemberListResult groupMember) {
            mGroupMember = groupMember;
        }

        public boolean isSelect() {
            return isSelect;
        }

        public void setSelect(boolean select) {
            isSelect = select;
        }

        public UltraGroupMemberListResult getGroupMember() {
            return mGroupMember;
        }

        public void setGroupMember(UltraGroupMemberListResult groupMember) {
            mGroupMember = groupMember;
        }
    }
}
