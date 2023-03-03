package cn.rongcloud.im.net.service;

import androidx.lifecycle.LiveData;
import cn.rongcloud.im.model.Result;
import cn.rongcloud.im.model.UserGroupInfo;
import cn.rongcloud.im.model.UserGroupMemberInfo;
import cn.rongcloud.im.net.SealTalkUrl;
import java.util.List;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserGroupService {

    @POST(SealTalkUrl.USER_GROUP_QUERY)
    LiveData<Result<List<UserGroupInfo>>> userGroupList(@Body RequestBody body);

    @POST(SealTalkUrl.USER_GROUP_ADD)
    LiveData<Result<String>> userGroupAdd(@Body RequestBody body);

    @POST(SealTalkUrl.USER_GROUP_DEL)
    LiveData<Result> userGroupDel(@Body RequestBody body);

    @POST(SealTalkUrl.USER_GROUP_MEMBER_QUERY)
    LiveData<Result<List<UserGroupMemberInfo>>> userGroupMemberList(@Body RequestBody body);

    @POST(SealTalkUrl.USER_GROUP_MEMBER_ADD)
    LiveData<Result> userGroupMemberAdd(@Body RequestBody body);

    @POST(SealTalkUrl.USER_GROUP_MEMBER_DEL)
    LiveData<Result> userGroupMemberDel(@Body RequestBody body);

    @POST(SealTalkUrl.USER_GROUP_CHANNEL_QUERY)
    LiveData<Result<List<UserGroupInfo>>> userGroupListInChannel(@Body RequestBody body);

    @POST(SealTalkUrl.USER_GROUP_CHANNEL_BIND)
    LiveData<Result> userGroupBindChannel(@Body RequestBody body);

    @POST(SealTalkUrl.USER_GROUP_CHANNEL_UNBIND)
    LiveData<Result> userGroupUnBindChannel(@Body RequestBody body);
}
