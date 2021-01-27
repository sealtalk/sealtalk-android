package cn.rongcloud.im.viewmodel;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.model.GroupMember;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.task.GroupTask;
import cn.rongcloud.im.utils.SingleSourceLiveData;
import cn.rongcloud.im.utils.SingleSourceMapLiveData;

/**
 * 群组成员列表视图模型
 */
public class GroupMemberListViewModel extends AndroidViewModel {
    private Context context;
    private String groupId;

//    private SingleSourceLiveData<Resource<List<GroupMember>>> groupMemberList = new SingleSourceLiveData<>();
    private SingleSourceLiveData<Resource<GroupEntity>> groupInfoLiveData = new SingleSourceLiveData<>();
    private SingleSourceMapLiveData<Resource<List<GroupMember>>, Resource<List<GroupMember>>> groupMemberList;

    private GroupTask groupTask;

    public GroupMemberListViewModel(@NonNull Application application) {
        super(application);

        groupTask = new GroupTask(application.getApplicationContext());
    }

    public GroupMemberListViewModel(@NonNull Application application, String groupId) {
        super(application);

        this.groupId = groupId;
        groupTask = new GroupTask(application.getApplicationContext());
        groupInfoLiveData.setSource(groupTask.getGroupInfo(groupId));
        groupMemberList = new SingleSourceMapLiveData<>(new Function<Resource<List<GroupMember>>, Resource<List<GroupMember>>>() {
            @Override
            public Resource<List<GroupMember>> apply(Resource<List<GroupMember>> input) {
                if (input != null && input.data != null) {
                    List<GroupMember> tmpList = new ArrayList<>();
                    tmpList.addAll(input.data);

                    Collections.sort(tmpList, new Comparator<GroupMember>() {
                        @Override
                        public int compare(GroupMember lhs, GroupMember rhs) {
                            if (lhs.getRole() == GroupMember.Role.GROUP_OWNER.getValue()) {
                                return -1;
                            } else if (lhs.getRole() != GroupMember.Role.GROUP_OWNER.getValue() && rhs.getRole() == GroupMember.Role.GROUP_OWNER.getValue()) {
                                return 1;
                            } else if (lhs.getRole() == GroupMember.Role.MANAGEMENT.getValue() && rhs.getRole() == GroupMember.Role.MEMBER.getValue()) {
                                return -1;
                            } else if (lhs.getRole() == GroupMember.Role.MEMBER.getValue() && rhs.getRole() == GroupMember.Role.MANAGEMENT.getValue()) {
                                return 1;
                            } else if(lhs.getRole() == GroupMember.Role.MANAGEMENT.getValue() && rhs.getRole() == GroupMember.Role.MANAGEMENT.getValue()){
                                return lhs.getJoinTime() > rhs.getJoinTime() ? 1 : -1;
                            } else if(lhs.getRole() == GroupMember.Role.MEMBER.getValue() && rhs.getRole() == GroupMember.Role.MEMBER.getValue()) {
                                return lhs.getJoinTime() > rhs.getJoinTime() ? 1 : -1;
                            }

                            return 0;
                        }
                    });
                    return new Resource<>(input.status, tmpList, input.code);
                }
                return new Resource<>(input.status, null, input.code);
            }
        });
        groupMemberList.setSource(groupTask.getGroupMemberInfoList(groupId));
    }

    /**
     * 获取群组成员列表
     *
     * @return
     */
    public LiveData<Resource<List<GroupMember>>> getGroupMemberList() {
        return groupMemberList;
    }

    /**
     * 按姓名查询群组成员
     *
     * @param filterName
     */
    public void requestGroupMember(String filterName) {
        groupMemberList.setSource(groupTask.getGroupMemberInfoList(groupId, filterName));
    }

    /**
     * 请求群组成员列表
     *
     * @param groupId
     */
    public void requestGroupMemberList(String groupId) {
        groupMemberList.setSource(groupTask.getGroupMemberInfoList(groupId));
    }

    /**
     * 请求群组信息
     *
     * @param groupId
     */
    public void requestGroupInfo(String groupId) {
        groupInfoLiveData.setSource(groupTask.getGroupInfo(groupId));
    }

    /**
     * 获取群组信息
     *
     * @return
     */
    public LiveData<Resource<GroupEntity>> getGroupInfo() {
        return groupInfoLiveData;
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
