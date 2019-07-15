package cn.rongcloud.im.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.UserSimpleInfo;
import cn.rongcloud.im.task.UserTask;
import cn.rongcloud.im.utils.SingleSourceLiveData;

public class BlackListViewModel extends AndroidViewModel {
    private UserTask userTask;

    SingleSourceLiveData<Resource<List<UserSimpleInfo>>> blacklistResult = new SingleSourceLiveData<>();
    public BlackListViewModel(@NonNull Application application) {
        super(application);
        userTask = new UserTask(application);
        getBlackList();
    }

    /**
     * 黑名单
     * @return
     */
    public LiveData<Resource<List<UserSimpleInfo>>> getBlackListResult() {
        return blacklistResult;
    }

    /**
     * 获取黑名单
     */
    private void getBlackList() {
        blacklistResult.setSource(userTask.getBlackList());
    }
}
