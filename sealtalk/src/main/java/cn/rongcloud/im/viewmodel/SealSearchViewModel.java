package cn.rongcloud.im.viewmodel;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.ThreadManager;
import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.db.model.UserInfo;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.task.FriendTask;
import cn.rongcloud.im.task.GroupTask;
import cn.rongcloud.im.task.UserTask;
import cn.rongcloud.im.ui.adapter.models.SearchConversationModel;
import cn.rongcloud.im.ui.adapter.models.SearchDivModel;
import cn.rongcloud.im.ui.adapter.models.SearchFriendModel;
import cn.rongcloud.im.ui.adapter.models.SearchGroupMember;
import cn.rongcloud.im.ui.adapter.models.SearchGroupModel;
import cn.rongcloud.im.ui.adapter.models.SearchModel;
import cn.rongcloud.im.ui.adapter.models.SearchShowMorModel;
import cn.rongcloud.im.ui.adapter.models.SearchTitleModel;
import cn.rongcloud.im.utils.SearchUtils;
import cn.rongcloud.im.utils.SingleSourceLiveData;
import cn.rongcloud.im.utils.SingleSourceMapLiveData;
import cn.rongcloud.im.utils.log.SLog;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.SearchConversationResult;

public class SealSearchViewModel extends AndroidViewModel {
    private static final String TAG = "SealSearchViewModel";
    private SingleSourceMapLiveData<List<FriendShipInfo>, List<SearchModel>> friendSearch;
    private SingleSourceMapLiveData<List<SearchGroupMember>, List<SearchModel>> groupSearch;
    private SingleSourceMapLiveData<List<GroupEntity>, List<SearchModel>> groupSearhByName;
    private SingleSourceMapLiveData<List<GroupEntity>, List<SearchModel>> groupContactSearhByName;
    private SingleSourceLiveData<Resource<List<GroupEntity>>> groupContactList = new SingleSourceLiveData<>();
    private MutableLiveData<List<SearchModel>> conversationSearch;
    private MutableLiveData<List<SearchModel>> messageSearch;
    private MediatorLiveData searchAll;
    private FriendTask friendTask;
    private GroupTask groupTask;
    private UserTask userTask;
    private String friendMatch;
    private String groupMatch;
    private String groupMatchbyName;
    private String groupContactMatchByName;
    private String conversationMatch;
    private List<SearchModel> resultAll;

