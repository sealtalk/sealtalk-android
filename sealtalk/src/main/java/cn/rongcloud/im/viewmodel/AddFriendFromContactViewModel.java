package cn.rongcloud.im.viewmodel;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;

import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.model.PhoneContactInfo;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.task.FriendTask;
import cn.rongcloud.im.ui.adapter.models.ListItemModel;
import cn.rongcloud.im.ui.adapter.viewholders.AddFriendFromContactItemViewHolder;
import cn.rongcloud.im.utils.CharacterParser;

/**
 * 添加好友视图模型
 */
public class AddFriendFromContactViewModel extends CommonListBaseViewModel {
    private FriendTask friendTask;
    private ModelBuilder allContactBuilder;
    private MutableLiveData<String> keywordLiveData = new MutableLiveData<>();

    public AddFriendFromContactViewModel(@NonNull Application application) {
        super(application);
        friendTask = new FriendTask(application);

    }

    @Override
    public void loadData() {
        conversationLiveData.addSource(friendTask.getPhoneContactInfo(), new Observer<Resource<List<PhoneContactInfo>>>() {
            @Override
            public void onChanged(Resource<List<PhoneContactInfo>> listResource) {
                if (listResource.data != null) {
                    allContactBuilder = builderModel();
                    for (PhoneContactInfo info : listResource.data) {
                        allContactBuilder.addModel(createPhoneContact(info));
                    }
                    allContactBuilder.buildFirstChar();
                    allContactBuilder.post();
                }
            }
        });

        // 搜索结果
        LiveData<List<PhoneContactInfo>> searchResult = Transformations.switchMap(keywordLiveData, keyword -> friendTask.searchPhoneContactInfo(keyword));
        conversationLiveData.addSource(searchResult, new Observer<List<PhoneContactInfo>>() {
            @Override
            public void onChanged(List<PhoneContactInfo> phoneContactInfos) {
                ModelBuilder searchModelBuilder = builderModel();
                if(phoneContactInfos != null) {
                    for (PhoneContactInfo info : phoneContactInfos) {
                        searchModelBuilder.addModel(createPhoneContact(info));
                    }
                }
                searchModelBuilder.buildFirstChar();
                searchModelBuilder.post();
            }
        });
    }

    /**
     * 创建联系人对象.
     *
     * @param info
     * @return
     */
    protected ListItemModel createPhoneContact(PhoneContactInfo info) {
        String name = info.getContactName();
        ListItemModel.ItemView itemView = new ListItemModel
                .ItemView(R.layout.add_friend_item_from_contact, ListItemModel.ItemView.Type.FRIEND, AddFriendFromContactItemViewHolder.class);
        ListItemModel<PhoneContactInfo> model = new ListItemModel<>(info.getUserId(), name, info, itemView);
        model.setPortraitUrl(info.getPortraitUrl());
        model.setFirstChar(CharacterParser.getInstance().getSpelling(name).toUpperCase());
        return model;
    }

    /**
     * 搜索关键字，针对通讯录名称和 SealTalk 号
     * @param keyword
     */
    public void search(String keyword) {
        if (TextUtils.isEmpty(keyword)) {
            allContactBuilder.post();
        } else {
            keywordLiveData.postValue(keyword);
        }
    }


}
