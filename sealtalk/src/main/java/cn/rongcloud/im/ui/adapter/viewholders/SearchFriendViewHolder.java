package cn.rongcloud.im.ui.adapter.viewholders;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import cn.rongcloud.im.R;
import cn.rongcloud.im.db.model.FriendDetailInfo;
import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.ui.adapter.models.SearchFriendModel;
import cn.rongcloud.im.ui.interfaces.OnContactItemClickListener;
import cn.rongcloud.im.utils.CharacterParser;
import cn.rongcloud.im.utils.ImageLoaderUtils;

public class SearchFriendViewHolder extends BaseViewHolder<SearchFriendModel> {
    private TextView tvNickName;
    private TextView tvDisplayName;
    private ImageView portrait;
    private View llDescription;
    private OnContactItemClickListener listener;
    private FriendShipInfo friendShipInfo;

    public SearchFriendViewHolder(@NonNull View itemView, OnContactItemClickListener l) {
        super(itemView);
        this.listener = l;
        portrait = itemView.findViewById(R.id.iv_portrait);
        tvDisplayName = itemView.findViewById(R.id.tv_name);
        tvNickName = itemView.findViewById(R.id.tv_detail);
        llDescription = itemView.findViewById(R.id.ll_description);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemContactClick(friendShipInfo);
                }
            }
        });
    }

    @Override
    public void update(SearchFriendModel searchFriendModel) {

        friendShipInfo = searchFriendModel.getBean();
        FriendDetailInfo info = friendShipInfo.getUser();

        if (!TextUtils.isEmpty(friendShipInfo.getDisplayName())) {
            llDescription.setVisibility(View.VISIBLE);

            if(searchFriendModel.getAliseStart() != -1){
                tvDisplayName.setText(CharacterParser.getSpannable(friendShipInfo.getDisplayName(), searchFriendModel.getAliseStart(), searchFriendModel.getAliseEnd()));
            } else {
                tvDisplayName.setText(friendShipInfo.getDisplayName());
            }

            if (searchFriendModel.getNameStart() != -1){
                tvNickName.setText(CharacterParser.getSpannable(info.getNickname(), searchFriendModel.getNameStart(), searchFriendModel.getNameEnd()));
            } else {
                tvNickName.setText(info.getNickname());
            }
        } else {
            if(searchFriendModel.getNameStart() != -1){
                tvDisplayName.setText(CharacterParser.getSpannable(info.getNickname(), searchFriendModel.getNameStart(), searchFriendModel.getNameEnd()));
            } else {
                tvDisplayName.setText(info.getNickname());
            }
            llDescription.setVisibility(View.GONE);
        }
        ImageLoaderUtils.displayUserPortraitImage(info.getPortraitUri(),portrait);

    }


}
