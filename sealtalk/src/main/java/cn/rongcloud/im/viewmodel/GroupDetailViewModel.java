package cn.rongcloud.im.viewmodel;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.model.AddMemberResult;
import cn.rongcloud.im.model.GroupMember;
import cn.rongcloud.im.model.GroupNoticeResult;
import cn.rongcloud.im.model.GroupRegularClearResult;
import cn.rongcloud.im.model.RegularClearStatusResult;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Result;
import cn.rongcloud.im.model.ScreenCaptureResult;
import cn.rongcloud.im.task.GroupTask;
import cn.rongcloud.im.task.PrivacyTask;
import cn.rongcloud.im.utils.SingleSourceLiveData;
import cn.rongcloud.im.utils.SingleSourceMapLiveData;
import io.rong.imlib.model.Conversation;

/**
 * 群组详情视图模型
 */
public class GroupDetailViewModel extends AndroidViewModel {
    private SingleSourceLiveData<Resource<GroupEntity>> groupInfoLiveData = new SingleSourceLiveData<>();
    private SingleSourceMapLiveData<Resource<List<GroupMember>>, Resource<List<GroupMember>>> groupMemberListLiveData;
    private SingleSourceLiveData<Resource<GroupNoticeResult>> groupNotice = new SingleSourceLiveData<>();
    private SingleSourceLiveData<Resource<Integer>> regularClearState = new SingleSourceLiveData<>();

    private String groupId;
    private Conversation.ConversationType conversationType;
    private GroupTask groupTask;
    private PrivacyTask privacyTask;

    private SingleSourceLiveData<Resource<Void>> uploadPortraitResult = new SingleSourceLiveData<>();
    private SingleSourceMapLiveData<Resource<List<AddMemberResult>>, Resource<List<AddMemberResult>>> addGroupMemberResult;
    private SingleSourceMapLiveData<Resource<Void>, Resource<Void>> removeGroupMemberResult;
    private SingleSourceLiveData<Resource<Void>> renameGroupNameResult = new SingleSourceLiveData<>();
    private SingleSourceLiveData<Resource<Void>> exitGroupResult = new SingleSourceLiveData<>();
    private MediatorLiveData<GroupMember> myselfInfo = new MediatorLiveData<>();

    private SingleSourceLiveData<Resource<Void>> saveToContactResult = new SingleSourceLiveData<>();
    private SingleSourceLiveData<Resource<Void>> removeFromContactResult = new SingleSourceLiveData<>();

    private SingleSourceLiveData<Resource<ScreenCaptureResult>> screenCaptureResult = new SingleSourceLiveData<>();
    private SingleSourceLiveData<Resource<Void>> setScreenCaptureResult = new SingleSourceLiveData<>();

    private SingleSourceLiveData<Resource<Void>> setCleanTimeResult = new SingleSourceLiveData<>();
    private IMManager imManager;

    public GroupDetailViewModel(@NonNull Application application) {
        super(application);

        groupTask = new GroupTask(application);
        imManager = IMManager.getInstance();
    }

