package cn.rongcloud.im.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.task.GroupTask;
import cn.rongcloud.im.utils.SingleSourceLiveData;

/**
 * 加入群组视图模型
 */
public class JoinGroupViewModel extends AndroidViewModel {
    private String groupId;
    private GroupTask groupTask;

    private SingleSourceLiveData<Resource<GroupEntity>> groupInfo = new SingleSourceLiveData<>();
    private SingleSourceLiveData<Resource<Void>> joinGroupResult = new SingleSourceLiveData<>();

    public JoinGroupViewModel(@NonNull Application application) {
        super(application);

        groupTask = new GroupTask(application);
    }

    public JoinGroupViewModel(@NonNull Application application, String groupId) {
        super(application);

        this.groupId = groupId;
        groupTask = new GroupTask(application);
        groupInfo.setSource(groupTask.getGroupInfo(groupId));
    }

    /**
     * 获取群组信息
     *
     * @return
     */
    public LiveData<Resource<GroupEntity>> getGroupInfo() {
        return groupInfo;
    }

    /**
     * 获取加入群组结果
     *
     * @return
     */
    public LiveData<Resource<Void>> getJoinGroupInfo() {
        return joinGroupResult;
    }

    /**
     * 请求加入群组
     */
    public void joinToGroup(){
        joinGroupResult.setSource(groupTask.joinGroup(groupId));
    }


    public static class Factory implements ViewModelProvider.Factory {
        private Application application;
        private String groupId;


        public Factory(Application application, String groupId) {
            this.application = application;
            this.groupId = groupId;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            try {
                return modelClass.getConstructor(Application.class, String.class).newInstance(application, groupId);
            } catch (Exception e) {
                throw new RuntimeException("Cannot create an instance of " + modelClass, e);
            }
        }
    }
}
