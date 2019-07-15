package cn.rongcloud.im.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.task.UserTask;
import cn.rongcloud.im.utils.SingleSourceMapLiveData;

public class BaseActivityViewModel extends AndroidViewModel {
    private final IMManager imManager;
    private UserTask userTask;
    private SingleSourceMapLiveData<Boolean, Boolean> kickedOffline;
    public BaseActivityViewModel(@NonNull Application application) {
        super(application);
        imManager = IMManager.getInstance();
        userTask = new UserTask(application);
        kickedOffline = new SingleSourceMapLiveData<>(new Function<Boolean, Boolean>() {
            @Override
            public Boolean apply(Boolean input) {
                userTask.logout();
                return input;
            }
        });
        kickedOffline.setSource(imManager.getKickedOffline());
    }

    /**
     * 被踢通知
     * @return
     */
    public LiveData<Boolean> getKickedOffline() {
        return kickedOffline;
    }
}
