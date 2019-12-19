package cn.rongcloud.im.viewmodel;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.db.model.FriendStatus;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.task.FriendTask;
import cn.rongcloud.im.ui.adapter.models.CharacterTitleInfo;
import cn.rongcloud.im.ui.adapter.models.CheckType;
import cn.rongcloud.im.ui.adapter.models.CheckableContactModel;
import cn.rongcloud.im.ui.adapter.models.ContactModel;
import cn.rongcloud.im.utils.CharacterParser;
import cn.rongcloud.im.utils.SingleSourceMapLiveData;
import cn.rongcloud.im.utils.log.SLog;

public class SelectBaseViewModel extends AndroidViewModel {

    private static final String TAG = "SelectBaseViewModel";
    private FriendTask friendTask;
    protected SingleSourceMapLiveData<Resource<List<FriendShipInfo>>, List<ContactModel>> friendsLiveData;
    protected SingleSourceMapLiveData<List<FriendShipInfo>, List<ContactModel>> groupMembersLiveData;
    protected SingleSourceMapLiveData<List<FriendShipInfo>, List<ContactModel>> excludeGroupLiveData;
    private MutableLiveData<Integer> selectedCount = new MutableLiveData<>();
    private MutableLiveData<List<ContactModel>> currentLiveData;
    private ArrayList<String> uncheckableFriendIdList;
    private ArrayList<String> excludeFriendIdList;
    private ArrayList<String> checkedFriendIdList;
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
        checkedFriendIdList = checkedUsers;
        checkedGroupList = checkedGroups;
        if (checkedFriendIdList != null) {
            selectedCount.setValue(checkedFriendIdList.size());
        } else {
            selectedCount.setValue(0);
        }
        friendsLiveData.setSource(friendTask.getAllFriends());
        currentLiveData = friendsLiveData;
    }

    public void loadFriendShip() {
        friendsLiveData.setSource(friendTask.getAllFriends());
        currentLiveData = friendsLiveData;
    }

    /**
     * 搜索关键字
     *
     * @param keyword
     */
    public void searchFriend(String keyword) {
        LiveData<List<FriendShipInfo>> searchDbLiveData = friendTask.searchFriendsFromDB(keyword);
        // 转换数据库搜索结果
        LiveData<Resource<List<FriendShipInfo>>> resourceLiveData = Transformations.switchMap(searchDbLiveData,
                new Function<List<FriendShipInfo>, LiveData<Resource<List<FriendShipInfo>>>>() {
                    @Override
                    public LiveData<Resource<List<FriendShipInfo>>> apply(List<FriendShipInfo> input) {
                        return new MutableLiveData<>(Resource.success(input));
                    }
                });
        friendsLiveData.setSource(resourceLiveData);
    }


    /**
     * 排除群组成员的好友列表
     *
     * @param groupId
     */
    public void loadFriendShipExclude(String groupId, ArrayList<String> uncheckableIdList) {
        SLog.i(TAG, "loadFriendShipExclude groupId:" + groupId);
        uncheckableFriendIdList = uncheckableIdList;
        if (checkedFriendIdList != null) {
            selectedCount.setValue(checkedFriendIdList.size());
        } else {
            selectedCount.setValue(0);
        }
        excludeGroupLiveData.setSource(friendTask.getAllFriendsExcludeGroup(groupId));
        currentLiveData = excludeGroupLiveData;
    }

    /**
     * 群组成员列表
     *
     * @param groupId
     */
    public void loadFriendShipExclude(String groupId) {
        SLog.i(TAG, "loadFriendShipInclude groupId:" + groupId);
        groupMembersLiveData.setSource(friendTask.getAllFriendsExcludeGroup(groupId));
        currentLiveData = groupMembersLiveData;
    }

    /**
     * 搜索群组成员
     *
     * @param groupId
     * @param keyword
     */
    public void searchFriendshipExclude(String groupId, String keyword) {
        groupMembersLiveData.setSource(friendTask.searchFriendsExcludeGroup(groupId, keyword));
    }

    /**
     * 群组成员列表
     *
     * @param groupId
     */
    public void loadGroupMemberExclude(String groupId, ArrayList<String> excludeList, ArrayList<String> includeList) {
        SLog.i(TAG, "loadGroupMemberExclude groupId:" + groupId);
        excludeFriendIdList = excludeList;
        checkedFriendIdList = includeList;
        if (checkedFriendIdList != null) {
            selectedCount.setValue(checkedFriendIdList.size());
        } else {
            selectedCount.setValue(0);
        }
        groupMembersLiveData.setSource(friendTask.getAllFriendsIncludeGroup(groupId));
        currentLiveData = groupMembersLiveData;
    }

    /**
     * 群组成员列表
     *
     * @param groupId
     */
    public void loadGroupMemberExclude(String groupId) {
        SLog.i(TAG, "loadGroupMemberExclude groupId:" + groupId);
        groupMembersLiveData.setSource(friendTask.getAllFriendsIncludeGroup(groupId));
        currentLiveData = groupMembersLiveData;
    }

    /**
     * 搜索群组内成员
     *
     * @param groupId
     * @param keyword
     */
    public void searchGroupMemberExclude(String groupId, String keyword) {
        groupMembersLiveData.setSource(friendTask.searchFriendsIncludeGroup(groupId, keyword));
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
                if (s.equals(characterParser.getCharacter())) {
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
            if (excludeFriendIdList != null) {
                if (excludeFriendIdList.contains(friendShipInfo.getUser().getId())) {
                    continue;
                }
            }
            // 非好友不添加入列表
            if (friendShipInfo.getStatus() != FriendStatus.IS_FRIEND.getStatusCode()) {
                continue;
            }

            String firstChar;
            String groupDisplayName = friendShipInfo.getGroupDisplayName();
            String displayName = friendShipInfo.getDisplayName();
            String nameFirstChar = friendShipInfo.getUser().getFirstCharacter();
            if (!TextUtils.isEmpty(groupDisplayName)) {
                firstChar = CharacterParser.getInstance().getSpelling(groupDisplayName).substring(0, 1).toUpperCase();
            } else if (!TextUtils.isEmpty(displayName)) {
                firstChar = CharacterParser.getInstance().getSpelling(displayName).substring(0, 1).toUpperCase();
            } else {
                firstChar = nameFirstChar;
            }

            if (TextUtils.isEmpty(firstChar)) {
                model = new ContactModel(new CharacterTitleInfo("#"), R.layout.contact_friend_title);
                temp = "#";
                output.add(model);
            } else if (!temp.equals(firstChar)) {
                model = new ContactModel(new CharacterTitleInfo(firstChar), R.layout.contact_friend_title);
                temp = firstChar;
                output.add(model);
            }
            CheckableContactModel<FriendShipInfo> checkableContactModel = new CheckableContactModel(friendShipInfo, R.layout.select_fragment_friend_item);
            if (uncheckableFriendIdList != null && uncheckableFriendIdList.contains(checkableContactModel.getBean().getUser().getId())) {
                checkableContactModel.setCheckType(CheckType.DISABLE);
            }
            SLog.i(TAG, "checkableContactModel.getBean().getUser().getId(): " + checkableContactModel.getBean().getUser().getId());
            if (checkedFriendIdList != null && checkedFriendIdList.contains(checkableContactModel.getBean().getUser().getId())) {
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

        if (contactModels == null) return strings;

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

    /**
     * 获取已选择的好友列表
     *
     * @return
     */
    public ArrayList<String> getCheckedFriendIdList() {
        return checkedFriendIdList;
    }

    public void cancelAllCheck() {
        List<ContactModel> ContactModels = currentLiveData.getValue();
        for (ContactModel model : ContactModels) {
            if (model.getType() == R.layout.select_fragment_friend_item) {
                CheckableContactModel<FriendShipInfo> checkableContactModel = (CheckableContactModel<FriendShipInfo>) model;
                checkableContactModel.setCheckType(CheckType.NONE);
            }
        }
        if (checkedFriendIdList != null) {
            checkedFriendIdList.clear();
        }
    }

    public SingleSourceMapLiveData<List<FriendShipInfo>, List<ContactModel>> getGroupMembersLiveData() {
        return groupMembersLiveData;
    }

    public SingleSourceMapLiveData<List<FriendShipInfo>, List<ContactModel>> getExcludeGroupLiveData() {
        return excludeGroupLiveData;
    }

    /**
     * 添加至已选列表
     */
    public void addToCheckedList(CheckableContactModel contactModel) {
        if (checkedFriendIdList == null) {
            checkedFriendIdList = new ArrayList<>();
        }
        Object bean = contactModel.getBean();
        if (bean instanceof FriendShipInfo) {
            FriendShipInfo friendShipInfo = (FriendShipInfo) bean;
            String id = friendShipInfo.getUser().getId();
            if (!checkedFriendIdList.contains(id)) {
                checkedFriendIdList.add(id);
                selectedCount.setValue(checkedFriendIdList.size());
            }
        }
    }

    /**
     * 在已选列表中移除
     */
    public void removeFromCheckedList(CheckableContactModel contactModel) {
        if (checkedFriendIdList == null) return;

        Object bean = contactModel.getBean();
        if (bean instanceof FriendShipInfo) {
            FriendShipInfo friendShipInfo = (FriendShipInfo) bean;
            String id = friendShipInfo.getUser().getId();
            boolean removed = checkedFriendIdList.remove(id);
            if (removed) {
                selectedCount.setValue(checkedFriendIdList.size());
            }
        }
    }

    /**
     * 获取选择用户的数量
     *
     * @return
     */
    public LiveData<Integer> getSelectedCount() {
        return selectedCount;
    }
}
