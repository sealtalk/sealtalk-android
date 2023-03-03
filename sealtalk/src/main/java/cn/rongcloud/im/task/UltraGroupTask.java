package cn.rongcloud.im.task;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import cn.rongcloud.im.common.ErrorCode;
import cn.rongcloud.im.db.DBManager;
import cn.rongcloud.im.db.dao.GroupDao;
import cn.rongcloud.im.db.dao.GroupMemberDao;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.file.FileManager;
import cn.rongcloud.im.im.IMManager;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Result;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.model.UltraChannelInfo;
import cn.rongcloud.im.model.UltraGroupChannelCreateResult;
import cn.rongcloud.im.model.UltraGroupChannelMembers;
import cn.rongcloud.im.model.UltraGroupCreateResult;
import cn.rongcloud.im.model.UltraGroupInfo;
import cn.rongcloud.im.model.UltraGroupMemberListResult;
import cn.rongcloud.im.net.HttpClientManager;
import cn.rongcloud.im.net.RetrofitUtil;
import cn.rongcloud.im.net.service.UltraGroupService;
import cn.rongcloud.im.ultraGroup.UltraGroupManager;
import cn.rongcloud.im.utils.NetworkOnlyResource;
import io.rong.imlib.IRongCoreEnum;
import io.rong.imlib.model.Conversation;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import okhttp3.RequestBody;

public class UltraGroupTask {
    private static final String TAG = "UltraGroupTask";
    private final UltraGroupService groupService;
    private final Context context;
    private final DBManager dbManager;
    private final FileManager fileManager;

    public UltraGroupTask(Context context) {
        this.context = context.getApplicationContext();
        groupService =
                HttpClientManager.getInstance(context)
                        .getClient()
                        .createService(UltraGroupService.class);
        dbManager = DBManager.getInstance(context);
        fileManager = new FileManager(context);
    }

    /** 超级群创建 */
    public LiveData<Resource<String>> createUltraGroup(
            String groupName, Uri portraitUri, String summary) {
        MediatorLiveData<Resource<String>> result = new MediatorLiveData<>();
        result.setValue(Resource.loading(null));
        LiveData<Resource<UltraGroupCreateResult>> ultraGroupCreate =
                new NetworkOnlyResource<UltraGroupCreateResult, Result<UltraGroupCreateResult>>() {
                    @NonNull
                    @Override
                    protected LiveData<Result<UltraGroupCreateResult>> createCall() {
                        HashMap<String, Object> paramsMap = new HashMap<>();
                        paramsMap.put("groupName", groupName);
                        paramsMap.put(
                                "portraitUri", portraitUri == null ? "" : portraitUri.toString());
                        paramsMap.put("summary", summary);
                        RequestBody body = RetrofitUtil.createJsonRequest(paramsMap);
                        return groupService.ultraGroupCreate(body);
                    }
                }.asLiveData();
        result.addSource(
                ultraGroupCreate,
                ultraGroupCreateResource -> {
                    if (ultraGroupCreateResource.status == Status.SUCCESS) {
                        result.removeSource(ultraGroupCreate);
                        UltraGroupCreateResult ultraGroupCreateResult =
                                ultraGroupCreateResource.data;
                        if (ultraGroupCreateResult != null) {
                            result.setValue(Resource.success(ultraGroupCreateResult.groupId));
                        } else {
                            if (ultraGroupCreateResource.code
                                    == ErrorCode.ULTRA_GROUP_CREATE_OVER_LIMIT.getCode()) {
                                result.setValue(
                                        Resource.error(
                                                ErrorCode.ULTRA_GROUP_CREATE_OVER_LIMIT.getCode(),
                                                null));
                            }
                        }
                    } else if (ultraGroupCreateResource.status == Status.ERROR) {
                        result.setValue(Resource.error(ultraGroupCreateResource.code, null));
                    }
                });
        return result;
    }

