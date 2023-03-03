package cn.rongcloud.im.ui.adapter;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cn.rongcloud.im.R;
import cn.rongcloud.im.model.UltraGroupInfo;
import cn.rongcloud.im.utils.RongGenerate;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import io.rong.imlib.ChannelClient;
import io.rong.imlib.IRongCoreCallback;
import io.rong.imlib.IRongCoreEnum;
import java.util.List;

public class UltraListAdapter extends BaseAdapter {
    private final LayoutInflater layoutInflater;
    private final List<UltraGroupInfo> data;

    public UltraListAdapter(Context context, List<UltraGroupInfo> data) {
        layoutInflater = LayoutInflater.from(context);
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.ultra_list_item, parent, false); // 加载布局
            holder = new ViewHolder();
            holder.imageView = convertView.findViewById(R.id.ultraImageView);
            holder.unreadImageView = convertView.findViewById(R.id.rc_ultra_conversation_unread_bg);
            holder.unread = convertView.findViewById(R.id.rc_ultra_conversation_unread_count);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.unreadImageView.setVisibility(View.GONE);
        holder.unread.setVisibility(View.GONE);

        UltraGroupInfo ultraUserInfo = data.get(position);
        if (TextUtils.isEmpty(ultraUserInfo.portraitUri)) {
            Uri groupPortraitUri =
                    Uri.parse(
                            RongGenerate.generateDefaultAvatar(
                                    convertView.getContext(),
                                    ultraUserInfo.groupId,
                                    ultraUserInfo.groupName));
            Glide.with(convertView)
                    .load(groupPortraitUri)
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(holder.imageView);
        } else {
            Glide.with(convertView)
                    .load(Uri.parse(ultraUserInfo.portraitUri))
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(holder.imageView);
        }

        showUltraGroupUnread(
                convertView.getContext(),
                ultraUserInfo.groupId,
                holder.unreadImageView,
                holder.unread);
        return convertView;
    }

    public void setList(List<UltraGroupInfo> dataList) {
        if (dataList == null) {
            return;
        }
        data.clear();
        data.addAll(dataList);
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        ImageView imageView;
        ImageView unreadImageView;
        TextView unread;
    }

    private void showUltraGroupUnread(
            Context context, String groupId, ImageView unreadImageView, TextView unread) {
        ChannelClient.getInstance()
                .getUltraGroupUnreadMentionedCount(
                        groupId,
                        new IRongCoreCallback.ResultCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer count) {
                                if (count == 0) {
                                    unreadImageView.setVisibility(View.GONE);
                                    unread.setVisibility(View.GONE);
                                } else if (count > 0 && count < 100) {
                                    unreadImageView.setVisibility(View.VISIBLE);
                                    unread.setVisibility(View.VISIBLE);
                                    unread.setText(String.valueOf(count));
                                } else {
                                    unreadImageView.setVisibility(View.VISIBLE);
                                    unread.setVisibility(View.VISIBLE);
                                    unread.setText(
                                            context.getString(
                                                    R.string.seal_main_chat_tab_more_read_message));
                                }
                            }

                            @Override
                            public void onError(IRongCoreEnum.CoreErrorCode e) {
                                unreadImageView.setVisibility(View.GONE);
                                unread.setVisibility(View.GONE);
                            }
                        });
    }
}
