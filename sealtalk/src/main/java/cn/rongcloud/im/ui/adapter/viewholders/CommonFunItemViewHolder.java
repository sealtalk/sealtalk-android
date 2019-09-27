package cn.rongcloud.im.ui.adapter.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.adapter.models.FunctionInfo;
import cn.rongcloud.im.ui.adapter.models.ListItemModel;
import cn.rongcloud.im.ui.view.UserInfoItemView;

public class CommonFunItemViewHolder extends BaseItemViewHolder<ListItemModel<FunctionInfo>> {
    private TextView unreadTv;
    private ImageView arrowIv;
    private UserInfoItemView itemUiv;
    private View.OnClickListener listener;

    public CommonFunItemViewHolder(@NonNull View itemView) {
        super(itemView);
        itemUiv = itemView.findViewById(R.id.uiv_item);
        arrowIv = itemView.findViewById(R.id.iv_arrow);
        unreadTv = itemView.findViewById(R.id.tv_unread);

        itemView.setOnClickListener(new View.OnClickListener() {
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
    public void update(ListItemModel<FunctionInfo> model) {
        final FunctionInfo data = model.getData();
        itemUiv.setName(model.getDisplayName());
        if (data.getDrawableRes() > 0) {
            itemUiv.setPortrait(data.getDrawableRes());
        } else {
            itemUiv.getHeaderImageView().setVisibility(View.GONE);
        }

        if (data.isShowArrow()) {
            arrowIv.setVisibility(View.VISIBLE);
        } else {
            arrowIv.setVisibility(View.GONE);
        }

        if (data.isShowDot()) {
            unreadTv.setVisibility(View.VISIBLE);
        } else {
            unreadTv.setVisibility(View.GONE);
        }

        if (data.getDotNumber() > 0) {
            unreadTv.setText(String.valueOf(data.getDotNumber()));
        }

    }

}
