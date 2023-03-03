package cn.rongcloud.im.task;

import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Result;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.model.UserGroupInfo;
import cn.rongcloud.im.model.UserGroupMemberInfo;
import cn.rongcloud.im.net.HttpClientManager;
import cn.rongcloud.im.net.RetrofitUtil;
import cn.rongcloud.im.net.service.UserGroupService;
import cn.rongcloud.im.utils.NetworkOnlyResource;
import io.rong.imlib.model.ConversationIdentifier;
import java.util.HashMap;
import java.util.List;
import okhttp3.RequestBody;

public class UserGroupTask {

    private final UserGroupService userGroupService;
    private final Context context;

    public UserGroupTask(Context context) {
        this.context = context;
        userGroupService =
                HttpClientManager.getInstance(context)
                        .getClient()
                        .createService(UserGroupService.class);
    }

    // 用户组列表
    public LiveData<Resource<List<UserGroupInfo>>> getUserGroupList(
            ConversationIdentifier identifier) {
        MediatorLiveData<Resource<List<UserGroupInfo>>> result = new MediatorLiveData<>();
        result.setValue(Resource.loading(null));
        LiveData<Resource<List<UserGroupInfo>>> userGroupList =
                new NetworkOnlyResource<List<UserGroupInfo>, Result<List<UserGroupInfo>>>() {
                    @NonNull
                    @Override
                    protected LiveData<Result<List<UserGroupInfo>>> createCall() {
                        HashMap<String, Object> paramsMap = new HashMap<>();
                        paramsMap.put("groupId", identifier.getTargetId());
                        paramsMap.put("pageNum", 1); // pageNum: 页数,默认1
                        paramsMap.put("limit", 50); // limit 每页大小,默认20,最大50

                        // 如果identifier中channelID不为空，则查询这个channel下的用户组列表
                        if (!TextUtils.isEmpty(identifier.getChannelId())) {
                            paramsMap.put("channelId", identifier.getChannelId());
                            RequestBody body = RetrofitUtil.createJsonRequest(paramsMap);
                            return userGroupService.userGroupListInChannel(body);
                        } else {
                            RequestBody body = RetrofitUtil.createJsonRequest(paramsMap);
                            return userGroupService.userGroupList(body);
                        }
                    }
                }.asLiveData();
        result.addSource(
                userGroupList,
                userGroupListResultResource -> {
                    if (userGroupListResultResource.status == Status.SUCCESS) {
                        result.removeSource(userGroupList);
                        result.setValue(Resource.success(userGroupListResultResource.data));
                    } else if (userGroupListResultResource.status == Status.ERROR) {
                        result.setValue(Resource.error(userGroupListResultResource.code, null));
                    }
                });
        return result;
    }

    // 用户组创建
    public LiveData<Resource<String>> userGroupAdd(String groupId, String userGroupName) {
        MediatorLiveData<Resource<String>> result = new MediatorLiveData<>();
        result.setValue(Resource.loading(null));

        LiveData<Resource<String>> userGroupAdd =
                new NetworkOnlyResource<String, Result<String>>() {
                    @NonNull
                    @Override
                    protected LiveData<Result<String>> createCall() {
                        HashMap<String, Object> paramsMap = new HashMap<>();
                        paramsMap.put("groupId", groupId);
                        paramsMap.put("userGroupName", userGroupName);
                        RequestBody body = RetrofitUtil.createJsonRequest(paramsMap);
                        return userGroupService.userGroupAdd(body);
                    }
                }.asLiveData();

        result.addSource(
                userGroupAdd,
                userGroupAddResultResource -> {
                    if (userGroupAddResultResource.status == Status.SUCCESS) {
                        result.removeSource(userGroupAdd);
                        result.setValue(Resource.success(userGroupAddResultResource.data));
                    } else if (userGroupAddResultResource.status == Status.ERROR) {
                        result.setValue(Resource.error(userGroupAddResultResource.code, null));
                    }
                });
        return result;
    }

    // 用户组删除
    public LiveData<Resource<String>> userGroupDel(String groupId, String userGroupId) {
        MediatorLiveData<Resource<String>> result = new MediatorLiveData<>();
        result.setValue(Resource.loading(null));

        LiveData<Resource<Void>> userGroupDel =
                new NetworkOnlyResource<Void, Result>() {
                    @NonNull
                    @Override
                    protected LiveData<Result> createCall() {
                        HashMap<String, Object> paramsMap = new HashMap<>();
                        paramsMap.put("groupId", groupId);
                        paramsMap.put("userGroupId", userGroupId);
                        RequestBody body = RetrofitUtil.createJsonRequest(paramsMap);
                        return userGroupService.userGroupDel(body);
                    }
                }.asLiveData();

        result.addSource(
                userGroupDel,
                userGroupDelResultResource -> {
                    if (userGroupDelResultResource.status == Status.SUCCESS) {
                        result.removeSource(userGroupDel);
                        result.setValue(Resource.success(null));
                    } else if (userGroupDelResultResource.status == Status.ERROR) {
                        result.setValue(Resource.error(userGroupDelResultResource.code, null));
                    }
                });
        return result;
    }

