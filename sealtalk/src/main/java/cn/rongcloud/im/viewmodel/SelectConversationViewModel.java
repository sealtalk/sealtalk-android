package cn.rongcloud.im.viewmodel;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.ThreadManager;
import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.task.FriendTask;
import cn.rongcloud.im.task.GroupTask;
import cn.rongcloud.im.ui.adapter.models.CheckType;
import cn.rongcloud.im.ui.adapter.models.CheckableContactModel;
import cn.rongcloud.im.ui.adapter.models.ContactModel;
import cn.rongcloud.im.utils.SingleSourceLiveData;
import cn.rongcloud.im.utils.log.SLog;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;

public class SelectConversationViewModel extends AndroidViewModel {

    private static final String TAG = "SelectConversationViewModel";
    protected SingleSourceLiveData<List<CheckableContactModel>> coversationLiveData;
    private MutableLiveData<Integer> selectedCount = new MutableLiveData<>();
    private ArrayList<String> checkedList;
    private ArrayList<String> unCheckedList;
    private RongIMClient rongIMClient;
    private Context mContext;
    private GroupTask groupTask;
    private FriendTask friendTask;

    public SelectConversationViewModel(@NonNull Application application) {
        super(application);
        mContext = application.getApplicationContext();
        rongIMClient = RongIMClient.getInstance();
        groupTask = new GroupTask(application);
        friendTask = new FriendTask(application);
        coversationLiveData = new SingleSourceLiveData<>();
        selectedCount.setValue(0);
    }

    public void loadConversation() {
        RongIMClient.getInstance().getConversationList(new RongIMClient.ResultCallback<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                convert(conversations);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {

            }
        }, Conversation.ConversationType.GROUP, Conversation.ConversationType.PRIVATE);
    }

    /**
     * @param input
     * @return
     */
    private void convert(List<Conversation> input) {
        if (input == null) return;
        SLog.i(TAG, "convert input.size()" + input.size());
        List<CheckableContactModel> output = new ArrayList<>();
        ThreadManager.getInstance().runOnWorkThread(new Runnable() {
            @Override
            public void run() {
                for (Conversation conversation : input) {
                    CheckableContactModel<Conversation> checkableContactModel = null;
                    if (conversation.getConversationType().equals(Conversation.ConversationType.GROUP)) {
                        final GroupEntity groupInfoSync = groupTask.getGroupInfoSync(conversation.getTargetId());
                        if (groupInfoSync != null) {
                            checkableContactModel = new CheckableContactModel(groupInfoSync, R.layout.select_conversation_item);
                        }
                    } else if (conversation.getConversationType() == Conversation.ConversationType.PRIVATE) {
                        final FriendShipInfo friendShipInfo = friendTask.getFriendShipInfoFromDBSync(conversation.getTargetId());
                        if (friendShipInfo != null) {
                            checkableContactModel = new CheckableContactModel(friendShipInfo, R.layout.select_conversation_item);
                        }
                    }
                    if (checkableContactModel != null) {
                        if (unCheckedList != null && unCheckedList.contains(checkableContactModel.getBean().getTargetId())) {
                            checkableContactModel.setCheckType(CheckType.UNCHECKED);
                        }
                        if (checkedList != null && checkedList.contains(checkableContactModel.getBean().getTargetId())) {
                            checkableContactModel.setCheckType(CheckType.CHECKED);
                        }
                        output.add(checkableContactModel);
                    }
                }
                ThreadManager.getInstance().runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        coversationLiveData.setValue(output);
                    }
                });
            }
        });

    }

    /**
     * 点击选取操作
     *
     * @param
     */
    public void onItemClicked(CheckableContactModel model) {
        SLog.i(TAG, "onItemClicked()");
        switch (model.getCheckType()) {
            case CHECKED:
                model.setCheckType(CheckType.NONE);
                break;
            case NONE:
                model.setCheckType(CheckType.CHECKED);
                break;
            default:
                break;
        }
        // 记录选中数
        int size = getCheckedList().size();
        selectedCount.setValue(size);
    }

    public ArrayList<String> getCheckedList() {
        ArrayList<String> strings = new ArrayList<>();
        List<CheckableContactModel> checkableContactModels = coversationLiveData.getValue();
        if (checkableContactModels == null) return strings;
        for (CheckableContactModel model : checkableContactModels) {
            if (model.getCheckType() == CheckType.CHECKED) {
                String id = "";
                if (model.getBean() instanceof GroupEntity) {
                    id = ((GroupEntity) model.getBean()).getId();
                } else if (model.getBean() instanceof FriendShipInfo) {
                    id = ((FriendShipInfo) model.getBean()).getUser().getId();
                }
                strings.add(id);
            }
        }
        return strings;
    }

    public void cancelAllCheck() {
        List<CheckableContactModel> conversationModels = coversationLiveData.getValue();
        for (ContactModel model : conversationModels) {
            CheckableContactModel checkableContactModel = (CheckableContactModel) model;
            checkableContactModel.setCheckType(CheckType.NONE);
        }
        coversationLiveData.setValue(conversationModels);
        selectedCount.setValue(0);
    }

    public void selectAllCheck() {
        List<CheckableContactModel> conversationModels = coversationLiveData.getValue();
        if (conversationModels != null) {
            for (ContactModel model : conversationModels) {
                CheckableContactModel checkableContactModel = (CheckableContactModel) model;
                checkableContactModel.setCheckType(CheckType.CHECKED);
            }
        }
        coversationLiveData.setValue(conversationModels);
        selectedCount.setValue(conversationModels.size());
    }

    /**
     * 获取选择用户的数量
     *
     * @return
     */
    public LiveData<Integer> getSelectedCount() {
        return selectedCount;
    }

    public LiveData<List<CheckableContactModel>> getConersationLiveData() {
        return coversationLiveData;
    }

    public void clearMessage() {
        List<CheckableContactModel> checkableContactModels = coversationLiveData.getValue();
        if (checkableContactModels != null) {
            Iterator<CheckableContactModel> iterator = checkableContactModels.iterator();
            while (iterator.hasNext()) {
                CheckableContactModel model = iterator.next();
                if (model.getCheckType() == CheckType.CHECKED) {
                    String targetId = "";
                    Conversation.ConversationType conversationType = Conversation.ConversationType.NONE;
                    if (model.getBean() instanceof GroupEntity) {
                        targetId = ((GroupEntity) model.getBean()).getId();
                        conversationType = Conversation.ConversationType.GROUP;
                    } else if (model.getBean() instanceof FriendShipInfo) {
                        targetId = ((FriendShipInfo) model.getBean()).getUser().getId();
                        conversationType = Conversation.ConversationType.PRIVATE;
                    }
                    IMManager.getInstance().clearConversationAndMessage(targetId, conversationType);
                    iterator.remove();
                }
            }
            coversationLiveData.setValue(checkableContactModels);
            selectedCount.setValue(0);
        }
    }
}
