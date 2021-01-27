package cn.rongcloud.im.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.task.FriendTask;
import cn.rongcloud.im.utils.SingleSourceMapLiveData;

public class NewFriendViewModel extends AndroidViewModel {

    private SingleSourceMapLiveData<Resource<List<FriendShipInfo>>, Resource<List<FriendShipInfo>>> friendsAll ;

    private SingleSourceMapLiveData<Resource<Boolean>,Resource<Boolean>> agreeResult;

    private SingleSourceMapLiveData<Resource<Void>,Resource<Void>> ingoreResult;

    private FriendTask friendTask ;
    public NewFriendViewModel(@NonNull Application application) {
        super(application);
        friendTask = new FriendTask(application);
        friendsAll = new SingleSourceMapLiveData<>(new Function<Resource<List<FriendShipInfo>>, Resource<List<FriendShipInfo>>>() {
            @Override
            public Resource<List<FriendShipInfo>> apply(Resource<List<FriendShipInfo>> input) {
                if (input == null || input.data == null) {
                    return null;
                }
                List<FriendShipInfo> tmpList = new ArrayList<>(input.data);
                if (tmpList.size() > 1) {
                    Collections.sort(tmpList, new Comparator<FriendShipInfo>() {

                        @Override
                        public int compare(FriendShipInfo lhs, FriendShipInfo rhs) {
                            Date date1 = lhs.getUpdatedAt();
                            Date date2 = rhs.getUpdatedAt();
                            if (date1 == null || date2 == null || date1 == date2) {
                                return -1;
                            }
                            if (date1.before(date2)) {
                                return 1;
                            }
                            return -1;
                        }
                    });
                }

                return new Resource(input.status,  tmpList, input.code);
            }
        });

        agreeResult = new SingleSourceMapLiveData<>(resource -> {
            if(resource.status == Status.SUCCESS){
                // 成功之后刷新列表
                getFriendsAllData();
            }
            return resource;
        });

        ingoreResult = new SingleSourceMapLiveData<>(resource -> {
            if(resource.status == Status.SUCCESS){
                // 成功之后刷新列表
                getFriendsAllData();
            }
            return resource;
        });

        getFriendsAllData();
    }

    /**
     * 获取好友列表
     */
    private void getFriendsAllData() {
        friendsAll.setSource(friendTask.getAllFriends());
    }


    /**
     * 获取好友列表
     * @return
     */
    public LiveData<Resource<List<FriendShipInfo>>> getFriendsAll() {
        return friendsAll;
    }

    /**
     * 同意添加好友结果
     * @return
     */
    public LiveData<Resource<Boolean>> getAgreeResult() {
        return agreeResult;
    }

    /**
     * 接受好友请求
     * @param friendId
     */
    public void agree(String friendId) {
        agreeResult.setSource(friendTask.agree(friendId));
    }

    /**
     * 忽略好友请求结果
     * @return
     */
    public LiveData<Resource<Void>> getIngoreResult() {
        return ingoreResult;
    }

    /**
     * 忽略好友请求
     * @param friendId
     */
    public void ingore(String friendId) {
        ingoreResult.setSource(friendTask.ingore(friendId));}
}
