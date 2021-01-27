package cn.rongcloud.im.viewmodel;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import java.util.List;

import cn.rongcloud.im.common.ErrorCode;
import cn.rongcloud.im.model.GroupResult;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.task.GroupTask;

/**
 * 创建群组视图模型
 */
public class CreateGroupViewModel extends AndroidViewModel {
    private MediatorLiveData<Resource<GroupResult>> createGroupResult = new MediatorLiveData<>();

    private GroupTask groupTask;

    public CreateGroupViewModel(@NonNull Application application) {
        super(application);

        groupTask = new GroupTask(application);
    }

    public void createGroup(String groupName, Uri groupPortrait, List<String> memberIdList) {
        LiveData<Resource<GroupResult>> createGroupResource = groupTask.createGroup(groupName, memberIdList);
        createGroupResult.addSource(createGroupResource, groupResultResource -> {
            if (groupResultResource.status != Status.LOADING) {
                createGroupResult.removeSource(createGroupResource);
            }
            // 判断是否创建群组成功
            if (groupResultResource.status == Status.SUCCESS) {
                GroupResult groupResult = groupResultResource.data;
                if (groupResult != null) {
                    // 上传群组头像
                    if (groupPortrait != null) {
                        nextToUploadPortraitResult(groupResult, groupPortrait);
                    } else {
                        createGroupResult.setValue(Resource.success(groupResult));
                    }
                } else {
                    createGroupResult.setValue(Resource.error(ErrorCode.API_ERR_OTHER.getCode(), null));
                }
            } else {
                createGroupResult.setValue(groupResultResource);
            }
        });
    }

    /**
     * 完成上传
     *
     * @param groupResult
     * @param groupPortrait
     */
    private void nextToUploadPortraitResult(GroupResult groupResult, Uri groupPortrait) {
        // 进行上传群组头像
        LiveData<Resource<Void>> uploadResource = groupTask.uploadAndSetGroupPortrait(groupResult.id, groupPortrait);
        createGroupResult.addSource(uploadResource, resource -> {
            if (resource.status != Status.LOADING) {
                createGroupResult.removeSource(uploadResource);
            }
            // 判断是否上传头像成功
            if (resource.status == Status.SUCCESS) {
                createGroupResult.setValue(Resource.success(groupResult));
            } else {
                createGroupResult.setValue(Resource.error(resource.code, groupResult));
            }
        });
    }

    /**
     * 获取创建群组结果
     *
     * @return
     */
    public LiveData<Resource<GroupResult>> getCreateGroupResult() {
        return createGroupResult;
    }
}