    public GroupDetailViewModel(@NonNull Application application, String targetId, Conversation.ConversationType conversationType) {
        super(application);
        this.groupId = targetId;
        this.conversationType = conversationType;

        privacyTask = new PrivacyTask(application);
        groupTask = new GroupTask(application);
        imManager = IMManager.getInstance();

        groupInfoLiveData.setSource(groupTask.getGroupInfo(groupId));

        groupMemberListLiveData = new SingleSourceMapLiveData<>(new Function<Resource<List<GroupMember>>, Resource<List<GroupMember>>>() {
            @Override
            public Resource<List<GroupMember>> apply(Resource<List<GroupMember>> input) {
                if (input != null && input.data != null) {
                    List<GroupMember> tmpList = new ArrayList<>();
                    tmpList.addAll(input.data);

                    Collections.sort(tmpList, new Comparator<GroupMember>() {
                        @Override
                        public int compare(GroupMember lhs, GroupMember rhs) {
                            if (lhs.getRole() == GroupMember.Role.GROUP_OWNER.getValue()) {
                                return -1;
                            } else if (lhs.getRole() != GroupMember.Role.GROUP_OWNER.getValue() && rhs.getRole() == GroupMember.Role.GROUP_OWNER.getValue()) {
                                return 1;
                            } else if (lhs.getRole() == GroupMember.Role.MANAGEMENT.getValue() && rhs.getRole() == GroupMember.Role.MEMBER.getValue()) {
                                return -1;
                            } else if (lhs.getRole() == GroupMember.Role.MEMBER.getValue() && rhs.getRole() == GroupMember.Role.MANAGEMENT.getValue()) {
                                return 1;
                            } else if (lhs.getRole() == GroupMember.Role.MANAGEMENT.getValue() && rhs.getRole() == GroupMember.Role.MANAGEMENT.getValue()) {
                                return lhs.getJoinTime() > rhs.getJoinTime() ? 1 : -1;
                            } else if (lhs.getRole() == GroupMember.Role.MEMBER.getValue() && rhs.getRole() == GroupMember.Role.MEMBER.getValue()) {
                                return lhs.getJoinTime() > rhs.getJoinTime() ? 1 : -1;
                            }

                            return 0;
                        }
                    });
                    return new Resource<>(input.status, tmpList, input.code);
                }
                return new Resource<>(input.status, null, input.code);
            }
        });

        groupMemberListLiveData.setSource(groupTask.getGroupMemberInfoList(groupId));

        addGroupMemberResult = new SingleSourceMapLiveData<>(resource -> {
            // 考虑到新增成员后一些数据需要同步所以重新加载群组信息和新成员信息
            refreshGroupInfo();
            refreshGroupMemberList();
            return resource;
        });

        removeGroupMemberResult = new SingleSourceMapLiveData<>(resource -> {
            // 删除成员后一些数据需要同步所以重新加载群组信息，因为是删除操作所以不需要再加载成员信息
            refreshGroupInfo();
            return resource;
        });

        myselfInfo.addSource(groupMemberListLiveData, new Observer<Resource<List<GroupMember>>>() {
            @Override
            public void onChanged(Resource<List<GroupMember>> listResource) {
                if (listResource.data != null && listResource.data.size() > 0) {
                    for (GroupMember member : listResource.data) {
                        if (member.getUserId().equals(imManager.getCurrentId())) {
                            myselfInfo.postValue(member);
                            break;
                        }
                    }
                }
            }
        });

        requestGroupNotice(groupId);
        requestRegularState(groupId);
        getScreenCaptureStatus();
    }

    /**
     * 刷新群组信息
     * 此方法一般不需要调用，默认在初始化时会数据刷新，仅在特殊情况需要请求网络同步最新数据时需要
     */
    public void refreshGroupInfo() {
        groupInfoLiveData.setSource(groupTask.getGroupInfo(groupId));
    }

    /**
     * 刷新群组成员列表
     * 此方法一般不需要调用，默认在初始化时会数据刷新，仅在特殊情况需要请求网络同步最新数据时需要
     */
    public void refreshGroupMemberList() {
        groupMemberListLiveData.setSource(groupTask.getGroupMemberInfoList(groupId));
    }

    /**
     * 获取群组信息
     *
     * @return
     */
    public LiveData<Resource<GroupEntity>> getGroupInfo() {
        return groupInfoLiveData;
    }

    /**
     * 上传并设置头像
     *
     * @param imageUri
     */
    public void setGroupPortrait(Uri imageUri) {
        uploadPortraitResult.setSource(groupTask.uploadAndSetGroupPortrait(groupId, imageUri));
    }

    /**
     * 获取上传群组头像结果
     *
     * @return
     */
    public LiveData<Resource<Void>> getUploadPortraitResult() {
        return uploadPortraitResult;
    }

    /**
     * 获取群组成员列表
     *
     * @return
     */
    public LiveData<Resource<List<GroupMember>>> getGroupMemberList() {
        return groupMemberListLiveData;
    }

    /**
     * 添加群组成员
     *
     * @param memberIdList
     */
    public void addGroupMember(List<String> memberIdList) {
        if (memberIdList != null && memberIdList.size() > 0) {
            addGroupMemberResult.setSource(groupTask.addGroupMember(groupId, memberIdList));
        }
    }

    /**
     * 移除群组成员
     *
     * @param memberIdList
     */
    public void removeGroupMember(List<String> memberIdList) {
        if (memberIdList != null && memberIdList.size() > 0) {
            removeGroupMemberResult.setSource(groupTask.kickGroupMember(groupId, memberIdList));
        }
    }

    /**
     * 获取添加群组成员结果
     *
     * @return
     */
    public LiveData<Resource<List<AddMemberResult>>> getAddGroupMemberResult() {
        return addGroupMemberResult;
    }

    /**
     * 获取日出群组成员结果
     *
     * @return
     */
    public LiveData<Resource<Void>> getRemoveGroupMemberResult() {
        return removeGroupMemberResult;
    }

