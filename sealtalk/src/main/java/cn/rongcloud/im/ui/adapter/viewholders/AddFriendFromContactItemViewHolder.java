package cn.rongcloud.im.ui.adapter.viewholders;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import cn.rongcloud.im.R;
import cn.rongcloud.im.model.PhoneContactInfo;
import cn.rongcloud.im.ui.adapter.models.ListItemModel;
import cn.rongcloud.im.utils.ImageLoaderUtils;

/**
 * 从通讯录添加好友列表视图
 */
public class AddFriendFromContactItemViewHolder extends BaseItemViewHolder<ListItemModel<PhoneContactInfo>> {
    private Context context;
    private final int ADD_BUTTON_PADDING_VERTICAL_DP = 5;
    private final int ADD_BUTTON_PADDING_HORIZON_DP = 13;

    private ListItemModel<PhoneContactInfo> model;
    private ImageView portraitIv;
    private TextView nameTv;
    private TextView stAccountTv;
    private TextView addTv;
    private int addButtonPaddingVerticalPx = 0;
    private int addButtonPaddingHorizonPx = 0;

    private OnAddFriendClickedListener addFriendClickedListener;

    public AddFriendFromContactItemViewHolder(@NonNull View itemView) {
        super(itemView);
        portraitIv = itemView.findViewById(R.id.item_iv_portrait);
        nameTv = itemView.findViewById(R.id.item_tv_name);
        stAccountTv = itemView.findViewById(R.id.item_tv_st_account);
        addTv = itemView.findViewById(R.id.item_tv_add);
        context = itemView.getContext();
        float densityDpi = context.getResources().getDisplayMetrics().density;
        addButtonPaddingVerticalPx = (int) densityDpi * ADD_BUTTON_PADDING_VERTICAL_DP;
        addButtonPaddingHorizonPx = (int) densityDpi * ADD_BUTTON_PADDING_HORIZON_DP;
    }

    @Override
    public void setOnClickItemListener(View.OnClickListener listener) {
    }

    @Override
    public void setOnLongClickItemListener(View.OnLongClickListener listener) {

    }


    @Override
    public void update(ListItemModel<PhoneContactInfo> phoneContactInfoListItemModel) {
        model = phoneContactInfoListItemModel;

        // 更接数据类型进行显示
        PhoneContactInfo data = model.getData();
        nameTv.setText(data.getContactName());
        String stAccount = data.getStAccount();
        if (!TextUtils.isEmpty(stAccount)) {
            stAccountTv.setText(context.getString(R.string.seal_st_account_content_format, stAccount));
            stAccountTv.setVisibility(View.VISIBLE);
        } else {
            stAccountTv.setVisibility(View.GONE);
        }
        String userId = data.getUserId();
        if (data.isFriend() == 0) { // 非好友
            addTv.setText(R.string.seal_new_friend_add);
            addTv.setTextColor(context.getResources().getColor(R.color.text_white));
            addTv.setPadding(addButtonPaddingHorizonPx, addButtonPaddingVerticalPx, addButtonPaddingHorizonPx, addButtonPaddingVerticalPx);
            addTv.setBackgroundResource(R.drawable.common_btn_blue_selector);
            addTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (addFriendClickedListener != null) {
                        addFriendClickedListener.onAddFriendClicked(userId);
                    }
                }
            });
        } else {
            addTv.setText(R.string.seal_new_friend_added);
            addTv.setPadding(0, 0, 0, 0);
            addTv.setTextColor(context.getResources().getColor(R.color.default_sub_text));
            addTv.setBackgroundResource(android.R.color.transparent);
            addTv.setOnClickListener(null);
        }

        ImageLoaderUtils.displayUserPortraitImage(model.getPortraitUrl(), portraitIv);
    }

    public void setAddFriendClickedListener(OnAddFriendClickedListener listener) {
        this.addFriendClickedListener = listener;
    }

    public interface OnAddFriendClickedListener {
        void onAddFriendClicked(String userId);
    }
}