    /** 超级群频道创建 */
    public LiveData<Resource<String>> ultraGroupChannelCreate(
            String groupId, String channelName, IRongCoreEnum.UltraGroupChannelType type) {
        MediatorLiveData<Resource<String>> result = new MediatorLiveData<>();
        result.setValue(Resource.loading(null));
        LiveData<Resource<UltraGroupChannelCreateResult>> ultraGroupChannelCreate =
                new NetworkOnlyResource<
                        UltraGroupChannelCreateResult, Result<UltraGroupChannelCreateResult>>() {
                    @NonNull
                    @Override
                    protected LiveData<Result<UltraGroupChannelCreateResult>> createCall() {
                        HashMap<String, Object> paramsMap = new HashMap<>();
                        paramsMap.put("groupId", groupId);
                        paramsMap.put("channelName", channelName);
                        if (type != null) {
                            paramsMap.put("type", type.getValue());
                        }
                        RequestBody body = RetrofitUtil.createJsonRequest(paramsMap);
                        return groupService.ultraGroupChannelCreate(body);
                    }
                }.asLiveData();
        result.addSource(
                ultraGroupChannelCreate,
                ultraGroupChannelCreateResource -> {
                    if (ultraGroupChannelCreateResource.status == Status.SUCCESS) {
                        result.removeSource(ultraGroupChannelCreate);
                        UltraGroupChannelCreateResult ultraGroupChannelCreateResult =
                                ultraGroupChannelCreateResource.data;
                        if (ultraGroupChannelCreateResult != null) {
                            UltraGroupManager.getInstance()
                                    .addChannel(
                                            context,
                                            groupId,
                                            ultraGroupChannelCreateResult.channelId,
                                            channelName,
                                            type);
                            result.setValue(
                                    Resource.success(ultraGroupChannelCreateResult.channelId));
                        } else {
                            if (ultraGroupChannelCreateResource.code
                                    == ErrorCode.ULTRA_GROUP_CHANNEL_OVER_LIMIT.getCode()) {
                                result.setValue(
                                        Resource.error(
                                                ErrorCode.ULTRA_GROUP_CHANNEL_OVER_LIMIT.getCode(),
                                                null));
                            } else {
                                result.setValue(
                                        Resource.error(ErrorCode.API_ERR_OTHER.getCode(), null));
                            }
                        }
                    } else if (ultraGroupChannelCreateResource.status == Status.ERROR) {
                        result.setValue(Resource.error(ultraGroupChannelCreateResource.code, null));
                    }
                });
        return result;
    }

    /** 解散超级群 */
    public LiveData<Resource<Void>> dismissUltraGroup(String groupId) {
        return new NetworkOnlyResource<Void, Result>() {
            @Override
            protected void saveCallResult(@NonNull Void item) {
                GroupDao groupDao = dbManager.getGroupDao();
                if (groupDao != null) {
                    groupDao.deleteGroup(groupId);
                }

                GroupMemberDao groupMemberDao = dbManager.getGroupMemberDao();
                if (groupMemberDao != null) {
                    groupMemberDao.deleteGroupMember(groupId);
                }

                IMManager.getInstance()
                        .clearConversationAndMessage(
                                groupId, Conversation.ConversationType.ULTRA_GROUP);
            }

            @NonNull
            @Override
            protected LiveData<Result> createCall() {
                HashMap<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("groupId", groupId);
                return groupService.ultraGroupDismiss(RetrofitUtil.createJsonRequest(bodyMap));
            }
        }.asLiveData();
    }

    /** 退出超级群组 */
    public LiveData<Resource<Void>> quitUltraGroup(String groupId) {
        return new NetworkOnlyResource<Void, Result>() {
            @Override
            protected void saveCallResult(@NonNull Void item) {
                GroupDao groupDao = dbManager.getGroupDao();
                if (groupDao != null) {
                    groupDao.deleteGroup(groupId);
                }

                GroupMemberDao groupMemberDao = dbManager.getGroupMemberDao();
                if (groupMemberDao != null) {
                    groupMemberDao.deleteGroupMember(groupId);
                }

                IMManager.getInstance()
                        .clearConversationAndMessage(
                                groupId, Conversation.ConversationType.ULTRA_GROUP);
            }

            @NonNull
            @Override
            protected LiveData<Result> createCall() {
                HashMap<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("groupId", groupId);
                return groupService.ultraGroupQuit(RetrofitUtil.createJsonRequest(bodyMap));
            }
        }.asLiveData();
    }

