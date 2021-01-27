package cn.rongcloud.im.ui.adapter;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.db.model.GroupExitedMemberInfo;
import cn.rongcloud.im.ui.widget.SelectableRoundedImageView;
import cn.rongcloud.im.utils.ImageLoaderUtils;
import io.rong.imkit.utils.RongDateUtils;

public class GroupExitedListAdapter extends BaseAdapter {


    private List<GroupExitedMemberInfo> datas = new ArrayList<>();
    private final int QUIT_BY_GROUP_OWNER = 0;
    private final int QUIT_BY_GROUP_MANAGER = 1;
    private final int QUIT_ACTIVE = 2;

    public void updateList(List<GroupExitedMemberInfo> datas) {
        if (datas == null) {
            return;
        }
        this.datas.clear();
        this.datas.addAll(datas);
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return datas == null ? 0 : datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas == null ? null : datas.get(position);
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
            convertView = View.inflate(parent.getContext(), R.layout.group_exited_item, null);
            holder.ivPortrait = convertView.findViewById(R.id.iv_portrait);
            holder.tvTitle = convertView.findViewById(R.id.tv_exited_title);
            holder.tvContent = convertView.findViewById(R.id.tv_exited_content);
            holder.tvTime = convertView.findViewById(R.id.tv_time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        GroupExitedMemberInfo info = datas.get(position);
        holder.tvTitle.setText(info.getQuitNickname());
        if (!TextUtils.isEmpty(info.getQuitPortraitUri())) {
            ImageLoaderUtils.displayGroupPortraitImage(info.getQuitPortraitUri(), holder.ivPortrait);
        }
        if (!TextUtils.isEmpty(info.getQuitTime())) {
            holder.tvTime.setText(RongDateUtils.getConversationFormatDate(Long.valueOf(info.getQuitTime()), convertView.getContext()));
        }
        switch (info.getQuitReason()) {
            case QUIT_BY_GROUP_OWNER:
                holder.tvContent.setText(convertView.getContext().getString(R.string.seal_group_manager_exited_content_owner, info.getOperatorName()));
                break;
            case QUIT_BY_GROUP_MANAGER:
                holder.tvContent.setText(convertView.getContext().getString(R.string.seal_group_manager_exited_content_manager, info.getOperatorName()));
                break;
            case QUIT_ACTIVE:
                holder.tvContent.setText(convertView.getContext().getString(R.string.seal_group_manager_exited_content_active));
                break;
        }
        return convertView;
    }

    class ViewHolder {
        SelectableRoundedImageView ivPortrait;
        TextView tvTitle;
        TextView tvContent;
        TextView tvTime;
    }
}
