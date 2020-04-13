package cn.rongcloud.im.ui.adapter.viewholders;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import cn.rongcloud.im.R;
import cn.rongcloud.im.db.model.FriendDetailInfo;
import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.model.GroupMember;
import cn.rongcloud.im.ui.adapter.models.CheckableContactModel;
import cn.rongcloud.im.ui.interfaces.OnCheckContactClickListener;
import cn.rongcloud.im.ui.widget.SelectableRoundedImageView;
import cn.rongcloud.im.utils.ImageLoaderUtils;

public class CheckableContactViewHolder extends CheckableBaseViewHolder<CheckableContactModel> {

    private TextView nameTextView;
    private SelectableRoundedImageView protraitImageView;
    private OnCheckContactClickListener checkableItemClickListener;
    private CheckableContactModel model;
    private ImageView checkBox;


    public CheckableContactViewHolder(@NonNull View itemView, OnCheckContactClickListener listener) {
        super(itemView);
        checkableItemClickListener = listener;
        protraitImageView = itemView.findViewById(R.id.iv_portrait);
        nameTextView = itemView.findViewById(R.id.tv_contact_name);
        checkBox = itemView.findViewById(R.id.cb_select);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkableItemClickListener.onContactContactClick(model);
            }
        });
    }

    @Override
    public void update(CheckableContactModel contactModel) {
        model = contactModel;
        String name = null;
        String portraitUrl = null;
        if (contactModel.getBean() instanceof FriendShipInfo) {
            FriendShipInfo friendShipInfo = (FriendShipInfo) contactModel.getBean();
            FriendDetailInfo info = friendShipInfo.getUser();
            String groupDisplayName = friendShipInfo.getGroupDisplayName();
            String displayName = friendShipInfo.getDisplayName();
            if (!TextUtils.isEmpty(groupDisplayName)) {
                name = groupDisplayName;
            } else if (!TextUtils.isEmpty(displayName)) {
                name = displayName;
            } else {
                name = info.getNickname();
            }
            portraitUrl = info.getPortraitUri();
        } else if (contactModel.getBean() instanceof GroupMember) {
            GroupMember groupMember = (GroupMember) contactModel.getBean();
            name = groupMember.getGroupNickName();
            if (TextUtils.isEmpty(name)) {
                name = groupMember.getName();
            }
            portraitUrl = groupMember.getPortraitUri();
        }

        nameTextView.setText(name);
        ImageLoaderUtils.displayUserPortraitImage(portraitUrl, protraitImageView);
        updateCheck(checkBox, contactModel.getCheckType());
    }

}
