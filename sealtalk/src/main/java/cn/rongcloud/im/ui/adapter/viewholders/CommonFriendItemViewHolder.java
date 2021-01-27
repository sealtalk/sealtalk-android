package cn.rongcloud.im.ui.adapter.viewholders;

import android.view.View;
import android.widget.CheckBox;

import androidx.annotation.NonNull;

import cn.rongcloud.im.R;
import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.ui.adapter.models.ListItemModel;
import cn.rongcloud.im.ui.view.UserInfoItemView;
import cn.rongcloud.im.utils.ImageLoaderUtils;

public class CommonFriendItemViewHolder extends BaseItemViewHolder<ListItemModel<FriendShipInfo>> {
    private final UserInfoItemView infoUiv;
    private ListItemModel<FriendShipInfo> model;
    private CheckBox checkBox;
    private View.OnClickListener listener;
    private View.OnLongClickListener longClickListener;

    public CommonFriendItemViewHolder(@NonNull View itemView) {
        super(itemView);
        infoUiv = itemView.findViewById(R.id.uiv_userinfo);
        checkBox = itemView.findViewById(R.id.cb_select);
        checkBox.setVisibility(View.VISIBLE);
        checkBox.setClickable(false);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (model != null) {
                    if (model.getCheckStatus() != ListItemModel.CheckStatus.NONE && model.getCheckStatus() != ListItemModel.CheckStatus.DISABLE) {
                        if (model.getCheckStatus() == ListItemModel.CheckStatus.CHECKED) {
                            model.setCheckStatus(ListItemModel.CheckStatus.UNCHECKED);
                            checkBox.setChecked(false);
                        } else if (model.getCheckStatus() == ListItemModel.CheckStatus.UNCHECKED) {
                            model.setCheckStatus(ListItemModel.CheckStatus.CHECKED);
                            checkBox.setChecked(true);
                        }
                    }
                }

                if (listener != null) {
                    listener.onClick(v);
                }
            }
        });
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (longClickListener != null) {
                    longClickListener.onLongClick(v);
                    return true;
                }
                return false;
            }
        });
    }


    @Override
    public void setOnClickItemListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void setOnLongClickItemListener(View.OnLongClickListener listener) {
        this.longClickListener = listener;
    }

    @Override
    public void update(ListItemModel<FriendShipInfo> friendShipInfoListItemModel) {
        model = friendShipInfoListItemModel;

        // 更接数据类型进行显示
        if (model.getCheckStatus() == ListItemModel.CheckStatus.NONE) {
            checkBox.setVisibility(View.GONE);
        } else if (model.getCheckStatus() == ListItemModel.CheckStatus.DISABLE) {
            checkBox.setVisibility(View.VISIBLE);
            checkBox.setEnabled(false);
        } else {
            checkBox.setVisibility(View.VISIBLE);
            if (model.getCheckStatus() == ListItemModel.CheckStatus.CHECKED) {
                checkBox.setChecked(true);
            } else {
                checkBox.setChecked(false);
            }
        }

        infoUiv.setName(model.getDisplayName());
        ImageLoaderUtils.displayUserPortraitImage(model.getPortraitUrl(), infoUiv.getHeaderImageView());
    }

    @Override
    public void setChecked(boolean checked) {
        if (checked) {
            model.setCheckStatus(ListItemModel.CheckStatus.CHECKED);
            checkBox.setChecked(true);
        } else {
            model.setCheckStatus(ListItemModel.CheckStatus.UNCHECKED);
            checkBox.setChecked(false);
        }
    }

    public void setDividerVisibility(boolean visibility) {
        if (visibility) {
            infoUiv.setDividerVisibility(View.VISIBLE);
        } else {
            infoUiv.setDividerVisibility(View.GONE);
        }
    }

}
