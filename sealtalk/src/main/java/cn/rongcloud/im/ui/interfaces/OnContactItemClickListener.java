package cn.rongcloud.im.ui.interfaces;

import cn.rongcloud.im.db.model.FriendShipInfo;

public interface OnContactItemClickListener {
    /**
     * 联系人列表人员点击监听
     *
     * @param friendShipInfo
     */
    void onItemContactClick(FriendShipInfo friendShipInfo);
}
