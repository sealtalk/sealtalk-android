package cn.rongcloud.im.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.utils.Resource;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imlib.model.UserInfo;

/**
 * Created by Bob on 15/10/12.
 */
public class NewTextReplyAdapter extends android.widget.BaseAdapter {
    private Context mContext;
    private List<UserInfo> mNumberList;
    private LayoutInflater mLayoutInflater;

    public NewTextReplyAdapter(Context context, List<UserInfo> list) {
        mLayoutInflater = LayoutInflater.from(context);
        this.mContext = context;
        this.mNumberList = list;
    }
    @Override
    public int getCount() {
        return mNumberList.size();
    }

    @Override
    public Object getItem(int i) {
        return mNumberList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        Resource res = new Resource(mNumberList.get(i).getPortraitUri());
        if (convertView == null || convertView.getTag() == null) {
            convertView = mLayoutInflater.inflate(R.layout.item_reply, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.mUserName = (TextView) convertView.findViewById(R.id.text1);
            viewHolder.mImageView = (AsyncImageView) convertView.findViewById(R.id.reply1);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (viewHolder != null) {

            viewHolder.mUserName.setText(mNumberList.get(i).getName());
            viewHolder.mImageView.setResource(mNumberList.get(i).getPortraitUri().toString(), R.drawable.de_default_portrait);

        }
        return convertView;
    }

    static class ViewHolder {
        TextView mUserName;

        AsyncImageView mImageView;

    }
}
