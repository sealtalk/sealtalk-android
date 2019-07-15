package cn.rongcloud.im.ui.adapter.viewholders;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import cn.rongcloud.im.R;
import cn.rongcloud.im.db.model.FriendDetailInfo;
import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.ui.adapter.models.CheckableContactModel;
import cn.rongcloud.im.ui.interfaces.OnCheckContactClickListener;
import cn.rongcloud.im.ui.widget.SelectableRoundedImageView;
import cn.rongcloud.im.utils.ImageLoaderUtils;

public class CheckableContactViewHolder extends CheckableBaseViewHolder<CheckableContactModel<FriendShipInfo>> {

    private TextView nameTextView;
    private SelectableRoundedImageView protraitImageView;
    private OnCheckContactClickListener checkableItemClickListener;
    private CheckableContactModel<FriendShipInfo> model;
    private ImageView checkBox;


    public CheckableContactViewHolder(@NonNull View itemView, OnCheckContactClickListener listener) {
        super(itemView);
        checkableItemClickListener = listener;
        protraitImageView = itemView.findViewById(R.id.iv_portrait);
        nameTextView = itemView.findViewById(R.id.tv_friendname);
        checkBox = itemView.findViewById(R.id.cb_select);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkableItemClickListener.onContactContactClick(model);
            }
        });
    }

    @Override
    public void update(CheckableContactModel<FriendShipInfo> friendShipInfoCheckableContactModel) {
        model = friendShipInfoCheckableContactModel;
        FriendShipInfo friendShipInfo = friendShipInfoCheckableContactModel.getBean();
        FriendDetailInfo info = friendShipInfo.getUser();
        nameTextView.setText(TextUtils.isEmpty(friendShipInfo.getDisplayName()) ? info.getNickname() : friendShipInfo.getDisplayName());
        ImageLoaderUtils.displayUserPortraitImage(info.getPortraitUri(), protraitImageView);
        updateCheck(checkBox, friendShipInfoCheckableContactModel.getCheckType());
    }

}
