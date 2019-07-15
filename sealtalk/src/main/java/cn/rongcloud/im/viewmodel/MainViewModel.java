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
import io.rong.imkit.manager.IUnReadMessageObserver;
import io.rong.imlib.model.Conversation;

public class MainViewModel extends AndroidViewModel {
    private Conversation.ConversationType[] conversationTypes;
    private final IMManager imManager;
    private MutableLiveData<Integer> unReadNum = new MutableLiveData<>();
    private LiveData<String> userId = new MutableLiveData<>();
    private MediatorLiveData<FriendShipInfo> privateChatLiveData = new MediatorLiveData<>();
    private FriendTask friendTask;

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

    @Override
    protected void onCleared() {
        super.onCleared();
        imManager.removeUnReadMessageCountChangedObserver(observer);
    }

    IUnReadMessageObserver observer = new IUnReadMessageObserver() {
        @Override
        public void onCountChanged(int i) {
            unReadNum.postValue(i);
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
