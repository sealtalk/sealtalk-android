package cn.rongcloud.im.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.model.GroupMember;
import cn.rongcloud.im.ui.view.UserInfoItemView;
import cn.rongcloud.im.utils.ImageLoaderUtils;

public class GroupManagementAdapter extends BaseAdapter {

    private List<GroupMember> datas =  new ArrayList<>();
    private OnManagementClickListener listener;

    /**
     * 更新列表
     * @param datas
     */
    public void updateList(List<GroupMember> datas) {
        this.datas.clear();
        if (datas != null &&  datas.size() > 0) {
            this.datas.addAll(datas);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return datas == null? 0 : datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas == null? null : datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            View view  = View.inflate(parent.getContext(), R.layout.item_group_management, null);
            viewHolder = new ViewHolder();
            viewHolder.userInfoUiv = view.findViewById(R.id.uiv_userinfo);
            viewHolder.delIv = view.findViewById(R.id.iv_del);
            view.setTag(viewHolder);
            convertView = view;
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        GroupMember member = datas.get(position);
        String userId = member.getUserId();
        viewHolder.userInfoUiv.setName(member.getName());
        ImageLoaderUtils.displayUserPortraitImage(member.getPortraitUri(), viewHolder.userInfoUiv.getHeaderImageView());
        // -1 为添加管理员项
        if (userId.equals("-1")) {
            viewHolder.userInfoUiv.setNameTextColor(R.color.color_gray_text);
            viewHolder.userInfoUiv.getHeaderImageView().setImageDrawable(null);
            viewHolder.userInfoUiv.getHeaderImageView().setBackgroundResource(R.drawable.seal_ic_add_management_plus);
            viewHolder.delIv.setVisibility(View.GONE);
        } else {
            viewHolder.delIv.setVisibility(View.VISIBLE);
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // -1 为添加管理员项
                if (userId.equals("-1")) {
                    if (listener != null) {
                        listener.onAdd(v, member);
                    }
                } else {
                    if (listener != null) {
                        listener.onClick(v, member);
                    }
                }

            }
        });

        return convertView;
    }

    public void setOnManagementClickListener (OnManagementClickListener l) {
        this.listener = l;
    }

    public interface OnManagementClickListener {
        void onClick(View view, GroupMember member);
        void onAdd(View view, GroupMember member);
    }


    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    public static class ViewHolder {
        public UserInfoItemView userInfoUiv;
        public ImageView delIv;
    }
}
