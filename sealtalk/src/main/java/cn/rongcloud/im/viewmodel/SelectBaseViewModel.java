package cn.rongcloud.im.viewmodel;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.task.FriendTask;
import cn.rongcloud.im.ui.adapter.models.CharacterTitleInfo;
import cn.rongcloud.im.ui.adapter.models.CheckType;
import cn.rongcloud.im.ui.adapter.models.CheckableContactModel;
import cn.rongcloud.im.ui.adapter.models.ContactModel;
import cn.rongcloud.im.utils.SingleSourceMapLiveData;
import cn.rongcloud.im.utils.log.SLog;

public class SelectBaseViewModel extends AndroidViewModel {

    private static final String TAG = "SelectBaseViewModel";
    private FriendTask friendTask;
    protected SingleSourceMapLiveData<Resource<List<FriendShipInfo>>, List<ContactModel>> friendsLiveData;
    protected SingleSourceMapLiveData<List<FriendShipInfo>, List<ContactModel>> groupMembersLiveData;
    protected SingleSourceMapLiveData<List<FriendShipInfo>, List<ContactModel>> excludeGroupLiveData;
    private MutableLiveData<List<ContactModel>> currentLiveData;
    private ArrayList<String> uncheckableFriendIdList;
    private ArrayList<String> excludeFriendIdList;
    private ArrayList<String> checkedIdList;
    private ArrayList<String> checkedGroupList;

    public SelectBaseViewModel(@NonNull Application application) {
        super(application);
        friendTask = new FriendTask(application);
        friendsLiveData = new SingleSourceMapLiveData<>(new Function<Resource<List<FriendShipInfo>>, List<ContactModel>>() {
            @Override
            public List<ContactModel> apply(Resource<List<FriendShipInfo>> input) {
                return convert(input.data);
            }
        });

        groupMembersLiveData = new SingleSourceMapLiveData<>(new Function<List<FriendShipInfo>, List<ContactModel>>() {
            @Override
            public List<ContactModel> apply(List<FriendShipInfo> input) {
                return convert(input);
            }
        });


        excludeGroupLiveData = new SingleSourceMapLiveData<>(new Function<List<FriendShipInfo>, List<ContactModel>>() {
            @Override
            public List<ContactModel> apply(List<FriendShipInfo> input) {
                return convert(input);
            }
        });
    }

    public void loadFriendShip(ArrayList<String> uncheckableIdList, ArrayList<String> checkedUsers, ArrayList<String> checkedGroups) {
        uncheckableFriendIdList = uncheckableIdList;
        checkedIdList = checkedUsers;
        checkedGroupList = checkedGroups;
        if (checkedIdList != null && checkedIdList.size() > 0) {
            SLog.i(TAG, "loadFriendShip() checkedUsers.get(0): " + checkedIdList.get(0));
        }
        friendsLiveData.setSource(friendTask.getAllFriends());
        currentLiveData = friendsLiveData;
    }

    /**
     * 排除群组成员的好友列表
     *
     * @param groupId
     */
    public void loadFriendShipExclude(String groupId, ArrayList<String> uncheckableIdList) {
        SLog.i(TAG, "loadFriendShipExclude groupId:" + groupId);
        uncheckableFriendIdList = uncheckableIdList;
        excludeGroupLiveData.setSource(friendTask.getAllFriendsExcludeGroup(groupId));
        currentLiveData = excludeGroupLiveData;
    }

    /**
     * 群组成员列表
     *
     * @param groupId
     */
    public void loadFriendShipInclued(String groupId, ArrayList<String> uncheckableIdList) {
        SLog.i(TAG, "loadFriendShipInclued groupId:" + groupId);
        uncheckableFriendIdList = uncheckableIdList;
        groupMembersLiveData.setSource(friendTask.getAllFriendsIncludeGroup(groupId));
        currentLiveData = groupMembersLiveData;
    }
    /**
     * 群组成员列表
     *
     * @param groupId
     */
    public void loadGroupMemberExclude(String groupId, ArrayList<String> excludeList) {
        SLog.i(TAG, "loadGroupMemberExclude groupId:" + groupId);
        excludeFriendIdList = excludeList;
        groupMembersLiveData.setSource(friendTask.getAllFriendsIncludeGroup(groupId));
        currentLiveData = groupMembersLiveData;
    }


