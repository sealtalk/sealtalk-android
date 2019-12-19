package cn.rongcloud.im.ui.interfaces;

import java.util.List;

import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.db.model.GroupEntity;

public interface OnForwardComfirmListener {
    void onForward(List<GroupEntity> groups, List<FriendShipInfo> friendShipInfos);
    void onForwardNoDialog(List<GroupEntity> groups, List<FriendShipInfo> friendShipInfos);
}