    /** 添加超级群成员 */
    public LiveData<Resource<List<String>>> addUltraGroupMember(
            String groupId, List<String> memberList) {
        MediatorLiveData<Resource<List<String>>> result = new MediatorLiveData<>();
        result.setValue(Resource.loading(null));
        LiveData<Resource<List<String>>> resourceLiveData =
                new NetworkOnlyResource<List<String>, Result<List<String>>>() {
                    @NonNull
                    @Override
                    protected LiveData<Result<List<String>>> createCall() {
                        HashMap<String, Object> bodyMap = new HashMap<>();
                        bodyMap.put("groupId", groupId);
                        bodyMap.put("memberIds", memberList);
                        return groupService.ultraGroupMemberAdd(
                                RetrofitUtil.createJsonRequest(bodyMap));
                    }
                }.asLiveData();
        result.addSource(
                resourceLiveData,
                ultraGroupAddResource -> {
                    if (ultraGroupAddResource.status == Status.SUCCESS) {
                        result.removeSource(resourceLiveData);
                        result.setValue(Resource.success(ultraGroupAddResource.data));
                    } else if (ultraGroupAddResource.status == Status.ERROR) {
                        result.setValue(Resource.error(ultraGroupAddResource.code, null));
                    }
                });
        return result;
    }

    /** 获取当前用户所属的群组信息 */
    public LiveData<Resource<List<UltraGroupInfo>>> getUltraGroupMemberList() {
        MediatorLiveData<Resource<List<UltraGroupInfo>>> result = new MediatorLiveData<>();
        result.setValue(Resource.loading(null));
        LiveData<Resource<List<UltraGroupInfo>>> resourceLiveData =
                new NetworkOnlyResource<List<UltraGroupInfo>, Result<List<UltraGroupInfo>>>() {
                    @NonNull
                    @Override
                    protected LiveData<Result<List<UltraGroupInfo>>> createCall() {
                        return groupService.ultraGroupUserIn();
                    }
                }.asLiveData();
        result.addSource(
                resourceLiveData,
                ultraGroupCreateResource -> {
                    if (ultraGroupCreateResource.status == Status.SUCCESS) {
                        result.removeSource(resourceLiveData);
                        result.setValue(Resource.success(ultraGroupCreateResource.data));
                    } else if (ultraGroupCreateResource.status == Status.ERROR) {
                        result.setValue(Resource.error(ultraGroupCreateResource.code, null));
                    }
                });
        return result;
    }

    /** 获取当前群组下频道列表 */
    public LiveData<Resource<List<UltraChannelInfo>>> getUltraGroupChannelList(String groupId) {
        MediatorLiveData<Resource<List<UltraChannelInfo>>> result = new MediatorLiveData<>();
        result.setValue(Resource.loading(null));
        LiveData<Resource<List<UltraChannelInfo>>> resourceLiveData =
                new NetworkOnlyResource<List<UltraChannelInfo>, Result<List<UltraChannelInfo>>>() {
                    @NonNull
                    @Override
                    protected LiveData<Result<List<UltraChannelInfo>>> createCall() {
                        HashMap<String, Object> paramsMap = new HashMap<>();
                        paramsMap.put("groupId", groupId);
                        paramsMap.put("pageNum", 1);
                        paramsMap.put("limit", 20);
                        RequestBody body = RetrofitUtil.createJsonRequest(paramsMap);
                        return groupService.getUltraGroupChannelList(body);
                    }
                }.asLiveData();
        result.addSource(
                resourceLiveData,
                ultraGroupCreateResource -> {
                    if (ultraGroupCreateResource.status == Status.SUCCESS) {
                        result.removeSource(resourceLiveData);
                        result.setValue(Resource.success(ultraGroupCreateResource.data));
                    } else if (ultraGroupCreateResource.status == Status.ERROR) {
                        result.setValue(Resource.error(ultraGroupCreateResource.code, null));
                    }
                });
        return result;
    }

    /** 获取群成员列表,通过成员名称筛选 */
    public LiveData<Resource<List<UltraGroupMemberListResult>>> getUltraGroupMemberInfoList(
            final String groupId, int pageNum, int limit) {
        return new NetworkOnlyResource<
                List<UltraGroupMemberListResult>, Result<List<UltraGroupMemberListResult>>>() {

            @NonNull
            @Override
            protected LiveData<Result<List<UltraGroupMemberListResult>>> createCall() {
                HashMap<String, Object> paramsMap = new HashMap<>();
                paramsMap.put("groupId", groupId);
                paramsMap.put("pageNum", pageNum);
                paramsMap.put("limit", limit);
                RequestBody body = RetrofitUtil.createJsonRequest(paramsMap);
                return groupService.getUltraGroupMembers(body);
            }
        }.asLiveData();
    }

