package cn.rongcloud.im.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.db.model.UserInfo;
import cn.rongcloud.im.model.AddFriendResult;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.model.UserSimpleInfo;
import cn.rongcloud.im.task.FriendTask;
import cn.rongcloud.im.task.UserTask;
import cn.rongcloud.im.utils.SingleSourceLiveData;

/**
 * 用户详细视图模型
 */
public class UserDetailViewModel extends AndroidViewModel {
    private MediatorLiveData<Resource<UserInfo>> userInfoLiveData = new MediatorLiveData<>();
    private SingleSourceLiveData<Resource<AddFriendResult>> inviteResult = new SingleSourceLiveData<>();
    private SingleSourceLiveData<Resource<Void>> addBlackListResult = new SingleSourceLiveData<>();
    private SingleSourceLiveData<Resource<Void>> removeBlackListResult = new SingleSourceLiveData<>();
    private SingleSourceLiveData<Resource<Void>> removeFriendResult = new SingleSourceLiveData<>();
    private LiveData<Boolean> isInBlackList;

    private String userId;
    private UserTask userTask;
    private FriendTask friendTask;

    public UserDetailViewModel(@NonNull Application application) {
        super(application);
    }

    public UserDetailViewModel(@NonNull Application application, String userId) {
        super(application);

        this.userId = userId;

        userTask = new UserTask(application);
        friendTask = new FriendTask(application);

        // 获取用于信息前先获取好友信息
        LiveData<Resource<FriendShipInfo>> friendInfo = friendTask.getFriendInfo(userId);
        userInfoLiveData.addSource(friendInfo, friendShipInfoResource -> {
            // 当有结果时，获取用户信息。此前有好友信息则会更新用户表，没有则只获取用户信息
            if (friendShipInfoResource.status != Status.LOADING) {
                userInfoLiveData.removeSource(friendInfo);
                userInfoLiveData.addSource(userTask.getUserInfo(userId), resource -> userInfoLiveData.setValue(resource));
            }
        });
        LiveData<Resource<UserSimpleInfo>> blackListUser = userTask.getInBlackListUser(userId);
        isInBlackList = Transformations.map(blackListUser, resource -> {
            // 当用户在黑名单时，返回在黑名单状态
            UserSimpleInfo data = resource.data;
            return data != null;
        });
    }

    /**
     * 获取用户信息
     *
     * @return
     */
    public LiveData<Resource<UserInfo>> getUserInfo() {
        return userInfoLiveData;
    }

    /**
     * 添加好友
     *
     * @param inviteMsg
     */
    public void inviteFriend(String inviteMsg) {
        inviteResult.setSource(friendTask.inviteFriend(userId, inviteMsg));
    }

    /**
     * 添加到黑名单
     */
    public void addToBlackList() {
        addBlackListResult.setSource(userTask.addToBlackList(userId));
    }

    /**
     * 移除黑名单
     */
    public void removeFromBlackList() {
        removeBlackListResult.setSource(userTask.removeFromBlackList(userId));
    }

    /**
     * 删除好友
     * @param friendId
     */
    public void deleteFriend(String friendId){
        removeFriendResult.setSource(friendTask.deleteFriend(friendId));
    }

    /**
     * 获取删除好友结果
     *
     * @return
     */
    public LiveData<Resource<Void>> getDeleteFriendResult(){
        return removeFriendResult;
    }

    /**
     * 判断当前用户是否在黑名单
     *
     * @return
     */
    public LiveData<Boolean> getIsInBlackList() {
        return isInBlackList;
    }

    /**
     * 获取添加到黑名单结果
     *
     * @return
     */
    public LiveData<Resource<Void>> getAddBlackListResult() {
        return addBlackListResult;
    }

    /**
     * 获取移除黑名单结果
     *
     * @return
     */
    public LiveData<Resource<Void>> getRemoveBlackListResult() {
        return removeBlackListResult;
    }

    /**
     * 获取邀请好友结果
     *
     * @return
     */
    public LiveData<Resource<AddFriendResult>> getInviteFriendResult() {
        return inviteResult;
    }

    public static class Factory implements ViewModelProvider.Factory {
        private Application application;
        private String userId;

        public Factory(Application application, String userId) {
            this.application = application;
            this.userId = userId;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            try {
                return modelClass.getConstructor(Application.class, String.class).newInstance(application, userId);
            } catch (Exception e) {
                throw new RuntimeException("Cannot create an instance of " + modelClass, e);
            }
        }
    }
}
