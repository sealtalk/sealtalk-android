package cn.rongcloud.im.viewmodel;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.rongcloud.im.R;
import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.task.FriendTask;
import cn.rongcloud.im.task.GroupTask;
import cn.rongcloud.im.ui.adapter.models.CheckType;
import cn.rongcloud.im.ui.adapter.models.SearchFriendModel;
import cn.rongcloud.im.ui.adapter.models.SearchGroupMember;
import cn.rongcloud.im.ui.adapter.models.SearchGroupModel;
import cn.rongcloud.im.ui.adapter.models.SearchModel;
import cn.rongcloud.im.utils.SearchUtils;
import cn.rongcloud.im.utils.SingleSourceMapLiveData;
import cn.rongcloud.im.utils.log.SLog;

public class ForwardSearchViewModel extends AndroidViewModel {
    private static final String TAG = "ForwardSearchViewModel";
    private SingleSourceMapLiveData<List<FriendShipInfo>, List<SearchModel>> friendSearch;
    private SingleSourceMapLiveData<List<SearchGroupMember>, List<SearchModel>> groupSearch;
    private String friendMatch;
    private String groupMatch;
    private FriendTask friendTask;
    private GroupTask groupTask;
    private List<SearchModel> resultAll;
    public MediatorLiveData<List<SearchModel>> searchAll;

    private boolean isSelect = false;

    public ForwardSearchViewModel(boolean isSelect, @NonNull Application application) {
        super(application);
        this.isSelect = isSelect;
        init(application);
    }

    public ForwardSearchViewModel(@NonNull Application application) {
        super(application);
        init(application);
    }

    protected void init(@NonNull Application application) {
        friendTask = new FriendTask(application);
        groupTask = new GroupTask(application);
        friendSearch = new SingleSourceMapLiveData<List<FriendShipInfo>, List<SearchModel>>(new Function<List<FriendShipInfo>, List<SearchModel>>() {

            @Override
            public List<SearchModel> apply(List<FriendShipInfo> input) {
                return convertFriend(input);
            }
        });

        groupSearch = new SingleSourceMapLiveData<List<SearchGroupMember>, List<SearchModel>>(new Function<List<SearchGroupMember>, List<SearchModel>>() {
            @Override
            public List<SearchModel> apply(List<SearchGroupMember> input) {
                return convertGroupSearch(input);
            }
        });
        initSearchAllLiveData();
    }


    /**
     * 查找搜索
     * @param match
     */
    public void search(String match) {
        resultAll = new ArrayList<>();
        searchFriend(match);
        searchGroup(match);
    }

    private void initSearchAllLiveData() {
        searchAll = new MediatorLiveData<List<SearchModel>>();
        searchAll.addSource(friendSearch, new Observer<List<SearchModel>>() {
            @Override
            public void onChanged(List<SearchModel> searchFriendModels) {
                SLog.i(TAG, "searchAll friendSearch size: " + searchFriendModels.size());
                List<SearchModel> samples = new ArrayList<>();
                samples.addAll(searchFriendModels);
                orderData(samples);
                searchAll.setValue(resultAll);
            }
        });

        searchAll.addSource(groupSearch, new Observer<List<SearchModel>>() {
            @Override
            public void onChanged(List<SearchModel> searchGroupModels) {
                SLog.i(TAG, "searchAll groupSearch size: " + searchGroupModels.size());
                List<SearchModel> samples = new ArrayList<>();
                samples.addAll(searchGroupModels);
                orderData(samples);
                searchAll.setValue(resultAll);
            }
        });
    }

    /**
     * 排序结果
     *
     * @param models
     */
    private void orderData(List<SearchModel> models) {
        if (resultAll == null || models.isEmpty())
            return;
        int priorityTarget = models.get(0).getPriority();
        if (resultAll.isEmpty() || priorityTarget > resultAll.get(resultAll.size() - 1).getPriority()) {
            resultAll.addAll(models);
            return;
        }
        for (int i = 0; i < resultAll.size(); i++) {
            if (priorityTarget < resultAll.get(i).getPriority()) {
                resultAll.addAll(i, models);
                break;
            } else if (priorityTarget > resultAll.get(i).getPriority()) {
                continue;
            }
        }
    }



    public void searchFriend(String match) {
        SLog.i(TAG, "searchFriend match: " + match);
        friendMatch = match;
        friendSearch.setSource(friendTask.searchFriendsFromDB(match));
    }

