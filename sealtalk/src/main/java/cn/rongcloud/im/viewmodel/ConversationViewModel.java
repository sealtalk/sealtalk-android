package cn.rongcloud.im.viewmodel;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.model.EvaluateInfo;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.ScreenCaptureResult;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.model.TypingInfo;
import cn.rongcloud.im.task.FriendTask;
import cn.rongcloud.im.task.GroupTask;
import cn.rongcloud.im.task.PrivacyTask;
import cn.rongcloud.im.task.UserTask;
import io.rong.imkit.userinfo.RongUserInfoManager;
import io.rong.imlib.MessageTag;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.cs.CustomServiceConfig;
import io.rong.imlib.cs.CustomServiceManager;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.UserInfo;
import io.rong.imlib.publicservice.model.PublicServiceProfile;
import io.rong.imlib.typingmessage.TypingStatus;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;

public class ConversationViewModel extends AndroidViewModel {

    private GroupTask groupTask;
    private MutableLiveData<List<EvaluateInfo>> evaluateList = new MutableLiveData<>();
    private MutableLiveData<TypingInfo> typingStatusInfo = new MutableLiveData<>();
    private MediatorLiveData<String> titleStr = new MediatorLiveData<>();

    private IMManager imManager;
    private FriendTask friendTask;
    private LiveData<String> groupAt;
    private PrivacyTask privacyTask;

    public ConversationViewModel(Application application) {
        super(application);
    }

    public ConversationViewModel(String targerId, Conversation.ConversationType conversationType, String title, @NonNull Application application) {
        super(application);
        imManager = IMManager.getInstance();
        friendTask = new FriendTask(application);
        groupTask = new GroupTask(application);
        privacyTask = new PrivacyTask(application);

        /**
         * 设置人工评价监听
         * 当人工评价有标签等配置时，在回调中返回配置
         */
        imManager.setCustomServiceHumanEvaluateListener(new CustomServiceManager.OnHumanEvaluateListener() {
            @Override
            public void onHumanEvaluate(JSONObject jsonObject) {

                List<EvaluateInfo> evaluateInfoList = EvaluateInfo.getEvaluateInfoList(jsonObject);
                evaluateList.postValue(evaluateInfoList);
            }
        });

        /**
         * 正在输入监听
         */
        imManager.setTypingStatusListener(new RongIMClient.TypingStatusListener() {
            @Override
            public void onTypingStatusChanged(Conversation.ConversationType type, String targetId, Collection<TypingStatus> typingStatusSet) {
                TypingInfo info = new TypingInfo();
                info.conversationType = type;
                info.targetId = targetId;
                int count = typingStatusSet.size();
                if (count > 0) {
                    List<TypingInfo.Typing> typingsList = new ArrayList<>();
                    Iterator iterator = typingStatusSet.iterator();
                    while (iterator.hasNext()) {
                        TypingInfo.Typing typing = new TypingInfo.Typing();
                        TypingStatus status = (TypingStatus) iterator.next();
                        String objectName = status.getTypingContentType();
                        MessageTag textTag = TextMessage.class.getAnnotation(MessageTag.class);
                        MessageTag voiceTag = VoiceMessage.class.getAnnotation(MessageTag.class);

                        //匹配对方正在输入的是文本消息还是语音消息
                        if (objectName.equals(textTag.value())) {
                            typing.type = TypingInfo.Typing.Type.text;
                        } else if (objectName.equals(voiceTag.value())) {
                            typing.type = TypingInfo.Typing.Type.voice;
                        }

                        typing.sendTime = status.getSentTime();
                        typing.userId = status.getUserId();
                        typingsList.add(typing);
                    }
                    info.typingList = typingsList;
                }
                typingStatusInfo.postValue(info);
            }
        });

        groupAt = imManager.mentionedInput();
//        getTitleByConversation(targerId, conversationType, title);
    }

