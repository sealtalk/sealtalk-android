package cn.rongcloud.im.net.service;

import androidx.lifecycle.LiveData;
import cn.rongcloud.im.model.Result;
import cn.rongcloud.im.model.UltraChannelInfo;
import cn.rongcloud.im.model.UltraGroupChannelCreateResult;
import cn.rongcloud.im.model.UltraGroupChannelMembers;
import cn.rongcloud.im.model.UltraGroupCreateResult;
import cn.rongcloud.im.model.UltraGroupInfo;
import cn.rongcloud.im.model.UltraGroupMemberListResult;
import cn.rongcloud.im.net.SealTalkUrl;
import java.util.List;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UltraGroupService {
    @POST(SealTalkUrl.ULTRA_GROUP_CREATE)
    LiveData<Result<UltraGroupCreateResult>> ultraGroupCreate(@Body RequestBody body);

    @POST(SealTalkUrl.ULTRA_GROUP_DISMISS)
    LiveData<Result> ultraGroupDismiss(@Body RequestBody body);

    @POST(SealTalkUrl.ULTRA_GROUP_MEMBER_ADD)
    LiveData<Result<List<String>>> ultraGroupMemberAdd(@Body RequestBody body);

    @POST(SealTalkUrl.ULTRA_GROUP_MEMBER_QUIT)
    LiveData<Result> ultraGroupQuit(@Body RequestBody body);

    @POST(SealTalkUrl.ULTRA_GROUP_USER_IN)
    LiveData<Result<List<UltraGroupInfo>>> ultraGroupUserIn();

    @POST(SealTalkUrl.GET_ULTRA_GROUP_MEMBERS)
    LiveData<Result<List<UltraGroupMemberListResult>>> getUltraGroupMembers(@Body RequestBody body);

    @POST(SealTalkUrl.ULTRA_GROUP_CHANNEL_CREATE)
    LiveData<Result<UltraGroupChannelCreateResult>> ultraGroupChannelCreate(@Body RequestBody body);

    @POST(SealTalkUrl.GROUP_SET_PORTRAIT_URL)
    LiveData<Result> setGroupPortraitUri(@Body RequestBody body);

    @POST(SealTalkUrl.GET_ULTRA_GROUP_CHANNEL)
    LiveData<Result<List<UltraChannelInfo>>> getUltraGroupChannelList(@Body RequestBody body);

    @POST(SealTalkUrl.ULTRA_GROUP_CHANGE_TYPE)
    LiveData<Result> changeUltraChannelType(@Body RequestBody body);

    @POST(SealTalkUrl.ULTRA_GROUP_PRIVATE_ADD_USERS)
    LiveData<Result> addUltraChannelUsers(@Body RequestBody body);

    @POST(SealTalkUrl.ULTRA_GROUP_PRIVATE_DEL_USERS)
    LiveData<Result> delUltraChannelUsers(@Body RequestBody body);

    @POST(SealTalkUrl.ULTRA_GROUP_PRIVATE_GET_USERS)
    LiveData<Result<UltraGroupChannelMembers>> getUltraChannelUsers(@Body RequestBody body);

    @POST(SealTalkUrl.ULTRA_GROUP_CHANNEL_DEL)
    LiveData<Result> delUltraGroupChannel(@Body RequestBody body);
}
