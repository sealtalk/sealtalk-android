package cn.rongcloud.im.ui.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.model.GroupMember;
import cn.rongcloud.im.ui.widget.SelectableRoundedImageView;
import cn.rongcloud.im.utils.ImageLoaderUtils;

import static cn.rongcloud.im.ui.adapter.RlGroupMemberAdapter.*;

public class RlGroupMemberAdapter extends Adapter<RlGroupMemberViewHolder> {

    private List<GroupMember> memberList = new ArrayList<>();
    private OnItemClickedListener onItemClickedListener;

    @NonNull
    @Override
    public RlGroupMemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.profile_item_rl_group_member, null, false);
        RlGroupMemberViewHolder viewHolder = new RlGroupMemberViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RlGroupMemberViewHolder holder, int position) {
        SelectableRoundedImageView avatarView = holder.avatarView;
        TextView usernameTv = holder.usernameTv;
        GroupMember groupMember = memberList.get(position);
        avatarView.setBackgroundResource(android.R.color.transparent);
        String portraitUri = groupMember.getPortraitUri();
        if (portraitUri != null) {
            ImageLoaderUtils.displayUserPortraitImage(portraitUri, avatarView);
        }
        if (!TextUtils.isEmpty(groupMember.getGroupNickName())) {
            usernameTv.setText(groupMember.getGroupNickName());
        } else if (!TextUtils.isEmpty(groupMember.getName())) {
            usernameTv.setText(groupMember.getName());
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickedListener != null) {
                    onItemClickedListener.onMemberClicked(memberList.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    /**
     * 传入新的数据 刷新UI的方法
     */
    public void updateListView(List<GroupMember> list) {
        this.memberList = list;
        notifyDataSetChanged();
    }

    public void setOnItemClickedListener(OnItemClickedListener onItemClickedListener) {
        this.onItemClickedListener = onItemClickedListener;
    }

    public interface OnItemClickedListener {
        /**
         * 当成员点击时回调
         *
         * @param groupMember
         */
        void onMemberClicked(GroupMember groupMember);
    }

    public static class RlGroupMemberViewHolder extends RecyclerView.ViewHolder {
        SelectableRoundedImageView avatarView;
        TextView usernameTv;
        View itemView;

        public RlGroupMemberViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarView = itemView.findViewById(R.id.profile_iv_grid_member_avatar);
            usernameTv = itemView.findViewById(R.id.profile_iv_grid_tv_username);
            this.itemView = itemView;
        }
    }
}
