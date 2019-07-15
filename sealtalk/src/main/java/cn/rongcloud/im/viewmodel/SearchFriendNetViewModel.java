package cn.rongcloud.im.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.db.model.FriendStatus;
import cn.rongcloud.im.model.AddFriendResult;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.SearchFriendInfo;
import cn.rongcloud.im.task.FriendTask;
import cn.rongcloud.im.utils.SingleSourceLiveData;
import cn.rongcloud.im.utils.SingleSourceMapLiveData;
import cn.rongcloud.im.utils.log.SLog;


public class SearchFriendNetViewModel extends AndroidViewModel {
    private static final String TAG = "SearchFriendNetViewModel";
    private FriendTask friendTask;
    private SingleSourceLiveData<Resource<SearchFriendInfo>> searchFriend;
    private SingleSourceMapLiveData<FriendShipInfo, Boolean> isFriend;
    private SingleSourceLiveData<Resource<AddFriendResult>> addFriend;

    public SearchFriendNetViewModel(@NonNull Application application) {
        super(application);
        friendTask = new FriendTask(application);
        searchFriend = new SingleSourceLiveData<>();
        addFriend = new SingleSourceLiveData<>();
        isFriend = new SingleSourceMapLiveData<FriendShipInfo, Boolean>(new Function<FriendShipInfo, Boolean>() {
            @Override
            public Boolean apply(FriendShipInfo input) {
                if(input != null){
                    return FriendStatus.getStatus(input.getStatus()) == FriendStatus.IS_FRIEND;
                } else {
                    return false;
                }
            }
        });
    }

    public void searchFriendFromServer(String region, String phone) {
        SLog.i(TAG, "searchFriendFromServer region: " + region + " phoneSearchFr: " + phone);
        searchFriend.setSource(friendTask.searchFriendFromServer(region, phone));
    }

    public LiveData<Resource<SearchFriendInfo>> getSearchFriend() {
        return searchFriend;
    }

    public LiveData<Boolean> getIsFriend() {
        return isFriend;
    }

    public void isFriend(String userId) {
        isFriend.setSource(friendTask.getFriendShipInfoFromDB(userId));
    }

    public void inviteFriend(String userId, String message) {
        addFriend.setSource(friendTask.inviteFriend(userId, message));
    }

    public LiveData<Resource<AddFriendResult>> getAddFriend() {
        return addFriend;
    }
}
