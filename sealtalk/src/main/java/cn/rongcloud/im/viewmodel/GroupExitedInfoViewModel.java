package cn.rongcloud.im.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import cn.rongcloud.im.common.ThreadManager;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.db.model.GroupExitedMemberInfo;
import cn.rongcloud.im.db.model.GroupNoticeInfo;
import cn.rongcloud.im.db.model.UserInfo;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.task.GroupTask;
import cn.rongcloud.im.task.UserTask;
import cn.rongcloud.im.utils.SingleSourceLiveData;

public class GroupExitedInfoViewModel extends AndroidViewModel {

    private GroupTask groupTask;
    private SingleSourceLiveData<Resource<List<GroupExitedMemberInfo>>> groupExitedInfo = new SingleSourceLiveData<>();

    public GroupExitedInfoViewModel(@NonNull Application application) {
        super(application);
        groupTask = new GroupTask(application);
    }

    /**
     * 请求群通知全部信息
     */
    public void requestExitedInfo(String groupId) {
        groupExitedInfo.setSource(groupTask.getGroupExitedMemberInfo(groupId));
    }

    public LiveData<Resource<List<GroupExitedMemberInfo>>> getExitedInfo() {
        return groupExitedInfo;
    }

}
