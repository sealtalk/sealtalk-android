package cn.rongcloud.im.net.service;

import androidx.lifecycle.LiveData;

import java.util.List;

import cn.rongcloud.im.db.model.GroupEntity;
import cn.rongcloud.im.db.model.GroupExitedMemberInfo;
import cn.rongcloud.im.db.model.GroupMemberInfoDes;
import cn.rongcloud.im.model.AddMemberResult;
import cn.rongcloud.im.model.CopyGroupResult;
import cn.rongcloud.im.model.GroupNoticeInfoResult;
import cn.rongcloud.im.model.GroupNoticeResult;
import cn.rongcloud.im.model.GroupMemberInfoResult;
import cn.rongcloud.im.model.GroupResult;
import cn.rongcloud.im.model.RegularClearStatusResult;
import cn.rongcloud.im.model.Result;
import cn.rongcloud.im.net.SealTalkUrl;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GroupService {

    @POST(SealTalkUrl.GROUP_CREATE)
    LiveData<Result<GroupResult>> createGroup(@Body RequestBody body);

    @POST(SealTalkUrl.GROUP_ADD_MEMBER)
    LiveData<Result<List<AddMemberResult>>> addGroupMember(@Body RequestBody body);

    @POST(SealTalkUrl.GROUP_JOIN)
    LiveData<Result> joinGroup(@Body RequestBody body);

    @POST(SealTalkUrl.GROUP_KICK_MEMBER)
    LiveData<Result> kickMember(@Body RequestBody body);

    @POST(SealTalkUrl.GROUP_QUIT)
    LiveData<Result> quitGroup(@Body RequestBody body);

    @POST(SealTalkUrl.GROUP_DISMISS)
    LiveData<Result> dismissGroup(@Body RequestBody body);

    @POST(SealTalkUrl.GROUP_TRANSFER)
    LiveData<Result> transferGroup(@Body RequestBody body);

    @POST(SealTalkUrl.GROUP_RENAME)
    LiveData<Result> renameGroup(@Body RequestBody body);

    @POST(SealTalkUrl.GROUP_SET_BULLETIN)
    LiveData<Result> setGroupBulletin(@Body RequestBody body);

    @GET(SealTalkUrl.GROUP_GET_BULLETIN)
    LiveData<Result<GroupNoticeResult>> getGroupBulletin(@Query("groupId") String id);

    @POST(SealTalkUrl.GROUP_SET_PORTRAIT_URL)
    LiveData<Result> setGroupPortraitUri(@Body RequestBody body);

    @POST(SealTalkUrl.GROUP_SET_DISPLAY_NAME)
    LiveData<Result> setMemberDisplayName(@Body RequestBody body);

    @GET(SealTalkUrl.GROUP_GET_INFO)
    LiveData<Result<GroupEntity>> getGroupInfo(@Path("group_id") String groupId);

    @GET(SealTalkUrl.GROUP_GET_MEMBER_INFO)
    LiveData<Result<List<GroupMemberInfoResult>>> getGroupMemberList(@Path("group_id") String groupId);

    @POST(SealTalkUrl.GROUP_REMOVE_MANAGER)
    LiveData<Result> removeManager(@Body RequestBody body);

    @POST(SealTalkUrl.GROUP_ADD_MANAGER)
    LiveData<Result> addManager(@Body RequestBody body);

    @POST(SealTalkUrl.GROUP_SAVE_TO_CONTACT)
    LiveData<Result> saveToContact(@Body RequestBody body);

    @HTTP(method = "DELETE", path = SealTalkUrl.GROUP_SAVE_TO_CONTACT, hasBody = true)
    LiveData<Result> removeFromContact(@Body RequestBody body);

    @POST(SealTalkUrl.GROUP_SET_REGULAR_CLEAR)
    LiveData<Result> setRegularClear(@Body RequestBody body);

    @POST(SealTalkUrl.GROUP_GET_REGULAR_CLEAR_STATE)
    LiveData<Result<RegularClearStatusResult>> getRegularClearState(@Body RequestBody body);

    @POST(SealTalkUrl.GROUP_MUTE_ALL)
    LiveData<Result> muteAll(@Body RequestBody body);

    @POST(SealTalkUrl.GROUP_MEMBER_PROTECTION)
    LiveData<Result> setGroupProtection(@Body RequestBody body);

    @POST(SealTalkUrl.GROUP_SET_CERTIFICATION)
    LiveData<Result<Void>> setCertification(@Body RequestBody body);

    @GET(SealTalkUrl.GROUP_GET_NOTICE_INFO)
    LiveData<Result<List<GroupNoticeInfoResult>>> getGroupNoticeInfo();

    @POST(SealTalkUrl.GROUP_SET_NOTICE_STATUS)
    LiveData<Result<Void>> setGroupNoticeStatus(@Body RequestBody body);

    @POST(SealTalkUrl.GROUP_CLEAR_NOTICE)
    LiveData<Result<Void>> clearGroupNotice();

    @POST(SealTalkUrl.GROUP_COPY)
    LiveData<Result<CopyGroupResult>> copyGroup(@Body RequestBody body);

    @POST(SealTalkUrl.GROUP_GET_EXITED)
    LiveData<Result<List<GroupExitedMemberInfo>>> getGroupExitedMemberInfo(@Body RequestBody body);

    @POST(SealTalkUrl.GROUP_GET_MEMBER_INFO_DES)
    LiveData<Result<GroupMemberInfoDes>> getGroupInfoDes(@Body RequestBody body);

    @POST(SealTalkUrl.GROUP_SET_MEMBER_INFO_DES)
    LiveData<Result<Void>> setGroupInfoDes(@Body RequestBody body);

}
