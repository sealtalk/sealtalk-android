package cn.rongcloud.im.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.model.UserSimpleInfo;
import cn.rongcloud.im.ui.view.UserInfoItemView;
import cn.rongcloud.im.utils.ImageLoaderUtils;

public class BlackListAdapter extends BaseAdapter {

    private List<UserSimpleInfo> data = new ArrayList<>();

    @Override
    public int getCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public Object getItem(int position) {
        return data == null ? null : data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        UserSimpleInfo userInfo = data.get(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_black_list, parent, false);
        }
        UserInfoItemView userInfoItemView = (UserInfoItemView) convertView;
        userInfoItemView.setName(userInfo.getName());
        ImageLoaderUtils.displayUserPortraitImage(userInfo.getPortraitUri(), userInfoItemView.getHeaderImageView());
        if (position != getCount() -1) {
            userInfoItemView.setDividerVisibility(View.VISIBLE);
        } else {
            userInfoItemView.setDividerVisibility(View.GONE);
        }
        return convertView;
    }

    /**
     * 刷新数据
     *
     * @param data
     */
    public void updateData(List<UserSimpleInfo> data) {
        if (data == null) {
            return;
        }
        this.data.clear();
        this.data.addAll(data);
        notifyDataSetChanged();
    }
}
