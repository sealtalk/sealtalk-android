package cn.rongcloud.im.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import cn.rongcloud.im.common.ThreadManager;
import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.task.FriendTask;
import cn.rongcloud.im.task.GroupTask;
import cn.rongcloud.im.utils.log.SLog;

public class ForwardTransferDialogViewModel extends AndroidViewModel {
    private static final String TAG = "ForwardFragmentViewModel";
    private MutableLiveData<FriendShipInfo> showTransferToFriendDialog;
    private MutableLiveData<GroupEntity> showTransferToGroupDialog;

    private GroupTask groupTask;
    private FriendTask friendTask;

    public ForwardTransferDialogViewModel(@NonNull Application application) {
        super(application);
        groupTask = new GroupTask(application);
        friendTask = new FriendTask(application);
        showTransferToFriendDialog = new MutableLiveData<>();
        showTransferToGroupDialog =  new MutableLiveData<>();
    }


    /**
     * 通知显示 转发给好友的dialog
     * @param userId
     */
    public void showTransferToFriendDialog(String userId) {
        ThreadManager.getInstance().runOnWorkThread(new Runnable() {
            @Override
            public void run() {
                FriendShipInfo friendShipInfo = friendTask.getFriendShipInfoFromDBSync(userId);
                // 有可能某种情况下第一次返回是空， 则重复抓去， 如果一只获取不到， 慢五次就为放弃
                int i = 5;
                while (friendShipInfo == null && i > 0) {
                    try {
                        friendShipInfo = friendTask.getFriendShipInfoFromDBSync(userId);
                        i--;
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                showTransferToFriendDialog.postValue(friendShipInfo);

            }
        });
    }

    /**
     * 通知显示 转发给好友的dialog
     * @param friendShipInfo
     */
    public void showTransferToFriendDialog(FriendShipInfo friendShipInfo) {
        showTransferToFriendDialog.postValue(friendShipInfo);
    }

    /**
     * 通知显示 转发给群的dialog
     * @param groupId
     */
    public void showTransferToGroupDialog(String groupId) {
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
                showTransferToGroupDialog.postValue(groupEntity);

            }
        });
    }


    /**
     * 通知显示 转发给群的dialog
     * @param groupEntity
     */
    public void showTransferToGroupDialog(GroupEntity groupEntity) {
        SLog.d("ss_group", "group==id==" + groupEntity);
        showTransferToGroupDialog.postValue(groupEntity);
    }

    /**
     * 转发好友 dialog
     * @return
     */
    public LiveData<FriendShipInfo> getShowTransferToFriendDialog() {
        return showTransferToFriendDialog;
    }

    /**
     * 转发群组dialog
     * @return
     */
    public LiveData<GroupEntity> getShowTransferToGroupDialog() {
        return showTransferToGroupDialog;
    }



}
