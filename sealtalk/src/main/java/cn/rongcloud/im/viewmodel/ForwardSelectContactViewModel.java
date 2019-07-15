package cn.rongcloud.im.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.task.FriendTask;
import cn.rongcloud.im.ui.adapter.models.ListItemModel;

public class ForwardSelectContactViewModel extends CommonListBaseViewModel {

   private FriendTask friendTask;
    public ForwardSelectContactViewModel(@NonNull Application application) {
        super(application);
        friendTask = new FriendTask(application);
    }

    @Override
    public void loadData() {
        LiveData<Resource<List<FriendShipInfo>>> allFriends = friendTask.getAllFriends();
        conversationLiveData.addSource(allFriends, new Observer<Resource<List<FriendShipInfo>>>() {

            @Override
            public void onChanged(Resource<List<FriendShipInfo>> listResource) {
                if (listResource.status != Status.LOADING) {
                    conversationLiveData.removeSource(allFriends);
                    if (listResource.data != null) {
                        final ModelBuilder modelBuilder = builderModel();
                        modelBuilder.addModel(createFunModel("1", getApplication().getString(R.string.seal_select_forward_select_group)));
                        modelBuilder.addFriendList(listResource.data);
                        modelBuilder.buildFirstChar();
                        modelBuilder.post();
                    }
                }
            }
        });
    }


    @Override
    protected ListItemModel createFriendModel(FriendShipInfo info) {
        ListItemModel model = super.createFriendModel(info);
        model.setCheckStatus(ListItemModel.CheckStatus.UNCHECKED);
        return model;
    }

    @Override
    protected ListItemModel createGroupModel(GroupEntity entity) {
        ListItemModel model = super.createGroupModel(entity);
        model.setCheckStatus(ListItemModel.CheckStatus.UNCHECKED);
        return model;
    }

}
