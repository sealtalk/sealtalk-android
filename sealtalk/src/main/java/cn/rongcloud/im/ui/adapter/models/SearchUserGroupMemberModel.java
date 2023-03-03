package cn.rongcloud.im.ui.adapter.models;

import cn.rongcloud.im.model.UserGroupMemberInfo;

public class SearchUserGroupMemberModel extends CheckModel<UserGroupMemberInfo> {

    public SearchUserGroupMemberModel(UserGroupMemberInfo bean, int type) {
        super(bean, type);
    }

    @Override
    public String toString() {
        return "SearchUserGroupMemberModel{" + "name='" + bean.nickname + '\'' + '}';
    }
}
