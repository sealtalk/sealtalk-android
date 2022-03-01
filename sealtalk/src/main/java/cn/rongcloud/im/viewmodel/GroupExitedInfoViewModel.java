package cn.rongcloud.im.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import cn.rongcloud.im.db.model.GroupExitedMemberInfo;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.task.GroupTask;
import cn.rongcloud.im.utils.SingleSourceLiveData;
import java.util.List;

public class GroupExitedInfoViewModel extends AndroidViewModel {

    private GroupTask groupTask;
    private SingleSourceLiveData<Resource<List<GroupExitedMemberInfo>>> groupExitedInfo =
            new SingleSourceLiveData<>();

    public GroupExitedInfoViewModel(@NonNull Application application) {
        super(application);
        groupTask = new GroupTask(application);
    }

    /** 请求群通知全部信息 */
    public void requestExitedInfo(String groupId) {
        groupExitedInfo.setSource(groupTask.getGroupExitedMemberInfo(groupId));
    }

    public LiveData<Resource<List<GroupExitedMemberInfo>>> getExitedInfo() {
        return groupExitedInfo;
    }
}
