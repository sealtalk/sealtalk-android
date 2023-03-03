package cn.rongcloud.im.ui.adapter.viewholders;

import static cn.rongcloud.im.ui.adapter.AbsSelectedAdapter.VIEW_TYPE_EDIT;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import cn.rongcloud.im.R;
import cn.rongcloud.im.model.UserGroupMemberInfo;
import cn.rongcloud.im.ui.adapter.models.SearchUserGroupMemberModel;
import cn.rongcloud.im.ui.interfaces.OnAdapterItemClickListener;
import cn.rongcloud.im.utils.ImageLoaderUtils;

public class UserGroupMemberHolder extends CheckableBaseViewHolder<SearchUserGroupMemberModel> {

    private TextView name;
    private ImageView portrait;
    private ImageView select;

    public UserGroupMemberHolder(@NonNull View itemView, OnAdapterItemClickListener listener) {
        super(itemView);
        name = itemView.findViewById(R.id.tv_user_name);
        portrait = itemView.findViewById(R.id.iv_portrait);
        select = itemView.findViewById(R.id.cb_select);
    }

    @Override
    public void update(int position, SearchUserGroupMemberModel model) {
        UserGroupMemberInfo bean = model.getBean();
        name.setText(bean.nickname);
        ImageLoaderUtils.displayUserPortraitImage(bean.portraitUri, portrait);
        if (model.getType() == VIEW_TYPE_EDIT) {
            select.setVisibility(View.VISIBLE);
            updateCheck(select, model.getCheckType());
        } else {
            select.setVisibility(View.GONE);
        }
    }
}