    public SealSearchViewModel(@NonNull Application application) {
        super(application);
        friendTask = new FriendTask(application);
        groupTask = new GroupTask(application);
        userTask = new UserTask(application);

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

        groupSearhByName = new SingleSourceMapLiveData<>(new Function<List<GroupEntity>, List<SearchModel>>() {
            @Override
            public List<SearchModel> apply(List<GroupEntity> input) {
                return convertGroupSearchByName(input);
            }
        });

        groupContactSearhByName = new SingleSourceMapLiveData<>(new Function<List<GroupEntity>, List<SearchModel>>() {
            @Override
            public List<SearchModel> apply(List<GroupEntity> input) {
                return convertGroupSearchByName(input);
            }
        });
        conversationSearch = new MutableLiveData<>();
        messageSearch = new MutableLiveData<>();
        initSearchAllLiveData();
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

    public void searchGroupByName(String match) {
        SLog.i(TAG, "searchGroupContactByName match: " + match);
        groupContactMatchByName = match;
        groupContactSearhByName.setSource(groupTask.searchGroupByName(match));
    }

    /**
     * 搜索会话
     *
     * @param match
     */
    public void searchConversation(String match) {
        SLog.i(TAG, "searchConversation match: " + match);
        conversationMatch = match;
        RongIMClient.getInstance().searchConversations(match,
                new Conversation.ConversationType[]{Conversation.ConversationType.PRIVATE, Conversation.ConversationType.GROUP},
                new String[]{"RC:TxtMsg", "RC:ImgTextMsg", "RC:FileMsg", "RC:ReferenceMsg"}, new RongIMClient.ResultCallback<List<SearchConversationResult>>() {

                    @Override
                    public void onSuccess(List<SearchConversationResult> searchConversationResults) {
                        ThreadManager.getInstance().runOnWorkThread(new Runnable() {
                            @Override
                            public void run() {
                                convertConversationAndSetValue(searchConversationResults);
                            }
                        });
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode errorCode) {
                        SLog.i(TAG, "searchConversations errorCode: " + errorCode);
                    }
                });
    }


    public void searchMessage(String targetId, Conversation.ConversationType conversationType, String name, String portrait, String match) {
        searchIMClientMessage(targetId, conversationType, name, portrait, match);
    }

    private void searchIMClientMessage(String targetId, Conversation.ConversationType conversationType, String name, String portrait, String match) {
        SLog.i(TAG, "searchIMClientMessage() match = " + match);
        RongIMClient.getInstance().searchMessages(conversationType,
                targetId, match, 50, 0, new RongIMClient.ResultCallback<List<Message>>() {

                    @Override
                    public void onSuccess(List<Message> messages) {
                        SLog.i(TAG, "searchIMClientMessage()  onSuccess size = " + messages.size());
                        List<SearchModel> result = new ArrayList<>();
                        SearchMessageModel searchMessageModel = null;
                        for (Message message : messages) {
                            searchMessageModel = new SearchMessageModel(message, R.layout.search_fragment_recycler_chatting_records_list, name, portrait, match);
                            result.add(searchMessageModel);
                        }
                        messageSearch.postValue(result);
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode errorCode) {

                    }
                });
    }


    public void searchAll(String math) {
        resultAll = new ArrayList<>();

        searchFriend(math);
        searchGroup(math);
        searchConversation(math);
    }

    /**
     * 搜索转发
     *
     * @param math
     */
    public void searchForward(String math) {
        resultAll = new ArrayList<>();
        searchFriend(math);
        searchGroup(math);
    }

    private void initSearchAllLiveData() {
        searchAll = new MediatorLiveData<List<SearchModel>>();
        searchAll.addSource(friendSearch, new Observer<List<SearchModel>>() {
            @Override
            public void onChanged(List<SearchModel> searchFriendModels) {
                SLog.i(TAG, "searchAll friendSearch size: " + searchFriendModels.size());
                if (searchFriendModels.size() == 1) { //只有个标题
                    handleTasksDown();
                    return;
                }

                List<SearchModel> samples = new ArrayList<>();
                if (searchFriendModels.size() > 4) {
                    samples.addAll(searchFriendModels.subList(0, 3));
                    samples.add(new SearchShowMorModel(R.string.seal_search_more_friend, R.layout.search_frament_show_more_item, SearchModel.SHOW_PRIORITY_FRIEND));
                } else if (searchFriendModels.size() > 1) { // 2~4 之间
                    samples.addAll(searchFriendModels);
                }
                samples.add(new SearchDivModel(null, R.layout.search_fragment_recycler_div_layout, SearchModel.SHOW_PRIORITY_FRIEND));
                orderData(samples);
                handleTasksDown();
            }
        });

        searchAll.addSource(groupSearch, new Observer<List<SearchModel>>() {
            @Override
            public void onChanged(List<SearchModel> searchGroupModels) {
                SLog.i(TAG, "searchAll groupSearch size: " + searchGroupModels.size());

                if (searchGroupModels.size() == 1) { //只有个标题
                    handleTasksDown();
                    return;
                }
                List<SearchModel> samples = new ArrayList<>();
                if (searchGroupModels.size() > 4) {
                    samples.addAll(searchGroupModels.subList(0, 3));
                    samples.add(new SearchShowMorModel(R.string.seal_search_more_group, R.layout.search_frament_show_more_item, SearchModel.SHOW_PRIORITY_GROUP));
                } else if (searchGroupModels.size() > 1) {
                    samples.addAll(searchGroupModels);
                }
                samples.add(new SearchDivModel(null, R.layout.search_fragment_recycler_div_layout, SearchModel.SHOW_PRIORITY_GROUP));
                orderData(samples);
                handleTasksDown();
            }
        });

        searchAll.addSource(conversationSearch, new Observer<List<SearchModel>>() {
            @Override
            public void onChanged(List<SearchModel> searchConversationModels) {
                SLog.i(TAG, "searchAll conversationSearch size: " + searchConversationModels.size());
                if (searchConversationModels.size() == 1) { //只有个标题
                    handleTasksDown();
                    return;
                }
                List<SearchModel> samples = new ArrayList<>();
                if (searchConversationModels.size() > 4) {
                    samples = new ArrayList<>();
                    samples.addAll(searchConversationModels.subList(0, 3));
                    samples.add(new SearchShowMorModel(R.string.seal_search_more_chatting_records, R.layout.search_frament_show_more_item, SearchModel.SHOW_PRIORITY_CONVERSATION));
                } else if (searchConversationModels.size() > 1) {
                    samples.addAll(searchConversationModels);
                }
                samples.add(new SearchDivModel(null, R.layout.search_fragment_recycler_div_layout, SearchModel.SHOW_PRIORITY_CONVERSATION));
                orderData(samples);
                handleTasksDown();
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
        List<SearchModel> removeList = new ArrayList<>();
        for (SearchModel model : resultAll) {
            if (model.getPriority() == priorityTarget) {
                removeList.add(model);
            }
        }
        resultAll.removeAll(removeList);

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

    /**
     * 检测是否完成并设置结果
     */
    private void handleTasksDown() {
        searchAll.setValue(resultAll);
    }

    private List<SearchModel> convertFriend(List<FriendShipInfo> input) {
        SLog.i(TAG, "convertFriend input.size = " + input.size());
        List<SearchModel> output = new ArrayList<>();
        SearchFriendModel searchFriendModel = null;
        output.add(new SearchTitleModel(R.string.seal_ac_search_friend, R.layout.search_fragment_recycler_title_layout, SearchModel.SHOW_PRIORITY_FRIEND));
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

            searchFriendModel = new SearchFriendModel(info, R.layout.serach_fragment_recycler_friend_item,
                    nickNameIndex, nickNameIndexEnd,
                    displayIndex, displayIndexEnd);
            output.add(searchFriendModel);
        }
        return output;
    }

    private void convertConversationAndSetValue(List<SearchConversationResult> searchConversationResults) {
        List<SearchModel> output = new ArrayList<>();
        output.add(new SearchTitleModel(R.string.seal_ac_search_chatting_records, R.layout.search_fragment_recycler_title_layout, SearchModel.SHOW_PRIORITY_CONVERSATION));
        SearchConversationModel searchConversationModel = null;
        for (int i = 0; i < searchConversationResults.size(); i++) {
            SearchConversationResult result = searchConversationResults.get(i);
            String name = ""; // 如下是在分情况查找name
            String portraitUrl = "";
            if (result.getConversation().getConversationType() == Conversation.ConversationType.PRIVATE) {
                String targetId = result.getConversation().getTargetId();
                if (IMManager.getInstance().getCurrentId().equals(targetId)) {
                    UserInfo userInfo = userTask.getUserInfoSync(targetId);
                    if (userInfo != null) {
                        name = userInfo.getName();
                        portraitUrl = userInfo.getPortraitUri();
                    }
                } else {
                    FriendShipInfo info = friendTask.getFriendShipInfoFromDBSync(result.getConversation().getTargetId());
                    if (info != null) {
                        name = info.getDisplayName() == null ? info.getDisplayName() : info.getUser().getNickname();
                        portraitUrl = info.getUser().getPortraitUri();
                    }
                }
            } else if (result.getConversation().getConversationType() == Conversation.ConversationType.GROUP) {
                GroupEntity groupEntity = groupTask.getGroupInfoSync(result.getConversation().getTargetId());
                if (groupEntity != null) {
                    name = groupEntity.getName();
                    portraitUrl = groupEntity.getPortraitUri();
                }
            }
            searchConversationModel = new SearchConversationModel(result, R.layout.serach_fragment_recycler_conversation_item, conversationMatch, name, portraitUrl);
            output.add(searchConversationModel);
        }
        conversationSearch.postValue(output);
    }

    private List<SearchModel> convertGroupSearch(List<SearchGroupMember> input) {
        List<SearchModel> output = new ArrayList<>();
        output.add(new SearchTitleModel(R.string.seal_ac_search_group, R.layout.search_fragment_recycler_title_layout, SearchModel.SHOW_PRIORITY_GROUP));

        HashMap<String, List<SearchGroupModel.GroupMemberMatch>> groupEntityListHashMap = new HashMap<>();
        HashMap<String, GroupEntity> entityHashMap = new HashMap<>();
        for (SearchGroupMember info : input) {
            int start = -1;
            int end = -1;
            SearchUtils.Range range = SearchUtils.rangeOfKeyword(info.getNickName(), groupMatch);
            if (range != null) {
                start = range.getStart();
                end = range.getEnd() + 1;
            }
            String groupId = info.getGroupEntity().getId();
            if (!groupEntityListHashMap.containsKey(groupId)) {
                groupEntityListHashMap.put(groupId, new ArrayList<>());
                entityHashMap.put(groupId, info.getGroupEntity());
            }
            if (start != -1) {
                groupEntityListHashMap.get(groupId).add(new SearchGroupModel.GroupMemberMatch(info.getNickName(), start, end));
            }
        }

        SearchGroupModel searchGroupModel = null;
        for (Map.Entry<String, List<SearchGroupModel.GroupMemberMatch>> entry : groupEntityListHashMap.entrySet()) {
            int start = -1;
            int end = -1;
            GroupEntity entity = entityHashMap.get(entry.getKey());
            SearchUtils.Range range = SearchUtils.rangeOfKeyword(entity.getName(), groupMatch);
            if (range != null) {
                start = range.getStart();
                end = range.getEnd() + 1;
            }
            searchGroupModel = new SearchGroupModel(entity, R.layout.serach_fragment_recycler_group_item, start, end, entry.getValue());
            output.add(searchGroupModel);
        }
        return output;
    }

    private List<SearchModel> convertGroupSearchByName(List<GroupEntity> input) {
        List<SearchModel> output = new ArrayList<>();
        SearchGroupModel model = null;
        for (GroupEntity info : input) {
            int start = -1;
            int end = -1;
            SearchUtils.Range range = SearchUtils.rangeOfKeyword(info.getName(), groupMatchbyName);
            if (range != null) {
                start = range.getStart();
                end = range.getEnd() + 1;
            }
            model = new SearchGroupModel(info, R.layout.serach_fragment_recycler_group_item, start, end, null);
            output.add(model);
        }
        return output;
    }

    public LiveData<List<SearchModel>> getSearchFriend() {
        return friendSearch;
    }

    public LiveData<List<SearchModel>> getGroupSearch() {
        return groupSearch;
    }

    public LiveData<List<SearchModel>> getGroupSearhByName() {
        return groupSearhByName;
    }

    public LiveData<List<SearchModel>> getGroupContactSearhByName() {
        return groupContactSearhByName;
    }

    public LiveData<List<SearchModel>> getConversationSearch() {
        return conversationSearch;
    }

    public LiveData<List<SearchModel>> getSearchAll() {
        return searchAll;
    }

    public MutableLiveData<List<SearchModel>> getMessageSearch() {
        return messageSearch;
    }

    public void requestGroupContactList() {
        groupContactList.setSource(userTask.getContactGroupList());
    }

    public LiveData<Resource<List<GroupEntity>>> getGroupContactList() {
        return groupContactList;
    }
}
