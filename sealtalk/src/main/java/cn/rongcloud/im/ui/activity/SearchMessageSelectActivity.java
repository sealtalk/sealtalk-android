package cn.rongcloud.im.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.UltraGroupMemberListResult;
import cn.rongcloud.im.utils.ToastUtils;
import cn.rongcloud.im.utils.log.SLog;
import cn.rongcloud.im.viewmodel.UltraGroupViewModel;
import io.rong.imlib.model.ConversationIdentifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class SearchMessageSelectActivity extends TitleBaseActivity {

    private ConversationIdentifier conversationIdentifier;
    private UserIdSelectAdapter mUserIdSelectAdapter;

    private UltraGroupViewModel groupDetailViewModel;
    private int type;
    private ChannelIdSelectAdapter channelIdSelectAdapter;

    public static void start(Activity activity, int type, ConversationIdentifier identifier) {
        Intent intent = new Intent(activity, SearchMessageSelectActivity.class);
        if (identifier != null) {
            intent.putExtra(IntentExtra.SERIA_CONVERSATION_IDENTIFIER, identifier);
        }
        intent.putExtra(SealSearchUltraGroupActivity.TYPE, type);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_message_select);
        Intent intent = getIntent();
        String TAG = "SearchMessageSelectActivity";
        if (intent == null) {
            SLog.e(TAG, "intent is null, finish " + TAG);
            finish();
            return;
        }
        type = getIntent().getIntExtra(SealSearchUltraGroupActivity.TYPE, 0);
        conversationIdentifier = initConversationIdentifier();

        initView();
        initData();
    }

    private void initData() {
        groupDetailViewModel = ViewModelProviders.of(this).get(UltraGroupViewModel.class);
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
                                if (mUserIdSelectAdapter != null) {
                                    mUserIdSelectAdapter.setData(list);
                                }
                            }
                        });
    }

    private void initView() {
        // 用户选择
        boolean isNeedSelectUserId =
                type == SealSearchUltraGroupActivity.TYPE_SEARCH_MESSAGES_BY_USER_FOR_CHANNELS
                        || type
                                == SealSearchUltraGroupActivity
                                        .TYPE_SEARCH_MESSAGES_BY_USER_FOR_ALL_CHANNELS;
        if (isNeedSelectUserId) {
            findViewById(R.id.tv_user_id_select).setVisibility(View.VISIBLE);
            findViewById(R.id.rv_user_id_select).setVisibility(View.VISIBLE);
            RecyclerView rvUserIdSelect = findViewById(R.id.rv_user_id_select);
            mUserIdSelectAdapter = new UserIdSelectAdapter(this);
            rvUserIdSelect.setAdapter(mUserIdSelectAdapter);
        }

        // ChannelID 选择
        boolean isNeedSelectChannelId =
                type == SealSearchUltraGroupActivity.TYPE_SEARCH_MESSAGES_BY_USER_FOR_CHANNELS
                        || type == SealSearchUltraGroupActivity.TYPE_SEARCH_MESSAGES_FOR_CHANNELS;
        if (isNeedSelectChannelId) {
            findViewById(R.id.rv_channel_id_select).setVisibility(View.VISIBLE);
            findViewById(R.id.tv_channel_id_select).setVisibility(View.VISIBLE);

            RecyclerView rvChannelId = findViewById(R.id.rv_channel_id_select);
            channelIdSelectAdapter = new ChannelIdSelectAdapter(this);
            rvChannelId.setAdapter(channelIdSelectAdapter);

            SharedPreferences sharedPreferences =
                    getSharedPreferences("ultra", Context.MODE_PRIVATE);
            Set<String> channelIds =
                    sharedPreferences.getStringSet("channel_ids", Collections.EMPTY_SET);
            ArrayList<ChannelIdModel> channelIdModels = new ArrayList<>();
            for (String channelId : channelIds) {
                channelIdModels.add(new ChannelIdModel(channelId));
            }
            channelIdSelectAdapter.setData(channelIdModels);
        }

        getTitleBar().setRightText("完成");
        getTitleBar()
                .setOnBtnRightClickListener(
                        "完成",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String selectUserId = "";
                                if (isNeedSelectUserId) {
                                    for (UiGroupMemberModel model :
                                            mUserIdSelectAdapter.getData()) {
                                        if (model.isSelect) {
                                            selectUserId = model.mGroupMember.user.id;
                                        }
                                    }
                                    if (selectUserId.isEmpty()) {
                                        ToastUtils.showToast("请选择用户");
                                        return;
                                    }
                                }
                                ArrayList<String> selectChannelIds = new ArrayList<>();
                                if (isNeedSelectChannelId) {
                                    if (channelIdSelectAdapter != null) {
                                        for (ChannelIdModel model :
                                                channelIdSelectAdapter.getData()) {
                                            if (model.isSelect) {
                                                selectChannelIds.add(model.getChannelId());
                                            }
                                        }
                                    }
                                    if (selectChannelIds.isEmpty()) {
                                        ToastUtils.showToast("请选择ChannelId");
                                        return;
                                    }
                                }

                                SealSearchUltraGroupActivity.start(
                                        SearchMessageSelectActivity.this,
                                        type,
                                        conversationIdentifier,
                                        selectUserId,
                                        selectChannelIds.toArray(
                                                new String[selectChannelIds.size()]));

                                finish();
                            }
                        });
    }

    private static class UserIdSelectAdapter extends RecyclerView.Adapter<UserIdSelectViewHolder> {

        private List<UiGroupMemberModel> data = new ArrayList<>();
        private Context mContext;

        public UserIdSelectAdapter(Context context) {
            mContext = context;
        }

        @NonNull
        @Override
        public SearchMessageSelectActivity.UserIdSelectViewHolder onCreateViewHolder(
                @NonNull ViewGroup parent, int viewType) {
            View view =
                    LayoutInflater.from(mContext)
                            .inflate(R.layout.select_fragment_contact_item, parent, false);
            return new UserIdSelectViewHolder(view);
        }

        @Override
        public void onBindViewHolder(
                @NonNull SearchMessageSelectActivity.UserIdSelectViewHolder holder,
                @SuppressLint("RecyclerView") int position) {

            holder.itemView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            for (int i = 0; i < data.size(); i++) {
                                UiGroupMemberModel uiGroupMemberModel = data.get(i);
                                if (i == position) {
                                    uiGroupMemberModel.setSelect(true);
                                } else {
                                    uiGroupMemberModel.setSelect(false);
                                }
                            }
                            notifyDataSetChanged();
                        }
                    });

            UiGroupMemberModel uiGroupMemberModel = data.get(position);
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

    private static class UserIdSelectViewHolder extends RecyclerView.ViewHolder {
        public ImageView cb_select;
        public ImageView iv_portrait;
        public TextView tv_contact_name;

        public UserIdSelectViewHolder(@NonNull View itemView) {
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

    private static class ChannelIdSelectAdapter
            extends RecyclerView.Adapter<ChannelIdSelectViewHolder> {

        private List<ChannelIdModel> data = new ArrayList<>();
        private Context mContext;

        public ChannelIdSelectAdapter(Context context) {
            mContext = context;
        }

        @NonNull
        @Override
        public SearchMessageSelectActivity.ChannelIdSelectViewHolder onCreateViewHolder(
                @NonNull ViewGroup parent, int viewType) {
            View view =
                    LayoutInflater.from(mContext)
                            .inflate(R.layout.select_fragment_channel_id_item, parent, false);
            return new ChannelIdSelectViewHolder(view);
        }

        @Override
        public void onBindViewHolder(
                @NonNull SearchMessageSelectActivity.ChannelIdSelectViewHolder holder,
                @SuppressLint("RecyclerView") int position) {
            ChannelIdModel channelIdModel = data.get(position);
            holder.itemView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            channelIdModel.setSelect(!channelIdModel.isSelect);
                            notifyItemChanged(holder.getAdapterPosition());
                        }
                    });

            holder.tv_channelId.setText(channelIdModel.getChannelId());
            if (channelIdModel.isSelect) {
                holder.cb_select.setImageResource(
                        R.drawable.seal_cb_select_friend_pic_btn_selected);
            } else {
                holder.cb_select.setImageResource(
                        R.drawable.seal_cb_select_contact_pic_btn_unselected);
            }
        }

        public List<ChannelIdModel> getData() {
            return data;
        }

        public void setData(List<ChannelIdModel> data) {
            this.data.clear();
            this.data.addAll(data);
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    private static class ChannelIdSelectViewHolder extends RecyclerView.ViewHolder {
        public ImageView cb_select;
        public TextView tv_channelId;

        public ChannelIdSelectViewHolder(@NonNull View itemView) {
            super(itemView);
            cb_select = itemView.findViewById(R.id.cb_select);
            tv_channelId = itemView.findViewById(R.id.tv_channel_id);
        }
    }

    private static class ChannelIdModel {
        private boolean isSelect;
        private String channelId;

        public ChannelIdModel(String channelId) {
            this.channelId = channelId;
        }

        public boolean isSelect() {
            return isSelect;
        }

        public void setSelect(boolean select) {
            isSelect = select;
        }

        public String getChannelId() {
            return this.channelId;
        }

        public void setChannelId(String channelId) {
            this.channelId = channelId;
        }
    }
}
