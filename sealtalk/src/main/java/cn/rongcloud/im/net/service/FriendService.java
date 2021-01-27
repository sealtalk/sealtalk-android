package cn.rongcloud.im.net.service;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.Map;

import cn.rongcloud.im.db.model.FriendDescription;
import cn.rongcloud.im.db.model.FriendShipInfo;
import cn.rongcloud.im.model.AddFriendResult;
import cn.rongcloud.im.model.GetContactInfoResult;
import cn.rongcloud.im.model.Result;
import cn.rongcloud.im.model.SearchFriendInfo;
import cn.rongcloud.im.net.SealTalkUrl;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

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
     * 忽略好友请求
     *
     * @return
     */
    @POST(SealTalkUrl.INGORE_FRIENDS)
    LiveData<Result<Void>> ingoreFriend(@Body RequestBody body);

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

    /**
     * 搜索好友
     *
     * @param queryMap
     * @return
     */
    @GET(SealTalkUrl.FIND_FRIEND)
    LiveData<Result<SearchFriendInfo>> searchFriend(@QueryMap(encoded = true) Map<String, String> queryMap);

    @POST(SealTalkUrl.DELETE_FREIND)
    LiveData<Result> deleteFriend(@Body RequestBody body);

    /**
     * 获取手机通讯录中的人员信息
     *
     * @param body
     * @return
     */
    @POST(SealTalkUrl.GET_CONTACTS_INFO)
    LiveData<Result<List<GetContactInfoResult>>> getContactsInfo(@Body RequestBody body);

    /**
     * 设置朋友备注和描述
     *
     * @param body
     * @return
     */
    @POST(SealTalkUrl.SET_FRIEND_DESCRIPTION)
    LiveData<Result<Void>> setFriendDescription(@Body RequestBody body);

    @POST(SealTalkUrl.GET_FRIEND_DESCRIPTION)
    LiveData<Result<FriendDescription>> getFriendDescription(@Body RequestBody body);

    /**
     * 批量删除好友
     *
     * @param body
     * @return
     */
    @POST(SealTalkUrl.MULTI_DELETE_FRIEND)
    LiveData<Result> deleteMultiFriend(@Body RequestBody body);
}
