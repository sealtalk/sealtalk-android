package cn.rongcloud.im.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.model.GroupMember;
import cn.rongcloud.im.ui.view.UserInfoItemView;
import cn.rongcloud.im.utils.ImageLoaderUtils;

/**
 * 列表展示群组成员适配
 */
public class ListGroupMemberAdapter extends BaseAdapter {
    private List<GroupMember> list;

    private Context context;

    public ListGroupMemberAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return list != null ? list.size() : 0;
    }

    @Override
    public GroupMember getItem(int position) {
        return list != null ? list.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.profile_item_list_group_member, parent, false);
            holder.userInfoUiv = convertView.findViewById(R.id.uiv_userinfo);
            holder.roleTv = convertView.findViewById(R.id.tv_role);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        GroupMember groupMember = list.get(position);
        String groupNickName = groupMember.getGroupNickName();

        if (!TextUtils.isEmpty(groupNickName)) {
            holder.userInfoUiv.setName(groupNickName);
        } else {
            holder.userInfoUiv.setName(groupMember.getName());
        }

        if (groupMember.getRole() == GroupMember.Role.GROUP_OWNER.getValue()) {
            holder.roleTv.setVisibility(View.VISIBLE);
            holder.roleTv.setText(R.string.seal_group_management_group_owner);

        } else if (groupMember.getRole() == GroupMember.Role.MANAGEMENT.getValue()) {
            holder.roleTv.setVisibility(View.VISIBLE);
            holder.roleTv.setText(R.string.seal_group_management_managements);
        } else {
            holder.roleTv.setVisibility(View.GONE);
        }

        ImageLoaderUtils.displayUserPortraitImage(groupMember.getPortraitUri(), holder.userInfoUiv.getHeaderImageView());
        return convertView;
    }


    public void updateListView(List<GroupMember> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    private class ViewHolder {
        UserInfoItemView userInfoUiv;
        TextView roleTv;
    }
}