    /**
     * 获取 title 根据不同的 type
     *
     * @param targetId
     * @param conversationType
     * @param title
     */
    public void getTitleByConversation(String targetId, Conversation.ConversationType conversationType, String title) {
        if (conversationType == null) {
            return;
        }

        if (conversationType.equals(Conversation.ConversationType.PRIVATE)) {
            if (!TextUtils.isEmpty(targetId)) {
                //从好友 task 中获取
                LiveData<Resource<FriendShipInfo>> friendInfo = friendTask.getFriendInfo(targetId);
                titleStr.addSource(friendInfo, new Observer<Resource<FriendShipInfo>>() {
                    @Override
                    public void onChanged(Resource<FriendShipInfo> friendShipInfoResource) {
                        if (friendShipInfoResource.status != Status.LOADING) {
                            titleStr.removeSource(friendInfo);
                        }
                        String displayName = "";
                        if (friendShipInfoResource != null && friendShipInfoResource.data != null) {
                            displayName = friendShipInfoResource.data.getDisplayName();
                            if (TextUtils.isEmpty(displayName) && friendShipInfoResource.data.getUser() != null) {
                                displayName = friendShipInfoResource.data.getUser().getNickname();
                            }
                        }

                        if (!TextUtils.isEmpty(displayName)) {
                            titleStr.postValue(displayName);
                        } else if (!TextUtils.isEmpty(title)) {
                            titleStr.postValue(title);
                        } else {
                            if (targetId.equals(RongIMClient.getInstance().getCurrentUserId())) {
                                UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(targetId);
                                if (userInfo != null && !TextUtils.isEmpty(userInfo.getName())) {
                                    titleStr.postValue(userInfo.getName());
                                } else {
                                    titleStr.postValue(targetId);
                                }
                            } else {
                                titleStr.postValue(targetId);
                            }
                        }
                    }
                });

            } else {
                titleStr.postValue(title);
            }

        } else if (conversationType.equals(Conversation.ConversationType.GROUP)) {

            LiveData<Resource<GroupEntity>> groupInfo = groupTask.getGroupInfo(targetId);
            titleStr.addSource(groupInfo, new Observer<Resource<GroupEntity>>() {
                @Override
                public void onChanged(Resource<GroupEntity> groupEntityResource) {

                    if (groupEntityResource.status != Status.LOADING) {
                        titleStr.removeSource(groupInfo);
                    }

                    String name = "";
                    if (groupEntityResource != null && groupEntityResource.data != null) {
                        name = groupEntityResource.data.getName() + "(" + groupEntityResource.data.getMemberCount() + ")";
                    }
                    if (!TextUtils.isEmpty(name)) {
                        titleStr.postValue(name);
                    } else if (!TextUtils.isEmpty(title)) {
                        titleStr.postValue(title);
                    } else {
                        titleStr.postValue(targetId);
                    }
                }
            });

        } else if (conversationType.equals(Conversation.ConversationType.APP_PUBLIC_SERVICE)) {
            if (targetId == null) {
                titleStr.postValue(title);
            } else {
                imManager.getPublicServiceProfile(Conversation.PublicServiceType.APP_PUBLIC_SERVICE, targetId, new RongIMClient.ResultCallback<PublicServiceProfile>() {
                    @Override
                    public void onSuccess(PublicServiceProfile publicServiceProfile) {
                        titleStr.postValue(publicServiceProfile.getName());
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode errorCode) {
                        titleStr.postValue("");
                    }
                });
            }

        } else if (conversationType.equals(Conversation.ConversationType.PUBLIC_SERVICE)) {
            if (targetId == null) {
                titleStr.postValue(title);
            } else {
                imManager.getPublicServiceProfile(Conversation.PublicServiceType.PUBLIC_SERVICE, targetId, new RongIMClient.ResultCallback<PublicServiceProfile>() {
                    @Override
                    public void onSuccess(PublicServiceProfile publicServiceProfile) {
                        titleStr.postValue(publicServiceProfile.getName());
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode errorCode) {
                        titleStr.postValue("");
                    }
                });
            }
        } else if (conversationType.equals(Conversation.ConversationType.CHATROOM)) {
            titleStr.postValue(title);
        } else if (conversationType.equals(Conversation.ConversationType.SYSTEM)) {
            titleStr.postValue("");
        } else if (conversationType.equals(Conversation.ConversationType.CUSTOMER_SERVICE)) {
            titleStr.postValue("");
        } else {
            titleStr.postValue("");
        }
    }

    /**
     * 评价信息
     *
     * @return
     */
    public LiveData<List<EvaluateInfo>> getEvaluateList() {
        return evaluateList;
    }


    /**
     * 获取正在输入的状态信息
     *
     * @return
     */
    public LiveData<TypingInfo> getTypingStatusInfo() {
        return typingStatusInfo;
    }

    /**
     * 提交评价
     *
     * @param targetId
     * @param stars
     * @param seletedLables
     * @param resolveStatus
     * @param suggestion
     * @param dialogId
     */
    public void submitEvaluate(String targetId, int stars, String seletedLables, CustomServiceConfig.CSEvaSolveStatus resolveStatus, String suggestion, String dialogId) {
        imManager.evaluateCustomService(targetId, stars, resolveStatus, seletedLables, suggestion, dialogId);
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        imManager.setCustomServiceHumanEvaluateListener(null);
        imManager.setTypingStatusListener(null);
    }


    /**
     * 会话标题
     *
     * @return
     */
    public LiveData<String> getTitleStr() {
        return titleStr;
    }

    /**
     * 群 @
     *
     * @return
     */
    public LiveData<String> getGroupAt() {
        return groupAt;
    }


    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private String targetId;
        private String title;
        private Conversation.ConversationType conversationType;
        private Application application;

        public Factory(String targetId, Conversation.ConversationType conversationType, String title, Application application) {
            this.conversationType = conversationType;
            this.targetId = targetId;
            this.title = title;
            this.application = application;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            try {
                return modelClass.getConstructor(String.class, Conversation.ConversationType.class, String.class, Application.class).newInstance(targetId, conversationType, title, application);
            } catch (Exception e) {
                throw new RuntimeException("Cannot create an instance of " + modelClass, e);
            }
        }
    }

    /**
     * 获取是否开启截屏通知
     */
    public LiveData<Resource<ScreenCaptureResult>> getScreenCaptureStatus(int conversationType, String targetId) {
        return privacyTask.getScreenCapture(conversationType, targetId);
    }

    /**
     * 发送截屏通知
     *
     * @param conversationType
     * @param targetId
     */
    public LiveData<Resource<Void>> sendScreenShotMsg(int conversationType, String targetId) {
        return privacyTask.sendScreenShotMessage(conversationType, targetId);
    }

}
