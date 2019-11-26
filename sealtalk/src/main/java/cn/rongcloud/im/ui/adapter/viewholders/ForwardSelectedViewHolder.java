package cn.rongcloud.im.ui.adapter.viewholders;

import android.view.View;

import androidx.annotation.NonNull;

import cn.rongcloud.im.R;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.ui.adapter.models.ListItemModel;
import cn.rongcloud.im.ui.view.UserInfoItemView;
import cn.rongcloud.im.utils.ImageLoaderUtils;

public class ForwardSelectedViewHolder extends BaseItemViewHolder<ListItemModel> {
    private final UserInfoItemView userInfoUiv;
    private View.OnClickListener listener;

    public ForwardSelectedViewHolder(@NonNull View itemView) {
        super(itemView);
        userInfoUiv = itemView.findViewById(R.id.uiv_userinfo);
        userInfoUiv.setDividerVisibility(View.VISIBLE);
        userInfoUiv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(v);
                }
            }
        });
    }


    @Override
    public void setOnClickItemListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void setOnLongClickItemListener(View.OnLongClickListener listener) {

    }

    @Override
    public void update(ListItemModel model) {
        if (model != null) {
            if (model.getItemView().getType() == ListItemModel.ItemView.Type.GROUP ) {
                GroupEntity group = (GroupEntity)model.getData();
              userInfoUiv.setName(model.getDisplayName() + "（" + group.getMemberCount()+"）");
            } else if (model.getItemView().getType() == ListItemModel.ItemView.Type.FRIEND ) {
                userInfoUiv.setName(model.getDisplayName());
            }
            ImageLoaderUtils.displayGroupPortraitImage(model.getPortraitUrl(), userInfoUiv.getHeaderImageView());
        }
    }
}
