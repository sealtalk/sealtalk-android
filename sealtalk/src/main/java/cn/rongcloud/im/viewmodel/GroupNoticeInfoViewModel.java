package cn.rongcloud.im.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import cn.rongcloud.im.common.ThreadManager;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.db.model.GroupNoticeInfo;
import cn.rongcloud.im.db.model.UserInfo;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.task.GroupTask;
import cn.rongcloud.im.task.UserTask;
import cn.rongcloud.im.utils.SingleSourceLiveData;

public class GroupNoticeInfoViewModel extends AndroidViewModel {

    private GroupTask groupTask;
    private UserTask userTask;
    private SingleSourceLiveData<Resource<List<GroupNoticeInfo>>> groupNoticeInfo = new SingleSourceLiveData<>();
    private SingleSourceLiveData<Resource<Void>> clearNoticeResult = new SingleSourceLiveData<>();
    private MutableLiveData<GroupEntity> showCertifiTipsDialog = new MutableLiveData<>();

    public GroupNoticeInfoViewModel(@NonNull Application application) {
        super(application);
        groupTask = new GroupTask(application);
        userTask = new UserTask(application);
        requestNoticeInfo();
    }

    /**
     * 请求群通知全部信息
     */
    public void requestNoticeInfo() {
        groupNoticeInfo.setSource(groupTask.getGroupNoticeInfo());
    }

    public SingleSourceLiveData<Resource<List<GroupNoticeInfo>>> getGroupNoticeInfo() {
        return groupNoticeInfo;
    }

    /**
     * 设置消息状态
     *
     * @param groupId
     * @param receiverId
     * @param status
     * @param noticeId
     */
    public LiveData<Resource<Void>> setGroupNoticeStatus(String groupId, String receiverId, String status, String noticeId) {
        return groupTask.setNoticeStatus(groupId, receiverId, status, noticeId);
    }

    /**
     * 获取群信息
     *
     * @param groupId
     * @return
     */
    public LiveData<Resource<GroupEntity>> getGroupInfo(String groupId) {
        return groupTask.getGroupInfo(groupId);
    }

    /**
     * 获取是否有群认证
     *
     * @param groupId
     * @return
     */
    public LiveData<GroupEntity> showCertifiTipsDialog(String groupId) {
        ThreadManager.getInstance().runOnWorkThread(new Runnable() {
            @Override
            public void run() {
                GroupEntity groupEntity = groupTask.getGroupInfoSync(groupId);
                int i = 5;
                while (groupEntity == null && i > 0) {
                    try {
                        groupEntity = groupTask.getGroupInfoSync(groupId);
                        i--;
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                showCertifiTipsDialog.postValue(groupEntity);
            }
        });
        return showCertifiTipsDialog;
    }

    /**
     * 清空群信息
     */
    public void clearNotice() {
        clearNoticeResult.setSource(groupTask.clearGroupNotice());
    }

    public LiveData<Resource<Void>> getClearNoticeResult() {
        return clearNoticeResult;
    }

    /**
     * 获取用户信息
     *
     * @param userId
     * @return
     */
    public LiveData<Resource<UserInfo>> getUserInfo(String userId) {
        return userTask.getUserInfo(userId);
    }

}
