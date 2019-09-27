package cn.rongcloud.im.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.model.GroupMember;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.task.GroupTask;
import cn.rongcloud.im.utils.CharacterParser;
import cn.rongcloud.im.utils.SingleSourceLiveData;
import cn.rongcloud.im.utils.SingleSourceMapLiveData;

public class GroupManagementViewModel extends AndroidViewModel {
    private String groupId;
    private MediatorLiveData<Resource<List<GroupMember>>> groupManagements = new MediatorLiveData<>();
    private MutableLiveData<GroupMember> groupOwner = new MutableLiveData<>();
    private MediatorLiveData<Resource<Void>> removeManagerResult = new MediatorLiveData<>();
    private MediatorLiveData<Resource<Void>> addManagerResult = new MediatorLiveData<>();
    private MediatorLiveData<Resource<Void>> transferResult = new MediatorLiveData<>();
    private MediatorLiveData<GroupEntity> groupInfo = new MediatorLiveData<>();
    private GroupTask groupTask;
    private SingleSourceMapLiveData<Resource<List<GroupMember>>, List<GroupMember>> groupMembersWithoutGroupOwner;
    private SingleSourceLiveData<Resource<Void>> muteAllResult = new SingleSourceLiveData<>();
    private SingleSourceLiveData<Resource<Void>> memberProtectionResult = new SingleSourceLiveData<>();
    private SingleSourceLiveData<Resource<Void>> setCerifiResult = new SingleSourceLiveData<>();

    public GroupManagementViewModel(@NonNull Application application) {
        super(application);
    }

    public GroupManagementViewModel(String groupId, @NonNull Application application) {
        super(application);
        groupTask = new GroupTask(application);
        this.groupId = groupId;
        groupMemberInfo(groupId);
        getGroupInfo(groupId);
    }

    private void getGroupInfo(String groupId) {
        LiveData<GroupEntity> mGroupEntity = groupTask.getGroupInfoInDB(groupId);
        groupInfo.addSource(mGroupEntity, new Observer<GroupEntity>() {
            @Override
            public void onChanged(GroupEntity groupEntity) {
                if (groupEntity != null) {
                    groupInfo.removeSource(mGroupEntity);
                    groupInfo.postValue(groupEntity);
                }
            }
        });
    }

    /**
     * 设置入群认证
     *
     * @param certiStatus
     */
    public void setCerification(int certiStatus) {
        setCerifiResult.setSource(groupTask.setCertification(groupId, certiStatus));
    }

    public LiveData<Resource<Void>> getCerifiResult() {
        return setCerifiResult;
    }

    /**
     * 开启禁言
     *
     * @param muteAllState
     */
    public void setMuteAll(int muteAllState) {
        muteAllResult.setSource(groupTask.setMuteAll(groupId, muteAllState, ""));
    }

    public LiveData<Resource<Void>> getMuteAllResult() {
        return muteAllResult;
    }

    /**
     * 设置成员保护
     *
     * @param memberProtection
     */
    public void setMemberProtection(int memberProtection) {
        memberProtectionResult.setSource(groupTask.setMemberProtection(groupId, memberProtection));
    }

    public LiveData<Resource<Void>> getMemberProtectionResult() {
        return memberProtectionResult;
    }

    /**
     * 获取群信息
     *
     * @return
     */
    public LiveData<GroupEntity> getGroupInfo() {
        return groupInfo;
    }

    private void groupMemberInfo(String groupId) {
        LiveData<Resource<List<GroupMember>>> getGroupMembers = groupTask.getGroupMemberInfoList(groupId);
        groupManagements.addSource(getGroupMembers, new Observer<Resource<List<GroupMember>>>() {
            @Override
            public void onChanged(Resource<List<GroupMember>> listResource) {
                List<GroupMember> managements = new ArrayList<>();
                if (listResource != null && listResource.data != null && listResource.data.size() > 0) {
                    List<GroupMember> data = listResource.data;
                    for (GroupMember member : data) {
                        if (member.getMemberRole() == GroupMember.Role.GROUP_OWNER) {
                            groupOwner.postValue(member);
                        } else if (member.getMemberRole() == GroupMember.Role.MANAGEMENT) {
                            managements.add(member);
                        }
                    }
                }

                groupManagements.postValue(new Resource<>(listResource.status, managements, listResource.code));
            }
        });


        groupMembersWithoutGroupOwner = new SingleSourceMapLiveData<>(new Function<Resource<List<GroupMember>>, List<GroupMember>>() {
            @Override
            public List<GroupMember> apply(Resource<List<GroupMember>> input) {
                List<GroupMember> withoutGroupOnwer = new ArrayList<>();
                if (input != null && input.data != null && input.data.size() > 0) {
                    List<GroupMember> data = input.data;
                    withoutGroupOnwer.addAll(data);
                    for (GroupMember member : data) {
                        if (member.getMemberRole() == GroupMember.Role.GROUP_OWNER) {
                            withoutGroupOnwer.remove(member);
                        }
                        String sortString = "#";
                        //汉字转换成拼音
                        String pinyin = CharacterParser.getInstance().getSpelling(member.getName());
                        if (pinyin != null) {
                            if (pinyin.length() > 0) {
                                sortString = pinyin.substring(0, 1).toUpperCase();
                            }
                        }
                        // 正则表达式，判断首字母是否是英文字母
                        if (sortString.matches("[A-Z]")) {
                            member.setNameSpelling(sortString.toUpperCase());
                        } else {
                            member.setNameSpelling("#");
                        }
                    }
                    Collections.sort(withoutGroupOnwer, new Comparator<GroupMember>() {
                        @Override
                        public int compare(GroupMember o1, GroupMember o2) {
                            if (o1.getNameSpelling().equals("@") || o2.getNameSpelling().equals("#")) {
                                return -1;
                            } else if (o1.getNameSpelling().equals("#") || o2.getNameSpelling().equals("@")) {
                                return 1;
                            } else {
                                return o1.getNameSpelling().compareTo(o2.getNameSpelling());
                            }
                        }
                    });
                    return withoutGroupOnwer;
                }
                return null;
            }
        });
        groupMembersWithoutGroupOwner.setSource(getGroupMembers);
    }

