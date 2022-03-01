package cn.rongcloud.im.ui.adapter.models;

import cn.rongcloud.im.db.model.GroupEntity;
import java.util.List;

public class CheckSearchGroupModel extends SearchGroupModel {
    private CheckType checkType = CheckType.NONE;

    public CheckSearchGroupModel(
            GroupEntity bean,
            int type,
            int groupNameStart,
            int groupNameEnd,
            List<GroupMemberMatch> memberMatches) {
        super(bean, type, groupNameStart, groupNameEnd, memberMatches);
    }

    public CheckType getCheckType() {
        return checkType;
    }

    public void setCheckType(CheckType checkType) {
        this.checkType = checkType;
    }
}
