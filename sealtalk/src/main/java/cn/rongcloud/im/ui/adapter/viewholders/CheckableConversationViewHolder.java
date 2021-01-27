package cn.rongcloud.im.ui.adapter.viewholders;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import cn.rongcloud.im.R;
import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.ui.adapter.models.CheckableContactModel;
import cn.rongcloud.im.ui.interfaces.OnCheckConversationClickListener;
import cn.rongcloud.im.ui.widget.SelectableRoundedImageView;
import cn.rongcloud.im.utils.ImageLoaderUtils;

public class CheckableConversationViewHolder extends CheckableBaseViewHolder<CheckableContactModel> {

    private TextView nameTextView;
    private SelectableRoundedImageView protraitImageView;
    private OnCheckConversationClickListener mListener;
    private ImageView checkBox;
    private CheckableContactModel model;
    private Context mContext;

    public CheckableConversationViewHolder(@NonNull View itemView,OnCheckConversationClickListener listener) {
        super(itemView);
        mContext = itemView.getContext();
        protraitImageView = itemView.findViewById(R.id.iv_portrait);
        nameTextView = itemView.findViewById(R.id.tv_contact_name);
        checkBox = itemView.findViewById(R.id.cb_select);
        mListener = listener;
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onCheckConversationClick(model);
            }
        });
    }

    @Override
    public void update(CheckableContactModel conversationContactModel) {
        model = conversationContactModel;
        if (conversationContactModel.getBean() instanceof GroupEntity){
            GroupEntity entity = (GroupEntity)conversationContactModel.getBean();
            nameTextView.setText(TextUtils.isEmpty(entity.getName()) ? "" : entity.getName());
            ImageLoaderUtils.displayUserPortraitImage(entity.getPortraitUri(), protraitImageView);
        } else if (conversationContactModel.getBean() instanceof FriendShipInfo){
            FriendShipInfo info = (FriendShipInfo)conversationContactModel.getBean();
            nameTextView.setText(TextUtils.isEmpty(info.getDisplayName()) ? info.getUser().getNickname() : info.getDisplayName());
            ImageLoaderUtils.displayUserPortraitImage(info.getUser().getPortraitUri(), protraitImageView);
        }
        updateCheck(checkBox, conversationContactModel.getCheckType());
    }
}