    public LiveData<Resource<Boolean>> changeChannelType(
            final String groupId, final String channel, final int type) {
        MediatorLiveData<Resource<Boolean>> result = new MediatorLiveData<>();
        result.setValue(Resource.loading(null));
        LiveData<Resource<Void>> resourceLiveData =
                new NetworkOnlyResource<Void, Result>() {

                    @NonNull
                    @Override
                    protected LiveData<Result> createCall() {
                        HashMap<String, Object> paramsMap = new HashMap<>();
                        paramsMap.put("groupId", groupId);
                        paramsMap.put("channelId", channel);
                        paramsMap.put("type", type);
                        RequestBody body = RetrofitUtil.createJsonRequest(paramsMap);
                        return groupService.changeUltraChannelType(body);
                    }
                }.asLiveData();
        result.addSource(
                resourceLiveData,
                new Observer<Resource<Void>>() {
                    @Override
                    public void onChanged(Resource<Void> voidResource) {
                        Log.d(TAG, "onChanged: " + voidResource.status);
                        if (voidResource.status == Status.SUCCESS) {
                            result.setValue(Resource.success(true));
                        } else if (voidResource.status == Status.ERROR) {
                            result.setValue(Resource.error(-1, false));
                        }
                    }
                });
        return result;
    }

    public LiveData<Resource<Boolean>> delChannelUsers(
            final String groupId, final String channelId, final List<String> memberIds) {
        MediatorLiveData<Resource<Boolean>> result = new MediatorLiveData<>();
        result.setValue(Resource.loading(null));
        LiveData<Resource<Void>> resourceLiveData =
                new NetworkOnlyResource<Void, Result>() {

                    @NonNull
                    @Override
                    protected LiveData<Result> createCall() {
                        HashMap<String, Object> paramsMap = new HashMap<>();
                        paramsMap.put("groupId", groupId);
                        paramsMap.put("channelId", channelId);
                        paramsMap.put("memberIds", memberIds);
                        RequestBody body = RetrofitUtil.createJsonRequest(paramsMap);
                        return groupService.delUltraChannelUsers(body);
                    }
                }.asLiveData();
        result.addSource(
                resourceLiveData,
                new Observer<Resource<Void>>() {
                    @Override
                    public void onChanged(Resource<Void> voidResource) {
                        Log.d(TAG, "onChanged: " + voidResource.status);
                        if (voidResource.status == Status.SUCCESS) {
                            result.setValue(Resource.success(true));
                        } else if (voidResource.status == Status.ERROR) {
                            result.setValue(Resource.error(-1, false));
                        }
                    }
                });
        return result;
    }

    public LiveData<Resource<Boolean>> delChannel(final String groupId, final String channelId) {
        MediatorLiveData<Resource<Boolean>> result = new MediatorLiveData<>();
        result.setValue(Resource.loading(null));
        LiveData<Resource<Void>> resourceLiveData =
                new NetworkOnlyResource<Void, Result>() {

                    @NonNull
                    @Override
                    protected LiveData<Result> createCall() {
                        HashMap<String, Object> paramsMap = new HashMap<>();
                        paramsMap.put("groupId", groupId);
                        paramsMap.put("channelId", channelId);
                        RequestBody body = RetrofitUtil.createJsonRequest(paramsMap);
                        return groupService.delUltraGroupChannel(body);
                    }
                }.asLiveData();
        result.addSource(
                resourceLiveData,
                new Observer<Resource<Void>>() {
                    @Override
                    public void onChanged(Resource<Void> voidResource) {
                        Log.d(TAG, "onChanged: " + voidResource.status);
                        if (voidResource.status == Status.SUCCESS) {
                            result.setValue(Resource.success(true));
                        } else if (voidResource.status == Status.ERROR) {
                            result.setValue(Resource.error(-1, false));
                        }
                    }
                });
        return result;
    }