    public void searchGroup(String match) {
        SLog.i(TAG, "searchGroup match: " + match);
        groupMatch = match;
        groupSearch.setSource(groupTask.searchGroup(match));
    }

    private List<SearchModel> convertFriend(List<FriendShipInfo> input) {
        List<SearchModel> output = new ArrayList<>();
        SearchFriendModel searchFriendModel = null;
        for (FriendShipInfo info : input) {
            String aliseName = info.getDisplayName();
            String nickName = info.getUser().getNickname();
            int displayIndex = -1;
            int displayIndexEnd = -1;
            int nickNameIndex = -1;
            int nickNameIndexEnd = -1;
            if (!TextUtils.isEmpty(aliseName)) {
                SearchUtils.Range range = SearchUtils.rangeOfKeyword(aliseName, friendMatch);
                if (range != null) {
                    displayIndex = range.getStart();
                    displayIndexEnd = range.getEnd() + 1;
                }
            }

            if (!TextUtils.isEmpty(nickName)) {

                SearchUtils.Range range = SearchUtils.rangeOfKeyword(nickName, friendMatch);
                if (range != null) {
                    nickNameIndex = range.getStart();
                    nickNameIndexEnd = range.getEnd() + 1;
                }
            }

            searchFriendModel = createFriendGroupModel(info,
                    nickNameIndex, nickNameIndexEnd,
                    displayIndex, displayIndexEnd);
            output.add(searchFriendModel);
        }
        return output;
    }

    private List<SearchModel> convertGroupSearch(List<SearchGroupMember> input) {
        List<SearchModel> output = new ArrayList<>();
        HashMap<GroupEntity, List<SearchGroupModel.GroupMemberMatch>> groupEntityListHashMap = new HashMap<>();
        for (SearchGroupMember info : input) {
            int start = -1;
            int end = -1;
            SearchUtils.Range range = SearchUtils.rangeOfKeyword(info.getNickName(), groupMatch);
            if (range != null) {
                start = range.getStart();
                end = range.getEnd() + 1;
            }
            if (!groupEntityListHashMap.containsKey(info.getGroupEntity())) {
                groupEntityListHashMap.put(info.getGroupEntity(), new ArrayList<>());
            }
            if (start != -1) {
                groupEntityListHashMap.get(info.getGroupEntity()).add(new SearchGroupModel.GroupMemberMatch(info.getNickName(), start, end));
            }
        }

        SearchGroupModel searchGroupModel = null;
        for (Map.Entry<GroupEntity, List<SearchGroupModel.GroupMemberMatch>> entry : groupEntityListHashMap.entrySet()) {
            int start = -1;
            int end = -1;
            SearchUtils.Range range = SearchUtils.rangeOfKeyword(entry.getKey().getName(), groupMatch);
            if (range != null) {
                start = range.getStart();
                end = range.getEnd() + 1;
            }
            searchGroupModel = createSearchGroupModel(entry.getKey(), start, end, entry.getValue());
            output.add(searchGroupModel);
        }
        return output;
    }

    public LiveData<List<SearchModel>> getSearchAll() {
        return searchAll;
    }

    protected SearchGroupModel createSearchGroupModel(GroupEntity entity, int start, int end, List<SearchGroupModel.GroupMemberMatch> matchs) {
        SearchGroupModel searchGroupModel = new SearchGroupModel(entity, R.layout.serach_fragment_forward_recycler_group_item, start, end, matchs);
        searchGroupModel.setId(entity.getId());
        if (isSelect) {
            searchGroupModel.setCheckType(CheckType.UNCHECKED);
        }
        return searchGroupModel;
    }

    protected SearchFriendModel createFriendGroupModel(FriendShipInfo info, int nickNameIndex, int nickNameIndexEnd, int displayIndex, int displayIndexEnd) {
        SearchFriendModel searchFriendModel = new SearchFriendModel(info, R.layout.serach_fragment_forward_recycler_friend_item,
                nickNameIndex, nickNameIndexEnd,
                displayIndex, displayIndexEnd);
        searchFriendModel.setId(info.getUser().getId());
        if (isSelect) {
            searchFriendModel.setCheckType(CheckType.UNCHECKED);
        }
        return searchFriendModel;
    }


    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private boolean isSelect;
        private Application application;

        public Factory(boolean isSelect, Application application) {
            this.isSelect = isSelect;
            this.application = application;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            try {
                return modelClass.getConstructor(boolean.class, Application.class).newInstance(isSelect, application);
            } catch (Exception e) {
                throw new RuntimeException("Cannot create an instance of " + modelClass, e);
            }
        }
    }
}
