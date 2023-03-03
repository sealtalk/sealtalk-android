package cn.rongcloud.im.ui.adapter.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import cn.rongcloud.im.R;
import cn.rongcloud.im.model.UserGroupMemberInfo;
import cn.rongcloud.im.ui.adapter.models.SearchUserGroupMemberModel;
import cn.rongcloud.im.utils.ImageLoaderUtils;

public class MemberPortraitHolder extends CheckableBaseViewHolder<SearchUserGroupMemberModel> {

    private TextView name;
    private ImageView portrait;

    public MemberPortraitHolder(@NonNull View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.tv_user_name);
        portrait = itemView.findViewById(R.id.iv_portrait);
    }

    @Override
    public void update(int position, SearchUserGroupMemberModel model) {
        UserGroupMemberInfo bean = (UserGroupMemberInfo) model.getBean();
        name.setText(bean.nickname);
        ImageLoaderUtils.displayUserPortraitImage(bean.portraitUri, portrait);
    }
}