    public LiveData<Resource<Boolean>> addChannelUsers(
            final String groupId, final String channelId, final List<String> memberIds) {
        MediatorLiveData<Resource<Boolean>> result = new MediatorLiveData<>();
        result.setValue(Resource.loading(null));
        LiveData<Resource<Void>> resourceLiveData =
                new NetworkOnlyResource<Void, Result>() {

                    @NonNull
                    @Override
                    protected LiveData<Result> createCall() {
                        HashMap<String, Object> paramsMap = new HashMap<>();
                        paramsMap.put("groupId", groupId);
                        paramsMap.put("channelId", channelId);
                        paramsMap.put("memberIds", memberIds);
                        RequestBody body = RetrofitUtil.createJsonRequest(paramsMap);
                        return groupService.addUltraChannelUsers(body);
                    }
                }.asLiveData();
        result.addSource(
                resourceLiveData,
                new Observer<Resource<Void>>() {
                    @Override
                    public void onChanged(Resource<Void> voidResource) {
                        Log.d(TAG, "onChanged: " + voidResource.status);
                        if (voidResource.status == Status.SUCCESS) {
                            result.setValue(Resource.success(true));
                        } else if (voidResource.status == Status.ERROR) {
                            result.setValue(Resource.error(-1, false));
                        }
                    }
                });
        return result;
    }

    public LiveData<Resource<List<String>>> getChannelUsers(
            String groupId, String channelId, int pageNum, int limit) {
        MediatorLiveData<Resource<List<String>>> result = new MediatorLiveData<>();
        result.setValue(Resource.loading(null));
        LiveData<Resource<UltraGroupChannelMembers>> resourceLiveData =
                new NetworkOnlyResource<
                        UltraGroupChannelMembers, Result<UltraGroupChannelMembers>>() {

                    @NonNull
                    @Override
                    protected LiveData<Result<UltraGroupChannelMembers>> createCall() {
                        HashMap<String, Object> paramsMap = new HashMap<>();
                        paramsMap.put("groupId", groupId);
                        paramsMap.put("channelId", channelId);
                        paramsMap.put("pageNum", pageNum);
                        paramsMap.put("limit", limit);
                        RequestBody body = RetrofitUtil.createJsonRequest(paramsMap);
                        return groupService.getUltraChannelUsers(body);
                    }
                }.asLiveData();
        result.addSource(
                resourceLiveData,
                new Observer<Resource<UltraGroupChannelMembers>>() {
                    @Override
                    public void onChanged(Resource<UltraGroupChannelMembers> voidResource) {
                        Log.d(TAG, "onChanged: " + voidResource.status);
                        if (voidResource.status == Status.SUCCESS) {
                            result.setValue(Resource.success(voidResource.data.getUsers()));
                        } else if (voidResource.status == Status.ERROR) {
                            result.setValue(Resource.error(-1, Collections.emptyList()));
                        }
                    }
                });
        return result;
    }

    /** 上传并设置群组头像 */
    public LiveData<Resource<String>> uploadAndSetGroupPortrait(Uri portraitUrl) {
        MediatorLiveData<Resource<String>> result = new MediatorLiveData<>();
        // 先上传图片文件
        LiveData<Resource<String>> uploadResource = fileManager.uploadImage(portraitUrl);
        result.addSource(
                uploadResource,
                resource -> {
                    if (resource.status != Status.LOADING) {
                        result.removeSource(uploadResource);
                    }

                    if (resource.status == Status.ERROR) {
                        result.setValue(Resource.error(resource.code, null));
                        return;
                    }

                    if (resource.status == Status.SUCCESS) {
                        String uploadUrl = resource.data;
                        result.setValue(Resource.success(uploadUrl));
                    }
                });

        return result;
    }

    /** 设置群组头像 */
    private LiveData<Resource<Void>> setGroupPortrait(
            String groupName, String groupId, String portraitUrl) {
        return new NetworkOnlyResource<Void, Result>() {
            @Override
            protected void saveCallResult(@NonNull Void item) {
                // 更新数据库中群组的头像
                GroupDao groupDao = dbManager.getGroupDao();
                if (groupDao != null) {
                    int updateResult;
                    updateResult = groupDao.updateGroupPortrait(groupId, portraitUrl);

                    // 更新成时同时更新缓存
                    if (updateResult > 0) {
                        GroupEntity groupInfo = groupDao.getGroupInfoSync(groupId);
                        IMManager.getInstance()
                                .updateGroupInfoCache(
                                        groupId, groupInfo.getName(), Uri.parse(portraitUrl));
                    }
                }
            }

            @NonNull
            @Override
            protected LiveData<Result> createCall() {
                HashMap<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("groupId", groupId);
                bodyMap.put("portraitUri", portraitUrl);
                return groupService.setGroupPortraitUri(RetrofitUtil.createJsonRequest(bodyMap));
            }
        }.asLiveData();
    }
}
