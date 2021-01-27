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
import cn.rongcloud.im.ui.widget.SelectableRoundedImageView;
import cn.rongcloud.im.utils.ImageLoaderUtils;

/**
 * 网格显示群成员的适配
 * 默认在最后一格中显示添加成员
 */
public class GridGroupMemberAdapter extends BaseAdapter {

    private List<GroupMember> list;
    private Context context;
    private int showMemberLimit;
    private boolean isAllowDelete = false;
    private boolean isAllowAdd = false;
    private OnItemClickedListener onItemClickedListener;

    public GridGroupMemberAdapter(Context context, int showMemberLimit) {
        this.context = context;
        this.showMemberLimit = showMemberLimit;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.profile_item_grid_group_member, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.avatarView = convertView.findViewById(R.id.profile_iv_grid_member_avatar);
            viewHolder.usernameTv = convertView.findViewById(R.id.profile_iv_grid_tv_username);
            convertView.setTag(viewHolder);
        } else{
            viewHolder = (ViewHolder)convertView.getTag();
        }
        SelectableRoundedImageView avatarView = viewHolder.avatarView;
        TextView usernameTv = viewHolder.usernameTv;

        // 最后一个item
        if (position == getCount() - 1 && (isAllowDelete || isAllowAdd)) {
            // 允许减员
            if(isAllowDelete){
                usernameTv.setText("");
                avatarView.setImageDrawable(null);
                avatarView.setBackgroundResource(R.drawable.profile_ic_grid_member_delete);
                avatarView.setOnClickListener(v -> {
                    if (onItemClickedListener != null) {
                        onItemClickedListener.onAddOrDeleteMemberClicked(false);
                    }
                });
            } else if(isAllowAdd){
                usernameTv.setText("");
                avatarView.setImageDrawable(null);
                avatarView.setBackgroundResource(R.drawable.profile_ic_grid_member_add);

                avatarView.setOnClickListener(v -> {
                    if (onItemClickedListener != null) {
                        onItemClickedListener.onAddOrDeleteMemberClicked(true);
                    }
                });
            }

            viewHolder.avatarUrl = null;
        } else if ((isAllowDelete && position == getCount() - 2) && isAllowAdd) {
            usernameTv.setText("");
            avatarView.setImageDrawable(null);
            avatarView.setBackgroundResource(R.drawable.profile_ic_grid_member_add);
            viewHolder.avatarUrl = null;

            avatarView.setOnClickListener(v -> {
                if (onItemClickedListener != null) {
                    onItemClickedListener.onAddOrDeleteMemberClicked(true);
                }

            });
        } else { // 普通成员
            final GroupMember groupMember = list.get(position);
            String groupNickName = groupMember.getGroupNickName();
            if (!TextUtils.isEmpty(groupNickName)) {
                usernameTv.setText(groupNickName);
            } else {
                usernameTv.setText(groupMember.getName());
            }

            avatarView.setBackgroundResource(android.R.color.transparent);
            String portraitUri = groupMember.getPortraitUri();
            if(portraitUri != null && !portraitUri.equals(viewHolder.avatarUrl)){
                ImageLoaderUtils.displayUserPortraitImage(portraitUri, avatarView);
                viewHolder.avatarUrl = portraitUri;
            }

            avatarView.setOnClickListener(v -> {
                if (onItemClickedListener != null) {
                    onItemClickedListener.onMemberClicked(groupMember);
                }
            });

        }

        return convertView;
    }

    @Override
    public int getCount() {
        // 判断是否允许删除成员时
        // 在允许删除成员时，在最后显示添加和删除按钮；当不运行删除成员时，仅显示添加按钮
        if (isAllowDelete && isAllowAdd) {
            return (list != null ? list.size() : 0) + 2;
        } else if (isAllowDelete || isAllowAdd) {
            return (list != null ? list.size() : 0) + 1;
        } else {
            return list != null ? list.size() : 0;
        }
    }

    @Override
    public GroupMember getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * 设置是否允许删除成员
     *
     * @param isAllowDelete
     */
    public void setAllowDeleteMember(boolean isAllowDelete) {
        this.isAllowDelete = isAllowDelete;
        notifyDataSetChanged();
    }

    /**
     * 设置是否允许添加成员
     *
     * @param isAllowAdd
     */
    public void setAllowAddMember(boolean isAllowAdd) {
        this.isAllowAdd = isAllowAdd;
    }

    /**
     * 传入新的数据 刷新UI的方法
     */
    public void updateListView(List<GroupMember> list) {
        if(showMemberLimit > 0) {
            if (list != null && list.size() > showMemberLimit) {
                list = list.subList(0, showMemberLimit);
            }
        }
        this.list = list;
        notifyDataSetChanged();
    }

    /**
     * 设置网格项点击事件
     *
     * @param onItemClickedListener
     */
    public void setOnItemClickedListener(OnItemClickedListener onItemClickedListener) {
        this.onItemClickedListener = onItemClickedListener;
    }

    public interface OnItemClickedListener {
        /**
         * 当点击添加或删除成员时回调
         *
         * @param isAdd true 为添加成员，false 为移除成员
         */
        void onAddOrDeleteMemberClicked(boolean isAdd);

        /**
         * 当成员点击时回调
         *
         * @param groupMember
         */
        void onMemberClicked(GroupMember groupMember);
    }

    private class ViewHolder{
        SelectableRoundedImageView avatarView;
        String avatarUrl;
        TextView usernameTv;
    }
}
