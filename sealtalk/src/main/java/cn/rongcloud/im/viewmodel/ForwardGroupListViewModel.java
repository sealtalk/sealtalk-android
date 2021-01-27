package cn.rongcloud.im.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.task.GroupTask;
import cn.rongcloud.im.ui.adapter.models.ListItemModel;

public class ForwardGroupListViewModel extends CommonListBaseViewModel {
    private GroupTask groupTask;
    private boolean isSelect = false;

    public ForwardGroupListViewModel(boolean isSelect, @NonNull Application application) {
        super(application);
        groupTask = new GroupTask(application);
        this.isSelect = isSelect;
    }

    public ForwardGroupListViewModel(@NonNull Application application) {
        super(application);
        groupTask = new GroupTask(application);
    }

    @Override
    public void loadData() {
        LiveData<List<GroupEntity>> allGroupInfoList = groupTask.getAllGroupInfoList();
        conversationLiveData.addSource(allGroupInfoList, new Observer<List<GroupEntity>>() {
            @Override
            public void onChanged(List<GroupEntity> groupEntities) {
                if (groupEntities != null) {
                    final ModelBuilder modelBuilder = builderModel();
                    modelBuilder.addGroupList(groupEntities);
                    modelBuilder.post();
                }
            }
        });
    }

    @Override
    protected ListItemModel createGroupModel(GroupEntity entity) {
        ListItemModel model = super.createGroupModel(entity);
        if (isSelect) {
            model.setCheckStatus(ListItemModel.CheckStatus.UNCHECKED);
        }
        return model;
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private boolean isSelect;
        private Application application;

        public Factory(boolean isSelect, Application application) {
            this.isSelect = isSelect;
            this.application = application;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            try {
                return modelClass.getConstructor(boolean.class, Application.class).newInstance(isSelect, application);
            } catch (Exception e) {
                throw new RuntimeException("Cannot create an instance of " + modelClass, e);
            }
        }
    }
}
