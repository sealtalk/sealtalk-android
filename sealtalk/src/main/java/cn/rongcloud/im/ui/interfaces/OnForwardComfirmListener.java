package cn.rongcloud.im.ui.interfaces;

import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.db.model.GroupEntity;
import java.util.List;

public interface OnForwardComfirmListener {
    void onForward(List<GroupEntity> groups, List<FriendShipInfo> friendShipInfos);

    void onForwardNoDialog(List<GroupEntity> groups, List<FriendShipInfo> friendShipInfos);
}