    // 用户组成员列表接口；
    public LiveData<Resource<List<UserGroupMemberInfo>>> userGroupMemberList(
            String groupId, String userGroupId) {
        MediatorLiveData<Resource<List<UserGroupMemberInfo>>> result = new MediatorLiveData<>();
        if (TextUtils.isEmpty(userGroupId)) {
            result.setValue(Resource.error(-1, null));
            return result;
        }

        result.setValue(Resource.loading(null));
        LiveData<Resource<List<UserGroupMemberInfo>>> userGroupMemberDel =
                new NetworkOnlyResource<
                        List<UserGroupMemberInfo>, Result<List<UserGroupMemberInfo>>>() {
                    @NonNull
                    @Override
                    protected LiveData<Result<List<UserGroupMemberInfo>>> createCall() {
                        HashMap<String, Object> paramsMap = new HashMap<>();
                        paramsMap.put("groupId", groupId);
                        paramsMap.put("userGroupId", userGroupId);
                        paramsMap.put("pageNum", 1); // pageNum: 页数,默认1
                        paramsMap.put("limit", 50); // limit 每页大小,默认20,最大50
                        RequestBody body = RetrofitUtil.createJsonRequest(paramsMap);
                        return userGroupService.userGroupMemberList(body);
                    }
                }.asLiveData();
        result.addSource(
                userGroupMemberDel,
                memberDelResultResource -> {
                    if (memberDelResultResource.status == Status.SUCCESS) {
                        result.removeSource(userGroupMemberDel);
                        result.setValue(Resource.success(memberDelResultResource.data));
                    } else if (memberDelResultResource.status == Status.ERROR) {
                        result.setValue(Resource.error(memberDelResultResource.code, null));
                    }
                });
        return result;
    }

    // 用户组成员添加/删除(会把请求的userGroupId返回给Observe)
    public LiveData<Resource<String>> userGroupMemberEdit(
            String groupId, String userGroupId, List<String> checkedList, boolean isAdd) {
        MediatorLiveData<Resource<String>> result = new MediatorLiveData<>();
        result.setValue(Resource.loading(null));

        LiveData<Resource<Void>> userGroupMemberAdd =
                new NetworkOnlyResource<Void, Result>() {
                    @NonNull
                    @Override
                    protected LiveData<Result> createCall() {
                        HashMap<String, Object> paramsMap = new HashMap<>();
                        paramsMap.put("groupId", groupId);
                        paramsMap.put("userGroupId", userGroupId);
                        paramsMap.put("memberIds", checkedList);
                        RequestBody body = RetrofitUtil.createJsonRequest(paramsMap);
                        if (isAdd) {
                            return userGroupService.userGroupMemberAdd(body);
                        } else {
                            return userGroupService.userGroupMemberDel(body);
                        }
                    }
                }.asLiveData();
        result.addSource(
                userGroupMemberAdd,
                memberAddResultResource -> {
                    if (memberAddResultResource.status == Status.SUCCESS) {
                        result.removeSource(userGroupMemberAdd);
                        result.setValue(Resource.success(userGroupId));
                    } else if (memberAddResultResource.status == Status.ERROR) {
                        result.setValue(Resource.error(memberAddResultResource.code, null));
                    }
                });
        return result;
    }

    // 用户组绑定/解绑频道
    public LiveData<Resource> editChannelUserGroup(
            String groupId, String channelId, List<String> userGroupIds, boolean isBind) {
        MediatorLiveData<Resource> result = new MediatorLiveData<>();
        result.setValue(Resource.loading(null));

        LiveData<Resource<Void>> userGroupBindChannel =
                new NetworkOnlyResource<Void, Result>() {
                    @NonNull
                    @Override
                    protected LiveData<Result> createCall() {
                        HashMap<String, Object> paramsMap = new HashMap<>();
                        paramsMap.put("groupId", groupId);
                        paramsMap.put("channelId", channelId);
                        paramsMap.put("userGroupIds", userGroupIds);
                        RequestBody body = RetrofitUtil.createJsonRequest(paramsMap);
                        if (isBind) {
                            return userGroupService.userGroupBindChannel(body);
                        } else {
                            return userGroupService.userGroupUnBindChannel(body);
                        }
                    }
                }.asLiveData();
        result.addSource(
                userGroupBindChannel,
                resource -> {
                    if (resource.status == Status.SUCCESS) {
                        result.removeSource(userGroupBindChannel);
                        result.setValue(Resource.success(resource.data));
                    } else if (resource.status == Status.ERROR) {
                        result.setValue(Resource.error(resource.code, null));
                    }
                });
        return result;
    }
}
