package cn.rongcloud.im.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import cn.rongcloud.im.R;
import cn.rongcloud.im.model.UserGroupMemberInfo;
import cn.rongcloud.im.ui.adapter.models.CheckModel;
import cn.rongcloud.im.ui.adapter.models.CheckType;
import cn.rongcloud.im.ui.adapter.models.SearchUserGroupMemberModel;
import cn.rongcloud.im.ui.adapter.viewholders.UserGroupMemberHolder;
import java.util.ArrayList;

public class UserGroupMemberAdapter
        extends AbsSelectedAdapter<UserGroupMemberHolder, SearchUserGroupMemberModel> {

    public UserGroupMemberAdapter() {}

    @NonNull
    @Override
    public UserGroupMemberHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_EDIT || viewType == VIEW_TYPE_NORMAL) {
            View itemView =
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.select_user_group_member_item, null, false);
            return new UserGroupMemberHolder(itemView, this);
        }
        throw new RuntimeException("ViewType error");
    }

    public ArrayList<UserGroupMemberInfo> getCheckedMemberInfoList() {
        ArrayList<UserGroupMemberInfo> list = new ArrayList<>();
        for (CheckModel model : originModelList) {
            if (model.getCheckType() == CheckType.CHECKED) {
                list.add((UserGroupMemberInfo) model.getBean());
            }
        }
        return list;
    }
}
