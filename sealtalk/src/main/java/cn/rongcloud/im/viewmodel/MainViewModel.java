package cn.rongcloud.im.viewmodel;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.task.FriendTask;
import io.rong.imkit.manager.UnReadMessageManager;
import io.rong.imlib.model.Conversation;

public class MainViewModel extends AndroidViewModel {
    private Conversation.ConversationType[] conversationTypes;
    private final IMManager imManager;
    private MutableLiveData<Integer> unReadNum = new MutableLiveData<>();
    private MutableLiveData<Integer> newFriendNum = new MutableLiveData<>();
    private LiveData<String> userId = new MutableLiveData<>();
    private MediatorLiveData<FriendShipInfo> privateChatLiveData = new MediatorLiveData<>();
    private FriendTask friendTask;
    /**
     * 群通知数
     */
    private int groupNotifyNum;
    /**
     * 未读消息数
     */
    private int unreadMessageNum;

    public MainViewModel(@NonNull Application application) {
        super(application);
        imManager = IMManager.getInstance();
        friendTask = new FriendTask(application);

        conversationTypes = new Conversation.ConversationType[]{
                Conversation.ConversationType.PRIVATE,
                Conversation.ConversationType.GROUP, Conversation.ConversationType.SYSTEM,
                Conversation.ConversationType.PUBLIC_SERVICE, Conversation.ConversationType.APP_PUBLIC_SERVICE
        };

        imManager.addUnReadMessageCountChangedObserver(observer, conversationTypes);


    }


    /**
     * 未读消息数
     *
     * @return
     */
    public LiveData<Integer> getUnReadNum() {
        return unReadNum;
    }

    /**
     * 更新群通知未读消息的数量
     *
     * @param num
     */
    public void setGroupNotifyUnReadNum(int num) {
        groupNotifyNum = num;
        unReadNum.postValue(unreadMessageNum + groupNotifyNum);
    }

    /**
     * 新朋友数量
     *
     * @return
     */
    public LiveData<Integer> getNewFriendNum() {
        return newFriendNum;
    }

    /**
     * 修改新朋友的数量
     *
     * @param count
     */
    public void setNewFriendNum(int count) {
        newFriendNum.postValue(count);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        imManager.removeUnReadMessageCountChangedObserver(observer);
    }

    UnReadMessageManager.IUnReadMessageObserver observer = new UnReadMessageManager.IUnReadMessageObserver() {
        @Override
        public void onCountChanged(int i) {
            unreadMessageNum = i;
            unReadNum.postValue(unreadMessageNum + groupNotifyNum);
        }
    };

    /**
     * 清理消息未读状态
     */
    public void clearMessageUnreadStatus() {
        imManager.clearMessageUnreadStatus(conversationTypes);
    }

    public void startPrivateChat(String userId) {
        if (TextUtils.isEmpty(userId)) return;
        LiveData<FriendShipInfo> friendShipInfoLiveData = friendTask.getFriendShipInfoFromDB(userId);
        privateChatLiveData.addSource(friendShipInfoLiveData, new Observer<FriendShipInfo>() {
            @Override
            public void onChanged(FriendShipInfo friendShipInfo) {
                privateChatLiveData.removeSource(friendShipInfoLiveData);
                privateChatLiveData.setValue(friendShipInfo);
            }
        });
    }

    public MediatorLiveData<FriendShipInfo> getPrivateChatLiveData() {
        return privateChatLiveData;
    }

}
