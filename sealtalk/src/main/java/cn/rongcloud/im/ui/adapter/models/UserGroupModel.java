package cn.rongcloud.im.ui.adapter.models;

import cn.rongcloud.im.model.UserGroupInfo;

public class UserGroupModel extends CheckModel<UserGroupInfo> {

    public UserGroupModel(UserGroupInfo bean, int type) {
        super(bean, type);
    }

    @Override
    public String toString() {
        return "UserGroupModel{" + "bean=" + bean + '}';
    }
}
