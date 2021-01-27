package cn.rongcloud.im.ui.adapter.viewholders;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import cn.rongcloud.im.R;
import cn.rongcloud.im.model.SimplePhoneContactInfo;
import cn.rongcloud.im.ui.adapter.models.ListItemModel;

/**
 * 从通讯录邀请好友列表视图
 */
public class InviteFriendFromContactItemViewHolder extends BaseItemViewHolder<ListItemModel<SimplePhoneContactInfo>> {
    private Context context;

    private ListItemModel<SimplePhoneContactInfo> model;
    private TextView nameTv;
    private TextView numberTv;
    private CheckBox checkBox;
    private View.OnClickListener checkBoxClickListener;

    public InviteFriendFromContactItemViewHolder(@NonNull View itemView) {
        super(itemView);
        context = itemView.getContext();
        nameTv = itemView.findViewById(R.id.item_tv_name);
        numberTv = itemView.findViewById(R.id.item_tv_phone_number);
        checkBox = itemView.findViewById(R.id.item_cb_select);
        checkBox.setOnCheckedChangeListener(checkListener);
    }

    @Override
    public void setOnClickItemListener(View.OnClickListener listener) {
        checkBoxClickListener = listener;
    }

    @Override
    public void setOnLongClickItemListener(View.OnLongClickListener listener) {

    }

    @Override
    public void update(ListItemModel<SimplePhoneContactInfo> phoneContactInfoListItemModel) {
        model = phoneContactInfoListItemModel;

        // 更接数据类型进行显示
        SimplePhoneContactInfo data = model.getData();
        nameTv.setText(data.getName());
        numberTv.setText(data.getPhone());
        ListItemModel.CheckStatus checkStatus = model.getCheckStatus();
        if (checkStatus == ListItemModel.CheckStatus.CHECKED) {
            checkWithOutTriggerListener(true);
        } else {
            checkWithOutTriggerListener(false);
        }
    }

    @Override
    public void setChecked(boolean checked) {
        checkWithOutTriggerListener(checked);
    }

    /**
     * 不触发监听设置选择框，防止触发监听处理不必要的逻辑
     *
     * @param checked
     */
    private void checkWithOutTriggerListener(boolean checked) {
        checkBox.setOnCheckedChangeListener(null);
        checkBox.setChecked(checked);
        checkBox.setOnCheckedChangeListener(checkListener);
    }

    /**
     * 选择框选择监听
     */
    private CompoundButton.OnCheckedChangeListener checkListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (model != null) {
                if (isChecked) {
                    model.setCheckStatus(ListItemModel.CheckStatus.CHECKED);
                } else {
                    model.setCheckStatus(ListItemModel.CheckStatus.UNCHECKED);
                }
                if (checkBoxClickListener != null) {
                    checkBoxClickListener.onClick(checkBox);
                }
            }
        }
    };

}
