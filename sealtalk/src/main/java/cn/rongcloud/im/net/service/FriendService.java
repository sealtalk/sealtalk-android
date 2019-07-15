package cn.rongcloud.im.net.service;

import androidx.lifecycle.LiveData;

import java.util.List;

import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.model.AddFriendResult;
import cn.rongcloud.im.model.Result;
import cn.rongcloud.im.model.SearchFriendInfo;
import cn.rongcloud.im.net.SealTalkUrl;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FriendService {

    /**
     * 获取所有好友信息
     *
     * @return
     */
    @GET(SealTalkUrl.GET_FRIEND_ALL)
    LiveData<Result<List<FriendShipInfo>>> getAllFriendList();

    /**
     * 获取好友信息
     *
     * @param friendId
     * @return
     */
    @GET(SealTalkUrl.GET_FRIEND_PROFILE)
    LiveData<Result<FriendShipInfo>> getFriendInfo(@Path("friendId") String friendId);

    /**
     * 同意添加好友
     *
     * @return
     */
    @POST(SealTalkUrl.ARGEE_FRIENDS)
    LiveData<Result<Boolean>> agreeFriend(@Body RequestBody body);

    /**
     * 设置好友备注名
     *
     * @param body
     * @return
     */
    @POST(SealTalkUrl.SET_DISPLAY_NAME)
    LiveData<Result> setFriendAlias(@Body RequestBody body);

    /**
     * 申请添加好友
     *
     * @param body
     * @return
     */
    @POST(SealTalkUrl.INVITE_FRIEND)
    LiveData<Result<AddFriendResult>> inviteFriend(@Body RequestBody body);

    @GET(SealTalkUrl.FIND_FRIEND)
    LiveData<Result<SearchFriendInfo>> searchFriend(@Path("region") String region, @Path("phone") String phone);

    @POST(SealTalkUrl.DELETE_FREIND)
    LiveData<Result> deleteFriend(@Body RequestBody body);
}
