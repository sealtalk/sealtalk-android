package cn.rongcloud.im.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.model.CopyGroupResult;
import cn.rongcloud.im.model.GroupMember;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.task.GroupTask;
import cn.rongcloud.im.utils.SingleSourceLiveData;
import java.util.List;

public class CopyGroupViewModel extends AndroidViewModel {
    private SingleSourceLiveData<Resource<GroupEntity>> groupInfo = new SingleSourceLiveData<>();
    private SingleSourceLiveData<Resource<CopyGroupResult>> copyGroupResult =
            new SingleSourceLiveData<>();
    private GroupTask groupTask;

    public CopyGroupViewModel(@NonNull Application application) {
        super(application);

        groupTask = new GroupTask(application);
    }

    public void requestGroupInfo(String groupId) {
        groupInfo.setSource(groupTask.getGroupInfo(groupId));
    }

    public LiveData<Resource<GroupEntity>> getGroupInfo() {
        return groupInfo;
    }

    /**
     * 复制群组
     *
     * @param groupId
     * @param name
     * @param portraitUri
     */
    public void copyGroup(String groupId, String name, String portraitUri) {
        copyGroupResult.setSource(groupTask.copyGroup(groupId, name, portraitUri));
    }

    public LiveData<Resource<CopyGroupResult>> getCopyGroupResult() {
        return copyGroupResult;
    }

    public LiveData<List<GroupMember>> getGroupMemberInfoList(String groupId) {
        return groupTask.getGroupMemberInfoListInDB(groupId);
    }
}
