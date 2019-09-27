package cn.rongcloud.im.ui.adapter;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.db.model.GroupNoticeInfo;
import cn.rongcloud.im.ui.widget.SelectableRoundedImageView;
import io.rong.imkit.utils.RongDateUtils;

public class GroupNoticeListAdapter extends BaseAdapter {

    private List<GroupNoticeInfo> datas = new ArrayList<>();
    private OnItemButtonClick mOnItemButtonClick;
    private OnRequestInfoListener onRequestGroupImageListener;

    public void updateList(List<GroupNoticeInfo> datas) {
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
            convertView = View.inflate(parent.getContext(), R.layout.group_notice_item, null);
            holder.groupName = convertView.findViewById(R.id.tv_group_name);
            holder.groupIv = convertView.findViewById(R.id.iv_new_header);
            holder.requestName = convertView.findViewById(R.id.tv_request_name);
            holder.requestCotent = convertView.findViewById(R.id.tv_request_content);
            holder.tvTime = convertView.findViewById(R.id.tv_time);
            holder.tvLeft = convertView.findViewById(R.id.tv_left);
            holder.tvRight = convertView.findViewById(R.id.tv_right);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        GroupNoticeInfo info = datas.get(position);
        // 0: 忽略、1: 同意、2: 等待 3 已过期
        if (info.getStatus() == 0) {
            holder.tvLeft.setVisibility(View.GONE);
            holder.tvRight.setText(parent.getContext().getString(R.string.seal_group_notice_ignored));
            holder.tvRight.setTextColor(parent.getContext().getResources().getColor(R.color.seal_login_text_button_color));
        } else if (info.getStatus() == 1) {
            holder.tvLeft.setVisibility(View.GONE);
            holder.tvRight.setText(parent.getContext().getString(R.string.seal_group_notice_agreed));
            holder.tvRight.setTextColor(parent.getContext().getResources().getColor(R.color.seal_login_text_button_color));
        } else if (info.getStatus() == 3) {
            holder.tvLeft.setVisibility(View.GONE);
            holder.tvRight.setText(parent.getContext().getString(R.string.seal_group_notice_overdue));
            holder.tvRight.setTextColor(parent.getContext().getResources().getColor(R.color.seal_login_text_button_color));
        } else if (info.getStatus() == 2) {
            holder.tvLeft.setVisibility(View.VISIBLE);
            holder.tvRight.setText(parent.getContext().getString(R.string.seal_group_notice_agree));
            holder.tvRight.setTextColor(parent.getContext().getResources().getColor(R.color.seal_group_notice_item_agree));
        }
        holder.tvLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemButtonClick != null) {
                    mOnItemButtonClick.onButtonIgnoreClick(v, position, info);
                }
            }
        });
        holder.tvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemButtonClick != null) {
                    mOnItemButtonClick.onButtonAgreeClick(v, position, info);
                }
            }
        });
        // 1: 待被邀请者处理、2: 待管理员处理
        if (info.getType() == 1) {
            if (!TextUtils.isEmpty(info.getGroupNickName())) {
                holder.groupName.setText(info.getGroupNickName());
            }
            if (!TextUtils.isEmpty(info.getRequesterNickName())) {
                holder.requestName.setText(info.getRequesterNickName());
            }
            if (!TextUtils.isEmpty(info.getReceiverId())) {//&& info.getReceiverId().equals(RongIM.getInstance().getCurrentUserId())
                holder.requestCotent.setText(R.string.seal_conversation_notification_group_tips);
            }
            if (!TextUtils.isEmpty(info.getGroupId())) {
                onRequestGroupImageListener.onRequestGroupInfo(holder.groupIv, position, info);
            }
        } else if (info.getType() == 2) {
            if (!TextUtils.isEmpty(info.getReceiverNickName())) {
                holder.groupName.setText(info.getReceiverNickName());
            }
            holder.requestName.setText(R.string.seal_conversation_notification_group_tips_add);
            if (!TextUtils.isEmpty(info.getGroupNickName())) {
                holder.requestCotent.setText(info.getGroupNickName());
            }
            if (!TextUtils.isEmpty(info.getReceiverId())) {
                onRequestGroupImageListener.onRequestUserInfo(holder.groupIv, position, info);
            }
        }
        if (!TextUtils.isEmpty(info.getCreatedTime())) {
            holder.tvTime.setText(RongDateUtils.getConversationFormatDate(Long.valueOf(info.getCreatedTime()), convertView.getContext()));
        }
        return convertView;
    }

    class ViewHolder {
        SelectableRoundedImageView groupIv;
        TextView groupName;
        TextView requestName;
        TextView requestCotent;
        TextView tvTime;
        TextView tvLeft;
        TextView tvRight;
    }

    /**
     * 设置点击事件
     *
     * @param onItemButtonClick
     */
    public void setOnItemButtonClick(OnItemButtonClick onItemButtonClick) {
        this.mOnItemButtonClick = onItemButtonClick;
    }

    /**
     * 点击事件接口
     */
    public interface OnItemButtonClick {
        boolean onButtonAgreeClick(View view, int position, GroupNoticeInfo info);

        boolean onButtonIgnoreClick(View view, int position, GroupNoticeInfo info);
    }

    public void setOnRequestInfoListener(OnRequestInfoListener onRequestGroupImageListener) {
        this.onRequestGroupImageListener = onRequestGroupImageListener;
    }

    public interface OnRequestInfoListener {
        boolean onRequestGroupInfo(View view, int position, GroupNoticeInfo info);

        boolean onRequestUserInfo(View view, int position, GroupNoticeInfo info);
    }
}
