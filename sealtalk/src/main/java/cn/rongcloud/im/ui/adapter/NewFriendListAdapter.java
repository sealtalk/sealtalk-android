package cn.rongcloud.im.ui.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.db.model.FriendDetailInfo;
import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.db.model.FriendStatus;
import cn.rongcloud.im.ui.widget.SelectableRoundedImageView;
import cn.rongcloud.im.utils.ImageLoaderUtils;

/**
 * 新朋友列表 Adapter
 */
public class NewFriendListAdapter extends BaseAdapter {

    private List<FriendShipInfo> datas = new ArrayList<>();
    private OnItemButtonClick mOnItemButtonClick;

    /**
     * 更新列表数据
     *
     * @param datas
     */
    public void updateList(List<FriendShipInfo> datas) {
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(parent.getContext(), R.layout.new_friends_item_user_ship, null);
            holder.nameTv = (TextView) convertView.findViewById(R.id.tv_name);
            holder.messageTv = (TextView) convertView.findViewById(R.id.tv_message);
            holder.headIv = (SelectableRoundedImageView) convertView.findViewById(R.id.iv_new_header);
            holder.stateTv = (TextView) convertView.findViewById(R.id.tv_state);
            holder.ignoreTv = convertView.findViewById(R.id.tv_ignore);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final FriendShipInfo info = (FriendShipInfo) datas.get(position);
        FriendDetailInfo user = info.getUser();
        if (info.getUser() != null) {
            holder.nameTv.setText(info.getUser().getNickname());
            ImageLoaderUtils.displayUserPortraitImage(user.getPortraitUri(), holder.headIv);
        }

        holder.messageTv.setText(info.getMessage());

        holder.stateTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemButtonClick != null) {
                    mOnItemButtonClick.onButtonClick(v, position, info);
                }
            }
        });

        holder.ignoreTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemButtonClick != null) {
                    mOnItemButtonClick.onIgnore(v, position, info);
                }
            }
        });

        switch (FriendStatus.getStatus(info.getStatus())) {
            case RECEIVE_REQUEST: //收到了好友邀请
                holder.stateTv.setText(R.string.seal_new_friend_agree);
                holder.stateTv.setBackgroundDrawable(parent.getContext().getResources().getDrawable(R.drawable.seal_new_friend_add_friend_selector));
                holder.ignoreTv.setVisibility(View.VISIBLE);
                break;
            case SEND_REQUEST: // 发出了好友邀请
                holder.stateTv.setText(R.string.seal_new_friend_request);
                holder.stateTv.setBackgroundDrawable(null);
                holder.ignoreTv.setVisibility(View.GONE);
                break;
            case IGNORE_REQUEST: // 忽略好友邀请
                holder.stateTv.setText(R.string.seal_new_friend_ignore);
                holder.stateTv.setBackgroundDrawable(null);
                holder.ignoreTv.setVisibility(View.GONE);
                break;
            case IS_FRIEND: // 已是好友
                holder.stateTv.setText(R.string.seal_new_friend_added);
                holder.stateTv.setBackgroundDrawable(null);
                holder.ignoreTv.setVisibility(View.GONE);
                break;
            case DELETE_FRIEND: // 删除了好友关系
                holder.stateTv.setText(R.string.seal_new_friend_deleted);
                holder.stateTv.setBackgroundDrawable(null);
                holder.ignoreTv.setVisibility(View.GONE);
                break;
            default:
                holder.ignoreTv.setVisibility(View.GONE);
        }
        return convertView;
    }

    /**
     * displayName :
     * message : 手机号:18622222222昵称:的用户请求添加你为好友
     * status : 11
     * updatedAt : 2016-01-07T06:22:55.000Z
     * user : {"id":"i3gRfA1ml","nickname":"nihaoa","portraitUri":""}
     */
    class ViewHolder {
        SelectableRoundedImageView headIv;
        TextView nameTv;
        TextView stateTv;
        TextView messageTv;
        TextView ignoreTv;
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
        boolean onButtonClick(View view, int position, FriendShipInfo info);

        boolean onIgnore(View view, int position, FriendShipInfo info);
    }

}