    public LiveData<List<ContactModel>> getFriendShipLiveData() {
        return friendsLiveData;
    }

    public int getIndex(String s) {
        if (currentLiveData.getValue() == null) return 0;
        for (int i = 0; i < currentLiveData.getValue().size(); i++) {
            Object o = currentLiveData.getValue().get(i).getBean();
            if (o instanceof CharacterTitleInfo) {
                CharacterTitleInfo characterParser = (CharacterTitleInfo) o;
                if (characterParser.equals(s)) {
                    return i;
                }
            }
        }
        return 0;
    }

    /**
     * @param input
     * @return
     */
    private List<ContactModel> convert(List<FriendShipInfo> input) {
        if (input == null) return null;
        SLog.i(TAG, "convert input.size()" + input.size());
        List<ContactModel> output = new ArrayList<>();
        ContactModel model = null;
        String temp = "";
        for (FriendShipInfo friendShipInfo : input) {
            if(excludeFriendIdList != null){
                if(excludeFriendIdList.contains(friendShipInfo.getUser().getId())){
                    continue;
                }
            }

            String c = friendShipInfo.getUser().getFirstCharacter();
            if (TextUtils.isEmpty(c)) {
                model = new ContactModel(new CharacterTitleInfo("#"), R.layout.contact_friend_title);
                temp = "#";
                output.add(model);
            } else if (!temp.equals(c)) {
                model = new ContactModel(new CharacterTitleInfo(c), R.layout.contact_friend_title);
                temp = c;
                output.add(model);
            }
            CheckableContactModel<FriendShipInfo> checkableContactModel = new CheckableContactModel(friendShipInfo, R.layout.select_fragment_friend_item);
            if (uncheckableFriendIdList != null && uncheckableFriendIdList.contains(checkableContactModel.getBean().getUser().getId())) {
                checkableContactModel.setCheckType(CheckType.DISABLE);
            }
            SLog.i(TAG, "checkableContactModel.getBean().getUser().getId(): " + checkableContactModel.getBean().getUser().getId());
            if (checkedIdList != null && checkedIdList.contains(checkableContactModel.getBean().getUser().getId())) {
                checkableContactModel.setCheckType(CheckType.CHECKED);
            }
            output.add(checkableContactModel);
        }
        return output;
    }

    /**
     * 点击选取操作
     *
     * @param
     */
    public void onItemClicked(CheckableContactModel model) {
        //继承实现，选人策略
    }

    public ArrayList<String> getCheckedGroupList() {
        return checkedGroupList;
    }

    public ArrayList<String> getCheckedList() {
        ArrayList<String> strings = new ArrayList<>();
        List<ContactModel> contactModels = currentLiveData.getValue();

        if(contactModels == null) return strings;

        for (ContactModel model : contactModels) {
            if (model.getType() == R.layout.select_fragment_friend_item) {
                CheckableContactModel<FriendShipInfo> checkableContactModel = (CheckableContactModel<FriendShipInfo>) model;
                if (checkableContactModel.getCheckType() == CheckType.CHECKED) {
                    strings.add(checkableContactModel.getBean().getUser().getId());
                }
            }
        }
        return strings;
    }

    public void cancelAllCheck() {
        List<ContactModel> ContactModels = currentLiveData.getValue();
        for (ContactModel model : ContactModels) {
            if (model.getType() == R.layout.select_fragment_friend_item) {
                CheckableContactModel<FriendShipInfo> checkableContactModel = (CheckableContactModel<FriendShipInfo>) model;
                checkableContactModel.setCheckType(CheckType.NONE);
            }
        }
    }

    public SingleSourceMapLiveData<List<FriendShipInfo>, List<ContactModel>> getGroupMembersLiveData() {
        return groupMembersLiveData;
    }

    public SingleSourceMapLiveData<List<FriendShipInfo>, List<ContactModel>> getExcludeGroupLiveData() {
        return excludeGroupLiveData;
    }
}
