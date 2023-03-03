package cn.rongcloud.im.ui.adapter.viewholders;

import static cn.rongcloud.im.ui.adapter.AbsSelectedAdapter.VIEW_TYPE_EDIT;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import cn.rongcloud.im.R;
import cn.rongcloud.im.model.UserGroupInfo;
import cn.rongcloud.im.ui.adapter.models.UserGroupModel;
import cn.rongcloud.im.ui.interfaces.OnCheckContactClickListener;

public class UserGroupHolder extends CheckableBaseViewHolder<UserGroupModel> {

    private TextView name;
    private TextView member;
    private ImageView rightImg;
    private ImageView select;

    public UserGroupHolder(@NonNull View itemView, OnCheckContactClickListener listener) {
        super(itemView);
        name = itemView.findViewById(R.id.tv_user_group_name);
        member = itemView.findViewById(R.id.tv_user_group_member);
        rightImg = itemView.findViewById(R.id.iv_right_image);
        select = itemView.findViewById(R.id.cb_select);
    }

    @Override
    public void update(UserGroupModel model) {
        UserGroupInfo bean = model.getBean();
        name.setText(bean.userGroupName + " - " + bean.userGroupId);
        String formatNumbers = itemView.getContext().getString(R.string.rc_user_group_member_num);
        member.setText(String.format(formatNumbers, bean.memberCount));

        if (model.getType() == VIEW_TYPE_EDIT) {
            rightImg.setVisibility(View.GONE);
            select.setVisibility(View.VISIBLE);
            updateCheck(select, model.getCheckType());
        } else {
            rightImg.setVisibility(View.VISIBLE);
            select.setVisibility(View.GONE);
        }
    }
}
