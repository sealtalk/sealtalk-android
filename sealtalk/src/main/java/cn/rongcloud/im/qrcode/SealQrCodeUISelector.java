package cn.rongcloud.im.qrcode;

import android.content.Context;
import android.content.Intent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.Date;
import java.util.List;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.ErrorCode;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.model.GroupMember;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.model.qrcode.QRCodeResult;
import cn.rongcloud.im.task.GroupTask;
import cn.rongcloud.im.ui.activity.JoinGroupActivity;
import cn.rongcloud.im.ui.activity.UserDetailActivity;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.Conversation;

/**
 * QR 二维码界面跳转工具
 */
public class SealQrCodeUISelector {
    private Context context;
    private GroupTask groupTask;

    public SealQrCodeUISelector(Context context) {
        this.context = context;
        groupTask = new GroupTask(context);
    }


    /**
     * 根据 QR 二维码中的 uri 进行相应的界面展示
     *
     * @param uri
     * @return Resource 状态 success 为成功跳转，data 中值为 null ;error 为没有跳转成功，此时 data 中的值为 错误信息
     */
    public LiveData<Resource<String>> handleUri(String uri) {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();
        QRCodeManager qrCodeManager = new QRCodeManager(context);
        QRCodeResult qrCodeResult = qrCodeManager.getQRCodeType(uri);
        switch (qrCodeResult.getType()) {
            case GROUP_INFO:
                checkGroupIsExist(qrCodeResult.getGroupInfoResult().getGroupId(), result);
                break;
            case USER_INFO:
                showUserDetail(qrCodeResult.getUserInfoResult().getUserId(), result);
                break;
            default:
                showOther(result);
        }

        return result;
    }

    /**
     * 检查群组是否存在
     *
     * @param groupId
     */
    private void checkGroupIsExist(String groupId, MutableLiveData<Resource<String>> result) {
        LiveData<Resource<GroupEntity>> groupInfo = groupTask.getGroupInfo(groupId);
        groupInfo.observeForever(new Observer<Resource<GroupEntity>>() {
            @Override
            public void onChanged(Resource<GroupEntity> resource) {
                if (resource.status != Status.LOADING || resource.data != null) {
                    groupInfo.removeObserver(this);
                }

                GroupEntity data = resource.data;

                if (data != null) {
                    String groupName = data.getName();
                    checkIsInGroup(groupId, groupName, result);
                } else if (resource.status == Status.ERROR) {
                    if (resource.code == ErrorCode.API_COMMON_ERROR.getCode()) {
                        result.postValue(Resource.error(ErrorCode.QRCODE_ERROR.getCode(), context.getString(R.string.profile_group_not_exist)));
                    } else {
                        result.postValue(Resource.error(ErrorCode.QRCODE_ERROR.getCode(), resource.message));
                    }
                }
            }
        });
    }

    /**
     * 检查是否在群组中
     *
     * @param groupId
     * @param groupName
     */
    private void checkIsInGroup(String groupId, String groupName, MutableLiveData<Resource<String>> result) {
        LiveData<Resource<List<GroupMember>>> groupMemberList = groupTask.getGroupMemberInfoList(groupId);
        String currentUserId = RongIM.getInstance().getCurrentUserId();

        groupMemberList.observeForever(new Observer<Resource<List<GroupMember>>>() {
            @Override
            public void onChanged(Resource<List<GroupMember>> resource) {
                if (resource.status != Status.LOADING) {
                    groupMemberList.removeObserver(this);
                }
                // 获取成员列表
                if (resource.status == Status.SUCCESS) {
                    List<GroupMember> groupMemberList = resource.data;
                    if (groupMemberList != null) {
                        for (GroupMember groupMember : groupMemberList) {
                            String userId = groupMember.getUserId();
                            if (currentUserId.equals(userId)) {
                                // 群组中包含自己则跳转到群聊天界面
                                toGroupChat(groupId, groupName, result);
                                return;
                            }
                        }

                        // 如果群组成员中没有自己则跳转到加入群组界面
                        showJoinGroup(groupId, result);
                        return;
                    }
                }

                if (resource.status == Status.ERROR) {
                    showJoinGroup(groupId, result);
                }
            }
        });
    }

    /**
     * 显示群聊
     *
     * @param groupId
     * @param groupName
     * @param result
     */
    private void toGroupChat(String groupId, String groupName, MutableLiveData<Resource<String>> result) {
        RongIM.getInstance().startConversation(context, Conversation.ConversationType.GROUP, groupId, groupName);
        result.postValue(Resource.success(null));
    }

    /**
     * 显示加入群组界面
     *
     * @param groupId
     */
    private void showJoinGroup(String groupId, MutableLiveData<Resource<String>> result) {
        Intent intent = new Intent(context, JoinGroupActivity.class);
        intent.putExtra(IntentExtra.STR_TARGET_ID, groupId);
        context.startActivity(intent);
        result.postValue(Resource.success(null));
    }

    /**
     * 显示用户详情
     *
     * @param userId
     */
    private void showUserDetail(String userId, MutableLiveData<Resource<String>> result) {
        Intent intent = new Intent(context, UserDetailActivity.class);
        intent.putExtra(IntentExtra.STR_TARGET_ID, userId);
        context.startActivity(intent);
        result.postValue(Resource.success(null));
    }

    /**
     * 其他二维码时不做处理
     */
    private void showOther(MutableLiveData<Resource<String>> result) {
        result.postValue(Resource.error(ErrorCode.QRCODE_ERROR.getCode(), context.getString(R.string.zxing_qr_can_not_recognized)));
    }
}
