package cn.rongcloud.im.ui.test.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MediatorLiveData;

import io.rong.imkit.IMCenter;
import io.rong.imkit.event.uievent.PageEvent;

public class ChatRoomViewModel extends AndroidViewModel {

    private MediatorLiveData<ChatRoomEvent> mChatRoomEventLiveData = new MediatorLiveData<>();

    public ChatRoomViewModel(@NonNull Application application) {
        super(application);
    }

    public MediatorLiveData<ChatRoomEvent> getChatRoomEventLiveData() {
        return mChatRoomEventLiveData;
    }

    public void executeChatRoomEvent(ChatRoomEvent chatRoomEvent) {
        mChatRoomEventLiveData.setValue(chatRoomEvent);
    }
}
