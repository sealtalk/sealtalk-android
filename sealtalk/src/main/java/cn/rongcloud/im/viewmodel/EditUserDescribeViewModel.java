package cn.rongcloud.im.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import cn.rongcloud.im.db.model.FriendDescription;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.task.FriendTask;
import cn.rongcloud.im.utils.SingleSourceLiveData;

/** 用户详细视图模型 */
public class EditUserDescribeViewModel extends AndroidViewModel {

    private SingleSourceLiveData<Resource<FriendDescription>> friendDescription =
            new SingleSourceLiveData<>();
    private SingleSourceLiveData<Resource<Void>> setFriendDescriptionResult =
            new SingleSourceLiveData<>();
    private FriendTask friendTask;
    private String userId;

    public EditUserDescribeViewModel(@NonNull Application application) {
        super(application);
    }

    public EditUserDescribeViewModel(@NonNull Application application, String userId) {
        super(application);
        this.userId = userId;
        this.friendTask = new FriendTask(application);
        requestFriendDescription();
    }

    /** 获取朋友描述 */
    public void requestFriendDescription() {
        friendDescription.setSource(friendTask.getFriendDescription(userId));
    }

    public LiveData<Resource<FriendDescription>> getFriendDescription() {
        return friendDescription;
    }

    /**
     * 设置朋友描述
     *
     * @param friendId
     * @param displayName
     * @param region
     * @param phone
     * @param description
     * @param imageUri
     */
    public void setFriendDescription(
            String friendId,
            String displayName,
            String region,
            String phone,
            String description,
            String imageUri) {
        setFriendDescriptionResult.setSource(
                friendTask.setFriendDescription(
                        friendId, displayName, region, phone, description, imageUri));
    }

    public LiveData<Resource<Void>> setFriendDescriptionResult() {
        return setFriendDescriptionResult;
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
                return modelClass
                        .getConstructor(Application.class, String.class)
                        .newInstance(application, userId);
            } catch (Exception e) {
                throw new RuntimeException("Cannot create an instance of " + modelClass, e);
            }
        }
    }
}
