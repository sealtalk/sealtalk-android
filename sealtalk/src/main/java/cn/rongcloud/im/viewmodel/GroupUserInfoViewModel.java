package cn.rongcloud.im.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;

import cn.rongcloud.im.db.model.GroupMemberInfoDes;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.task.GroupTask;
import cn.rongcloud.im.utils.SingleSourceLiveData;

public class GroupUserInfoViewModel extends AndroidViewModel {
    private SingleSourceLiveData<Resource<GroupMemberInfoDes>> groupMemberInfoDes = new SingleSourceLiveData<>();
    private SingleSourceLiveData<Resource<Void>> setGroupMemberInfoDesResult = new SingleSourceLiveData<>();
    private GroupTask groupTask;

    public GroupUserInfoViewModel(@NonNull Application application) {
        super(application);
        groupTask = new GroupTask(application);
    }

    /**
     * 获取群成员用户信息
     *
     * @param groupId
     * @param memberId
     */
    public void requestMemberInfoDes(String groupId, String memberId) {
        groupMemberInfoDes.setSource(groupTask.getGroupMemberInfoDes(groupId, memberId));
    }

    public LiveData<Resource<GroupMemberInfoDes>> getGroupMemberInfoDes() {
        return groupMemberInfoDes;
    }

    /**
     * 设置群成员用户信息
     *
     * @param groupId
     * @param memberId
     * @param groupNickname
     * @param region
     * @param phone
     * @param WeChat
     * @param Alipay
     * @param memberDesc
     */
    public void setMemberInfoDes(String groupId, String memberId, String groupNickname
            , String region, String phone, String WeChat, String Alipay, ArrayList<String> memberDesc) {
        setGroupMemberInfoDesResult.setSource(groupTask.setGroupMemberInfoDes(groupId, memberId, groupNickname
                , region, phone, WeChat, Alipay, memberDesc));
    }

    public LiveData<Resource<Void>> setMemberInfoDesResult() {
        return setGroupMemberInfoDesResult;
    }

}

