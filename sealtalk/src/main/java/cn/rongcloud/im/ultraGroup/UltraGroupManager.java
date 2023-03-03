package cn.rongcloud.im.ultraGroup;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import cn.rongcloud.im.model.UltraChannelInfo;
import cn.rongcloud.im.model.UltraGroupInfo;
import cn.rongcloud.im.utils.RongGenerate;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.model.Group;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author gusd @Date 2022/06/17 */
public class UltraGroupManager {

    private List<UltraGroupInfo> mUltraGroupInfoList = new ArrayList<>();
    private Map<String, List<UltraChannelInfo>> mUltraChannelInfoMap = new HashMap<>();

    List<GroupChangeListener> mGroupChangeListeners = new ArrayList<>();

    private UltraGroupManager() {}

    private static class SingleHolder {
        private static final UltraGroupManager instance = new UltraGroupManager();
    }

    public static UltraGroupManager getInstance() {
        return SingleHolder.instance;
    }

    /**
     * 添加群组通知接口
     *
     * @param listener GroupChangeListener
     */
    public void addGroupChangeListener(GroupChangeListener listener) {
        if (!mGroupChangeListeners.contains(listener)) {
            mGroupChangeListeners.add(listener);
        }
    }

    /** 移除群组通知接口 */
    public void clearGroupChangeListener() {
        mGroupChangeListeners.clear();
    }

    /** 遍历接口列表，进行调用notifyGroupChange */
    public void notifyGroupChange() {
        for (GroupChangeListener listener : mGroupChangeListeners) {
            if (listener == null) {
                continue;
            }
            listener.onGroupChange();
        }
    }

    /**
     * 遍历接口列表，进行调用onGroupCreate
     *
     * @param ultraGroupInfo 创建的UltraGroupInfo
     */
    public void notifyGroupCreate(UltraGroupInfo ultraGroupInfo) {
        for (GroupChangeListener listener : mGroupChangeListeners) {
            if (listener == null) {
                continue;
            }
            listener.onGroupCreate(ultraGroupInfo);
        }
    }

    public void refreshUltraChannelInfo(
            Context context, String groupId, List<UltraChannelInfo> channelInfoList) {
        List<UltraChannelInfo> oldInfoList = mUltraChannelInfoMap.get(groupId);
        if (oldInfoList != null && !oldInfoList.isEmpty()) {
            for (UltraChannelInfo ultraChannelInfo : channelInfoList) {
                UltraChannelInfo oldInfo =
                        findChannelInfoFromList(oldInfoList, ultraChannelInfo.channelId);
                if (oldInfo != null) {
                    if (!compareChannelInfo(oldInfo, ultraChannelInfo)) {
                        RongUserInfoManager.getInstance()
                                .refreshGroupInfoCache(
                                        createGroupInfoFromChannelInfo(
                                                context, groupId, ultraChannelInfo));
                    }
                } else {
                    RongUserInfoManager.getInstance()
                            .refreshGroupInfoCache(
                                    createGroupInfoFromChannelInfo(
                                            context, groupId, ultraChannelInfo));
                }
            }
        } else {
            for (UltraChannelInfo ultraChannelInfo : channelInfoList) {
                Group groupInfo =
                        createGroupInfoFromChannelInfo(context, groupId, ultraChannelInfo);
                RongUserInfoManager.getInstance().refreshGroupInfoCache(groupInfo);
            }
        }
        mUltraChannelInfoMap.put(groupId, channelInfoList);
    }

    public void refreshUltraChannelInfo(
            Context context, String groupId, UltraChannelInfo channelInfoList) {
        UltraChannelInfo ultraChannelInfo = getUltraChannelInfo(groupId, channelInfoList.channelId);
        if (ultraChannelInfo != null) {
            ultraChannelInfo.setType(ultraChannelInfo.type);
            ultraChannelInfo.setChannelName(ultraChannelInfo.channelName);
            ultraChannelInfo.setChannelId(ultraChannelInfo.channelId);
            RongUserInfoManager.getInstance()
                    .refreshGroupInfoCache(
                            createGroupInfoFromChannelInfo(context, groupId, ultraChannelInfo));
        }
    }

    public UltraChannelInfo getUltraChannelInfo(String groupId, String channelId) {
        List<UltraChannelInfo> ultraChannelInfos = mUltraChannelInfoMap.get(groupId);
        if (ultraChannelInfos != null) {
            for (UltraChannelInfo ultraChannelInfo : ultraChannelInfos) {
                if (TextUtils.equals(ultraChannelInfo.getChannelId(), channelId)) {
                    return ultraChannelInfo;
                }
            }
        }
        return null;
    }

    private UltraChannelInfo findChannelInfoFromList(
            List<UltraChannelInfo> list, String channelId) {
        for (UltraChannelInfo ultraChannelInfo : list) {
            if (TextUtils.equals(ultraChannelInfo.channelId, channelId)) {
                return ultraChannelInfo;
            }
        }
        return null;
    }

    private boolean compareChannelInfo(UltraChannelInfo info1, UltraChannelInfo info2) {
        return TextUtils.equals(info1.getChannelName(), info2.getChannelName())
                && TextUtils.equals(info1.getChannelId(), info2.getChannelId())
                && info1.getType() == info2.getType();
    }

    public Group createGroupInfoFromChannelInfo(
            Context context, String groupId, UltraChannelInfo channelInfo) {
        Uri portraitUri =
                Uri.parse(
                        RongGenerate.generateDefaultAvatar(
                                context, channelInfo.channelId, channelInfo.channelName));
        return new Group(groupId + channelInfo.channelId, channelInfo.channelName, portraitUri);
    }

    public void addChannel(
            Context context,
            String groupId,
            String channelId,
            String channelName,
            IRongCoreEnum.UltraGroupChannelType type) {
        UltraChannelInfo channelInfo = getUltraChannelInfo(groupId, channelId);
        if (channelInfo != null) {
            channelInfo.setChannelId(channelId);
            channelInfo.setChannelName(channelName);
            channelInfo.setType(type.getValue());
        } else {
            List<UltraChannelInfo> ultraChannelInfos = mUltraChannelInfoMap.get(groupId);
            if (ultraChannelInfos != null) {
                channelInfo = new UltraChannelInfo();
                channelInfo.setChannelId(channelId);
                channelInfo.setChannelName(channelName);
                if (type != null) {
                    channelInfo.setType(type.getValue());
                }
                ultraChannelInfos.add(channelInfo);
            }
        }
        if (channelInfo != null) {
            RongUserInfoManager.getInstance()
                    .refreshGroupInfoCache(
                            createGroupInfoFromChannelInfo(context, groupId, channelInfo));
        }
    }

    /** 超级群群组通知接口 */
    public interface GroupChangeListener {
        /** 超级群群组表更 */
        void onGroupChange();

        /**
         * 超级群群组创建回调
         *
         * @param ultraGroupInfo 创建的群组信息
         */
        void onGroupCreate(UltraGroupInfo ultraGroupInfo);
    }
}
