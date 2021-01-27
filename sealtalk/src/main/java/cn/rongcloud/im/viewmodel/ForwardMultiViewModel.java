package cn.rongcloud.im.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.task.FriendTask;
import cn.rongcloud.im.task.GroupTask;

/**
 * 转发时,这里保存最终选择的数据
 */
public class ForwardMultiViewModel extends AndroidViewModel {
    private Set<String> checkedGroupList;
    private Set<String> checkedFriendList;
    private MutableLiveData<CheckCount> checkCountLiveData;
    private MediatorLiveData<DialogData> dialogDataMutableLiveData;
    private GroupTask groupTask;
    private FriendTask friendTask;
    private boolean isShowDialog;
    private List<GroupEntity> groupEntities;
    private List<FriendShipInfo> friendShipInfos;
    private int taskCount;

    public ForwardMultiViewModel(@NonNull Application application) {
        super(application);
        checkedGroupList = new HashSet<>();
        checkedFriendList = new HashSet<>();
        checkCountLiveData = new MutableLiveData<>();
        dialogDataMutableLiveData = new MediatorLiveData<>();
        groupTask = new GroupTask(application);
        friendTask = new FriendTask(application);
    }

    public void init(List<String> checkUsers, List<String> checkGroups) {
        checkedFriendList = new HashSet<>();
        checkedFriendList.addAll(checkUsers);
        checkedGroupList = new HashSet<>();
        checkedGroupList.addAll(checkGroups);
        checkCountLiveData.postValue(new CheckCount(checkedGroupList.size(), checkedFriendList.size()));
    }


    public void switchCheckGroup(GroupEntity groupEntity) {
        String id = groupEntity.getId();
        if (checkedGroupList.contains(id)) {
            checkedGroupList.remove(id);
        } else {
            checkedGroupList.add(id);
        }
        checkCountLiveData.postValue(new CheckCount(checkedGroupList.size(), checkedFriendList.size()));
    }

    public void switchCheckFriend(FriendShipInfo friendShipInfo) {
        String id = friendShipInfo.getUser().getId();
        if (checkedFriendList.contains(id)) {
            checkedFriendList.remove(id);
        } else {
            checkedFriendList.add(id);
        }
        checkCountLiveData.postValue(new CheckCount(checkedGroupList.size(), checkedFriendList.size()));
    }

    public LiveData<CheckCount> getCheckCountLiveData() {
        return checkCountLiveData;
    }

    public static class CheckCount {
        private int groupCount;
        private int friendCount;

        public CheckCount(int groupCount, int firendCount) {
            this.groupCount = groupCount;
            this.friendCount = firendCount;
        }

        public int getGroupCount() {
            return groupCount;
        }

        public int getFriendCount() {
            return friendCount;
        }
    }

    public static class DialogData {
        private List<GroupEntity> groups;
        private List<FriendShipInfo> friendShipInfos;

        public DialogData(List<GroupEntity> groups, List<FriendShipInfo> friendShipInfos) {
            this.groups = groups;
            this.friendShipInfos = friendShipInfos;
        }

        public List<GroupEntity> getGroups() {
            return groups;
        }

        public List<FriendShipInfo> getFriendShipInfos() {
            return friendShipInfos;
        }
    }

    public List<String> getCheckedList() {
        List<String> result = new ArrayList<>();
        result.addAll(checkedGroupList);
        result.addAll(checkedFriendList);
        return result;
    }

    public ArrayList<String> getGroupCheckedList() {
        return new ArrayList<>(checkedGroupList);
    }

    public ArrayList<String> getFriendCheckedList() {
        return new ArrayList<>(checkedFriendList);
    }

    public void showMultiDialog(List<String> groupIds, List<String> friendIds) {
        if (taskCount != 0) {
            return;
        }
        taskCount = 2;
        LiveData<List<GroupEntity>> groupLiveData = groupTask.getGroupInfoList(groupIds.toArray(new String[groupIds.size()]));
        dialogDataMutableLiveData.addSource(groupLiveData, new Observer<List<GroupEntity>>() {
            @Override
            public void onChanged(List<GroupEntity> entities) {
                dialogDataMutableLiveData.removeSource(groupLiveData);
                groupEntities = entities;
                taskCount--;
                setDialogDataValue();
            }
        });
        LiveData<List<FriendShipInfo>> friendLiveData = friendTask.getFriendShipInfoListFromDB(friendIds.toArray(new String[friendIds.size()]));
        dialogDataMutableLiveData.addSource(friendLiveData, new Observer<List<FriendShipInfo>>() {
            @Override
            public void onChanged(List<FriendShipInfo> friendShipInfoList) {
                dialogDataMutableLiveData.removeSource(friendLiveData);
                friendShipInfos = friendShipInfoList;
                taskCount--;
                setDialogDataValue();
            }
        });
    }

    private void setDialogDataValue() {
        if (taskCount == 0) {
            dialogDataMutableLiveData.postValue(new DialogData(groupEntities, friendShipInfos));
        }
    }


    public MediatorLiveData<DialogData> getDialogDataMutableLiveData() {
        return dialogDataMutableLiveData;
    }
}
