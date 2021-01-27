package cn.rongcloud.im.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.task.FriendTask;
import cn.rongcloud.im.utils.SingleSourceLiveData;

/**
 * 设置别名视图模型
 */
public class EditAliasViewModel extends AndroidViewModel {
    private SingleSourceLiveData<Resource<Void>> setAliasResult = new SingleSourceLiveData<>();

    private String userId;
    private FriendTask friendTask;

    public EditAliasViewModel(@NonNull Application application) {
        super(application);
    }

    public EditAliasViewModel(@NonNull Application application, String userId) {
        super(application);
        this.userId = userId;
        friendTask = new FriendTask(application);
    }

    /**
     * 设置备注名称
     *
     * @param alias
     */
    public void setAlias(String alias) {
        setAliasResult.setSource(friendTask.setFriendAliasName(userId, alias));
    }

    /**
     * 获取设置备注名结果
     *
     * @return
     */
    public LiveData<Resource<Void>> getSetAliasResult() {
        return setAliasResult;
    }


    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private String userId;
        private Application application;

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
