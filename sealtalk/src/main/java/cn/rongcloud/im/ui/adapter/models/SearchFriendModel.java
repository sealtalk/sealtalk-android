package cn.rongcloud.im.ui.adapter.models;

import cn.rongcloud.im.db.model.FriendShipInfo;

public class SearchFriendModel extends SearchModel<FriendShipInfo> {
    //start 如果为 -1 则没有找到
    private int nameStart;
    private int nameEnd;
    private int aliasStart;
    private int aliasEnd;

    public SearchFriendModel(FriendShipInfo bean, int type, int nameStart, int nameEnd, int aliasStart, int aliasEnd) {
        super(bean, type);
        this.nameStart = nameStart;
        this.nameEnd = nameEnd;
        this.aliasStart = aliasStart;
        this.aliasEnd = aliasEnd;
    }

    public int getNameStart() {
        return nameStart;
    }

    public int getNameEnd() {
        return nameEnd;
    }

    public int getAliseStart() {
        return aliasStart;
    }

    public int getAliseEnd() {
        return aliasEnd;
    }

    private CheckType checkType = CheckType.NONE;

    public CheckType getCheckType() {
        return checkType;
    }

    public void setCheckType(CheckType checkType) {
        this.checkType = checkType;
    }
}
