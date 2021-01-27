package cn.rongcloud.im.viewmodel;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import cn.rongcloud.im.db.model.UserInfo;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Result;
import cn.rongcloud.im.task.UserTask;
import cn.rongcloud.im.utils.SingleSourceLiveData;
import cn.rongcloud.im.utils.log.SLog;

public class UserInfoViewModel extends AndroidViewModel {

    private final UserTask userTask;
    private IMManager imManager;
    private SingleSourceLiveData<Resource<UserInfo>> userInfo = new SingleSourceLiveData<>();
    private SingleSourceLiveData<Resource<Result>> setNameResult = new SingleSourceLiveData<>();
    private SingleSourceLiveData<Resource<Result>> uploadPotraitResult = new SingleSourceLiveData<>();
    private SingleSourceLiveData<Resource<Result>> changePasswordResult = new SingleSourceLiveData<>();
    private SingleSourceLiveData<Resource<Result>> setStAccountResult = new SingleSourceLiveData<>();
    private SingleSourceLiveData<Resource<Result>> setGenderResult = new SingleSourceLiveData<>();

    public UserInfoViewModel(@NonNull Application application) {
        super(application);
        imManager = IMManager.getInstance();
        userTask = new UserTask(application);
        requestUserInfo(imManager.getCurrentId());
    }

    public UserInfoViewModel(String userId, @NonNull Application application) {
        super(application);
        userTask = new UserTask(application);
        requestUserInfo(userId);
    }


    /**
     * 获取 UserInfo
     *
     * @return
     */
    public LiveData<Resource<UserInfo>> getUserInfo() {
        return userInfo;
    }

    /**
     * 设置name 结果
     *
     * @return
     */
    public LiveData<Resource<Result>> getSetNameResult() {
        return setNameResult;
    }

    /**
     * 设置 StAccount 结果
     *
     * @return
     */
    public LiveData<Resource<Result>> getSetStAccountResult() {
        return setStAccountResult;
    }

    /**
     * 设置性别结果
     *
     * @return
     */
    public LiveData<Resource<Result>> getSetGenderResult() {
        return setGenderResult;
    }

    /**
     * 上传头像结果
     *
     * @return
     */
    public LiveData<Resource<Result>> getUploadPortraitResult() {
        return uploadPotraitResult;
    }

    /**
     * 密码修改
     *
     * @return
     */
    public LiveData<Resource<Result>> getChangePasswordResult() {
        return changePasswordResult;
    }

    /**
     * 设置用户名
     *
     * @param newName
     */
    public void setName(String newName) {
        setNameResult.setSource(userTask.setMyNickName(newName));
    }

    /**
     * 设置自己的 SealTalk 账号
     *
     * @param stAccount
     */
    public void setStAccount(String stAccount) {
        setStAccountResult.setSource(userTask.setStAccount(stAccount));
    }

    /**
     * 设置性别
     *
     * @param gender
     */
    public void setGender(String gender) {
        setGenderResult.setSource(userTask.setGender(gender));
    }

    /**
     * 上传头像
     *
     * @param uri
     */
    public void uploadPortrait(Uri uri) {
        uploadPotraitResult.setSource(userTask.setPortrait(uri));
    }

    /**
     * 修改密码
     *
     * @param oldPassword
     * @param newPassword
     */
    public void changePassword(String oldPassword, String newPassword) {
        changePasswordResult.setSource(userTask.changePassword(oldPassword, newPassword));
    }

    /**
     * 请求用户信息
     *
     * @param userId
     */
    public void requestUserInfo(String userId) {
        SLog.d("ss_usertask", "userId == " + userId);
        userInfo.setSource(userTask.getUserInfo(userId));
    }


    /**
     * 退出
     */
    public void logout() {
        imManager.logout();
        userTask.logout();
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private String userId;
        private Application application;

        public Factory(String userId, Application application) {
            this.userId = userId;
            this.application = application;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            try {
                return modelClass.getConstructor(String.class, Application.class).newInstance(userId, application);
            } catch (Exception e) {
                throw new RuntimeException("Cannot create an instance of " + modelClass, e);
            }
        }
    }

}
