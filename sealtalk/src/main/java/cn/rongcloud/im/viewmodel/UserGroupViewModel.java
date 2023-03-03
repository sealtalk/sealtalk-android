package cn.rongcloud.im.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.UltraGroupMemberListResult;
import cn.rongcloud.im.model.UserGroupInfo;
import cn.rongcloud.im.model.UserGroupMemberInfo;
import cn.rongcloud.im.task.UltraGroupTask;
import cn.rongcloud.im.task.UserGroupTask;
import cn.rongcloud.im.utils.SingleSourceLiveData;
import io.rong.common.RLog;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.IRongCoreListener;
import io.rong.imlib.model.ConversationIdentifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class UserGroupViewModel extends AndroidViewModel
        implements IRongCoreListener.UserGroupStatusListener {

    private static final String TAG = "UserGroupViewModel";

    protected Application mApplication;
    private Handler mMainHandler;
    private final UserGroupTask userGroupTask;
    private final UltraGroupTask ultraGroupTask;
    private ConversationIdentifier mIdentifier;

    // LiveData
    // 超级群群成员列表
    private final SingleSourceLiveData<Resource<List<UltraGroupMemberListResult>>>
            ultraGroupMemberInfoListResult = new SingleSourceLiveData<>();
    // 超级群所有用户组列表
    private final SingleSourceLiveData<Resource<List<UserGroupInfo>>> userGroupListResult =
            new SingleSourceLiveData<>();
    // 用户组创建
    private final SingleSourceLiveData<Resource<String>> userGroupAddResult =
            new SingleSourceLiveData<>();
    // 用户组删除
    private final SingleSourceLiveData<Resource<String>> userGroupDelResult =
            new SingleSourceLiveData<>();
    // 用户组成员列表
    private final SingleSourceLiveData<Resource<List<UserGroupMemberInfo>>>
            userGroupMemberListResult = new SingleSourceLiveData<>();
    // 用户组成员添加
    private final SingleSourceLiveData<Resource<String>> userGroupMemberAddResult =
            new SingleSourceLiveData<>();
    // 用户组成员移除
    private final SingleSourceLiveData<Resource<String>> userGroupMemberDelResult =
            new SingleSourceLiveData<>();
    // 超级群频道绑定用户组
    private final SingleSourceLiveData<Resource> userGroupBindChannelResult =
            new SingleSourceLiveData<>();
    // 超级群频道解绑用户组
    private final SingleSourceLiveData<Resource> userGroupUnBindChannelResult =
            new SingleSourceLiveData<>();

    public UserGroupViewModel(@NonNull Application application) {
        super(application);
        mApplication = application;
        mMainHandler = new Handler(Looper.getMainLooper());
        userGroupTask = new UserGroupTask(application);
        ultraGroupTask = new UltraGroupTask(application);
        IMManager.getInstance().addUserGroupStatusListener(this);
    }

    @Override
    protected void onCleared() {
        IMManager.getInstance().removeUserGroupStatusListener(this);
    }

    public void inject(ConversationIdentifier identifier) {
        mIdentifier = identifier;
    }

    /** 获取超级群下群成员 */
    public SingleSourceLiveData<Resource<List<UltraGroupMemberListResult>>>
            getUltraGroupMemberInfoListResult() {
        return ultraGroupMemberInfoListResult;
    }

    public void getUltraGroupMemberInfoList(String groupId) {
        ultraGroupMemberInfoListResult.setSource(
                ultraGroupTask.getUltraGroupMemberInfoList(groupId, 1, 100));
    }

    /** 获取超级群下用户组。如果identifier中channelID不为空，则查询这个channel下的用户组列表 */
    public SingleSourceLiveData<Resource<List<UserGroupInfo>>> getUserGroupListResult() {
        return userGroupListResult;
    }

    public void getUserGroupList(ConversationIdentifier identifier) {
        userGroupListResult.setSource(userGroupTask.getUserGroupList(identifier));
    }

    /** 用户组添加 */
    public SingleSourceLiveData<Resource<String>> getUserGroupAddResult() {
        return userGroupAddResult;
    }

    public void userGroupAdd(ConversationIdentifier identifier, String userGroupName) {
        userGroupAddResult.setSource(
                userGroupTask.userGroupAdd(identifier.getTargetId(), userGroupName));
    }

    /** 用户组删除 */
    public SingleSourceLiveData<Resource<String>> getUserGroupDelResult() {
        return userGroupDelResult;
    }

    public void userGroupDel(String groupId, String userGroupId) {
        userGroupDelResult.setSource(userGroupTask.userGroupDel(groupId, userGroupId));
    }

    /** 用户组群成员列表 */
    public SingleSourceLiveData<Resource<List<UserGroupMemberInfo>>>
            getUserGroupMemberListResult() {
        return userGroupMemberListResult;
    }

    public void userGroupMemberList(String groupId, String userGroupId) {
        userGroupMemberListResult.setSource(
                userGroupTask.userGroupMemberList(groupId, userGroupId));
    }

    /** 用户组群成员添加 */
    public SingleSourceLiveData<Resource<String>> getUserGroupMemberAddResult() {
        return userGroupMemberAddResult;
    }

    public void userGroupMemberAdd(String targetId, String userGroupId, List<String> list) {
        userGroupMemberAddResult.setSource(
                userGroupTask.userGroupMemberEdit(targetId, userGroupId, list, true));
    }

    /** 用户组群成员移除 */
    public SingleSourceLiveData<Resource<String>> getUserGroupMemberDelResult() {
        return userGroupMemberDelResult;
    }

    public void userGroupMemberDel(
            ConversationIdentifier identifier, String userGroupId, List<String> list) {
        userGroupMemberDelResult.setSource(
                userGroupTask.userGroupMemberEdit(
                        identifier.getTargetId(), userGroupId, list, false));
    }

    /** 用户组频道绑定 */
    public SingleSourceLiveData<Resource> getChannelUserGroupBindResult() {
        return userGroupBindChannelResult;
    }

    public void userGroupBindChannel(ConversationIdentifier identifier, List<String> userGroupIds) {
        userGroupBindChannelResult.setSource(
                userGroupTask.editChannelUserGroup(
                        identifier.getTargetId(), identifier.getChannelId(), userGroupIds, true));
    }

    /** 用户组频道解除绑定 */
    public SingleSourceLiveData<Resource> getChannelUserGroupUnBindResult() {
        return userGroupUnBindChannelResult;
    }

    public void userGroupUnBindChannel(
            ConversationIdentifier identifier, List<String> userGroupIds) {
        userGroupUnBindChannelResult.setSource(
                userGroupTask.editChannelUserGroup(
                        identifier.getTargetId(), identifier.getChannelId(), userGroupIds, false));
    }

    public boolean editChannelUserGroup(
            ConversationIdentifier identifier, List<String> targetList) {
        if (userGroupListResult.getValue() == null) {
            return false;
        }
        List<UserGroupInfo> sourceList = userGroupListResult.getValue().data;
        // 源List和目标List为空，不需要处理
        if ((targetList == null || targetList.isEmpty())
                && (sourceList == null || sourceList.isEmpty())) {
            return false;
        }
        // 源List和目标List相同，不需要处理
        if (targetList != null && sourceList != null && targetList.size() == sourceList.size()) {
            boolean same = true;
            for (int i = 0; i < targetList.size(); i++) {
                if (!TextUtils.equals(targetList.get(i), sourceList.get(i).userGroupId)) {
                    same = false;
                }
            }
            if (same) {
                return false;
            }
        }

        if (targetList == null || targetList.isEmpty()) {
            List<String> targetUnbindList = new ArrayList<>();
            // 目标List为空，源List全部添加解除绑定List
            for (UserGroupInfo groupInfo : sourceList) {
                targetUnbindList.add(groupInfo.userGroupId);
            }
            userGroupUnBindChannel(identifier, targetUnbindList);
        } else if (sourceList == null || sourceList.isEmpty()) {
            // 源List为空，目标List全部添加绑定List
            userGroupBindChannel(identifier, targetList);
        } else { // 源List目标List均不为空
            List<String> targetBindList = new ArrayList<>();
            List<String> targetUnbindList = new ArrayList<>();

            HashSet<String> sourceSet = new HashSet<>();
            HashSet<String> targetSet = new HashSet<>(targetList);
            for (UserGroupInfo info : sourceList) {
                sourceSet.add(info.userGroupId);
                // 构建目标解除绑定List；
                if (!targetSet.contains(info.userGroupId)) {
                    targetUnbindList.add(info.userGroupId);
                }
            }
            // 构建目标绑定List；
            for (String id : targetList) {
                if (!sourceSet.contains(id)) {
                    targetBindList.add(id);
                }
            }
            if (!targetBindList.isEmpty()) {
                userGroupBindChannel(identifier, targetBindList);
            }
            if (!targetUnbindList.isEmpty()) {
                userGroupUnBindChannel(identifier, targetUnbindList);
            }
        }
        return true;
    }

    private void refreshUserGroupList(ConversationIdentifier identifier) {
        if (mIdentifier != null
                && identifier != null
                && mIdentifier.getType() == identifier.getType()
                && TextUtils.equals(mIdentifier.getTargetId(), identifier.getTargetId())) {
            mMainHandler.post(() -> getUserGroupList(mIdentifier));
        }
    }

    @Override
    public void userGroupDisbandFrom(ConversationIdentifier identifier, String[] userGroupIds) {
        RLog.d(
                TAG,
                "userGroupDisbandFrom: " + mIdentifier + " , " + identifier + " , " + userGroupIds);
        refreshUserGroupList(identifier);
    }

    @Override
    public void userAddedTo(ConversationIdentifier identifier, String[] userGroupIds) {
        RLog.d(TAG, "userAddedTo: " + mIdentifier + " , " + identifier + " , " + userGroupIds);
        refreshUserGroupList(identifier);
    }

    @Override
    public void userRemovedFrom(ConversationIdentifier identifier, String[] userGroupIds) {
        RLog.d(TAG, "userRemovedFrom: " + mIdentifier + " , " + identifier + " , " + userGroupIds);
        refreshUserGroupList(identifier);
    }

    @Override
    public void userGroupBindTo(
            ConversationIdentifier identifier,
            IRongCoreEnum.UltraGroupChannelType channelType,
            String[] userGroupIds) {
        RLog.d(
                TAG,
                "userGroupBindTo: "
                        + mIdentifier
                        + " , "
                        + identifier
                        + " , "
                        + channelType
                        + " , "
                        + userGroupIds);
        refreshUserGroupList(identifier);
    }

    @Override
    public void userGroupUnbindFrom(
            ConversationIdentifier identifier,
            IRongCoreEnum.UltraGroupChannelType channelType,
            String[] userGroupIds) {
        RLog.d(
                TAG,
                "userGroupUnbindFrom: "
                        + mIdentifier
                        + " , "
                        + identifier
                        + " , "
                        + channelType
                        + " , "
                        + userGroupIds);
        refreshUserGroupList(identifier);
    }
}