    /**
     * 修改群组名称
     *
     * @param newGroupName
     */
    public void renameGroupName(String newGroupName) {
        renameGroupNameResult.setSource(groupTask.renameGroup(groupId, newGroupName));
    }

    /**
     * 获取修改群组名称结果
     *
     * @return
     */
    public LiveData<Resource<Void>> getRenameGroupResult() {
        return renameGroupNameResult;
    }

    /**
     * 解散群组
     */
    public void dismissGroup() {
        exitGroupResult.setSource(groupTask.dismissGroup(groupId));
    }

    /**
     * 退出群组
     */
    public void exitGroup() {
        exitGroupResult.setSource(groupTask.quitGroup(groupId));
    }

    /**
     * 获取退出或解散群组的结果
     *
     * @return
     */
    public LiveData<Resource<Void>> getExitGroupResult() {
        return exitGroupResult;
    }

    /**
     * 获取用户信息
     *
     * @return
     */
    public LiveData<GroupMember> getMyselfInfo() {
        return myselfInfo;
    }

    /**
     * 保存到通讯录
     */
    public void saveToContact() {
        Resource<GroupEntity> value = groupInfoLiveData.getValue();
        if (value != null && value.data != null && value.data.getIsInContact() == 1) return;
        saveToContactResult.setSource(groupTask.saveGroupToContact(groupId));
    }

    /**
     * 获取保存到通讯录结果
     *
     * @return
     */
    public LiveData<Resource<Void>> getSaveToContact() {
        return saveToContactResult;
    }

    /**
     * 从通讯录中删除
     */
    public void removeFromContact() {
        Resource<GroupEntity> value = groupInfoLiveData.getValue();
        if (value != null && value.data != null && value.data.getIsInContact() == 0) return;

        removeFromContactResult.setSource(groupTask.removeGroupFromContact(groupId));
    }

    /**
     * 获取通讯录删除结果
     *
     * @return
     */
    public LiveData<Resource<Void>> getRemoveFromContactResult() {
        return removeFromContactResult;
    }

    /**
     * 请求群公告
     *
     * @param groupId
     */
    public void requestGroupNotice(String groupId) {
        groupNotice.setSource(groupTask.getGroupNotice(groupId));
    }

    /**
     * 请求设置定时清理时间状态
     *
     * @param groupId
     */
    public void requestRegularState(String groupId) {
        regularClearState.setSource(groupTask.getRegularClearState(groupId));
    }

    public LiveData<Resource<Integer>> getRegularState() {
        return regularClearState;
    }

    /**
     * 设置定时清理群消息
     *
     * @param regularClearState
     */
    public void setRegularClear(int regularClearState) {
        setCleanTimeResult.setSource(groupTask.setRegularClear(groupId, regularClearState));
    }

    /**
     * 设置定时清理群消息结果
     *
     * @return
     */
    public LiveData<Resource<Void>> getRegularClearResult() {
        return setCleanTimeResult;
    }


    /**
     * 获取群公告
     *
     * @return
     */
    public LiveData<Resource<GroupNoticeResult>> getGroupNoticeResult() {
        return groupNotice;
    }

    /**
     * 获取是否开启截屏通知(群内聊天 targetId 为 groupId)
     */
    private void getScreenCaptureStatus() {
        screenCaptureResult.setSource(privacyTask.getScreenCapture(conversationType.getValue(), groupId));
    }

    public LiveData<Resource<ScreenCaptureResult>> getScreenCaptureStatusResult() {
        return screenCaptureResult;
    }

    /**
     * 设置是否开启截屏通知
     *
     * @param status
     */
    public void setScreenCaptureStatus(int status) {
        setScreenCaptureResult.setSource(privacyTask.setScreenCapture(conversationType.getValue(), groupId, status));
    }

    public LiveData<Resource<Void>> getSetScreenCaptureResult(){
        return  setScreenCaptureResult;
    }

    public static class Factory implements ViewModelProvider.Factory {
        private String targetId;
        private Conversation.ConversationType conversationType;
        private Application application;

        public Factory(Application application, String targetId, Conversation.ConversationType conversationType) {
            this.conversationType = conversationType;
            this.targetId = targetId;
            this.application = application;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            try {
                return modelClass.getConstructor(Application.class, String.class, Conversation.ConversationType.class).newInstance(application, targetId, conversationType);
            } catch (Exception e) {
                throw new RuntimeException("Cannot create an instance of " + modelClass, e);
            }
        }
    }
}
