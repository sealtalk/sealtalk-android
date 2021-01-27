package cn.rongcloud.im.viewmodel;

import android.app.Activity;
import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.common.BatchForwardHelper;
import cn.rongcloud.im.common.ErrorCode;
import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.model.Resource;
import io.rong.contactcard.message.ContactMessage;
import io.rong.imkit.feature.forward.ForwardManager;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UserInfo;
import io.rong.imlib.location.message.LocationMessage;

public class ForwardActivityViewModel extends AndroidViewModel {
    private MutableLiveData<Boolean> isSingleLiveData;
    private MutableLiveData<Resource> forwardSuccessLiveData;

    public ForwardActivityViewModel(@NonNull Application application) {
        super(application);
        isSingleLiveData = new MutableLiveData<>();
        forwardSuccessLiveData = new MutableLiveData<>();
    }

    public void setIsSinglePick(boolean isSingle) {
        isSingleLiveData.postValue(isSingle);
    }

    public void switchMutiSingle() {
        isSingleLiveData.postValue(!isSingleLiveData.getValue());
    }

    public LiveData<Boolean> getIsSingleLiveData() {
        return isSingleLiveData;
    }

    public void ForwardMessage(Activity activity, List<GroupEntity> groupEntityList, List<FriendShipInfo> friendShipInfoList, List<Message> messageList) {
        ForwardMessage(activity, groupEntityList, friendShipInfoList, messageList, true);
    }

    public void ForwardMessage(Activity activity, List<GroupEntity> groupEntityList, List<FriendShipInfo> friendShipInfoList, List<Message> messageList, boolean useSDKForward) {
        ArrayList<Conversation> conversationList = new ArrayList<>();
        if (groupEntityList != null) {
            for (GroupEntity groupEntity : groupEntityList) {
                if (useSDKForward) {
                    conversationList.add(Conversation.obtain(Conversation.ConversationType.GROUP, groupEntity.getId(), ""));
                } else {
                    forwardMessage(Conversation.ConversationType.GROUP, groupEntity.getId(), messageList);
                }
            }

        }
        if (friendShipInfoList != null) {
            for (FriendShipInfo friendShipInfo : friendShipInfoList) {
                if (useSDKForward) {
                    conversationList.add(Conversation.obtain(Conversation.ConversationType.PRIVATE, friendShipInfo.getUser().getId(), ""));
                } else {
                    forwardMessage(Conversation.ConversationType.PRIVATE, friendShipInfo.getUser().getId(), messageList);
                }
            }
        }
        if (conversationList.size() > 0) {
            ForwardManager.setForwardMessageResult(activity, conversationList);
        }
    }

    /**
     * 实际转发消息
     *
     * @param conversationType
     * @param targetId
     */
    private void forwardMessage(Conversation.ConversationType conversationType, String targetId, List<Message> messageList) {
        for (Message fwdMessage : messageList) {
            MessageContent messageContent = fwdMessage.getContent();
            if (messageContent != null) {
                messageContent.setUserInfo(null);
            }
            if (messageContent instanceof ContactMessage) {
                String portraitUrl = ((ContactMessage) messageContent).getImgUrl();
                if (TextUtils.isEmpty(portraitUrl) || portraitUrl.toLowerCase().startsWith("file://")) {
                    portraitUrl = null;
                }
                String sendContactMsgUserName = "";
                UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(RongIMClient.getInstance().getCurrentUserId());
                if (userInfo != null) {
                    sendContactMsgUserName = userInfo.getName();
                }
                ContactMessage contactMessage = ContactMessage.obtain(((ContactMessage) messageContent).getId(),
                        ((ContactMessage) messageContent).getName(), portraitUrl,
                        RongIMClient.getInstance().getCurrentUserId(), sendContactMsgUserName, null);
                Message message = Message.obtain(targetId, conversationType, contactMessage);
                sendMessage(message);

            } else if (messageContent instanceof LocationMessage) {//判断是否是定位消息
                Message message = Message.obtain(targetId, conversationType, messageContent);
                sendMessage(message);
            } else {
                Message message = Message.obtain(targetId, conversationType, messageContent);
                sendMessage(message);

            }
        }
    }

    /**
     * 这里需要延迟 300ms 来发送，防止消息阻塞
     *
     * @param message
     */
    private void sendMessage(Message message) {
        BatchForwardHelper.getInstance().batchSendMessage(message, callback);

    }

    public MutableLiveData<Resource> getForwardSuccessLiveData() {
        return forwardSuccessLiveData;
    }

    private IRongCallback.ISendMediaMessageCallback callback = new IRongCallback.ISendMediaMessageCallback() {
        @Override
        public void onProgress(Message message, int i) {

        }

        @Override
        public void onCanceled(Message message) {

        }

        @Override
        public void onAttached(Message message) {

        }

        @Override
        public void onSuccess(Message message) {
            forwardSuccessLiveData.postValue(Resource.success(null));
        }

        @Override
        public void onError(Message message, RongIMClient.ErrorCode errorCode) {
            if (errorCode == RongIMClient.ErrorCode.RC_NET_UNAVAILABLE || errorCode == RongIMClient.ErrorCode.RC_NET_CHANNEL_INVALID) {
                forwardSuccessLiveData.postValue(Resource.error(ErrorCode.NETWORK_ERROR.getCode(), null));
            } else {
                forwardSuccessLiveData.postValue(Resource.error(ErrorCode.UNKNOWN_ERROR.getCode(), null));
            }
        }
    };
}