    /**
     * 群主
     *
     * @return
     */
    public LiveData<GroupMember> getGroupOwner() {
        return groupOwner;
    }

    /**
     * 获去群管理
     *
     * @return
     */
    public LiveData<Resource<List<GroupMember>>> getGroupManagements() {
        return groupManagements;
    }

    /**
     * 删除管理员结果
     *
     * @return
     */
    public LiveData<Resource<Void>> getRemoveManagerResult() {
        return removeManagerResult;
    }

    /**
     * 添加管理员结果
     *
     * @return
     */
    public LiveData<Resource<Void>> getAddManagerResult() {
        return addManagerResult;
    }


    /**
     * 除群主之外的所有成员
     *
     * @return
     */
    public LiveData<List<GroupMember>> getGroupMembersWithoutGroupOwner() {
        return groupMembersWithoutGroupOwner;
    }

    /**
     * 转让群角色结果
     *
     * @return
     */
    public LiveData<Resource<Void>> getTransferResult() {
        return transferResult;
    }

    /**
     * 删除管理员
     *
     * @param member
     */
    public void deleteManagement(GroupMember member) {

        LiveData<Resource<Void>> resourceLiveData = groupTask.removeManager(member.getGroupId(), new String[]{member.getUserId()});

        removeManagerResult.addSource(resourceLiveData, new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> resource) {
                if (resource.status != Status.LOADING) {
                    removeManagerResult.removeSource(resourceLiveData);
                }

                if (resource.status == Status.SUCCESS) {
                    groupMemberInfo(groupId);
                    removeManagerResult.addSource(groupManagements, new Observer<Resource<List<GroupMember>>>() {
                        @Override
                        public void onChanged(Resource<List<GroupMember>> listResource) {
                            if (listResource.status != Status.LOADING) {
                                removeManagerResult.removeSource(groupManagements);
                                removeManagerResult.postValue(resource);
                            }
                        }
                    });
                } else {
                    removeManagerResult.postValue(resource);
                }
            }
        });
    }

    /**
     * 添加管理员
     *
     * @param membersIds
     */
    public void addManagement(List<String> membersIds) {
        String[] memIds = new String[membersIds.size()];
        for (int i = 0; i < membersIds.size(); i++) {
            memIds[i] = membersIds.get(i);
        }

        LiveData<Resource<Void>> resourceLiveData = groupTask.addManager(groupId, memIds);

        addManagerResult.addSource(resourceLiveData, new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> resource) {
                if (resource.status != Status.LOADING) {
                    addManagerResult.removeSource(resourceLiveData);
                }

                if (resource.status == Status.SUCCESS) {
                    groupMemberInfo(groupId);
                    addManagerResult.addSource(groupManagements, new Observer<Resource<List<GroupMember>>>() {
                        @Override
                        public void onChanged(Resource<List<GroupMember>> listResource) {
                            if (listResource.status != Status.LOADING) {
                                addManagerResult.removeSource(groupManagements);
                                addManagerResult.postValue(resource);
                            }
                        }
                    });
                } else {
                    addManagerResult.postValue(resource);
                }
            }
        });
    }


    /**
     * 转让群角色
     *
     * @param groupId
     * @param userId
     */
    public void transferGroupOwner(String groupId, String userId) {
//        transferResult.setSource(groupTask.transferGroup(groupId, userId));
        LiveData<Resource<Void>> transferGroup = groupTask.transferGroup(groupId, userId);
        transferResult.addSource(transferGroup, new Observer<Resource<Void>>() {
            @Override
            public void onChanged(Resource<Void> resource) {
                if (resource.status != Status.LOADING) {
                    transferResult.removeSource(transferGroup);
                }

                if (resource.status == Status.SUCCESS) {
                    LiveData<Resource<List<GroupMember>>> groupMemberInfoList = groupTask.getGroupMemberInfoList(groupId);
                    transferResult.addSource(groupMemberInfoList, new Observer<Resource<List<GroupMember>>>() {
                        @Override
                        public void onChanged(Resource<List<GroupMember>> resourceMemberList) {
                            if (resourceMemberList.status != Status.LOADING) {
                                transferResult.removeSource(groupMemberInfoList);
                                return;
                            }
                            transferResult.postValue(resource);
                        }
                    });
                } else {
                    transferResult.postValue(resource);
                }
            }
        });
    }


    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private String groupId;
        private Application application;

        public Factory(String groupId, Application application) {
            this.groupId = groupId;
            this.application = application;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            try {
                return modelClass.getConstructor(String.class, Application.class).newInstance(groupId, application);
            } catch (Exception e) {
                throw new RuntimeException("Cannot create an instance of " + modelClass, e);
            }
        }
    }

}
